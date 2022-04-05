package me.mrletsplay.shittyauth.page;

import java.util.UUID;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.UUIDHelper;
import me.mrletsplay.webinterfaceapi.http.HttpStatusCodes;
import me.mrletsplay.webinterfaceapi.http.document.HttpDocument;
import me.mrletsplay.webinterfaceapi.http.header.HttpClientContentTypes;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;

public class JoinPage implements HttpDocument {
	
	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		
		JSONObject authData = (JSONObject) ctx.getClientHeader().getPostData().getParsedAs(HttpClientContentTypes.JSON);
		String accessToken = authData.getString("accessToken");
		
		String accID = ShittyAuth.tokenStorage.getAccountID(accessToken);
		String shortUUID = accID == null ? "" : UUIDHelper.toShortUUID(UUID.fromString(accID));
		if(accID == null || !shortUUID.equals(authData.getString("selectedProfile"))) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.UNAUTHORIZED_401);
			return;
		}
		
		ShittyAuth.userServers.put(shortUUID, authData.getString("serverId"));
		ctx.getServerHeader().setStatusCode(HttpStatusCodes.NO_CONTENT_204);
		ctx.getServerHeader().setContent(new byte[0]);
	}

}
