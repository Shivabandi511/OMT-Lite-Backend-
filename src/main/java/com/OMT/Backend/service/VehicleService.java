package com.OMT.Backend.service;

import com.OMT.Backend.dto.*;
import com.OMT.Backend.exception.BadRequestException;
import com.OMT.Backend.exception.ConflictException;
import com.OMT.Backend.exception.ResourceNotFoundException;
import com.OMT.Backend.model.*;
import com.OMT.Backend.repository.VehicleRepository;
import com.OMT.Backend.repository.YardRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Za-hj-npr-z0-9]{17}$");

    private final VehicleRepository vehicleRepository;
    private final YardRepository yardRepository;

    public VehicleService(VehicleRepository vehicleRepository, YardRepository yardRepository) {
        this.vehicleRepository = vehicleRepository;
        this.yardRepository = yardRepository;
    }

    @Transactional(readOnly = true)
    public List<VehicleDTO> getAllVehicles(String search, Long yardId, RoutingDecision routingDecision, VehicleStatus status) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return vehicleRepository.searchVehicles(cleanSearch, yardId, routingDecision, status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehicleDTO getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));
        return toDTO(vehicle);
    }

    @Transactional
    public VehicleDTO createVehicle(VehicleCreateRequest request) {
        verifyCanOperate();
        String normalizedVin = request.getVin().trim().toUpperCase();

        // Validate VIN format (17 chars, no I, O, Q)
        if (!VIN_PATTERN.matcher(normalizedVin).matches()) {
            throw new BadRequestException("VIN must be exactly 17 alphanumeric characters and cannot contain letters I, O, or Q.");
        }

        // Validate VIN uniqueness
        if (vehicleRepository.existsByVin(normalizedVin)) {
            throw new ConflictException("A vehicle with VIN '" + normalizedVin + "' already exists in the system.");
        }

        // Validate Yard exists and is ACTIVE
        Yard yard = yardRepository.findById(request.getYardId())
                .orElseThrow(() -> new ResourceNotFoundException("Yard not found with ID: " + request.getYardId()));

        if (yard.getStatus() != YardStatus.ACTIVE) {
            throw new BadRequestException("Cannot register vehicle under inactive yard '" + yard.getName() + "'. Please choose an active yard.");
        }

        String generatedAssetCode = generateNextAssetCode();

        Vehicle vehicle = Vehicle.builder()
                .assetCode(generatedAssetCode)
                .vin(normalizedVin)
                .make(request.getMake().trim())
                .model(request.getModel().trim())
                .manufactureYear(request.getManufactureYear())
                .yard(yard)
                .routingDecision(request.getRoutingDecision())
                .status(VehicleStatus.RECEIVED)
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        return toDTO(saved);
    }

    @Transactional
    public VehicleDTO updateVehicle(Long id, VehicleUpdateRequest request) {
        verifyCanOperate();
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        // Completed vehicles are read-only
        if (vehicle.getStatus() == VehicleStatus.COMPLETED) {
            throw new BadRequestException("Completed vehicles are read-only and cannot be modified.");
        }

        // Validate Yard exists and is ACTIVE if changed
        if (!vehicle.getYard().getId().equals(request.getYardId())) {
            Yard yard = yardRepository.findById(request.getYardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Yard not found with ID: " + request.getYardId()));

            if (yard.getStatus() != YardStatus.ACTIVE) {
                throw new BadRequestException("Cannot reassign vehicle to inactive yard '" + yard.getName() + "'.");
            }
            vehicle.setYard(yard);
        }

        vehicle.setMake(request.getMake().trim());
        vehicle.setModel(request.getModel().trim());
        vehicle.setManufactureYear(request.getManufactureYear());
        vehicle.setRoutingDecision(request.getRoutingDecision());
        vehicle.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        Vehicle updated = vehicleRepository.save(vehicle);
        return toDTO(updated);
    }

    @Transactional
    public VehicleDTO updateVehicleStatus(Long id, VehicleStatusRequest request) {
        verifyCanOperate();
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        VehicleStatus current = vehicle.getStatus();
        VehicleStatus target = request.getStatus();

        // Completed vehicles are read-only in this assessment; no reopening
        if (current == VehicleStatus.COMPLETED) {
            throw new BadRequestException("Completed vehicles are read-only. Status cannot be modified or reopened.");
        }

        // Only valid forward status changes are allowed: RECEIVED -> IN_PROGRESS -> COMPLETED
        boolean isValidForwardTransition =
                (current == VehicleStatus.RECEIVED && target == VehicleStatus.IN_PROGRESS) ||
                (current == VehicleStatus.IN_PROGRESS && target == VehicleStatus.COMPLETED);

        if (!isValidForwardTransition) {
            throw new BadRequestException("Invalid status transition: Cannot transition vehicle from " +
                    current + " to " + target + ". Allowed path is RECEIVED → IN_PROGRESS → COMPLETED.");
        }

        vehicle.setStatus(target);
        Vehicle updated = vehicleRepository.save(vehicle);
        return toDTO(updated);
    }

    private void verifyCanOperate() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            if (user.getRole() == Role.STAFF && !user.isCanMakeOperations()) {
                throw new BadRequestException("Your account is currently assigned Read-Only access. Please contact your Tenant Administrator to enable operational permissions.");
            }
        }
    }

    private synchronized String generateNextAssetCode() {
        Long maxId = vehicleRepository.findMaxId();
        long nextNum = (maxId == null ? 0 : maxId) + 1;
        String code = String.format("AST-%04d", nextNum);
        while (vehicleRepository.existsByAssetCode(code)) {
            nextNum++;
            code = String.format("AST-%04d", nextNum);
        }
        return code;
    }

    public VehicleDTO toDTO(Vehicle vehicle) {
        return VehicleDTO.builder()
                .id(vehicle.getId())
                .assetCode(vehicle.getAssetCode())
                .vin(vehicle.getVin())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .manufactureYear(vehicle.getManufactureYear())
                .yardId(vehicle.getYard().getId())
                .yardName(vehicle.getYard().getName())
                .yardCode(vehicle.getYard().getYardCode())
                .routingDecision(vehicle.getRoutingDecision())
                .status(vehicle.getStatus())
                .notes(vehicle.getNotes())
                .createdAt(vehicle.getCreatedAt())
                .isReadOnly(vehicle.getStatus() == VehicleStatus.COMPLETED)
                .build();
    }
}

