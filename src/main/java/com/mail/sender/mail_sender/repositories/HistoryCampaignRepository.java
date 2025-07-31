package com.mail.sender.mail_sender.repositories;

import com.mail.sender.mail_sender.models.HistoryCampaign;
import com.mail.sender.mail_sender.models.RecipientList;
import com.mail.sender.mail_sender.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoryCampaignRepository extends JpaRepository<HistoryCampaign, Long> {
        List<HistoryCampaign> findByUser(User user);

        @Query("SELECT hc FROM HistoryCampaign hc " +
                        "WHERE hc.status = :status " +
                        "AND hc.triggerDelay <= :currentTime")
        List<HistoryCampaign> findByStatusAndTriggerDelayBefore(
                        @Param("status") String status,
                        @Param("currentTime") LocalDateTime currentTime);

        // Add this method to find child campaigns by parent
        List<HistoryCampaign> findByParentCampaign(HistoryCampaign parentCampaign);

        List<HistoryCampaign> findByRecipientList(RecipientList recipientList);

}
