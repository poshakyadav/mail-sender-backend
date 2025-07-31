package com.mail.sender.mail_sender.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mail.sender.mail_sender.models.EmailTemplateCategory;

public interface EmailTemplateCategoryRepository extends JpaRepository<EmailTemplateCategory, Long> {

}
