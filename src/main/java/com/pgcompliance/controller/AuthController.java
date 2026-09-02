package com.pgcompliance.controller;

import com.pgcompliance.dto.LoginRequestDto;
import com.pgcompliance.dto.LoginResponseDto;
import com.pgcompliance.dto.RegisterRequestDto;
import com.pgcompliance.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public String register(
      @RequestBody RegisterRequestDto request) {

    return authService.register(request);
  }

  @PostMapping("/login")
  public LoginResponseDto login(
      @RequestBody LoginRequestDto request) {

    return authService.login(request);
  }
}