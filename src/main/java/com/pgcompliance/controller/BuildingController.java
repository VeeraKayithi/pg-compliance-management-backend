package com.pgcompliance.controller;

import com.pgcompliance.dto.BuildingRequestDto;
import com.pgcompliance.dto.BuildingResponseDto;
import com.pgcompliance.service.BuildingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buildings")
@RequiredArgsConstructor
public class BuildingController {

  private final BuildingService buildingService;

  @PostMapping
  public BuildingResponseDto createBuilding(
      @Valid @RequestBody BuildingRequestDto request) {

    return buildingService.createBuilding(request);
  }

  @GetMapping
  public List<BuildingResponseDto> getAllBuildings() {

    return buildingService.getAllBuildings();
  }

  @GetMapping("/{id}")
  public BuildingResponseDto getBuildingById(
      @PathVariable Long id) {

    return buildingService.getBuildingById(id);
  }

  @PutMapping("/{id}")
  public BuildingResponseDto updateBuilding(
      @PathVariable Long id,
      @RequestBody BuildingRequestDto request) {

    return buildingService.updateBuilding(id, request);
  }

  @DeleteMapping("/{id}")
  public String deleteBuilding(
      @PathVariable Long id) {

    buildingService.deleteBuilding(id);

    return "Building deleted successfully";
  }
}