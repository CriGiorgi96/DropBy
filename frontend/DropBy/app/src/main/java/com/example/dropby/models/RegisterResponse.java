package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    @SerializedName("message") public String message;
    @SerializedName("user_id") public int userId;
    @SerializedName("email")   public String email;
    @SerializedName("error")   public String error;

    public int getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public String getEmail() {
        return email;
    }

    public String getError() {
        return error;
    }
}