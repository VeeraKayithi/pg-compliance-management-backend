package com.pgcompliance.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TenantAccountResponseDto {

  private Long userId;

  private Long tenantId;

  private String tenantName;

  private String username;

  private String role;

  private Boolean active;

  private String message;
}