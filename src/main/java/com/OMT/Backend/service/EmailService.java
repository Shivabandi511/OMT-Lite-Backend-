package com.OMT.Backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:bandishivakumar880@gmail.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // In-memory debug storage for latest dispatched OTPs (accessible during assessment / demo testing)
    private final Map<String, String> latestOtps = new ConcurrentHashMap<>();

    public EmailService() {
    }

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeCredentialsEmail(String toEmail, String name, String password, String role, boolean canMakeOperations) {
        String accessType = canMakeOperations ? "Full Operations (Vehicle Intake, Editing & Workflow)" : "Read-Only (View & Search Only)";

        String subject = "Welcome to OMT Lite - Your Account Credentials";
        String loginUrl = frontendUrl + "/mfa/login";
        String body = String.format("""
            Hello %s,

            Your Tenant Administrator has set up your account on OMT Lite (Yard & Vehicle Management System).

            Here are your login credentials:
            --------------------------------------------------
            Portal URL:  %s
            Username:    %s
            Password:    %s
            Role:        %s
            Access:      %s
            --------------------------------------------------

            For security, please sign in and update your password if desired.

            Regards,
            OMT Lite Team
            Yard & Vehicle Management System
            """, name, loginUrl, toEmail, password, role, accessType);

        sendEmail(toEmail, subject, body);
    }

    public void sendOtpEmail(String toEmail, String name, String otpCode, String purpose) {
        latestOtps.put(toEmail.toLowerCase(), otpCode);

        String recipientName = (name != null && !name.trim().isEmpty()) ? name.trim() : "User";
        String expiryTime = java.time.LocalDateTime.now()
                .plusMinutes(5)
                .format(java.time.format.DateTimeFormatter.ofPattern("h:mm a 'on' dd MMM yyyy"));

        String subject = otpCode + " is your OMT Lite login verification code";
        String body = String.format("""
            Hi %s,

            Use the verification code below to sign in to your OMT Lite account:

                %s

            This code can be used once. It expires 5 minutes from now, at
            %s.
            For your security, do not share it with anyone.

            If you did not request this code, someone else knows your
            password — this code is only sent after a password has been
            accepted.

            Change your password now: open the OMT Lite app yourself, sign
            in, and change your password from your profile settings. There is
            no link in this email on purpose — do not trust one that arrives in
            any email asking you to do this.

            — OMT Lite
            """, recipientName, otpCode, expiryTime);

        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String toEmail, String subject, String body) {
        log.info("================== [OUTGOING EMAIL DISPATCH] ==================");
        log.info("To: {}", toEmail);
        log.info("Subject: {}", subject);
        log.info("Body:\n{}", body);
        log.info("===============================================================");

        if (mailSender != null) {
            try {
                jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
                org.springframework.mail.javamail.MimeMessageHelper helper = 
                        new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, false, "UTF-8");
                
                String senderEmail = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "shivabandi880@gmail.com";
                helper.setFrom(senderEmail, "OMT Lite");

                // If fictional assessment domain (@omt.com), route to the verified tester Gmail
                String destinationEmail = toEmail;
                if (toEmail != null && toEmail.toLowerCase().endsWith("@omt.com")) {
                    destinationEmail = "bandishivakumar880@gmail.com";
                }
                helper.setTo(destinationEmail);
                helper.setSubject(subject);
                helper.setText(body);
                mailSender.send(mimeMessage);
                log.info("Email successfully dispatched via SMTP to {} (original recipient: {})", destinationEmail, toEmail);
            } catch (Exception e) {
                log.error("SMTP send failed: {}", e.getMessage(), e);
            }
        }
    }

    public String getLatestOtp(String email) {
        return latestOtps.get(email != null ? email.toLowerCase() : "");
    }
}
