package com.mail.sender.mail_sender.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "click_event")
public class ClickEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne
    // @JoinColumn(name = "tracked_link_id")
    // private TrackedLink trackedLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracked_link_id")
    private TrackedLink trackedLink;

    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;

}