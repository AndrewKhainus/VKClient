package com.radomar.vkclient.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.Loader;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.app.LoaderManager.LoaderCallbacks;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.radomar.vkclient.R;
import com.radomar.vkclient.content_provider.NewsContentProvider;
import com.radomar.vkclient.global.Constants;
import com.radomar.vkclient.loader.ImageLoader;
import com.radomar.vkclient.sync_adapter.SyncAdapter;

/**
 * Created by Radomar on 27.01.2016
 */
public class ShareDialog extends DialogFragment implements View.OnClickListener,
                                                           GoogleApiClient.ConnectionCallbacks,
                                                           GoogleApiClient.OnConnectionFailedListener,
                                                           LocationListener, LoaderCallbacks<Bitmap> {

    private Button mBtCancel;
    private Button mBtShare;
    private Button mBtAddLocation;

    private EditText mEtMessage;
    private ImageView mIvSelectedImage;
    private Uri mImageUri;
    private TextView mTvSelectedImage;
    private TextView mTvLocation;

    private Bundle mLoaderBundle;

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
        View view = inflater.inflate(R.layout.dialog_fragment, null);

        findViews(view);
        setListeners();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initImageLoader();
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
                mTvSelectedImage.setText(mImageUri.toString());
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
            mTvSelectedImage.setText(mImageUri.toString());

            restartImageLoader();
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
        mTvSelectedImage = (TextView) view.findViewById(R.id.tvSelectedImage_DF);
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
                if (isOnline()) {
                    shareContent();
                } else {
                    writeToDb();
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
            return;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
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
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }

    @Override
    public void onConnectionFailed(@Nullable ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void writeToDb() {
        if (!(mEtMessage.getText().toString().equals("") && mImageUri == null)) {

            ContentValues values = new ContentValues();
            values.put(NewsContentProvider.SHARED_IMAGE_URL, mTvSelectedImage.getText().toString());
            values.put(NewsContentProvider.SHARED_MESSAGE, mEtMessage.getText().toString());
            values.put(NewsContentProvider.LATITUDE, mLat);
            values.put(NewsContentProvider.LONGITUDE, mLong);

            getActivity().getContentResolver().insert(NewsContentProvider.SHARE_CONTENT_URI, values);
        }
    }

    private void initImageLoader() {
        initBundleForLoader();
        getLoaderManager().initLoader(Constants.IMAGE_LOADER_ID, mLoaderBundle, this);
    }

    private void restartImageLoader() {
        initBundleForLoader();
        getLoaderManager().restartLoader(Constants.IMAGE_LOADER_ID, mLoaderBundle, this);
    }


    private void initBundleForLoader() {
        mLoaderBundle = new Bundle();
        mLoaderBundle.putParcelable(Constants.LOADER_URI_KEY, mImageUri);
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        Loader<Bitmap> mLoader = null;

        if (id == Constants.IMAGE_LOADER_ID) {
            mLoader = new ImageLoader(getActivity(), args);
        }
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
        mIvSelectedImage.setImageBitmap(data);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {

    }
}
