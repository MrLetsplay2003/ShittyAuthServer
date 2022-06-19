package me.mrletsplay.shittyauth.page.api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;

public class UserSkinDocument implements HttpDocument {

	public static final UserSkinDocument INSTANCE = new UserSkinDocument();

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		String accID = ctx.getPathParameters().get("uuid");
		if(accID.contains("_")) accID = accID.substring(0, accID.indexOf("_"));
		Account acc = Webinterface.getAccountStorage().getAccountByID(accID);
		if(acc == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NOT_FOUND_404);
			ctx.getServerHeader().setContent("text/plain", "404 Not Found".getBytes(StandardCharsets.UTF_8));
			return;
		}

		File f = new File("shittyauth/skins/", accID + ".png");
		if(!f.exists()) f = new File("include/default_skin.png");

		try {
			byte[] bytes = Files.readAllBytes(f.toPath());
			ctx.getServerHeader().setContent(bytes);
			ctx.getServerHeader().getFields().set("Content-Type", "image/png");
		} catch (IOException e) {
			Webinterface.getLogger().error("Failed to load skin", e);
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.INTERNAL_SERVER_ERROR_500);
			ctx.getServerHeader().setContent("text/plain", "500 Internal Server error".getBytes(StandardCharsets.UTF_8));
			return;
		}
	}

}
