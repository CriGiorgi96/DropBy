package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LocationsResponse {
    @SerializedName("locations")
    private List<Location> locations;

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }
}