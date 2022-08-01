package me.mrletsplay.shittyauth.page;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;
import me.mrletsplay.webinterfaceapi.page.Page;
import me.mrletsplay.webinterfaceapi.page.PageSection;
import me.mrletsplay.webinterfaceapi.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.page.action.value.ActionValue;
import me.mrletsplay.webinterfaceapi.page.element.Button;
import me.mrletsplay.webinterfaceapi.page.element.InputField;
import me.mrletsplay.webinterfaceapi.page.element.TitleText;
import me.mrletsplay.webinterfaceapi.page.element.layout.Grid;
import me.mrletsplay.webinterfaceapi.session.Session;

public class CreateAccountPage extends Page {

	public static final String PATH = "/shittyauth/create";

	public CreateAccountPage() {
		super("Create Account", PATH, true);

		PageSection s = new PageSection();
		s.setSlimLayout(true);
		addSection(s);

		s.addTitle("Create your Minecraft account");
		s.setGrid(new Grid().setColumns("25fr", "75fr"));

		s.addElement(TitleText.builder().text("Username").leftboundText().create());
		InputField username = InputField.builder().placeholder("Minecraft Username").create();
		username.setMinLength(3);
		username.setMaxLength(16);
		s.addElement(username);

		s.addElement(TitleText.builder().text("Password").leftboundText().create());
		InputField password = InputField.builder().placeholder("Minecraft Password").create();
		s.addElement(password);

		s.addElement(Button.builder()
			.text("Create")
			.fullWidth()
			.onClick(SendJSAction.of("shittyauth", "createAccount", ActionValue.object()
				.put("username", ActionValue.elementValue(username))
				.put("password", ActionValue.elementValue(password)))
				.onSuccess(RedirectAction.to(AccountPage.PATH)))
			.create());
	}

	@Override
	public void createContent() {
		if(!Session.requireSession()) return;

		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		Account acc = Session.getCurrentSession().getAccount();
		AccountConnection mcCon = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
		if(mcCon != null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.FOUND_302);
			ctx.getServerHeader().getFields().set("Location", AccountPage.PATH);
			return;
		}

		super.createContent();
	}

}
