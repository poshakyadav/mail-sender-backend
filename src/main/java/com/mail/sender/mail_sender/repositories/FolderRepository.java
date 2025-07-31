package com.mail.sender.mail_sender.repositories;

import com.mail.sender.mail_sender.models.Folder;
import com.mail.sender.mail_sender.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByUser(User user);
}