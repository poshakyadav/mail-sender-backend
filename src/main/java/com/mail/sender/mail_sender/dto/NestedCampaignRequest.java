package com.mail.sender.mail_sender.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import com.mail.sender.mail_sender.utils.TriggerCondition;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class NestedCampaignRequest {
    @NotNull(message = "Parent campaign ID is required")
    private Long parentCampaignId;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Email body is required")
    private String body;
    
    @NotNull(message = "Trigger condition is required")
    private TriggerCondition triggerCondition;
    
    @FutureOrPresent(message = "Trigger delay must be in the future")
    private LocalDateTime triggerDelay;

    // @Min(1)
    private int delayHours; // Change from LocalDateTime to hours

    private int delayMinutes; // Changed from delayHours to delayMinutes
}
