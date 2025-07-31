package com.mail.sender.mail_sender.dto;

import com.mail.sender.mail_sender.utils.ResetStrategy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsDTO {
    private Boolean autoSwitchEnabled;
    private ResetStrategy resetStrategy;
    private String timezone;

    // Standard getters/setters for other fields
    public Boolean isAutoSwitchEnabled() {
        return autoSwitchEnabled;
    }

    public void setAutoSwitchEnabled(Boolean autoSwitchEnabled) {
        this.autoSwitchEnabled = autoSwitchEnabled;
    }

    public ResetStrategy getResetStrategy() {
        return resetStrategy;
    }

    public void setResetStrategy(ResetStrategy resetStrategy) {
        this.resetStrategy = resetStrategy;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    // Getters and Setters
    public boolean isUtcResetEnabled() {
        return resetStrategy == ResetStrategy.UTC_MIDNIGHT;
    }

    public void setUtcResetEnabled(boolean utcResetEnabled) {
        this.resetStrategy = utcResetEnabled ? ResetStrategy.UTC_MIDNIGHT : ResetStrategy.LAST_RESET_DATE;
    }

}
