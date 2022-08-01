package me.mrletsplay.shittyauth.util;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import me.mrletsplay.mrcore.misc.FriendlyException;

public class CryptoHelper {

	private static final String
		PUBKEY_HEADER = "-----BEGIN RSA PUBLIC KEY-----",
		PUBKEY_FOOTER = "-----END RSA PUBLIC KEY-----",
		PRIVKEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----",
		PRIVKEY_FOOTER = "-----END RSA PRIVATE KEY-----";

	private static final KeyFactory RSA;
	private static final Base64.Encoder ENCODER = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8));

	static {
		try {
			RSA = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			throw new FriendlyException(e);
		}
	}

	public static KeyPair generateRSAKeyPair() {
		try {
			return KeyPairGenerator.getInstance("RSA").generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new FriendlyException(e);
		}
	}

	public static PublicKey parseRSAPublicKey(byte[] bytes) {
		X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
		try {
			return RSA.generatePublic(spec);
		} catch (InvalidKeySpecException e) {
			throw new FriendlyException(e);
		}
	}

	public static PrivateKey parseRSAPrivateKey(byte[] bytes) {
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
		try {
			return RSA.generatePrivate(spec);
		} catch (InvalidKeySpecException e) {
			throw new FriendlyException(e);
		}
	}

	public static PublicKey parseRSAPublicKey(String str) {
		byte[] bs = Base64.getMimeDecoder().decode(str.substring(PUBKEY_HEADER.length(), str.length() - PUBKEY_FOOTER.length()));
		return parseRSAPublicKey(bs);
	}

	public static PrivateKey parseRSAPrivateKey(String str) {
		byte[] bs = Base64.getMimeDecoder().decode(str.substring(PRIVKEY_HEADER.length(), str.length() - PRIVKEY_FOOTER.length()));
		return parseRSAPrivateKey(bs);
	}

	public static String encodeRSAPublicKey(PublicKey key) {
		byte[] bs = key.getEncoded();
		return PUBKEY_HEADER + "\n" + ENCODER.encodeToString(bs) + "\n" + PUBKEY_FOOTER;
	}

	public static String encodeRSAPrivateKey(PrivateKey key) {
		byte[] bs = key.getEncoded();
		return PRIVKEY_HEADER + "\n" + ENCODER.encodeToString(bs) + "\n" + PRIVKEY_FOOTER;
	}

}
