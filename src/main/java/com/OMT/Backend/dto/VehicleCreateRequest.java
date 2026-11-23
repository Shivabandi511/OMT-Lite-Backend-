package com.OMT.Backend.dto;

import com.OMT.Backend.model.RoutingDecision;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCreateRequest {

    @NotBlank(message = "VIN is required")
    @Pattern(regexp = "^[A-HJ-NPR-Za-hj-npr-z0-9]{17}$", message = "VIN must be exactly 17 alphanumeric characters (excluding letters I, O, Q)")
    private String vin;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Manufacture year is required")
    @Min(value = 1900, message = "Manufacture year must be 1900 or later")
    @Max(value = 2030, message = "Manufacture year must be a valid future/current year up to 2030")
    private Integer manufactureYear;

    @NotNull(message = "Yard ID is required")
    private Long yardId;

    @NotNull(message = "Routing decision is required (DISMANTLE or KEEP_WHOLE)")
    private RoutingDecision routingDecision;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}

