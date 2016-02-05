package com.radomar.vkclient.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.radomar.vkclient.global.Constants;
import com.radomar.vkclient.utils.FileUtils;


/**
 * Created by Radomar on 09.01.2016
 */
public class ImageLoader extends AsyncTaskLoader<Bitmap> {

    private Uri mUri;
    private Bitmap mBitmap;

    public ImageLoader(Context context, Bundle args) {
        super(context);
        if (args != null) {
            mUri = args.getParcelable(Constants.LOADER_URI_KEY);
        }
    }

    @Override
    public Bitmap loadInBackground() {

        String pathFromURI = FileUtils.getInstance().getRealPathFromURI(getContext(), mUri);

        mBitmap = decodeSampledBitmapFromImage(pathFromURI, 150, 150);

        return mBitmap;
    }

    @Override
    protected void onStartLoading() {

        if (mBitmap != null) {
            deliverResult(mBitmap);
        }

        if (mUri != null && mBitmap == null) {
            forceLoad();
        }
    }

    private Bitmap decodeSampledBitmapFromImage(String pathName, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
