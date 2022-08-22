package me.mrletsplay.shittyauth.user;

import java.io.File;

import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.FileCustomConfig;
import me.mrletsplay.mrcore.config.mapper.JSONObjectMapper;
import me.mrletsplay.webinterfaceapi.Webinterface;

public class FileUserDataStorage implements UserDataStorage {

	private FileCustomConfig config;

	@Override
	public void initialize() {
		config = ConfigLoader.loadFileConfig(new File(Webinterface.getDataDirectory(), "shittyauth/user-data.yml"));
		config.registerMapper(JSONObjectMapper.create(UserData.class));
	}

	@Override
	public void updateUserData(String accID, UserData userData) {
		config.set(accID, userData);
		config.saveToFile();
	}

	@Override
	public UserData getUserData(String accID) {
		return config.getGeneric(accID, UserData.class, null, false);
	}

}
