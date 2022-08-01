package me.mrletsplay.shittyauth.auth;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.webinterfaceapi.sql.SQLHelper;

public class SQLAccessTokenStorage implements AccessTokenStorage {

	// TODO: Expire tokens, keep only one token per clientToken, add a way for users to manually invalidate tokens?

	@Override
	public void initialize() {
		SQLHelper.run(c -> {
			try(PreparedStatement st = c.prepareStatement("CREATE TABLE IF NOT EXISTS " + SQLHelper.tableName("shittyauth_tokens") + "(TokenHash VARCHAR(255) PRIMARY KEY, AccountId VARCHAR(255), ClientToken VARCHAR(255), SoftExpiresAt BIGINT, ExpiresAt BIGINT)")) {
				st.execute();
			}
		});
	}

	@Override
	public AccessToken generateToken(String accID, String clientToken) {
		if(clientToken == null) clientToken = UUID.randomUUID().toString();
		AccessToken token = createNewToken(accID, clientToken);
		SQLHelper.run(c -> {
			try(PreparedStatement st = c.prepareStatement("INSERT INTO " + SQLHelper.tableName("shittyauth_tokens") + "(TokenHash, AccountId, ClientToken, SoftExpiresAt, ExpiresAt) VALUES(?, ?, ?, ?, ?)")) {
				st.setString(1, hash(token.getAccessToken()));
				st.setString(2, accID);
				st.setString(3, token.getClientToken());
				st.setLong(4, token.getSoftExpiresAt());
				st.setLong(5, token.getExpiresAt());
				st.execute();
			}
		});
		return token;
	}

	@Override
	public String getAccountID(String token) {
		return SQLHelper.run(c -> {
			try(PreparedStatement st = c.prepareStatement("SELECT AccountId FROM " + SQLHelper.tableName("shittyauth_tokens") + " WHERE TokenHash = ?")) {
				st.setString(1, hash(token));
				try(ResultSet r = st.executeQuery()) {
					if(!r.next()) return null;
					return r.getString("AccountId");
				}
			}
		});
	}

	@Override
	public StoredAccessToken getStoredToken(String token) {
		return SQLHelper.run(c -> {
			try(PreparedStatement st = c.prepareStatement("SELECT * FROM " + SQLHelper.tableName("shittyauth_tokens") + " WHERE TokenHash = ?")) {
				st.setString(1, hash(token));
				try(ResultSet r = st.executeQuery()) {
					if(!r.next()) return null;
					return new StoredAccessToken(r.getString("ClientToken"), r.getString("AccountId"), r.getLong("SoftExpiresAt"), r.getLong("ExpiresAt"));
				}
			}
		});
	}

	@Override
	public boolean checkValid(String token, String clientToken) {
		return SQLHelper.run(c -> {
			try(PreparedStatement st = c.prepareStatement("SELECT ClientToken FROM " + SQLHelper.tableName("shittyauth_tokens") + " WHERE TokenHash = ?")) {
				st.setString(1, hash(token));
				try(ResultSet r = st.executeQuery()) {
					if(!r.next()) return false;
					return r.getString("ClientToken").equals(clientToken);
				}
			}
		});
	}

	@Override
	public void removeToken(String token) {
		SQLHelper.run(c -> {
			try(PreparedStatement st = c.prepareStatement("DELETE FROM " + SQLHelper.tableName("shittyauth_tokens") + " WHERE TokenHash = ?")) {
				st.setString(1, hash(token));
				st.execute();
			}
		});
	}

	@Override
	public void cleanUp() {
		long currentTime = System.currentTimeMillis();
		SQLHelper.run(c -> {
			try(PreparedStatement st = c.prepareStatement("DELETE FROM " + SQLHelper.tableName("shittyauth_tokens") + " WHERE ExpiresAt > ?")) {
				st.setLong(1, currentTime);
				st.execute();
			}
		});
	}

	private String hash(String raw) {
		try {
			byte[] salt = new byte[16]; // No salt initialization needed, token is already random
			KeySpec spec = new PBEKeySpec(raw.toCharArray(), salt, 65536, 128);
			SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = f.generateSecret(spec).getEncoded();
			return Base64.getEncoder().encodeToString(hash);
		}catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new FriendlyException(e);
		}
	}

}
