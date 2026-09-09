package com.pgcompliance.config;

import com.pgcompliance.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http
                                /*
                                 * Uses your existing CorsConfigurationSource bean.
                                 */
                                .cors(cors -> {
                                })

                                /*
                                 * The backend is a stateless REST API using JWT.
                                 */
                                .csrf(csrf -> csrf.disable())

                                /*
                                 * Spring Security will not create or use
                                 * server-side HTTP sessions.
                                 */
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth

                                                /*
                                                 * Public authentication endpoints.
                                                 *
                                                 * Login must remain public because the user
                                                 * does not have a JWT before signing in.
                                                 *
                                                 * Activation endpoints must remain public
                                                 * because an inactive Tenant cannot log in yet.
                                                 */
                                                .requestMatchers(
                                                                "/api/v1/auth/login",
                                                                "/api/v1/auth/activation/validate",
                                                                "/api/v1/auth/activation/complete",
                                                                "/api/v1/auth/activation/resend")
                                                .permitAll()

                                                /*
                                                 * Keep registration public only during
                                                 * development if you still need it.
                                                 *
                                                 * Before production, change this endpoint
                                                 * to Admin-only access.
                                                 */
                                                .requestMatchers(
                                                                "/api/v1/auth/register")
                                                .permitAll()

                                                /*
                                                 * Tenant self-profile.
                                                 *
                                                 * A Tenant can view only the profile linked
                                                 * to the authenticated User account.
                                                 */
                                                .requestMatchers(
                                                                "/api/v1/tenants/me")
                                                .hasRole("TENANT")

                                                /*
                                                 * Notification Center APIs.
                                                 *
                                                 * Both roles may access notifications, but the
                                                 * Notification Service returns only records
                                                 * belonging to the authenticated user.
                                                 */
                                                .requestMatchers(
                                                                "/api/v1/notifications/**")
                                                .hasAnyRole(
                                                                "ADMIN",
                                                                "TENANT")

                                                /*
                                                 * Only an Admin can create a linked
                                                 * portal account for an existing Tenant.
                                                 */
                                                .requestMatchers(
                                                                "/api/v1/users/tenant-account")
                                                .hasRole("ADMIN")

                                                /*
                                                 * Admin-only management APIs.
                                                 *
                                                 * The /api/v1/tenants/me rule must appear
                                                 * before /api/v1/tenants/**.
                                                 */
                                                .requestMatchers(
                                                                "/api/v1/buildings/**",
                                                                "/api/v1/rooms/**",
                                                                "/api/v1/tenants/**")
                                                .hasRole("ADMIN")

                                                /*
                                                 * Any endpoint not listed above still
                                                 * requires a valid authenticated user.
                                                 */
                                                .anyRequest()
                                                .authenticated())

                                /*
                                 * Run the custom JWT filter before Spring's
                                 * username/password authentication filter.
                                 */
                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}