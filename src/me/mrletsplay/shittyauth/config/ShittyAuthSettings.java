package me.mrletsplay.shittyauth.config;

import me.mrletsplay.webinterfaceapi.webinterface.config.setting.AutoSetting;
import me.mrletsplay.webinterfaceapi.webinterface.config.setting.AutoSettings;
import me.mrletsplay.webinterfaceapi.webinterface.config.setting.SettingsCategory;
import me.mrletsplay.webinterfaceapi.webinterface.config.setting.impl.StringSetting;

public class ShittyAuthSettings implements AutoSettings {
	
	public static final ShittyAuthSettings INSTANCE = new ShittyAuthSettings();
	
	@AutoSetting
	private static SettingsCategory
		http = new SettingsCategory("HTTP");
	
	// HTTP
	public static final StringSetting
		SKIN_BASE_URL = http.addString("http.skin-base-url", null, "Skin base URL", "Base URL to use for skin/cape URLs (uses default HTTP host if not set)");
	
	private ShittyAuthSettings() {}
	
}
