package com.OMT.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {
    private long totalYards;
    private long activeYards;
    private long inactiveYards;
    private long totalVehicles;
    private long vehiclesReceived;
    private long vehiclesInProgress;
    private long vehiclesCompleted;
    private long vehiclesDismantle;
    private long vehiclesKeepWhole;
    private long totalParts;
    private long extractedParts;
    private long pendingParts;
    private long extractableParts;
}

