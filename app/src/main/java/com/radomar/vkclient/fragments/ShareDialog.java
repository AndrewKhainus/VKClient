package com.radomar.vkclient.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.radomar.vkclient.R;
import com.radomar.vkclient.global.Constants;
import com.radomar.vkclient.loader.DatabaseLoader;
import com.radomar.vkclient.sync_adapter.SyncAdapter;
import com.radomar.vkclient.utils.ConnectionUtils;
import com.radomar.vkclient.utils.FileUtils;

/**
 * Created by Radomar on 27.01.2016
 */
public class ShareDialog extends DialogFragment implements View.OnClickListener,
                                                            ConnectionCallbacks,
                                                            OnConnectionFailedListener,
                                                            LocationListener,
                                                            LoaderCallbacks<Void>{

    private static final int INTERVAL = 1000 * 10;
    private static final int FASTEST_INTERVAL = 1000;

    private Button mBtCancel;
    private Button mBtShare;
    private Button mBtAddLocation;

    private EditText mEtMessage;
    private ImageView mIvSelectedImage;
    private Uri mImageUri;
    private String mSelectedImage;
    private TextView mTvLocation;

    private double mLatitude;
    private double mLongitude;
    private String mLat;
    private String mLong;

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);

        connectToPlayServices();
        initLocationRequest();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment, container, false);

        findViews(view);
        setListeners();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mImageUri = savedInstanceState.getParcelable(Constants.KEY_URI);
            if (mImageUri != null) {
                mSelectedImage = mImageUri.toString();
            }

            mLatitude = savedInstanceState.getDouble(Constants.KEY_LAT);
            mLongitude = savedInstanceState.getDouble(Constants.KEY_LONG);
            mTvLocation.setText(String.format("%f\n%f", mLatitude, mLongitude));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mSelectedImage = mImageUri.toString();
            Glide.with(getActivity()).
                    load(FileUtils.getInstance().getRealPathFromURI(getActivity(), mImageUri)).
                    into(mIvSelectedImage);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mImageUri != null) {
            outState.putParcelable(Constants.KEY_URI, mImageUri);
        }
        if (mTvLocation != null) {
            outState.putDouble(Constants.KEY_LAT, mLatitude);
            outState.putDouble(Constants.KEY_LONG, mLongitude);
        }
    }

    private void findViews(View view) {
        mBtAddLocation = (Button) view.findViewById(R.id.btLocation_DF);
        mBtCancel = (Button) view.findViewById(R.id.btCancel_DF);
        mBtShare = (Button) view.findViewById(R.id.btShare_DF);
        mEtMessage = (EditText) view.findViewById(R.id.etMessage_DF);
        mIvSelectedImage = (ImageView) view.findViewById(R.id.ivImage_DF);
        mTvLocation = (TextView) view.findViewById(R.id.tvLocation_DF);
    }

    private void setListeners() {
        mBtAddLocation.setOnClickListener(this);
        mBtCancel.setOnClickListener(this);
        mBtShare.setOnClickListener(this);
        mIvSelectedImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btCancel_DF:
                dismiss();
                break;
            case R.id.btShare_DF:
                if (ConnectionUtils.getInstance().isOnline(getActivity())) {
                    shareContent();
                } else {
                    if (!(mEtMessage.getText().toString().equals("") && mImageUri == null)) {
                        initLoader();

                        Intent intent = new Intent();
                        intent.putExtra(Constants.DIALOG_TAG, true);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    }
                }
                dismiss();
                break;
            case R.id.ivImage_DF:
                addImage();
                break;
            case R.id.btLocation_DF:
                mLat = String.valueOf(mLatitude);
                mLong = String.valueOf(mLongitude);
                mTvLocation.setText(String.format("%s\n%s", mLat, mLong));
                break;
        }
    }

    private void addImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.PICK_IMAGE);
    }

    private void shareContent() {
        if (!(mEtMessage.getText().toString().equals("") && mImageUri == null)) {
            SyncAdapter.syncImmediately(Constants.SHARE_DATA_PARAM,
                    mEtMessage.getText().toString(),
                    mImageUri,
                    mLat,
                    mLong);
        } else {
            Log.d("sometag", "no action");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.PERMISSIONS_REQUEST_CODE);
            return;
        }

        updateLocation();
    }

    @SuppressWarnings("ResourceType")
    @RequiresPermission(anyOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void updateLocation() {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (lastLocation != null) {
            mLatitude = lastLocation.getLatitude();
            mLongitude = lastLocation.getLongitude();
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    private void connectToPlayServices() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void initLocationRequest() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
    }

    @Override
    public void onConnectionFailed(@Nullable ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    private void initLoader() {
        getLoaderManager().initLoader(Constants.DATABASE_LOADER_ID, null, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("sometag", "onRequestPermissionsResult");
        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                }
                break;
        }
    }

    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.BUNDLE_KEY_URI, mSelectedImage);
        bundle.putString(Constants.BUNDLE_KEY_MESSAGE, mEtMessage.getText().toString());
        bundle.putString(Constants.BUNDLE_KEY_LAT, mLat);
        bundle.putString(Constants.BUNDLE_KEY_LONG, mLong);
        return new DatabaseLoader(getActivity(), bundle);
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {
    }
}
