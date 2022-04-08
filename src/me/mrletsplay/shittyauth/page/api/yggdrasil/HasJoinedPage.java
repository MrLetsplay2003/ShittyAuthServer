package me.mrletsplay.shittyauth.page.api.yggdrasil;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.Base64;
import java.util.UUID;

import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.UUIDHelper;
import me.mrletsplay.shittyauth.textures.TexturesHelper;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.webinterfaceapi.http.HttpStatusCodes;
import me.mrletsplay.webinterfaceapi.http.document.HttpDocument;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;
import me.mrletsplay.webinterfaceapi.webinterface.auth.WebinterfaceAccount;
import me.mrletsplay.webinterfaceapi.webinterface.auth.impl.PasswordAuth;

public class HasJoinedPage implements HttpDocument {

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		
		WebinterfaceAccount acc = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(PasswordAuth.ID, ctx.getClientHeader().getPath().getQueryParameterValue("username"));
		if(acc == null || acc.getConnection(PasswordAuth.ID) == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NOT_FOUND_404);
			return;
		}
		
		String playerID = UUIDHelper.toShortUUID(UUID.fromString(acc.getID()));
		String serverHash = ShittyAuth.userServers.get(playerID);
		String hash = ctx.getClientHeader().getPath().getQueryParameterValue("serverId");
		if(hash == null || serverHash == null || !serverHash.equals(hash)) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NO_CONTENT_204);
			return;
		}
		
		ShittyAuth.userServers.remove(playerID);
		
		JSONObject obj = new JSONObject();
		obj.put("id", UUIDHelper.toShortUUID(UUID.fromString(acc.getID())));
		obj.put("name", acc.getConnection(PasswordAuth.ID).getUserName());
		
		JSONArray a = new JSONArray();
		JSONObject b = new JSONObject();
		b.put("name", "textures");
		
		JSONObject textures = new JSONObject();
		textures.put("timestamp", System.currentTimeMillis());
		textures.put("profileId", UUIDHelper.toShortUUID(UUID.fromString(acc.getID())));
		textures.put("profileName", acc.getConnection(PasswordAuth.ID).getUserName());
		
		UserData d = ShittyAuth.dataStorage.getUserData(acc.getID());
		textures.put("textures", TexturesHelper.getTexturesObject(acc.getID(), d));
		
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
