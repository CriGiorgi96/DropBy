package com.example.dropby.models;

<<<<<<< HEAD
import androidx.annotation.Nullable;
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Review implements Serializable {

    @SerializedName("id")
<<<<<<< HEAD
    @Nullable
    private Integer id;

    @SerializedName("user_id")
    @Nullable
    private Integer userId;
=======
    private int id;

    @SerializedName("user_id")
    private int userId;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

    @SerializedName("username")
    private String username;

    @SerializedName("event_id")
<<<<<<< HEAD
    @Nullable
    private Integer eventId;
=======
    private int eventId;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

    @SerializedName("event_title")
    private String eventTitle;

    @SerializedName("rating")
<<<<<<< HEAD
    @Nullable
    private Integer rating;
=======
    private int rating;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

    @SerializedName("comment")
    private String comment;

    @SerializedName("created_at")
    private String createdAt;

<<<<<<< HEAD
    @SerializedName("event_date")
    private String eventDate;

    @SerializedName("event_city")
    private String eventCity;

    @SerializedName("location_name")
    private String locationName;

    @SerializedName("profile_image")
    private String profileImage;

    public int    getId()              { return id      != null ? id      : 0; }
    public int    getUserId()          { return userId  != null ? userId  : 0; }
    public String getUsername()        { return username; }
    public String getProfileImage()    { return profileImage; }
    public int    getEventId()         { return eventId != null ? eventId : 0; }
    public String getEventTitle()      { return eventTitle; }
    public int    getRating()          { return rating  != null ? rating  : 0; }
    public String getComment()         { return comment; }
    public String getCreatedAt()       { return createdAt; }
    public String getEventDate()       { return eventDate; }
    public String getEventCity()       { return eventCity; }
    public String getLocationName()    { return locationName; }

    public String getStarsString() {
        int r = getRating();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= r ? "★" : "☆");
=======
    public int getId()           { return id; }
    public int getUserId()       { return userId; }
    public String getUsername()  { return username; }
    public int getEventId()      { return eventId; }
    public String getEventTitle(){ return eventTitle; }
    public int getRating()       { return rating; }
    public String getComment()   { return comment; }
    public String getCreatedAt() { return createdAt; }

    /** Restituisce le stelle come stringa (es. "★★★★☆" per 4/5) */
    public String getStarsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= rating ? "★" : "☆");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        return sb.toString();
    }
}
