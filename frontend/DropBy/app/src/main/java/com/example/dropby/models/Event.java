package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("slug")
    private String slug;

    @SerializedName("title")
    private String title;

    @SerializedName("title_eng")
    private String titleEng;

    @SerializedName("description")
    private String description;

    @SerializedName("description_eng")
    private String descriptionEng;

    @SerializedName("category")
    private String category;

    @SerializedName("duration")
    private String duration;

    @SerializedName("info_url_it")
    private String infoUrlIt;

    @SerializedName("info_url_eng")
    private String infoUrlEng;

    @SerializedName("images")
    private List<EventImage> images;

    @SerializedName("avg_rating")
    private double avgRating;

    @SerializedName("review_count")
    private int reviewCount;

    @SerializedName("replicas")
    private List<EventReplica> replicas;

    @SerializedName("next_show_date")
    private String nextShowDate;

    @SerializedName("next_location_name")
    private String nextLocationName;

    // ── Getter ───────────────────────────────────────────────────────────────

    public int    getId()               { return id; }
    public String getSlug()             { return slug; }
    public String getTitle()            { return title; }
    public String getTitleEng()         { return titleEng; }
    public String getDescription()      { return description; }
    public String getDescriptionEng()   { return descriptionEng; }
    public String getCategory()         { return category; }
    public String getDuration()         { return duration; }
    public String getInfoUrlIt()        { return infoUrlIt; }
    public String getInfoUrlEng()       { return infoUrlEng; }
    public double getAvgRating()        { return avgRating; }
    public int    getReviewCount()      { return reviewCount; }
    public List<EventReplica> getReplicas()  { return replicas; }
    public String getNextShowDate()     { return nextShowDate; }
    public String getNextLocationName() { return nextLocationName; }

    public List<EventImage> getImages() { return images; }

    public EventImage getMainImage() {
        if (images != null && !images.isEmpty()) return images.get(0);
        return null;
    }

    /**
     * Prima replica NON ancora avvenuta (le repliche arrivano ordinate ASC).
     * Quando include_past=true il backend restituisce anche quelle passate
     * (is_past=true), quindi non basta prendere replicas.get(0).
     */
    public EventReplica getNextReplica() {
        if (replicas == null || replicas.isEmpty()) return null;
        for (EventReplica r : replicas) {
            if (!r.isPast()) return r;
        }
        return null;
    }

    /** Repliche non ancora passate (filtra quelle con is_past=true nel dettaglio). */
    public List<EventReplica> getUpcomingReplicas() {
        List<EventReplica> result = new ArrayList<>();
        if (replicas == null) return result;
        for (EventReplica r : replicas) {
            if (!r.isPast()) result.add(r);
        }
        return result;
    }

    // ── Inner classes ─────────────────────────────────────────────────────────

    public static class EventImage implements Serializable {
        @SerializedName("url")  private String url;
        @SerializedName("alt")  private String alt;
        public String getUrl() { return url; }
        public String getAlt() { return alt; }
    }

    public static class EventReplica implements Serializable {
        @SerializedName("id")                 private int    id;
        @SerializedName("show_date")          private String showDate;
        @SerializedName("ticket_url")         private String ticketUrl;
        @SerializedName("location_id")        private int    locationId;
        @SerializedName("location_name")      private String locationName;
        @SerializedName("location_city")      private String locationCity;
        @SerializedName("location_image_est") private String locationImageEst;
        @SerializedName("location_image_int") private String locationImageInt;
        @SerializedName("is_past")            private boolean past;

        public int    getId()                { return id; }
        public String getShowDate()          { return showDate; }
        public String getTicketUrl()         { return ticketUrl; }
        public int    getLocationId()        { return locationId; }
        public String getLocationName()      { return locationName; }
        public String getLocationCity()      { return locationCity; }
        public String getLocationImageEst()  { return locationImageEst; }
        public String getLocationImageInt()  { return locationImageInt; }
        public boolean isPast()              { return past; }

        /** Parte data dell'ISO timestamp: "2026-06-15T20:00:00" → "2026-06-15" */
        public String getDatePart() {
            if (showDate == null) return "";
            int t = showDate.indexOf('T');
            return t > 0 ? showDate.substring(0, t) : showDate;
        }

        /** Parte orario dell'ISO timestamp: "2026-06-15T20:00:00" → "20:00" */
        public String getTimePart() {
            if (showDate == null) return "";
            int t = showDate.indexOf('T');
            if (t < 0 || t + 1 >= showDate.length()) return "";
            String time = showDate.substring(t + 1);
            return time.length() >= 5 ? time.substring(0, 5) : time;
        }
    }
}
