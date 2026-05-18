package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Reminder implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("event_id")
    private int eventId;

    @SerializedName("remind_at")
    private String remindAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("event_title")
    private String eventTitle;

    @SerializedName("event_date")
    private String eventDate;

    @SerializedName("event_city")
    private String eventCity;

    public int    getId()           { return id; }
    public int    getEventId()      { return eventId; }
    public String getRemindAt()     { return remindAt; }
    public String getCreatedAt()    { return createdAt; }
    public String getEventTitle()   { return eventTitle; }
    public String getEventDate()    { return eventDate; }
    public String getEventCity()    { return eventCity; }
}
