package com.mail.sender.mail_sender.repositories;

import java.util.List;
import java.util.Optional;

import com.mail.sender.mail_sender.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mail.sender.mail_sender.models.Recipient;
import com.mail.sender.mail_sender.models.RecipientList;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    Optional<Recipient> findByEmail(String email);

    List<Recipient> findByUser(User user); // Fetch all recipients for a specific user

    Optional<Recipient> findByEmailAndUser(String email, User user);

    @Query("SELECT r FROM Recipient r WHERE r.recipientList.id = :listId")
    List<Recipient> findByRecipientListId(@Param("listId") Long listId);


    // Check if a recipient email exists within a specific recipient list
    Optional<Recipient> findByEmailAndRecipientList(String email, RecipientList recipientList);

    // Check if a recipient email exists within a specific recipient list by listId
    boolean existsByEmailAndRecipientListId(String email, Long listId);

}
