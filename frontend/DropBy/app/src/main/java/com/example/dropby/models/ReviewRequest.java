package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {

    @SerializedName("event_id")
    private int eventId;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    public ReviewRequest(int eventId, int rating, String comment) {
        this.eventId = eventId;
        this.rating  = rating;
        this.comment = comment;
    }
}
