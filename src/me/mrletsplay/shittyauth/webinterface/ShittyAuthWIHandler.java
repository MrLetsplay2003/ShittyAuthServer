package me.mrletsplay.shittyauth.webinterface;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.textures.SkinType;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.webinterfaceapi.DefaultPermissions;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;
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
			if(img.getWidth() != 64 || (img.getHeight() != 64 && img.getHeight() != 32)) return ActionResponse.error("Skin must be 64x64 or 64x32 pixels");
			BufferedImage copy = new BufferedImage(64, img.getHeight(), BufferedImage.TYPE_INT_ARGB);
			copy.createGraphics().drawImage(img, 0, 0, null);
			File outFile = new File("shittyauth/skins/" + con.getUserID().toString() + ".png");
			IOUtils.createFile(outFile);
			ImageIO.write(copy, "PNG", outFile);
			UserData d = ShittyAuth.dataStorage.getUserData(con.getUserID());
			d.setSkinLastChanged(System.currentTimeMillis());
			ShittyAuth.dataStorage.updateUserData(con.getUserID(), d);
			return ActionResponse.success();
		}catch(IOException e) {
			return ActionResponse.error("Invalid skin file");
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
			if(img.getWidth() != 64 || img.getHeight() != 32) return ActionResponse.error("Cape must be 64x32");
			BufferedImage copy = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
			copy.createGraphics().drawImage(img, 0, 0, null);
			File outFile = new File("shittyauth/capes/" + con.getUserID().toString() + ".png");
			IOUtils.createFile(outFile);
			ImageIO.write(copy, "PNG", outFile);
			UserData d = ShittyAuth.dataStorage.getUserData(con.getUserID());
			d.setCapeLastChanged(System.currentTimeMillis());
			ShittyAuth.dataStorage.updateUserData(con.getUserID(), d);
			return ActionResponse.success();
		}catch(IOException e) {
			return ActionResponse.error("Invalid cape file");
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

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "setSetting")
	public ActionResponse setSetting(ActionEvent event) {
		if(!event.getAccount().hasPermission(DefaultPermissions.SETTINGS)) return ActionResponse.error("No permission");
		return SettingsPage.handleSetSettingRequest(ShittyAuth.config, event);
	}

}
