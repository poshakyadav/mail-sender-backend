package com.mail.sender.mail_sender.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mail.sender.mail_sender.models.EmailTemplateCategory;
import com.mail.sender.mail_sender.repositories.EmailTemplateCategoryRepository;

@Service
public class EmailTemplateCategoryServiceImpl implements EmailTemplateCategoryService {

    private final EmailTemplateCategoryRepository categoryRepository;


    public EmailTemplateCategoryServiceImpl(EmailTemplateCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public EmailTemplateCategory createCategory(EmailTemplateCategory category) {
        return categoryRepository.save(category);
    }

    @Override
    public List<EmailTemplateCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

}
