package me.mrletsplay.shittyauth;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import me.mrletsplay.mrcore.http.HttpRequest;
import me.mrletsplay.shittyauth.auth.AccessTokenStorage;
import me.mrletsplay.shittyauth.auth.FileAccessTokenStorage;
import me.mrletsplay.shittyauth.auth.SQLAccessTokenStorage;
import me.mrletsplay.shittyauth.config.ShittyAuthSettings;
import me.mrletsplay.shittyauth.page.AccountPage;
import me.mrletsplay.shittyauth.page.AdminPage;
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
import me.mrletsplay.shittyauth.page.api.shittyauth.ShittyAuthAPI;
import me.mrletsplay.shittyauth.page.api.shittyauth.ShittyAuthAdminAPI;
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
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.shittyauth.user.UserDataStorage;
import me.mrletsplay.shittyauth.util.DefaultTexture;
import me.mrletsplay.shittyauth.util.InvalidSkinException;
import me.mrletsplay.shittyauth.util.InvalidUsernameException;
import me.mrletsplay.shittyauth.webinterface.ShittyAuthWIHandler;
import me.mrletsplay.simplehttpserver.http.HttpRequestMethod;
import me.mrletsplay.simplehttpserver.http.document.DocumentProvider;
import me.mrletsplay.simplehttpserver.http.document.FileDocument;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;
import me.mrletsplay.webinterfaceapi.config.DefaultSettings;
import me.mrletsplay.webinterfaceapi.config.FileConfig;
import me.mrletsplay.webinterfaceapi.page.PageCategory;
import me.mrletsplay.webinterfaceapi.setup.Setup;
import me.mrletsplay.webinterfaceapi.sql.SQLHelper;

public class ShittyAuth {

	public static final String ACCOUNT_CONNECTION_NAME = "shittyauth";

	private static final Path
		SKINS_PATH = Paths.get("shittyauth/skins/"),
		CAPES_PATH = Paths.get("shittyauth/capes/"),
		DEFAULT_SKIN_PATH = DefaultTexture.SKIN_STEVE.getPath(),
		DEFAULT_CAPE_PATH = DefaultTexture.CAPE_MIGRATOR.getPath();

	public static PublicKey publicKey;
	public static PrivateKey privateKey;
	public static AccessTokenStorage tokenStorage;
	public static UserDataStorage dataStorage;
	public static FileConfig config;
	public static Map<String, String> userServers = new HashMap<>();

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		DefaultSettings.HOME_PAGE_PATH.defaultValue(AccountPage.PATH);
		DefaultSettings.CORS_ALLOW_ALL_ORIGINS.defaultValue(true);
		DefaultSettings.CORS_ALLOWED_HEADERS.defaultValue(Arrays.asList("*"));
		DefaultSettings.CORS_ALLOW_CREDENTIALS.defaultValue(true);
		DefaultSettings.ALLOW_ANONYMOUS.defaultValue(false);

		if(!Webinterface.getSetup().isDone() && "true".equals(System.getenv("SHITTYAUTH_SKIP_SETUP"))) {
			Webinterface.getLogger().info("Skipping setup");

			Setup setup = Webinterface.getSetup();
			setup.getSteps().forEach(s -> {
				if(setup.isStepDone(s.getID())) return;
				setup.addCompletedStep(s.getID());
			});
		}

		Webinterface.start();
		Webinterface.extractResources("/shittyauth-resources.list");

		Webinterface.getLogger().info("Downloading default textures");
		for(DefaultTexture texture : DefaultTexture.values()) {
			Path path = texture.getPath();
			if(!Files.exists(path)) {
				Webinterface.getLogger().info("Downloading " + texture.name());
				HttpRequest.createGet(texture.getURL()).execute().transferTo(path.toFile());
			}
		}

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
		provider.register(HttpRequestMethod.POST, "/player/certificates", new PlayerCertificatesDocument());
		provider.register(HttpRequestMethod.POST, "/player/report", new PlayerReportDocument());
		provider.register(HttpRequestMethod.GET, "/yggdrasil_session_pubkey.der", new FileDocument(Paths.get("shittyauth/public_key.der")));

		provider.register(HttpRequestMethod.GET, "/game/joinserver.jsp", new LegacyJoinServerDocument());
		provider.register(HttpRequestMethod.GET, "/game/checkserver.jsp", new LegacyCheckServerDocument());
		provider.registerPattern(HttpRequestMethod.GET, "/cape/{uuid}", UserCapeDocument.INSTANCE);
		provider.registerPattern(HttpRequestMethod.GET, "/skin/{uuid}", UserSkinDocument.INSTANCE);
		provider.registerPattern(HttpRequestMethod.GET, "/MinecraftSkins/{name}", LegacyUserSkinDocument.INSTANCE);
		provider.registerPattern(HttpRequestMethod.GET, "/MinecraftCapes/{name}", LegacyUserCapeDocument.INSTANCE);

		if(ShittyAuth.config.getSetting(ShittyAuthSettings.AUTHLIB_INJECTOR_COMPAT)) {
			// Provide authlib metadata
			provider.register(HttpRequestMethod.GET, "/authserver", new AuthLibInjectorMetadataDocument());

			// Redirect all of the authlib mappings to their respective ShittyAuth mappings
			// See https://github.com/yushijinhun/authlib-injector/blob/961366e012c26849810a744897bf41b2e926b734/src/main/java/moe/yushi/authlibinjector/httpd/DefaultURLRedirector.java#L36
			AuthLibInjectorDocument authlibDoc = new AuthLibInjectorDocument();
			List<String> authlibPaths = Arrays.asList("/api", "/authserver", "/sessionserver", "/skins", "/minecraftservices");
			for(String path : authlibPaths) {
				String p = path + "/{page...}";
				provider.registerPattern(HttpRequestMethod.GET, p, authlibDoc);
				provider.registerPattern(HttpRequestMethod.POST, p, authlibDoc);
			}
		}

		new ShittyAuthAPI().register(provider);
		new ShittyAuthAdminAPI().register(provider);

		Webinterface.registerActionHandler(new ShittyAuthWIHandler());

		initFromEnvvars();

		PageCategory cat = Webinterface.createCategory("Minecraft");
		cat.addPage(new AccountPage());
		cat.addPage(new CreateAccountPage());
		cat.addPage(new AdminPage());
		cat.addPage(new SettingsPage());
	}

	private static void initFromEnvvars() {
		String adminUser = System.getenv("SHITTYAUTH_ADMIN_USER");
		String adminPass = System.getenv("SHITTYAUTH_ADMIN_PASSWORD");
		if(adminUser != null && adminPass != null) {
			Webinterface.getLogger().info("Creating admin user");

			Account acc = getAccountByUsername(adminUser);
			if(acc != null) {
				Webinterface.getLogger().info("Account with username '" + adminUser + "' already exists. Skipping");
			}else {
				try {
					createAccount(adminUser, adminPass);
				} catch (InvalidUsernameException e) {
					Webinterface.getLogger().error("Failed to create account", e);
				}
			}
		}
	}

	public static Account createAccount(String username, String password) throws InvalidUsernameException {
		if(!ShittyAuthWIHandler.USERNAME_PATTERN.matcher(username).matches()) throw new InvalidUsernameException("Invalid username");

		String uuid = UUID.randomUUID().toString();
		AccountConnection conn = new AccountConnection(ACCOUNT_CONNECTION_NAME, uuid, username, null, null);
		Account acc = Webinterface.getAccountStorage().createAccount(conn);
		Webinterface.getCredentialsStorage().storeCredentials(ACCOUNT_CONNECTION_NAME, uuid, password);
		ShittyAuth.dataStorage.updateUserData(uuid, UserData.createNew());
		return acc;
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

	public static void updateUserSkin(String userID, DefaultTexture texture) throws IOException {
		Path texPath = texture.getPath();
		try {
			BufferedImage image = ImageIO.read(texPath.toFile());
			updateUserSkin(userID, image);
		}catch(InvalidSkinException ignored) {}
	}

	public static void updateUserSkin(String userID, BufferedImage image) throws IOException, InvalidSkinException {
		if(image.getWidth() != 64 || (image.getHeight() != 64 && image.getHeight() != 32)) throw new InvalidSkinException("Skin must be 64x64 or 64x32 pixels");
		BufferedImage copy = new BufferedImage(64, image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		copy.createGraphics().drawImage(image, 0, 0, null);

		if(!Files.exists(SKINS_PATH)) Files.createDirectories(SKINS_PATH);
		Path skinPath = SKINS_PATH.resolve(userID + ".png");
		try(OutputStream out = Files.newOutputStream(skinPath)) {
			ImageIO.write(copy, "PNG", out);
		}

		UserData d = ShittyAuth.dataStorage.getUserData(userID);
		d.setSkinLastChanged(System.currentTimeMillis());
		ShittyAuth.dataStorage.updateUserData(userID, d);
	}

	public static BufferedImage loadUserSkin(String userID) throws IOException {
		return ImageIO.read(new ByteArrayInputStream(loadUserSkinRaw(userID)));
	}

	public static byte[] loadUserSkinRaw(String userID) throws IOException {
		Path skinPath = SKINS_PATH.resolve(userID + ".png");
		if(!skinPath.normalize().startsWith(SKINS_PATH)) throw new IOException("Invalid path");
		if(!Files.exists(skinPath)) skinPath = DEFAULT_SKIN_PATH;
		return Files.readAllBytes(skinPath);
	}

	public static void updateUserCape(String userID, DefaultTexture texture) throws IOException {
		Path texPath = texture.getPath();
		try {
			BufferedImage image = ImageIO.read(texPath.toFile());
			updateUserCape(userID, image);
		}catch(InvalidSkinException ignored) {}
	}

	public static void updateUserCape(String userID, BufferedImage image) throws IOException, InvalidSkinException{
		if(image.getWidth() != 64 || image.getHeight() != 32)  throw new InvalidSkinException("Cape must be 64x32");
		BufferedImage copy = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
		copy.createGraphics().drawImage(image, 0, 0, null);

		if(!Files.exists(CAPES_PATH)) Files.createDirectories(CAPES_PATH);
		Path skinPath = CAPES_PATH.resolve(userID + ".png");
		try(OutputStream out = Files.newOutputStream(skinPath)) {
			ImageIO.write(copy, "PNG", out);
		}

		UserData d = ShittyAuth.dataStorage.getUserData(userID);
		d.setCapeLastChanged(System.currentTimeMillis());
		ShittyAuth.dataStorage.updateUserData(userID, d);
	}

	public static BufferedImage loadUserCape(String userID) throws IOException {
		return ImageIO.read(new ByteArrayInputStream(loadUserCapeRaw(userID)));
	}

	public static byte[] loadUserCapeRaw(String userID) throws IOException {
		Path capePath = CAPES_PATH.resolve(userID + ".png");
		if(!capePath.normalize().startsWith(CAPES_PATH)) throw new IOException("Invalid path");
		if(!Files.exists(capePath)) capePath = DEFAULT_CAPE_PATH;
		return Files.readAllBytes(capePath);
	}

}
