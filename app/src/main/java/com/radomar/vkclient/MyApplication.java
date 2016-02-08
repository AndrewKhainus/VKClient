package com.radomar.vkclient;

import android.app.Application;

import com.radomar.vkclient.global.Constants;
import com.vk.sdk.VKSdk;

/**
 * Created by Radomar on 08.02.2016
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.customInitialize(getApplicationContext(), Constants.VK_API_KEY, String.valueOf(Constants.VK_API_KEY));
    }

}
