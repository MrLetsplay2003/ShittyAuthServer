package me.mrletsplay.shittyauth.page.api.services;

import java.nio.charset.StandardCharsets;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;

public class PlayerReportDocument implements HttpDocument {

	@Override
	public void createContent() {
		// TODO: currently not supported
		HttpRequestContext.getCurrentContext().getServerHeader().setStatusCode(HttpStatusCodes.BAD_REQUEST_400);
		JSONObject error = new JSONObject();
		error.put("errorMessage", "Chat reporting is not supported by ShittyAuthServer");
		HttpRequestContext.getCurrentContext().getServerHeader().setContent("text/plain", error.toString().getBytes(StandardCharsets.UTF_8));
	}

}
