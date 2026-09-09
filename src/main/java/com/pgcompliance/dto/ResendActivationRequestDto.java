package com.pgcompliance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendActivationRequestDto {

    @NotBlank(message = "Username is required")
    private String username;
}
