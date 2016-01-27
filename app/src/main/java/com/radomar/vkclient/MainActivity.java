package com.radomar.vkclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.radomar.vkclient.fragments.VKFragment;
import com.radomar.vkclient.global.Constants;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startVKFragment();
    }

    private void startVKFragment() {
        VKFragment vkFragment = new VKFragment();
        getFragmentManager().beginTransaction().replace(R.id.container_AM,
                vkFragment, Constants.TAG_FRAGMENT_VK).commit();
    }

}
