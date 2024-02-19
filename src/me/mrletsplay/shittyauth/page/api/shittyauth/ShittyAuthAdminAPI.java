package me.mrletsplay.shittyauth.page.api.shittyauth;

import static me.mrletsplay.shittyauth.page.api.shittyauth.ShittyAuthAPI.error;
import static me.mrletsplay.shittyauth.page.api.shittyauth.ShittyAuthAPI.requireAuthorization;

import java.util.List;

import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.config.ShittyAuthSettings;
import me.mrletsplay.shittyauth.webinterface.ShittyAuthWIHandler;
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

public class ShittyAuthAdminAPI implements EndpointCollection {

	private static final JsonObjectValidator CHANGE_USERNAME_VALIDATOR = new JsonObjectValidator()
		.require("userID", JSONType.STRING)
		.require("newUsername", JSONType.STRING);

	private static final JsonObjectValidator CHANGE_PASSWORD_VALIDATOR = new JsonObjectValidator()
		.require("userID", JSONType.STRING)
		.require("newPassword", JSONType.STRING);

	private static final JsonObjectValidator DELETE_ACCOUNT_VALIDATOR = new JsonObjectValidator()
		.require("userID", JSONType.STRING);

	private static final JsonObjectValidator GLOBAL_SETTINGS_VALIDATOR = new JsonObjectValidator()
		.optional("authlibCompat", JSONType.BOOLEAN);

	private static Account requireAdmin(HttpRequestContext ctx) {
		Account account = requireAuthorization(ctx);
		if(account == null) return null;

		if(!account.hasPermission("*")) {
			ctx.respond(HttpStatusCodes.ACCESS_DENIED_403, new JsonResponse(error("Unauthorized")));
			return null;
		}

		return account;
	}

	@Endpoint(method = HttpRequestMethod.GET, path = "/accounts")
	public void accounts() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		Account account = requireAdmin(ctx);
		if(account == null) return;

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

		String newUsername = object.getString("newUsername");
		if(!ShittyAuthWIHandler.USERNAME_PATTERN.matcher(newUsername).matches()) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("New username is invalid")));
			return;
		}

		Account account = requireAdmin(ctx);
		if(account == null) return;

		Account otherAccount = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(ShittyAuth.ACCOUNT_CONNECTION_NAME, object.getString("userID"));
		if(otherAccount == null) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Account doesn't exist")));
			return;
		}

		if(account.getID().equals(otherAccount.getID())) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Can't edit own account via admin API")));
			return;
		}

		if(ShittyAuth.getAccountByUsername(newUsername) != null) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Username is already taken")));
			return;
		}

		AccountConnection connection = otherAccount.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		AccountConnection newConnection = new AccountConnection(connection.getConnectionName(), connection.getUserID(), newUsername, connection.getUserEmail(), connection.getUserAvatar());
		otherAccount.removeConnection(connection);
		otherAccount.addConnection(newConnection);

		ctx.respond(HttpStatusCodes.OK_200, JsonResponse.EMPTY_OBJECT);
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

		String newPassword = object.getString("newPassword");
		if(newPassword.isEmpty()) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("New password can't be empty")));
			return;
		}

		Account account = requireAdmin(ctx);
		if(account == null) return;

		String userID = object.getString("userID");
		Account otherAccount = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(ShittyAuth.ACCOUNT_CONNECTION_NAME, userID);
		if(otherAccount == null) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Account doesn't exist")));
			return;
		}

		if(account.getID().equals(otherAccount.getID())) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Can't edit own account via admin API")));
			return;
		}

		Webinterface.getCredentialsStorage().storeCredentials(ShittyAuth.ACCOUNT_CONNECTION_NAME, userID, newPassword);
		ctx.respond(HttpStatusCodes.OK_200, JsonResponse.EMPTY_OBJECT);
	}

	@Endpoint(method = HttpRequestMethod.POST, path = "/deleteAccount")
	public void deleteAccount() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONObject object;
		if((object = ctx.expectContent(DefaultClientContentTypes.JSON_OBJECT)) == null){
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Bad JSON")));
			return;
		}

		ValidationResult result = DELETE_ACCOUNT_VALIDATOR.validate(object);
		if(!result.isOk()) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, result.asJsonResponse());
			return;
		}

		Account account = requireAdmin(ctx);
		if(account == null) return;

		Account otherAccount = Webinterface.getAccountStorage().getAccountByConnectionSpecificID(ShittyAuth.ACCOUNT_CONNECTION_NAME, object.getString("userID"));
		if(otherAccount == null) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Account doesn't exist")));
			return;
		}

		if(account.getID().equals(otherAccount.getID())) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Can't edit own account via admin API")));
			return;
		}

		Webinterface.getAccountStorage().deleteAccount(otherAccount.getID());
		ctx.respond(HttpStatusCodes.OK_200, JsonResponse.EMPTY_OBJECT);
	}

	@Endpoint(method = HttpRequestMethod.GET, path = "/globalSettings")
	public void globalSettings() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		Account account = requireAdmin(ctx);
		if(account == null) return;

		JSONObject obj = new JSONObject();
		obj.put("authlibCompat", ShittyAuth.config.getSetting(ShittyAuthSettings.AUTHLIB_INJECTOR_COMPAT));

		ctx.respond(HttpStatusCodes.OK_200, new JsonResponse(obj));
	}

	@Endpoint(method = HttpRequestMethod.PATCH, path = "/updateGlobalSettings")
	public void updateGlobalSettings() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();

		JSONObject object;
		if((object = ctx.expectContent(DefaultClientContentTypes.JSON_OBJECT)) == null){
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, new JsonResponse(error("Bad JSON")));
			return;
		}

		ValidationResult result = GLOBAL_SETTINGS_VALIDATOR.validate(object);
		if(!result.isOk()) {
			ctx.respond(HttpStatusCodes.BAD_REQUEST_400, result.asJsonResponse());
			return;
		}

		Account account = requireAdmin(ctx);
		if(account == null) return;

		if(object.has("authlibCompat")) {
			boolean authlibCompat = object.getBoolean("authlibCompat");
			ShittyAuth.config.setSetting(ShittyAuthSettings.AUTHLIB_INJECTOR_COMPAT, authlibCompat);
		}

		ctx.respond(HttpStatusCodes.OK_200, JsonResponse.EMPTY_OBJECT);
	}

	@Override
	public String getBasePath() {
		return "/api/shittyauth/admin";
	}

}
