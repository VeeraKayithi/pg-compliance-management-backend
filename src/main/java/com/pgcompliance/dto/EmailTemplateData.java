package com.pgcompliance.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailTemplateData {
    private String recipientEmail;
    private String recipientName;
    private String emailTitle;
    private String emailCategory;
    private String emailHeading;
    private String emailSummary;
    private String emailBody;
    private String detailsTitle;
    private String emailDetails;
    private String actionLabel;
    private String actionUrl;
}
