package com.pgcompliance.service.impl;

import com.pgcompliance.constant.NotificationEventType;
import com.pgcompliance.constant.NotificationSeverity;
import com.pgcompliance.constant.RoomStatus;
import com.pgcompliance.constant.SharingType;
import com.pgcompliance.constant.UserRole;
import com.pgcompliance.dto.RoomRequestDto;
import com.pgcompliance.dto.RoomResponseDto;
import com.pgcompliance.entity.Building;
import com.pgcompliance.entity.Room;
import com.pgcompliance.exception.ResourceNotFoundException;
import com.pgcompliance.repository.BuildingRepository;
import com.pgcompliance.repository.RoomRepository;
import com.pgcompliance.repository.TenantRepository;
import com.pgcompliance.repository.UserRepository;
import com.pgcompliance.service.NotificationService;
import com.pgcompliance.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RoomResponseDto createRoom(RoomRequestDto request) {
        Building building = buildingRepository
                .findById(request.getBuildingId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Building not found"
                        )
                );

        if (roomRepository
                .existsByBuildingBuildingIdAndRoomNumber(
                        request.getBuildingId(),
                        request.getRoomNumber()
                )) {
            throw new IllegalStateException(
                    "Room already exists in building"
            );
        }

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .sharingType(request.getSharingType())
                .roomStatus(request.getRoomStatus())
                .building(building)
                .createdAt(LocalDateTime.now())
                .build();

        Room savedRoom = roomRepository.save(room);

        if (savedRoom.getRoomStatus()
                == RoomStatus.UNDER_REPAIR) {
            notifyActiveAdmins(
                    NotificationEventType.ROOM_UNDER_REPAIR,
                    NotificationSeverity.WARNING,
                    "Room placed under repair",
                    roomDescription(savedRoom)
                            + " has been placed under repair.",
                    savedRoom,
                    "ROOM_UNDER_REPAIR:"
                            + savedRoom.getRoomId()
                            + ":"
                            + System.currentTimeMillis()
            );
        }

        return mapToResponse(savedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDto> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponseDto getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found"
                        )
                );

        return mapToResponse(room);
    }

    @Override
    @Transactional
    public RoomResponseDto updateRoom(
            Long roomId,
            RoomRequestDto request
    ) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found"
                        )
                );

        if (!room.getBuilding().getBuildingId()
                .equals(request.getBuildingId())) {
            Building requestedBuilding = buildingRepository
                    .findById(request.getBuildingId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Building not found"
                            )
                    );
            room.setBuilding(requestedBuilding);
        }

        boolean duplicateRoomNumber = roomRepository
                .existsByBuildingBuildingIdAndRoomNumber(
                        request.getBuildingId(),
                        request.getRoomNumber()
                )
                && !room.getRoomNumber()
                .equals(request.getRoomNumber());

        if (duplicateRoomNumber) {
            throw new IllegalStateException(
                    "Room already exists in building"
            );
        }

        RoomStatus previousStatus = room.getRoomStatus();

        room.setRoomNumber(request.getRoomNumber());
        room.setSharingType(request.getSharingType());
        room.setRoomStatus(request.getRoomStatus());

        Room updatedRoom = roomRepository.save(room);

        if (previousStatus != RoomStatus.UNDER_REPAIR
                && updatedRoom.getRoomStatus()
                == RoomStatus.UNDER_REPAIR) {
            notifyActiveAdmins(
                    NotificationEventType.ROOM_UNDER_REPAIR,
                    NotificationSeverity.WARNING,
                    "Room placed under repair",
                    roomDescription(updatedRoom)
                            + " has been placed under repair.",
                    updatedRoom,
                    "ROOM_UNDER_REPAIR:"
                            + updatedRoom.getRoomId()
                            + ":"
                            + System.currentTimeMillis()
            );
        }

        return mapToResponse(updatedRoom);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found with id: " + roomId
                        )
                );

        if (tenantRepository.countByRoomRoomId(roomId) > 0) {
            throw new IllegalStateException(
                    "Cannot delete room. Tenants are assigned to this room."
            );
        }

        roomRepository.delete(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDto> getRoomsByBuilding(
            Long buildingId
    ) {
        return roomRepository
                .findByBuildingBuildingId(buildingId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void notifyActiveAdmins(
            NotificationEventType eventType,
            NotificationSeverity severity,
            String title,
            String message,
            Room room,
            String deduplicationKey
    ) {
        userRepository.findByRoleAndActiveTrue(UserRole.ADMIN)
                .forEach(admin ->
                        notificationService.createNotification(
                                admin.getUserId(),
                                eventType,
                                severity,
                                "ROOM",
                                title,
                                message,
                                "/admin/rooms",
                                "ROOM",
                                room.getRoomId(),
                                deduplicationKey
                        )
                );
    }

    private String roomDescription(Room room) {
        return "Room " + room.getRoomNumber()
                + " in "
                + room.getBuilding().getBuildingName();
    }

    private RoomResponseDto mapToResponse(Room room) {
        return RoomResponseDto.builder()
                .roomId(room.getRoomId())
                .buildingId(
                        room.getBuilding().getBuildingId()
                )
                .buildingName(
                        room.getBuilding().getBuildingName()
                )
                .roomNumber(room.getRoomNumber())
                .sharingType(room.getSharingType())
                .capacity(getCapacity(room.getSharingType()))
                .roomStatus(room.getRoomStatus())
                .build();
    }

    private Integer getCapacity(SharingType type) {
        return switch (type) {
            case SINGLE -> 1;
            case DOUBLE -> 2;
            case TRIPLE -> 3;
            case FOUR -> 4;
        };
    }
}
