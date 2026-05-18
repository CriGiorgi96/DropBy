package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")    public String token;
    @SerializedName("user_id")  public int userId;
    @SerializedName("username") public String username;
    @SerializedName("gender")   public String gender;
    @SerializedName("email")    public String email;
    @SerializedName("error")    public String error;
}
