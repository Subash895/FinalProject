package com.smartCity.Web.auth;

import java.util.Set;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.smartCity.Web.config.GoogleAuthProperties;

@Service
public class GoogleIdTokenVerifier {

	private static final String GOOGLE_JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";
	private static final Set<String> VALID_ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

	private final GoogleAuthProperties googleAuthProperties;
	private final JwtDecoder jwtDecoder;

	public GoogleIdTokenVerifier(GoogleAuthProperties googleAuthProperties) {
		this.googleAuthProperties = googleAuthProperties;
		this.jwtDecoder = StringUtils.hasText(googleAuthProperties.getGoogleClientId())
				? buildDecoder(googleAuthProperties.getGoogleClientId())
				: null;
	}

	public boolean isConfigured() {
		return StringUtils.hasText(googleAuthProperties.getGoogleClientId());
	}

	public String getClientId() {
		return googleAuthProperties.getGoogleClientId();
	}

	public VerifiedGoogleUser verify(String credential) {
		if (!isConfigured()) {
			throw new IllegalStateException("Google sign-in is not configured on the server");
		}
		if (!StringUtils.hasText(credential)) {
			throw new RuntimeException("Missing Google credential");
		}

		try {
			Jwt jwt = jwtDecoder.decode(credential);
			String email = jwt.getClaimAsString("email");
			String name = jwt.getClaimAsString("name");
			Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");

			if (!StringUtils.hasText(email)) {
				throw new RuntimeException("Google account did not provide an email address");
			}
			if (!Boolean.TRUE.equals(emailVerified)) {
				throw new RuntimeException("Google account email is not verified");
			}

			return new VerifiedGoogleUser(jwt.getSubject(), email, name);
		} catch (JwtException ex) {
			throw new RuntimeException("Invalid Google sign-in token", ex);
		}
	}

	private JwtDecoder buildDecoder(String clientId) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWK_SET_URI).build();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefault(),
				issuerValidator(),
				audienceValidator(clientId),
				emailVerifiedValidator()));
		return decoder;
	}

	private OAuth2TokenValidator<Jwt> issuerValidator() {
		return jwt -> {
			String issuer = jwt.getIssuer() == null ? null : jwt.getIssuer().toString();
			if (issuer != null && VALID_ISSUERS.contains(issuer)) {
				return OAuth2TokenValidatorResult.success();
			}
			return OAuth2TokenValidatorResult.failure(
					new OAuth2Error("invalid_token", "Invalid Google token issuer", null));
		};
	}

	private OAuth2TokenValidator<Jwt> audienceValidator(String clientId) {
		return jwt -> jwt.getAudience().contains(clientId)
				? OAuth2TokenValidatorResult.success()
				: OAuth2TokenValidatorResult.failure(
						new OAuth2Error("invalid_token", "Google token audience mismatch", null));
	}

	private OAuth2TokenValidator<Jwt> emailVerifiedValidator() {
		return jwt -> Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))
				? OAuth2TokenValidatorResult.success()
				: OAuth2TokenValidatorResult.failure(
						new OAuth2Error("invalid_token", "Google account email is not verified", null));
	}

	public record VerifiedGoogleUser(String subject, String email, String name) {
	}
}

