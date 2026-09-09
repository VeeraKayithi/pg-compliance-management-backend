package com.pgcompliance.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivationValidationResponseDto {

    private boolean valid;
    private boolean expired;
    private boolean used;
    private String username;
    private String tenantName;
    private String message;
}
