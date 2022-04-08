package me.mrletsplay.shittyauth.page.api.legacy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import me.mrletsplay.webinterfaceapi.http.HttpStatusCodes;
import me.mrletsplay.webinterfaceapi.http.document.HttpDocument;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;
import me.mrletsplay.webinterfaceapi.webinterface.auth.WebinterfaceAccount;
import me.mrletsplay.webinterfaceapi.webinterface.auth.impl.PasswordAuth;

public class LegacyUserCapeDocument implements HttpDocument {

	public static final String PATH_PREFIX = "/MinecraftCloaks/";
	public static final LegacyUserCapeDocument INSTANCE = new LegacyUserCapeDocument();

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		String username = ctx.getClientHeader().getPath().getDocumentPath().substring(PATH_PREFIX.length());
		if(username.endsWith(".png")) username = username.substring(0, username.length() - ".png".length());
		WebinterfaceAccount acc = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(PasswordAuth.ID, username);
		if(acc == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NOT_FOUND_404);
			ctx.getServerHeader().setContent("text/plain", "404 Not Found".getBytes(StandardCharsets.UTF_8));
			return;
		}

		File f = new File("shittyauth/capes/", acc.getID() + ".png");
		if(!f.exists()) f = new File("shittyauth/default_cape.png");
		
		try {
			byte[] bytes = Files.readAllBytes(f.toPath());
			ctx.getServerHeader().setContent(bytes);
			ctx.getServerHeader().getFields().setFieldValue("Content-Type", "image/png");
		} catch (IOException e) {
			e.printStackTrace();
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.INTERNAL_SERVER_ERROR_500);
			ctx.getServerHeader().setContent("text/plain", "500 Internal Server error".getBytes(StandardCharsets.UTF_8));
			return;
		}
	}

}
