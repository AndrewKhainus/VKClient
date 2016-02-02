package com.radomar.vkclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.radomar.vkclient.fragments.VKFragment;
import com.radomar.vkclient.global.Constants;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startVKFragment();

//        checkGPS();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void startVKFragment() {
        VKFragment vkFragment = new VKFragment();
        getFragmentManager().beginTransaction().replace(R.id.container_AM,
                vkFragment, Constants.TAG_FRAGMENT_VK).commit();
    }

//    private void checkGPS() {
//        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            Toast.makeText(this, "GPS is Enabled", Toast.LENGTH_SHORT).show();
//        }else{
//            showGPSDisabledAlertToUser();
//        }
//    }

//    private void showGPSDisabledAlertToUser(){
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
//                .setCancelable(false)
//                .setPositiveButton("Goto Settings Page To Enable GPS",
//                        new DialogInterface.OnClickListener(){
//                            public void onClick(DialogInterface dialog, int id){
//                                Intent callGPSSettingIntent = new Intent(
//                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                                startActivity(callGPSSettingIntent);
//                            }
//                        });
//        alertDialogBuilder.setNegativeButton("Cancel",
//                new DialogInterface.OnClickListener(){
//                    public void onClick(DialogInterface dialog, int id){
//                        dialog.cancel();
//                    }
//                });
//        AlertDialog alert = alertDialogBuilder.create();
//        alert.show();
//    }



}
