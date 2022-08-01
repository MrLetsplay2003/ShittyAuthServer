package me.mrletsplay.shittyauth.page.api.yggdrasil;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.Base64;
import java.util.UUID;

import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.textures.TexturesHelper;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.shittyauth.util.UUIDHelper;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;

public class HasJoinedPage implements HttpDocument {

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		String username = ctx.getClientHeader().getPath().getQuery().getFirst("username");
		if(username == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NOT_FOUND_404);
			return;
		}

		Account acc = ShittyAuth.getAccountByUsername(username);
		if(acc == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NOT_FOUND_404);
			return;
		}

		AccountConnection con = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);

		String serverHash = ShittyAuth.userServers.get(con.getUserID());
		String hash = ctx.getClientHeader().getPath().getQuery().getFirst("serverId");
		if(hash == null || serverHash == null || !serverHash.equals(hash)) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NO_CONTENT_204);
			return;
		}

		ShittyAuth.userServers.remove(con.getUserID());

		JSONObject obj = new JSONObject();
		obj.put("id", UUIDHelper.toShortUUID(UUID.fromString(con.getUserID())));
		obj.put("name", con.getUserName());

		JSONArray a = new JSONArray();
		JSONObject b = new JSONObject();
		b.put("name", "textures");

		JSONObject textures = new JSONObject();
		textures.put("timestamp", System.currentTimeMillis());
		textures.put("profileId", UUIDHelper.toShortUUID(UUID.fromString(con.getUserID())));
		textures.put("profileName", con.getUserName());

		UserData d = ShittyAuth.dataStorage.getUserData(con.getUserID());
		textures.put("textures", TexturesHelper.getTexturesObject(con.getUserID(), d));

		String b64Value = Base64.getEncoder().encodeToString(textures.toString().getBytes());
		b.put("value", b64Value);

		try {
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initSign(ShittyAuth.privateKey);
			sig.update(b64Value.getBytes(StandardCharsets.UTF_8));
			b.put("signature", Base64.getEncoder().encodeToString(sig.sign()));
		}catch (Exception e) {
			e.printStackTrace();
		}
		a.add(b);
		obj.put("properties", a);

		ctx.getServerHeader().setContent("application/json", obj.toString().getBytes(StandardCharsets.UTF_8));
	}

}
