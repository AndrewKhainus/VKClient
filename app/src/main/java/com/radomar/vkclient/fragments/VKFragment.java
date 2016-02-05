package com.radomar.vkclient.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


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

    private static final int DIALOG_REQUEST_CODE = 42;

    private RecyclerView mRecyclerView;
    private CustomRecyclerAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Button mLoginButton;
    private Button mRequestButton;
    private FloatingActionButton mFab;

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
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("sometag", "onActivityResult");
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, this)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case DIALOG_REQUEST_CODE:
                    updateUI(data.getBooleanExtra(Constants.DIALOG_TAG, false));
                    break;
            }
        }
    }

    private void findViews(View view) {
        mLoginButton = (Button) view.findViewById(R.id.btLogin_FK);
        mRequestButton = (Button) view.findViewById(R.id.btRequest_FK);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srRefresh_FK);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab_FK);
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
        mFab.setOnClickListener(this);
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
            case R.id.fab_FK:
                ShareDialog shareDialog = new ShareDialog();
                shareDialog.setTargetFragment(this, DIALOG_REQUEST_CODE);
                shareDialog.show(getFragmentManager(), Constants.TAG_SHARE_DIALOG);
                break;
            case R.id.btRequest_FK:

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
        mAdapter.setCursor(data);
        mAdapter.notifyDataSetChanged();
        mIsLoading = false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void initCursorLoader() {
        getLoaderManager().initLoader(Constants.LOADER_ID, null, this);
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
                    SyncAdapter.syncImmediately(Constants.DOWNLOAD_ANYWAY_PARAM, null, null, null, null);
                }
            }
        });
    }

    @Override
    public void onResult(VKAccessToken res) {
        SyncAdapter.initializeSyncAdapter(getActivity());
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
                    SyncAdapter.syncImmediately(Constants.SWIPE_REFRESH_PARAM, null, null, null, null);
                }
            }
        }, 1000);
    }

    private void updateUI(boolean isDataAdded) {
        if (isDataAdded) {
//TODO change fab color
        }
    }

}
