package com.pindrop.controller;

import com.pindrop.dto.PlaceFromLocationRequest;
import com.pindrop.model.SavedPlace;
import com.pindrop.service.PlaceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
 // Vite default
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @PostMapping("/from-location")
    public ResponseEntity<SavedPlace> saveFromLocation(@Valid @RequestBody PlaceFromLocationRequest req) throws Exception {
        SavedPlace saved = placeService.saveFromLocation(req);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<SavedPlace>> listPlaces() throws Exception {
        return ResponseEntity.ok(placeService.listSavedPlaces());
    }
}
