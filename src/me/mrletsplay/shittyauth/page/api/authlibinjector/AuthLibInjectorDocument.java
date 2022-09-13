package me.mrletsplay.shittyauth.page.api.authlibinjector;

import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.Webinterface;

public class AuthLibInjectorDocument implements HttpDocument {

	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		String page = ctx.getPathParameters().get("page");
		HttpDocument doc = Webinterface.getDocumentProvider().getDocument("/" + page);
		if(doc == null) {
			Webinterface.getDocumentProvider().get404Document().createContent();
			return;
		}

		doc.createContent();
	}

}
