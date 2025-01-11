package com.mail.sender.mail_sender.models;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MailRequest {

    private List<String> recipients;
    private String subject;
    private String body;
    private boolean isHtml;
    private List<MultipartFile> attachments;

}
