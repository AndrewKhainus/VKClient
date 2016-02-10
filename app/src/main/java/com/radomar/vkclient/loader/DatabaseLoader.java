package com.radomar.vkclient.loader;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;

import com.radomar.vkclient.content_provider.NewsContentProvider;
import com.radomar.vkclient.global.Constants;

/**
 * Created by Radomar on 09.02.2016
 */
public class DatabaseLoader extends CursorLoader {

    private String mSelectedImage;
    private String mMessage;
    private String mLat;
    private String mLong;

    public DatabaseLoader(Context context, Bundle args) {
        super(context);
        mSelectedImage = args.getString(Constants.BUNDLE_KEY_URI);
        mMessage = args.getString(Constants.BUNDLE_KEY_MESSAGE);
        mLat = args.getString(Constants.BUNDLE_KEY_LAT);
        mLong = args.getString(Constants.BUNDLE_KEY_LONG);
    }

    @Override
    public Cursor loadInBackground() {
        ContentValues values = new ContentValues();
        values.put(NewsContentProvider.SHARED_IMAGE_URL, mSelectedImage);
        values.put(NewsContentProvider.SHARED_MESSAGE, mMessage);
        values.put(NewsContentProvider.LATITUDE, mLat);
        values.put(NewsContentProvider.LONGITUDE, mLong);

        getContext().getContentResolver().insert(NewsContentProvider.SHARE_CONTENT_URI, values);
        return null;
    }
}
