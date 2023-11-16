package me.mrletsplay.shittyauth.page.api.shittyauth;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.textures.SkinType;
import me.mrletsplay.shittyauth.textures.TexturesHelper;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.shittyauth.util.DefaultTexture;
import me.mrletsplay.shittyauth.webinterface.ShittyAuthWIHandler;
import me.mrletsplay.simplehttpserver.http.HttpRequestMethod;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.endpoint.Endpoint;
import me.mrletsplay.simplehttpserver.http.endpoint.EndpointCollection;
import me.mrletsplay.simplehttpserver.http.endpoint.RequestParameter;
import me.mrletsplay.simplehttpserver.http.header.DefaultClientContentTypes;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.simplehttpserver.http.response.HttpResponse;
import me.mrletsplay.simplehttpserver.http.response.JsonResponse;
import me.mrletsplay.simplehttpserver.http.util.MimeType;
import me.mrletsplay.simplehttpserver.http.validation.JsonObjectValidator;
import me.mrletsplay.simplehttpserver.http.validation.result.ValidationResult;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;
import me.mrletsplay.webinterfaceapi.session.Session;

public class ShittyAuthAPI implements EndpointCollection {

	private static final JsonObjectValidator LOGIN_VALIDATOR = new JsonObjectValidator()
		.require("username", JSONType.STRING)
		.require("password", JSONType.STRING);

	private static final JsonObjectValidator CHANGE_USERNAME_VALIDATOR = new JsonObjectValidator()
		.require("newUsername", JSONType.STRING);

	private static final JsonObjectValidator CHANGE_PASSWORD_VALIDATOR = new JsonObjectValidator()
		.require("oldPassword", JSONType.STRING)
		.require("newPassword", JSONType.STRING);

	private static final JsonObjectValidator UPDATE_SKIN_SETTINGS_VALIDATOR = new JsonObjectValidator()
		.optional("skinType", JSONType.STRING)
		.optional("capeEnabled", JSONType.BOOLEAN);

	private JSONObject error(String message) {
		JSONObject error = new JSONObject();
		error.put("error", message);
		return error;
	}

	private Account requireAuthorization(HttpRequestContext ctx) {
		String sessionID = ctx.getClientHeader().getFields().getFirst("Authorization");
		if(sessionID == null) {
			ctx.respond(HttpStatusCodes.ACCESS_DENIED_403, new JsonResponse(error("Unauthorized")));
			return null;
		}

		Session session = Session.getSession(sessionID);
		if(session == null) {
			ctx.respond(HttpStatusCodes.ACCESS_DENIED_403, new JsonResponse(error("Unauthorized")));
			return null;
		}

		Account account = session.getAccount();
		AccountConnection connection = account.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(connection == null) {
			ctx.respond(HttpStatusCodes.NOT_FOUND_404, new JsonResponse(error("No account")));
			return null;
		}

		return account;
	}

	@Endpoint(method = HttpRequestMethod.GET, path = "/meta")
	public void version() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONObject meta = new JSONObject();
		meta.put("version", 1);

		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(meta));
	}

	@Endpoint(method = HttpRequestMethod.POST, path = "/login")
	public void login() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONObject object;
		if((object = ctx.expectContent(DefaultClientContentTypes.JSON_OBJECT)) == null){
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Bad JSON")));
			return;
		}

		ValidationResult result = LOGIN_VALIDATOR.validate(object);
		if(!result.isOk()) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, result.asJsonResponse());
			return;
		}

		String username = object.getString("username");
		String password = object.getString("password");
		Account account = ShittyAuth.getAccountByUsername(username);
		if(account == null) {
			ctx.respond(HttpStatusCodes.ACCESS_DENIED_403, new JsonResponse(error("Invalid credentials")));
			return;
		}

		AccountConnection connection = account.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(!Webinterface.getCredentialsStorage().checkCredentials(ShittyAuth.ACCOUNT_CONNECTION_NAME, connection.getUserID(), password)) {
			ctx.respond(HttpStatusCodes.ACCESS_DENIED_403, new JsonResponse(error("Invalid credentials")));
			return;
		}

		Session session = Session.startSession(account);
		JSONObject response = new JSONObject();
		response.put("token", session.getSessionID());
		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(response));
	}

	@Endpoint(method = HttpRequestMethod.GET, path = "/me")
	public void me() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		Account account = requireAuthorization(ctx);
		if(account == null) return;

		AccountConnection connection = account.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);

		JSONObject accountInfo = new JSONObject();
		accountInfo.put("id", connection.getUserID());
		accountInfo.put("username", connection.getUserName());
		accountInfo.put("isAdmin", account.hasPermission("*"));
		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(accountInfo));
	}

	@Endpoint(method = HttpRequestMethod.GET, path = "/skin")
	public void skin() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		Account account = requireAuthorization(ctx);
		if(account == null) return;

		AccountConnection connection = account.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);

		UserData data = ShittyAuth.dataStorage.getUserData(connection.getUserID());

		JSONObject skinInfo = new JSONObject();
		skinInfo.put("skinURL", String.format(TexturesHelper.getSkinBaseURL() + TexturesHelper.SKIN_PATH, connection.getUserID(), data.getSkinLastChanged()));
		skinInfo.put("skinType", data.getSkinType().name().toLowerCase());
		skinInfo.put("capeURL", String.format(TexturesHelper.getSkinBaseURL() + TexturesHelper.CAPE_PATH, connection.getUserID(), data.getCapeLastChanged()));
		skinInfo.put("capeEnabled", data.hasCape());
		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(skinInfo));
	}

	@Endpoint(method = HttpRequestMethod.POST, path = "/changeUsername")
	public void changeUsername() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONObject object;
		if((object = ctx.expectContent(DefaultClientContentTypes.JSON_OBJECT)) == null){
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Bad JSON")));
			return;
		}

		ValidationResult result = CHANGE_USERNAME_VALIDATOR.validate(object);
		if(!result.isOk()) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, result.asJsonResponse());
			return;
		}

		Account account = requireAuthorization(ctx);
		if(account == null) return;

		String newUsername = object.getString("newUsername");
		if(!ShittyAuthWIHandler.USERNAME_PATTERN.matcher(newUsername).matches()) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Invalid username")));
			return;
		}

		AccountConnection connection = account.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		AccountConnection newConnection = new AccountConnection(connection.getConnectionName(), connection.getUserID(), newUsername, connection.getUserEmail(), connection.getUserAvatar());
		account.removeConnection(connection);
		account.addConnection(newConnection);

		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(new JSONObject()));
	}

	@Endpoint(method = HttpRequestMethod.POST, path = "/changePassword")
	public void changePassword() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONObject object;
		if((object = ctx.expectContent(DefaultClientContentTypes.JSON_OBJECT)) == null){
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Bad JSON")));
			return;
		}

		ValidationResult result = CHANGE_PASSWORD_VALIDATOR.validate(object);
		if(!result.isOk()) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, result.asJsonResponse());
			return;
		}

		Account account = requireAuthorization(ctx);
		if(account == null) return;

		String oldPassword = object.getString("oldPassword");
		String newPassword = object.getString("newPassword");

		AccountConnection connection = account.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(!Webinterface.getCredentialsStorage().checkCredentials(ShittyAuth.ACCOUNT_CONNECTION_NAME, connection.getUserID(), oldPassword)) {
			ctx.respond(HttpStatusCodes.ACCESS_DENIED_403, new JsonResponse(error("Invalid credentials")));
			return;
		}

		Webinterface.getCredentialsStorage().storeCredentials(ShittyAuth.ACCOUNT_CONNECTION_NAME, connection.getUserID(), newPassword);
		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(new JSONObject()));
	}

	@Endpoint(method = HttpRequestMethod.PUT, path = "/updateSkinSettings")
	public void updateSkinSettings() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONObject object;
		if((object = ctx.expectContent(DefaultClientContentTypes.JSON_OBJECT)) == null){
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Bad JSON")));
			return;
		}

		ValidationResult result = UPDATE_SKIN_SETTINGS_VALIDATOR.validate(object);
		if(!result.isOk()) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, result.asJsonResponse());
			return;
		}

		Account account = requireAuthorization(ctx);
		if(account == null) return;

		AccountConnection connection = account.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);

		SkinType skinType = null;

		if(object.has("skinType")) {
			try {
				skinType = SkinType.valueOf(object.getString("skinType").toUpperCase());
			}catch(IllegalArgumentException e) {
				ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Invalid skin type")));
				return;
			}
		}

		Boolean capeEnabled = object.optBoolean("capeEnabled").orElse(null);

		if(skinType != null || capeEnabled != null) {
			UserData data = ShittyAuth.dataStorage.getUserData(connection.getUserID());
			if(skinType != null) data.setSkinType(skinType);
			if(capeEnabled != null) data.setHasCape(capeEnabled);
			ShittyAuth.dataStorage.updateUserData(connection.getUserID(), data);
		}

		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(new JSONObject()));
	}

	@Endpoint(method = HttpRequestMethod.GET, path = "/admin/accounts")
	public void adminAccounts() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		Account account = requireAuthorization(ctx);
		if(account == null) return;

		if(!account.hasPermission("*")) {
			ctx.respond(HttpStatusCodes.ACCESS_DENIED_403, new JsonResponse(error("Unauthorized")));
			return;
		}

		List<Account> allAccounts = Webinterface.getAccountStorage().getAccounts();
		JSONArray accounts = new JSONArray();
		for(Account a : allAccounts) {
			AccountConnection connection = a.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
			if(connection == null) continue;

			JSONObject obj = new JSONObject();
			obj.put("id", connection.getUserID());
			obj.put("username", connection.getUserName());
			obj.put("isAdmin", a.hasPermission("*"));
			accounts.add(obj);
		}

		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(accounts));
	}

	@Endpoint(method = HttpRequestMethod.GET, path = "/avatar/{userID}", pathPattern = true)
	public void avatar(@RequestParameter("userID") String userID) {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		Account account = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(ShittyAuth.ACCOUNT_CONNECTION_NAME, userID);
		if(account == null) {
			ctx.respond(HttpStatusCodes.NOT_FOUND_404, new JsonResponse(error("Account not found")));
			return;
		}

		byte[] headBytes;
		try {
			BufferedImage skinImage = ShittyAuth.loadUserSkin(userID);
			BufferedImage headImage = skinImage.getSubimage(8, 8, 8, 8);
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			ImageIO.write(headImage, "PNG", bOut);
			headBytes = bOut.toByteArray();
		} catch (IOException e) {
			ctx.respond(HttpStatusCodes.INTERNAL_SERVER_ERROR_500, new JsonResponse(error("Failed to load skin")));
			return;
		}

		ctx.respond(HttpStatusCodes.OK_200, new HttpResponse() {

			@Override
			public MimeType getContentType() {
				return MimeType.PNG;
			}

			@Override
			public byte[] getContent() {
				return headBytes;
			}
		});
	}

	@Endpoint(method = HttpRequestMethod.GET, path = "/defaultSkins")
	public void defaultSkins() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONObject res = new JSONObject();

		JSONArray skins = new JSONArray();
		for(var en : DefaultTexture.getSkins().entrySet()) {
			JSONObject skin = new JSONObject();
			skin.put("id", en.getValue().name().toLowerCase());
			skin.put("name", en.getKey());
			skin.put("url", en.getValue().getURL()); // TODO: provide url on ShittyAuthServer
			skins.add(skin);
		}
		res.put("skins", skins);

		JSONArray slimSkins = new JSONArray();
		for(var en : DefaultTexture.getSlimSkins().entrySet()) {
			JSONObject skin = new JSONObject();
			skin.put("id", en.getValue().name().toLowerCase());
			skin.put("name", en.getKey());
			skin.put("url", en.getValue().getURL()); // TODO: provide url on ShittyAuthServer
			slimSkins.add(skin);
		}
		res.put("slimSkins", slimSkins);

		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(res));
	}

	@Endpoint(method = HttpRequestMethod.GET, path = "/defaultCapes")
	public void defaultCapes() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONArray capes = new JSONArray();
		for(var en : DefaultTexture.getCapes().entrySet()) {
			JSONObject cape = new JSONObject();
			cape.put("id", en.getValue().name().toLowerCase());
			cape.put("name", en.getKey());
			cape.put("url", en.getValue().getURL()); // TODO: provide url on ShittyAuthServer
			capes.add(cape);
		}

		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(capes));
	}

	@Override
	public String getBasePath() {
		return "/api/shittyauth";
	}

}
