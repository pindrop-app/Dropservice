package com.pindrop.pins.controller;

import com.pindrop.pins.model.Pin;
import com.pindrop.pins.service.PinService;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pins")
public class PinController {

    private static final Logger log = LoggerFactory.getLogger(PinController.class);


    private final PinService service;

    public PinController(PinService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping
    public ResponseEntity<Pin> create(@RequestBody Pin pin) {
        Pin saved = service.createPin(pin);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Pin>> listAll() {
        return ResponseEntity.ok(service.listPins());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Pin>> listByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.listPinsByUser(userId));
    }

    @GetMapping("/pinit/{userId}")
    public ResponseEntity<List<Pin>> listPinnedByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.listPinnedPins(userId));
    }

    @PostMapping("/{pinId}/pinit")
    public ResponseEntity<?> togglePinIt(@PathVariable String pinId, @RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        log.info("Toggle PinIt userId={}, pinId={}", userId, pinId);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }
        Pin updated = service.pinIt(pinId, userId);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }
}
