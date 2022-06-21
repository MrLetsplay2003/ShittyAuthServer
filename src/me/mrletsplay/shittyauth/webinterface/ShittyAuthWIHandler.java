package me.mrletsplay.shittyauth.webinterface;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.textures.SkinType;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.webinterfaceapi.DefaultPermissions;
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

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "uploadSkin")
	public ActionResponse uploadSkin(ActionEvent event) {
		Account acc = event.getAccount();
		AccountConnection con = acc.getConnection(PasswordAuth.ID);
		if(con == null) return ActionResponse.error("No MC account");
		byte[] skinBytes = FileUpload.getUploadedFileBytes(event);
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(skinBytes));
			if(img.getWidth() != 64 || (img.getHeight() != 64 && img.getHeight() != 32)) return ActionResponse.error("Skin must be 64x64 or 64x32 pixels");
			BufferedImage copy = new BufferedImage(64, img.getHeight(), BufferedImage.TYPE_INT_ARGB);
			copy.createGraphics().drawImage(img, 0, 0, null);
			File outFile = new File("shittyauth/skins/" + acc.getID().toString() + ".png");
			IOUtils.createFile(outFile);
			ImageIO.write(copy, "PNG", outFile);
			UserData d = ShittyAuth.dataStorage.getUserData(acc.getID());
			d.setSkinLastChanged(System.currentTimeMillis());
			ShittyAuth.dataStorage.updateUserData(acc.getID(), d);
			return ActionResponse.success();
		}catch(IOException e) {
			return ActionResponse.error("Invalid skin file");
		}
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "uploadCape")
	public ActionResponse uploadCape(ActionEvent event) {
		Account acc = event.getAccount();
		AccountConnection con = acc.getConnection(PasswordAuth.ID);
		if(con == null) return ActionResponse.error("No MC account");
		byte[] capeBytes = FileUpload.getUploadedFileBytes(event);
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(capeBytes));
			if(img.getWidth() != 64 || img.getHeight() != 32) return ActionResponse.error("Cape must be 64x32");
			BufferedImage copy = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
			copy.createGraphics().drawImage(img, 0, 0, null);
			File outFile = new File("shittyauth/capes/" + acc.getID().toString() + ".png");
			IOUtils.createFile(outFile);
			ImageIO.write(copy, "PNG", outFile);
			UserData d = ShittyAuth.dataStorage.getUserData(acc.getID());
			d.setCapeLastChanged(System.currentTimeMillis());
			ShittyAuth.dataStorage.updateUserData(acc.getID(), d);
			return ActionResponse.success();
		}catch(IOException e) {
			return ActionResponse.error("Invalid cape file");
		}
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "setSkinType")
	public ActionResponse setSkinType(ActionEvent event) {
		Account acc = event.getAccount();
		AccountConnection con = acc.getConnection(PasswordAuth.ID);
		if(con == null) return ActionResponse.error("No MC account");
		SkinType type = SkinType.valueOf(event.getData().getString("type"));
		UserData d = ShittyAuth.dataStorage.getUserData(acc.getID());
		d.setSkinType(type);
		ShittyAuth.dataStorage.updateUserData(acc.getID(), d);
		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "setEnableCape")
	public ActionResponse setEnableCape(ActionEvent event) {
		Account acc = event.getAccount();
		AccountConnection con = acc.getConnection(PasswordAuth.ID);
		if(con == null) return ActionResponse.error("No MC account");
		boolean enable = event.getData().getBoolean("enable");
		UserData d = ShittyAuth.dataStorage.getUserData(acc.getID());
		d.setHasCape(enable);
		ShittyAuth.dataStorage.updateUserData(acc.getID(), d);
		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "shittyauth", requestTypes = "setSetting")
	public ActionResponse setSetting(ActionEvent event) {
		if(!event.getAccount().hasPermission(DefaultPermissions.SETTINGS)) return ActionResponse.error("No permission");
		return SettingsPage.handleSetSettingRequest(ShittyAuth.config, event);
	}

}
