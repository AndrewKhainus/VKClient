package com.radomar.vkclient.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telecom.Connection;
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
import com.radomar.vkclient.global.Constants;
import com.radomar.vkclient.sync_adapter.SyncAdapter;

/**
 * Created by Radomar on 27.01.2016
 */
public class ShareDialog extends DialogFragment implements View.OnClickListener,
                                                           GoogleApiClient.ConnectionCallbacks,
                                                           GoogleApiClient.OnConnectionFailedListener,
                                                           LocationListener{

    private Button mBtCancel;
    private Button mBtShare;
    private Button mBtAddLocation;

    private EditText mEtMessage;
    private ImageView mIvImage;
    private Uri mImageUri;
    private TextView mTvSelectedImage;
    private TextView mTvLocation;

    private double mLatitude;
    private double mLongitude;

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
            mImageUri = savedInstanceState.getParcelable("key");
            if (mImageUri != null) {
                mTvSelectedImage.setText(mImageUri.toString());
            }

            mLatitude = savedInstanceState.getDouble("latitude");
            mLongitude = savedInstanceState.getDouble("longitude");
            mTvLocation.setText(String.format("%f\n%f", mLatitude, mLongitude));
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mTvSelectedImage.setText(mImageUri.toString());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mImageUri != null) {
            outState.putParcelable("key", mImageUri);
        }
        if (mTvLocation != null) {
            outState.putDouble("latitude", mLatitude);
            outState.putDouble("longitude", mLongitude);
        }
    }

    private void findViews(View view) {
        mBtAddLocation = (Button) view.findViewById(R.id.btLocation_DF);
        mBtCancel = (Button) view.findViewById(R.id.btCancel_DF);
        mBtShare = (Button) view.findViewById(R.id.btShare_DF);
        mEtMessage = (EditText) view.findViewById(R.id.etMessage_DF);
        mIvImage = (ImageView) view.findViewById(R.id.ivImage_DF);
        mTvSelectedImage = (TextView) view.findViewById(R.id.tvSelectedImage_DF);
        mTvLocation = (TextView) view.findViewById(R.id.tvLocation_DF);
    }

    private void setListeners() {
        mBtAddLocation.setOnClickListener(this);
        mBtCancel.setOnClickListener(this);
        mBtShare.setOnClickListener(this);
        mIvImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btCancel_DF:
                dismiss();
                break;
            case R.id.btShare_DF:
                shareContent();
                dismiss();
                break;
            case R.id.ivImage_DF:
                addImage();
                break;
            case R.id.btLocation_DF:
                mTvLocation.setText(String.format("%f\n%f", mLatitude, mLongitude));
                break;
        }
    }

    private void addImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.PICK_IMAGE);
    }

    private void shareContent() {
        if ( !(mEtMessage.getText().toString().equals("") && mImageUri == null)) {
            SyncAdapter.syncImmediatelyAndShare(mEtMessage.getText().toString(),
                                                mImageUri,
                                                String.valueOf(mLatitude),
                                                String.valueOf(mLongitude));

//            SyncAdapter.sharePhoto(getRealPathFromURI(mImageUri));
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


}
