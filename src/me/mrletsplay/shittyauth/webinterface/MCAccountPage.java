package me.mrletsplay.shittyauth.webinterface;

import me.mrletsplay.shittyauth.ShittyAuth;
import me.mrletsplay.shittyauth.textures.SkinType;
import me.mrletsplay.shittyauth.user.UserData;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ShowToastAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SubmitUploadAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.CheckboxValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceCheckBox;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceFileUpload;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSelect;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceTitleText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceVerticalSpacer;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;
import me.mrletsplay.webinterfaceapi.webinterface.session.WebinterfaceSession;

public class MCAccountPage extends WebinterfacePage {

	public MCAccountPage() {
		super("MC Account", "/mc/account");
		setIcon("mdi:minecraft");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.setSlimLayout(true);
		s.addLayoutOptions(new GridLayout("75fr", "25fr"));
		s.dynamic(els -> {
			UserData d = ShittyAuth.dataStorage.getUserData(WebinterfaceSession.getCurrentSession().getAccountID());
			
			els.add(WebinterfaceTitleText.builder()
					.leftboundText()
					.text("Skin Type")
					.fullWidth()
					.create());
			
			WebinterfaceSelect skinTypeSel = WebinterfaceSelect.builder()
					.addOption("Steve", "STEVE", d.getSkinType() == SkinType.STEVE)
					.addOption("Alex", "ALEX", d.getSkinType() == SkinType.ALEX)
					.create();
			els.add(skinTypeSel);
			
			els.add(WebinterfaceButton.builder()
					.text("Update skin type")
					.onClick(new SendJSAction("shittyauth", "setSkinType", new ElementValue(skinTypeSel)).onSuccess(ShowToastAction.info("Updated!")))
					.create());
		});
		
		WebinterfaceVerticalSpacer sp1 = new WebinterfaceVerticalSpacer("30px");
		sp1.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
		s.addElement(sp1);
		
		s.addElement(WebinterfaceTitleText.builder()
				.leftboundText()
				.text("Skin")
				.fullWidth()
				.create());
		
		WebinterfaceFileUpload uploadSkin = WebinterfaceFileUpload.builder()
				.uploadHandler("shittyauth", "uploadSkin")
				.create();
		s.addElement(uploadSkin);
		
		s.addElement(WebinterfaceButton.builder()
				.text("Upload Skin")
				.onClick(new SubmitUploadAction(uploadSkin).onSuccess(ShowToastAction.info("Skin uploaded!")))
				.create());
		
		WebinterfaceVerticalSpacer sp2 = new WebinterfaceVerticalSpacer("30px");
		sp2.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
		s.addElement(sp2);
		
		s.addElement(WebinterfaceTitleText.builder()
				.leftboundText()
				.text("Cape")
				.fullWidth()
				.create());
		
		WebinterfaceFileUpload uploadCape = WebinterfaceFileUpload.builder()
				.uploadHandler("shittyauth", "uploadCape")
				.create();
		s.addElement(uploadCape);
		
		s.addElement(WebinterfaceButton.builder()
				.text("Upload Cape")
				.onClick(new SubmitUploadAction(uploadCape).onSuccess(ShowToastAction.info("Cape uploaded!")))
				.create());

		s.addElement(WebinterfaceText.builder()
				.leftboundText()
				.text("Enable cape?")
				.create());
		
		s.dynamic(() -> {
			UserData d = ShittyAuth.dataStorage.getUserData(WebinterfaceSession.getCurrentSession().getAccountID());
			
			return WebinterfaceCheckBox.builder()
					.initialState(d.hasCape())
					.onChange(ch -> new SendJSAction("shittyauth", "setEnableCape", new CheckboxValue(ch)).onSuccess(ShowToastAction.info("Updated!")))
					.create();
		});
		
		addSection(s);
	}
	
}
