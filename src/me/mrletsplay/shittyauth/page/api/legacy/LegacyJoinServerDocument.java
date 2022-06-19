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
		Account acc = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(PasswordAuth.ID, accName);
		if(accID == null || !acc.getID().equals(accID)) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.UNAUTHORIZED_401);
			return;
		}

		ShittyAuth.userServers.put(UUIDHelper.toShortUUID(UUID.fromString(acc.getID())), serverId);
		ctx.getServerHeader().setStatusCode(HttpStatusCodes.OK_200);
		ctx.getServerHeader().setContent("text/plain", "ok".getBytes(StandardCharsets.UTF_8));
	}

}
