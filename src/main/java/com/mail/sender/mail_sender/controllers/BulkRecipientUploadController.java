package com.mail.sender.mail_sender.controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.UserRepository;
import com.mail.sender.mail_sender.services.JwtService;

@RestController
@RequestMapping("/api/bulk")
public class BulkRecipientUploadController {

        @Autowired
        private JobLauncher jobLauncher;

        @Autowired
        private Job importRecipientJob;

        // @Autowired
        // private RecipientItemProcessor processor;

        @Autowired // Add this
        private JwtService jwtService;

        @Autowired // Add this
        private UserRepository userRepository;

        @PostMapping("/upload/{listId}")
        public ResponseEntity<String> handleFileUpload(@PathVariable Long listId,
                        @RequestParam("file") MultipartFile file,
                        @RequestHeader("Authorization") String token) throws Exception {

                String username = jwtService.extractUsername(token.substring(7));
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                // Save the uploaded file to a temporary location
                Path tempDir = Files.createTempDirectory("bulk_upload");
                Path tempFile = tempDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));
                Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
                String filePath = tempFile.toAbsolutePath().toString();

                // Configure job parameters
                JobParameters jobParameters = new JobParametersBuilder()
                                .addLong("time", System.currentTimeMillis())
                                .addLong("listId", listId)
                                .addLong("userId", user.getId())
                                .addString("filePath", filePath)
                                .toJobParameters();

                try {
                        // Launch the job and wait for completion
                        JobExecution jobExecution = jobLauncher.run(importRecipientJob, jobParameters);
                        if (jobExecution.getStatus() == BatchStatus.FAILED) {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body("Bulk upload failed");
                        }
                        return ResponseEntity.ok("Bulk upload completed successfully");
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Bulk upload failed: " + e.getMessage());
                }
        }

}
