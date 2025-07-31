package com.mail.sender.mail_sender.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.utils.ResetStrategy;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); 
    
    List<User> findByResetStrategy(ResetStrategy resetStrategy);
}

