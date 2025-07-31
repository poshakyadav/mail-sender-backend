package com.mail.sender.mail_sender.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.mail.sender.mail_sender.models.EmailConfiguration;
import com.mail.sender.mail_sender.models.EmailTracking;
import com.mail.sender.mail_sender.models.HistoryCampaign;
import com.mail.sender.mail_sender.models.MailRequest;
import com.mail.sender.mail_sender.models.MailResponse;
import com.mail.sender.mail_sender.models.Recipient;
import com.mail.sender.mail_sender.models.RecipientList;
import com.mail.sender.mail_sender.models.TrackedLink;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.EmailConfigurationRepository;
import com.mail.sender.mail_sender.repositories.EmailTrackingRepository;
import com.mail.sender.mail_sender.repositories.HistoryCampaignRepository;
import com.mail.sender.mail_sender.repositories.RecipientListRepository;
import com.mail.sender.mail_sender.repositories.RecipientRepository;
import com.mail.sender.mail_sender.repositories.TrackedLinkRepository;
import com.mail.sender.mail_sender.repositories.UnsubscribedRecipientRepository;

import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender defaultMailSender;
    private final EmailConfigurationService emailConfigService;
    private final RecipientRepository recipientRepository;
    private final UnsubscribedRecipientRepository unsubscribedRecipientRepository;
    private final HistoryCampaignRepository campaignRepository;
    private final EmailTrackingRepository emailTrackingRepository;
    private final RecipientListRepository recipientListRepository;
    private final TrackedLinkRepository trackedLinkRepository; // New repository
    private final EmailConfigurationRepository emailConfigRepository;

    // Cache for JavaMailSender instances keyed by EmailConfiguration ID.
    private final ConcurrentMap<Long, JavaMailSender> mailSenderCache = new ConcurrentHashMap<>();

    // Dedicated lock for synchronizing configuration selection and update.
    private final Object configLock = new Object();

    public EmailServiceImpl(JavaMailSender defaultMailSender,
            EmailConfigurationService emailConfigService,
            RecipientRepository recipientRepository,
            UnsubscribedRecipientRepository unsubscribedRecipientRepository,
            HistoryCampaignRepository campaignRepository,
            EmailTrackingRepository emailTrackingRepository,
            RecipientListRepository recipientListRepository,
            TrackedLinkRepository trackedLinkRepository,
            EmailConfigurationRepository emailConfigRepository) { // Added new dependency
        this.defaultMailSender = defaultMailSender;
        this.emailConfigService = emailConfigService;
        this.recipientRepository = recipientRepository;
        this.unsubscribedRecipientRepository = unsubscribedRecipientRepository;
        this.campaignRepository = campaignRepository;
        this.emailTrackingRepository = emailTrackingRepository;
        this.recipientListRepository = recipientListRepository;
        this.trackedLinkRepository = trackedLinkRepository;
        this.emailConfigRepository = emailConfigRepository;
    }

    // Instead of creating a new JavaMailSender every time, use a cache.
    private JavaMailSender getMailSenderForConfig(EmailConfiguration config) {
        return mailSenderCache.computeIfAbsent(config.getId(),
                id -> createMailSender(config.getEmail(), config.getAppPassword()));
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

        // Enable connection pooling and set timeouts.
        props.put("mail.smtp.connectionpool", "true");
        props.put("mail.smtp.connectionpoolsize", "10"); // Match thread pool size
        // props.put("mail.smtp.connectiontimeout", "30000"); // 30 seconds
        props.put("mail.smtp.timeout", "60000"); // 60 seconds
        props.put("mail.smtp.writetimeout", "60000"); // 60 seconds

        // Debugging (logs SMTP communication)
        props.put("mail.debug", "true");
        return mailSender;
    }

    // Evict a cached JavaMailSender when a configuration is deleted.
    public void evictMailSenderForConfig(Long configId) {
        mailSenderCache.remove(configId);
    }

    @Override
    public MailResponse sendEmail(MailRequest request, User user) {
        System.out.println("Entered in sendMail method");
        try {
            EmailConfiguration config;
            if (user.isAutoSwitchEnabled()) {
                System.out.println("Entered in autoswitch if block");
                config = emailConfigService.getAvailableConfiguration(user);
                System.out.println("The current config is : " + config);
            } else {
                System.out.println("Entered in autoswitch else block");
                config = emailConfigService.getActiveConfiguration(user);
                System.out.println("The current config is : " + config);
            }

            System.out.println("Comming out from if block");

            if (config == null) {
                System.out.println("Entered in if block error");
                return new MailResponse("ERROR", "No available email configurations");
            }

            // Campaign handling
            final HistoryCampaign campaign;
            if (request.getCampaignId() == null) {
                System.out.println("Entered in campaign creating if block");
                HistoryCampaign newCampaign = new HistoryCampaign();
                newCampaign.setSubject(request.getSubject());
                newCampaign.setBody(request.getBody());
                newCampaign.setUser(user);
                newCampaign.setStatus("SENT");
                newCampaign.setSendTime(LocalDateTime.now());

                if (request.getRecipientListId() != null) {
                    System.out.println("Entered getting recipient list if block");
                    RecipientList recipientList = recipientListRepository.findById(request.getRecipientListId())
                            .orElseThrow(() -> new IllegalArgumentException("Recipient list not found"));
                    newCampaign.setRecipientList(recipientList);
                }
                campaign = campaignRepository.save(newCampaign);
            } else {
                campaign = campaignRepository.findById(request.getCampaignId())
                        .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
            }

            final HistoryCampaign savedCampaign = campaign;

            // Filter valid recipients
            List<String> validRecipients = request.getRecipients().stream()
                    .filter(email -> {
                        // System.out.println("Entered Filter valid recipients");
                        Recipient recipient = recipientRepository
                                .findByEmailAndRecipientList(email, savedCampaign.getRecipientList())
                                .orElse(null);
                        return recipient != null &&
                                !unsubscribedRecipientRepository.existsByUserAndRecipient(user, recipient);
                    })
                    .collect(Collectors.toList());

            if (validRecipients.isEmpty()) {
                // System.out.println("Entered in valid recipient is empty block");
                return new MailResponse("ERROR", "No valid recipients to send email.");
            }

            ExecutorService executorService = Executors.newFixedThreadPool(20);

            for (String recipientEmail : validRecipients) {
                // System.out.println("Entered in for loop and storing valid recipient to
                // string");
                executorService.submit(() -> {
                    // System.out.println("Entered in executorService.submit block");

                    // Dynamically select configuration if auto-switch is enabled.
                    EmailConfiguration currentConfig;
                    JavaMailSender currentMailSender;
                    synchronized (configLock) {
                        // System.out.println("Entered undered synchronized (configLock) ");
                        if (user.isAutoSwitchEnabled()) {
                            currentConfig = emailConfigService.getAvailableConfiguration(user);
                            System.out.println("Entered under the current config if block and current config is : "
                                    + currentConfig.getEmail());

                            if (currentConfig == null) {
                                System.err.println("No available configuration for " + recipientEmail);
                                return;
                            }
                            System.out.println("Entered in if block of user.isAutoSwitchEnabled() ");
                        } else {
                            currentConfig = config;
                            System.out.println("Entered under the current config else block and current config is : "
                                    + config.getEmail());
                        }
                        // Reuse JavaMailSender from cache
                        currentMailSender = getMailSenderForConfig(currentConfig);
                    }

                    String sendInstance = UUID.randomUUID().toString();
                    try {
                        // System.out.println("Entered under the try block to send the mail");
                        MimeMessage message = currentMailSender.createMimeMessage();
                        MimeMessageHelper helper = new MimeMessageHelper(message, true);

                        Recipient recipientEntity = recipientRepository
                                .findByEmailAndRecipientList(recipientEmail, savedCampaign.getRecipientList())
                                .orElseGet(() -> {
                                    Recipient newRecipient = new Recipient();
                                    newRecipient.setEmail(recipientEmail);
                                    newRecipient.setUser(user);
                                    newRecipient.setRecipientList(savedCampaign.getRecipientList());
                                    return recipientRepository.save(newRecipient);
                                });

                        String processedBody = processEmailContent(
                                request.getBody(),
                                savedCampaign,
                                recipientEntity,
                                sendInstance);
                        System.out.println("processEmailContent successfully");

                        String unsubscribeUrl = generateUnsubscribeUrl(recipientEmail, user);
                        String finalBody = processedBody + "<br><a href=\"" + unsubscribeUrl + "\">Unsubscribe</a>";
                        System.out.println("processedBody successfully");

                        helper.setFrom(currentConfig.getEmail());
                        helper.setTo(recipientEmail);
                        helper.setSubject(request.getSubject());
                        helper.setText(finalBody, true);
                        // System.out.println("setted metadata successfully for sending the mail");c,s
                        

                        // Save tracking info before sending
                        EmailTracking tracking = new EmailTracking();
                        tracking.setRecipient(recipientEntity);
                        tracking.setCampaign(savedCampaign);
                        tracking.setSendInstance(sendInstance);
                        tracking.setSentAt(LocalDateTime.now());
                        emailTrackingRepository.save(tracking);
                        System.out.println("Setted tracking details successfully");
                        System.out.println("----------------------------");

                        // Send email
                        System.out.println("Attempting to send email to " + recipientEmail);
                        currentMailSender.send(message);
                        System.out.println("Mail sent successfully to " + recipientEmail);

                        // Update count immediately after a successful send
                        // synchronized (configLock) {
                        // currentConfig.setDailySentCount(currentConfig.getDailySentCount() + 1);

                        // currentConfig.setHourlySentCount(currentConfig.getHourlySentCount() + 1);
                        // emailConfigRepository.save(currentConfig);
                        // }

                        emailConfigRepository.incrementDailyCount(currentConfig.getId());
                        emailConfigRepository.incrementHourlyCount(currentConfig.getId());
                        System.out.println("Counters updated for " + recipientEmail
                                + "\n |||||||||||||||||||||||||||||||||||||||||||");
                    } catch (MailAuthenticationException ex) {
                        System.err.println("Authentication error for " + recipientEmail);
                        ex.printStackTrace(); // Log full exception
                                              // Evict invalid sender from cache
                        mailSenderCache.remove(currentConfig.getId());
                        handleBounce(recipientEmail, savedCampaign, sendInstance, ex.getMessage());
                    } catch (MailException e) {
                        System.err.println("MailException for " + recipientEmail);
                        e.printStackTrace(); // Log full exception

                        handleBounce(recipientEmail, savedCampaign, sendInstance, e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error sending email to " + recipientEmail + ": " + e.getMessage());
                        e.printStackTrace(); // Log full exception
                    }
                });
            }

            executorService.shutdown();

            // Add graceful shutdown
            try {
                executorService.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new MailResponse("SUCCESS", "Emails are being sent asynchronously");
        } catch (Exception e) {
            return new MailResponse("ERROR", "Mail sending failed: " + e.getMessage());
        }
    }

    @Override
    public MailResponse sendScheduledEmail(MailRequest request, User user, HistoryCampaign campaign) {
        try {
            // Dynamically select configuration based on user settings.
            EmailConfiguration config;
            if (user.isAutoSwitchEnabled()) {
                config = emailConfigService.getAvailableConfiguration(user);
                System.out.println("The current config is : " + config);
            } else {
                config = emailConfigService.getActiveConfiguration(user);
            }
            if (config == null) {
                return new MailResponse("ERROR", "No available email configurations");
            }

            final HistoryCampaign savedCampaign = campaign;

            if (request.getRecipients().isEmpty()) {
                return new MailResponse("ERROR", "No valid recipients to send email.");
            }

            ExecutorService executorService = Executors.newFixedThreadPool(10);

            for (String recipientEmail : request.getRecipients()) {
                executorService.submit(() -> {
                    // Dynamically select configuration if auto-switch is enabled.
                    EmailConfiguration currentConfig;
                    JavaMailSender currentMailSender;
                    synchronized (configLock) {
                        if (user.isAutoSwitchEnabled()) {
                            currentConfig = emailConfigService.getAvailableConfiguration(user);
                            if (currentConfig == null) {
                                System.err.println("No available configuration for " + recipientEmail);
                                return;
                            }
                        } else {
                            currentConfig = config;
                        }
                        // Reuse JavaMailSender from cache.
                        currentMailSender = getMailSenderForConfig(currentConfig);
                    }

                    String sendInstance = UUID.randomUUID().toString();
                    try {
                        MimeMessage message = currentMailSender.createMimeMessage();
                        MimeMessageHelper helper = new MimeMessageHelper(message, true);

                        Recipient recipientEntity = recipientRepository
                                .findByEmailAndRecipientList(recipientEmail, savedCampaign.getRecipientList())
                                .orElseGet(() -> {
                                    Recipient newRecipient = new Recipient();
                                    newRecipient.setEmail(recipientEmail);
                                    newRecipient.setUser(user);
                                    newRecipient.setRecipientList(savedCampaign.getRecipientList());
                                    return recipientRepository.save(newRecipient);
                                });

                        String processedBody = processEmailContent(
                                request.getBody(),
                                savedCampaign,
                                recipientEntity,
                                sendInstance);

                        String unsubscribeUrl = generateUnsubscribeUrl(recipientEmail, user);
                        String finalBody = processedBody + "<br><a href=\"" + unsubscribeUrl + "\">Unsubscribe</a>";

                        helper.setFrom(currentConfig.getEmail());
                        helper.setTo(recipientEmail);
                        helper.setSubject(request.getSubject());
                        helper.setText(finalBody, true);

                        // Save tracking info before sending.
                        EmailTracking tracking = new EmailTracking();
                        tracking.setRecipient(recipientEntity);
                        tracking.setCampaign(savedCampaign);
                        tracking.setSendInstance(sendInstance);
                        tracking.setSentAt(LocalDateTime.now());
                        emailTrackingRepository.save(tracking);

                        // Send email.
                        currentMailSender.send(message);
                        System.out.println("Mail sent successfully to " + recipientEmail);

                        // Update count immediately after a successful send.
                        // synchronized (configLock) {
                        // currentConfig.setDailySentCount(currentConfig.getDailySentCount() + 1);

                        // currentConfig.setHourlySentCount(currentConfig.getHourlySentCount() + 1);
                        // emailConfigRepository.save(currentConfig);
                        // }

                        // Update counts using atomic increments
                        emailConfigRepository.incrementDailyCount(currentConfig.getId());
                        emailConfigRepository.incrementHourlyCount(currentConfig.getId());
                    } catch (MailAuthenticationException ex) {
                        // Evict invalid sender from cache.
                        mailSenderCache.remove(currentConfig.getId());
                        handleBounce(recipientEmail, savedCampaign, sendInstance, ex.getMessage());
                    } catch (MailException e) {
                        handleBounce(recipientEmail, savedCampaign, sendInstance, e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error sending email to " + recipientEmail + ": " + e.getMessage());
                    }
                });
            }

            executorService.shutdown();
            // Graceful shutdown.
            try {
                executorService.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new MailResponse("SUCCESS", "Emails are being sent asynchronously");
        } catch (Exception e) {
            return new MailResponse("ERROR", "Mail sending failed: " + e.getMessage());
        }
    }

    private String processEmailContent(String originalContent, HistoryCampaign campaign, Recipient recipient,
            String sendInstance) {
        Document doc = Jsoup.parse(originalContent);

        // Process links
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String originalUrl = link.attr("href");
            String uuid = UUID.randomUUID().toString();

            // Create and save tracked link
            TrackedLink trackedLink = new TrackedLink();
            trackedLink.setId(uuid);
            trackedLink.setOriginalUrl(originalUrl);
            trackedLink.setCampaign(campaign);
            trackedLink.setRecipient(recipient);
            trackedLink.setSendInstance(sendInstance);
            trackedLinkRepository.save(trackedLink);

            // Replace with tracking URL
            String trackingUrl = "http://localhost:8080/api/mail/track/" + uuid;
            link.attr("href", trackingUrl);
        }

        return doc.html();
    }

    private String generateUnsubscribeUrl(String recipientEmail, User user) {
        return "http://localhost:8080/api/mail/unsubscribe?email="
                + URLEncoder.encode(recipientEmail, StandardCharsets.UTF_8)
                + "&userId=" + user.getId();
    }

    private void handleBounce(String recipientEmail, HistoryCampaign campaign, String sendInstance,
            String errorMessage) {
        Recipient recipient = recipientRepository.findByEmail(recipientEmail).orElse(null);
        if (recipient == null)
            return;

        EmailTracking tracking = emailTrackingRepository
                .findByRecipientAndCampaignAndSendInstance(recipient, campaign, sendInstance)
                .orElseGet(EmailTracking::new);

        tracking.setRecipient(recipient);
        tracking.setCampaign(campaign);
        tracking.setSendInstance(sendInstance);
        tracking.setSentAt(LocalDateTime.now());

        if (errorMessage.toLowerCase().contains("550")) {
            tracking.setHardBounce(true);
        } else {
            tracking.setSoftBounce(true);
        }

        emailTrackingRepository.save(tracking);
    }
}