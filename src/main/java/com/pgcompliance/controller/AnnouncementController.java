package com.pgcompliance.controller;

import com.pgcompliance.dto.*;
import com.pgcompliance.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/announcements")
@RequiredArgsConstructor
public class AnnouncementController {
    private final AnnouncementService announcementService;

    @PostMapping("/preview")
    public ResponseEntity<AnnouncementPreviewResponseDto> preview(
            @Valid @RequestBody AnnouncementPreviewRequestDto request
    ) {
        return ResponseEntity.ok(announcementService.preview(request));
    }

    @PostMapping
    public ResponseEntity<AnnouncementResponseDto> send(
            @Valid @RequestBody AnnouncementCreateRequestDto request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                announcementService.send(request, authentication.getName())
        );
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementResponseDto>> history() {
        return ResponseEntity.ok(announcementService.getHistory());
    }

    @GetMapping("/{announcementId}/deliveries")
    public ResponseEntity<List<AnnouncementDeliveryResponseDto>> deliveries(
            @PathVariable Long announcementId
    ) {
        return ResponseEntity.ok(announcementService.getDeliveries(announcementId));
    }
}
