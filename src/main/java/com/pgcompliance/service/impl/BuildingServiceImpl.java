package com.pgcompliance.service.impl;

import com.pgcompliance.dto.BuildingRequestDto;
import com.pgcompliance.dto.BuildingResponseDto;
import com.pgcompliance.entity.Building;
import com.pgcompliance.exception.ResourceNotFoundException;
import com.pgcompliance.repository.BuildingRepository;
import com.pgcompliance.repository.RoomRepository;
import com.pgcompliance.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingServiceImpl implements BuildingService {

  private final BuildingRepository buildingRepository;
  private final RoomRepository roomRepository;

  @Override
  public BuildingResponseDto createBuilding(BuildingRequestDto request) {

    Building building = Building.builder()
        .buildingName(request.getBuildingName())
        .buildingType(request.getBuildingType())
        .address(request.getAddress())
        .city(request.getCity())
        .createdAt(LocalDateTime.now())
        .build();

    Building savedBuilding = buildingRepository.save(building);

    return mapToResponse(savedBuilding);
  }

  @Override
  public List<BuildingResponseDto> getAllBuildings() {

    return buildingRepository.findAll()
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  public BuildingResponseDto getBuildingById(Long id) {

    Building building = buildingRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Building not found with id: " + id));

    return mapToResponse(building);
  }

  @Override
  public BuildingResponseDto updateBuilding(Long id, BuildingRequestDto request) {

    Building building = buildingRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Building not found with id: " + id));

    building.setBuildingName(request.getBuildingName());
    building.setBuildingType(request.getBuildingType());
    building.setAddress(request.getAddress());
    building.setCity(request.getCity());

    Building updatedBuilding = buildingRepository.save(building);

    return mapToResponse(updatedBuilding);
  }

  @Override
  public void deleteBuilding(Long id) {

    Building building = buildingRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Building not found with id: " + id));

    if (roomRepository.existsByBuildingBuildingId(id)) {

      throw new RuntimeException(
          "Cannot delete building. Rooms are assigned to this building.");
    }

    buildingRepository.delete(building);
  }

  private BuildingResponseDto mapToResponse(Building building) {

    return BuildingResponseDto.builder()
        .buildingId(building.getBuildingId())
        .buildingName(building.getBuildingName())
        .buildingType(building.getBuildingType())
        .address(building.getAddress())
        .city(building.getCity())
        .build();
  }
}