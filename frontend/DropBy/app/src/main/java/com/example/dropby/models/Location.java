package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Location implements Serializable {
    private static final long serialVersionUID = 2L;

    @SerializedName("id")
    private int id;

    @SerializedName("slug")
    private String slug;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("city")
    private String city;

    @SerializedName("region")
    private String region;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("capacity")
    private int capacity;

    @SerializedName("services")
    private List<String> services;

    @SerializedName("link")
    private String link;

    // ── NUOVI CAMPI IMMAGINE (sostituiscono "images" e "floor_plan" come array/oggetto) ──
    // Il backend restituisce il solo nome file, es. "verona_est.jpg"
    // Il frontend costruisce l'URL completo tramite ApiClient.resolveLocationImage()

    @SerializedName("image_est")
    private String imageEst;          // foto esterna

    @SerializedName("image_int")
    private String imageInt;          // foto interna

    @SerializedName("floor_plan")
    private String floorPlan;         // planimetria (nome file, non più oggetto)

    // ── GETTER ──────────────────────────────────────────────────────────────

    public int getId()               { return id; }
    public String getSlug()          { return slug; }
    public String getName()          { return name; }
    public String getAddress()       { return address; }
    public String getCity()          { return city; }
    public String getRegion()        { return region; }
    public double getLatitude()      { return latitude; }
    public double getLongitude()     { return longitude; }
    public int getCapacity()         { return capacity; }
    public List<String> getServices(){ return services; }
    public String getLink()          { return link; }
    public String getImageEst()      { return imageEst; }
    public String getImageInt()      { return imageInt; }
    public String getFloorPlan()     { return floorPlan; }

    // ── HELPER: costruisce l'URL assoluto passando per ApiClient ────────────
    // Usati da adapter e fragment per evitare di ripetere la logica di path

    public String resolveImageEstUrl() {
        return com.example.dropby.network.ApiClient
                .resolveLocationImage(imageEst, LocationImageType.EXTERIOR);
    }

    public String resolveImageIntUrl() {
        return com.example.dropby.network.ApiClient
                .resolveLocationImage(imageInt, LocationImageType.INTERIOR);
    }

    public String resolveFloorPlanUrl() {
        return com.example.dropby.network.ApiClient
                .resolveLocationImage(floorPlan, LocationImageType.FLOOR_PLAN);
    }

    public enum LocationImageType {
        EXTERIOR,   // → static/images/foto_luoghi/foto_teatri/
        INTERIOR,   // → static/images/foto_luoghi/foto_teatri/
        FLOOR_PLAN  // → static/images/foto_luoghi/planimetrie/
    }
}