package com.radomar.vkclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.radomar.vkclient.interfaces.APIService;
import com.radomar.vkclient.models.Model;
import com.radomar.vkclient.models.PhotoModel;
import com.radomar.vkclient.models.UploadServer;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Radomar on 03.02.2016
 */
public class RestClient {

    private static RestClient mInstance;
    private static final String BASE_URL = "https://api.vk.com/method/";
    private APIService mAPIService;

    public static RestClient getInstance() {
        if (mInstance == null) {
            mInstance = new RestClient();
        }
        return mInstance;
    }

    private RestClient() {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(UploadServer.class, new RetrieveServerUrlDeserializer())
                .registerTypeAdapter(PhotoModel.class, new RetrivePhotoIdDeserializer())
                .registerTypeAdapter(Model.class, new CustomJsonDeserializer())
                .create();

        Retrofit client = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mAPIService = client.create(APIService.class);
    }

    public APIService getAPIService() {
        return mAPIService;
    }

}
