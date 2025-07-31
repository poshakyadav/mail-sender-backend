package com.mail.sender.mail_sender.repositories;

import com.mail.sender.mail_sender.models.ClickEvent;
import com.mail.sender.mail_sender.models.HistoryCampaign;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    @Query("SELECT c.userAgent, COUNT(c) " +
            "FROM ClickEvent c " +
            "WHERE c.trackedLink.campaign = :campaign " +
            "GROUP BY c.userAgent")
    List<Object[]> findDevicesByCampaign(@Param("campaign") HistoryCampaign campaign);

    @Query("SELECT DATE(ce.timestamp) as date, COUNT(ce) as count " +
           "FROM ClickEvent ce " +
           "WHERE ce.trackedLink.campaign = :campaign " +
           "GROUP BY DATE(ce.timestamp) " +
           "ORDER BY DATE(ce.timestamp)")
    List<Object[]> findClicksOverTime(@Param("campaign") HistoryCampaign campaign);
}
