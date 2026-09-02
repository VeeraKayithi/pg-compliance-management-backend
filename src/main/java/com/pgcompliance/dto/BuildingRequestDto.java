package com.pgcompliance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BuildingRequestDto {

    @NotBlank(message = "Building name is required")
    private String buildingName;

    @NotBlank(message = "Building type is required")
    private String buildingType;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;
}