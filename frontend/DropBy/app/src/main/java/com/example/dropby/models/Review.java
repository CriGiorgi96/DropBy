package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Review implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("username")
    private String username;

    @SerializedName("event_id")
    private int eventId;

    @SerializedName("event_title")
    private String eventTitle;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("event_date")
    private String eventDate;

    @SerializedName("event_city")
    private String eventCity;

    @SerializedName("location_name")
    private String locationName;

    public int getId()              { return id; }
    public int getUserId()          { return userId; }
    public String getUsername()     { return username; }
    public int getEventId()         { return eventId; }
    public String getEventTitle()   { return eventTitle; }
    public int getRating()          { return rating; }
    public String getComment()      { return comment; }
    public String getCreatedAt()    { return createdAt; }
    public String getEventDate()    { return eventDate; }
    public String getEventCity()    { return eventCity; }
    public String getLocationName() { return locationName; }

    /** Restituisce le stelle come stringa (es. "★★★★☆" per 4/5) */
    public String getStarsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= rating ? "★" : "☆");
        return sb.toString();
    }
}
