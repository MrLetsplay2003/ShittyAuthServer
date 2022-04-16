package me.mrletsplay.shittyauth.page.api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import me.mrletsplay.webinterfaceapi.http.HttpStatusCodes;
import me.mrletsplay.webinterfaceapi.http.document.HttpDocument;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;
import me.mrletsplay.webinterfaceapi.webinterface.auth.WebinterfaceAccount;

public class UserSkinDocument implements HttpDocument {
	
	public static final UserSkinDocument INSTANCE = new UserSkinDocument();

	public static final String PATH_PREFIX = "/skin/s";
	
	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		String accID = ctx.getClientHeader().getPath().getDocumentPath().substring(PATH_PREFIX.length());
		if(accID.contains("_")) accID = accID.substring(0, accID.indexOf("_"));
		WebinterfaceAccount acc = Webinterface.getAccountStorage().getAccountByID(accID);
		if(acc == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.NOT_FOUND_404);
			ctx.getServerHeader().setContent("text/plain", "404 Not Found".getBytes(StandardCharsets.UTF_8));
			return;
		}
		
		File f = new File("shittyauth/skins/", accID + ".png");
		if(!f.exists()) f = new File("shittyauth/default_skin.png");
		
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
