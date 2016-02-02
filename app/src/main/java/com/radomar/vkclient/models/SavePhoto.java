package com.radomar.vkclient.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Radomar on 02.02.2016
 */
public class SavePhoto {

    @SerializedName("server")
    public String server;

    @SerializedName("photo")
    public String photo;

    @SerializedName("hash")
    public String hash;
}
