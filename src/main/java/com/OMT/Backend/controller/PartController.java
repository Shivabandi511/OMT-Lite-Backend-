package com.OMT.Backend.controller;

import com.OMT.Backend.dto.PartDTO;
import com.OMT.Backend.service.PartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles/{vehicleId}/parts")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @GetMapping
    public ResponseEntity<List<PartDTO>> getParts(@PathVariable Long vehicleId) {
        List<PartDTO> parts = partService.getPartsForVehicle(vehicleId);
        return ResponseEntity.ok(parts);
    }

    @PatchMapping("/{partId}/extraction")
    public ResponseEntity<PartDTO> updateExtraction(
            @PathVariable Long vehicleId,
            @PathVariable Long partId,
            @RequestBody Map<String, Boolean> body) {
        Boolean canBeExtracted = body.getOrDefault("canBeExtracted", true);
        PartDTO updated = partService.updateExtraction(vehicleId, partId, canBeExtracted);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{partId}/status")
    public ResponseEntity<PartDTO> updateStatus(
            @PathVariable Long vehicleId,
            @PathVariable Long partId,
            @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "PENDING");
        PartDTO updated = partService.updateStatus(vehicleId, partId, status);
        return ResponseEntity.ok(updated);
    }
}
