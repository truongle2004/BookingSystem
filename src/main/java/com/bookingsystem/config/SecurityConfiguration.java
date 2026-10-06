package com.bookingsystem.config;

import com.bookingsystem.platform.web.ApiErrorWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/** Configures HTTP access rules for the application and its API documentation. */
@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

    private static final String CLERK_JWKS_RELATIVE_PATH = ".well-known/jwks.json";

    /**
     * Allows public access to the API documentation while protecting application endpoints.
     *
     * @param http HTTP security configuration
     * @param clerkProperties Clerk JWT verification settings
     * @return configured security filter chain
     * @throws Exception if Spring Security cannot build the filter chain
     */
    @Bean
    @SuppressWarnings({
        "java:S112", // HttpSecurity.build() declares the generic checked Exception.
        "java:S4502" // Safe because authentication only accepts stateless Authorization header bearer tokens.
    })
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http,
            final ClerkProperties clerkProperties,
            final ApiErrorWriter errorWriter) throws Exception {
        http
                // This resource server does not authenticate with automatically submitted browser cookies.
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> errorWriter.write(
                                request, response, 401, "UNAUTHENTICATED", "Authentication is required."))
                        .accessDeniedHandler((request, response, exception) -> errorWriter.write(
                                request, response, 403, "FORBIDDEN", "Access is forbidden.")))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/error", "/health/**", "/openapi.yaml",
                                "/scalar", "/scalar/**", "/v3/api-docs", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/v1/**").authenticated()
                        .anyRequest().permitAll());

        if (clerkProperties.isConfigured()) {
            http.oauth2ResourceServer(resourceServer -> resourceServer
                    .authenticationEntryPoint((request, response, exception) -> errorWriter.write(
                            request, response, 401, "UNAUTHENTICATED", "Authentication is required."))
                    .accessDeniedHandler((request, response, exception) -> errorWriter.write(
                            request, response, 403, "FORBIDDEN", "Access is forbidden."))
                    .jwt(jwt -> jwt.decoder(clerkJwtDecoder(clerkProperties))));
        }

        return http.build();
    }

    private JwtDecoder clerkJwtDecoder(final ClerkProperties clerkProperties) {
        String issuerUri = clerkProperties.getIssuerUri();
        String jwkSetUri = clerkProperties.getJwkSetUri();
        if (!StringUtils.hasText(jwkSetUri)) {
            jwkSetUri = issuerUri.endsWith("/")
                    ? issuerUri + CLERK_JWKS_RELATIVE_PATH
                    : issuerUri + "/" + CLERK_JWKS_RELATIVE_PATH;
        }

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        JwtTimestampValidator timestampValidator = new JwtTimestampValidator(clerkProperties.getClockSkew());
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator(issuerUri);
        OAuth2TokenValidator<Jwt> clerkClaimsValidator = new ClerkJwtClaimsValidator(
                clerkProperties.getAudience(),
                clerkProperties.getAuthorizedParties());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                timestampValidator, issuerValidator, clerkClaimsValidator));
        return decoder;
    }
}
