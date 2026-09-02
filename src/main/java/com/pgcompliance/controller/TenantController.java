package com.pgcompliance.controller;

import com.pgcompliance.dto.TenantRequestDto;
import com.pgcompliance.dto.TenantResponseDto;
import com.pgcompliance.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

  private final TenantService tenantService;

  /**
   * Create Tenant
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TenantResponseDto createTenant(
      @Valid @RequestBody TenantRequestDto request) {

    return tenantService.createTenant(request);
  }

  /**
   * Get All Tenants
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<TenantResponseDto> getAllTenants() {

    return tenantService.getAllTenants();
  }

  /**
   * Get Tenant By Id
   */
  @GetMapping("/{tenantId}")
  @ResponseStatus(HttpStatus.OK)
  public TenantResponseDto getTenantById(
      @PathVariable Long tenantId) {

    return tenantService.getTenantById(tenantId);
  }

  /**
   * Update Tenant
   */
  @PutMapping("/{tenantId}")
  @ResponseStatus(HttpStatus.OK)
  public TenantResponseDto updateTenant(
      @PathVariable Long tenantId,
      @Valid @RequestBody TenantRequestDto request) {

    return tenantService.updateTenant(
        tenantId,
        request);
  }

  /**
   * Soft Delete Tenant
   * Marks tenant as LEFT
   */
  @PutMapping("/{tenantId}/leave")
  @ResponseStatus(HttpStatus.OK)
  public String markTenantAsLeft(
      @PathVariable Long tenantId) {

    tenantService.markTenantAsLeft(tenantId);

    return "Tenant marked as LEFT successfully";
  }
}