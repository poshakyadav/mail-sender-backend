package com.mail.sender.mail_sender.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String company;
    private String position;

    @Email
    @NotBlank
    private String email;

    @CreationTimestamp
    private LocalDateTime addedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Prevent recursive serialization
    private User user; // Associate with a user

    @ManyToOne
    @JoinColumn(name = "recipient_list_id", nullable = false)
    @JsonIgnore // Prevent recursive serialization
    private RecipientList recipientList;

    @Column(nullable = false)
    private boolean isUnsubscribed = false;

}
