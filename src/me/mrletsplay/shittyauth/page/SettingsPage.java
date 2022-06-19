package me.mrletsplay.shittyauth.page;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.config.ShittyAuthSettings;
import me.mrletsplay.webinterfaceapi.DefaultPermissions;
import me.mrletsplay.webinterfaceapi.page.Page;
import me.mrletsplay.webinterfaceapi.page.PageSection;
import me.mrletsplay.webinterfaceapi.page.element.SettingsPane;

public class SettingsPage extends Page {

	public static final String URL = "/mc/settings";

	public SettingsPage() {
		super("Settings", URL, DefaultPermissions.SETTINGS);
		setIcon("mdi:cog");

		PageSection sc2 = new PageSection();
		sc2.setSlimLayout(true);
		sc2.addTitle("Settings");
		sc2.addElement(new SettingsPane(() -> ShittyAuth.config, ShittyAuthSettings.INSTANCE.getSettingsCategories(), "shittyauth", "setSetting"));

		addSection(sc2);
	}

}
