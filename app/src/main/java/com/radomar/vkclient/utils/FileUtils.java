package com.radomar.vkclient.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by Radomar on 05.02.2016
 */
public class FileUtils {

    private static FileUtils mInstance;

    private FileUtils() {}

    public static FileUtils getInstance() {
        if (mInstance == null) {
            mInstance = new FileUtils();
        }
        return mInstance;
    }

    public String getRealPathFromURI(Context mContext, Uri contentURI) {
        String result;
        Cursor cursor = mContext.getContentResolver().query(contentURI, null, null, null, null);
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
