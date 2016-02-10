package com.radomar.vkclient.deserializers;

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

    private static final String COORDINATES = "coordinates";
    private static final String RESPONSE = "response";
    private static final String ITEMS = "items";
    private static final String DATE = "date";
    private static final String TEXT = "text";
    private static final String SOURCE_ID = "source_id";
    private static final String COMMENTS = "comments";
    private static final String COUNT = "count";
    private static final String LIKES = "likes";
    private static final String REPOSTS = "reposts";
    private static final String GEO = "geo";
    private static final String ATTACHMENTS = "attachments";
    private static final String TYPE = "type";
    private static final String PHOTO = "photo";
    private static final String PHOTO_604 = "photo_604";
    private static final String PHOTO_100 = "photo_100";
    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String PROFILES = "profiles";
    private static final String GROUPS = "groups";
    private static final String NEXT_FROM = "next_from";

    @Override
    public Model deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Model model = new Model();

        JsonArray array = json.getAsJsonObject().getAsJsonObject(RESPONSE).getAsJsonArray(ITEMS);
        for (int i = 0; i < array.size(); i++) {
            NewsModel newsModel = new NewsModel();
            JsonObject newsJsonObject = array.get(i).getAsJsonObject();
            newsModel.date = newsJsonObject.get(DATE).getAsLong();
            newsModel.text = newsJsonObject.get(TEXT).getAsString();
            newsModel.sourceId = newsJsonObject.get(SOURCE_ID).getAsString();

// comments, likes, reposts
            newsModel.comments = newsJsonObject.getAsJsonObject(COMMENTS).get(COUNT).getAsInt();
            newsModel.like = newsJsonObject.getAsJsonObject(LIKES).get(COUNT).getAsInt();
            newsModel.repost = newsJsonObject.getAsJsonObject(REPOSTS).get(COUNT).getAsInt();

//  coordinates
            JsonObject geoJsonObject = newsJsonObject.getAsJsonObject(GEO);
            if (geoJsonObject != null) {
                String[] coordinates = geoJsonObject.get(COORDINATES).getAsString().split(" ");
                newsModel.latitude = coordinates[0];
                newsModel.longitude = coordinates[1];
            }

            JsonArray attachmentsArray = newsJsonObject.getAsJsonArray(ATTACHMENTS);
            if (attachmentsArray != null) {
                if (attachmentsArray.size() > 0) {
                    String s = attachmentsArray.get(0).getAsJsonObject().get(TYPE).getAsString();
                    if (s.equals(PHOTO)) {
                        newsModel.photoUrl = attachmentsArray.get(0).getAsJsonObject().getAsJsonObject(PHOTO).get(PHOTO_604).getAsString();
                    }
                }
            }

            model.newsList.add(newsModel);
        }

        array = json.getAsJsonObject().getAsJsonObject(RESPONSE).getAsJsonArray(GROUPS);
        for (int i = 0; i < array.size(); i++) {
            AuthorModel authorModel = new AuthorModel();
            JsonObject newsJsonObject = array.get(i).getAsJsonObject();
            authorModel.sourceId = "-" + newsJsonObject.get(ID).getAsString();
            authorModel.name = newsJsonObject.get(NAME).getAsString();
            authorModel.photoUrl = newsJsonObject.get(PHOTO_100).getAsString();
            model.authorsList.add(authorModel);
        }

        array = json.getAsJsonObject().getAsJsonObject(RESPONSE).getAsJsonArray(PROFILES);
        for (int i = 0; i < array.size(); i++) {
            AuthorModel authorModel = new AuthorModel();
            JsonObject newsJsonObject = array.get(i).getAsJsonObject();
            authorModel.sourceId = newsJsonObject.get(ID).getAsString();
            authorModel.name = newsJsonObject.get(FIRST_NAME).getAsString() + newsJsonObject.get(LAST_NAME).getAsString();
            authorModel.photoUrl = newsJsonObject.get(PHOTO_100).getAsString();
            model.authorsList.add(authorModel);
        }

        JsonElement element = json.getAsJsonObject().getAsJsonObject(RESPONSE).get(NEXT_FROM);
        if (element != null) {
            model.startFrom = element.getAsString();
        }

        return model;
    }
}
