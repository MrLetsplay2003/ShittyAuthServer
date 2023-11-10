package me.mrletsplay.shittyauth.page.api.shittyauth;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.textures.TexturesHelper;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.simplehttpserver.http.HttpRequestMethod;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.endpoint.Endpoint;
import me.mrletsplay.simplehttpserver.http.endpoint.EndpointCollection;
import me.mrletsplay.simplehttpserver.http.header.DefaultClientContentTypes;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.simplehttpserver.http.response.JsonResponse;
import me.mrletsplay.simplehttpserver.http.validation.JsonObjectValidator;
import me.mrletsplay.simplehttpserver.http.validation.result.ValidationResult;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;
import me.mrletsplay.webinterfaceapi.session.Session;

public class ShittyAuthAPI implements EndpointCollection {

	private JSONObject error(String message) {
		JSONObject error = new JSONObject();
		error.put("error", message);
		return error;
	}

	private Account requireShittyAuth(HttpRequestContext ctx) {
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

	private static final JsonObjectValidator LOGIN_VALIDATOR = new JsonObjectValidator()
		.require("username", JSONType.STRING)
		.require("password", JSONType.STRING);

	@Endpoint(method = HttpRequestMethod.POST, path = "/login")
	public void login() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONObject object = ctx.getClientHeader().getPostData().getParsedAs(DefaultClientContentTypes.JSON_OBJECT);
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
		if(connection == null || !Webinterface.getCredentialsStorage().checkCredentials(ShittyAuth.ACCOUNT_CONNECTION_NAME, connection.getUserID(), password)) {
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

		Account account = requireShittyAuth(ctx);
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

		Account account = requireShittyAuth(ctx);
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

	private static final JsonObjectValidator CHANGE_PASSWORD_VALIDATOR = new JsonObjectValidator()
		.require("oldPassword", JSONType.STRING)
		.require("newPassword", JSONType.STRING);

	@Endpoint(method = HttpRequestMethod.POST, path = "/changePassword")
	public void changePassword() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONObject object = ctx.getClientHeader().getPostData().getParsedAs(DefaultClientContentTypes.JSON_OBJECT);
		ValidationResult result = CHANGE_PASSWORD_VALIDATOR.validate(object);
		if(!result.isOk()) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, result.asJsonResponse());
			return;
		}

		Account account = requireShittyAuth(ctx);
		if(account == null) return;

		String oldPassword = object.getString("oldPassword");
		String newPassword = object.getString("newPassword");

		AccountConnection connection = account.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(connection == null || !Webinterface.getCredentialsStorage().checkCredentials(ShittyAuth.ACCOUNT_CONNECTION_NAME, connection.getUserID(), oldPassword)) {
			ctx.respond(HttpStatusCodes.ACCESS_DENIED_403, new JsonResponse(error("Invalid credentials")));
			return;
		}

		Webinterface.getCredentialsStorage().storeCredentials(ShittyAuth.ACCOUNT_CONNECTION_NAME, connection.getUserID(), newPassword);
		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(new JSONObject()));
	}

	@Override
	public String getBasePath() {
		return "/api/shittyauth";
	}

}
