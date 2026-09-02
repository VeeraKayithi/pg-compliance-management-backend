package com.pgcompliance.dto;

import com.pgcompliance.constant.TenantStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantResponseDto {

    private Long tenantId;

    private String name;

    private String mobileNumber;

    private String email;

    private String roomNumber;

    private Long roomId;

    private TenantStatus tenantStatus;
}