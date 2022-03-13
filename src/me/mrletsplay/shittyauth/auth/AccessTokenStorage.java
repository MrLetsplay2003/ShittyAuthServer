package me.mrletsplay.shittyauth.auth;

import java.io.File;
import java.util.UUID;

import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.FileCustomConfig;
import me.mrletsplay.mrcore.config.mapper.JSONObjectMapper;

public class AccessTokenStorage {
	
	private FileCustomConfig config;
//	private SecureRandom random;
	
	public AccessTokenStorage() {
		config = ConfigLoader.loadFileConfig(new File("shittyauth/token-storage.yml"));
		config.registerMapper(JSONObjectMapper.create(StoredAccessToken.class));
//		random = new SecureRandom();
	}
	
	public AccessToken generateToken(String accID, String clientToken) {
		if(clientToken == null) clientToken = UUID.randomUUID().toString();
		String tok = newToken();
		AccessToken at = new AccessToken(tok, accID, clientToken);
		config.set(tok, new StoredAccessToken(clientToken, accID));
		config.saveToFile();
		return at;
	}
	
	public String getAccountID(String token) {
		StoredAccessToken tok = config.getGeneric(token, StoredAccessToken.class);
		if(tok == null) return null;
		return tok.getAccountID();
	}
	
	public StoredAccessToken getStoredToken(String token) {
		return config.getGeneric(token, StoredAccessToken.class);
	}
	
	public boolean checkValid(String token, String clientToken) {
		StoredAccessToken tok = config.getGeneric(token, StoredAccessToken.class);
		if(tok == null) return false;
		return clientToken == null || clientToken.equals(tok.getClientToken());
	}
	
	public void cleanUp() {
		// TODO: delete expired tokens
	}
	
	private static String newToken() {
		return UUID.randomUUID().toString();
	}

//	private byte[] generateSalt() {
//		byte[] salt = new byte[16];
//		random.nextBytes(salt);
//		return salt;
//	}
//	
//	private String hash(String raw, byte[] salt) {
//		try {
//			KeySpec spec = new PBEKeySpec(raw.toCharArray(), salt, 65536, 128);
//			SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//			byte[] hash = f.generateSecret(spec).getEncoded();
//			return Base64.getEncoder().encodeToString(hash);
//		}catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
//			throw new FriendlyException(e);
//		}
//	}

}
