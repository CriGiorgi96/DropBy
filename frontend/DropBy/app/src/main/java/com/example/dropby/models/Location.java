package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Location implements Serializable {
    private static final long serialVersionUID = 3L;

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
    private String floorPlan;

    @SerializedName("description_it")
    private String descriptionIt;

    @SerializedName("description_en")
    private String descriptionEn;

    @SerializedName("services_it")
    private List<String> servicesIt;

    @SerializedName("events")
    private List<LocationEvent> events;

    // ── GETTER ──────────────────────────────────────────────────────────────

    public int getId()                       { return id; }
    public String getSlug()                  { return slug; }
    public String getName()                  { return name; }
    public String getAddress()               { return address; }
    public String getCity()                  { return city; }
    public String getRegion()                { return region; }
    public double getLatitude()              { return latitude; }
    public double getLongitude()             { return longitude; }
    public int getCapacity()                 { return capacity; }
    public List<String> getServices()        { return services; }
    public List<String> getServicesIt()      { return servicesIt; }
    public String getLink()                  { return link; }
    public String getImageEst()              { return imageEst; }
    public String getImageInt()              { return imageInt; }
    public String getFloorPlan()             { return floorPlan; }
    public String getDescriptionIt()         { return descriptionIt; }
    public String getDescriptionEn()         { return descriptionEn; }
    public List<LocationEvent> getEvents()   { return events; }

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
        EXTERIOR,
        INTERIOR,
        FLOOR_PLAN
    }

    // ── Inner class: singola replica in programma al teatro ──────────────────

    public static class LocationEvent implements Serializable {

        @SerializedName("replica_id")
        private int replicaId;

        @SerializedName("event_id")
        private int eventId;

        @SerializedName("event_title")
        private String eventTitle;

        @SerializedName("event_title_eng")
        private String eventTitleEng;

        @SerializedName("event_category")
        private String eventCategory;

        @SerializedName("event_duration")
        private String eventDuration;

        @SerializedName("show_date")
        private String showDate;

        @SerializedName("ticket_url")
        private String ticketUrl;

        @SerializedName("is_past")
        private boolean past;

        public int    getReplicaId()      { return replicaId; }
        public int    getEventId()        { return eventId; }
        public String getEventTitle()     { return eventTitle; }
        public String getEventTitleEng()  { return eventTitleEng; }
        public String getEventCategory()  { return eventCategory; }
        public String getEventDuration()  { return eventDuration; }
        public String getShowDate()       { return showDate; }
        public String getTicketUrl()      { return ticketUrl; }
        public boolean isPast()           { return past; }

        /** "2026-06-15T20:00:00" → "2026-06-15" */
        public String getDatePart() {
            if (showDate == null) return "";
            int t = showDate.indexOf('T');
            return t > 0 ? showDate.substring(0, t) : showDate;
        }

        /** "2026-06-15T20:00:00" → "20:00" */
        public String getTimePart() {
            if (showDate == null) return "";
            int t = showDate.indexOf('T');
            if (t < 0 || t + 1 >= showDate.length()) return "";
            String time = showDate.substring(t + 1);
            return time.length() >= 5 ? time.substring(0, 5) : time;
        }
    }
}