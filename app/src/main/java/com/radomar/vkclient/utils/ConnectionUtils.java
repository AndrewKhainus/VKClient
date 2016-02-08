package com.radomar.vkclient.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Radomar on 08.02.2016
 */
public class ConnectionUtils {

    private static ConnectionUtils mInstance;

    private ConnectionUtils() {
    }

    public static ConnectionUtils getInstance() {
        if (mInstance == null) {
            mInstance = new ConnectionUtils();
        }
        return mInstance;
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
