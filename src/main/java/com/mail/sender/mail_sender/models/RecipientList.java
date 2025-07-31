package com.mail.sender.mail_sender.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RecipientList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JsonIgnore // Prevent recursive serialization
    private Folder folder;

    @OneToMany(mappedBy = "recipientList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recipient> recipients = new ArrayList<>();

    // New mapping to delete attached campaigns automatically
    @OneToMany(mappedBy = "recipientList", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<HistoryCampaign> historyCampaigns = new ArrayList<>();


    // Getters and Setters

    // Helper method
    public void addRecipient(Recipient recipient) {
        recipients.add(recipient);
        recipient.setRecipientList(this);
    }
}