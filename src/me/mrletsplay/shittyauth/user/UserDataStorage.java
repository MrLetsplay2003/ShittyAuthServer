package me.mrletsplay.shittyauth.user;

public interface UserDataStorage {

	public void initialize();

	public void updateUserData(String accID, UserData userData);

	public UserData getUserData(String accID);

}