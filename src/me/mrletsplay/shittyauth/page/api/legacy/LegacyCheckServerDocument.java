package me.mrletsplay.shittyauth.page.api.legacy;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.UUIDHelper;
import me.mrletsplay.webinterfaceapi.http.HttpStatusCodes;
import me.mrletsplay.webinterfaceapi.http.document.HttpDocument;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;
import me.mrletsplay.webinterfaceapi.webinterface.auth.WebinterfaceAccount;
import me.mrletsplay.webinterfaceapi.webinterface.auth.impl.PasswordAuth;

public class LegacyCheckServerDocument implements HttpDocument {
	
	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		Map<String, List<String>> params = ctx.getClientHeader().getPath().getQueryParameters();
		
		if(!params.containsKey("user") || !params.containsKey("serverId")) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.BAD_REQUEST_400);
			ctx.getServerHeader().setContent("text/plain", "Bad Request".getBytes(StandardCharsets.UTF_8));
			return;
		}
		
		String accName = params.get("user").get(0);
		String serverId = params.get("serverId").get(0);
		
		WebinterfaceAccount acc = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(PasswordAuth.ID, accName);
		String joinedServer = ShittyAuth.userServers.get(UUIDHelper.toShortUUID(UUID.fromString(acc.getID())));
		if(joinedServer == null || !joinedServer.equals(serverId)) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.UNAUTHORIZED_401);
			return;
		}
		
		ctx.getServerHeader().setStatusCode(HttpStatusCodes.OK_200);
		ctx.getServerHeader().setContent("text/plain", "YES".getBytes(StandardCharsets.UTF_8));
	}

}
