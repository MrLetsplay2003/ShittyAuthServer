package me.mrletsplay.shittyauth.page.api.legacy;

import java.nio.charset.StandardCharsets;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.simplehttpserver.http.request.urlencoded.URLEncoded;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;

public class LegacyJoinServerDocument implements HttpDocument {

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		URLEncoded query = ctx.getClientHeader().getPath().getQuery();

		if(!query.has("sessionId") || !query.has("user") || !query.has("serverId")) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.BAD_REQUEST_400);
			ctx.getServerHeader().setContent("text/plain", "Bad Request".getBytes(StandardCharsets.UTF_8));
			return;
		}

		String accessToken = query.getFirst("sessionId");
		String accName = query.getFirst("user");
		String serverId = query.getFirst("serverId");

		String accID = ShittyAuth.tokenStorage.getAccountID(accessToken);

		if(accID == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.UNAUTHORIZED_401);
			return;
		}

		Account acc = ShittyAuth.getAccountByUsername(accName);
		AccountConnection con = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(!con.getUserID().equals(accID)) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.UNAUTHORIZED_401);
			return;
		}

		ShittyAuth.userServers.put(con.getUserID(), serverId);
		ctx.getServerHeader().setStatusCode(HttpStatusCodes.OK_200);
		ctx.getServerHeader().setContent("text/plain", "ok".getBytes(StandardCharsets.UTF_8));
	}

}
