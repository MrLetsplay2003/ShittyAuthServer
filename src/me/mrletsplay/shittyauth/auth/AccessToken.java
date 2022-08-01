package me.mrletsplay.shittyauth.auth;

import java.util.Objects;

public class AccessToken extends StoredAccessToken {

	private String accessToken;

	public AccessToken(String clientToken, String accountID, long softExpiresAt, long expiresAt, String accessToken) {
		super(clientToken, accountID, softExpiresAt, expiresAt);
		this.accessToken = accessToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(accessToken);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccessToken other = (AccessToken) obj;
		return Objects.equals(accessToken, other.accessToken);
	}

}
