package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Proposal implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("from_user_id")
    private int fromUserId;

    @SerializedName("from_username")
    private String fromUsername;

    @SerializedName("to_user_id")
    private int toUserId;

    @SerializedName("to_username")
    private String toUsername;

    @SerializedName("event_id")
    private int eventId;

    @SerializedName("event_title")
    private String eventTitle;

    @SerializedName("event_date")
    private String eventDate;

    @SerializedName("event_city")
    private String eventCity;

    // pending | accepted | declined | maybe
    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    // Presente solo nelle risposte di GET /api/proposals/status
    @SerializedName("response_emoji")
    private String responseEmoji;

    public int getId()              { return id; }
    public int getFromUserId()      { return fromUserId; }
    public String getFromUsername() { return fromUsername; }
    public int getToUserId()        { return toUserId; }
    public String getToUsername()   { return toUsername; }
    public int getEventId()         { return eventId; }
    public String getEventTitle()   { return eventTitle; }
    public String getEventDate()    { return eventDate; }
    public String getEventCity()    { return eventCity; }
    public String getStatus()       { return status; }
    public String getCreatedAt()    { return createdAt; }
    public String getResponseEmoji(){ return responseEmoji; }

    public String getStatusEmoji() {
        if (status == null) return "⏳";
        switch (status) {
            case "accepted": return "👍";
            case "declined": return "👎";
            case "maybe":    return "🤔";
            default:         return "⏳";
        }
    }
}
