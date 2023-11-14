package me.mrletsplay.shittyauth.page.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.simplehttpserver.http.util.MimeType;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;

public class UserSkinDocument implements HttpDocument {

	public static final UserSkinDocument INSTANCE = new UserSkinDocument();

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		String accID = ctx.getPathParameters().get("uuid");
		if(accID.contains("_")) accID = accID.substring(0, accID.indexOf("_"));
		Account acc = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(ShittyAuth.ACCOUNT_CONNECTION_NAME, accID);
		if(acc == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NOT_FOUND_404);
			ctx.getServerHeader().setContent(MimeType.TEXT, "404 Not Found".getBytes(StandardCharsets.UTF_8));
			return;
		}

		try {
			byte[] bytes = ShittyAuth.loadUserSkinRaw(accID);
			ctx.getServerHeader().setContent(MimeType.PNG, bytes);
			ctx.getServerHeader().getFields().set("Content-Type", "image/png");
		} catch (IOException e) {
			Webinterface.getLogger().error("Failed to load skin", e);
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.INTERNAL_SERVER_ERROR_500);
			ctx.getServerHeader().setContent(MimeType.TEXT, "500 Internal Server error".getBytes(StandardCharsets.UTF_8));
			return;
		}
	}

}
