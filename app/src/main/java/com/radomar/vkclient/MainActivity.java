package com.radomar.vkclient;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.radomar.vkclient.fragments.VKFragment;
import com.radomar.vkclient.global.Constants;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startVKFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void startVKFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        VKFragment vkFragment = (VKFragment) fragmentManager.findFragmentByTag(Constants.TAG_FRAGMENT_VK);

        if(vkFragment == null) {
            vkFragment = new VKFragment();
            fragmentManager.beginTransaction().replace(R.id.container_AM,
                    vkFragment, Constants.TAG_FRAGMENT_VK).commit();
        }
    }

}
