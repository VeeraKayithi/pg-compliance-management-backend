package com.pgcompliance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // Disables CSRF for REST APIs
        .authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated() // Requires Basic Auth for requests
        )
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }
}