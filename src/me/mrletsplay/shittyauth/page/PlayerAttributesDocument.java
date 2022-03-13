package me.mrletsplay.shittyauth.page;

import java.nio.charset.StandardCharsets;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.webinterfaceapi.http.document.HttpDocument;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;

public class PlayerAttributesDocument implements HttpDocument {
	
	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		String auth = ctx.getClientHeader().getFields().getFieldValue("Authorization");
		System.out.println(auth);
		
		JSONObject obj = new JSONObject();
		JSONObject privileges = new JSONObject();
		JSONObject enabled = new JSONObject();
		enabled.set("enabled", true);
		JSONObject disabled = new JSONObject();
		disabled.set("enabled", false);
		privileges.set("onlineChat", enabled);
		privileges.set("multiplayerServer", enabled);
		privileges.set("multiplayerRealms", enabled);
		privileges.set("telemetry", disabled);
		obj.put("privileges", privileges);
		
		JSONObject prof = new JSONObject();
		prof.put("profanityFilterOn", false);
		obj.put("profanityFilterPreferences", prof);
		System.out.println(obj.toFancyString());
		ctx.getServerHeader().setContent("application/json", obj.toString().getBytes(StandardCharsets.UTF_8));
	}

}
