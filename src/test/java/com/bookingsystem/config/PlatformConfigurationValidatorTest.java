package com.bookingsystem.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

@DisplayName("Platform configuration validation")
class PlatformConfigurationValidatorTest {

    @Test
    @DisplayName("rejects non-positive Clerk HTTP timeouts")
    void afterPropertiesSetShouldRejectNonPositiveClerkTimeout() {
        MockEnvironment environment = validEnvironment();
        ClerkProperties properties = validClerkProperties();
        properties.setJwkReadTimeout(Duration.ZERO);

        PlatformConfigurationValidator validator = new PlatformConfigurationValidator(environment, properties);

        assertThatThrownBy(validator::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLERK_JWK_READ_TIMEOUT must be greater than zero");
    }

    @Test
    @DisplayName("rejects an invalid explicit Clerk JWKS URI")
    void afterPropertiesSetShouldRejectInvalidJwkSetUri() {
        MockEnvironment environment = validEnvironment();
        ClerkProperties properties = validClerkProperties();
        properties.setJwkSetUri("not-a-uri");

        PlatformConfigurationValidator validator = new PlatformConfigurationValidator(environment, properties);

        assertThatThrownBy(validator::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLERK_JWK_SET_URI must be a valid absolute URI");
    }

    private MockEnvironment validEnvironment() {
        return new MockEnvironment()
                .withProperty("app.environment", "test")
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/bookingsystem")
                .withProperty("spring.datasource.username", "bookingsystem")
                .withProperty("spring.datasource.password", "bookingsystem");
    }

    private ClerkProperties validClerkProperties() {
        ClerkProperties properties = new ClerkProperties();
        properties.setIssuerUri("https://example.clerk.accounts.dev");
        return properties;
    }
}
