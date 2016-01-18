package com.radomar.vkclient.interfaces;

import com.vk.sdk.VKAccessToken;

/**
 * Created by Radomar on 13.01.2016
 */
public interface PublisherInterface {

    void addListener(ActionListener listener);

    void removeListener(ActionListener listener);

    void notifySubscribers(VKAccessToken token);
}
