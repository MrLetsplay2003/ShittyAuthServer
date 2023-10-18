package me.mrletsplay.shittyauth.page.api.yggdrasil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;

public class PublicKeysPage implements HttpDocument {

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		ctx.getServerHeader().setStatusCode(HttpStatusCodes.OK_200);

		JSONObject obj = new JSONObject();

		// playerCertificateKeys is not currently used by clients using the ShittyAuthServer. It might be used for signed chat messages and the connected player certificates
		obj.put("playerCertificateKeys", new JSONArray());

		// profilePropertyKeys is used for signing properties on the profile page. ShittyAuthServer just uses the normal private key for that
		JSONArray profileKeys = new JSONArray();
		JSONObject key = new JSONObject();
		key.put("publicKey", Base64.getEncoder().encodeToString(ShittyAuth.publicKey.getEncoded()));
		profileKeys.add(key);
		obj.put("profilePropertyKeys", profileKeys);

		ctx.getServerHeader().setContent(obj.toString().getBytes(StandardCharsets.UTF_8));
	}

}
