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
import me.mrletsplay.shittyauth.auth.FileAccessTokenStorage;
import me.mrletsplay.shittyauth.auth.SQLAccessTokenStorage;
import me.mrletsplay.shittyauth.config.ShittyAuthSettings;
import me.mrletsplay.shittyauth.page.AccountPage;
import me.mrletsplay.shittyauth.page.CreateAccountPage;
import me.mrletsplay.shittyauth.page.SettingsPage;
import me.mrletsplay.shittyauth.page.api.UserCapeDocument;
import me.mrletsplay.shittyauth.page.api.UserSkinDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyCheckServerDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyJoinServerDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyUserCapeDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyUserSkinDocument;
import me.mrletsplay.shittyauth.page.api.services.PlayerAttributesDocument;
import me.mrletsplay.shittyauth.page.api.services.PlayerCertificatesDocument;
import me.mrletsplay.shittyauth.page.api.services.PlayerReportDocument;
import me.mrletsplay.shittyauth.page.api.yggdrasil.AuthenticatePage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.HasJoinedPage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.JoinPage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.ProfilePage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.ValidatePage;
import me.mrletsplay.shittyauth.user.FileUserDataStorage;
import me.mrletsplay.shittyauth.user.SQLUserDataStorage;
import me.mrletsplay.shittyauth.user.UserDataStorage;
import me.mrletsplay.shittyauth.webinterface.ShittyAuthWIHandler;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;
import me.mrletsplay.webinterfaceapi.config.DefaultSettings;
import me.mrletsplay.webinterfaceapi.config.FileConfig;
import me.mrletsplay.webinterfaceapi.page.PageCategory;
import me.mrletsplay.webinterfaceapi.sql.SQLHelper;

public class ShittyAuth {

	public static final String ACCOUNT_CONNECTION_NAME = "shittyauth";

	public static PrivateKey privateKey;
	public static AccessTokenStorage tokenStorage;
	public static UserDataStorage dataStorage;
	public static FileConfig config;
	public static Map<String, String> userServers = new HashMap<>();

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		DefaultSettings.HOME_PAGE_PATH.defaultValue(AccountPage.PATH);
		Webinterface.start();
		Webinterface.extractResources("/shittyauth-resources.list");

		if(SQLHelper.isAvailable()) {
			tokenStorage = new SQLAccessTokenStorage();
			dataStorage = new SQLUserDataStorage();
		}else {
			tokenStorage = new FileAccessTokenStorage();
			dataStorage = new FileUserDataStorage();
		}

		tokenStorage.initialize();
		dataStorage.initialize();

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
		Webinterface.getDocumentProvider().registerDocument("/player/certificates", new PlayerCertificatesDocument());
		Webinterface.getDocumentProvider().registerDocument("/player/report", new PlayerReportDocument());
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
		cat.addPage(new AccountPage());
		cat.addPage(new CreateAccountPage());
		cat.addPage(new SettingsPage());
	}

	public static Account getAccountByUsername(String username) {
		// TODO: more efficient implementation?
		for(Account a : Webinterface.getAccountStorage().getAccounts()) {
			AccountConnection con = a.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
			if(con != null && con.getUserName().equals(username)) {
				return a;
			}
		}
		return null;
	}

}
