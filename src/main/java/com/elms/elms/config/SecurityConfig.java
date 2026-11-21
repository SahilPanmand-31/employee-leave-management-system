package com.elms.elms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.elms.elms.security.JwtFilter;

// All security rules in one place:
//  - /auth/** and Swagger are public
//  - only MANAGER can create/update/delete employees and approve/reject leave
//  - every other endpoint needs a valid JWT (EMPLOYEE or MANAGER)
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // REST API with tokens, no browser session/cookies -> CSRF not needed
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // MANAGER only
                        .requestMatchers(HttpMethod.POST, "/employees").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/employees/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/employees/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/leaves/pending").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/leaves/approve/**", "/leaves/reject/**").hasRole("MANAGER")

                        // Any logged-in user (EMPLOYEE or MANAGER)
                        .anyRequest().authenticated())

                // No token or bad token -> 401 (a valid token with the wrong role gets 403)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // Check the JWT before Spring's username/password filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt hashes passwords with a random salt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
