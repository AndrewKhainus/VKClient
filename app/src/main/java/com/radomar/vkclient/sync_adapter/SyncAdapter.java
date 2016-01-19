package com.radomar.vkclient.sync_adapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.radomar.vkclient.global.Constants;
import com.radomar.vkclient.interfaces.APIService;
import com.radomar.vkclient.models.NewsModel;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Radomar on 15.01.2016
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 1;
    ContentResolver mContentResolver;

    private static String baseUrl = "https://api.vk.com/" ;

    public static final String AUTHORITY = "com.radomar.vkclient.NewsProvider";
    public static final String ACCOUNT_TYPE = "com.radomar.vkclient";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        VKSdk.customInitialize(context, Constants.VK_API_KEY, String.valueOf(Constants.VK_API_KEY));
        mContentResolver = context.getContentResolver();
        Log.d("sometag", "constructor SyncAdapter");
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        Log.d("sometag", "begin sync");
//        if (VKSdk.isLoggedIn()) {
//            makeResponse();
//        }
        if (VKSdk.getAccessToken().accessToken != null) {
            makeResponse();
        }
    }

    private void makeResponse() {
        Log.d("sometag", "makeResponse");
        Retrofit client = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService apiService = client.create(APIService.class);

            Call<NewsModel> call = apiService.getJSON("post", 5.44, VKAccessToken.currentToken().accessToken);
            call.enqueue(new Callback<NewsModel>() {
                @Override
                public void onResponse(Response<NewsModel> response, Retrofit retrofit) {

                    Log.d("sometag", "Status Code = " + response.code());
                    Log.d("sometag", response.raw().toString());
                    NewsModel newsModel = response.body();
                    Log.d("sometag", "total news = " + newsModel.response.items.size());

                    if (response.code() == 200) {

                    }
                }

                @Override
                public void onFailure(Throwable t) {
                }
            });
    }

    public static void syncImmediately(Context context) {
        Log.d("sometag", "syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putString("method_name", "getJSON");
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                AUTHORITY, bundle);
    }

    public static Account getSyncAccount(Context context) {
        Log.d("sometag", "getSyncAccount");

        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account account = new Account("dummyaccount", ACCOUNT_TYPE);

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(account) ) {

            if (!accountManager.addAccountExplicitly(account, "", null)) {
                Log.d("sometag", "account already exist");

            }

//            onAccountCreated(sAccount, context);
        }
        return account;
    }

    private static void onAccountCreated(Account account, Context context) {
        Log.d("sometag", "onAccountCreated");
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL);

        ContentResolver.setSyncAutomatically(account, AUTHORITY, true);

        syncImmediately(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval) {
        Log.d("sometag", "configurePeriodicSync");
//        Bundle bundle = new Bundle();
//        bundle.putString("method_name", "getJSON");
        ContentResolver.addPeriodicSync(getSyncAccount(context), AUTHORITY, Bundle.EMPTY, syncInterval);

    }

    public static void initializeSyncAdapter(Context context) {
        Log.d("sometag", "initializeSyncAdapter");

        onAccountCreated(getSyncAccount(context), context);

    }


}
