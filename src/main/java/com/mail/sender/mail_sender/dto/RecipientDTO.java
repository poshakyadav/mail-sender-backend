package com.mail.sender.mail_sender.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecipientDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String company;
    private String position;
    
    // getters & setters
}