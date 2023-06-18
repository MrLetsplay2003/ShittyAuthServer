package me.mrletsplay.shittyauth.page.api.yggdrasil;

import java.nio.charset.StandardCharsets;

import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;

public class PublicKeysPage implements HttpDocument {

	@Override
	public void createContent() {
		// TODO: Needs to be implemented. Not sure how important the endpoint is, an empty response seems to suffice for the server to not complain
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		ctx.getServerHeader().setStatusCode(HttpStatusCodes.OK_200);
		ctx.getServerHeader().setContent("{}".getBytes(StandardCharsets.UTF_8));
	}

}
