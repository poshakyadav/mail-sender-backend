package com.mail.sender.mail_sender.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceDistributionDTO {
    private String deviceType;
    private long count;
    
    public DeviceDistributionDTO(String deviceType, long count) {
        this.deviceType = deviceType;
        this.count = count;
    }
    // Getters/setters
}
