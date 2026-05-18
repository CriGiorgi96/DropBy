package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("username")   public String username;
    @SerializedName("email")      public String email;
    @SerializedName("password")   public String password;
    @SerializedName("first_name") public String firstName;
    @SerializedName("last_name")  public String lastName;
    @SerializedName("birthday")   public String birthday;
    @SerializedName("gender")     public String gender;

    public RegisterRequest(String username, String email, String password,
                           String firstName, String lastName,
                           String birthday, String gender) {
        this.username  = username;
        this.email     = email;
        this.password  = password;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.birthday  = birthday;
        this.gender    = gender;
    }
}
