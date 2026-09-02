package com.pgcompliance.repository;

import com.pgcompliance.constant.TenantStatus;
import com.pgcompliance.entity.Tenant;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository
    extends JpaRepository<Tenant, Long> {

  boolean existsByMobileNumber(String mobileNumber);

  long countByRoomRoomId(Long roomId);

  long countByRoomRoomIdAndTenantStatus(Long roomId, TenantStatus tenantStatus);

  Optional<Tenant> findByMobileNumber(
      String mobileNumber);

}