package me.mrletsplay.shittyauth.page.api.authlibinjector;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.util.CryptoHelper;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.config.DefaultSettings;

public class AuthLibInjectorMetadataDocument implements HttpDocument {

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONObject obj = new JSONObject();
		JSONObject meta = new JSONObject();
		meta.put("implementationName", "ShittyAuthServer");
		meta.put("implementationVersion", "69.420");
		meta.put("serverName", "ShittyAuthServer");
		JSONObject links = new JSONObject();
		links.put("homepage", Webinterface.getConfig().getSetting(DefaultSettings.HTTP_BASE_URL));
		links.put("register", Webinterface.getConfig().getSetting(DefaultSettings.HTTP_BASE_URL));
		meta.put("links", links);

		meta.put("feature.legacy_skin_api", true);
		meta.put("feature.no_mojang_namespace", true);
		meta.put("feature.enable_profile_key", true);
		meta.put("feature.username_check", true);
		obj.put("meta", meta);

		JSONArray skinDomains = new JSONArray();
		skinDomains.add(URI.create(Webinterface.getConfig().getSetting(DefaultSettings.HTTP_BASE_URL)).getHost());
		obj.put("skinDomains", skinDomains);

		obj.put("signaturePublicKey", CryptoHelper.encodeRSAPublicKey(ShittyAuth.publicKey));
		ctx.getServerHeader().setContent("application/json", obj.toString().getBytes(StandardCharsets.UTF_8));
	}

}
