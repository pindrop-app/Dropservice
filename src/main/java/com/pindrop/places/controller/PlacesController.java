package com.pindrop.places.controller;

import com.pindrop.places.service.PlacesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/places")
public class PlacesController {
    private static final Logger log = LoggerFactory.getLogger(PlacesController.class);
    private final PlacesService placesService;

    public PlacesController(PlacesService placesService) {
        this.placesService = placesService;
    }

    @GetMapping("/reverse")
    public ResponseEntity<?> reverse(@RequestParam("lat") double lat, @RequestParam("lon") double lon) {
        log.info("Reverse geocode request lat={}, lon={}", lat, lon);
        Map<String, String> result = placesService.reverseGeocode(lat, lon);
        return ResponseEntity.ok(result);
    }
}
