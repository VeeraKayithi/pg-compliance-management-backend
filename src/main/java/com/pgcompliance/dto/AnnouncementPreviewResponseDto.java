package com.pgcompliance.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnnouncementPreviewResponseDto {
    private long activeTenantCount;
    private long inAppEligibleCount;
    private long emailEligibleCount;
    private long inAppSkippedCount;
    private long emailSkippedCount;
}
