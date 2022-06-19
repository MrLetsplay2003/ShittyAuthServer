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
import me.mrletsplay.shittyauth.page.ProfilePage;
import me.mrletsplay.shittyauth.page.SettingsPage;
import me.mrletsplay.shittyauth.page.api.PlayerAttributesDocument;
import me.mrletsplay.shittyauth.page.api.UserCapeDocument;
import me.mrletsplay.shittyauth.page.api.UserSkinDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyCheckServerDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyJoinServerDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyUserCapeDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyUserSkinDocument;
import me.mrletsplay.shittyauth.page.api.yggdrasil.AuthenticatePage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.HasJoinedPage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.JoinPage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.ValidatePage;
import me.mrletsplay.shittyauth.user.UserDataStorage;
import me.mrletsplay.shittyauth.webinterface.MCAccountPage;
import me.mrletsplay.shittyauth.webinterface.ShittyAuthWIHandler;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.config.FileConfig;
import me.mrletsplay.webinterfaceapi.page.PageCategory;

public class ShittyAuth {

	public static PrivateKey privateKey;
	public static AccessTokenStorage tokenStorage;
	public static UserDataStorage dataStorage;
	public static FileConfig config;
	public static Map<String, String> userServers = new HashMap<>();

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		Webinterface.start();
		Webinterface.extractResources("/shittyauth-resources.list");

		tokenStorage = new AccessTokenStorage();
		dataStorage = new UserDataStorage();
		config = new FileConfig(new File("shittyauth/shittyauth.yml"));
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
		PlayerAttributesDocument doc = new PlayerAttributesDocument();
		Webinterface.getDocumentProvider().registerDocument("/player/attributes", doc);
		Webinterface.getDocumentProvider().registerDocument("/privileges", doc); // for MC 1.16 or older
		Webinterface.getDocumentProvider().registerFileDocument("/yggdrasil_session_pubkey.der", new File("shittyauth/public_key.der"));

		Webinterface.getDocumentProvider().registerDocument("/game/joinserver.jsp", new LegacyJoinServerDocument());
		Webinterface.getDocumentProvider().registerDocument("/game/checkserver.jsp", new LegacyCheckServerDocument());
		Webinterface.getDocumentProvider().registerDocumentPattern("/cape/{uuid}", UserCapeDocument.INSTANCE);
		Webinterface.getDocumentProvider().registerDocumentPattern("/skin/{uuid}", UserSkinDocument.INSTANCE);
		Webinterface.getDocumentProvider().registerDocumentPattern("/session/minecraft/profile/{uuid}", ProfilePage.INSTANCE);
		Webinterface.getDocumentProvider().registerDocumentPattern("/MinecraftSkins/{name}", LegacyUserSkinDocument.INSTANCE);
		Webinterface.getDocumentProvider().registerDocumentPattern("/MinecraftCapes/{name}", LegacyUserCapeDocument.INSTANCE);

		Webinterface.registerActionHandler(new ShittyAuthWIHandler());

		PageCategory cat = Webinterface.createCategory("Minecraft");
		cat.addPage(new MCAccountPage());
		cat.addPage(new SettingsPage());
	}

}
