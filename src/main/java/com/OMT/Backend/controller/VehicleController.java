package com.OMT.Backend.controller;

import com.OMT.Backend.dto.*;
import com.OMT.Backend.model.RoutingDecision;
import com.OMT.Backend.model.VehicleStatus;
import com.OMT.Backend.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public ResponseEntity<List<VehicleDTO>> getAllVehicles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long yardId,
            @RequestParam(required = false) RoutingDecision routingDecision,
            @RequestParam(required = false) VehicleStatus status) {
        List<VehicleDTO> vehicles = vehicleService.getAllVehicles(search, yardId, routingDecision, status);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> getVehicleById(@PathVariable Long id) {
        VehicleDTO vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    @PostMapping
    public ResponseEntity<VehicleDTO> createVehicle(@Valid @RequestBody VehicleCreateRequest request) {
        VehicleDTO created = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleDTO> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleUpdateRequest request) {
        VehicleDTO updated = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VehicleDTO> updateVehicleStatus(
            @PathVariable Long id,
            @Valid @RequestBody VehicleStatusRequest request) {
        VehicleDTO updated = vehicleService.updateVehicleStatus(id, request);
        return ResponseEntity.ok(updated);
    }
}

