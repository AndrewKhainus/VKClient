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
import android.content.SharedPreferences.Editor;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.radomar.vkclient.R;
import com.radomar.vkclient.utils.RestClient;
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

    private static final String TAG = "sometag";
    private static final int SYNC_INTERVAL = 15;

    private static Account sAccount;

    private String mStartFrom;

    private SharedPreferences mSharedPreferences;

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

        if (VKSdk.isLoggedIn() && actionType != null) {
            if ((actionType.equals(Constants.GET_NEWS_PARAM) && getStartFrom() == null) || actionType.equals(Constants.DOWNLOAD_ANYWAY_PARAM)) {
                downloadMoreOldNews();
            }
            if (actionType.equals(Constants.SWIPE_REFRESH_PARAM)) {
                downloadLatestNews();
            }
            if (actionType.equals(Constants.SHARE_DATA_PARAM)) {
                Uri uri = null;
                try {
                    uri = Uri.parse(extras.getString("image_uri"));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                shareData(extras.getString("message"), uri, extras.getString("latitude"), extras.getString("longitude"));
            }

            shareOfflineMessages();
        }
    }

    @Override
    public void onResponse(Response<Model> response, Retrofit retrofit) {
        Model model = response.body();
        Log.d(TAG, response.raw().toString());
        if (response.code() == 200) {
            writeDataToDB(model);
        }
    }

    @Override
    public void onFailure(Throwable t) {
    }

    private void downloadLatestNews() {

        APIService apiService = RestClient.getInstance().getAPIService();
        long firstPublishTime = getFirstPublishTime();
        if (firstPublishTime != 0) {
            firstPublishTime += 1;
        }

        Call<Model> call = apiService.newQuery("post",
                Constants.VERSION,
                firstPublishTime,
                Constants.COUNT,
                VKAccessToken.currentToken().accessToken);

        call.enqueue(this);
    }

    private void downloadMoreOldNews() {
        APIService apiService = RestClient.getInstance().getAPIService();

        Call<Model> call = apiService.getOlderNews("post",
                Constants.VERSION,
                mStartFrom,
                Constants.COUNT,
                VKAccessToken.currentToken().accessToken);
        call.enqueue(this);
    }

    public static void initSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account account = new Account("dummyaccount", ACCOUNT_TYPE);

        if ( null == accountManager.getPassword(account) ) {
            if (!accountManager.addAccountExplicitly(account, "", null)) {
                Log.d(TAG, "account already exist");
            }
        }
        sAccount = account;
    }

    private static void onAccountCreated() {
        configurePeriodicSync();

        ContentResolver.setSyncAutomatically(sAccount, AUTHORITY, true);
        syncImmediately(Constants.GET_NEWS_PARAM, null, null, null, null);
    }

    public static void configurePeriodicSync() {
        Log.d(TAG, "configurePeriodicSync");
        Bundle bundle = new Bundle();
        bundle.putString("immediately", Constants.SWIPE_REFRESH_PARAM);
        ContentResolver.addPeriodicSync(sAccount, AUTHORITY, bundle, SYNC_INTERVAL);
    }

    public static void initializeSyncAdapter(Context context) {
        initSyncAccount(context);

        onAccountCreated();
    }

    public static void syncImmediately(String param, @Nullable String text, @Nullable Uri uri, @Nullable String latitude, @Nullable String longitude) {
        Log.d(TAG, "syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putString("immediately", param);
        if (uri != null) {
            bundle.putString("image_uri", uri.toString());
        }
        if (text != null) {
            bundle.putString("message", text);
        }
        if (latitude != null) {
            bundle.putString("latitude", latitude);
        }
        if (longitude != null) {
            bundle.putString("longitude", latitude);
        }
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
        Log.d(TAG, "write to DB");

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
            Log.d(TAG, "write latitude = " + newsModel.latitude);
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
                    Log.d(TAG, "write author");
                }
                c.close();
            }
        }

//  write to shared preferences
        if (data.startFrom != null) {
            mSharedPreferences = getContext().getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
            Editor ed = mSharedPreferences.edit();
            ed.putString(Constants.START_FROM, data.startFrom);
            ed.apply();
            mStartFrom = data.startFrom;
        }

    }

    private String getStartFrom() {
        mSharedPreferences = getContext().getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(Constants.START_FROM, null);
    }

    private void shareData(String message, @Nullable Uri uri, String latitude, String longitude) {
        if (uri != null) {
            retrieveUploadServerUrl(message, uri, latitude, longitude);
        } else {
            wallPost(message, null, latitude, longitude, null);
        }
    }

    private void retrieveUploadServerUrl(final String message, final Uri uri, final String latitude, final String longitude) {
        Log.d(TAG, "retrieveUploadServerUrl");
        APIService apiService = RestClient.getInstance().getAPIService();
        Call<UploadServer> call = apiService.getUploadUrl(VKAccessToken.currentToken().accessToken);
        call.enqueue(new Callback<UploadServer>() {
            @Override
            public void onResponse(Response<UploadServer> response, Retrofit retrofit) {
                if (response.code() == 200) {
                    uploadPhoto(response.body(), message, uri, latitude, longitude);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "onFailure retrieveUploadServerUrl");
            }
        });
    }

    private void uploadPhoto(UploadServer uploadServer, final String message, final Uri uri, final String latitude, final String longitude ) {
        Log.d(TAG, "uploadPhoto");
        APIService apiService = RestClient.getInstance().getAPIService();
        File file = new File(getRealPathFromURI(uri));
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        Call<SavePhoto> uploadPhotoCall = apiService.uploadPhoto(uploadServer.uploadUrl, body);
        uploadPhotoCall.enqueue(new Callback<SavePhoto>() {
            @Override
            public void onResponse(Response<SavePhoto> response, Retrofit retrofit) {
                if (response.code() == 200) {
                    retrievePhotoId(response.body(), message, uri, latitude, longitude);
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    private void retrievePhotoId(SavePhoto savePhoto, final String message, final Uri uri,final String latitude, final String longitude) {
        Log.d(TAG, "retrievePhotoId");
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
                    wallPost(message, uri, latitude, longitude, response.body().id);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "onFailure retrievePhotoId");
            }
        });
    }

    private void wallPost(final String message, final Uri uri, final String latitude, final String longitude, @Nullable String photoId) {
        Log.d(TAG, "wallPost");
        APIService apiService = RestClient.getInstance().getAPIService();
        Call<FinalResponse> call = apiService.shareQuery(VKAccessToken.currentToken().userId,
                0,
                message,
                photoId,
                latitude,
                longitude,
                VKAccessToken.currentToken().accessToken);
        call.enqueue(new Callback<FinalResponse>() {
            @Override
            public void onResponse(Response<FinalResponse> response, Retrofit retrofit) {
                Log.d(TAG, response.raw().toString());
                Log.d(TAG, "onResponce");

            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "onFailure wallPost");
                createNotification(message, uri, latitude, longitude);
            }
        });
    }

    private void createNotification(String message, Uri uri, String latitude, String longitude) {
        Log.d(TAG, "createNotification");

        Intent intent = new Intent();
        intent.setAction("com.radomar.vkclient.share_again");
        intent.putExtra(Constants.EXTRA_MESSAGE, message);
        intent.putExtra(Constants.EXTRA_URI, uri);
        intent.putExtra(Constants.EXTRA_LAT, latitude);
        intent.putExtra(Constants.EXTRA_LONG, longitude);

        PendingIntent contentIntent = PendingIntent.getBroadcast(getContext(), (int)System.currentTimeMillis(), intent, 0);

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

    private void shareOfflineMessages() {
        Cursor cursor = getContext().getContentResolver().query(NewsContentProvider.SHARE_CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
//                    Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(NewsContentProvider.SHARED_IMAGE_URL)));
                    shareData(cursor.getString(cursor.getColumnIndex(NewsContentProvider.SHARED_MESSAGE)),
                            Uri.parse(cursor.getString(cursor.getColumnIndex(NewsContentProvider.SHARED_IMAGE_URL))),
                            cursor.getString(cursor.getColumnIndex(NewsContentProvider.LATITUDE)),
                            cursor.getString(cursor.getColumnIndex(NewsContentProvider.LONGITUDE)));
                } while (cursor.moveToNext());
            }
            getContext().getContentResolver().delete(NewsContentProvider.SHARE_CONTENT_URI,
                    null,
                    null);
            cursor.close();
        }
    }

}
