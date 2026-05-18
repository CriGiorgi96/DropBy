package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Friend implements Serializable {

    @SerializedName("friendship_id")
    private int friendshipId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    public int getFriendshipId() { return friendshipId; }
    public int getUserId()       { return userId; }
    public String getUsername()  { return username; }
    public String getEmail()     { return email; }
    public String getStatus()    { return status; }
    public String getCreatedAt() { return createdAt; }
}
