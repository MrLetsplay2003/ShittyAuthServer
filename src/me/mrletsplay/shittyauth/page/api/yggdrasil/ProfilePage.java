package me.mrletsplay.shittyauth.page.api.yggdrasil;

import java.nio.charset.StandardCharsets;
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
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;

public class ProfilePage implements HttpDocument {

	public static final ProfilePage INSTANCE = new ProfilePage();

	@Override
	public void createContent() {
		// TODO: ?unsigned=(1|0|true|false)
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		String uuid = ctx.getPathParameters().get("uuid");
		UUID uuidU = UUIDHelper.parseShortUUID(uuid);
		if(uuidU == null) {
			ctx.getServerHeader().setContent("text/plain", "404 Not Found".getBytes(StandardCharsets.UTF_8));
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NOT_FOUND_404);
			return;
		}

		Account acc = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(ShittyAuth.ACCOUNT_CONNECTION_NAME, uuidU.toString());
		if(acc == null) {
			ctx.getServerHeader().setContent("text/plain", "404 Not Found".getBytes(StandardCharsets.UTF_8));
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NOT_FOUND_404);
			return;
		}

		AccountConnection con = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);

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
		// TODO: signatureRequired (present with true if ?unsigned=false)

		UserData d = ShittyAuth.dataStorage.getUserData(con.getUserID());
		textures.put("textures", TexturesHelper.getTexturesObject(con.getUserID(), d));
		b.put("value", Base64.getEncoder().encodeToString(textures.toString().getBytes(StandardCharsets.UTF_8)));
		a.add(b);
		obj.put("properties", a);

		ctx.getServerHeader().setContent("application/json", obj.toString().getBytes(StandardCharsets.UTF_8));
	}

}
