package com.mail.sender.mail_sender.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mail.sender.mail_sender.models.EmailConfiguration;
import com.mail.sender.mail_sender.models.User;

import jakarta.transaction.Transactional;

@Repository
public interface EmailConfigurationRepository extends JpaRepository<EmailConfiguration, Long> {
    Optional<EmailConfiguration> findByEmail(String email);

    Optional<EmailConfiguration> findByIsActiveTrue();

    Optional<EmailConfiguration> findByEmailAndUser(String email, User user);

    List<EmailConfiguration> findByUser(User user);

    Optional<EmailConfiguration> findByUserAndIsActiveTrue(User user);

    Optional<EmailConfiguration> findByIdAndUser(Long id, User user);

    @Modifying
    @Transactional 
    @Query("UPDATE EmailConfiguration e SET e.dailySentCount = e.dailySentCount + 1 WHERE e.id = :id")
    void incrementDailyCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE EmailConfiguration e SET e.hourlySentCount = e.hourlySentCount + 1 WHERE e.id = :id")
    void incrementHourlyCount(@Param("id") Long id);
}
