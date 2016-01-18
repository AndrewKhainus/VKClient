package com.radomar.vkclient.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Radomar on 14.01.2016
 */
public class ResponseModel {

    @SerializedName("items")
    @Expose
    public List<ItemModel> items = new ArrayList<>();
}
