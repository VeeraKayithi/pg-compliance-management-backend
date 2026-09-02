package com.pgcompliance.controller;

import com.pgcompliance.dto.ApiResponse;
import com.pgcompliance.dto.RoomRequestDto;
import com.pgcompliance.dto.RoomResponseDto;
import com.pgcompliance.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

  private final RoomService roomService;

  /**
   * Create Room
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RoomResponseDto createRoom(
      @Valid @RequestBody RoomRequestDto request) {

    return roomService.createRoom(request);
  }

  /**
   * Get All Rooms
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<RoomResponseDto> getAllRooms() {

    return roomService.getAllRooms();
  }

  /**
   * Get Room By Id
   */
  @GetMapping("/{roomId}")
  @ResponseStatus(HttpStatus.OK)
  public RoomResponseDto getRoomById(@PathVariable Long roomId) {

    return roomService.getRoomById(roomId);
  }

  /**
   * Update Room
   */
  @PutMapping("/{roomId}")
  @ResponseStatus(HttpStatus.OK)
  public RoomResponseDto updateRoom(@PathVariable Long roomId, @Valid @RequestBody RoomRequestDto request) {

    return roomService.updateRoom(roomId, request);
  }

  /**
   * Delete Room
   */
  @DeleteMapping("/{roomId}")
  public ApiResponse deleteRoom(@PathVariable Long roomId) {
    

    roomService.deleteRoom(roomId);

    return new ApiResponse(
        "Room deleted successfully");
  }

  /**
   * Get Rooms By Building
   */
  @GetMapping("/building/{buildingId}")
  @ResponseStatus(HttpStatus.OK)
  public List<RoomResponseDto> getRoomsByBuilding(@PathVariable Long buildingId) {

    return roomService.getRoomsByBuilding(buildingId);
  }
}