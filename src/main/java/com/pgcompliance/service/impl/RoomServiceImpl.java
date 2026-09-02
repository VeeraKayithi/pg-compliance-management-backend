package com.pgcompliance.service.impl;

import com.pgcompliance.dto.RoomRequestDto;
import com.pgcompliance.dto.RoomResponseDto;
import com.pgcompliance.entity.Building;
import com.pgcompliance.entity.Room;
import com.pgcompliance.exception.ResourceNotFoundException;
import com.pgcompliance.repository.BuildingRepository;
import com.pgcompliance.repository.RoomRepository;
import com.pgcompliance.repository.TenantRepository;
import com.pgcompliance.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final TenantRepository tenantRepository;

    @Override
    public RoomResponseDto createRoom(RoomRequestDto request) {

        Building building = buildingRepository.findById(
                request.getBuildingId()).orElseThrow(() -> new ResourceNotFoundException("Building not found"));

        if (roomRepository.existsByBuildingBuildingIdAndRoomNumber(request.getBuildingId(), request.getRoomNumber())) {

            throw new RuntimeException("Room already exists in building");
        }

        Room room = Room.builder().roomNumber(request.getRoomNumber()).sharingType(request.getSharingType())
                .roomStatus(request.getRoomStatus())
                .building(building)
                .createdAt(LocalDateTime.now())
                .build();

        Room savedRoom = roomRepository.save(room);

        return mapToResponse(savedRoom);
    }

    @Override
    public List<RoomResponseDto> getAllRooms() {

        return roomRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public RoomResponseDto getRoomById(
            Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found"));

        return mapToResponse(room);
    }

    @Override
    public RoomResponseDto updateRoom(
            Long roomId,
            RoomRequestDto request) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found"));

        room.setRoomNumber(
                request.getRoomNumber());

        room.setSharingType(
                request.getSharingType());

        room.setRoomStatus(
                request.getRoomStatus());

        Room updatedRoom = roomRepository.save(room);

        return mapToResponse(updatedRoom);
    }

    @Override
    public void deleteRoom(Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with id: " + roomId));

        // Validation
        if (tenantRepository.countByRoomRoomId(roomId) > 0) {

            throw new RuntimeException(
                    "Cannot delete room. Tenants are assigned to this room.");
        }

        roomRepository.delete(room);
    }

    @Override
    public List<RoomResponseDto> getRoomsByBuilding(
            Long buildingId) {

        return roomRepository
                .findByBuildingBuildingId(buildingId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private RoomResponseDto mapToResponse(
            Room room) {

        return RoomResponseDto.builder()
                .roomId(room.getRoomId())
                .buildingId(
                        room.getBuilding().getBuildingId())
                .buildingName(
                        room.getBuilding().getBuildingName())
                .roomNumber(room.getRoomNumber())
                .sharingType(room.getSharingType())
                .capacity(
                        getCapacity(room.getSharingType()))
                .roomStatus(room.getRoomStatus())
                .build();
    }

    private Integer getCapacity(
            com.pgcompliance.constant.SharingType type) {

        return switch (type) {
            case SINGLE -> 1;
            case DOUBLE -> 2;
            case TRIPLE -> 3;
            case FOUR -> 4;
        };
    }
}