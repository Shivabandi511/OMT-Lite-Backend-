package com.OMT.Backend.controller;

import com.OMT.Backend.dto.YardCreateRequest;
import com.OMT.Backend.dto.YardDTO;
import com.OMT.Backend.dto.YardStatusRequest;
import com.OMT.Backend.dto.YardUpdateRequest;
import com.OMT.Backend.model.YardStatus;
import com.OMT.Backend.service.YardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/yards")
public class YardController {

    private final YardService yardService;

    public YardController(YardService yardService) {
        this.yardService = yardService;
    }

    @GetMapping
    public ResponseEntity<List<YardDTO>> getAllYards(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) YardStatus status) {
        List<YardDTO> yards = yardService.getAllYards(search, status);
        return ResponseEntity.ok(yards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<YardDTO> getYardById(@PathVariable Long id) {
        YardDTO yard = yardService.getYardById(id);
        return ResponseEntity.ok(yard);
    }

    @PostMapping
    public ResponseEntity<YardDTO> createYard(@Valid @RequestBody YardCreateRequest request) {
        YardDTO created = yardService.createYard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<YardDTO> updateYard(@PathVariable Long id, @Valid @RequestBody YardUpdateRequest request) {
        YardDTO updated = yardService.updateYard(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<YardDTO> updateYardStatus(@PathVariable Long id, @Valid @RequestBody YardStatusRequest request) {
        YardDTO updated = yardService.updateYardStatus(id, request);
        return ResponseEntity.ok(updated);
    }
}

