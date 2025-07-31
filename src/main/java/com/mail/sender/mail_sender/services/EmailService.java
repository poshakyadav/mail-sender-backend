package com.mail.sender.mail_sender.services;


import com.mail.sender.mail_sender.models.HistoryCampaign;
import com.mail.sender.mail_sender.models.MailRequest;
import com.mail.sender.mail_sender.models.MailResponse;
import com.mail.sender.mail_sender.models.User;

public interface EmailService {
   MailResponse sendEmail(MailRequest request, User user);
   MailResponse sendScheduledEmail(MailRequest request, User user, HistoryCampaign campaign);
}
