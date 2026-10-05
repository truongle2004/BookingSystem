package com.bookingsystem.config;

import java.util.List;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

/** Validates Clerk-specific JWT claims that are not covered by the standard JWT validators. */
final class ClerkJwtClaimsValidator implements OAuth2TokenValidator<Jwt> {

    private static final String INVALID_TOKEN = "invalid_token";

    private final String audience;

    private final List<String> authorizedParties;

    ClerkJwtClaimsValidator(final String audience, final List<String> authorizedParties) {
        this.audience = audience;
        this.authorizedParties = List.copyOf(authorizedParties);
    }

    @Override
    public OAuth2TokenValidatorResult validate(final Jwt jwt) {
        if (StringUtils.hasText(audience) && !jwt.getAudience().contains(audience)) {
            return failure("The token audience is not accepted");
        }

        String authorizedParty = jwt.getClaimAsString("azp");
        if (StringUtils.hasText(authorizedParty)
                && !authorizedParties.isEmpty()
                && !authorizedParties.contains(authorizedParty)) {
            return failure("The token authorized party is not accepted");
        }

        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult failure(final String description) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(INVALID_TOKEN, description, null));
    }
}
