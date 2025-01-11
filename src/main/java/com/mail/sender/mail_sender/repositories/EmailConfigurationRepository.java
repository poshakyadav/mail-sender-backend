package com.mail.sender.mail_sender.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mail.sender.mail_sender.models.EmailConfiguration;

@Repository
public interface EmailConfigurationRepository extends JpaRepository<EmailConfiguration, Long> {
    Optional<EmailConfiguration> findByEmail(String email);
    Optional<EmailConfiguration> findByIsActiveTrue();
}

