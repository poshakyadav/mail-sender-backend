package com.mail.sender.mail_sender.controllers;

import com.mail.sender.mail_sender.models.*;
import com.mail.sender.mail_sender.repositories.*;
import com.mail.sender.mail_sender.utils.TriggerCondition;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mail")
public class EmailTrackingController {

        @Autowired
        private EmailTrackingRepository emailTrackingRepository;
        @Autowired
        private TrackedLinkRepository trackedLinkRepository;
        @Autowired
        private ClickEventRepository clickEventRepository;

        @Autowired
        private HistoryCampaignRepository campaignRepository;

        @Autowired
        private RecipientListRepository recipientListRepository;

        // NEW: Dynamic link tracking endpoint
        @GetMapping("/track/{uuid}")
        public ResponseEntity<Void> trackLinkClick(
                        @PathVariable String uuid,
                        HttpServletRequest request) {

                TrackedLink trackedLink = trackedLinkRepository.findById(uuid)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid tracking link"));

                // Update EmailTracking
                EmailTracking tracking = emailTrackingRepository.findByRecipientAndCampaignAndSendInstance(
                                trackedLink.getRecipient(),
                                trackedLink.getCampaign(),
                                trackedLink.getSendInstance())
                                .orElseThrow(() -> new IllegalArgumentException("Tracking record not found"));

                tracking.setClicked(true);
                emailTrackingRepository.save(tracking);

                // Get all child campaigns of the parent
                List<HistoryCampaign> childCampaigns = campaignRepository
                                .findByParentCampaign(trackedLink.getCampaign());

                for (HistoryCampaign child : childCampaigns) {
                        RecipientList childList = child.getRecipientList();
                        Recipient recipient = trackedLink.getRecipient();

                        if (child.getTriggerCondition() == TriggerCondition.CLICKED) {
                                // Add to CLICKED campaign if not already present
                                if (!childList.getRecipients().contains(recipient)) {
                                        childList.addRecipient(recipient);
                                        recipientListRepository.save(childList);
                                }
                        } else if (child.getTriggerCondition() == TriggerCondition.NOT_CLICKED) {
                                // Remove from NOT_CLICKED campaign
                                childList.getRecipients().removeIf(r -> r.getId().equals(recipient.getId()));
                                recipientListRepository.save(childList);
                        }
                }

                 // Log click event and return redirect (existing code)
                ClickEvent clickEvent = new ClickEvent();
                clickEvent.setTrackedLink(trackedLink);
                clickEvent.setTimestamp(LocalDateTime.now());
                clickEvent.setIpAddress(request.getRemoteAddr());
                clickEvent.setUserAgent(request.getHeader("User-Agent"));
                clickEventRepository.save(clickEvent);

                // Redirect to original URL
                return ResponseEntity.status(HttpStatus.FOUND)
                                .location(URI.create(trackedLink.getOriginalUrl()))
                                .build();
        }

}
