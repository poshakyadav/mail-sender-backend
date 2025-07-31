package com.mail.sender.mail_sender.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DateCount {
    private LocalDate date;
    private int count;
    
    // constructor, getters, setters
}
