package com.mail.sender.mail_sender.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mail.sender.mail_sender.utils.TriggerCondition;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class HistoryCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    @Lob
    private String body;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "recipient_list_id", nullable = false)
    private RecipientList recipientList;

    private String status; // Example status: "SENT", "PENDING", etc.

    private LocalDateTime sendTime;

    // ------------------
    @ManyToOne
    @JoinColumn(name = "parent_campaign_id")
    @JsonIgnore 
    private HistoryCampaign parentCampaign;

    @OneToMany(mappedBy = "parentCampaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoryCampaign> childCampaigns;

    @Enumerated(EnumType.STRING)
    private TriggerCondition triggerCondition;

    private LocalDateTime triggerDelay; // Time after which to send

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailTracking> emailTrackings;

     // Add Cascade Delete for TrackedLink
     @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
     private List<TrackedLink> trackedLinks;

    // Getters and Setters
}
