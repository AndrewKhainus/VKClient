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
import android.text.TextUtils;

/**
 * Created by Radomar on 15.01.2016
 */
public class NewsContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.radomar.vkclient.NewsProvider";

    static final String DB_NAME = "mydb14";
    static final int DB_VERSION = 1;

    static final String NEWS_TABLE = "news";
    static final String AUTHORS_TABLE = "authors";
    static final String START_FROM_TABLE = "start_from";

    public static final String NEWS_ID = "_id";
    public static final String NEWS_TEXT = "text";


    public static final String PUBLISH_TIME = "publish_time";
    public static final String LIKE = "likes";
    public static final String REPOST = "repost";
    public static final String COMMENTS = "comments";
    public static final String GEO_LABEL = "geo";
    public static final String IMAGE_URL = "image_url";

    public static final String ID = "id";
    public static final String SOURCE_ID = "source_id";
    public static final String AUTHOR_NAME = "author_name";
    public static final String PHOTO_URL = "photo_url";

    public static final String START_FROM = "start_from";


    static final String CREATE_NEWS_TABLE = "create table " + NEWS_TABLE + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SOURCE_ID + " TEXT, " + PUBLISH_TIME + " INTEGER, "
            + NEWS_TEXT + " TEXT, " + IMAGE_URL + " TEXT, "
            + LIKE + " INTEGER, " + REPOST + " INTEGER, "
            + COMMENTS + " INTEGER, " + GEO_LABEL + " TEXT);";


    static final String CREATE_AUTHORS_TABLE = "create table " + AUTHORS_TABLE + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SOURCE_ID + " TEXT, "
            + AUTHOR_NAME + " TEXT, "
            + PHOTO_URL + " TEXT" + ");";

    static final String CREATE_START_FROM_TABLE = "create table " + START_FROM_TABLE + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + START_FROM + " TEXT" + ");";

    // path
    static final String NEWS_PATH = "news";
    static final String AUTHORS_PATH = "authors";
    static final String START_FROM_PATH = "start_from";

    public static final Uri NEWS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + NEWS_PATH);

    public static final Uri AUTHORS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + AUTHORS_PATH);

    public static final Uri START_FROM_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + START_FROM_PATH);

    // общий Uri
    static final int URI_NEWS = 1;

    // Uri с указанным ID
    static final int URI_NEWS_ID = 2;

    static final int URI_AUTHORS = 3;

    static final int URI_AUTHORS_ID = 4;

    static final int URI_START_FROM = 5;

    static final int URI_START_FROM_ID = 6;

    // описание и создание UriMatcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, NEWS_PATH, URI_NEWS);
        uriMatcher.addURI(AUTHORITY, NEWS_PATH + "/#", URI_NEWS_ID);
        uriMatcher.addURI(AUTHORITY, AUTHORS_PATH, URI_AUTHORS);
        uriMatcher.addURI(AUTHORITY, AUTHORS_PATH + "/#", URI_AUTHORS_ID);
        uriMatcher.addURI(AUTHORITY, START_FROM_PATH, URI_START_FROM);
        uriMatcher.addURI(AUTHORITY, START_FROM_PATH + "/#", URI_START_FROM_ID);
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

        switch (uriMatcher.match(uri)) {
            case URI_NEWS:
                db = dbHelper.getWritableDatabase();

                String query = "select N.text, N.publish_time, N.image_url, "  +
                        "N.likes, N.repost, N.comments, " +
                        "A.author_name, A.photo_url " +
                        "from news N " +
                        "inner join authors A " +
                        "on A.source_id = N.source_id " +
                        "order by publish_time DESC";

                Cursor cursor = db.rawQuery(query, null);

                cursor.setNotificationUri(getContext().getContentResolver(),
                        NEWS_CONTENT_URI);
                return cursor;

            case URI_AUTHORS:
                db = dbHelper.getWritableDatabase();
                cursor = db.query(AUTHORS_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(),
                        AUTHORS_CONTENT_URI);
                return cursor;

            case URI_START_FROM:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ID + " ASC";
                }
                db = dbHelper.getWritableDatabase();
                cursor = db.query(START_FROM_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);

                cursor.setNotificationUri(getContext().getContentResolver(),
                        START_FROM_CONTENT_URI);
                return cursor;

            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

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
            case URI_START_FROM:
                rowID = db.insert(START_FROM_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(START_FROM_CONTENT_URI, rowID);
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
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

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
                    selection = ID + " = " + id;
                } else {
                    selection = selection + " AND " + ID + " = " + id;
                }
                cnt = db.update(AUTHORS_TABLE, values, selection, selectionArgs);
                break;
            case URI_START_FROM:
                if (TextUtils.isEmpty(selection)) {
                    selection = ID + " = " + id;
                } else {
                    selection = selection + " AND " + ID + " = " + id;
                }
                cnt = db.update(START_FROM_TABLE, values, selection, selectionArgs);
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
            db.execSQL(CREATE_START_FROM_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
