package com.radomar.vkclient.interfaces;

import com.radomar.vkclient.models.NewsModel;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Radomar on 14.01.2016
 */
public interface APIService {

    @GET("/method/newsfeed.get")
    Call<NewsModel> getJSON(@Query("filters") String filter, @Query("v") double version, @Query("access_token") String token );

}
