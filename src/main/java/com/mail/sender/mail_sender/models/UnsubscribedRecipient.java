package com.mail.sender.mail_sender.models;

import jakarta.persistence.*;

@Entity
@Table(name = "unsubscribed_recipients")
public class UnsubscribedRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Company/User who sent the email
    private User user;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false) // Recipient who unsubscribed
    private Recipient recipient;

    // Constructor
    public UnsubscribedRecipient() {}

    public UnsubscribedRecipient(User user, Recipient recipient) {
        this.user = user;
        this.recipient = recipient;
    }

    // Getters and Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }
}
