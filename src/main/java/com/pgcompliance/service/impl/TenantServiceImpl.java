package com.pgcompliance.service.impl;

import com.pgcompliance.constant.RoomStatus;
import com.pgcompliance.constant.SharingType;
import com.pgcompliance.constant.TenantStatus;
import com.pgcompliance.dto.TenantRequestDto;
import com.pgcompliance.dto.TenantResponseDto;
import com.pgcompliance.entity.Room;
import com.pgcompliance.entity.Tenant;
import com.pgcompliance.exception.ResourceNotFoundException;
import com.pgcompliance.repository.RoomRepository;
import com.pgcompliance.repository.TenantRepository;
import com.pgcompliance.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

  private final TenantRepository tenantRepository;
  private final RoomRepository roomRepository;

  @Override
  public TenantResponseDto createTenant(
      TenantRequestDto request) {

    Room room = roomRepository.findById(
        request.getRoomId())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Room not found with id : "
                + request.getRoomId()));

    // Room availability validation

    if (room.getRoomStatus() == RoomStatus.BLOCKED
        || room.getRoomStatus() == RoomStatus.UNDER_REPAIR) {

      throw new RuntimeException(
          "Room is not available for tenant assignment");
    }

    // Capacity Validation

    long currentOccupancy = tenantRepository
        .countByRoomRoomIdAndTenantStatus(
            room.getRoomId(),
            TenantStatus.ACTIVE);

    int maxCapacity = getCapacity(room.getSharingType());

    if (currentOccupancy >= maxCapacity) {

      room.setRoomStatus(RoomStatus.OCCUPIED);

      roomRepository.save(room);

      throw new RuntimeException(
          "Room capacity exceeded");
    }

    /*
     * Check Existing Tenant By Mobile Number
     */

    Optional<Tenant> existingTenant = tenantRepository.findByMobileNumber(
        request.getMobileNumber());

    if (existingTenant.isPresent()) {

      Tenant tenant = existingTenant.get();

      // Tenant already active

      if (tenant.getTenantStatus() == TenantStatus.ACTIVE) {

        throw new RuntimeException(
            "Tenant already exists with this mobile number");
      }

      // Reactivate old tenant

      tenant.setName(request.getName());
      tenant.setEmail(request.getEmail());
      tenant.setJoiningDate(LocalDate.now());
      tenant.setTenantStatus(TenantStatus.ACTIVE);
      tenant.setRoom(room);

      Tenant updatedTenant = tenantRepository.save(tenant);

      updateRoomStatus(room);

      return mapToResponse(updatedTenant);
    }

    /*
     * Create Brand New Tenant
     */

    Tenant tenant = Tenant.builder()
        .name(request.getName())
        .mobileNumber(request.getMobileNumber())
        .email(request.getEmail())
        .joiningDate(LocalDate.now())
        .tenantStatus(TenantStatus.ACTIVE)
        .room(room)
        .build();

    Tenant savedTenant = tenantRepository.save(tenant);

    updateRoomStatus(room);

    return mapToResponse(savedTenant);
  }

  @Override
  public List<TenantResponseDto> getAllTenants() {

    return tenantRepository.findAll()
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public TenantResponseDto getTenantById(Long id) {

    Tenant tenant = tenantRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Tenant not found"));

    return mapToResponse(tenant);
  }

  @Override
  public TenantResponseDto updateTenant(
      Long id,
      TenantRequestDto request) {

    Tenant tenant = tenantRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Tenant not found"));

    tenant.setName(request.getName());
    tenant.setMobileNumber(
        request.getMobileNumber());
    tenant.setEmail(request.getEmail());

    Tenant updatedTenant = tenantRepository.save(tenant);

    return mapToResponse(updatedTenant);
  }

  @Override
  public void markTenantAsLeft(Long tenantId) {

    Tenant tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Tenant not found"));

    tenant.setTenantStatus(TenantStatus.LEFT);

    tenantRepository.save(tenant);

    updateRoomStatus(tenant.getRoom());
  }

  private TenantResponseDto mapToResponse(
      Tenant tenant) {

    return TenantResponseDto.builder()
        .tenantId(tenant.getTenantId())
        .name(tenant.getName())
        .mobileNumber(
            tenant.getMobileNumber())
        .email(tenant.getEmail())
        .roomId(
            tenant.getRoom().getRoomId())
        .roomNumber(
            tenant.getRoom().getRoomNumber())
        .tenantStatus(
            tenant.getTenantStatus())
        .build();
  }

  private void updateRoomStatus(Room room) {

    long activeOccupancy = tenantRepository
        .countByRoomRoomIdAndTenantStatus(
            room.getRoomId(),
            TenantStatus.ACTIVE);

    int capacity = getCapacity(room.getSharingType());

    if (activeOccupancy >= capacity) {

      room.setRoomStatus(
          RoomStatus.OCCUPIED);

    } else {

      room.setRoomStatus(
          RoomStatus.AVAILABLE);
    }

    roomRepository.save(room);
  }

  private int getCapacity(
      SharingType sharingType) {

    switch (sharingType) {

      case SINGLE:
        return 1;

      case DOUBLE:
        return 2;

      case TRIPLE:
        return 3;

      case FOUR:
        return 4;

      default:
        return 0;
    }
  }
}