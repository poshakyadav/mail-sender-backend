package com.mail.sender.mail_sender.models;


import com.mail.sender.mail_sender.utils.TemplateType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "email_template")
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    // @Column(name = "category", nullable = false)
    // private String category; // This could be a predefined category, e.g.,
    // 'welcome', 'newsletter', etc.

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private EmailTemplateCategory category;

    @Column(name = "html_content", columnDefinition = "TEXT", nullable = false)
    private String htmlContent; // The HTML content of the template

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // User who created the template

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType type; // Type can be 'default' or 'user-created'

    // Getters and setters
}
