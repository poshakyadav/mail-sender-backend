package com.mail.sender.mail_sender.dto;

public class CampaignPerformanceDTO {
    private Long campaignId;
    private String subject;
    private int totalSent;
    private int clicks;

    public CampaignPerformanceDTO(Long campaignId, String subject, 
                                 int totalSent, int clicks) {
        this.campaignId = campaignId;
        this.subject = subject;
        this.totalSent = totalSent;
        this.clicks = clicks;
    }

    // Getters and Setters
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public int getTotalSent() { return totalSent; }
    public void setTotalSent(int totalSent) { this.totalSent = totalSent; }
    public int getClicks() { return clicks; }
    public void setClicks(int clicks) { this.clicks = clicks; }
}