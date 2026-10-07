package com.bookingsystem.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class ClerkPropertiesTest {

    private static final String ISSUER_URI = "https://example.clerk.accounts.dev";

    private static final String JWK_SET_URI = ISSUER_URI + "/.well-known/jwks.json";

    private static final String AUDIENCE = "booking-api";

    private static final String FIRST_AUTHORIZED_PARTY = "http://localhost:3000";

    private static final String SECOND_AUTHORIZED_PARTY = "https://booking.example.com";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void bindsClerkConfigurationProperties() {
        contextRunner
                .withPropertyValues(
                        ClerkProperties.ISSUER_URI_PROPERTY + "=" + ISSUER_URI,
                        ClerkProperties.JWK_SET_URI_PROPERTY + "=" + JWK_SET_URI,
                        ClerkProperties.AUDIENCE_PROPERTY + "=" + AUDIENCE,
                        ClerkProperties.CLOCK_SKEW_PROPERTY + "=45s",
                        ClerkProperties.JWK_CONNECT_TIMEOUT_PROPERTY + "=2s",
                        ClerkProperties.JWK_READ_TIMEOUT_PROPERTY + "=4s",
                        ClerkProperties.AUTHORIZED_PARTIES_PROPERTY + "="
                                + FIRST_AUTHORIZED_PARTY + "," + SECOND_AUTHORIZED_PARTY)
                .run(context -> assertThat(context)
                        .hasSingleBean(ClerkProperties.class)
                        .getBean(ClerkProperties.class)
                        .satisfies(properties -> {
                            assertThat(properties.getIssuerUri()).isEqualTo(ISSUER_URI);
                            assertThat(properties.getJwkSetUri()).isEqualTo(JWK_SET_URI);
                            assertThat(properties.getAudience()).isEqualTo(AUDIENCE);
                            assertThat(properties.getAuthorizedParties())
                                    .isEqualTo(List.of(FIRST_AUTHORIZED_PARTY, SECOND_AUTHORIZED_PARTY));
                            assertThat(properties.getClockSkew()).isEqualTo(java.time.Duration.ofSeconds(45));
                            assertThat(properties.getJwkConnectTimeout()).isEqualTo(java.time.Duration.ofSeconds(2));
                            assertThat(properties.getJwkReadTimeout()).isEqualTo(java.time.Duration.ofSeconds(4));
                            assertThat(properties.isConfigured()).isTrue();
                        }));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ClerkProperties.class)
    static class TestConfiguration {
    }
}
