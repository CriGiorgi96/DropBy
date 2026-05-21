package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class FavoriteItem implements Serializable {

    @SerializedName("favorite_id")
    private int favoriteId;

    @SerializedName("event_id")
    private int eventId;

    @SerializedName("title")
    private String title;

    @SerializedName("category")
    private String category;

    @SerializedName("city")
    private String city;

    @SerializedName("event_date")
    private String eventDate;

    @SerializedName("price")
    private String price;

    @SerializedName("images")
    private List<Event.EventImage> images;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("created_at")
    private String createdAt;

    public int getFavoriteId()  { return favoriteId; }
    public int getEventId()     { return eventId; }
    public String getTitle()    { return title; }
    public String getCategory() { return category; }
    public String getCity()     { return city; }
    public String getEventDate(){ return eventDate; }
    public String getPrice()    { return price; }
    public String getCreatedAt(){ return createdAt; }

    public String getMainImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty()) return imageUrl;
        if (images != null && !images.isEmpty()) return images.get(0).getUrl();
        return null;
    }
}
