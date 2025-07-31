package com.mail.sender.mail_sender.utils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mail.sender.mail_sender.models.HistoryCampaign;
import com.mail.sender.mail_sender.models.MailRequest;
import com.mail.sender.mail_sender.models.Recipient;
import com.mail.sender.mail_sender.repositories.EmailTrackingRepository;
import com.mail.sender.mail_sender.repositories.HistoryCampaignRepository;
import com.mail.sender.mail_sender.repositories.TrackedLinkRepository;
import com.mail.sender.mail_sender.services.EmailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CampaignScheduler {
    private final HistoryCampaignRepository campaignRepository;
    private final EmailTrackingRepository emailTrackingRepository;
    private final TrackedLinkRepository trackedLinkRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 60000) // Run every 1 minute (for testing)
    public void processScheduledCampaigns() {
        System.out.println("Running Scheduling...");
        List<HistoryCampaign> scheduledCampaigns = campaignRepository
                .findByStatusAndTriggerDelayBefore("SCHEDULED", LocalDateTime.now());

        scheduledCampaigns.forEach(campaign -> {
            try {
                // Get eligible recipients based on the campaign's trigger condition
                List<String> recipients = getEligibleRecipients(campaign);
                System.out.println("All eligible recipients " + recipients);

                if (!recipients.isEmpty()) {
                    MailRequest request = new MailRequest();
                    request.setSubject(campaign.getSubject());
                    request.setBody(campaign.getBody());
                    request.setRecipients(recipients); // Pass the filtered recipients
                    request.setCampaignId(campaign.getId());

                    // Send the email to the filtered recipients
                    emailService.sendScheduledEmail(request, campaign.getUser(), campaign);

                    // Set actual send time
                    campaign.setSendTime(LocalDateTime.now());
                }

                campaign.setStatus("SENT");
                campaignRepository.save(campaign);
            } catch (Exception e) {
                // Log error and mark campaign as failed
                campaign.setStatus("FAILED");
                campaignRepository.save(campaign);
                // TODO: Add proper error logging
            }
        });
    }


    private List<String> getEligibleRecipients(HistoryCampaign campaign) {
        return campaign.getRecipientList().getRecipients().stream()
                .map(Recipient::getEmail)
                .collect(Collectors.toList());
    }
}