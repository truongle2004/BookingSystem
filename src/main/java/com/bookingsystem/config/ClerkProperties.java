package com.bookingsystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.clerk")
public class ClerkProperties {

	public static final String PREFIX = "app.clerk";

	public static final String ISSUER_URI_PROPERTY = PREFIX + ".issuer-uri";

	public static final String JWK_SET_URI_PROPERTY = PREFIX + ".jwk-set-uri";

	public static final String AUDIENCE_PROPERTY = PREFIX + ".audience";

	private String issuerUri;

	private String jwkSetUri;

	private String audience;

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
}
