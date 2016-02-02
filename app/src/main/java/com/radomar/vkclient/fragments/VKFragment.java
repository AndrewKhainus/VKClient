package com.radomar.vkclient.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.melnykov.fab.FloatingActionButton;
import com.radomar.vkclient.R;
import com.radomar.vkclient.adapters.CustomRecyclerAdapter;
import com.radomar.vkclient.content_provider.NewsContentProvider;
import com.radomar.vkclient.global.Constants;
import com.radomar.vkclient.loader.NewsCursorLoader;
import com.radomar.vkclient.sync_adapter.SyncAdapter;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

/**
 * Created by Radomar on 12.01.2016
 */
public class VKFragment extends Fragment implements View.OnClickListener,
                                                    VKCallback<VKAccessToken>,
                                                    LoaderManager.LoaderCallbacks<Cursor>,
                                                    SwipeRefreshLayout.OnRefreshListener {

//    TODO save in bundle: 1) mIsLoading

    private SharedPreferences mSharedPref;

    private RecyclerView mRecyclerView;
    private CustomRecyclerAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ContentObserver mContentObserver;

    private Button mLoginButton;
    private Button mRequestButton;
    private FloatingActionButton fab;

    private boolean mIsLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VKSdk.customInitialize(getActivity(), Constants.VK_API_KEY, String.valueOf(Constants.VK_API_KEY));


        SyncAdapter.initializeSyncAdapter(getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vk, container, false);

        findViews(view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        initRecyclerView(view);

        initAdapter();
        setListener();
        fab.attachToRecyclerView(mRecyclerView);

//  TODO make something with login button
        if (VKSdk.isLoggedIn()) {
            mLoginButton.setText(getString(R.string.sign_out));
        } else {
            mLoginButton.setText(getString(R.string.sign_in));
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        initCursorLoader();
        registerContentObserver();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterContentObserver();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, this)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void findViews(View view) {
        mLoginButton = (Button) view.findViewById(R.id.btLogin_FK);
        mRequestButton = (Button) view.findViewById(R.id.btRequest_FK);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srRefresh_FK);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
    }

    private void initRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rvList_FK);
        mRecyclerView.setLayoutManager(mLayoutManager);
        addOnScrollListener(mRecyclerView);
    }

    private void initAdapter() {
            mAdapter = new CustomRecyclerAdapter(getActivity(), null);
            mRecyclerView.setAdapter(mAdapter);
    }

    private void setListener() {
        mLoginButton.setOnClickListener(this);
        mRequestButton.setOnClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btLogin_FK:
                if (!VKSdk.isLoggedIn()) {
//                    login with scopes
                    VKSdk.login(this, "friends", "photos", "wall");
                    mLoginButton.setText(getString(R.string.sign_out));
                } else {
                    VKSdk.logout();
                    mLoginButton.setText(getString(R.string.sign_in));
                }
                break;
            case R.id.btRequest_FK:

                break;
            case R.id.fab:
                new ShareDialog().show(getFragmentManager(), Constants.TAG_SHARE_DIALOG);
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new NewsCursorLoader(getActivity(),
                                    NewsContentProvider.NEWS_CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    NewsContentProvider.PUBLISH_TIME);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("sometag", "onLoadFinished");
        mAdapter.setCursor(data);
        mAdapter.notifyDataSetChanged();
        mIsLoading = false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void initCursorLoader() {
        getLoaderManager().initLoader(100, null, this);
    }

    private void addOnScrollListener(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = mRecyclerView.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if ((totalItemCount - visibleItemCount) <= firstVisibleItem && !mIsLoading) {
                    mIsLoading = true;
                    SyncAdapter.anywaySyncImmediately();
                }
            }
        });
    }

    private void registerContentObserver() {
        mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
//                TODO forceLoad should not be here. Sometimes crush.     http://developer.android.com/intl/ru/reference/android/content/AsyncTaskLoader.html
                getLoaderManager().getLoader(100).forceLoad();
            }
        };
        getActivity().getContentResolver().registerContentObserver(NewsContentProvider.NEWS_CONTENT_URI, true, mContentObserver);
    }

    private void unregisterContentObserver() {
        getActivity().getContentResolver().unregisterContentObserver(mContentObserver);
    }

    @Override
    public void onResult(VKAccessToken res) {
        SyncAdapter.initializeSyncAdapter(getActivity());
        mSharedPref = getActivity().getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor ed = mSharedPref.edit();
        ed.putString(Constants.OWNER_ID_KEY, res.userId);
        ed.apply();
    }

    @Override
    public void onError(VKError error) {

    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                if(mAdapter != null) {
                    SyncAdapter.syncBySwipeToRefresh();
                }
            }
        }, 1000);
    }

}
