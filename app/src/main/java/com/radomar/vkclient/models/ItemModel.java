package com.radomar.vkclient.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Radomar on 14.01.2016
 */
public class ItemModel {

    @SerializedName("date")
    @Expose
    public Long date;

    @SerializedName("text")
    @Expose
    public String text;

    @SerializedName("attachments")
    @Expose
    public List<AttachmentModel> attachments = new ArrayList<>();

//    @SerializedName("comments")
//    @Expose
//    public int commentsCount;
//
//    @SerializedName("likes")
//    @Expose
//    public int likesCount;
//
//    @SerializedName("reposts")
//    @Expose
//    public int repostCount;

}
