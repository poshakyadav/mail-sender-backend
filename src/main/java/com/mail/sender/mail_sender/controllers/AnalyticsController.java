package com.mail.sender.mail_sender.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mail.sender.mail_sender.dto.CampaignPerformanceDTO;
import com.mail.sender.mail_sender.dto.DashboardAnalyticsDTO;
import com.mail.sender.mail_sender.dto.DeviceCount;
import com.mail.sender.mail_sender.models.HistoryCampaign;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.ClickEventRepository;
import com.mail.sender.mail_sender.repositories.EmailTrackingRepository;
import com.mail.sender.mail_sender.repositories.HistoryCampaignRepository;
import com.mail.sender.mail_sender.repositories.UserRepository;
import com.mail.sender.mail_sender.services.JwtService;

@RestController
@RequestMapping("/api/mail/analytics")
public class AnalyticsController {

    private final EmailTrackingRepository emailTrackingRepository;
    private final HistoryCampaignRepository campaignRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ClickEventRepository clickEventRepository;

    public AnalyticsController(EmailTrackingRepository emailTrackingRepository,
            HistoryCampaignRepository campaignRepository,
            JwtService jwtService,
            UserRepository userRepository, ClickEventRepository clickEventRepository) {
        this.emailTrackingRepository = emailTrackingRepository;
        this.campaignRepository = campaignRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.clickEventRepository = clickEventRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardAnalyticsDTO> getDashboardAnalytics(
            @RequestHeader("Authorization") String token) {

        User user = getUserFromToken(token);

        List<HistoryCampaign> campaigns = campaignRepository.findByUser(user);
        DashboardAnalyticsDTO dashboard = new DashboardAnalyticsDTO();

        // Total campaigns
        dashboard.setTotalCampaigns(campaigns.size());

        // Aggregate stats
        int totalClicks = emailTrackingRepository.countByUserAndClickedTrue(user);
        dashboard.setTotalClicks(totalClicks);

        // // Calculate average click rate
        // int totalSent = emailTrackingRepository.countByUser(user);
        // dashboard.setAvgClickRate(totalSent > 0 ? (double) totalClicks / totalSent *
        // 100 : 0);

        // New: Calculate total sent emails
        int totalSent = campaigns.stream()
                .mapToInt(c -> emailTrackingRepository.countByCampaign(c))
                .sum();
        dashboard.setTotalSent(totalSent);

        // Calculate average click rate using totalSent
        dashboard.setAvgClickRate(totalSent > 0 ? (double) totalClicks / totalSent * 100 : 0);

        // Device distribution calculation
        Map<String, Long> deviceMap = new HashMap<>();
        campaigns.forEach(campaign -> {
            List<Object[]> deviceData = clickEventRepository.findDevicesByCampaign(campaign);
            deviceData.forEach(row -> {
                String userAgent = (String) row[0];
                Long count = (Long) row[1];
                String deviceType = parseDevice(userAgent);
                deviceMap.merge(deviceType, count, Long::sum);
            });
        });

        List<DeviceCount> deviceDistribution = deviceMap.entrySet().stream()
                .map(entry -> new DeviceCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        dashboard.setDeviceDistribution(deviceDistribution);

        // Campaign performance
        List<CampaignPerformanceDTO> performance = campaigns.stream()
                .map(c -> new CampaignPerformanceDTO(
                        c.getId(),
                        c.getSubject(),
                        emailTrackingRepository.countByCampaign(c),
                        emailTrackingRepository.countByCampaignAndClickedTrue(c)))
                .collect(Collectors.toList());
        dashboard.setCampaignPerformance(performance);

        return ResponseEntity.ok(dashboard);
    }

    private User getUserFromToken(String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // Add device parsing helper method
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
