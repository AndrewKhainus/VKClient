package com.radomar.vkclient.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Radomar on 22.01.2016
 */
public class Model {

//    @SerializedName("items")
//    @Expose
    public List<NewsModel> newsList = new ArrayList<>();

    public List<AuthorModel> authorsList = new ArrayList<>();

    public String startFrom;
}
