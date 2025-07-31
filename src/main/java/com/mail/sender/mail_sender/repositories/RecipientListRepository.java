package com.mail.sender.mail_sender.repositories;

import com.mail.sender.mail_sender.models.RecipientList;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RecipientListRepository extends JpaRepository<RecipientList, Long> {

}