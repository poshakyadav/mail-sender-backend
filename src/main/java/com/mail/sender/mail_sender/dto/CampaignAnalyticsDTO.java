package com.mail.sender.mail_sender.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampaignAnalyticsDTO {
    private Long campaignId;
    private String subject;
    private int totalSent;
    private int clicks;
    private double clickRate;
    private List<DateCount> clicksOverTime;
    private List<LinkClickCount> topLinks;
    private List<DeviceCount> deviceTypes;
    
    // constructor, getters, setters
}
