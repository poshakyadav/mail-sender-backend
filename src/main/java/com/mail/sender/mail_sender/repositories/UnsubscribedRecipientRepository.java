package com.mail.sender.mail_sender.repositories;

import com.mail.sender.mail_sender.models.Recipient;
import com.mail.sender.mail_sender.models.UnsubscribedRecipient;
import com.mail.sender.mail_sender.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnsubscribedRecipientRepository extends JpaRepository<UnsubscribedRecipient, Long> {

    boolean existsByUserAndRecipient(User user, Recipient recipient);

    void deleteByUserAndRecipient(User user, Recipient recipient);
}