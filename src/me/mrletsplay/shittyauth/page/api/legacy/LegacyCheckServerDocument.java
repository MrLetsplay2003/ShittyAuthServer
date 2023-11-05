package me.mrletsplay.shittyauth.page.api.legacy;

import java.nio.charset.StandardCharsets;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.simplehttpserver.http.request.urlencoded.UrlEncoded;
import me.mrletsplay.simplehttpserver.http.util.MimeType;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;

public class LegacyCheckServerDocument implements HttpDocument {

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		UrlEncoded query = ctx.getClientHeader().getPath().getQuery();

		if(!query.has("user") || !query.has("serverId")) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.BAD_REQUEST_400);
			ctx.getServerHeader().setContent(MimeType.TEXT, "Bad Request".getBytes(StandardCharsets.UTF_8));
			return;
		}

		String accName = query.getFirst("user");
		String serverId = query.getFirst("serverId");

		Account acc = ShittyAuth.getAccountByUsername(accName);
		AccountConnection con = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		String joinedServer = ShittyAuth.userServers.get(con.getUserID());
		if(joinedServer == null || !joinedServer.equals(serverId)) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.UNAUTHORIZED_401);
			return;
		}

		ctx.getServerHeader().setStatusCode(HttpStatusCodes.OK_200);
		ctx.getServerHeader().setContent(MimeType.TEXT, "YES".getBytes(StandardCharsets.UTF_8));
	}

}
