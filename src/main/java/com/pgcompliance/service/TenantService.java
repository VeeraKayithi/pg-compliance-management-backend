package com.pgcompliance.service;

import com.pgcompliance.dto.TenantRequestDto;
import com.pgcompliance.dto.TenantResponseDto;

import java.util.List;


public interface TenantService {

  TenantResponseDto createTenant(
      TenantRequestDto request);

  List<TenantResponseDto> getAllTenants();

  TenantResponseDto getTenantById(Long id);

  TenantResponseDto updateTenant(
      Long id,
      TenantRequestDto request);

  void markTenantAsLeft(Long tenantId);
}