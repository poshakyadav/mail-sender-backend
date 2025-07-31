package com.mail.sender.mail_sender.services;


import java.util.List;

import com.mail.sender.mail_sender.models.EmailTemplateCategory;

public interface EmailTemplateCategoryService {
    EmailTemplateCategory createCategory(EmailTemplateCategory category);
    List<EmailTemplateCategory> getAllCategories();
}
