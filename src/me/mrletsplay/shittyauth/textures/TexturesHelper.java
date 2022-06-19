package me.mrletsplay.shittyauth.textures;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.config.ShittyAuthSettings;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.config.DefaultSettings;

public class TexturesHelper {

	public static final String
		FALLBACK_HOST = "http://" + Webinterface.getConfig().getSetting(DefaultSettings.HTTP_HOST) + ":" + Webinterface.getConfig().getSetting(DefaultSettings.HTTP_PORT),
		SKIN_PATH = "/skin/%s_%s",
		CAPE_PATH = "/cape/%s_%s";

	private static String getHost() {
		String configHost = ShittyAuth.config.getSetting(ShittyAuthSettings.SKIN_BASE_URL);
		return configHost != null ? configHost : FALLBACK_HOST;
	}

	public static JSONObject getTexturesObject(String accID, UserData userData) {
		JSONObject textures2 = new JSONObject();
		JSONObject skin = new JSONObject();
		skin.put("url", String.format(getHost() + SKIN_PATH, accID, userData.getSkinLastChanged()));
		if(userData.getSkinType() == SkinType.ALEX) {
			JSONObject meta = new JSONObject();
			meta.put("model", "slim");
			skin.put("metadata", meta);
		}
		textures2.put("SKIN", skin);
		if(userData.hasCape()) {
			JSONObject cape = new JSONObject();
			cape.put("url", String.format(getHost() + CAPE_PATH, accID, userData.getCapeLastChanged()));
			textures2.put("CAPE", cape);
		}
		return textures2;
	}

}
