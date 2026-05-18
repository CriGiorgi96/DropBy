package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class UserSearchResult implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    public int getId()          { return id; }
    public String getUsername() { return username; }
    public String getEmail()    { return email; }

    // ── Wrapper per la risposta API ──────────────────────────────────────────
    public static class Response {
        @SerializedName("users")
        private List<UserSearchResult> users;
        public List<UserSearchResult> getUsers() { return users; }
    }
}
