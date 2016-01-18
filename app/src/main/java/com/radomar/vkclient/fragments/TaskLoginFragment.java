package com.radomar.vkclient.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import com.radomar.vkclient.interfaces.PublisherInterface;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.api.VKError;

/**
 * Created by Radomar on 13.01.2016
 */
public class TaskLoginFragment extends Fragment implements VKCallback<VKAccessToken> {

    private VKAccessToken mToken;
    private PublisherInterface mPublisherInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPublisherInterface = (PublisherInterface)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPublisherInterface = null;
    }

    @Override
    public void onResult(VKAccessToken res) {
        mToken = res;
        if (res != null) {
            mPublisherInterface.notifySubscribers(res);
        }
    }

    @Override
    public void onError(VKError error) {

    }

    public VKAccessToken getLoginResult() {
        return mToken;
    }
}
