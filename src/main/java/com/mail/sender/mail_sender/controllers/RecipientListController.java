package com.mail.sender.mail_sender.controllers;

import com.mail.sender.mail_sender.models.Folder;
import com.mail.sender.mail_sender.models.Recipient;
import com.mail.sender.mail_sender.models.RecipientList;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.FolderRepository;
import com.mail.sender.mail_sender.repositories.RecipientListRepository;
import com.mail.sender.mail_sender.repositories.RecipientRepository;
import com.mail.sender.mail_sender.repositories.UserRepository;
import com.mail.sender.mail_sender.services.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
public class RecipientListController {
        private final RecipientListRepository recipientListRepository;
        private final FolderRepository folderRepository;
        private final JwtService jwtService;
        private final UserRepository userRepository;
        private final RecipientRepository recipientRepository;

        public RecipientListController(
                        RecipientListRepository recipientListRepository,
                        FolderRepository folderRepository,
                        JwtService jwtService,
                        UserRepository userRepository,
                        RecipientRepository recipientRepository) {
                this.recipientListRepository = recipientListRepository;
                this.folderRepository = folderRepository;
                this.jwtService = jwtService;
                this.userRepository = userRepository;
                this.recipientRepository = recipientRepository;
        }

        @PostMapping("/{folderId}")
        public ResponseEntity<RecipientList> createRecipientList(
                        @PathVariable Long folderId,
                        @RequestBody RecipientList recipientList,
                        @RequestHeader("Authorization") String token) {

                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                Folder folder = folderRepository.findById(folderId)
                                .filter(f -> f.getUser().equals(user)) // Ensure folder belongs to the user
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Folder not found or does not belong to the user"));

                recipientList.setFolder(folder); // Link recipient list to folder
                return ResponseEntity.ok(recipientListRepository.save(recipientList));
        }

        @GetMapping("/{folderId}")
        public ResponseEntity<List<RecipientList>> getRecipientListsByFolderId(
                        @PathVariable Long folderId,
                        @RequestHeader("Authorization") String token) {

                // Extract the JWT token and username
                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                // Fetch user from username
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                // Fetch the folder by ID and ensure it belongs to the user
                Folder folder = folderRepository.findById(folderId)
                                .filter(f -> f.getUser().equals(user)) // Ensure folder belongs to the user
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Folder not found or does not belong to the user"));

                // Return all the recipient lists in the folder
                return ResponseEntity.ok(folder.getRecipientLists());
        }

        @PostMapping("/{listId}/recipients")
        public ResponseEntity<RecipientList> addRecipientsToList(
                        @PathVariable Long listId,
                        @RequestBody List<Recipient> recipients,
                        @RequestHeader("Authorization") String token) {

                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                RecipientList recipientList = recipientListRepository.findById(listId)
                                .filter(list -> list.getFolder().getUser().equals(user))
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "List not found or does not belong to the user"));

                for (Recipient recipient : recipients) {
                        if (recipientRepository.findByEmailAndRecipientList(recipient.getEmail(), recipientList)
                                        .isPresent()) {
                                continue; // Skip adding duplicate within the same list
                        }
                        recipient.setUser(user);
                        recipientList.addRecipient(recipient); // Use the helper method
                }

                return ResponseEntity.ok(recipientListRepository.save(recipientList));
        }

        @GetMapping("/{listId}/recipients")
        public ResponseEntity<List<Recipient>> getRecipientsByListId(
                        @PathVariable Long listId,
                        @RequestHeader("Authorization") String token) {

                String jwt = token.substring(7);
                String username = jwtService.extractUsername(jwt);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                RecipientList recipientList = recipientListRepository.findById(listId)
                                .filter(list -> list.getFolder().getUser().equals(user))
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "List not found or does not belong to the user"));

                List<Recipient> recipients = recipientRepository.findByRecipientListId(listId);
                return ResponseEntity.ok(recipients);
        }

}
