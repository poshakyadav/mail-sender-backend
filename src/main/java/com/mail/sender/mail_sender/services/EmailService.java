package com.mail.sender.mail_sender.services;

import com.mail.sender.mail_sender.models.MailRequest;
import com.mail.sender.mail_sender.models.MailResponse;

public interface EmailService {
    MailResponse sendEmail(MailRequest request);
}
