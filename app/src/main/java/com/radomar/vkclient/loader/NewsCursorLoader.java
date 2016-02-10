package com.radomar.vkclient.loader;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.radomar.vkclient.content_provider.NewsContentProvider;

/**
 * Created by Radomar on 15.01.2016
 */
public class NewsCursorLoader extends CursorLoader {

    private Cursor mCursor;

    private Uri mUri;
    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mSortOrder;

    private ForceLoadContentObserver mObserver;

    public NewsCursorLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;

        mObserver = new ForceLoadContentObserver();
    }

    @Override
    public Cursor loadInBackground() {
        mCursor = getContext().getContentResolver().query(mUri, mProjection, mSelection, mSelectionArgs, mSortOrder);
        if (mCursor != null) {
            mCursor.registerContentObserver(mObserver);
            mCursor.setNotificationUri(getContext().getContentResolver(), NewsContentProvider.NEWS_CONTENT_URI);
        }
        return mCursor;
    }

    @Override
    protected void onStartLoading() {
        if (mCursor == null) {
            onForceLoad();
        } else {
            mCursor.setNotificationUri(getContext().getContentResolver(), mUri);
            deliverResult(mCursor);
        }
    }

    @Override
    public void deliverResult(Cursor cursor) {
        super.deliverResult(cursor);
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Log.d("sometag", "onContentChanged");
        onForceLoad();
    }
}
