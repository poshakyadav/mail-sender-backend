package com.mail.sender.mail_sender.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import com.mail.sender.mail_sender.filter.JwtAuthenticationFilter;
import com.mail.sender.mail_sender.services.UserDetailsServiceImp;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final UserDetailsServiceImp userDetailsServiceImp;

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        private final CustomLogoutHandler logoutHandler;

        public SecurityConfig(UserDetailsServiceImp userDetailsServiceImp,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        CustomLogoutHandler logoutHandler) {
                this.userDetailsServiceImp = userDetailsServiceImp;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.logoutHandler = logoutHandler;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())
                                .authorizeHttpRequests(
                                                req -> req.requestMatchers("/login/**", "/register/**",
                                                                "/refresh_token/**", "/api/check-username",
                                                                "/api/mail/unsubscribe",
                                                                "/api/mail/confirm-unsubscribe",
                                                                "/api/mail/track/click", "/api/mail/track/open", "/api/mail/track/**")
                                                                .permitAll()
                                                                // .requestMatchers("/admin_only/**").hasAuthority("ADMIN")
                                                                .anyRequest()
                                                                .authenticated())
                                .userDetailsService(userDetailsServiceImp)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .exceptionHandling(
                                                e -> e.accessDeniedHandler(
                                                                (request, response, accessDeniedException) -> response
                                                                                .setStatus(403))
                                                                .authenticationEntryPoint(new HttpStatusEntryPoint(
                                                                                HttpStatus.UNAUTHORIZED)))
                                .logout(l -> l
                                                .logoutUrl("/logout")
                                                .addLogoutHandler(logoutHandler)
                                                .logoutSuccessHandler((request, response,
                                                                authentication) -> SecurityContextHolder
                                                                                .clearContext()))
                                .build();

        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }

        @Bean
        public MultipartResolver multipartResolver() {
                return new StandardServletMultipartResolver();
        }
}
