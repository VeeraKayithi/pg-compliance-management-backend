package com.pgcompliance.service;

public interface EmailService {

    void sendTenantActivationEmail(
            String recipientEmail,
            String tenantName,
            String username,
            String activationLink
    );
}
