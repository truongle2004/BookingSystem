package com.bookingsystem.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class ClerkPropertiesTest {

    private static final String ISSUER_URI = "https://example.clerk.accounts.dev";

    private static final String JWK_SET_URI = ISSUER_URI + "/.well-known/jwks.json";

    private static final String AUDIENCE = "booking-api";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void bindsClerkConfigurationProperties() {
        contextRunner
                .withPropertyValues(
                        ClerkProperties.ISSUER_URI_PROPERTY + "=" + ISSUER_URI,
                        ClerkProperties.JWK_SET_URI_PROPERTY + "=" + JWK_SET_URI,
                        ClerkProperties.AUDIENCE_PROPERTY + "=" + AUDIENCE)
                .run(context -> assertThat(context)
                        .hasSingleBean(ClerkProperties.class)
                        .getBean(ClerkProperties.class)
                        .satisfies(properties -> {
                            assertThat(properties.getIssuerUri()).isEqualTo(ISSUER_URI);
                            assertThat(properties.getJwkSetUri()).isEqualTo(JWK_SET_URI);
                            assertThat(properties.getAudience()).isEqualTo(AUDIENCE);
                        }));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ClerkProperties.class)
    static class TestConfiguration {
    }
}
