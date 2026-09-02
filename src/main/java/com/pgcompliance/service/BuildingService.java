package com.pgcompliance.service;

import com.pgcompliance.dto.BuildingRequestDto;
import com.pgcompliance.dto.BuildingResponseDto;

import java.util.List;

public interface BuildingService {

        BuildingResponseDto createBuilding(BuildingRequestDto request);

        List<BuildingResponseDto> getAllBuildings();

        BuildingResponseDto getBuildingById(Long id);

        BuildingResponseDto updateBuilding(
                        Long id,
                        BuildingRequestDto request);

        void deleteBuilding(Long id);
}