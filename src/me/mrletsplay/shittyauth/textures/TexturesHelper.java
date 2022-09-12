package me.mrletsplay.shittyauth.textures;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.config.DefaultSettings;

public class TexturesHelper {

	public static final String
		SKIN_PATH = "/skin/%s_%s",
		CAPE_PATH = "/cape/%s_%s";

	private static String getSkinBaseURL() {
		return Webinterface.getConfig().getSetting(DefaultSettings.HTTP_BASE_URL);
	}

	public static JSONObject getTexturesObject(String accID, UserData userData) {
		JSONObject textures2 = new JSONObject();
		JSONObject skin = new JSONObject();
		skin.put("url", String.format(getSkinBaseURL() + SKIN_PATH, accID, userData.getSkinLastChanged()));
		if(userData.getSkinType() == SkinType.ALEX) {
			JSONObject meta = new JSONObject();
			meta.put("model", "slim");
			skin.put("metadata", meta);
		}
		textures2.put("SKIN", skin);
		if(userData.hasCape()) {
			JSONObject cape = new JSONObject();
			cape.put("url", String.format(getSkinBaseURL() + CAPE_PATH, accID, userData.getCapeLastChanged()));
			textures2.put("CAPE", cape);
		}
		return textures2;
	}

}
