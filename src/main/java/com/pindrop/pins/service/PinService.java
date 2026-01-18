package com.pindrop.pins.service;

import com.pindrop.bookmarks.model.UserBookmark;
import com.pindrop.bookmarks.repo.UserBookmarkRepository;
import com.pindrop.pins.model.Pin;
import com.pindrop.pins.repo.PinRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PinService {

    private static final Logger log = LoggerFactory.getLogger(PinService.class);


    private final PinRepository pinRepo;
    private final UserBookmarkRepository bookmarkRepo;

    public PinService(PinRepository pinRepo, UserBookmarkRepository bookmarkRepo) {
        this.pinRepo = pinRepo;
        this.bookmarkRepo = bookmarkRepo;
    }

    public Pin createPin(Pin pin) {
        log.info("createPin called userId={}, contentLen={}, lat={}, lon={}, imageUrlPresent={} ", pin.getUserId(), pin.getContent() == null ? 0 : pin.getContent().length(), pin.getLat(), pin.getLon(), pin.getImageUrl()!=null && !pin.getImageUrl().isBlank());
        if (pin.getPinId() == null || pin.getPinId().isBlank()) {
            pin.setPinId(UUID.randomUUID().toString());
        }
        if (pin.getCreatedAt() == null) {
            pin.setCreatedAt(Instant.now().toString());
        }
        pinRepo.put(pin);
        return pin;
    }

    public List<Pin> listPins() {
        return pinRepo.listAll();
    }

    public List<Pin> listPinsByUser(String userId) {
        return pinRepo.listByUserId(userId);
    }

    /**
     * PinIt tab: return pins bookmarked by a user.
     * Source of truth is the UserBookmarks table (PK userId, SK pinId).
     */
    public List<Pin> listPinnedPins(String userId) {
        List<UserBookmark> bookmarks = bookmarkRepo.listByUserId(userId);
        List<Pin> out = new ArrayList<>();
        for (UserBookmark b : bookmarks) {
            if (b.getPinId() == null || b.getPinId().isBlank()) continue;
            Pin p = pinRepo.getById(b.getPinId());
            if (p != null) out.add(p);
        }
        return out;
    }

    /**
     * Toggle bookmark for a user on a pin.
     * Writes to UserBookmarks, does NOT mutate the Pin record.
     */
    public Pin pinIt(String pinId, String userId) {
        log.info("pinIt called userId={}, pinId={}", userId, pinId);
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }

        Pin existing = pinRepo.getById(pinId);
        if (existing == null) {
            return null;
        }

        UserBookmark current = bookmarkRepo.get(userId, pinId);
        if (current != null) {
            bookmarkRepo.delete(userId, pinId);
        } else {
            UserBookmark b = new UserBookmark();
            b.setUserId(userId);
            b.setPinId(pinId);
            b.setSavedAt(Instant.now().toString());
            bookmarkRepo.put(b);
        }

        return existing;
    }
}
