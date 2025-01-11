package com.mail.sender.mail_sender.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mail.sender.mail_sender.models.Recipient;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    Optional<Recipient> findByEmail(String email);
}
