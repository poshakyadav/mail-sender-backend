package com.mail.sender.mail_sender.repositories;

import com.mail.sender.mail_sender.models.HistoryCampaign;
import com.mail.sender.mail_sender.models.TrackedLink;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackedLinkRepository extends JpaRepository<TrackedLink, String> {

        @Query("SELECT t.originalUrl, COUNT(c) FROM TrackedLink t " +
                        "JOIN t.clickEvents c " +
                        "WHERE t.campaign = :campaign " +
                        "GROUP BY t.originalUrl " +
                        "ORDER BY COUNT(c) DESC")
        List<Object[]> findTopLinks(@Param("campaign") HistoryCampaign campaign);

        // ------------------------------------------------------
        // In TrackedLinkRepository.java
        // @Query("SELECT DISTINCT tl.recipient.email FROM TrackedLink tl " +
        // "WHERE tl.campaign = :campaign")
        // List<String> findClickedRecipients(@Param("campaign") HistoryCampaign
        // campaign);

        @Query("SELECT DISTINCT tl.recipient.email FROM TrackedLink tl " +
                        "WHERE tl.campaign = :campaign " +
                        "AND EXISTS (SELECT 1 FROM ClickEvent c WHERE c.trackedLink = tl)")
        List<String> findClickedRecipients(@Param("campaign") HistoryCampaign campaign);

}
