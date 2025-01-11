package com.mail.sender.mail_sender.services;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mail.sender.mail_sender.models.EmailConfiguration;
import com.mail.sender.mail_sender.models.MailRequest;
import com.mail.sender.mail_sender.models.MailResponse;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender defaultMailSender; // Default sender for fallback
    private final EmailConfigurationService emailConfigService;

    public EmailServiceImpl(JavaMailSender defaultMailSender, EmailConfigurationService emailConfigService) {
        this.defaultMailSender = defaultMailSender;
        this.emailConfigService = emailConfigService;
    }

    private JavaMailSender createMailSender(String email, String appPassword) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(email);
        mailSender.setPassword(appPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return mailSender;
    }

    @Override
    public MailResponse sendEmail(MailRequest request) {
        try {
            EmailConfiguration activeConfig = emailConfigService.getActiveConfiguration();

            // Create a new JavaMailSender instance with the active configuration
            JavaMailSender mailSender = createMailSender(activeConfig.getEmail(), activeConfig.getAppPassword());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(activeConfig.getEmail());
            helper.setTo(request.getRecipients().toArray(new String[0]));
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), request.isHtml());

            if (request.getAttachments() != null) {
                for (MultipartFile file : request.getAttachments()) {
                    helper.addAttachment(file.getOriginalFilename(), file);
                }
            }

            mailSender.send(message);
            return new MailResponse("SUCCESS", "Mail sent successfully");
        } catch (Exception e) {
            return new MailResponse("ERROR", "Mail sending failed: " + e.getMessage());
        }
    }
}
