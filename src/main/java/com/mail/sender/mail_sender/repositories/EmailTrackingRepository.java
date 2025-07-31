package com.mail.sender.mail_sender.repositories;

import com.mail.sender.mail_sender.models.EmailTracking;
import com.mail.sender.mail_sender.models.HistoryCampaign;
import com.mail.sender.mail_sender.models.Recipient;
import com.mail.sender.mail_sender.models.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTrackingRepository extends JpaRepository<EmailTracking, Long> {
        Optional<EmailTracking> findByRecipientAndCampaignAndSendInstance(Recipient recipient, HistoryCampaign campaign,
                        String sendInstance);

        @Query("SELECT COUNT(e) FROM EmailTracking e WHERE e.campaign = :campaign")
        int countByCampaign(@Param("campaign") HistoryCampaign campaign);

        // ---------------------------------------------------------------
        @Query("SELECT COUNT(e) FROM EmailTracking e WHERE e.campaign = :campaign AND e.clicked = true")
        int countByCampaignAndClickedTrue(@Param("campaign") HistoryCampaign campaign);

        @Query("SELECT COUNT(e) FROM EmailTracking e WHERE e.campaign.user = :user")
        int countByUser(@Param("user") User user);

        @Query("SELECT COUNT(e) FROM EmailTracking e WHERE e.campaign.user = :user AND e.clicked = true")
        int countByUserAndClickedTrue(@Param("user") User user);

        // ------------------------------------------------------------------

        @Query("SELECT DISTINCT r.email FROM EmailTracking et JOIN et.recipient r WHERE et.campaign = :campaign AND et.clicked = false")
        List<String> findNotClickedRecipients(@Param("campaign") HistoryCampaign campaign);

}
