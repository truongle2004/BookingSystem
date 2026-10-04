package com.bookingsystem.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

@DisplayName("Clerk JWT claims validator")
class ClerkJwtClaimsValidatorTest {

    private static final String AUDIENCE = "booking-api";

    private static final String AUTHORIZED_PARTY = "https://booking.example.com";

    private static final Instant ISSUED_AT = Instant.parse("2026-10-04T00:00:00Z");

    private static final Instant EXPIRES_AT = Instant.parse("2026-10-04T01:00:00Z");

    @Test
    @DisplayName("accepts matching audience and authorized party")
    void validateShouldSucceedWhenClaimsMatch() {
        ClerkJwtClaimsValidator validator = new ClerkJwtClaimsValidator(AUDIENCE, List.of(AUTHORIZED_PARTY));

        OAuth2TokenValidatorResult result = validator.validate(jwt(List.of(AUDIENCE), AUTHORIZED_PARTY));

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("rejects an unexpected audience")
    void validateShouldFailWhenAudienceDoesNotMatch() {
        ClerkJwtClaimsValidator validator = new ClerkJwtClaimsValidator(AUDIENCE, List.of(AUTHORIZED_PARTY));

        OAuth2TokenValidatorResult result = validator.validate(jwt(List.of("another-api"), AUTHORIZED_PARTY));

        assertThat(result.getErrors())
                .singleElement()
                .satisfies(error -> assertThat(error.getDescription()).contains("audience"));
    }

    @Test
    @DisplayName("rejects an unexpected authorized party")
    void validateShouldFailWhenAuthorizedPartyDoesNotMatch() {
        ClerkJwtClaimsValidator validator = new ClerkJwtClaimsValidator(AUDIENCE, List.of(AUTHORIZED_PARTY));

        OAuth2TokenValidatorResult result = validator.validate(jwt(List.of(AUDIENCE), "https://attacker.example.com"));

        assertThat(result.getErrors())
                .singleElement()
                .satisfies(error -> assertThat(error.getDescription()).contains("authorized party"));
    }

    @Test
    @DisplayName("allows an absent authorized party as documented by Clerk")
    void validateShouldSucceedWhenAuthorizedPartyIsAbsent() {
        ClerkJwtClaimsValidator validator = new ClerkJwtClaimsValidator(AUDIENCE, List.of(AUTHORIZED_PARTY));

        OAuth2TokenValidatorResult result = validator.validate(jwt(List.of(AUDIENCE), null));

        assertThat(result.hasErrors()).isFalse();
    }

    private Jwt jwt(final List<String> audiences, final String authorizedParty) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuedAt(ISSUED_AT)
                .expiresAt(EXPIRES_AT)
                .subject("user_123")
                .audience(audiences);
        if (authorizedParty != null) {
            builder.claim("azp", authorizedParty);
        }
        return builder.build();
    }
}
