package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Notification implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("type")
    private String type;

    @SerializedName("content")
    private String content;

    @SerializedName("related_id")
    private Integer relatedId;

    @SerializedName("from_user_id")
    private Integer fromUserId;

    @SerializedName("from_username")
    private String fromUsername;

    @SerializedName("is_read")
    private boolean isRead;

    @SerializedName("created_at")
    private String createdAt;

    // Popolato solo per notifiche di tipo event_proposal / proposal_response
    @SerializedName("event_title")
    private String eventTitle;

    public int getId()              { return id; }
    public String getType()         { return type; }
    public String getContent()      { return content; }
    public Integer getRelatedId()   { return relatedId; }
    public Integer getFromUserId()  { return fromUserId; }
    public String getFromUsername() { return fromUsername; }
    public boolean isRead()         { return isRead; }
    public String getCreatedAt()    { return createdAt; }
    public String getEventTitle()   { return eventTitle != null ? eventTitle : ""; }

    public String getTypeLabel() {
        if (type == null) return "";
        switch (type) {
            case "friend_request":   return "Richiesta amicizia";
            case "event_proposal":   return "Proposta evento";
            case "proposal_response":return "Risposta alla proposta";
            default:                 return type;
        }
    }
}
