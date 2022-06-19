package me.mrletsplay.shittyauth.page.api.legacy;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.UUIDHelper;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.simplehttpserver.http.request.urlencoded.URLEncoded;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.impl.PasswordAuth;

public class LegacyCheckServerDocument implements HttpDocument {

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		URLEncoded query = ctx.getClientHeader().getPath().getQuery();

		if(!query.has("user") || !query.has("serverId")) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.BAD_REQUEST_400);
			ctx.getServerHeader().setContent("text/plain", "Bad Request".getBytes(StandardCharsets.UTF_8));
			return;
		}

		String accName = query.getFirst("user");
		String serverId = query.getFirst("serverId");

		Account acc = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(PasswordAuth.ID, accName);
		String joinedServer = ShittyAuth.userServers.get(UUIDHelper.toShortUUID(UUID.fromString(acc.getID())));
		if(joinedServer == null || !joinedServer.equals(serverId)) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.UNAUTHORIZED_401);
			return;
		}

		ctx.getServerHeader().setStatusCode(HttpStatusCodes.OK_200);
		ctx.getServerHeader().setContent("text/plain", "YES".getBytes(StandardCharsets.UTF_8));
	}

}
