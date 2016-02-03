package com.radomar.vkclient.sync_adapter;

import android.accounts.Account;
import android.accounts.AccountManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.radomar.vkclient.R;
import com.radomar.vkclient.RestClient;
import com.radomar.vkclient.content_provider.NewsContentProvider;
import com.radomar.vkclient.global.Constants;
import com.radomar.vkclient.interfaces.APIService;
import com.radomar.vkclient.models.AuthorModel;
import com.radomar.vkclient.models.FinalResponse;
import com.radomar.vkclient.models.Model;
import com.radomar.vkclient.models.NewsModel;
import com.radomar.vkclient.models.PhotoModel;
import com.radomar.vkclient.models.SavePhoto;
import com.radomar.vkclient.models.UploadServer;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

import java.io.File;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Radomar on 15.01.2016
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter implements Callback<Model> {
//TODO refactor code
    private static final int SYNC_INTERVAL = 10;

    private static String baseUrl = "https://api.vk.com/" ;

    private static Account sAccount;

    private String mStartFrom;

    private String mMessage;
    private String mLat;
    private String mLang;
    private String photoId;
    private Uri mUri;

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

        String actionType = extras.getString("immediately");
        if (VKSdk.isLoggedIn() && isOnline()) {
            if (actionType != null) {
                if (actionType.equals("getNews") && getStartFrom() == null) {
                    Log.d("sometag", "SYNC IMMEDIATELY");
                    downloadMoreOldNews();
                }
                if (actionType.equals("downloadAnyway")) {
                    downloadMoreOldNews();
                }
                if (actionType.equals("swipeToRefresh")) {
                    downloadLatestNews();
                }
                if (actionType.equals("shareData")) {
                    mMessage = extras.getString("message");
                    mLat = extras.getString("latitude");
                    mLang = extras.getString("longitude");
                    mUri = Uri.parse(extras.getString("image_uri"));

                    shareData();
                }
            }

            actionType = extras.getString("method_name2");
            if (actionType != null) {
                if (actionType.equals("getJSON")) {
                    downloadLatestNews();
                }
            }

            Cursor cursor = getContext().getContentResolver().query(NewsContentProvider.SHARE_CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
//                        TODO share data from cursor
//                        shareContent(cursor.getString(cursor.getColumnIndex(NewsContentProvider.SHARED_MESSAGE)),
//                                cursor.getString(cursor.getColumnIndex(NewsContentProvider.LATITUDE)),
//                                cursor.getString(cursor.getColumnIndex(NewsContentProvider.LONGITUDE)));

//                        getContext().getContentResolver().delete(NewsContentProvider.SHARE_CONTENT_URI,
//                                NewsContentProvider.ID + "=" + cursor.getPosition(),
//                                null);
                    } while (cursor.moveToNext());
                }
                cursor.close();

            }

        }

        if (VKSdk.isLoggedIn() && !isOnline()) {
            if (actionType != null && actionType.equals("shareData")) {
                ContentValues values = new ContentValues();
//                values.put(NewsContentProvider.SHARED_IMAGE_URL, extras.getString(""));
                values.put(NewsContentProvider.SHARED_MESSAGE, extras.getString("message"));
                values.put(NewsContentProvider.LATITUDE, extras.getString("latitude"));
                values.put(NewsContentProvider.LONGITUDE, extras.getString("longitude"));

                getContext().getContentResolver().insert(NewsContentProvider.SHARE_CONTENT_URI, values);
            }
        }
    }

    private void downloadLatestNews() {

        APIService apiService = RestClient.getInstance().getAPIService();
        long firstPublishTime = getFirstPublishTime();
        if (firstPublishTime != 0) {
            firstPublishTime += 1;
        }

        Call<Model> call = apiService.newQuery("post",
                                                5.44,
                                                firstPublishTime,
                                                10,
                                                VKAccessToken.currentToken().accessToken);

        call.enqueue(this);
    }

    private void downloadMoreOldNews() {
        APIService apiService = RestClient.getInstance().getAPIService();

        Call<Model> call = apiService.getOlderNews("post",
                                                    5.44,
                                                    mStartFrom,
                                                    10,
                                                    VKAccessToken.currentToken().accessToken);
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

            values.put(NewsContentProvider.PUBLISH_TIME, newsModel.date);
            values.put(NewsContentProvider.LIKE, newsModel.like);
            values.put(NewsContentProvider.SOURCE_ID, newsModel.sourceId);
            values.put(NewsContentProvider.REPOST, newsModel.repost);
            values.put(NewsContentProvider.COMMENTS, newsModel.comments);
            values.put(NewsContentProvider.IMAGE_URL, newsModel.photoUrl);
            values.put(NewsContentProvider.LATITUDE, newsModel.latitude);
            values.put(NewsContentProvider.LONGITUDE, newsModel.longitude);
            Log.d("sometag", "write latitude = " + newsModel.latitude);
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

    private void shareData() {
        if (mUri != null) {
            retrieveUploadServerUrl();
        } else {
            wallPost();
        }
    }

    private void retrieveUploadServerUrl() {
        Log.d("sometag", "retrieveUploadServerUrl");
        APIService apiService = RestClient.getInstance().getAPIService();
        Call<UploadServer> call = apiService.getUploadUrl(VKAccessToken.currentToken().accessToken);
        call.enqueue(new Callback<UploadServer>() {
            @Override
            public void onResponse(Response<UploadServer> response, Retrofit retrofit) {
                if (response.code() == 200) {
                    uploadPhoto(response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("sometag", "onFailure retrieveUploadServerUrl");
            }
        });
    }

    private void uploadPhoto(UploadServer uploadServer) {
        Log.d("sometag", "uploadPhoto");
        APIService apiService = RestClient.getInstance().getAPIService();
        File file = new File(getRealPathFromURI(mUri));
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        Call<SavePhoto> uploadPhotoCall = apiService.uploadPhoto(uploadServer.uploadUrl, body);
        uploadPhotoCall.enqueue(new Callback<SavePhoto>() {
            @Override
            public void onResponse(Response<SavePhoto> response, Retrofit retrofit) {
                if (response.code() == 200) {
                    retrievePhotoId(response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    private void retrievePhotoId(SavePhoto savePhoto) {
        Log.d("sometag", "retrievePhotoId");
        APIService apiService = RestClient.getInstance().getAPIService();

        Call<PhotoModel> call = apiService.saveWallPhoto(VKAccessToken.currentToken().userId,
                savePhoto.photo,
                savePhoto.server,
                savePhoto.hash,
                VKAccessToken.currentToken().accessToken);
        call.enqueue(new Callback<PhotoModel>() {
            @Override
            public void onResponse(Response<PhotoModel> response, Retrofit retrofit) {
                if (response.code() == 200) {
                    photoId = response.body().id;
                    wallPost();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("sometag", "onFailure retrievePhotoId");
            }
        });
    }

    private void wallPost() {
        Log.d("sometag", "wallPost");
        APIService apiService = RestClient.getInstance().getAPIService();
        Call<FinalResponse> call = apiService.shareQuery(VKAccessToken.currentToken().userId,
                                                 0,
                                                 mMessage,
                                                 photoId,
                                                 mLat,
                                                 mLang,
                                                 VKAccessToken.currentToken().accessToken);
        call.enqueue(new Callback<FinalResponse>() {
            @Override
            public void onResponse(Response<FinalResponse> response, Retrofit retrofit) {
                Log.d("sometag", "onResponce");
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("sometag", "onFailure wallPost");
                createNotification(mMessage, mLat, mLang);
            }
        });
    }

    private void createNotification(String message, String latitude, String longitude) {
        Log.d("sometag", "createNotification");

        Intent intent = new Intent();
        intent.setAction("com.radomar.vkclient.share_again");
        intent.putExtra("extra_message", message);
        intent.putExtra("extra_latitude", latitude);
        intent.putExtra("extra_longitude", longitude);
//TODO string constants

        PendingIntent contentIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.
                setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                .setAutoCancel(true)
                .setContentTitle("failed")
                .setContentText(message);

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

    public static void syncImmediatelyAndShare(String text, Uri uri, String latitude, String longitude) {
        Log.d("sometag", "syncImmediatelyAndShare");
        Bundle bundle = new Bundle();
        bundle.putString("immediately", "shareData");
        bundle.putString("message", text);
        bundle.putString("latitude", latitude);
        bundle.putString("longitude", longitude);
        if (uri != null) {
            bundle.putString("image_uri", uri.toString());
        }
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(sAccount,
                AUTHORITY, bundle);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContext().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

}
