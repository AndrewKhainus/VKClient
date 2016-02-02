package com.radomar.vkclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.radomar.vkclient.sync_adapter.SyncAdapter;

/**
 * Created by Radomar on 01.02.2016
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO string constants2
        Log.d("sometag", "onReceive");
        SyncAdapter.syncImmediatelyAndShare(intent.getStringExtra("extra_message"),
                                            intent.getStringExtra("extra_latitude"),
                                            intent.getStringExtra("extra_longitude"));
    }
}
