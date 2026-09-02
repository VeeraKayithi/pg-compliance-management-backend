package com.pgcompliance.service;

import com.pgcompliance.dto.RoomRequestDto;
import com.pgcompliance.dto.RoomResponseDto;

import java.util.List;

public interface RoomService {

  RoomResponseDto createRoom(RoomRequestDto request);

  List<RoomResponseDto> getAllRooms();

  RoomResponseDto getRoomById(Long roomId);

  RoomResponseDto updateRoom(Long roomId, RoomRequestDto request);

  void deleteRoom(Long roomId);

  List<RoomResponseDto> getRoomsByBuilding(Long buildingId);
}