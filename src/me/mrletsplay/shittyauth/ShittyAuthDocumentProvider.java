package me.mrletsplay.shittyauth;

import me.mrletsplay.shittyauth.page.ProfilePage;
import me.mrletsplay.shittyauth.page.UserLegacySkinDocument;
import me.mrletsplay.webinterfaceapi.http.document.HttpDocument;
import me.mrletsplay.webinterfaceapi.webinterface.document.WebinterfaceDocumentProvider;

public class ShittyAuthDocumentProvider extends WebinterfaceDocumentProvider {
	
	public static final ShittyAuthDocumentProvider INSTANCE = new ShittyAuthDocumentProvider();
	
	@Override
	public HttpDocument getDocument(String path) {
		System.out.println("REQ: " + path);
		if(path.startsWith(ProfilePage.PATH_PREFIX)) {
			return ProfilePage.INSTANCE;
		}
		
		if(path.startsWith(UserLegacySkinDocument.PATH_PREFIX)) {
			return UserLegacySkinDocument.INSTANCE;
		}
		
		return super.getDocument(path);
	}

}
