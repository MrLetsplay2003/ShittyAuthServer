package me.mrletsplay.shittyauth.page.api.yggdrasil;

import java.nio.charset.StandardCharsets;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.header.DefaultClientContentTypes;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;

public class SignoutPage implements HttpDocument {

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
			password = obj.getString("password");

		Account acc = ShittyAuth.getAccountByUsername(username);
		AccountConnection con = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(acc == null || !Webinterface.getCredentialsStorage().checkCredentials(ShittyAuth.ACCOUNT_CONNECTION_NAME, con.getUserID(), password)) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.UNAUTHORIZED_401);
			JSONObject response = new JSONObject();
			response.put("error", "ForbiddenOperationException");
			response.put("errorMessage", "Invalid credentials. Invalid username or password.");
			ctx.getServerHeader().setContent("application/json", response.toString().getBytes(StandardCharsets.UTF_8));
			return;
		}

		ShittyAuth.tokenStorage.removeTokensByAccountID(con.getUserID());
		ctx.getServerHeader().setStatusCode(HttpStatusCodes.NO_CONTENT_204);
	}

}
