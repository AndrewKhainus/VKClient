package com.radomar.vkclient.interfaces;

import com.radomar.vkclient.models.Model;
import com.radomar.vkclient.models.NewsModel;

import java.util.Map;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * Created by Radomar on 14.01.2016
 */
public interface APIService {

    @GET("/method/newsfeed.get")
    Call<Model> getOlderNews(@Query("filters") String filter,
                            @Query("v") double version,
                            @Query("start_from") String startFrom,
                            @Query("count") int count,
                            @Query("access_token") String token );


    @GET("/method/newsfeed.get")
    Call<Model> newQuery(@Query("filters") String filter,
                         @Query("v") double version,
                         @Query("start_time") long startTime,
                         @Query("count") int count,
                         @Query("access_token") String token );
}
