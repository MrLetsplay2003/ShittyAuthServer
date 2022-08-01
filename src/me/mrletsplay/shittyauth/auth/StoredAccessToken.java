package me.mrletsplay.shittyauth.auth;

public class StoredAccessToken {

	protected String clientToken;
	protected String accountID;

	/**
	 * Time when the token becomes unusable for authentication when joining a server, but can still be refreshed
	 */
	protected long softExpiresAt;

	/**
	 * Time when the token fully expires and cannot be refreshed anymore
	 */
	protected long expiresAt;

	public StoredAccessToken(String clientToken, String accountID, long softExpiresAt, long expiresAt) {
		this.clientToken = clientToken;
		this.accountID = accountID;
		this.softExpiresAt = softExpiresAt;
		this.expiresAt = expiresAt;
	}

	public String getClientToken() {
		return clientToken;
	}

	public String getAccountID() {
		return accountID;
	}

	public long getSoftExpiresAt() {
		return softExpiresAt;
	}

	public long getExpiresAt() {
		return expiresAt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountID == null) ? 0 : accountID.hashCode());
		result = prime * result + ((clientToken == null) ? 0 : clientToken.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StoredAccessToken other = (StoredAccessToken) obj;
		if (accountID == null) {
			if (other.accountID != null)
				return false;
		} else if (!accountID.equals(other.accountID))
			return false;
		if (clientToken == null) {
			if (other.clientToken != null)
				return false;
		} else if (!clientToken.equals(other.clientToken))
			return false;
		return true;
	}

}
