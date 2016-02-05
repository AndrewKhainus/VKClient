package com.radomar.vkclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.radomar.vkclient.global.Constants;

/**
 * Created by Radomar on 29.01.2016
 */
public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int ZOOM = 14;

    private double mLatitude;
    private double mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_map_activity);

        Bundle extras = getIntent().getExtras();
        mLatitude = Double.parseDouble(extras.getString(Constants.KEY_LATITUDE));
        mLongitude = Double.parseDouble(extras.getString(Constants.KEY_LONGITUDE));

        initMap();
    }

    private void initMap() {
        final MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragmentGoogleMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (googleMap == null) {
            return;
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude, mLongitude), ZOOM));

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLatitude, mLongitude)));

    }
}
