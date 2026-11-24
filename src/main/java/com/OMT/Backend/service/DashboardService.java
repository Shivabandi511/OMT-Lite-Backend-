package com.OMT.Backend.service;

import com.OMT.Backend.dto.DashboardSummaryDTO;
import com.OMT.Backend.model.RoutingDecision;
import com.OMT.Backend.model.VehicleStatus;
import com.OMT.Backend.model.YardStatus;
import com.OMT.Backend.repository.PartRepository;
import com.OMT.Backend.repository.VehicleRepository;
import com.OMT.Backend.repository.YardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final YardRepository yardRepository;
    private final VehicleRepository vehicleRepository;
    private final PartRepository partRepository;

    public DashboardService(YardRepository yardRepository,
                            VehicleRepository vehicleRepository,
                            PartRepository partRepository) {
        this.yardRepository = yardRepository;
        this.vehicleRepository = vehicleRepository;
        this.partRepository = partRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary() {
        long totalYards = yardRepository.count();
        long activeYards = yardRepository.countByStatus(YardStatus.ACTIVE);
        long inactiveYards = totalYards - activeYards;

        long totalVehicles = vehicleRepository.count();
        long received = vehicleRepository.countByStatus(VehicleStatus.RECEIVED);
        long inProgress = vehicleRepository.countByStatus(VehicleStatus.IN_PROGRESS);
        long completed = vehicleRepository.countByStatus(VehicleStatus.COMPLETED);
        long dismantle = vehicleRepository.countByRoutingDecision(RoutingDecision.DISMANTLE);
        long keepWhole = vehicleRepository.countByRoutingDecision(RoutingDecision.KEEP_WHOLE);

        long totalParts = partRepository.count();
        long extractedParts = partRepository.countByStatus("EXTRACTED");
        long pendingParts = partRepository.countByStatus("PENDING");
        long extractableParts = partRepository.countByCanBeExtracted(true);

        return DashboardSummaryDTO.builder()
                .totalYards(totalYards)
                .activeYards(activeYards)
                .inactiveYards(inactiveYards)
                .totalVehicles(totalVehicles)
                .vehiclesReceived(received)
                .vehiclesInProgress(inProgress)
                .vehiclesCompleted(completed)
                .vehiclesDismantle(dismantle)
                .vehiclesKeepWhole(keepWhole)
                .totalParts(totalParts)
                .extractedParts(extractedParts)
                .pendingParts(pendingParts)
                .extractableParts(extractableParts)
                .build();
    }
}

