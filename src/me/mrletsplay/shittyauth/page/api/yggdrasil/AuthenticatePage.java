package me.mrletsplay.shittyauth.page.api.yggdrasil;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.UUIDHelper;
import me.mrletsplay.shittyauth.auth.AccessToken;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.header.DefaultClientContentTypes;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.impl.PasswordAuth;

public class AuthenticatePage implements HttpDocument {

	// https://wiki.vg/Authentication

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		JSONObject obj = ctx.getClientHeader().getPostData().getParsedAs(DefaultClientContentTypes.JSON_OBJECT);

		if(!obj.has("username") || !obj.has("password")) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.UNAUTHORIZED_401);
			JSONObject response = new JSONObject();
			response.put("error", "ForbiddenOperationException");
			response.put("errorMessage", "Forbidden");
			ctx.getServerHeader().setContent("application/json", response.toString().getBytes(StandardCharsets.UTF_8));
			return;
		}

		String
			username = obj.getString("username"),
			password = obj.getString("password"),
			clientToken = obj.optString("clientToken").orElse(null);

		Account acc = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(PasswordAuth.ID, username);
		if(acc == null || !Webinterface.getCredentialsStorage().checkCredentials(username, password)) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.UNAUTHORIZED_401);
			JSONObject response = new JSONObject();
			response.put("error", "ForbiddenOperationException");
			response.put("errorMessage", "Invalid credentials. Invalid username or password.");
			ctx.getServerHeader().setContent("application/json", response.toString().getBytes(StandardCharsets.UTF_8));
			return;
		}

		JSONObject response = new JSONObject();

		if(obj.optBoolean("requestUser").orElse(false)) {
			JSONObject user = new JSONObject();
			user.put("username", username);
			JSONArray properties = new JSONArray();
			user.put("properties", properties);
			user.put("id", acc.getID());
			response.put("user", user);
		}

		JSONObject profile = new JSONObject();
		profile.put("name", username);
		profile.put("id", UUIDHelper.toShortUUID(UUID.fromString(acc.getID())));

		JSONArray availableProfiles = new JSONArray();
		availableProfiles.add(profile);
		response.put("availableProfiles", availableProfiles);

		response.put("selectedProfile", profile);

		AccessToken tok = ShittyAuth.tokenStorage.generateToken(acc.getID(), clientToken);
		response.put("accessToken", tok.getAccessToken());
		response.put("clientToken", tok.getClientToken());

		ctx.getServerHeader().setContent("application/json", response.toString().getBytes(StandardCharsets.UTF_8));
	}

}
