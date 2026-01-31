package com.pindrop.pins.controller;

import com.pindrop.pins.model.Pin;
import com.pindrop.pins.service.PinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pins")
@CrossOrigin
public class PinController {

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
        return ResponseEntity.ok(service.createPin(pin));
    }

    @GetMapping
    public ResponseEntity<List<Pin>> listAll() {
        return ResponseEntity.ok(service.listPins());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Pin>> listByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.listPinsByUser(userId));
    }
}
