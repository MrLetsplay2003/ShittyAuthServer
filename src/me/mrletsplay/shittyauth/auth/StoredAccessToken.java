package me.mrletsplay.shittyauth.auth;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class StoredAccessToken implements JSONConvertible {
	
	@JSONValue
	private String clientToken;
	
	@JSONValue
	private String accountID;

	@JSONConstructor
	private StoredAccessToken() {}
	
	public StoredAccessToken(String clientToken, String accountID) {
		this.clientToken = clientToken;
		this.accountID = accountID;
	}

	public String getClientToken() {
		return clientToken;
	}

	public String getAccountID() {
		return accountID;
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
