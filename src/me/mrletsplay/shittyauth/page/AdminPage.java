package me.mrletsplay.shittyauth.page;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.webinterfaceapi.DefaultPermissions;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.auth.AccountConnection;
import me.mrletsplay.webinterfaceapi.page.Page;
import me.mrletsplay.webinterfaceapi.page.PageSection;
import me.mrletsplay.webinterfaceapi.page.action.ConfirmAction;
import me.mrletsplay.webinterfaceapi.page.action.ReloadPageAction;
import me.mrletsplay.webinterfaceapi.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.page.action.value.ActionValue;
import me.mrletsplay.webinterfaceapi.page.element.Button;
import me.mrletsplay.webinterfaceapi.page.element.Group;
import me.mrletsplay.webinterfaceapi.page.element.Image;
import me.mrletsplay.webinterfaceapi.page.element.InputField;
import me.mrletsplay.webinterfaceapi.page.element.PasswordField;
import me.mrletsplay.webinterfaceapi.page.element.Text;
import me.mrletsplay.webinterfaceapi.page.element.TitleText;
import me.mrletsplay.webinterfaceapi.page.element.VerticalSpacer;
import me.mrletsplay.webinterfaceapi.page.element.builder.Align;
import me.mrletsplay.webinterfaceapi.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.page.element.layout.Grid;

public class AdminPage extends Page {

	public static final String PATH = "/shittyauth/admin";

	public AdminPage() {
		super("Admin", PATH);
		setIcon("mdi:security");
		setPermission(DefaultPermissions.MODIFY_USERS);

		PageSection sc = new PageSection();
		sc.setSlimLayout(true);

		sc.addHeading("Accounts", 2);
		sc.getStyle().setProperty("grid-template-columns", "1fr");
		sc.getMobileStyle().setProperty("grid-template-columns", "1fr");

		sc.dynamic(els -> {
			for(Account acc : Webinterface.getAccountStorage().getAccounts()) {
				AccountConnection conn = acc.getConnection(ShittyAuth.ACCOUNT_CONNECTION_NAME);
				if(conn == null) continue;

				Group grp = new Group();
				grp.getStyle().setProperty("border", "1px solid var(--theme-color-content-border)");
				grp.getStyle().setProperty("border-radius", "5px");
				grp.getStyle().setProperty("margin", "5px 0px");
				grp.setGrid(new Grid().setColumns("min-content", "auto"));

				TitleText tt = TitleText.builder()
					.text(conn.getUserName())
					.leftboundText()
					.noLineBreaks()
					.create();
				tt.getStyle().setProperty("font-size", "24px");
				grp.addElement(tt);

				grp.addElement(Image.builder()
					.src("/api/shittyauth/avatar/" + conn.getUserID())
					.width("64px")
					.height("64px")
					.withLayoutOptions((container, element) -> element.appendAttribute("style", "image-rendering: pixelated;"))
					.align(Align.LEFT_CENTER)
					.create());

				grp.addElement(TitleText.builder()
					.text("User ID")
					.noLineBreaks()
					.leftboundText()
					.withLayoutOptions(DefaultLayoutOption.NEW_LINE)
					.create());
				grp.addElement(Text.builder()
					.text(conn.getUserID())
					.leftboundText()
					.create());

				grp.addElement(TitleText.builder()
					.text("WIAPI account name")
					.noLineBreaks()
					.leftboundText()
					.withLayoutOptions(DefaultLayoutOption.NEW_LINE)
					.create());
				grp.addElement(Text.builder()
					.text(acc.getUsername())
					.leftboundText()
					.create());

				grp.addElement(TitleText.builder()
					.text("WIAPI account ID")
					.noLineBreaks()
					.leftboundText()
					.withLayoutOptions(DefaultLayoutOption.NEW_LINE)
					.create());
				grp.addElement(Text.builder()
					.text(acc.getID())
					.leftboundText()
					.create());

				Group actions = new Group();
				actions.addLayoutOptions(new Grid().setColumns("1fr auto"), DefaultLayoutOption.FULL_WIDTH);

				InputField usernameField = InputField.builder()
					.placeholder("Username")
					.create();
				actions.addElement(usernameField);

				actions.addElement(Button.builder()
					.text("Change MC username")
					.onClick(ConfirmAction.of(SendJSAction.of(
							"shittyauth", "changeMCUsername",
							ActionValue.object()
								.put("account", ActionValue.string(acc.getID()))
								.put("username", ActionValue.elementValue(usernameField))
						)
						.onSuccess(ReloadPageAction.delayed(100))))
					.create());

				PasswordField passwordField = PasswordField.builder()
					.placeholder("Password")
					.create();
				actions.addElement(passwordField);

				actions.addElement(Button.builder()
					.text("Change MC password")
					.onClick(ConfirmAction.of(SendJSAction.of(
							"shittyauth", "changeMCPassword",
							ActionValue.object()
								.put("account", ActionValue.string(acc.getID()))
								.put("password", ActionValue.elementValue(passwordField))
						)
						.onSuccess(ReloadPageAction.delayed(100))))
					.create());
				grp.addElement(actions);

				// TODO: this should be in the main WIAPI users page, not here
				PasswordField wiapiPasswordField = PasswordField.builder()
					.placeholder("Password")
					.create();
				actions.addElement(wiapiPasswordField);

				actions.addElement(Button.builder()
					.text("Change WIAPI password")
					.onClick(ConfirmAction.of(SendJSAction.of(
							"shittyauth", "changeWIAPIPassword",
							ActionValue.object()
								.put("account", ActionValue.string(acc.getID()))
								.put("password", ActionValue.elementValue(wiapiPasswordField))
						)
						.onSuccess(ReloadPageAction.delayed(100))))
					.create());

				grp.addElement(Button.builder()
					.text("Delete account")
					.width("auto")
					.align(Align.LEFT_CENTER)
					.withLayoutOptions(DefaultLayoutOption.FULL_WIDTH)
					.onClick(ConfirmAction.of(SendJSAction.of("webinterface", "deleteAccount", ActionValue.object().put("account", ActionValue.string(acc.getID()))).onSuccess(ReloadPageAction.delayed(100))))
					.create());
				grp.addElement(new VerticalSpacer("30px"));

				els.add(grp);
			}
		});

		addSection(sc);
	}

}
