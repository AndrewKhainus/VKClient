package com.radomar.vkclient.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Radomar on 14.01.2016
 */
public class NewsModel {

    @SerializedName("response")
    @Expose

    public ResponseModel response;

}
