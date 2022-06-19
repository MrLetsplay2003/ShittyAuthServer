package me.mrletsplay.shittyauth.page.api.legacy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.impl.PasswordAuth;

public class LegacyUserCapeDocument implements HttpDocument {

	public static final String PATH_PREFIX = "/MinecraftCloaks/";
	public static final LegacyUserCapeDocument INSTANCE = new LegacyUserCapeDocument();

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		String username = ctx.getPathParameters().get("name");
		if(username.endsWith(".png")) username = username.substring(0, username.length() - ".png".length());
		Account acc = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(PasswordAuth.ID, username);
		if(acc == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NOT_FOUND_404);
			ctx.getServerHeader().setContent("text/plain", "404 Not Found".getBytes(StandardCharsets.UTF_8));
			return;
		}

		File f = new File("shittyauth/capes/", acc.getID() + ".png");
		if(!f.exists()) f = new File("include/default_cape.png");

		try {
			byte[] bytes = Files.readAllBytes(f.toPath());
			ctx.getServerHeader().setContent(bytes);
			ctx.getServerHeader().getFields().set("Content-Type", "image/png");
		} catch (IOException e) {
			Webinterface.getLogger().error("Failed to load cape", e);
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.INTERNAL_SERVER_ERROR_500);
			ctx.getServerHeader().setContent("text/plain", "500 Internal Server error".getBytes(StandardCharsets.UTF_8));
			return;
		}
	}

}
