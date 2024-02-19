package me.mrletsplay.shittyauth.webinterface;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.textures.SkinType;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.shittyauth.util.InvalidSkinException;
import me.mrletsplay.webinterfaceapi.DefaultPermissions;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;
import me.mrletsplay.webinterfaceapi.auth.impl.PasswordAuth;
import me.mrletsplay.webinterfaceapi.page.SettingsPage;
import me.mrletsplay.webinterfaceapi.page.action.ActionEvent;
import me.mrletsplay.webinterfaceapi.page.action.ActionHandler;
import me.mrletsplay.webinterfaceapi.page.action.ActionResponse;
import me.mrletsplay.webinterfaceapi.page.action.WebinterfaceHandler;
import me.mrletsplay.webinterfaceapi.page.element.FileUpload;

public class ShittyAuthWIHandler implements ActionHandler {

	public static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,16}");

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "createAccount")
	public ActionResponse createAccount(ActionEvent event) {
		Account acc = event.getAccount();
		if(acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME) != null) return ActionResponse.error("Account already has a Minecraft account");

		String username = event.getData().getString("username");
		String password = event.getData().getString("password");

		if(!USERNAME_PATTERN.matcher(username).matches()) return ActionResponse.error("Invalid username");

		if(ShittyAuth.getAccountByUsername(username) != null) return ActionResponse.error("An account with that username already exists");

		UUID uuid = UUID.randomUUID();
		AccountConnection con = new AccountConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME, uuid.toString(), username, null, null);
		acc.addConnection(con);
		Webinterface.getCredentialsStorage().storeCredentials(ShittyAuth.ACCOUNT_CONNECTION_NAME, uuid.toString(), password);
		ShittyAuth.dataStorage.updateUserData(con.getUserID(), UserData.createNew());

		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "uploadSkin")
	public ActionResponse uploadSkin(ActionEvent event) {
		Account acc = event.getAccount();
		AccountConnection con = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(con == null) return ActionResponse.error("No Minecraft account");
		byte[] skinBytes = FileUpload.getUploadedFileBytes(event);
		if(skinBytes.length == 0) return ActionResponse.error("No file provided");
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(skinBytes));
			if(img == null) return ActionResponse.error("Invalid image file");
			ShittyAuth.updateUserSkin(con.getUserID(), img);
			return ActionResponse.success();
		}catch(IOException e) {
			return ActionResponse.error("Invalid skin file");
		} catch (InvalidSkinException e) {
			return ActionResponse.error(e.getMessage());
		}
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "uploadCape")
	public ActionResponse uploadCape(ActionEvent event) {
		Account acc = event.getAccount();
		AccountConnection con = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(con == null) return ActionResponse.error("No MC account");
		byte[] capeBytes = FileUpload.getUploadedFileBytes(event);
		if(capeBytes.length == 0) return ActionResponse.error("No file provided");
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(capeBytes));
			if(img == null) return ActionResponse.error("Invalid image file");
			ShittyAuth.updateUserCape(con.getUserID(), img);
			return ActionResponse.success();
		}catch(IOException e) {
			return ActionResponse.error("Invalid cape file");
		} catch (InvalidSkinException e) {
			return ActionResponse.error(e.getMessage());
		}
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "setSkinType")
	public ActionResponse setSkinType(ActionEvent event) {
		Account acc = event.getAccount();
		AccountConnection con = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(con == null) return ActionResponse.error("No Minecraft account");
		SkinType type = SkinType.valueOf(event.getData().getString("type"));
		UserData d = ShittyAuth.dataStorage.getUserData(con.getUserID());
		d.setSkinType(type);
		ShittyAuth.dataStorage.updateUserData(con.getUserID(), d);
		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "setEnableCape")
	public ActionResponse setEnableCape(ActionEvent event) {
		Account acc = event.getAccount();
		AccountConnection con = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(con == null) return ActionResponse.error("No Minecraft account");
		boolean enable = event.getData().getBoolean("enable");
		UserData d = ShittyAuth.dataStorage.getUserData(con.getUserID());
		d.setHasCape(enable);
		ShittyAuth.dataStorage.updateUserData(con.getUserID(), d);
		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "resetPassword")
	public ActionResponse resetPassword(ActionEvent event) {
		Account acc = event.getAccount();
		AccountConnection con = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(con == null) return ActionResponse.error("No Minecraft account");

		String password = event.getData().getString("password");
		Webinterface.getCredentialsStorage().storeCredentials(ShittyAuth.ACCOUNT_CONNECTION_NAME, con.getUserID(), password);
		ShittyAuth.tokenStorage.removeTokensByAccountID(con.getUserID()); // Invalidate all sessions

		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "changeMCUsername", permission = DefaultPermissions.MODIFY_USERS)
	public ActionResponse changeMCUsername(ActionEvent event) {
		String accountID = event.getData().getString("account");
		String username = event.getData().getString("username");

		Account account = Webinterface.getAccountStorage().getAccountByID(accountID);
		if(account == null) return ActionResponse.error("Account doesn't exist");

		AccountConnection connection = account.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(connection == null) return ActionResponse.error("No Minecraft account");

		AccountConnection newConnection = new AccountConnection(connection.getConnectionName(), connection.getUserID(), username, connection.getUserEmail(), connection.getUserAvatar());
		account.removeConnection(connection);
		account.addConnection(newConnection);

		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "changeMCPassword", permission = DefaultPermissions.MODIFY_USERS)
	public ActionResponse changeMCPassword(ActionEvent event) {
		String accountID = event.getData().getString("account");
		String password = event.getData().getString("password");

		Account account = Webinterface.getAccountStorage().getAccountByID(accountID);
		if(account == null) return ActionResponse.error("Account doesn't exist");

		AccountConnection connection = account.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(connection == null) return ActionResponse.error("No Minecraft account");

		Webinterface.getCredentialsStorage().storeCredentials(ShittyAuth.ACCOUNT_CONNECTION_NAME, connection.getUserID(), password);
		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "changeWIAPIPassword", permission = DefaultPermissions.MODIFY_USERS)
	public ActionResponse changeWIAPIPassword(ActionEvent event) {
		String accountID = event.getData().getString("account");
		String password = event.getData().getString("password");

		Account account = Webinterface.getAccountStorage().getAccountByID(accountID);
		if(account == null) return ActionResponse.error("Account doesn't exist");

		AccountConnection connection = account.getConnection(PasswordAuth.ID);
		if(connection == null) return ActionResponse.error("Not a password-based account");

		Webinterface.getCredentialsStorage().storeCredentials(PasswordAuth.ID, connection.getUserID(), password);
		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "setSetting", permission = DefaultPermissions.SETTINGS)
	public ActionResponse setSetting(ActionEvent event) {
		return SettingsPage.handleSetSettingRequest(ShittyAuth.config, event);
	}

}
