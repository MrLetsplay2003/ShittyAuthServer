package me.mrletsplay.shittyauth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import me.mrletsplay.shittyauth.auth.AccessTokenStorage;
import me.mrletsplay.shittyauth.config.ShittyAuthSettings;
import me.mrletsplay.shittyauth.page.AuthenticatePage;
import me.mrletsplay.shittyauth.page.HasJoinedPage;
import me.mrletsplay.shittyauth.page.JoinPage;
import me.mrletsplay.shittyauth.page.SettingsPage;
import me.mrletsplay.shittyauth.page.UserCapeDocument;
import me.mrletsplay.shittyauth.page.UserSkinDocument;
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

//		Webinterface.getHttpServer().setDocumentProvider(CockumentProvider.INSTANCE);
//		if(Webinterface.getHttpsServer() != null) Webinterface.getHttpsServer().setDocumentProvider(CockumentProvider.INSTANCE);
		
//		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
//		gen.initialize(4096);
//		
//		KeyPair pair = gen.generateKeyPair();
//		PrivateKey priv = pair.getPrivate();
//		PublicKey pub = pair.getPublic();
//		
//		Files.write(Path.of("sus_pub.der"), pub.getEncoded(), StandardOpenOption.CREATE);
//		Files.write(Path.of("sus_priv.der"), priv.getEncoded(), StandardOpenOption.CREATE);
		
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Files.readAllBytes(Path.of("sus_priv.der")));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKey = keyFactory.generatePrivate(spec);
		
		Webinterface.getDocumentProvider().registerDocument("/session/minecraft/hasJoined", new HasJoinedPage());
		Webinterface.getDocumentProvider().registerDocument("/session/minecraft/join", new JoinPage());
		Webinterface.getDocumentProvider().registerDocument("/authenticate", new AuthenticatePage());
		Webinterface.getDocumentProvider().registerDocument(UserSkinDocument.PATH, new UserSkinDocument());
		Webinterface.getDocumentProvider().registerDocument(UserCapeDocument.PATH, new UserCapeDocument());
		
		Webinterface.registerActionHandler(new ShittyAuthWIHandler());
		
		WebinterfacePageCategory cat = Webinterface.createCategory("Minecraft");
		cat.addPage(new MCAccountPage());
		cat.addPage(new SettingsPage());
		
//		HttpServer httpServer = Webinterface.getHttpServer();
//		httpServer.getExecutor().submit(() -> {
//			Supplier<Boolean> keepRunning = () -> httpServer.isRunning() && !httpServer.getExecutor().isShutdown() && !Thread.interrupted();
//			while(keepRunning.get()) {
//				tokenStorage.cleanUp();
//				for(int i = 0; i < 10; i++) {
//					if(!keepRunning.get()) return;
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException ignored) {}
//				}
//			}
//		});
	}

}
