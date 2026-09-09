package com.pgcompliance.controller;

import com.pgcompliance.dto.*;
import com.pgcompliance.service.AccountActivationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/activation")
@RequiredArgsConstructor
public class AccountActivationController {

  private final AccountActivationService activationService;

  @GetMapping("/validate")
  public ResponseEntity<ActivationValidationResponseDto> validate(
      @RequestParam String token) {
    return ResponseEntity.ok(
        activationService.validateToken(token));
  }

  @PostMapping("/complete")
  public ResponseEntity<ActivationResponseDto> complete(
      @Valid @RequestBody CompleteActivationRequestDto request) {
    activationService.completeActivation(request);
    return ResponseEntity.ok(
        new ActivationResponseDto(
            "Account activated successfully. You can now sign in."));
  }

  @PostMapping("/resend")
  public ResponseEntity<ActivationResponseDto> resend(
      @Valid @RequestBody ResendActivationRequestDto request) {
    activationService.resendActivation(request.getUsername());
    return ResponseEntity.ok(
        new ActivationResponseDto(
            "If the account is eligible, a new activation email has been sent."));
  }
}
