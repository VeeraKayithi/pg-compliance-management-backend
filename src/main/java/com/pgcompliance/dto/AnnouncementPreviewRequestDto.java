package com.pgcompliance.dto;

import com.pgcompliance.constant.CommunicationChannel;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AnnouncementPreviewRequestDto {
    @NotEmpty(message = "Select at least one delivery channel")
    private Set<CommunicationChannel> channels;
}
