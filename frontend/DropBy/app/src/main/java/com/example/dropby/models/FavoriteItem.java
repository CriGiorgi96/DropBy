package com.example.dropby.models;

<<<<<<< HEAD
import androidx.annotation.Nullable;
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class FavoriteItem implements Serializable {

    @SerializedName("favorite_id")
<<<<<<< HEAD
    @Nullable
    private Integer favoriteId;

    @SerializedName("event_id")
    @Nullable
    private Integer eventId;
=======
    private int favoriteId;

    @SerializedName("event_id")
    private int eventId;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

    @SerializedName("title")
    private String title;

    @SerializedName("category")
    private String category;

    @SerializedName("city")
    private String city;

    @SerializedName("event_date")
    private String eventDate;

<<<<<<< HEAD
    // Il backend può restituire price come stringa o come numero decimale
    @SerializedName("price")
    @Nullable
    private Object price;
=======
    @SerializedName("price")
    private String price;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

    @SerializedName("images")
    private List<Event.EventImage> images;

<<<<<<< HEAD
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
=======
    @SerializedName("created_at")
    private String createdAt;

    public int getFavoriteId() { return favoriteId; }
    public int getEventId()    { return eventId; }
    public String getTitle()   { return title; }
    public String getCategory(){ return category; }
    public String getCity()    { return city; }
    public String getEventDate(){ return eventDate; }
    public String getPrice()   { return price; }
    public String getCreatedAt(){ return createdAt; }

    public String getMainImageUrl() {
        if (images != null && !images.isEmpty()) return images.get(0).getUrl();
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        return null;
    }
}
