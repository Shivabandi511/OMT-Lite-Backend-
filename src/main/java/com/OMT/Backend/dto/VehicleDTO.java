package com.OMT.Backend.dto;

import com.OMT.Backend.model.RoutingDecision;
import com.OMT.Backend.model.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDTO {
    private Long id;
    private String assetCode;
    private String vin;
    private String make;
    private String model;
    private Integer manufactureYear;
    private Long yardId;
    private String yardName;
    private String yardCode;
    private RoutingDecision routingDecision;
    private VehicleStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private boolean isReadOnly;
}

