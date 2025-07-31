package com.mail.sender.mail_sender.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardAnalyticsDTO {
    private int totalCampaigns;
    private int totalClicks;
    private double avgClickRate;
    private List<CampaignPerformanceDTO> campaignPerformance;
    // private List<CampaignAnalyticsDTO> campaignAnalyticsDTOs;

    private int totalSent;
    private List<DeviceCount> deviceDistribution;
    
    // constructor, getters, setters
}

