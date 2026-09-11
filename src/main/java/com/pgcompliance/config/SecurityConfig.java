package com.pgcompliance.config;

import com.pgcompliance.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                /*
                 * Uses the CorsConfigurationSource bean
                 * already defined in CorsConfig.java.
                 */
                .cors(cors -> {
                })

                /*
                 * CSRF is disabled because this application
                 * uses stateless JWT Bearer authentication.
                 */
                .csrf(csrf ->
                        csrf.disable()
                )

                /*
                 * Do not create or use server-side sessions.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Configure endpoint authorization.
                 *
                 * Specific endpoint rules must appear before
                 * broader wildcard endpoint rules.
                 */
                .authorizeHttpRequests(auth -> auth

                        /*
                         * Allow CORS preflight requests.
                         */
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        /*
                         * Public login endpoint.
                         */
                        .requestMatchers(
                                "/api/v1/auth/login"
                        )
                        .permitAll()

                        /*
                         * Public Tenant account activation
                         * endpoints.
                         *
                         * These must remain public because the
                         * Tenant is inactive before activation.
                         */
                        .requestMatchers(
                                "/api/v1/auth/activation/validate",
                                "/api/v1/auth/activation/complete",
                                "/api/v1/auth/activation/resend"
                        )
                        .permitAll()

                        /*
                         * Public health-check endpoint.
                         *
                         * Other actuator endpoints remain
                         * protected.
                         */
                        .requestMatchers(
                                "/actuator/health"
                        )
                        .permitAll()

                        /*
                         * Tenant self-profile endpoint.
                         *
                         * This rule must appear before the
                         * broader /api/v1/tenants/** rule.
                         */
                        .requestMatchers(
                                "/api/v1/tenants/me"
                        )
                        .hasRole("TENANT")

                        /*
                         * Admin and Tenant users can access
                         * their respective notifications.
                         *
                         * Notification ownership continues
                         * to be validated in the service.
                         */
                        .requestMatchers(
                                "/api/v1/notifications/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "TENANT"
                        )

                        /*
                         * Admin Communication Center.
                         */
                        .requestMatchers(
                                "/api/v1/admin/announcements/**"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Only an Admin can create a portal
                         * account for a Tenant.
                         */
                        .requestMatchers(
                                "/api/v1/users/tenant-account"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Building management.
                         */
                        .requestMatchers(
                                "/api/v1/buildings/**"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Room management.
                         */
                        .requestMatchers(
                                "/api/v1/rooms/**"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Tenant management.
                         *
                         * /api/v1/tenants/me was already
                         * handled above as Tenant-only.
                         */
                        .requestMatchers(
                                "/api/v1/tenants/**"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Remaining User-management APIs.
                         */
                        .requestMatchers(
                                "/api/v1/users/**"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Require authentication for any
                         * endpoint not explicitly listed.
                         */
                        .anyRequest()
                        .authenticated()
                )

                /*
                 * Run the JWT authentication filter before
                 * Spring Security's standard authentication
                 * filter.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}