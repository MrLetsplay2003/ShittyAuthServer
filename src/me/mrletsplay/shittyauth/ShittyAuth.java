package me.mrletsplay.shittyauth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import me.mrletsplay.shittyauth.auth.AccessTokenStorage;
import me.mrletsplay.shittyauth.config.ShittyAuthSettings;
import me.mrletsplay.shittyauth.page.AuthenticatePage;
import me.mrletsplay.shittyauth.page.HasJoinedPage;
import me.mrletsplay.shittyauth.page.JoinPage;
import me.mrletsplay.shittyauth.page.PlayerAttributesDocument;
import me.mrletsplay.shittyauth.page.SettingsPage;
import me.mrletsplay.shittyauth.page.UserCapeDocument;
import me.mrletsplay.shittyauth.page.UserSkinDocument;
import me.mrletsplay.shittyauth.page.ValidatePage;
import me.mrletsplay.shittyauth.user.UserDataStorage;
import me.mrletsplay.shittyauth.webinterface.MCAccountPage;
import me.mrletsplay.shittyauth.webinterface.ShittyAuthWIHandler;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;
import me.mrletsplay.webinterfaceapi.webinterface.config.WebinterfaceFileConfig;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageCategory;

public class ShittyAuth {
	
	public static PrivateKey privateKey;
	public static AccessTokenStorage tokenStorage;
	public static UserDataStorage dataStorage;
	public static WebinterfaceFileConfig config;
	public static Map<String, String> userServers = new HashMap<>();
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		Webinterface.setDocumentProvider(ShittyAuthDocumentProvider.INSTANCE);
		Webinterface.start();
		
		tokenStorage = new AccessTokenStorage();
		dataStorage = new UserDataStorage();
		config = new WebinterfaceFileConfig(new File("shittyauth/shittyauth.yml"));
		config.registerSettings(ShittyAuthSettings.INSTANCE);

		File privateKeyFile = new File("shittyauth/private_key.der");
		if(!privateKeyFile.exists()) {
			File publicKeyFile = new File("shittyauth/public_key.der");
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(4096);
			
			KeyPair pair = gen.generateKeyPair();
			PrivateKey priv = pair.getPrivate();
			PublicKey pub = pair.getPublic();
			
			Files.write(publicKeyFile.toPath(), pub.getEncoded(), StandardOpenOption.CREATE);
			Files.write(privateKeyFile.toPath(), priv.getEncoded(), StandardOpenOption.CREATE);
			
			Webinterface.getLogger().info("Generated a new key pair");
		}
		
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Files.readAllBytes(privateKeyFile.toPath()));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKey = keyFactory.generatePrivate(spec);
		
		Webinterface.getDocumentProvider().registerDocument("/session/minecraft/hasJoined", new HasJoinedPage());
		Webinterface.getDocumentProvider().registerDocument("/session/minecraft/join", new JoinPage());
		Webinterface.getDocumentProvider().registerDocument("/authenticate", new AuthenticatePage());
		Webinterface.getDocumentProvider().registerDocument("/validate", new ValidatePage());
		Webinterface.getDocumentProvider().registerDocument("/player/attributes", new PlayerAttributesDocument());
		Webinterface.getDocumentProvider().registerDocument(UserSkinDocument.PATH, new UserSkinDocument());
		Webinterface.getDocumentProvider().registerDocument(UserCapeDocument.PATH, new UserCapeDocument());
		Webinterface.getDocumentProvider().registerFileDocument("/yggdrasil_session_pubkey.der", new File("shittyauth/public_key.der"));
		
		Webinterface.registerActionHandler(new ShittyAuthWIHandler());
		
		WebinterfacePageCategory cat = Webinterface.createCategory("Minecraft");
		cat.addPage(new MCAccountPage());
		cat.addPage(new SettingsPage());
	}

}
