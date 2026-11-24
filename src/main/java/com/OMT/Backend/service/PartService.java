package com.OMT.Backend.service;

import com.OMT.Backend.dto.PartDTO;
import com.OMT.Backend.exception.ResourceNotFoundException;
import com.OMT.Backend.model.Part;
import com.OMT.Backend.model.Vehicle;
import com.OMT.Backend.repository.PartRepository;
import com.OMT.Backend.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PartService {

    private final PartRepository partRepository;
    private final VehicleRepository vehicleRepository;

    // Standard part definitions to seed dynamically per vehicle
    private static final String[][] DEFAULT_PARTS = {
            {"Front Subframe", "BODY PANELS", "true", "EXTRACTED"},
            {"Rocker Panel – Right", "BODY PANELS", "true", "MISSING"},
            {"Roof Panel", "BODY PANELS", "true", "PENDING"},
            {"Front Left Door Mirror", "BODY PANELS", "true", "PENDING"},
            {"Front Right Door Mirror", "BODY PANELS", "true", "EXTRACTED"},
            {"Radiator Support Frame", "BODY PANELS", "true", "PENDING"},
            {"Rocker Panel – Left", "BODY PANELS", "true", "PENDING"},
            {"Front Bumper", "BODY PANELS", "true", "PENDING"},
            {"Engine Block (2.4L)", "ENGINE", "true", "PENDING"},
            {"Transmission Assembly", "ENGINE", "true", "PENDING"},
            {"Alternator 130A", "ELECTRICAL", "true", "PENDING"},
            {"Starter Motor", "ELECTRICAL", "true", "PENDING"},
            {"Front Brake Calipers", "BRAKES", "true", "PENDING"},
            {"Rear Brake Rotors", "BRAKES", "true", "PENDING"},
            {"Front Struts Pair", "SUSPENSION", "true", "PENDING"},
            {"Steering Rack & Pinion", "SUSPENSION", "true", "PENDING"},
            {"Instrument Cluster", "INTERIOR", "true", "PENDING"},
            {"Front Driver Seat", "INTERIOR", "false", "DAMAGED"}
    };

    public PartService(PartRepository partRepository, VehicleRepository vehicleRepository) {
        this.partRepository = partRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public List<PartDTO> getPartsForVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        List<Part> existingParts = partRepository.findByVehicleIdOrderByIdAsc(vehicleId);

        // Auto-seed parts if this vehicle has no parts yet
        if (existingParts.isEmpty()) {
            List<Part> toSave = new ArrayList<>();
            int idx = 1;
            for (String[] def : DEFAULT_PARTS) {
                String code = String.format("%s-%03d", vehicle.getAssetCode(), idx++);
                Part part = Part.builder()
                        .partCode(code)
                        .name(def[0])
                        .category(def[1])
                        .canBeExtracted(Boolean.parseBoolean(def[2]))
                        .status(def[3])
                        .vehicle(vehicle)
                        .build();
                toSave.add(part);
            }
            existingParts = partRepository.saveAll(toSave);
        }

        return existingParts.stream().map(this::toDTO).toList();
    }

    @Transactional
    public PartDTO updateExtraction(Long vehicleId, Long partId, boolean canBeExtracted) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + partId));

        if (!part.getVehicle().getId().equals(vehicleId)) {
            throw new IllegalArgumentException("Part does not belong to specified vehicle");
        }

        part.setCanBeExtracted(canBeExtracted);
        Part saved = partRepository.save(part);
        return toDTO(saved);
    }

    @Transactional
    public PartDTO updateStatus(Long vehicleId, Long partId, String status) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + partId));

        if (!part.getVehicle().getId().equals(vehicleId)) {
            throw new IllegalArgumentException("Part does not belong to specified vehicle");
        }

        part.setStatus(status.toUpperCase().trim());
        Part saved = partRepository.save(part);
        return toDTO(saved);
    }

    private PartDTO toDTO(Part part) {
        return PartDTO.builder()
                .id(part.getId())
                .partCode(part.getPartCode())
                .name(part.getName())
                .category(part.getCategory())
                .canBeExtracted(part.getCanBeExtracted())
                .status(part.getStatus())
                .vehicleId(part.getVehicle().getId())
                .createdAt(part.getCreatedAt())
                .build();
    }
}
