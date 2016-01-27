package com.radomar.vkclient.sync_adapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.radomar.vkclient.content_provider.NewsContentProvider;
import com.radomar.vkclient.global.Constants;
import com.radomar.vkclient.interfaces.APIService;
import com.radomar.vkclient.models.AuthorModel;
import com.radomar.vkclient.CustomJsonDeserializer;
import com.radomar.vkclient.models.Model;
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
public class SyncAdapter extends AbstractThreadedSyncAdapter implements Callback<Model>{
//TODO refactor code
    private static final int SYNC_INTERVAL = 3;

    private static String baseUrl = "https://api.vk.com/" ;

    private static Account sAccount;

    private String mStartFrom;

    public static final String AUTHORITY = "com.radomar.vkclient.NewsProvider";
    public static final String ACCOUNT_TYPE = "com.radomar.vkclient";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        VKSdk.customInitialize(context, Constants.VK_API_KEY, String.valueOf(Constants.VK_API_KEY));

        mStartFrom = getStartFrom();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        if (VKSdk.isLoggedIn()) {
            if (extras.getString("immediately") != null) {
                if (extras.getString("immediately").equals("getNews") && getStartFrom() == null) {
                    Log.d("sometag", "SYNC IMMEDIATELY");
                    downloadMoreOldNews();
                }
                if (extras.getString("immediately").equals("downloadAnyway")) {
                    downloadMoreOldNews();
                }
                if (extras.getString("immediately").equals("swipeToRefresh")) {
                    downloadLatestNews();
                }
            }

            if (extras.getString("method_name2") != null) {
                if (extras.getString("method_name2").equals("getJSON")) {
                    Log.d("sometag", "SYNC PERIODICALY");
                    downloadLatestNews();
                }
            }
        }
    }

    private void downloadLatestNews() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Model.class, new CustomJsonDeserializer())
                .create();

        Retrofit client = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        APIService apiService = client.create(APIService.class);
        long firstPublishTime = getFirstPublishTime();
        if (firstPublishTime != 0) {
            firstPublishTime += 1;
        }

        Call<Model> call = apiService.newQuery("post", 5.44,
                firstPublishTime,
                10,
                VKAccessToken.currentToken().accessToken);

        call.enqueue(this);
    }

    private void downloadMoreOldNews() {
        Log.d("sometag", "downloadMoreOldNews");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Model.class, new CustomJsonDeserializer())
                .create();

        Retrofit client = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        APIService apiService = client.create(APIService.class);

        Call<Model> call = apiService.getOlderNews("post", 5.44, mStartFrom, 10, VKAccessToken.currentToken().accessToken);
        call.enqueue(this);
    }

    public static void initSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account account = new Account("dummyaccount", ACCOUNT_TYPE);
        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(account) ) {
            if (!accountManager.addAccountExplicitly(account, "", null)) {
                Log.d("sometag", "account already exist");
            }
        }
        sAccount = account;
    }

    private static void onAccountCreated() {
        Log.d("sometag", "onAccountCreated");
        configurePeriodicSync();

        ContentResolver.setSyncAutomatically(sAccount, AUTHORITY, true);
//        Log.d("sometag", "end time ------- " + endTime);
//        Log.d("sometag", "end time ------- " + getStartFrom());
        syncImmediately();
    }

    public static void configurePeriodicSync() {
        Log.d("sometag", "configurePeriodicSync");
        Bundle bundle = new Bundle();
        bundle.putString("method_name2", "getJSON");
        ContentResolver.addPeriodicSync(sAccount, AUTHORITY, bundle, SYNC_INTERVAL);
    }

    public static void syncImmediately() {
        Log.d("sometag", "syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putString("immediately", "getNews");
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(sAccount,
                AUTHORITY, bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        initSyncAccount(context);
        onAccountCreated();
    }

    public static void anywaySyncImmediately() {
        Log.d("sometag", "syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putString("immediately", "downloadAnyway");
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(sAccount,
                AUTHORITY, bundle);
    }

    public static void syncBySwipeToRefresh() {
        Log.d("sometag", "syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putString("immediately", "swipeToRefresh");
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(sAccount,
                AUTHORITY, bundle);
    }

    private long getFirstPublishTime() {
        String[] projection = {NewsContentProvider.PUBLISH_TIME};
        long time = 0;
        Cursor cursor = getContext().getContentResolver().query(NewsContentProvider.NEWS_CONTENT_URI, projection, null, null, NewsContentProvider.PUBLISH_TIME);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                time = cursor.getLong(cursor.getColumnIndex(NewsContentProvider.PUBLISH_TIME));
            }
            cursor.close();
        }
        return time;
    }

    private void writeDataToDB(Model data) {
        Log.d("sometag", "write to DB");

//  write to NEWS table
        for (NewsModel newsModel: data.newsList) {
            ContentValues values = new ContentValues();
            values.put(NewsContentProvider.NEWS_TEXT, newsModel.text);
            Log.d("sometag", "write publish time = " + newsModel.date);
            values.put(NewsContentProvider.PUBLISH_TIME, newsModel.date);
            values.put(NewsContentProvider.LIKE, newsModel.like);
            values.put(NewsContentProvider.SOURCE_ID, newsModel.sourceId);
            values.put(NewsContentProvider.REPOST, newsModel.repost);
            values.put(NewsContentProvider.COMMENTS, newsModel.comments);
            values.put(NewsContentProvider.IMAGE_URL, newsModel.photoUrl);
            getContext().getContentResolver().insert(NewsContentProvider.NEWS_CONTENT_URI, values);
        }

//  write to AUTHORS table
        for (AuthorModel authorModel: data.authorsList) {
           Cursor c = getContext().getContentResolver().query(NewsContentProvider.AUTHORS_CONTENT_URI, null, "source_id = " + authorModel.sourceId, null, null);
            if (c != null) {
                if (c.getCount() == 0) {
                    ContentValues values = new ContentValues();
                    values.put(NewsContentProvider.SOURCE_ID, authorModel.sourceId);
                    values.put(NewsContentProvider.AUTHOR_NAME, authorModel.name);
                    values.put(NewsContentProvider.PHOTO_URL, authorModel.photoUrl);
                    getContext().getContentResolver().insert(NewsContentProvider.AUTHORS_CONTENT_URI, values);
                    Log.d("sometag", "write author");
                }
                c.close();
            }
        }

//  write to START_FROM table
        if (data.startFrom != null) {
            ContentValues values = new ContentValues();
            values.put(NewsContentProvider.START_FROM, data.startFrom);
            getContext().getContentResolver().insert(NewsContentProvider.START_FROM_CONTENT_URI, values);
            mStartFrom = data.startFrom;
        }

    }

    private String getStartFrom() {
        String[] projection = {NewsContentProvider.START_FROM};
        String startFrom = null;
        Cursor cursor = getContext().getContentResolver().query(NewsContentProvider.START_FROM_CONTENT_URI, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToLast()) {
                startFrom = cursor.getString(cursor.getColumnIndex(NewsContentProvider.START_FROM));
            }
            cursor.close();
        }
        return startFrom;
    }

    @Override
    public void onResponse(Response<Model> response, Retrofit retrofit) {
        Model model = response.body();
        Log.d("sometag", response.raw().toString());
        if (response.code() == 200) {
            writeDataToDB(model);
        }
    }

    @Override
    public void onFailure(Throwable t) {

    }


}
