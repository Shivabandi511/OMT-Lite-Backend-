package com.OMT.Backend.dto;

import com.OMT.Backend.model.YardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YardStatusRequest {
    @NotNull(message = "Status is required (ACTIVE or INACTIVE)")
    private YardStatus status;
}

