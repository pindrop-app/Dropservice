package com.pindrop.controller;

import com.pindrop.dto.CreateDropRequest;
import com.pindrop.model.Drop;
import com.pindrop.service.DropService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drops")

public class DropController {

    private final DropService dropService;

    public DropController(DropService dropService) {
        this.dropService = dropService;
    }

    @PostMapping
    public ResponseEntity<Drop> createDrop(@Valid @RequestBody CreateDropRequest req) throws Exception {
        Drop drop = dropService.createDrop(req);
        return ResponseEntity.ok(drop);
    }

    @GetMapping
    public ResponseEntity<List<Drop>> listDrops() throws Exception {
        return ResponseEntity.ok(dropService.listDrops());
    }
}
