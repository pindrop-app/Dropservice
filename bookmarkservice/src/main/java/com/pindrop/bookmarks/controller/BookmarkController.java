package com.pindrop.bookmarks.controller;

import com.pindrop.bookmarks.service.BookmarkService;
import com.pindrop.pins.model.Pin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookmarks")
@CrossOrigin
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Pin>> listPinned(@PathVariable String userId) {
        return ResponseEntity.ok(bookmarkService.listPinnedPins(userId));
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggle(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String pinId = body.get("pinId");
        if (userId == null || userId.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        if (pinId == null || pinId.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "pinId is required"));

        bookmarkService.togglePinIt(userId, pinId);

        Map<String, Object> res = new HashMap<>();
        res.put("userId", userId);
        res.put("pinId", pinId);
        res.put("pinned", bookmarkService.isPinned(userId, pinId));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/has")
    public ResponseEntity<Map<String, Object>> has(@RequestParam String userId, @RequestParam String pinId) {
        boolean pinned = bookmarkService.isPinned(userId, pinId);
        return ResponseEntity.ok(Map.of("userId", userId, "pinId", pinId, "pinned", pinned));
    }
}
