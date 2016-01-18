package com.radomar.vkclient;

import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.radomar.vkclient.fragments.TaskLoginFragment;
import com.radomar.vkclient.fragments.VKFragment;
import com.radomar.vkclient.global.Constants;
import com.radomar.vkclient.interfaces.ActionListener;
import com.radomar.vkclient.interfaces.GetCallbackInterface;
import com.radomar.vkclient.interfaces.OnStartAddAndRemoveListener;
import com.radomar.vkclient.interfaces.PublisherInterface;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PublisherInterface, OnStartAddAndRemoveListener, GetCallbackInterface {

    private List<ActionListener> mListeners = new ArrayList<>();
    private FragmentManager mFragmentManager;
    private TaskLoginFragment mTaskLoginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getFragmentManager();

        startVKFragment();
        addTaskFragment();
    }



    private void startVKFragment() {
        VKFragment vkFragment = new VKFragment();
        getFragmentManager().beginTransaction().replace(R.id.container_AM,
                vkFragment, Constants.TAG_FRAGMENT_VK).commit();
    }

    private void addTaskFragment() {
        mTaskLoginFragment = (TaskLoginFragment) mFragmentManager.findFragmentByTag(Constants.TAG_TASK_LOGIN);
        if (mTaskLoginFragment == null) {
            mTaskLoginFragment = new TaskLoginFragment();
            mFragmentManager.beginTransaction().add(R.id.container_AM, mTaskLoginFragment, Constants.TAG_TASK_LOGIN).commit();
        }
    }

    @Override
    public void addListener(ActionListener listener) {
        mListeners.add(listener);
//        VKAccessToken token = mTaskLoginFragment.getLoginResult();
        if (mTaskLoginFragment.getLoginResult() != null) {
            notifySubscribers(mTaskLoginFragment.getLoginResult());
        }
    }

    @Override
    public void removeListener(ActionListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void notifySubscribers(VKAccessToken token) {
        for (ActionListener actionListener: mListeners) {
            actionListener.doAction(token);
        }
    }

    @Override
    public void onStartAddListener(ActionListener listener) {
        addListener(listener);
    }

    @Override
    public void onStartRemoveListener(ActionListener listener) {
        removeListener(listener);
    }

    @Override
    public VKCallback<VKAccessToken> getTaskLoginCallback() {
        return mTaskLoginFragment;
    }
}
