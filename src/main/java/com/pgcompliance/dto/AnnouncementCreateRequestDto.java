package com.pgcompliance.dto;

import com.pgcompliance.constant.AnnouncementPriority;
import com.pgcompliance.constant.CommunicationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AnnouncementCreateRequestDto {
    @NotBlank(message = "Announcement title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Announcement message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;

    @NotNull(message = "Priority is required")
    private AnnouncementPriority priority;

    @NotEmpty(message = "Select at least one delivery channel")
    private Set<CommunicationChannel> channels;

    @Size(max = 300, message = "Deep link must not exceed 300 characters")
    private String deepLink;
}
