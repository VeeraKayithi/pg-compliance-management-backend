package com.pgcompliance.service.impl;

import com.pgcompliance.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromAddress;

  @Override
  public void sendTenantActivationEmail(
      String recipientEmail,
      String tenantName,
      String username,
      String activationLink) {
    SimpleMailMessage email = new SimpleMailMessage();
    email.setFrom(fromAddress);
    email.setTo(recipientEmail);
    email.setSubject("Activate your Nandu PG portal account");
    email.setText(
        "Hello " + tenantName + ",\n\n"
            + "Welcome to Nandu PG.\n\n"
            + "Username: " + username + "\n\n"
            + "Use the secure link below to activate your account and create your private password:\n"
            + activationLink + "\n\n"
            + "This link expires in 15 minutes and can be used only once.\n\n"
            + "If you did not expect this account, contact Nandu PG administration.\n\n"
            + "Regards,\nNandu PG");

    mailSender.send(email);
  }
}
