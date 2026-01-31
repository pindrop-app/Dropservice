package com.pindrop.pins.service;

import com.pindrop.pins.model.Pin;
import com.pindrop.pins.repo.PinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PinService {

    private static final Logger log = LoggerFactory.getLogger(PinService.class);

    private final PinRepository pinRepo;

    public PinService(PinRepository pinRepo) {
        this.pinRepo = pinRepo;
    }

    // Create a new drop (post)
    public Pin createPin(Pin input) {
        log.info("createPin userId={}, contentLen={}, hasImage={}, savedPlaceId={}",
                safe(input.getUserId()),
                input.getContent() == null ? 0 : input.getContent().length(),
                input.getImageUrl() != null,
                safe(input.getSavedPlaceId()));

        Pin pin = new Pin();

        // Your Pin model uses pinId as the DynamoDB partition key
        pin.setPinId(UUID.randomUUID().toString());

        pin.setUserId(input.getUserId());
        pin.setContent(input.getContent());
        pin.setRating(input.getRating());
        pin.setImageUrl(input.getImageUrl());

        pin.setSavedPlaceId(input.getSavedPlaceId());
        pin.setLocationName(input.getLocationName());
        pin.setLat(input.getLat());
        pin.setLon(input.getLon());

        pin.setCreatedAt(Instant.now().toString());

        // Bookmarks are in a separate service now, so no pinnedBy/pinnedAt logic here

        pinRepo.put(pin);
        log.info("createPin done pinId={} userId={}", safe(pin.getPinId()), safe(pin.getUserId()));
        return pin;
    }

    // Home feed
    public List<Pin> listPins() {
        log.debug("listPins");
        return pinRepo.listAll();
    }

    // User profile feed
    public List<Pin> listPinsByUser(String userId) {
        log.debug("listPinsByUser userId={}", safe(userId));
        return pinRepo.listByUserId(userId);
    }

    // Single pin lookup (useful for bookmark service etc.)
    public Pin getById(String pinId) {
        log.debug("getById pinId={}", safe(pinId));
        return pinRepo.getById(pinId);
    }

    private static String safe(String v) {
        return v == null ? "null" : v;
    }
}
