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
  private String username;

  @NotBlank(message = "Temporary password is required")
  @Size(min = 8, message = "Temporary password must contain at least 8 characters")
  private String temporaryPassword;
}