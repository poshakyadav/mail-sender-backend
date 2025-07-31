package com.mail.sender.mail_sender.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mail.sender.mail_sender.utils.ResetStrategy;
import com.mail.sender.mail_sender.utils.Role;

@Entity
@Table(name = "user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> folders = new ArrayList<>(); // Added relationship with Folder

    @OneToMany(mappedBy = "user")
    private List<Token> tokens;

    @OneToMany(mappedBy = "user")
    private List<Recipient> recipients; // Add relationship with Recipient

    @OneToMany(mappedBy = "user")
    private List<EmailConfiguration> emailConfigurations; // Add relationship with EmailConfiguration

    @Column(name = "auto_switch_enabled")
    private Boolean autoSwitchEnabled = false;

    @Column(name = "reset_strategy")
    @Enumerated(EnumType.STRING)
    private ResetStrategy resetStrategy = ResetStrategy.LAST_RESET_DATE;

    @Column(name = "timezone")
    private String timezone = "UTC";

    // Getter and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Recipient> recipients) {
        this.recipients = recipients;
    }

    public List<EmailConfiguration> getEmailConfigurations() {
        return emailConfigurations;
    }

    public void setEmailConfigurations(List<EmailConfiguration> emailConfigurations) {
        this.emailConfigurations = emailConfigurations;
    }

    public Boolean isAutoSwitchEnabled() {
        return autoSwitchEnabled;
    }

    public void setAutoSwitchEnabled(Boolean autoSwitchEnabled) {
        this.autoSwitchEnabled = autoSwitchEnabled;
    }

    public ResetStrategy getResetStrategy() {
        return resetStrategy;
    }

    public void setResetStrategy(ResetStrategy resetStrategy) {
        this.resetStrategy = resetStrategy;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    
}
