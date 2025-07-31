package com.mail.sender.mail_sender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling 
@SpringBootApplication
public class MailSenderApplication {

	public static void main(String[] args) {
		SpringApplication.run(MailSenderApplication.class, args);
	}

}
