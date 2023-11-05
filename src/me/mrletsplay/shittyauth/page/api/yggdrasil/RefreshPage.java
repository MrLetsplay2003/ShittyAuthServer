package me.mrletsplay.shittyauth.page.api.yggdrasil;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.auth.AccessToken;
import me.mrletsplay.shittyauth.auth.StoredAccessToken;
import me.mrletsplay.shittyauth.util.UUIDHelper;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.header.DefaultClientContentTypes;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.simplehttpserver.http.util.MimeType;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;

public class RefreshPage implements HttpDocument {

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		JSONObject obj = ctx.getClientHeader().getPostData().getParsedAs(DefaultClientContentTypes.JSON_OBJECT);
		String
			accessToken = obj.getString("accessToken"),
			clientToken = obj.optString("clientToken").orElse(null);
		boolean requestUser = obj.optBoolean("requestUser").orElse(false);

		// Check the old token
		StoredAccessToken tok = ShittyAuth.tokenStorage.getStoredToken(accessToken);
		if(tok == null || (clientToken != null && !clientToken.equals(tok.getClientToken()))) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.ACCESS_DENIED_403);
			JSONObject err = new JSONObject();
			err.put("error", "ForbiddenOperationException");
			err.put("errorMessage", "Invalid token.");
			return;
		}

		Account acc = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(ShittyAuth.ACCOUNT_CONNECTION_NAME, tok.getAccountID());
		if(acc == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.ACCESS_DENIED_403);
			JSONObject err = new JSONObject();
			err.put("error", "ForbiddenOperationException");
			err.put("errorMessage", "Invalid token.");
			return;
		}

		// Invalidate the old token
		ShittyAuth.tokenStorage.removeToken(accessToken);

		AccountConnection con = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);

		// Then create a new token
		AccessToken newTok = ShittyAuth.tokenStorage.generateToken(con.getUserID(), clientToken);

		JSONObject r = new JSONObject();
		r.put("accessToken", newTok.getAccessToken());
		r.put("clientToken", newTok.getClientToken());

		JSONObject selectedProfile = new JSONObject();
		selectedProfile.put("id", UUIDHelper.toShortUUID(UUID.fromString(con.getUserID())));
		selectedProfile.put("name", con.getUserName());
		r.put("selectedProfile", selectedProfile);

		if(requestUser) { // To be compliant with Yggdrasil (https://wiki.vg/Authentication#Refresh)
			JSONObject user = new JSONObject();
			user.put("username", con.getUserName());
			user.put("id", acc.getID());

			user.put("properties", new JSONArray()); // TODO: Maybe add preferredLanguage?
		}

		ctx.getServerHeader().setContent(MimeType.JSON, r.toString().getBytes(StandardCharsets.UTF_8));
	}

}
