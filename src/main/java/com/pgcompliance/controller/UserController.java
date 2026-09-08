package com.pgcompliance.controller;

import com.pgcompliance.dto.TenantAccountRequestDto;
import com.pgcompliance.dto.TenantAccountResponseDto;
import com.pgcompliance.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/tenant-account")
  public ResponseEntity<TenantAccountResponseDto> createTenantAccount(
      @Valid @RequestBody TenantAccountRequestDto request) {

    TenantAccountResponseDto response = userService.createTenantAccount(
        request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }
}