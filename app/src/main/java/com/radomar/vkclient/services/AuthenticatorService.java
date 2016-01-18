package com.radomar.vkclient.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.radomar.vkclient.Authenticator;

/**
 * Created by Radomar on 16.01.2016
 */
public class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
