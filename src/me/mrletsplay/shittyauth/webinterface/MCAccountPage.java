package me.mrletsplay.shittyauth.webinterface;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.textures.SkinType;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.webinterfaceapi.page.Page;
import me.mrletsplay.webinterfaceapi.page.PageSection;
import me.mrletsplay.webinterfaceapi.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.page.action.ShowToastAction;
import me.mrletsplay.webinterfaceapi.page.action.SubmitUploadAction;
import me.mrletsplay.webinterfaceapi.page.action.value.ActionValue;
import me.mrletsplay.webinterfaceapi.page.element.Button;
import me.mrletsplay.webinterfaceapi.page.element.CheckBox;
import me.mrletsplay.webinterfaceapi.page.element.FileUpload;
import me.mrletsplay.webinterfaceapi.page.element.Select;
import me.mrletsplay.webinterfaceapi.page.element.Text;
import me.mrletsplay.webinterfaceapi.page.element.TitleText;
import me.mrletsplay.webinterfaceapi.page.element.VerticalSpacer;
import me.mrletsplay.webinterfaceapi.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.page.element.layout.Grid;
import me.mrletsplay.webinterfaceapi.session.Session;

public class MCAccountPage extends Page {

	public MCAccountPage() {
		super("MC Account", "/mc/account");
		setIcon("mdi:minecraft");

		PageSection s = new PageSection();
		s.setSlimLayout(true);
		s.setGrid(new Grid().setColumns("75fr", "25fr"));
		s.dynamic(els -> {
			UserData d = ShittyAuth.dataStorage.getUserData(Session.getCurrentSession().getAccountID());

			els.add(TitleText.builder()
					.leftboundText()
					.text("Skin Type")
					.fullWidth()
					.create());

			Select skinTypeSel = Select.builder()
					.addOption("Steve", "STEVE", d.getSkinType() == SkinType.STEVE)
					.addOption("Alex", "ALEX", d.getSkinType() == SkinType.ALEX)
					.create();
			els.add(skinTypeSel);

			els.add(Button.builder()
					.text("Update skin type")
					.onClick(SendJSAction.of("shittyauth", "setSkinType", ActionValue.object().put("type", ActionValue.elementValue(skinTypeSel))).onSuccess(ShowToastAction.info("Updated!")))
					.create());
		});

		VerticalSpacer sp1 = new VerticalSpacer("30px");
		sp1.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
		s.addElement(sp1);

		s.addElement(TitleText.builder()
				.leftboundText()
				.text("Skin")
				.fullWidth()
				.create());

		FileUpload uploadSkin = FileUpload.builder()
				.uploadHandler("shittyauth", "uploadSkin")
				.create();
		s.addElement(uploadSkin);

		s.addElement(Button.builder()
				.text("Upload Skin")
				.onClick(SubmitUploadAction.of(uploadSkin).onSuccess(ShowToastAction.info("Skin uploaded!")))
				.create());

		VerticalSpacer sp2 = new VerticalSpacer("30px");
		sp2.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
		s.addElement(sp2);

		s.addElement(TitleText.builder()
				.leftboundText()
				.text("Cape")
				.fullWidth()
				.create());

		FileUpload uploadCape = FileUpload.builder()
				.uploadHandler("shittyauth", "uploadCape")
				.create();
		s.addElement(uploadCape);

		s.addElement(Button.builder()
				.text("Upload Cape")
				.onClick(SubmitUploadAction.of(uploadCape).onSuccess(ShowToastAction.info("Cape uploaded!")))
				.create());

		s.addElement(Text.builder()
				.leftboundText()
				.text("Enable cape?")
				.create());

		s.dynamic(() -> {
			UserData d = ShittyAuth.dataStorage.getUserData(Session.getCurrentSession().getAccountID());

			return CheckBox.builder()
					.initialState(d.hasCape())
					.onChange(ch -> SendJSAction.of("shittyauth", "setEnableCape", ActionValue.object().put("enable", ActionValue.checkboxValue(ch))).onSuccess(ShowToastAction.info("Updated!")))
					.create();
		});

		addSection(s);
	}

}
