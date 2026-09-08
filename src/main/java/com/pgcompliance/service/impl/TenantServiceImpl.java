package com.pgcompliance.service.impl;

import com.pgcompliance.constant.NotificationEventType;
import com.pgcompliance.constant.NotificationSeverity;
import com.pgcompliance.constant.RoomStatus;
import com.pgcompliance.constant.SharingType;
import com.pgcompliance.constant.TenantStatus;
import com.pgcompliance.constant.UserRole;
import com.pgcompliance.dto.TenantProfileResponseDto;
import com.pgcompliance.dto.TenantRequestDto;
import com.pgcompliance.dto.TenantResponseDto;
import com.pgcompliance.entity.Room;
import com.pgcompliance.entity.Tenant;
import com.pgcompliance.entity.User;
import com.pgcompliance.exception.ResourceNotFoundException;
import com.pgcompliance.repository.RoomRepository;
import com.pgcompliance.repository.TenantRepository;
import com.pgcompliance.repository.UserRepository;
import com.pgcompliance.service.NotificationService;
import com.pgcompliance.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TenantResponseDto createTenant(
            TenantRequestDto request
    ) {
        Room requestedRoom = findRoom(request.getRoomId());
        validateRoomForAssignment(requestedRoom, null);

        Optional<Tenant> existingTenant =
                tenantRepository.findByMobileNumber(
                        request.getMobileNumber()
                );

        if (existingTenant.isPresent()) {
            Tenant tenant = existingTenant.get();

            if (tenant.getTenantStatus() == TenantStatus.ACTIVE) {
                throw new IllegalStateException(
                        "Tenant already exists with this mobile number"
                );
            }

            Room previousRoom = tenant.getRoom();

            tenant.setName(request.getName());
            tenant.setEmail(request.getEmail());
            tenant.setJoiningDate(LocalDate.now());
            tenant.setTenantStatus(TenantStatus.ACTIVE);
            tenant.setRoom(requestedRoom);

            Tenant updatedTenant = tenantRepository.save(tenant);

            if (previousRoom != null
                    && !previousRoom.getRoomId()
                    .equals(requestedRoom.getRoomId())) {
                synchronizeRoomStatus(previousRoom);
            }

            synchronizeRoomStatus(requestedRoom);
            createRoomAssignmentNotificationIfAccountExists(
                    updatedTenant,
                    requestedRoom
            );

            return mapToResponse(updatedTenant);
        }

        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .mobileNumber(request.getMobileNumber())
                .email(request.getEmail())
                .joiningDate(LocalDate.now())
                .tenantStatus(TenantStatus.ACTIVE)
                .room(requestedRoom)
                .build();

        Tenant savedTenant = tenantRepository.save(tenant);

        synchronizeRoomStatus(requestedRoom);

        return mapToResponse(savedTenant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponseDto> getAllTenants() {
        return tenantRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponseDto getTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tenant not found with id: " + id
                        )
                );

        return mapToResponse(tenant);
    }

    @Override
    @Transactional
    public TenantResponseDto updateTenant(
            Long id,
            TenantRequestDto request
    ) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tenant not found with id: " + id
                        )
                );

        Room oldRoom = tenant.getRoom();
        Room requestedRoom = findRoom(request.getRoomId());

        boolean roomChanged = oldRoom == null
                || !oldRoom.getRoomId()
                .equals(requestedRoom.getRoomId());

        Optional<Tenant> tenantWithMobile =
                tenantRepository.findByMobileNumber(
                        request.getMobileNumber()
                );

        if (tenantWithMobile.isPresent()
                && !tenantWithMobile.get().getTenantId().equals(id)) {
            throw new IllegalStateException(
                    "Another tenant already exists with this mobile number"
            );
        }

        if (roomChanged
                && tenant.getTenantStatus() == TenantStatus.ACTIVE) {
            validateRoomForAssignment(
                    requestedRoom,
                    tenant.getTenantId()
            );
        }

        tenant.setName(request.getName());
        tenant.setMobileNumber(request.getMobileNumber());
        tenant.setEmail(request.getEmail());
        tenant.setRoom(requestedRoom);

        Tenant updatedTenant = tenantRepository.save(tenant);

        if (roomChanged && oldRoom != null) {
            synchronizeRoomStatus(oldRoom);
        }

        synchronizeRoomStatus(requestedRoom);

        if (roomChanged) {
            createRoomTransferNotification(
                    updatedTenant,
                    oldRoom,
                    requestedRoom
            );
        }

        return mapToResponse(updatedTenant);
    }

    @Override
    @Transactional
    public void markTenantAsLeft(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tenant not found with id: " + tenantId
                        )
                );

        if (tenant.getTenantStatus() == TenantStatus.LEFT) {
            throw new IllegalStateException(
                    "Tenant is already marked as LEFT"
            );
        }

        Room assignedRoom = tenant.getRoom();

        tenant.setTenantStatus(TenantStatus.LEFT);
        tenantRepository.save(tenant);

        if (assignedRoom != null) {
            synchronizeRoomStatus(assignedRoom);
        }

        createTenantLeftNotification(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantProfileResponseDto getMyProfile(
            String username
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User account not found"
                        )
                );

        Tenant tenant = user.getTenant();

        if (tenant == null) {
            throw new ResourceNotFoundException(
                    "No tenant profile is linked to this user account"
            );
        }

        Room room = tenant.getRoom();

        if (room == null) {
            throw new ResourceNotFoundException(
                    "No room is assigned to this tenant"
            );
        }

        return TenantProfileResponseDto.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .mobileNumber(tenant.getMobileNumber())
                .email(tenant.getEmail())
                .joiningDate(tenant.getJoiningDate())
                .tenantStatus(tenant.getTenantStatus())
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .sharingType(room.getSharingType().name())
                .roomStatus(room.getRoomStatus().name())
                .buildingId(
                        room.getBuilding().getBuildingId()
                )
                .buildingName(
                        room.getBuilding().getBuildingName()
                )
                .username(user.getUsername())
                .build();
    }

    private void createRoomAssignmentNotificationIfAccountExists(
            Tenant tenant,
            Room room
    ) {
        userRepository.findByTenantTenantId(
                tenant.getTenantId()
        ).ifPresent(tenantUser ->
                notificationService.createNotification(
                        tenantUser.getUserId(),
                        NotificationEventType.TENANT_ROOM_ASSIGNED,
                        NotificationSeverity.INFO,
                        "TENANT",
                        "Room assigned",
                        "You have been assigned to Room "
                                + room.getRoomNumber()
                                + " in "
                                + room.getBuilding().getBuildingName()
                                + ".",
                        "/tenant/dashboard",
                        "TENANT",
                        tenant.getTenantId(),
                        "TENANT_ROOM_ASSIGNED:"
                                + tenant.getTenantId()
                                + ":"
                                + room.getRoomId()
                                + ":"
                                + tenant.getJoiningDate()
                )
        );
    }

    private void createRoomTransferNotification(
            Tenant tenant,
            Room oldRoom,
            Room newRoom
    ) {
        userRepository.findByTenantTenantId(
                tenant.getTenantId()
        ).ifPresent(tenantUser -> {
            String oldDescription = oldRoom == null
                    ? "an unassigned room"
                    : roomDescription(oldRoom);

            notificationService.createNotification(
                    tenantUser.getUserId(),
                    NotificationEventType.TENANT_ROOM_TRANSFERRED,
                    NotificationSeverity.INFO,
                    "TENANT",
                    "Room assignment updated",
                    "Your accommodation has been transferred from "
                            + oldDescription
                            + " to "
                            + roomDescription(newRoom)
                            + ".",
                    "/tenant/dashboard",
                    "TENANT",
                    tenant.getTenantId(),
                    "TENANT_ROOM_TRANSFERRED:"
                            + tenant.getTenantId()
                            + ":"
                            + (oldRoom == null
                            ? "NONE"
                            : oldRoom.getRoomId())
                            + ":"
                            + newRoom.getRoomId()
                            + ":"
                            + System.currentTimeMillis()
            );
        });
    }

    private void createTenantLeftNotification(Tenant tenant) {
        userRepository.findByTenantTenantId(
                tenant.getTenantId()
        ).ifPresent(tenantUser ->
                notificationService.createNotification(
                        tenantUser.getUserId(),
                        NotificationEventType.TENANT_MARKED_LEFT,
                        NotificationSeverity.INFO,
                        "TENANT",
                        "Resident status updated",
                        "Your Nandu PG resident status has been updated to LEFT.",
                        "/tenant/dashboard",
                        "TENANT",
                        tenant.getTenantId(),
                        "TENANT_MARKED_LEFT:"
                                + tenant.getTenantId()
                                + ":"
                                + tenant.getJoiningDate()
                )
        );
    }

    private void synchronizeRoomStatus(Room room) {
        RoomStatus previousStatus = room.getRoomStatus();

        long activeOccupancy = tenantRepository
                .countByRoomRoomIdAndTenantStatus(
                        room.getRoomId(),
                        TenantStatus.ACTIVE
                );

        int capacity = getCapacity(room.getSharingType());

        RoomStatus calculatedStatus = activeOccupancy >= capacity
                ? RoomStatus.OCCUPIED
                : RoomStatus.AVAILABLE;

        room.setRoomStatus(calculatedStatus);
        roomRepository.save(room);

        if (previousStatus != calculatedStatus) {
            if (calculatedStatus == RoomStatus.OCCUPIED) {
                notifyActiveAdmins(
                        NotificationEventType.ROOM_FULL,
                        NotificationSeverity.WARNING,
                        "Room reached full capacity",
                        roomDescription(room)
                                + " is now fully occupied.",
                        room,
                        "ROOM_FULL:"
                                + room.getRoomId()
                                + ":"
                                + System.currentTimeMillis()
                );
            } else if (previousStatus == RoomStatus.OCCUPIED
                    && calculatedStatus == RoomStatus.AVAILABLE) {
                notifyActiveAdmins(
                        NotificationEventType.ROOM_AVAILABLE,
                        NotificationSeverity.INFO,
                        "Room availability updated",
                        roomDescription(room)
                                + " now has an available bed.",
                        room,
                        "ROOM_AVAILABLE:"
                                + room.getRoomId()
                                + ":"
                                + System.currentTimeMillis()
                );
            }
        }
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

    private Room findRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found with id: " + roomId
                        )
                );
    }

    private void validateRoomForAssignment(
            Room room,
            Long currentTenantId
    ) {
        if (room.getRoomStatus() == RoomStatus.BLOCKED
                || room.getRoomStatus()
                == RoomStatus.UNDER_REPAIR) {
            throw new IllegalStateException(
                    "Room is not available for tenant assignment"
            );
        }

        long currentOccupancy = tenantRepository
                .countByRoomRoomIdAndTenantStatus(
                        room.getRoomId(),
                        TenantStatus.ACTIVE
                );

        if (currentTenantId != null) {
            boolean alreadyInRoom = tenantRepository
                    .findById(currentTenantId)
                    .map(existingTenant ->
                            existingTenant.getRoom() != null
                                    && existingTenant.getRoom()
                                    .getRoomId()
                                    .equals(room.getRoomId())
                                    && existingTenant
                                    .getTenantStatus()
                                    == TenantStatus.ACTIVE
                    )
                    .orElse(false);

            if (alreadyInRoom) {
                currentOccupancy--;
            }
        }

        if (currentOccupancy
                >= getCapacity(room.getSharingType())) {
            throw new IllegalStateException(
                    "Room capacity exceeded"
            );
        }
    }

    private TenantResponseDto mapToResponse(Tenant tenant) {
        return TenantResponseDto.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .mobileNumber(tenant.getMobileNumber())
                .email(tenant.getEmail())
                .roomId(tenant.getRoom().getRoomId())
                .roomNumber(tenant.getRoom().getRoomNumber())
                .tenantStatus(tenant.getTenantStatus())
                .build();
    }

    private int getCapacity(SharingType sharingType) {
        return switch (sharingType) {
            case SINGLE -> 1;
            case DOUBLE -> 2;
            case TRIPLE -> 3;
            case FOUR -> 4;
        };
    }
}
