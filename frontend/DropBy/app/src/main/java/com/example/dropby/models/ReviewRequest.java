package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {

    @SerializedName("event_id")
    private int eventId;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("replica_id")
    private int replicaId;

    public ReviewRequest(int eventId, int rating, String comment) {
        this.eventId   = eventId;
        this.rating    = rating;
        this.comment   = comment;
        this.replicaId = -1;
    }

    public ReviewRequest(int eventId, int rating, String comment, int replicaId) {
        this.eventId   = eventId;
        this.rating    = rating;
        this.comment   = comment;
        this.replicaId = replicaId;
    }
}
