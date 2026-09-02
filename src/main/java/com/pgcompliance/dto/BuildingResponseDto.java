package com.pgcompliance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuildingResponseDto {

    private Long buildingId;

    private String buildingName;

    private String buildingType;

    private String address;

    private String city;
}