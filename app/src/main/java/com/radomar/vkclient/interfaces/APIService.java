package com.radomar.vkclient.interfaces;

import com.radomar.vkclient.models.Model;
import com.radomar.vkclient.models.PhotoModel;
import com.radomar.vkclient.models.SavePhoto;
import com.radomar.vkclient.models.UploadServer;
import com.squareup.okhttp.RequestBody;

import retrofit.Call;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.Url;

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

    @POST("/method/wall.post")
    Call<Model> shareQuery(@Query("owner_id") String ownerId,
                           @Query("friends_only") int friendsOnly,
                           @Query("message") String message,
                           @Query("attachments") String attachments,
                           @Query("lat") String latitude,
                           @Query("long") String longitude,
                           @Query("access_token") String token);

    @GET("/method/photos.getWallUploadServer")
    Call<UploadServer> getUploadUrl(@Query("access_token") String token);

    @Multipart
    @POST
    Call<SavePhoto> uploadPhoto(@Url String url,
                                @Part("photo\"; filename=\"image.png\" ") RequestBody _file);

    @POST("/method/photos.saveWallPhoto")
    Call<PhotoModel> saveWallPhoto(
//                                     @Query("user_id") String userId,
                                     @Query(value = "photo", encoded=true) String photo,
                                     @Query(value = "server") String server,
                                     @Query(value = "hash") String hash,
                                     @Query(value = "access_token") String token);

}
