package com.mail.sender.mail_sender.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LinkClickCount {
    private String originalUrl;
    private int clicks;
    
    // constructor, getters, setters
}