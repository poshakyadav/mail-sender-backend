package com.mail.sender.mail_sender.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EmailConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String appPassword;

    private boolean isActive;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Prevent recursive serialization
    private User user; // Associate with a user

    @Column(name = "daily_sent_count")
    private Integer dailySentCount = 0;

    // @Column(name = "last_reset_date")
    // private LocalDate lastResetDate = LocalDate.now();

    @Column(name = "last_reset_date")
    private LocalDateTime lastResetDate = LocalDateTime.now();

    @Column(name = "hourly_sent_count")
    private Integer hourlySentCount = 0;

    @Column(name = "last_hour_reset")
    private LocalDateTime lastHourReset = LocalDateTime.now();

}
