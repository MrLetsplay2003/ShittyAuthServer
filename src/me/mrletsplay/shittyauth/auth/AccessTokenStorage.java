package me.mrletsplay.shittyauth.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public interface AccessTokenStorage {

	public void initialize();

	public AccessToken generateToken(String accID, String clientToken);

	public String getAccountID(String token);

	public StoredAccessToken getStoredToken(String token);

	public boolean checkValid(String token, String clientToken);

	public void removeToken(String token);

	public void removeTokensByAccountID(String accID);

	public void cleanUp();

	public default AccessToken createNewToken(String accountID, String clientToken) {
		String tok = newToken();
		Instant softExpires = Instant.now().plus(7, ChronoUnit.DAYS);
		Instant expires = Instant.now().plus(30, ChronoUnit.DAYS);
		return new AccessToken(clientToken, accountID, softExpires.toEpochMilli(), expires.toEpochMilli(), tok);
	}

	private static String newToken() {
		return UUID.randomUUID().toString();
	}

}