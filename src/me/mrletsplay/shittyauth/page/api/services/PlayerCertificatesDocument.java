package me.mrletsplay.shittyauth.page.api.services;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.auth.StoredAccessToken;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.shittyauth.util.CryptoHelper;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.simplehttpserver.http.util.MimeType;

public class PlayerCertificatesDocument implements HttpDocument {

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		String auth = ctx.getClientHeader().getFields().getFirst("Authorization");
		if(!auth.startsWith("Bearer ")) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.ACCESS_DENIED_403);
			return;
		}

		auth = auth.substring("Bearer ".length());
		StoredAccessToken tok = ShittyAuth.tokenStorage.getStoredToken(auth);
		if(tok == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.ACCESS_DENIED_403);
			return;
		}

		Instant expAt = Instant.now().plus(7, ChronoUnit.DAYS); // TODO: Use correct timestamps
		UserData userData = ShittyAuth.dataStorage.getUserData(tok.getAccountID());

		JSONObject obj = new JSONObject();
		JSONObject keyPair = new JSONObject();
		keyPair.put("privateKey", CryptoHelper.encodeRSAPrivateKey(userData.getPrivateKey()));
		keyPair.put("publicKey", CryptoHelper.encodeRSAPublicKey(userData.getPublicKey()));
		obj.put("keyPair", keyPair);

		try {
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initSign(ShittyAuth.privateKey);
			sig.update(getSignaturePayload(UUID.fromString(tok.getAccountID()), expAt, userData.getPublicKey()));
			byte[] sign = sig.sign();
//			obj.put("publicKeySignature", Base64.getEncoder().encodeToString(sign)); TODO: v1 signature?
			obj.put("publicKeySignatureV2", Base64.getEncoder().encodeToString(sign));
		}catch (Exception e) {
			e.printStackTrace();
		}

		obj.put("refreshedAfter", Instant.now().plus(7, ChronoUnit.DAYS).toString());
		obj.put("expiresAt", expAt.toString());

		ctx.getServerHeader().setContent(MimeType.JSON, obj.toString().getBytes(StandardCharsets.UTF_8));
	}

	private byte[] getSignaturePayload(UUID playerUUID, Instant expiresAt, PublicKey key) {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		DataOutputStream dOut = new DataOutputStream(bOut);
		try {
			dOut.writeLong(playerUUID.getMostSignificantBits());
			dOut.writeLong(playerUUID.getLeastSignificantBits());
			dOut.writeLong(expiresAt.toEpochMilli());
			dOut.write(key.getEncoded());
		} catch (IOException ignored) {}
		return bOut.toByteArray();
	}

}
