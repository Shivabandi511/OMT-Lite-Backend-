package com.OMT.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartDTO {
    private Long id;
    private String partCode;
    private String name;
    private String category;
    private Boolean canBeExtracted;
    private String status;
    private Long vehicleId;
    private LocalDateTime createdAt;
}
