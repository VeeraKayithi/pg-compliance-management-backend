package com.pgcompliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TenantRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    private String mobileNumber;

    private String email;

    @NotNull
    private Long roomId;
}