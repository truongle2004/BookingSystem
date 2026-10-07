package com.bookingsystem.config;

import java.util.ArrayList;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.clerk")
public class ClerkProperties {

    public static final String PREFIX = "app.clerk";

    public static final String ISSUER_URI_PROPERTY = PREFIX + ".issuer-uri";

    public static final String JWK_SET_URI_PROPERTY = PREFIX + ".jwk-set-uri";

    public static final String AUDIENCE_PROPERTY = PREFIX + ".audience";

    public static final String AUTHORIZED_PARTIES_PROPERTY = PREFIX + ".authorized-parties";

    public static final String CLOCK_SKEW_PROPERTY = PREFIX + ".clock-skew";

    public static final String JWK_CONNECT_TIMEOUT_PROPERTY = PREFIX + ".jwk-connect-timeout";

    public static final String JWK_READ_TIMEOUT_PROPERTY = PREFIX + ".jwk-read-timeout";

    private String issuerUri;

    private String jwkSetUri;

    private String audience;

    private List<String> authorizedParties = new ArrayList<>();

    private Duration clockSkew = Duration.ofSeconds(30);

    private Duration jwkConnectTimeout = Duration.ofSeconds(3);

    private Duration jwkReadTimeout = Duration.ofSeconds(5);

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public List<String> getAuthorizedParties() {
        return authorizedParties;
    }

    public void setAuthorizedParties(List<String> authorizedParties) {
        this.authorizedParties = authorizedParties;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(issuerUri);
    }

    public Duration getClockSkew() {
        return clockSkew;
    }

    public void setClockSkew(final Duration clockSkew) {
        this.clockSkew = clockSkew;
    }

    public Duration getJwkConnectTimeout() {
        return jwkConnectTimeout;
    }

    public void setJwkConnectTimeout(final Duration jwkConnectTimeout) {
        this.jwkConnectTimeout = jwkConnectTimeout;
    }

    public Duration getJwkReadTimeout() {
        return jwkReadTimeout;
    }

    public void setJwkReadTimeout(final Duration jwkReadTimeout) {
        this.jwkReadTimeout = jwkReadTimeout;
    }
}
