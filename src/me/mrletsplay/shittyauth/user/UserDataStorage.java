package me.mrletsplay.shittyauth.user;

import java.io.File;

import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.FileCustomConfig;
import me.mrletsplay.mrcore.config.mapper.JSONObjectMapper;

public class UserDataStorage {
	
	private FileCustomConfig config;
	
	public UserDataStorage() {
		config = ConfigLoader.loadFileConfig(new File("shittyauth/user-data.yml"));
		config.registerMapper(JSONObjectMapper.create(UserData.class));
	}
	
	public void updateUserData(String accID, UserData userData) {
		config.set(accID, userData);
		config.saveToFile();
	}
	
	public UserData getUserData(String accID) {
		return config.getGeneric(accID, UserData.class, new UserData(), false);
	}
	
}
