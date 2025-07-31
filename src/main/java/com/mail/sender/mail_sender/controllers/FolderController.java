package com.mail.sender.mail_sender.controllers;

import com.mail.sender.mail_sender.models.Folder;
import com.mail.sender.mail_sender.models.RecipientList;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.FolderRepository;
import com.mail.sender.mail_sender.repositories.RecipientListRepository;
import com.mail.sender.mail_sender.repositories.UserRepository;
import com.mail.sender.mail_sender.services.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FolderController {
    private final FolderRepository folderRepository;
    private final RecipientListRepository recipientListRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public FolderController(FolderRepository folderRepository, JwtService jwtService, UserRepository userRepository,RecipientListRepository recipientListRepository) {
        this.folderRepository = folderRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.recipientListRepository = recipientListRepository;
    }

    @PostMapping
    public ResponseEntity<Folder> createFolder(@RequestBody Folder folder, @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        folder.setUser(user); // Link folder to user
        return ResponseEntity.ok(folderRepository.save(folder));
    }

    @GetMapping
    public ResponseEntity<List<Folder>> getFolders(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(folderRepository.findByUser(user));
    }



    // Delete folder API
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId, 
                                             @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        Folder folder = folderRepository.findById(folderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
        
        if (!folder.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        folderRepository.delete(folder);
        return ResponseEntity.ok().build();
    }
    
    // Delete recipient list API
    @DeleteMapping("/{folderId}/recipientList/{recipientListId}")
    public ResponseEntity<Void> deleteRecipientList(@PathVariable Long folderId, 
                                                    @PathVariable Long recipientListId,
                                                    @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        Folder folder = folderRepository.findById(folderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
        
        if (!folder.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        RecipientList recipientList = recipientListRepository.findById(recipientListId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient List not found"));
        
        if (!recipientList.getFolder().getId().equals(folderId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        recipientListRepository.delete(recipientList);
        return ResponseEntity.ok().build();
    }
}
