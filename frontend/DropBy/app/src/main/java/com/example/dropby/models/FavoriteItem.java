package com.example.dropby.models;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class FavoriteItem implements Serializable {

    @SerializedName("favorite_id")
    @Nullable
    private Integer favoriteId;

    @SerializedName("event_id")
    @Nullable
    private Integer eventId;

    @SerializedName("title")
    private String title;

    @SerializedName("category")
    private String category;

    @SerializedName("city")
    private String city;

    @SerializedName("event_date")
    private String eventDate;

    // Il backend può restituire price come stringa o come numero decimale
    @SerializedName("price")
    @Nullable
    private Object price;

    @SerializedName("images")
    private List<Event.EventImage> images;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("created_at")
    private String createdAt;

    public int    getFavoriteId()  { return favoriteId != null ? favoriteId : 0; }
    public int    getEventId()     { return eventId    != null ? eventId    : 0; }
    public String getTitle()       { return title; }
    public String getCategory()    { return category; }
    public String getCity()        { return city; }
    public String getEventDate()   { return eventDate; }
    public String getPrice()       { return price != null ? price.toString() : null; }
    public String getCreatedAt()   { return createdAt; }

    public String getMainImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty()) return imageUrl;
        if (images  != null && !images.isEmpty())   return images.get(0).getUrl();
        return null;
    }
}
