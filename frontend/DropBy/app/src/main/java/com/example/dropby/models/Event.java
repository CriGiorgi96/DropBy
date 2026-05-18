package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Event implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("slug")
    private String slug;

    @SerializedName("production_id")
    private int productionId;

    @SerializedName("location_id")
    private int locationId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("city")
    private String city;

    // CORRETTO: Deve corrispondere alla chiave 'date' del JSON
    @SerializedName("event_date")
    private String date;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("duration")
    private String duration;

    @SerializedName("price")
    private String price;

    @SerializedName("ticket_url")
    private String ticketUrl;

    @SerializedName("source_url")
    private String sourceUrl;

    @SerializedName("images")
    private List<EventImage> images;

    public int getId() { return id; }
    public String getSlug() { return slug; }
    public int getProductionId() { return productionId; }
    public int getLocationId() { return locationId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getCity() { return city; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getDuration() { return duration; }
    public String getPrice() { return price; }
    public String getTicketUrl() { return ticketUrl; }
    public String getSourceUrl() { return sourceUrl; }

    public EventImage getMainImage() {
        if (images != null && !images.isEmpty()) return images.get(0);
        return null;
    }

    public List<EventImage> getImages() { return images; }

    public static class EventImage implements Serializable {
        @SerializedName("url")
        private String url;
        @SerializedName("alt")
        private String alt;
        public String getUrl() { return url; }
        public String getAlt() { return alt; }
    }
}