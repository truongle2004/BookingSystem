package com.bookingsystem.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

final class ClerkPropertiesTestValues {

	static final String ISSUER_URI = "https://example.clerk.accounts.dev";

	static final String JWK_SET_URI = ISSUER_URI + "/.well-known/jwks.json";

	static final String AUDIENCE = "booking-api";

	private ClerkPropertiesTestValues() {
	}
}

@SpringBootTest(
		classes = ClerkPropertiesTest.TestConfiguration.class,
		properties = {
			ClerkProperties.ISSUER_URI_PROPERTY + "=" + ClerkPropertiesTestValues.ISSUER_URI,
			ClerkProperties.JWK_SET_URI_PROPERTY + "=" + ClerkPropertiesTestValues.JWK_SET_URI,
			ClerkProperties.AUDIENCE_PROPERTY + "=" + ClerkPropertiesTestValues.AUDIENCE
		})
class ClerkPropertiesTest {

	@org.springframework.beans.factory.annotation.Autowired
	private ClerkProperties clerkProperties;

	@Test
	void bindsClerkConfigurationProperties() {
		assertThat(clerkProperties.getIssuerUri()).isEqualTo(ClerkPropertiesTestValues.ISSUER_URI);
		assertThat(clerkProperties.getJwkSetUri()).isEqualTo(ClerkPropertiesTestValues.JWK_SET_URI);
		assertThat(clerkProperties.getAudience()).isEqualTo(ClerkPropertiesTestValues.AUDIENCE);
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(ClerkProperties.class)
	static class TestConfiguration {
	}
}
