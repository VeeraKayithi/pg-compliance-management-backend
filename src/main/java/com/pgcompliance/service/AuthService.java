package com.pgcompliance.service;

import com.pgcompliance.dto.LoginRequestDto;
import com.pgcompliance.dto.LoginResponseDto;
import com.pgcompliance.dto.RegisterRequestDto;

public interface AuthService {

  String register(
      RegisterRequestDto request);

  LoginResponseDto login(
      LoginRequestDto request);
}