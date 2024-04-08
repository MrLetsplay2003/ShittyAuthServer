package me.mrletsplay.shittyauth.config;

import me.mrletsplay.webinterfaceapi.config.setting.AutoSetting;
import me.mrletsplay.webinterfaceapi.config.setting.AutoSettings;
import me.mrletsplay.webinterfaceapi.config.setting.SettingsCategory;
import me.mrletsplay.webinterfaceapi.config.setting.impl.BooleanSetting;

public class ShittyAuthSettings implements AutoSettings {

	public static final ShittyAuthSettings INSTANCE = new ShittyAuthSettings();

	@AutoSetting
	private static SettingsCategory
		general = new SettingsCategory("General"),
		http = new SettingsCategory("HTTP");

	// General
	public static final BooleanSetting
		ALLOW_REGISTRATION = general.addBoolean("general.allow-registration", true, "Allow creation of Minecraft account", "Does not affect registration of WIAPI accounts");

	// HTTP
	public static final BooleanSetting
		AUTHLIB_INJECTOR_COMPAT = http.addBoolean("http.authlib-injector-compat", false, "Authlib-injector compat (Requires restart)", "Enable compatibility for authlib-injector");

	private ShittyAuthSettings() {}

}
