package com.pindrop.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.pindrop.dto.PlaceFromLocationRequest;
import com.pindrop.model.SavedPlace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class PlaceService {

    private final Firestore firestore;
    private final RestTemplate restTemplate;
    private final String googlePlacesApiKey;

    public PlaceService(Firestore firestore,
                        @Value("${google.places.api-key}") String googlePlacesApiKey) {
        this.firestore = firestore;
        this.googlePlacesApiKey = googlePlacesApiKey;
        this.restTemplate = new RestTemplate();
    }

    public SavedPlace saveFromLocation(PlaceFromLocationRequest req) throws Exception {
        double lat = req.getLat();
        double lon = req.getLon();

        // If Google Places API key is not configured, skip the lookup and save a fallback place.
        boolean hasValidApiKey = googlePlacesApiKey != null
                && !googlePlacesApiKey.isBlank()
                && !googlePlacesApiKey.startsWith("YOUR_");

        String name = null;
        String vicinity = null;

        if (hasValidApiKey) {
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=50&type=restaurant|point_of_interest&key=%s",
                    lat, lon, googlePlacesApiKey
            );

            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("results") instanceof List<?> results && !results.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> place = (Map<String, Object>) results.get(0);
                name = place.get("name") != null ? place.get("name").toString() : null;
                vicinity = place.get("vicinity") != null ? place.get("vicinity").toString() : null;
            }
        }

        // Fallback if Google returned nothing (or key not configured)
        if (name == null || name.isBlank()) {
            name = "Current Location";
        }
        if (vicinity == null) {
            vicinity = String.format("Lat %.6f, Lon %.6f", lat, lon);
        }

        SavedPlace saved = new SavedPlace();
        saved.setId(UUID.randomUUID().toString());
        saved.setName(name);
        saved.setAddress(vicinity);
        saved.setLat(lat);
        saved.setLon(lon);
        saved.setCreatedAt(Instant.now().toEpochMilli());

        firestore.collection("saved_places")
                .document(saved.getId())
                .set(saved)
                .get();

        return saved;
    }

    public List<SavedPlace> listSavedPlaces() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = firestore.collection("saved_places")
                .get()
                .get()
                .getDocuments();

        // If the DB is empty, seed a couple of mock places so the frontend has something to show.
        if (docs == null || docs.isEmpty()) {
            List<SavedPlace> seeded = new ArrayList<>();

            SavedPlace p1 = new SavedPlace();
            p1.setId(UUID.randomUUID().toString());
            p1.setName("Jinya Ramen");
            p1.setAddress("Austin, TX");
            p1.setLat(30.2672);
            p1.setLon(-97.7431);
            p1.setCreatedAt(Instant.now().toEpochMilli());
            seeded.add(p1);

            SavedPlace p2 = new SavedPlace();
            p2.setId(UUID.randomUUID().toString());
            p2.setName("Blue Tokai Coffee");
            p2.setAddress("San Antonio, TX");
            p2.setLat(29.4241);
            p2.setLon(-98.4936);
            p2.setCreatedAt(Instant.now().toEpochMilli());
            seeded.add(p2);

            for (SavedPlace p : seeded) {
                firestore.collection("saved_places")
                        .document(p.getId())
                        .set(p)
                        .get();
            }
            return seeded;
        }

        List<SavedPlace> out = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            SavedPlace p = doc.toObject(SavedPlace.class);
            p.setId(doc.getId());
            out.add(p);
        }
        return out;
    }
}
