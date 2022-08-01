package me.mrletsplay.shittyauth.auth;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.FileCustomConfig;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.webinterfaceapi.Webinterface;

public class FileAccessTokenStorage implements AccessTokenStorage {

	// TODO: Expire tokens, keep only one token per clientToken, add a way for users to manually invalidate tokens?

	private FileCustomConfig config;

	@Override
	public void initialize() {
		config = ConfigLoader.loadFileConfig(new File(Webinterface.getDataDirectory(), "shittyauth/token-storage.yml"));
	}

	@Override
	public AccessToken generateToken(String accID, String clientToken) {
		if(clientToken == null) clientToken = UUID.randomUUID().toString();
		AccessToken token = createNewToken(accID, clientToken);

		String tokenHash = hash(token.getAccessToken());
		config.set(tokenHash + ".client-token", token.getClientToken());
		config.set(tokenHash + ".account-id", token.getAccountID());
		config.set(tokenHash + ".soft-expires-at", token.getSoftExpiresAt());
		config.set(tokenHash + ".expires-at", token.getExpiresAt());
		config.saveToFile();
		return token;
	}

	@Override
	public String getAccountID(String token) {
		return config.getString(hash(token) + ".account-id");
	}

	@Override
	public StoredAccessToken getStoredToken(String token) {
		String tokenHash = hash(token);
		return new StoredAccessToken(
			config.getString(tokenHash + ".client-token"),
			config.getString(tokenHash + ".account-id"),
			config.getLong(tokenHash + ".soft-expires-at"),
			config.getLong(tokenHash + ".expires-at"));
	}

	@Override
	public boolean checkValid(String token, String clientToken) {
		String clientTok = config.getString(hash(token) + ".client-token");
		if(clientTok == null) return false;
		return clientTok.equals(clientToken);
	}

	@Override
	public void removeToken(String token) {
		config.unset(hash(token));
		config.saveToFile();
	}

	@Override
	public void cleanUp() {
		long currentTime = System.currentTimeMillis();
		for(String k : config.getKeys()) {
			if(config.getLong(k + ".expires-at") > currentTime) {
				config.unset(k);
			}
		}
		config.saveToFile();
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
