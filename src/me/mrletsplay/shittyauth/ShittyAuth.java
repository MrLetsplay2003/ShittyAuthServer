package me.mrletsplay.shittyauth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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
import me.mrletsplay.shittyauth.page.api.authlibinjector.AuthLibInjectorDocument;
import me.mrletsplay.shittyauth.page.api.authlibinjector.AuthLibInjectorMetadataDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyCheckServerDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyJoinServerDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyUserCapeDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyUserSkinDocument;
import me.mrletsplay.shittyauth.page.api.services.PlayerAttributesDocument;
import me.mrletsplay.shittyauth.page.api.services.PlayerCertificatesDocument;
import me.mrletsplay.shittyauth.page.api.services.PlayerReportDocument;
import me.mrletsplay.shittyauth.page.api.yggdrasil.AuthenticatePage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.HasJoinedPage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.InvalidatePage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.JoinPage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.ProfilePage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.PublicKeysPage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.RefreshPage;
import me.mrletsplay.shittyauth.page.api.yggdrasil.ValidatePage;
import me.mrletsplay.shittyauth.user.FileUserDataStorage;
import me.mrletsplay.shittyauth.user.SQLUserDataStorage;
import me.mrletsplay.shittyauth.user.UserDataStorage;
import me.mrletsplay.shittyauth.webinterface.ShittyAuthWIHandler;
import me.mrletsplay.simplehttpserver.http.HttpRequestMethod;
import me.mrletsplay.simplehttpserver.http.document.DefaultDocumentProvider;
import me.mrletsplay.simplehttpserver.http.document.DocumentProvider;
import me.mrletsplay.simplehttpserver.http.document.FileDocument;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;
import me.mrletsplay.webinterfaceapi.config.DefaultSettings;
import me.mrletsplay.webinterfaceapi.config.FileConfig;
import me.mrletsplay.webinterfaceapi.page.PageCategory;
import me.mrletsplay.webinterfaceapi.sql.SQLHelper;

public class ShittyAuth {

	public static final String ACCOUNT_CONNECTION_NAME = "shittyauth";

	public static PublicKey publicKey;
	public static PrivateKey privateKey;
	public static AccessTokenStorage tokenStorage;
	public static UserDataStorage dataStorage;
	public static FileConfig config;
	public static Map<String, String> userServers = new HashMap<>();

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		DefaultSettings.HOME_PAGE_PATH.defaultValue(AccountPage.PATH);

		DocumentProvider proxy = new DefaultDocumentProvider() {

			@Override
			public HttpDocument get(HttpRequestMethod arg0, String arg1) {
				System.out.println(arg0 + " " + arg1);
				return super.get(arg0, arg1);
			}

		};
		Webinterface.setDocumentProvider(proxy);

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

		File publicKeyFile = new File("shittyauth/public_key.der");
		File privateKeyFile = new File("shittyauth/private_key.der");
		if(!privateKeyFile.exists() || !publicKeyFile.exists()) {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(4096);

			KeyPair pair = gen.generateKeyPair();
			PrivateKey priv = pair.getPrivate();
			PublicKey pub = pair.getPublic();

			Files.write(publicKeyFile.toPath(), pub.getEncoded(), StandardOpenOption.CREATE);
			Files.write(privateKeyFile.toPath(), priv.getEncoded(), StandardOpenOption.CREATE);

			Webinterface.getLogger().info("Generated a new key pair");
		}

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");

		X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Files.readAllBytes(publicKeyFile.toPath()));
		publicKey = keyFactory.generatePublic(pubSpec);
		PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Files.readAllBytes(privateKeyFile.toPath()));
		privateKey = keyFactory.generatePrivate(privSpec);

		DocumentProvider provider = Webinterface.getDocumentProvider();

		provider.register(HttpRequestMethod.GET, "/session/minecraft/hasJoined", new HasJoinedPage());
		provider.register(HttpRequestMethod.POST, "/session/minecraft/join", new JoinPage());
		provider.registerPattern(HttpRequestMethod.GET, "/session/minecraft/profile/{uuid}", ProfilePage.INSTANCE);
		provider.register(HttpRequestMethod.POST, "/authenticate", new AuthenticatePage());
		provider.register(HttpRequestMethod.POST, "/validate", new ValidatePage());
		provider.register(HttpRequestMethod.POST, "/invalidate", new InvalidatePage());
		provider.register(HttpRequestMethod.POST, "/signout", new InvalidatePage());
		provider.register(HttpRequestMethod.POST, "/refresh", new RefreshPage());
		provider.register(HttpRequestMethod.GET, "/publickeys", new PublicKeysPage());

		PlayerAttributesDocument doc = new PlayerAttributesDocument();
		provider.register(HttpRequestMethod.GET, "/player/attributes", doc);
		provider.register(HttpRequestMethod.GET, "/privileges", doc); // for MC 1.16 or older
		provider.register(HttpRequestMethod.GET, "/player/certificates", new PlayerCertificatesDocument());
		provider.register(HttpRequestMethod.POST, "/player/report", new PlayerReportDocument());
		provider.register(HttpRequestMethod.GET, "/yggdrasil_session_pubkey.der", new FileDocument(Paths.get("shittyauth/public_key.der")));

		provider.register(HttpRequestMethod.GET, "/game/joinserver.jsp", new LegacyJoinServerDocument());
		provider.register(HttpRequestMethod.GET, "/game/checkserver.jsp", new LegacyCheckServerDocument());
		provider.registerPattern(HttpRequestMethod.GET, "/cape/{uuid}", UserCapeDocument.INSTANCE);
		provider.registerPattern(HttpRequestMethod.GET, "/skin/{uuid}", UserSkinDocument.INSTANCE);
		provider.registerPattern(HttpRequestMethod.GET, "/MinecraftSkins/{name}", LegacyUserSkinDocument.INSTANCE);
		provider.registerPattern(HttpRequestMethod.GET, "/MinecraftCapes/{name}", LegacyUserCapeDocument.INSTANCE);

		if(ShittyAuth.config.getSetting(ShittyAuthSettings.AUTHLIB_INJECTOR_COMPAT)) {
			provider.register(HttpRequestMethod.GET, "/authserver", new AuthLibInjectorMetadataDocument());

			AuthLibInjectorDocument authlibDoc = new AuthLibInjectorDocument();
			provider.registerPattern(HttpRequestMethod.GET, "/authserver/{page...}", authlibDoc);
			provider.registerPattern(HttpRequestMethod.POST, "/authserver/{page...}", authlibDoc);
			provider.registerPattern(HttpRequestMethod.GET, "/skins/MinecraftSkins/{name}", LegacyUserSkinDocument.INSTANCE);
		}

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
			if(con != null && con.getUserName().equalsIgnoreCase(username)) {
				return a;
			}
		}
		return null;
	}

}
