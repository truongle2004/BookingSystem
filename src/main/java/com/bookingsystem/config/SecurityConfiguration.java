package com.bookingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Configures HTTP access rules for the application and its API documentation. */
@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

    /**
     * Allows public access to the API documentation while protecting application endpoints.
     *
     * @param http HTTP security configuration
     * @return configured security filter chain
     * @throws Exception if Spring Security cannot build the filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/scalar", "/scalar/**", "/v3/api-docs", "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}
