package com.pindrop.drops.controller;

import com.pindrop.drops.dto.CreateDropRequest;
import com.pindrop.pins.model.Pin;
import com.pindrop.pins.service.PinService;
import com.pindrop.places.service.PlacesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/drops")
public class DropController {

    private static final Logger log = LoggerFactory.getLogger(DropController.class);

    private final PinService pinService;
    private final PlacesService placesService;

    public DropController(PinService pinService, PlacesService placesService) {
        this.pinService = pinService;
        this.placesService = placesService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping
    public ResponseEntity<List<Pin>> listDrops() {
        log.info("List drops");
        return ResponseEntity.ok(pinService.listPins());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Pin>> listDropsByUser(@PathVariable String userId) {
        log.info("List drops by userId={}", userId);
        return ResponseEntity.ok(pinService.listPinsByUser(userId));
    }

    @PostMapping
    public ResponseEntity<?> createDrop(@RequestBody CreateDropRequest req) {
        log.info("Create drop userId={}, savedPlaceId={}, lat={}, lon={}, contentLen={}",
                req.getUserId(), req.getSavedPlaceId(), req.getLat(), req.getLon(),
                req.getContent() == null ? 0 : req.getContent().length());

        if (req.getUserId() == null || req.getUserId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }

        Pin p = new Pin();
        p.setUserId(req.getUserId());
        p.setContent(req.getContent() == null ? "" : req.getContent());
        if (req.getRating() != null) p.setRating(req.getRating());
        p.setImageUrl(req.getImageUrl());

        String savedPlaceId = (req.getSavedPlaceId() == null || req.getSavedPlaceId().isBlank())
                ? "current-location"
                : req.getSavedPlaceId();
        p.setSavedPlaceId(savedPlaceId);

        String locationName = (req.getLocationName() == null || req.getLocationName().isBlank())
                ? "Current Location"
                : req.getLocationName();

        // Enrich current location via Google reverse geocoding (best-effort)
        if ("current-location".equals(savedPlaceId) && req.getLat() != null && req.getLon() != null) {
            var geo = placesService.reverseGeocode(req.getLat(), req.getLon());
            String name = geo.getOrDefault("name", "Current location");
            String addr = geo.getOrDefault("formattedAddress", "");
            if (name != null && !name.isBlank()) {
                locationName = (addr == null || addr.isBlank()) ? name : (name + " | " + addr);
            }
        }
        p.setLocationName(locationName);

        if (req.getLat() != null) p.setLat(req.getLat());
        if (req.getLon() != null) p.setLon(req.getLon());

        Pin created = pinService.createPin(p);
        return ResponseEntity.ok(created);
    }
}
