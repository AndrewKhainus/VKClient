package com.radomar.vkclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.radomar.vkclient.models.AuthorModel;
import com.radomar.vkclient.models.Model;
import com.radomar.vkclient.models.NewsModel;


import java.lang.reflect.Type;

/**
 * Created by Radomar on 22.01.2016
 */
public class CustomJsonDeserializer implements JsonDeserializer<Model> {

    @Override
    public Model deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Model model = new Model();

        JsonArray array = json.getAsJsonObject().getAsJsonObject("response").getAsJsonArray("items");
        for (int i = 0; i < array.size(); i++) {
            NewsModel newsModel = new NewsModel();
            JsonObject newsJsonObject = array.get(i).getAsJsonObject();
            newsModel.date = newsJsonObject.get("date").getAsLong();
            newsModel.text = newsJsonObject.get("text").getAsString();
            newsModel.sourceId = newsJsonObject.get("source_id").getAsString();

            newsModel.comments = newsJsonObject.getAsJsonObject("comments").get("count").getAsInt();
            newsModel.like = newsJsonObject.getAsJsonObject("likes").get("count").getAsInt();
            newsModel.repost = newsJsonObject.getAsJsonObject("reposts").get("count").getAsInt();

//  TODO type "photo" can be not only first position
            JsonArray attachmentsArray = newsJsonObject.getAsJsonArray("attachments");
            if (attachmentsArray != null) {
                if (attachmentsArray.size() > 0) {
                    String s = attachmentsArray.get(0).getAsJsonObject().get("type").getAsString();
                    if (s.equals("photo")) {
                        newsModel.photoUrl = attachmentsArray.get(0).getAsJsonObject().getAsJsonObject("photo").get("photo_604").getAsString();
                    }
                }
            }

            model.newsList.add(newsModel);
        }

        array = json.getAsJsonObject().getAsJsonObject("response").getAsJsonArray("groups");
        for (int i = 0; i < array.size(); i++) {
            AuthorModel authorModel = new AuthorModel();
            JsonObject newsJsonObject = array.get(i).getAsJsonObject();
            authorModel.sourceId = "-" + newsJsonObject.get("id").getAsString();
            authorModel.name = newsJsonObject.get("name").getAsString();
            authorModel.photoUrl = newsJsonObject.get("photo_100").getAsString();
            model.authorsList.add(authorModel);
        }

        array = json.getAsJsonObject().getAsJsonObject("response").getAsJsonArray("profiles");
        for (int i = 0; i < array.size(); i++) {
            AuthorModel authorModel = new AuthorModel();
            JsonObject newsJsonObject = array.get(i).getAsJsonObject();
            authorModel.sourceId = newsJsonObject.get("id").getAsString();
            authorModel.name = newsJsonObject.get("first_name").getAsString() + newsJsonObject.get("last_name").getAsString();
            authorModel.photoUrl = newsJsonObject.get("photo_100").getAsString();
            model.authorsList.add(authorModel);
        }

        JsonElement element = json.getAsJsonObject().getAsJsonObject("response").get("next_from");
        if (element != null) {
            model.startFrom = element.getAsString();
        }

        return model;
    }
}
