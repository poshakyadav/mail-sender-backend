package com.mail.sender.mail_sender.config;

import java.util.regex.Pattern;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.Nullable;

import com.mail.sender.mail_sender.dto.RecipientDTO;
import com.mail.sender.mail_sender.models.Recipient;
import com.mail.sender.mail_sender.models.RecipientList;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.RecipientListRepository;
import com.mail.sender.mail_sender.repositories.RecipientRepository;
import com.mail.sender.mail_sender.repositories.UserRepository;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@StepScope
public class RecipientItemProcessor implements ItemProcessor<RecipientDTO, Recipient> {

    @Autowired
    private RecipientListRepository recipientListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Value("#{jobParameters['listId']}")
    private Long listId;

    @Value("#{jobParameters['userId']}")
    private Long userId;

    @Override
    @Nullable
    public Recipient process(@NonNull RecipientDTO dto) throws Exception {

        // Validate email format
        if (!isValidEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Invalid email format: " + dto.getEmail());
        }

        // Check for duplicates within the list
        if (recipientRepository.existsByEmailAndRecipientListId(dto.getEmail(), listId)) {
            throw new DuplicateKeyException("Duplicate email in list: " + dto.getEmail());
        }

        Recipient recipient = new Recipient();
        recipient.setEmail(dto.getEmail());
        recipient.setFirstName(dto.getFirstName());
        recipient.setLastName(dto.getLastName());
        recipient.setPhoneNumber(dto.getPhoneNumber());
        recipient.setCompany(dto.getCompany());
        recipient.setPosition(dto.getPosition());
        
        RecipientList list = recipientListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        recipient.setRecipientList(list);
        recipient.setUser(user);

        return recipient;
    }

    private boolean isValidEmail(String email) {
        return Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
                .matcher(email)
                .matches();
    }

}
