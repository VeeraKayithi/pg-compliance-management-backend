package com.pgcompliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantAccountRequestDto {

  @NotNull(message = "Tenant ID is required")
  private Long tenantId;

  @NotBlank(message = "Username is required")
  @Size(min = 4, max = 100, message = "Username must contain between 4 and 100 characters")
  private String username;
}