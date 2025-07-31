package com.mail.sender.mail_sender.controllers;

import com.mail.sender.mail_sender.dto.CampaignAnalyticsDTO;
import com.mail.sender.mail_sender.dto.DateCount;
import com.mail.sender.mail_sender.dto.DeviceCount;
import com.mail.sender.mail_sender.dto.HistoryCampaignDTO;
import com.mail.sender.mail_sender.dto.LinkClickCount;
import com.mail.sender.mail_sender.dto.NestedCampaignRequest;
import com.mail.sender.mail_sender.models.*;
import com.mail.sender.mail_sender.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.mail.sender.mail_sender.services.EmailService;
import com.mail.sender.mail_sender.services.JwtService;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mail")
public class MailController {

        private final EmailService emailService;
        private final JwtService jwtService;
        private final UserRepository userRepository;
        private final RecipientListRepository recipientListRepository;
        private final HistoryCampaignRepository campaignRepository;
        private final RecipientRepository recipientRepository;
        private final UnsubscribedRecipientRepository unsubscribedRecipientRepository;
        private final EmailTrackingRepository emailTrackingRepository;
        private final TrackedLinkRepository trackedLinkRepository;
        private final ClickEventRepository clickEventRepository;

        public MailController(EmailService emailService, JwtService jwtService, UserRepository userRepository,
                        RecipientListRepository recipientListRepository, HistoryCampaignRepository campaignRepository,
                        RecipientRepository recipientRepository,
                        UnsubscribedRecipientRepository unsubscribedRecipientRepository,
                        EmailTrackingRepository emailTrackingRepository,
                        TrackedLinkRepository trackedLinkRepository,
                        ClickEventRepository clickEventRepository) {
                this.emailService = emailService;
                this.jwtService = jwtService;
                this.userRepository = userRepository;
                this.recipientListRepository = recipientListRepository;
                this.campaignRepository = campaignRepository;
                this.recipientRepository = recipientRepository;
                this.unsubscribedRecipientRepository = unsubscribedRecipientRepository;
                this.emailTrackingRepository = emailTrackingRepository;
                this.trackedLinkRepository = trackedLinkRepository;
                this.clickEventRepository = clickEventRepository;
        }

        @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<MailResponse> sendEmail(
                        @ModelAttribute @Valid MailRequest request,
                        @RequestHeader("Authorization") String token) {
                System.out.println("Mail sending process start");
                System.out.println("printing Recipient List Id : " + request.getRecipientListId());
                System.out.println("printing campaign id : " + request.getCampaignId());

                String jwt = token.substring(7); // Remove "Bearer "
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                MailResponse response = emailService.sendEmail(request, user);
                System.out.println("Mail Sending process ends");
                return ResponseEntity.ok(response);
        }

        @PostMapping(value = "/send/list/{listId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<MailResponse> sendEmailToList(
                        @PathVariable Long listId,
                        @ModelAttribute @Valid MailRequest request,
                        @RequestHeader("Authorization") String token) {

                System.out.println("Entered in sendEmailToList()");
                System.out.println("Campaign id :" + request.getCampaignId());
                System.out.println("Recipient List Id : " + request.getRecipientListId());
                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                RecipientList recipientList = recipientListRepository.findById(listId)
                                .filter(list -> list.getFolder().getUser().equals(user)) // Ensure list belongs to user
                                .orElseThrow(() -> new IllegalArgumentException("Recipient list not found"));

                List<String> recipientEmails = recipientList.getRecipients()
                                .stream()
                                .map(Recipient::getEmail)
                                .collect(Collectors.toList());

                request.setRecipients(recipientEmails);
                return sendEmail(request, token); // Reuse the existing sendEmail logic
        }

        // ---------------------------------------- for campaign-----------
        // New Endpoint for Saving a Campaign
        @PostMapping(value = "/save-campaign")
        public ResponseEntity<HistoryCampaign> saveCampaign(
                        @ModelAttribute @Valid MailRequest request,
                        @RequestHeader("Authorization") String token) {

                System.out.println("Mail Saving start");
                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                System.out.println("Recipient list ID is: " + request.getRecipientListId());
                if (request.getRecipientListId() == null) {
                        throw new IllegalArgumentException("Recipient list ID is required");
                }

                RecipientList recipientList = recipientListRepository.findById(request.getRecipientListId())
                                .orElseThrow(() -> new IllegalArgumentException("Recipient list not found"));

                HistoryCampaign campaign = new HistoryCampaign();
                campaign.setSubject(request.getSubject());
                campaign.setBody(request.getBody());
                campaign.setUser(user);
                campaign.setRecipientList(recipientList);
                campaign.setStatus("DRAFT"); // Changed status from "SENT" to "DRAFT"
                campaign.setSendTime(LocalDateTime.now());

                // Save campaign to the database
                HistoryCampaign savedCampaign = campaignRepository.save(campaign);

                System.out.println("Mail Saving end");
                return ResponseEntity.ok(savedCampaign);
        }

        // New Endpoint for Fetching Sent Campaigns
        @GetMapping("/sent-campaigns")
        public ResponseEntity<List<HistoryCampaignDTO>> getSentCampaigns(@RequestHeader("Authorization") String token) {
                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                List<HistoryCampaign> campaigns = campaignRepository.findByUser(user);

                // Convert List<HistoryCampaign> to List<HistoryCampaignDTO>
                List<HistoryCampaignDTO> campaignDTOs = campaigns.stream()
                                .map(HistoryCampaignDTO::new)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(campaignDTOs);
        }

        @PostMapping("/resend/{campaignId}")
        public ResponseEntity<MailResponse> resendCampaign(
                        @PathVariable Long campaignId,
                        @RequestHeader("Authorization") String token) {

                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                HistoryCampaign campaign = campaignRepository.findById(campaignId)
                                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

                // Retrieve the recipient list
                RecipientList recipientList = campaign.getRecipientList();
                if (recipientList == null) {
                        throw new IllegalArgumentException("No recipient list found for this campaign");
                }

                // Get all recipient emails
                List<String> recipientEmails = recipientList.getRecipients()
                                .stream()
                                .map(Recipient::getEmail)
                                .collect(Collectors.toList());

                if (recipientEmails.isEmpty()) {
                        throw new IllegalArgumentException("No recipients found in the list");
                }

                // Create a new MailRequest and set recipients
                MailRequest request = new MailRequest();
                request.setSubject(campaign.getSubject());
                request.setBody(campaign.getBody());
                request.setRecipients(recipientEmails); // Set the recipients before sending the email
                request.setCampaignId(campaignId);

                // Send the email
                MailResponse response = emailService.sendEmail(request, user);

                return ResponseEntity.ok(response);
        }

        // ---------------------------------------------
        @GetMapping("/campaign/{campaignId}/recipient-list-status")
        public ResponseEntity<Map<String, Object>> getRecipientListStatus(
                        @PathVariable Long campaignId,
                        @RequestHeader("Authorization") String token) {

                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                HistoryCampaign campaign = campaignRepository.findById(campaignId)
                                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

                RecipientList recipientList = campaign.getRecipientList();

                // Count the number of campaigns using this recipient list
                List<HistoryCampaign> campaignsUsingList = campaignRepository.findByRecipientList(recipientList);
                int usageCount = campaignsUsingList.size();

                Map<String, Object> response = new HashMap<>();
                response.put("usageCount", usageCount);
                response.put("recipientListId", recipientList.getId());
                return ResponseEntity.ok(response);
        }

        // -----------------------------------------------
        // Delete a campaign by ID
        @DeleteMapping("/delete-campaign/{campaignId}")
        public ResponseEntity<?> deleteCampaign(
                        @PathVariable Long campaignId,
                        @RequestParam(required = false, defaultValue = "false") boolean deleteRecipientList,
                        @RequestHeader("Authorization") String token) {

                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                HistoryCampaign campaign = campaignRepository.findById(campaignId)
                                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

                // Ensure user owns the campaign before deleting
                if (!campaign.getUser().equals(user)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body("You are not authorized to delete this campaign.");
                }

                RecipientList recipientList = campaign.getRecipientList();
                List<HistoryCampaign> campaignsUsingList = campaignRepository.findByRecipientList(recipientList);

                // If more than one campaign uses the list, do not allow deletion of the
                // recipient list.
                if (campaignsUsingList.size() > 1 && deleteRecipientList) {
                        return ResponseEntity.badRequest().body(
                                        "Recipient list cannot be deleted because it is attached to other campaigns. Delete campaign only?");
                }

                campaignRepository.delete(campaign);

                
                // Optionally delete the recipient list if it's only attached to this campaign
                if (deleteRecipientList && campaignsUsingList.size() == 1) {
                        recipientListRepository.delete(recipientList);
                        return ResponseEntity.ok("Campaign and associated recipient list deleted successfully.");
                } else {
                        return ResponseEntity.ok("Campaign deleted successfully. Recipient list remains.");
                }
        }

        // Fetch a single campaign for editing
        @GetMapping("/campaign/{campaignId}")
        public ResponseEntity<HistoryCampaignDTO> getCampaign(
                        @PathVariable Long campaignId,
                        @RequestHeader("Authorization") String token) {

                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                HistoryCampaign campaign = campaignRepository.findById(campaignId)
                                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

                if (!campaign.getUser().equals(user)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }

                return ResponseEntity.ok(new HistoryCampaignDTO(campaign));
        }

        // Update an existing campaign
        @PutMapping("/update-campaign/{campaignId}")
        public ResponseEntity<HistoryCampaign> updateCampaign(
                        @PathVariable Long campaignId,
                        @RequestBody HistoryCampaignDTO updatedCampaign,
                        @RequestHeader("Authorization") String token) {

                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                HistoryCampaign campaign = campaignRepository.findById(campaignId)
                                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

                if (!campaign.getUser().equals(user)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }

                // Update campaign details
                campaign.setSubject(updatedCampaign.getSubject());
                campaign.setBody(updatedCampaign.getBody());

                if (updatedCampaign.getRecipientListId() != null) {
                        RecipientList newRecipientList = recipientListRepository
                                        .findById(updatedCampaign.getRecipientListId())
                                        .orElseThrow(() -> new IllegalArgumentException("Recipient list not found"));
                        campaign.setRecipientList(newRecipientList);
                }

                HistoryCampaign savedCampaign = campaignRepository.save(campaign);
                return ResponseEntity.ok(savedCampaign);
        }

        // Use this endpoint implementation in your MailController
        @GetMapping("/campaign/{campaignId}/analytics")
        public ResponseEntity<CampaignAnalyticsDTO> getCampaignAnalytics(
                        @PathVariable Long campaignId,
                        @RequestHeader("Authorization") String token) {

                // Authentication and authorization checks
                User user = getUserFromToken(token);
                HistoryCampaign campaign = campaignRepository.findById(campaignId)
                                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

                if (!campaign.getUser().equals(user)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

                // Calculate basic metrics
                int totalSent = emailTrackingRepository.countByCampaign(campaign);
                int clicks = emailTrackingRepository.countByCampaignAndClickedTrue(campaign);

                // Build DTO
                CampaignAnalyticsDTO analytics = new CampaignAnalyticsDTO();
                analytics.setCampaignId(campaign.getId());
                analytics.setSubject(campaign.getSubject());
                analytics.setTotalSent(totalSent);
                analytics.setClicks(clicks);
                analytics.setClickRate(totalSent > 0 ? (double) clicks / totalSent * 100 : 0);

                // Get clicks over time
                List<Object[]> clicksData = clickEventRepository.findClicksOverTime(campaign);
                List<DateCount> clicksOverTime = clicksData.stream()
                                .map(o -> new DateCount(
                                                ((java.sql.Date) o[0]).toLocalDate(),
                                                ((Long) o[1]).intValue()))
                                .collect(Collectors.toList());
                analytics.setClicksOverTime(clicksOverTime);

                // Get top clicked links
                List<Object[]> linkData = trackedLinkRepository.findTopLinks(campaign);
                List<LinkClickCount> topLinks = linkData.stream()
                                .map(l -> new LinkClickCount(
                                                (String) l[0],
                                                ((Long) l[1]).intValue()))
                                .collect(Collectors.toList());
                analytics.setTopLinks(topLinks);

                // Get device types
                List<Object[]> deviceData = clickEventRepository.findDevicesByCampaign(campaign);
                Map<String, Long> deviceMap = new HashMap<>();

                deviceData.forEach(row -> {
                        String userAgent = (String) row[0];
                        Long count = (Long) row[1];
                        String deviceType = parseDevice(userAgent);
                        deviceMap.merge(deviceType, count, Long::sum);
                });

                // Convert to list of DeviceCount objects
                List<DeviceCount> devices = deviceMap.entrySet().stream()
                                .map(entry -> new DeviceCount(entry.getKey(), entry.getValue()))
                                .collect(Collectors.toList());

                analytics.setDeviceTypes(devices);

                return ResponseEntity.ok(analytics);
        }

        // /-----------------------------------------

        // Get eligible recipients for nested campaign
        @GetMapping("/campaign/{campaignId}/recipients")
        public ResponseEntity<List<String>> getRecipientsByAction(
                        @PathVariable Long campaignId,
                        @RequestParam String actionType,
                        @RequestHeader("Authorization") String token) {
                User user = getUserFromToken(token);
                HistoryCampaign campaign = campaignRepository.findById(campaignId)
                                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

                List<String> recipients = new ArrayList<>();

                switch (actionType.toUpperCase()) {
                        // case "OPENED":
                        // recipients = emailTrackingRepository.findOpenedRecipients(campaign);
                        // break;
                        // case "NOT_OPENED":
                        // recipients = emailTrackingRepository.findNotOpenedRecipients(campaign);
                        // break;
                        case "CLICKED":
                                recipients = trackedLinkRepository.findClickedRecipients(campaign);
                                break;
                        case "NOT_CLICKED":
                                recipients = emailTrackingRepository.findNotClickedRecipients(campaign);
                                break;
                }

                return ResponseEntity.ok(recipients);
        }

        // Create nested campaign
        @PostMapping("/nested-campaign")
        public ResponseEntity<HistoryCampaign> createNestedCampaign(
                        @RequestBody NestedCampaignRequest request,
                        @RequestHeader("Authorization") String token) {
                User user = getUserFromToken(token);

                HistoryCampaign parentCampaign = campaignRepository.findById(request.getParentCampaignId())
                                .orElseThrow(() -> new IllegalArgumentException("Parent campaign not found"));

                // Create a new empty recipient list for the nested campaign
                RecipientList childRecipientList = new RecipientList();
                childRecipientList.setName("Follow-up for " + parentCampaign.getSubject());
                childRecipientList.setFolder(parentCampaign.getRecipientList().getFolder()); // Inherit folder from
                                                                                             // parent
                recipientListRepository.save(childRecipientList);

                HistoryCampaign newCampaign = new HistoryCampaign();
                newCampaign.setSubject(request.getSubject());
                newCampaign.setBody(request.getBody());
                newCampaign.setParentCampaign(parentCampaign);
                // newCampaign.setRecipientList(parentCampaign.getRecipientList()); // Inherit
                // recipient list
                newCampaign.setRecipientList(childRecipientList);
                newCampaign.setTriggerCondition(request.getTriggerCondition());
                // newCampaign.setTriggerDelay(LocalDateTime.now().plusHours(request.getDelayHours()));
                newCampaign.setTriggerDelay(LocalDateTime.now().plusMinutes(request.getDelayMinutes())); // Use minutes
                newCampaign.setStatus("SCHEDULED");
                newCampaign.setSendTime(null); // Explicitly set sendTime to null
                newCampaign.setUser(user);

                return ResponseEntity.ok(campaignRepository.save(newCampaign));
        }

        private User getUserFromToken(String token) {
                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);
                return userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        // Add this helper method at the bottom of MailController
        private String parseDevice(String userAgent) {
                if (userAgent == null)
                        return "Unknown";
                String ua = userAgent.toLowerCase();

                if (ua.contains("mobile"))
                        return "Mobile";
                if (ua.contains("tablet"))
                        return "Tablet";
                if (ua.contains("windows") || ua.contains("macintosh"))
                        return "Desktop";
                return "Other";
        }

}
