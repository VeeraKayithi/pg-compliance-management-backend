package com.pgcompliance.dto;

import com.pgcompliance.constant.CommunicationChannel;
import com.pgcompliance.constant.CommunicationDeliveryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnnouncementDeliveryResponseDto {
    private Long deliveryId;
    private Long tenantId;
    private String tenantName;
    private String email;
    private String username;
    private CommunicationChannel channel;
    private CommunicationDeliveryStatus status;
    private String failureReason;
    private LocalDateTime sentAt;
    private LocalDateTime failedAt;
}
