package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;

public class OtpResponse {
    @SerializedName("message")           public String  message;
    @SerializedName("error")             public String  error;
    @SerializedName("user_id")           public int     userId;
    @SerializedName("username")          public String  username;
    @SerializedName("email")             public String  email;
    @SerializedName("gender")            public String  gender;
    @SerializedName("token")             public String  token;
    @SerializedName("profile_image")     public String  profileImage;
    @SerializedName("private_favorites") public boolean privateFavorites;
    @SerializedName("private_reviews")   public boolean privateReviews;
}
