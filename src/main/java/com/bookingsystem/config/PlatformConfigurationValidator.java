package com.bookingsystem.config;

import java.net.URI;
import java.util.Set;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Fails startup once when public environment configuration is missing or unsafe. */
@Component
public class PlatformConfigurationValidator implements InitializingBean {

    private static final Set<String> ENVIRONMENTS = Set.of("local", "test", "production");

    private final Environment environment;

    private final ClerkProperties clerkProperties;

    public PlatformConfigurationValidator(
            final Environment environment,
            final ClerkProperties clerkProperties) {
        this.environment = environment;
        this.clerkProperties = clerkProperties;
    }

    @Override
    public void afterPropertiesSet() {
        String appEnvironment = environment.getRequiredProperty("app.environment");
        if (!ENVIRONMENTS.contains(appEnvironment)) {
            throw configurationError("APP_ENV must be local, test, or production");
        }

        String databaseUrl = environment.getRequiredProperty("spring.datasource.url");
        if (!databaseUrl.startsWith("jdbc:postgresql://")) {
            throw configurationError("DATABASE_URL must start with jdbc:postgresql:// (got: <redacted>)");
        }
        requireText("DATABASE_USER", environment.getProperty("spring.datasource.username"));
        requireText("DATABASE_PASSWORD", environment.getProperty("spring.datasource.password"));
        requireText("CLERK_ISSUER_URI", clerkProperties.getIssuerUri());

        URI issuerUri;
        try {
            issuerUri = URI.create(clerkProperties.getIssuerUri());
        } catch (IllegalArgumentException exception) {
            throw configurationError("CLERK_ISSUER_URI must be a valid absolute URI");
        }
        if (!issuerUri.isAbsolute() || !StringUtils.hasText(issuerUri.getHost())) {
            throw configurationError("CLERK_ISSUER_URI must be a valid absolute URI");
        }
        if ("production".equals(appEnvironment) && !"https".equalsIgnoreCase(issuerUri.getScheme())) {
            throw configurationError("CLERK_ISSUER_URI must use https when APP_ENV=production");
        }
    }

    private void requireText(final String variable, final String value) {
        if (!StringUtils.hasText(value)) {
            throw configurationError(variable + " is required");
        }
    }

    private IllegalStateException configurationError(final String message) {
        return new IllegalStateException("Configuration error: " + message);
    }
}
