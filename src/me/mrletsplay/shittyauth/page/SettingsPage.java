package me.mrletsplay.shittyauth.page;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.config.ShittyAuthSettings;
import me.mrletsplay.webinterfaceapi.webinterface.DefaultPermissions;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSettingsPane;

public class SettingsPage extends WebinterfacePage {
	
	public static final String URL = "/mc/settings";

	public SettingsPage() {
		super("Settings", URL, DefaultPermissions.SETTINGS);
		setIcon("mdi:cog");
		
		WebinterfacePageSection sc2 = new WebinterfacePageSection();
		sc2.setSlimLayout(true);
		sc2.addTitle("Settings");
		sc2.addElement(new WebinterfaceSettingsPane(() -> ShittyAuth.config, ShittyAuthSettings.INSTANCE.getSettingsCategories(), "shittyauth", "setSetting"));
		
		addSection(sc2);
	}
	
}
