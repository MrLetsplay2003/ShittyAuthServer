package me.mrletsplay.shittyauth.auth;

public class AccessToken {
	
	private String accessToken;
	private String accountID;
	private String clientToken;
	
	public AccessToken(String accessToken, String accountID, String clientToken) {
		this.accessToken = accessToken;
		this.accountID = accountID;
		this.clientToken = clientToken;
	}

	public String getAccessToken() {
		return accessToken;
	}
	
	public String getAccountID() {
		return accountID;
	}
	
	public String getClientToken() {
		return clientToken;
	}
	
}
