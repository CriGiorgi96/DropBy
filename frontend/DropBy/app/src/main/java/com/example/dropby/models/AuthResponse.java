package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}
