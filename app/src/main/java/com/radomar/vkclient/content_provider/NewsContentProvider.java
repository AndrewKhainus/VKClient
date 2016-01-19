package com.radomar.vkclient.content_provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by Radomar on 15.01.2016
 */
public class NewsContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.radomar.vkclient.NewsProvider";

    static final String DB_NAME = "mydb";
    static final int DB_VERSION = 1;

    static final String NEWS_TABLE = "news";
    static final String AUTHORS_TABLE = "authors";

    static final String NEWS_ID = "_id";
    static final String NEWS_TEXT = "text";


    public static final String PUBLISH_TIME = "publish_time";
    public static final String LIKE = "like";
    public static final String REPOST = "repost";
    public static final String COMMENTS = "comments";
    public static final String GEO_LABEL = "geo";
    public static final String IMAGE_URL = "image_url";

    public static final String ID = "id";
    public static final String SIGNER_ID = "signer_id";
    public static final String FIRST_NAME = "first_name";
    public static final String SECOND_NAME = "second_name";
    public static final String PHOTO_URL = "photo_url";


    static final String CREATE_NEWS_TABLE = "create table " + NEWS_TABLE + "("
            + NEWS_ID + " integer primary key autoincrement, "
            + SIGNER_ID + " INTEGER, " + PUBLISH_TIME + " INTEGER, "
            + NEWS_TEXT + " TEXT, " + IMAGE_URL + " TEXT, "
            + LIKE + " INTEGER, " + REPOST + " INTEGER, "
            + COMMENTS + " INTEGER, " + GEO_LABEL + " TEXT, "
            + "FOREIGN KEY (" + SIGNER_ID + ") REFERENCES " + AUTHORS_TABLE + "(" + SIGNER_ID + "));";


    static final String CREATE_AUTHORS_TABLE = "create table " + AUTHORS_TABLE + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SIGNER_ID + " INTEGER, " + FIRST_NAME + " TEXT, "
            + SECOND_NAME + " TEXT, " + PHOTO_URL + " TEXT" + ");";

    // path
    static final String NEWS_PATH = "news";
    static final String AUTHORS_PATH = "authors";

    public static final Uri NEWS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + NEWS_PATH);

    public static final Uri AUTHORS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + AUTHORS_PATH);

    // общий Uri
    static final int URI_NEWS = 1;

    // Uri с указанным ID
    static final int URI_NEWS_ID = 2;

    static final int URI_AUTHORS = 3;

    static final int URI_AUTHORS_ID = 4;

    // описание и создание UriMatcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, NEWS_PATH, URI_NEWS);
        uriMatcher.addURI(AUTHORITY, NEWS_PATH + "/#", URI_NEWS_ID);
        uriMatcher.addURI(AUTHORITY, AUTHORS_PATH, URI_AUTHORS);
        uriMatcher.addURI(AUTHORITY, AUTHORS_PATH + "/#", URI_AUTHORS_ID);
    }

    DBHelper dbHelper;
    SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        String id = uri.getLastPathSegment();
        switch (uriMatcher.match(uri)) {
            case URI_NEWS:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = NEWS_ID + " ASC";
                }
                break;
            case URI_NEWS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = NEWS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + NEWS_ID + " = " + id;
                }
                break;
            case URI_AUTHORS:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = SIGNER_ID + " ASC";
                }
                break;
            case URI_AUTHORS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = ID + " = " + id;
                } else {
                    selection = selection + " AND " + ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(NEWS_TABLE, projection, selection,
                selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(),
                NEWS_CONTENT_URI);

        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != URI_NEWS || uriMatcher.match(uri) != URI_AUTHORS) {
            throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        long rowID;
        Uri resultUri;
        switch (uriMatcher.match(uri)) {
            case URI_NEWS:
                rowID = db.insert(NEWS_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(NEWS_CONTENT_URI, rowID);
                break;
            case URI_AUTHORS:
                rowID = db.insert(AUTHORS_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(AUTHORS_CONTENT_URI, rowID);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        // уведомляем ContentResolver, что данные по адресу resultUri изменились
        getContext().getContentResolver().notifyChange(resultUri, null);

        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        String id = uri.getLastPathSegment();
        db = dbHelper.getWritableDatabase();
        int cnt;

        switch (uriMatcher.match(uri)) {
            case URI_NEWS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = NEWS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + NEWS_ID + " = " + id;
                }

                cnt = db.delete(NEWS_TABLE, selection, selectionArgs);
                break;
            case URI_AUTHORS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = ID + " = " + id;
                } else {
                    selection = selection + " AND " + ID + " = " + id;
                }

                cnt = db.delete(AUTHORS_TABLE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        String id = uri.getLastPathSegment();
        db = dbHelper.getWritableDatabase();
        int cnt;

        switch (uriMatcher.match(uri)) {
            case URI_NEWS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = NEWS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + NEWS_ID + " = " + id;
                }
                cnt = db.update(NEWS_TABLE, values, selection, selectionArgs);
                break;
            case URI_AUTHORS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = NEWS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + NEWS_ID + " = " + id;
                }
                cnt = db.update(AUTHORS_TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return cnt;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_NEWS_TABLE);
            db.execSQL(CREATE_AUTHORS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
