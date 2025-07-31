package com.mail.sender.mail_sender.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email_tracking")
public class EmailTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Recipient recipient; // Link to recipient

    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    private HistoryCampaign campaign; // Link to campaign

    @Column(nullable = false)
    private String sendInstance; // Unique identifier per send attempt

    // private boolean opened = false; // Open tracking

    @Column(nullable = false)
    private boolean clicked = false; // Click tracking

    @Column(nullable = false)
    private boolean hardBounce = false; // Hard bounce

    @Column(nullable = false)
    private boolean softBounce = false; // Soft bounce

    private LocalDateTime sentAt;
    @CreationTimestamp
    private LocalDateTime trackedAt; // Timestamp of tracking event
}
