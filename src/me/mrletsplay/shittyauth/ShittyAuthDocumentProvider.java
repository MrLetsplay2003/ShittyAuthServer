package me.mrletsplay.shittyauth;

import me.mrletsplay.shittyauth.page.ProfilePage;
import me.mrletsplay.shittyauth.page.api.UserCapeDocument;
import me.mrletsplay.shittyauth.page.api.UserSkinDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyUserCapeDocument;
import me.mrletsplay.shittyauth.page.api.legacy.LegacyUserSkinDocument;
import me.mrletsplay.webinterfaceapi.http.document.HttpDocument;
import me.mrletsplay.webinterfaceapi.webinterface.document.WebinterfaceDocumentProvider;

public class ShittyAuthDocumentProvider extends WebinterfaceDocumentProvider {
	
	public static final ShittyAuthDocumentProvider INSTANCE = new ShittyAuthDocumentProvider();
	
	@Override
	public HttpDocument getDocument(String path) {
		if(path.startsWith(ProfilePage.PATH_PREFIX)) {
			return ProfilePage.INSTANCE;
		}
		
		if(path.startsWith(UserSkinDocument.PATH_PREFIX)) {
			return UserSkinDocument.INSTANCE;
		}
		
		if(path.startsWith(UserCapeDocument.PATH_PREFIX)) {
			return UserCapeDocument.INSTANCE;
		}
		
		if(path.startsWith(LegacyUserSkinDocument.PATH_PREFIX)) {
			return LegacyUserSkinDocument.INSTANCE;
		}
		
		if(path.startsWith(LegacyUserCapeDocument.PATH_PREFIX)) {
			return LegacyUserCapeDocument.INSTANCE;
		}
		
		return super.getDocument(path);
	}

}
