package com.securebank.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securebank.dto.ApiResponse;
import com.securebank.repository.SecurityActivityRepository;
import com.securebank.security.TokenAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final SecurityActivityRepository securityActivityRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String allowedOrigins;

    public SecurityConfig(TokenAuthenticationFilter tokenAuthenticationFilter,
                          SecurityActivityRepository securityActivityRepository) {
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
        this.securityActivityRepository = securityActivityRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // High security BCrypt hashing with strength 12
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            // Phase 8: Standard HTTP Security Headers
            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/forgot-password/**",
                    "/api/auth/reset-password",
                    "/error"
                ).permitAll()
                .requestMatchers("/api/auth/change-password").authenticated()
                .requestMatchers("/api/review/**").hasRole("REVIEWER")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    ApiResponse apiResponse = new ApiResponse(false, "Authentication required. Please log in.");
                    objectMapper.writeValue(response.getOutputStream(), apiResponse);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // Phase 8: Audit Log ACCESS_DENIED with user ID, URI, and role
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    String principal = (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";
                    String uri = request.getRequestURI();
                    securityActivityRepository.insertActivity(principal, "ACCESS_DENIED", "Unauthorized attempt to access resource: " + uri, request.getRemoteAddr());

                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    ApiResponse apiResponse = new ApiResponse(false, "Access denied. Insufficient permissions.");
                    objectMapper.writeValue(response.getOutputStream(), apiResponse);
                })
            )
            .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
