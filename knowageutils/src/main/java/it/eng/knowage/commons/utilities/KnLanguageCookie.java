package it.eng.knowage.commons.utilities;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class KnLanguageCookie {

	private KnLanguageCookie() {
	}

	public static Cookie getCookie(String knLanguage) {
		Cookie localeCookie_fl = new Cookie("kn.lang", knLanguage);
		localeCookie_fl.setHttpOnly(true);
		localeCookie_fl.setPath("/");
		return localeCookie_fl;
	}

	public static void setCookie(HttpServletResponse resp, String knLanguage) {
		Cookie c = getCookie(knLanguage);
		resp.addCookie(c);
	}

}
