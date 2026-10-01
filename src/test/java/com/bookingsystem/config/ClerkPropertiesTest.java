package com.bookingsystem.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(
		classes = ClerkPropertiesTest.TestConfiguration.class,
		properties = {
			"app.clerk.issuer-uri=https://example.clerk.accounts.dev",
			"app.clerk.jwk-set-uri=https://example.clerk.accounts.dev/.well-known/jwks.json",
			"app.clerk.audience=booking-api"
		})
class ClerkPropertiesTest {

	@org.springframework.beans.factory.annotation.Autowired
	private ClerkProperties clerkProperties;

	@Test
	void bindsClerkConfigurationProperties() {
		assertThat(clerkProperties.getIssuerUri()).isEqualTo("https://example.clerk.accounts.dev");
		assertThat(clerkProperties.getJwkSetUri())
				.isEqualTo("https://example.clerk.accounts.dev/.well-known/jwks.json");
		assertThat(clerkProperties.getAudience()).isEqualTo("booking-api");
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(ClerkProperties.class)
	static class TestConfiguration {
	}
}
