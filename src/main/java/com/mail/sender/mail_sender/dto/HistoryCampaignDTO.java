package com.mail.sender.mail_sender.dto;

import com.mail.sender.mail_sender.models.HistoryCampaign;

import java.time.LocalDateTime;

public class HistoryCampaignDTO {
    private Long id;
    private String subject;
    private String body;
    private String status;
    private LocalDateTime sendTime;
    private String recipientListName;
    private int recipientCount; // Add recipient count
    private Long recipientListId;
    private Long folderId;
    private LocalDateTime triggerDelay;

    private Long parentCampaignId;
    private String parentSubject;

    // Default constructor
    public HistoryCampaignDTO() {
    }

    public HistoryCampaignDTO(HistoryCampaign campaign) {
        this.id = campaign.getId();
        this.subject = campaign.getSubject();
        this.body = campaign.getBody();
        this.status = campaign.getStatus();
        this.sendTime = campaign.getSendTime();
        this.recipientListName = campaign.getRecipientList().getName();
        this.recipientCount = campaign.getRecipientList().getRecipients().size(); // Fetch count
        this.recipientListId = campaign.getRecipientList().getId();
        this.folderId = campaign.getRecipientList().getFolder().getId();
        this.triggerDelay = campaign.getTriggerDelay();

        this.parentCampaignId = campaign.getParentCampaign() != null ? campaign.getParentCampaign().getId() : null;
        this.parentSubject = campaign.getParentCampaign() != null ? campaign.getParentCampaign().getSubject() : null;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public String getRecipientListName() {
        return recipientListName;
    }

    public void setRecipientListName(String recipientListName) {
        this.recipientListName = recipientListName;
    }

    public int getRecipientCount() {
        return recipientCount;
    }

    public void setRecipientCount(int recipientCount) {
        this.recipientCount = recipientCount;
    }

    public Long getRecipientListId() {
        return recipientListId;
    }

    public void setRecipientListId(Long recipientListId) {
        this.recipientListId = recipientListId;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public LocalDateTime getTriggerDelay() {
        return triggerDelay;
    }

    public void setTriggerDelay(LocalDateTime triggerDelay) {
        this.triggerDelay = triggerDelay;
    }

    public Long getParentCampaignId() {
        return parentCampaignId;
    }

    public void setParentCampaignId(Long parentCampaignId) {
        this.parentCampaignId = parentCampaignId;
    }

    public String getParentSubject() {
        return parentSubject;
    }

    public void setParentSubject(String parentSubject) {
        this.parentSubject = parentSubject;
    }

    
}
