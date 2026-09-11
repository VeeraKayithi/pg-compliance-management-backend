package com.pgcompliance.service;

import com.pgcompliance.dto.*;

import java.util.List;

public interface AnnouncementService {
    AnnouncementPreviewResponseDto preview(AnnouncementPreviewRequestDto request);
    AnnouncementResponseDto send(AnnouncementCreateRequestDto request, String adminUsername);
    List<AnnouncementResponseDto> getHistory();
    List<AnnouncementDeliveryResponseDto> getDeliveries(Long announcementId);
}
