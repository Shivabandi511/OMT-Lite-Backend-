package com.OMT.Backend.dto;

import com.OMT.Backend.model.YardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YardDTO {
    private Long id;
    private String yardCode;
    private String name;
    private String country;
    private String city;
    private String address;
    private YardStatus status;
    private LocalDateTime createdAt;
    private long assignedVehicleCount;
    private long activeVehicleCount;
}

