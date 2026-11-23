package com.OMT.Backend.service;

import com.OMT.Backend.dto.YardCreateRequest;
import com.OMT.Backend.dto.YardDTO;
import com.OMT.Backend.dto.YardStatusRequest;
import com.OMT.Backend.dto.YardUpdateRequest;
import com.OMT.Backend.exception.BadRequestException;
import com.OMT.Backend.exception.ResourceNotFoundException;
import com.OMT.Backend.model.Yard;
import com.OMT.Backend.model.YardStatus;
import com.OMT.Backend.repository.VehicleRepository;
import com.OMT.Backend.repository.YardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class YardService {

    private final YardRepository yardRepository;
    private final VehicleRepository vehicleRepository;

    public YardService(YardRepository yardRepository, VehicleRepository vehicleRepository) {
        this.yardRepository = yardRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<YardDTO> getAllYards(String search, YardStatus status) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return yardRepository.searchYards(cleanSearch, status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public YardDTO getYardById(Long id) {
        Yard yard = yardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yard not found with ID: " + id));
        return toDTO(yard);
    }

    @Transactional
    public YardDTO createYard(YardCreateRequest request) {
        String generatedCode = generateNextYardCode(request.getCountry());

        Yard yard = Yard.builder()
                .yardCode(generatedCode)
                .name(request.getName().trim())
                .country(request.getCountry().trim())
                .city(request.getCity().trim())
                .address(request.getAddress().trim())
                .status(YardStatus.ACTIVE)
                .build();

        Yard savedYard = yardRepository.save(yard);
        return toDTO(savedYard);
    }

    @Transactional
    public YardDTO updateYard(Long id, YardUpdateRequest request) {
        Yard yard = yardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yard not found with ID: " + id));

        yard.setName(request.getName().trim());
        yard.setCountry(request.getCountry().trim());
        yard.setCity(request.getCity().trim());
        yard.setAddress(request.getAddress().trim());

        Yard updatedYard = yardRepository.save(yard);
        return toDTO(updatedYard);
    }

    @Transactional
    public YardDTO updateYardStatus(Long id, YardStatusRequest request) {
        Yard yard = yardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yard not found with ID: " + id));

        if (request.getStatus() == YardStatus.INACTIVE) {
            long activeVehicles = vehicleRepository.countActiveVehiclesByYardId(id);
            if (activeVehicles > 0) {
                throw new BadRequestException("Cannot deactivate yard '" + yard.getName() + "' (" + yard.getYardCode() +
                        "): it has " + activeVehicles + " active vehicle(s) currently assigned. Please complete or reassign them first.");
            }
        }

        yard.setStatus(request.getStatus());
        Yard updatedYard = yardRepository.save(yard);
        return toDTO(updatedYard);
    }

    private synchronized String generateNextYardCode(String country) {
        String prefix = "YRD";
        if (country != null && !country.trim().isEmpty()) {
            String c = country.trim().toUpperCase();
            if (c.contains("UNITED STATES") || c.equals("USA") || c.equals("US")) {
                prefix = "US";
            } else if (c.contains("NIGERIA") || c.equals("NG")) {
                prefix = "NG";
            } else if (c.contains("CANADA") || c.equals("CA")) {
                prefix = "CA";
            } else if (c.contains("UNITED KINGDOM") || c.equals("UK") || c.equals("GB")) {
                prefix = "UK";
            } else if (c.length() >= 2) {
                prefix = c.substring(0, 2);
            }
        }
        Long maxId = yardRepository.findMaxId();
        long nextNum = (maxId == null ? 0 : maxId) + 1;
        String code = String.format("%s-%04d", prefix, nextNum);
        while (yardRepository.existsByYardCode(code)) {
            nextNum++;
            code = String.format("%s-%04d", prefix, nextNum);
        }
        return code;
    }

    public YardDTO toDTO(Yard yard) {
        long assignedCount = vehicleRepository.countByYardId(yard.getId());
        long activeCount = vehicleRepository.countActiveVehiclesByYardId(yard.getId());

        return YardDTO.builder()
                .id(yard.getId())
                .yardCode(yard.getYardCode())
                .name(yard.getName())
                .country(yard.getCountry())
                .city(yard.getCity())
                .address(yard.getAddress())
                .status(yard.getStatus())
                .createdAt(yard.getCreatedAt())
                .assignedVehicleCount(assignedCount)
                .activeVehicleCount(activeCount)
                .build();
    }
}

