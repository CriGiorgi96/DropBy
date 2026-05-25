package com.example.dropby.models;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
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

    // "event_date" = alias SQL del repository; "show_date" = colonna nativa event_replicas
    @SerializedName(value = "event_date", alternate = {"show_date"})
    @Nullable
    private String eventDate;

    // Il backend invia price come REAL (numero JSON); Double evita JsonSyntaxException
    @SerializedName("price")
    @Nullable
    private Double price;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("created_at")
    private String createdAt;

    public int    getFavoriteId()  { return favoriteId != null ? favoriteId : 0; }
    public int    getEventId()     { return eventId    != null ? eventId    : 0; }
    public String getTitle()       { return title    != null ? title    : ""; }
    public String getCategory()    { return category != null ? category : ""; }
    public String getCity()        { return city     != null ? city     : ""; }
    public String getCreatedAt()   { return createdAt != null ? createdAt : ""; }
    public String getPrice()       { return (price != null && price > 0) ? String.format("%.2f €", price) : "Gratuito"; }

    /**
     * Restituisce la parte data in formato "YYYY-MM-DD".
     * Gestisce sia date pure ("2026-06-15") che timestamp ISO ("2026-06-15T20:00:00").
     * Ritorna "" se il campo è assente o null (mai NullPointerException nel Fragment).
     */
    public String getEventDate() {
        if (eventDate == null || eventDate.isEmpty()) return "";
        int t = eventDate.indexOf('T');
        return t > 0 ? eventDate.substring(0, t) : eventDate;
    }

    public String getMainImageUrl() {
        return (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : null;
    }
}
