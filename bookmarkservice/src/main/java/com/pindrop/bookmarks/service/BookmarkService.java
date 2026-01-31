package com.pindrop.bookmarks.service;

import com.pindrop.bookmarks.model.UserBookmark;
import com.pindrop.bookmarks.repo.UserBookmarkRepository;
import com.pindrop.pins.model.Pin;
import com.pindrop.pins.repo.PinRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookmarkService {

    private final UserBookmarkRepository bookmarkRepo;
    private final PinRepository pinRepo;

    public BookmarkService(UserBookmarkRepository bookmarkRepo, PinRepository pinRepo) {
        this.bookmarkRepo = bookmarkRepo;
        this.pinRepo = pinRepo;
    }

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

    public boolean isPinned(String userId, String pinId) {
        return bookmarkRepo.get(userId, pinId) != null;
    }

    public void togglePinIt(String userId, String pinId) {
        UserBookmark current = bookmarkRepo.get(userId, pinId);
        if (current != null) {
            bookmarkRepo.delete(userId, pinId);
            return;
        }
        UserBookmark b = new UserBookmark();
        b.setUserId(userId);
        b.setPinId(pinId);
        b.setSavedAt(Instant.now().toString());
        bookmarkRepo.put(b);
    }
}
