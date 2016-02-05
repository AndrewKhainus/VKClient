package com.radomar.vkclient.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.radomar.vkclient.global.Constants;
import com.radomar.vkclient.sync_adapter.SyncAdapter;

/**
 * Created by Radomar on 01.02.2016
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO string constants2
        Log.d("sometag", "onReceive");
        SyncAdapter.syncImmediately(Constants.SHARE_DATA_PARAM,
                                    intent.getStringExtra("extra_message"),
                                    (Uri) intent.getParcelableExtra("extra_uri"),
                                    intent.getStringExtra("extra_latitude"),
                                    intent.getStringExtra("extra_longitude"));
    }
}
