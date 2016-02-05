package com.radomar.vkclient.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.radomar.vkclient.models.PhotoModel;

import java.lang.reflect.Type;

/**
 * Created by Radomar on 03.02.2016
 */
public class RetrivePhotoIdDeserializer implements JsonDeserializer<PhotoModel> {

    @Override
    public PhotoModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        PhotoModel photoModel = new PhotoModel();

        photoModel.id = json.getAsJsonObject().getAsJsonArray("response").get(0).getAsJsonObject().get("id").getAsString();
        return photoModel;
    }
}
