package com.radomar.vkclient;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.radomar.vkclient.models.UploadServer;

import java.lang.reflect.Type;

/**
 * Created by Radomar on 28.01.2016
 */
public class UploadServerUrlJsonDeserializer implements JsonDeserializer<UploadServer> {

    @Override
    public UploadServer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {


        UploadServer uploadServer = new UploadServer();
        uploadServer.uploadUrl = json.getAsJsonObject().getAsJsonObject("response").get("upload_url").getAsString();
        return uploadServer;
    }
}
