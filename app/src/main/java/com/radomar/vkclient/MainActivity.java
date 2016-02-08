package com.radomar.vkclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.radomar.vkclient.fragments.VKFragment;
import com.radomar.vkclient.global.Constants;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // FIXME: you should set fragment only once; in other case default save-restore state behaviour won't work  
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
        VKFragment vkFragment = new VKFragment();
        getFragmentManager().beginTransaction().replace(R.id.container_AM,
                vkFragment, Constants.TAG_FRAGMENT_VK).commit();
    }

}
