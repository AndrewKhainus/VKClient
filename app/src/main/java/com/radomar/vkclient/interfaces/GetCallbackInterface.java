package com.radomar.vkclient.interfaces;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;

/**
 * Created by Radomar on 06.01.2016
 */
public interface GetCallbackInterface {

    VKCallback<VKAccessToken> getTaskLoginCallback();

}
