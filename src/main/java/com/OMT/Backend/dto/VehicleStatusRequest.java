package com.OMT.Backend.dto;

import com.OMT.Backend.model.VehicleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleStatusRequest {
    @NotNull(message = "Status is required (RECEIVED, IN_PROGRESS, or COMPLETED)")
    private VehicleStatus status;
}

