package com.pindrop.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.pindrop.dto.CreateDropRequest;
import com.pindrop.model.Drop;
import com.pindrop.model.SavedPlace;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class DropService {

    private final Firestore firestore;

    public DropService(Firestore firestore) {
        this.firestore = firestore;
    }

    public Drop createDrop(CreateDropRequest req) throws Exception {
        double lat;
        double lon;
        String savedPlaceId = req.getSavedPlaceId();

        if (savedPlaceId != null && !savedPlaceId.isBlank()) {
            DocumentReference ref = firestore.collection("saved_places").document(savedPlaceId);
            SavedPlace place = ref.get().get().toObject(SavedPlace.class);
            if (place == null) {
                throw new IllegalArgumentException("Saved place not found");
            }
            lat = place.getLat();
            lon = place.getLon();
        } else {
            if (req.getLat() == null || req.getLon() == null) {
                throw new IllegalArgumentException("lat/lon required when no savedPlaceId is provided");
            }
            lat = req.getLat();
            lon = req.getLon();
        }

        Drop drop = new Drop();
        drop.setId(UUID.randomUUID().toString());
        drop.setContent(req.getContent());
        drop.setRating(req.getRating());
        drop.setImageUrl(req.getImageUrl());
        drop.setSavedPlaceId(savedPlaceId);
        drop.setLat(lat);
        drop.setLon(lon);
        drop.setCreatedAt(Instant.now().toEpochMilli());

        firestore.collection("drops")
                .document(drop.getId())
                .set(drop)
                .get();

        return drop;
    }

    public List<Drop> listDrops() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = firestore.collection("drops")
                .orderBy("createdAt")
                .get()
                .get()
                .getDocuments();

        List<Drop> out = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            Drop d = doc.toObject(Drop.class);
            d.setId(doc.getId());
            out.add(d);
        }
        return out;
    }
}
