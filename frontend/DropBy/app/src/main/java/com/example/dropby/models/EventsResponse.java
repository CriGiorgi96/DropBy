package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EventsResponse {

    @SerializedName("events")
    private List<Event> events;

    public List<Event> getEvents() {
        return events;
    }
}