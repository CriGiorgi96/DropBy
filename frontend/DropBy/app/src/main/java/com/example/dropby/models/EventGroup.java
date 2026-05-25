package com.example.dropby.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Raggruppa un Event con le sue repliche come Occurrence.
 * In precedenza veniva costruito lato client da una lista piatta di eventi;
 * ora il backend restituisce già un Event per opera con l'array "replicas",
 * quindi from() crea semplicemente un gruppo per ogni Event ricevuto.
 */
public class EventGroup {

    /** Una singola replica: data + orario + (opzionale) nome del teatro. */
    public static class Occurrence {
        public final String date;
        public final String startTime;
        public final String locationName;

        public Occurrence(String date, String startTime, String locationName) {
            this.date         = date      != null ? date      : "—";
            this.startTime    = startTime != null ? startTime : "";
            this.locationName = locationName;
        }

        /** "12 mag · 21:00" oppure "12 mag · 21:00 — Teatro X" */
        public String toDisplayString(boolean includeVenue) {
            StringBuilder sb = new StringBuilder(date);
            if (startTime != null && !startTime.isEmpty()) {
                sb.append(" · ").append(startTime);
            }
            if (includeVenue && locationName != null && !locationName.isEmpty()) {
                sb.append(" — ").append(locationName);
            }
            return sb.toString();
        }
    }

    private final Event           representative;
    private final List<Occurrence> occurrences = new ArrayList<>();

    public EventGroup(Event representative) {
        this.representative = representative;
    }

    public void addOccurrence(Occurrence o) {
        occurrences.add(o);
    }

    public Event             getRepresentative() { return representative; }
    public List<Occurrence>  getOccurrences()    { return occurrences; }

    public String            getTitle()    { return representative.getTitle(); }
    public String            getCategory() { return representative.getCategory(); }
    public Event.EventImage  getMainImage(){ return representative.getMainImage(); }

    /**
     * Trasforma una lista di Event (già uno per opera, con replicas embedded)
     * in una lista di EventGroup pronti per l'adapter.
     *
     * Un EventGroup distinto viene creato per OGNI event.getId() univoco.
     * Due spettacoli diversi nello stesso teatro producono sempre Card separate.
     *
     * @param events    lista restituita dall'API
     * @param withVenue true = includi il nome del teatro in ogni Occurrence
     */
    public static List<EventGroup> from(List<Event> events, boolean withVenue) {
        if (events == null) {
            Log.d("EventGroup", "from() called with null events");
            return new ArrayList<>();
        }
        Log.d("EventGroup", "from() input size: " + events.size());

        // LinkedHashMap garantisce un gruppo per ID univoco, preservando l'ordine
        Map<Integer, EventGroup> byId = new LinkedHashMap<>();

        for (Event e : events) {
            int eventId = e.getId();
            Log.d("EventGroup", "processing event id=" + eventId + " title=" + e.getTitle());

            // Ogni event_id univoco → un solo EventGroup
            EventGroup g = byId.get(eventId);
            if (g == null) {
                g = new EventGroup(e);
                byId.put(eventId, g);
            }

            List<Event.EventReplica> replicas = e.getReplicas();
            if (replicas != null && !replicas.isEmpty()) {
                Log.d("EventGroup", "  replicas count: " + replicas.size());
                for (Event.EventReplica r : replicas) {
                    String venue = withVenue ? r.getLocationName() : null;
                    g.addOccurrence(new Occurrence(r.getDatePart(), r.getTimePart(), venue));
                }
            } else {
                // Fallback: usa next_show_date se non ci sono repliche nell'array
                Log.d("EventGroup", "  no replicas, using nextShowDate");
                String showDate = e.getNextShowDate();
                String datePart = "", timePart = "";
                if (showDate != null) {
                    int t = showDate.indexOf('T');
                    datePart = t > 0 ? showDate.substring(0, t) : showDate;
                    if (t >= 0 && t + 1 < showDate.length()) {
                        String time = showDate.substring(t + 1);
                        timePart = time.length() >= 5 ? time.substring(0, 5) : time;
                    }
                }
                String venue = withVenue ? e.getNextLocationName() : null;
                g.addOccurrence(new Occurrence(datePart, timePart, venue));
            }
        }

        List<EventGroup> result = new ArrayList<>(byId.values());
        Log.d("EventGroup", "from() output size: " + result.size());
        return result;
    }
}
