package com.pgcompliance.service.impl;

import com.pgcompliance.dto.EmailTemplateData;
import com.pgcompliance.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Year;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String TEMPLATE_PATH =
            "templates/nandu-email-template.html";
    private static final String LOGO_PATH =
            "static/images/nandu-logo.png";
    private static final String LOGO_CONTENT_ID = "nanduLogo";

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendEmail(EmailTemplateData data) {
        validate(data);

        try {
            String html = replaceVariables(loadTemplate(), data);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(senderEmail, "Nandu PG Management");
            helper.setTo(data.getRecipientEmail());
            helper.setSubject(data.getEmailTitle());
            helper.setText(html, true);

            ClassPathResource logo = new ClassPathResource(LOGO_PATH);
            if (!logo.exists()) {
                throw new IllegalStateException(
                        "Email logo not found at classpath:" + LOGO_PATH
                );
            }

            helper.addInline(LOGO_CONTENT_ID, logo, "image/png");
            mailSender.send(message);
        } catch (MessagingException | IOException | MailException exception) {
            throw new IllegalStateException(
                    "Unable to send Nandu PG HTML email",
                    exception
            );
        }
    }

    @Override
    public void sendTenantActivationEmail(
            String recipientEmail,
            String tenantName,
            String username,
            String activationLink
    ) {
        String details =
                "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">"
                        + "<tr><td style=\"padding:5px 0;color:#78716c;font-size:13px;\">Username</td>"
                        + "<td align=\"right\" style=\"padding:5px 0;color:#292524;font-size:13px;font-weight:700;\">"
                        + escapeHtml(username)
                        + "</td></tr>"
                        + "<tr><td style=\"padding:5px 0;color:#78716c;font-size:13px;\">Link validity</td>"
                        + "<td align=\"right\" style=\"padding:5px 0;color:#292524;font-size:13px;font-weight:700;\">15 minutes</td></tr>"
                        + "</table>";

        sendEmail(EmailTemplateData.builder()
                .recipientEmail(recipientEmail)
                .recipientName(tenantName)
                .emailTitle("Activate your Nandu PG portal account")
                .emailCategory("Resident Portal")
                .emailHeading("Your portal access is ready")
                .emailSummary("Create your private password to activate your resident account.")
                .emailBody("Your Nandu PG portal account has been created. Use the secure activation button below to create your private password and complete account activation.")
                .detailsTitle("Account Information")
                .emailDetails(details)
                .actionLabel("Activate Account")
                .actionUrl(activationLink)
                .build());
    }

    private String loadTemplate() throws IOException {
        ClassPathResource template = new ClassPathResource(TEMPLATE_PATH);
        if (!template.exists()) {
            throw new IllegalStateException(
                    "Email template not found at classpath:" + TEMPLATE_PATH
            );
        }

        try (InputStream inputStream = template.getInputStream()) {
            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }

    private String replaceVariables(String template, EmailTemplateData data) {
        return template
                .replace("{{EMAIL_TITLE}}", escapeHtml(data.getEmailTitle()))
                .replace("{{EMAIL_CATEGORY}}", escapeHtml(data.getEmailCategory()))
                .replace("{{EMAIL_HEADING}}", escapeHtml(data.getEmailHeading()))
                .replace("{{EMAIL_SUMMARY}}", escapeHtml(data.getEmailSummary()))
                .replace("{{RECIPIENT_NAME}}", escapeHtml(data.getRecipientName()))
                .replace("{{EMAIL_BODY}}", formatBody(data.getEmailBody()))
                .replace("{{DETAILS_TITLE}}", escapeHtml(data.getDetailsTitle()))
                .replace("{{EMAIL_DETAILS}}", valueOrEmpty(data.getEmailDetails()))
                .replace("{{ACTION_LABEL}}", escapeHtml(data.getActionLabel()))
                .replace("{{ACTION_URL}}", escapeHtmlAttribute(data.getActionUrl()))
                .replace("{{CURRENT_YEAR}}", String.valueOf(Year.now().getValue()));
    }

    private void validate(EmailTemplateData data) {
        if (data == null) {
            throw new IllegalStateException("Email template data is required");
        }
        require(data.getRecipientEmail(), "Recipient email is required");
        require(data.getRecipientName(), "Recipient name is required");
        require(data.getEmailTitle(), "Email title is required");
        require(data.getEmailHeading(), "Email heading is required");
        require(data.getEmailBody(), "Email body is required");
        require(data.getActionLabel(), "Action label is required");
        require(data.getActionUrl(), "Action URL is required");
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
    }

    private String formatBody(String value) {
        return escapeHtml(valueOrEmpty(value)).replace("\n", "<br>");
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String escapeHtmlAttribute(String value) {
        return escapeHtml(valueOrEmpty(value));
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
