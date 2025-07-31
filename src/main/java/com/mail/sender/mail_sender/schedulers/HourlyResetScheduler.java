package com.mail.sender.mail_sender.schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mail.sender.mail_sender.models.EmailConfiguration;
import com.mail.sender.mail_sender.repositories.EmailConfigurationRepository;

@Component
public class HourlyResetScheduler {

    @Autowired
    private EmailConfigurationRepository configRepository;

    /**
     * This scheduler runs every minute.
     * It checks all configurations and resets the hourlySentCount if an hour has
     * elapsed since the last reset.
     */
    @Scheduled(cron = "0 * * * * *") // Every minute at 0 seconds.
    public void resetHourlyCounts() {
        List<EmailConfiguration> configs = configRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (EmailConfiguration config : configs) {
            // if (Duration.between(config.getLastHourReset(), now).toMinutes() >= 60) {
            // config.setHourlySentCount(0);
            // config.setLastHourReset(now);
            // }

            // If lastHourReset is null, initialize it to now and set the count to 0
            if (config.getLastHourReset() == null) {
                config.setHourlySentCount(0);
                config.setLastHourReset(now);
            } else if (Duration.between(config.getLastHourReset(), now).toMinutes() >= 60) {
                config.setHourlySentCount(0);
                config.setLastHourReset(now);
            }
        }
        configRepository.saveAll(configs);
        System.out.println("Hourly counters reset for configurations as needed.");
    }
}
