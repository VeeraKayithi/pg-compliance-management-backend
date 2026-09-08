package com.pgcompliance.service;

import com.pgcompliance.dto.TenantAccountRequestDto;
import com.pgcompliance.dto.TenantAccountResponseDto;

public interface UserService {

  TenantAccountResponseDto createTenantAccount(
      TenantAccountRequestDto request);
}