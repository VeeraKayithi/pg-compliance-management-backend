package com.pgcompliance.service;

import com.pgcompliance.dto.EmailTemplateData;

public interface EmailService {
    void sendEmail(EmailTemplateData templateData);

    void sendTenantActivationEmail(
            String recipientEmail,
            String tenantName,
            String username,
            String activationLink
    );
}
