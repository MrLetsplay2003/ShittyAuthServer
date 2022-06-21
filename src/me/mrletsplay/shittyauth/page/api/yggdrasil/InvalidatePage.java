package me.mrletsplay.shittyauth.page.api.yggdrasil;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.auth.StoredAccessToken;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.header.DefaultClientContentTypes;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;

public class InvalidatePage implements HttpDocument {

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		JSONObject obj = ctx.getClientHeader().getPostData().getParsedAs(DefaultClientContentTypes.JSON_OBJECT);
		String
			accessToken = obj.getString("accessToken"),
			clientToken = obj.optString("clientToken").orElse(null);

		// Check the token
		StoredAccessToken tok = ShittyAuth.tokenStorage.getStoredToken(accessToken);
		if(tok == null || (clientToken != null && !clientToken.equals(tok.getClientToken()))) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.ACCESS_DENIED_403);
			JSONObject err = new JSONObject();
			err.put("error", "ForbiddenOperationException");
			err.put("errorMessage", "Invalid token.");
			return;
		}

		Account acc = Webinterface.getAccountStorage().getAccountByID(tok.getAccountID());
		if(acc == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.ACCESS_DENIED_403);
			JSONObject err = new JSONObject();
			err.put("error", "ForbiddenOperationException");
			err.put("errorMessage", "Invalid token.");
			return;
		}

		// Invalidate the token
		ShittyAuth.tokenStorage.removeToken(accessToken);
	}

}
