package com.radomar.vkclient.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.radomar.vkclient.R;
import com.radomar.vkclient.adapters.NewsRecyclerAdapter;
import com.radomar.vkclient.content_provider.NewsContentProvider;
import com.radomar.vkclient.global.Constants;
import com.radomar.vkclient.interfaces.APIService;
import com.radomar.vkclient.interfaces.ActionListener;
import com.radomar.vkclient.interfaces.GetCallbackInterface;
import com.radomar.vkclient.interfaces.OnStartAddAndRemoveListener;
import com.radomar.vkclient.loader.NewsCursorLoader;
import com.radomar.vkclient.models.ItemModel;
import com.radomar.vkclient.models.NewsModel;
import com.radomar.vkclient.sync_adapter.SyncAdapter;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Radomar on 12.01.2016
 */
public class VKFragment extends Fragment implements View.OnClickListener,
                                                    ActionListener,
                                                    LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "sometag";
    private static String baseUrl = "https://api.vk.com/" ;

    private OnStartAddAndRemoveListener mOnStartAddAndRemoveListener;
    private GetCallbackInterface mGetCallbackInterface;

    private RecyclerView mRecyclerView;
    private NewsRecyclerAdapter mAdapter;

    private Button mLoginButton;
    private Button mRequestButton;

    private Cursor mCursor;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOnStartAddAndRemoveListener = (OnStartAddAndRemoveListener) activity;
        mGetCallbackInterface = (GetCallbackInterface) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VKSdk.customInitialize(getActivity(), Constants.VK_API_KEY, String.valueOf(Constants.VK_API_KEY));

//        mAccount = CreateSyncAccount(getActivity());

//        ContentResolver.addPeriodicSync(
//                mAccount,
//                AUTHORITY,
//                Bundle.EMPTY,
//                20L);
//
//        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);

        SyncAdapter.initializeSyncAdapter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vk, container, false);

        findViews(view);
        initRecyclerView(view);
        initAdapter();

        setListener();

        if (VKSdk.isLoggedIn()) {
            mLoginButton.setText("sign out");
        }


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        initCursorLoader();
    }

    @Override
    public void onStart() {
        super.onStart();
        mOnStartAddAndRemoveListener.onStartAddListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        initCursorLoader();
    }

    @Override
    public void onStop() {
        super.onStop();
        mOnStartAddAndRemoveListener.onStartRemoveListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnStartAddAndRemoveListener = null;
        mGetCallbackInterface = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, mGetCallbackInterface.getTaskLoginCallback())) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void findViews(View view) {
        mLoginButton = (Button) view.findViewById(R.id.btLogin_FK);
        mRequestButton = (Button) view.findViewById(R.id.btRequest_FK);
    }

    private void initRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rvList_FK);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void initAdapter() {
            mAdapter = new NewsRecyclerAdapter(getActivity(), getFakeData());
            mRecyclerView.setAdapter(mAdapter);
    }

    private void setListener() {
        mLoginButton.setOnClickListener(this);
        mRequestButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btLogin_FK:
                if (!VKSdk.isLoggedIn()) {
//                    login with scopes
                    VKSdk.login(this, "wall", "friends");
                } else {
                    VKSdk.logout();
                    mLoginButton.setText("sign in");
                }
                break;
            case R.id.btRequest_FK:
                SyncAdapter.syncImmediately(getActivity());//TODO: remove; just for test
//                initCursorLoader();
                break;
        }
    }

    @Override
    public void doAction(VKAccessToken token) {
//        begin to work
        Log.d(TAG, token.toString());
        Log.d("sometag", "ready");
    }

    private ArrayList<ItemModel> getFakeData() {
        ArrayList<ItemModel> list = new ArrayList<>();
//        for (int i = 0; i < 50; i++ ) {
//            NewsModel model = new NewsModel();
//            model.news = String.valueOf(i);
//            list.add(model);
//        }

        return list;
    }

    private void initRetrofit() {
        Retrofit client = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService apiService = client.create(APIService.class);
        Log.d("sometag", VKSdk.getAccessToken().accessToken);
        Call<NewsModel> call = apiService.getJSON("post", 5.44 ,VKSdk.getAccessToken().accessToken);
        call.enqueue(new Callback<NewsModel>() {
            @Override
            public void onResponse(Response<NewsModel> response, Retrofit retrofit) {

                Log.d("sometag", "Status Code = " + response.code());
                Log.d("sometag", response.raw().toString());
                NewsModel newsModel = response.body();

                Log.d("sometag", "total news = " + newsModel.response.items.size());

                if (response.code() == 200) {
                    mAdapter.setData(newsModel.response.items);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("sometag", "onCreateLoader");
        return new NewsCursorLoader(getActivity(), NewsContentProvider.NEWS_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("sometag", "onLoadFinished");
        mCursor = data;

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("sometag", "onLoaderReset");
    }

    private void initCursorLoader() {
        getLoaderManager().initLoader(100, null, this);
    }

//    public static Account CreateSyncAccount(Context context) {
//        // Create the account type and default account
//        Account newAccount = new Account(
//                "dummyaccount", "com.radomar.vkclient");
//        // Get an instance of the Android account manager
//        AccountManager accountManager =
//                (AccountManager) context.getSystemService(
//                         context.ACCOUNT_SERVICE);
//        /*
//         * Add the account and account type, no password or user data
//         * If successful, return the Account object, otherwise report an error.
//         */
//        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
//            /*
//             * If you don't set android:syncable="true" in
//             * in your <provider> element in the manifest,
//             * then call context.setIsSyncable(account, AUTHORITY, 1)
//             * here.
//             */
//            Log.d("sometag", "good");
//            return newAccount;
//        } else {
//            /*
//             * The account exists or some other error occurred. Log this, report it,
//             * or handle it internally.
//             */
//            Log.d("sometag", "bad, The account exists");
//            return newAccount;
//        }
//    }
}
