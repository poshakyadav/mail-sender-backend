package com.mail.sender.mail_sender.models;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tracked_link")
public class TrackedLink {
    @Id
    private String id; // UUID

    private String originalUrl;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private HistoryCampaign campaign;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Recipient recipient;

    private String sendInstance;

    @OneToMany(mappedBy = "trackedLink", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClickEvent> clickEvents = new ArrayList<>();
}