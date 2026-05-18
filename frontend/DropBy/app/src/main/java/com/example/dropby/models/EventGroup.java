package com.example.dropby.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta una produzione teatrale con tutte le sue repliche.
 * Non arriva dal backend: viene costruito lato client raggruppando
 * la lista piatta di Event per production_id.
 *
 * Uso:
 *   List<EventGroup> groups = EventGroup.from(flatEvents);
 */
public class EventGroup {

    /** Una singola replica: data + orario + (opzionale) nome del teatro */
    public static class Occurrence {
        public final String date;
        public final String startTime;
        public final String locationName; // null se non disponibile

        public Occurrence(String date, String startTime, String locationName) {
            this.date         = date      != null ? date      : "—";
            this.startTime    = startTime != null ? startTime : "—";
            this.locationName = locationName;
        }

        /** Stringa leggibile: "12 mag · 21:00" o "12 mag · 21:00 — Teatro X" */
        public String toDisplayString(boolean includeVenue) {
            StringBuilder sb = new StringBuilder();
            sb.append(date).append(" · ").append(startTime);
            if (includeVenue && locationName != null && !locationName.isEmpty()) {
                sb.append(" — ").append(locationName);
            }
            return sb.toString();
        }
    }

    private final Event representative; // primo evento (per titolo, immagine, categoria…)
    private final List<Occurrence> occurrences = new ArrayList<>();

    public EventGroup(Event representative) {
        this.representative = representative;
    }

    public void addOccurrence(Occurrence o) {
        occurrences.add(o);
    }

    public Event getRepresentative()     { return representative; }
    public List<Occurrence> getOccurrences() { return occurrences; }

    // Shortcut usati dagli adapter
    public String getTitle()    { return representative.getTitle(); }
    public String getCategory() { return representative.getCategory(); }
    public Event.EventImage getMainImage() { return representative.getMainImage(); }

    /**
     * Trasforma una lista piatta di Event in una lista di EventGroup.
     *
     * @param events   lista piatta restituita dall'API
     * @param withVenue true = includi il nome del teatro in ogni Occurrence
     *                  (utile in EventDetailFragment, inutile in TheaterDetailFragment)
     */
    public static List<EventGroup> from(List<Event> events, boolean withVenue) {
        if (events == null) return new ArrayList<>();

        // Mantiene l'ordine di prima apparizione (LinkedHashMap)
        java.util.LinkedHashMap<Integer, EventGroup> map = new java.util.LinkedHashMap<>();

        // Ordina per data prima di raggruppare (così la prima occurrence è quella più vicina)
        List<Event> sorted = new ArrayList<>(events);
        java.util.Collections.sort(sorted, (a, b) -> {
            if (a.getDate() == null && b.getDate() == null) return 0;
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            return a.getDate().compareTo(b.getDate());
        });

        for (Event e : sorted) {
            int key = e.getProductionId() != 0 ? e.getProductionId() : e.getId();
            if (!map.containsKey(key)) {
                map.put(key, new EventGroup(e));
            }
            String venueName = withVenue ? e.getCity() : null; // fallback: città
            map.get(key).addOccurrence(new Occurrence(e.getDate(), e.getStartTime(), venueName));
        }

        return new ArrayList<>(map.values());
    }
}