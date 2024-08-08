package it.eng.knowage.commons.utilities;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class KnLanguageCookie {

	private KnLanguageCookie() {
	}

	public static Cookie getCookie(String knLanguage) {
		Cookie cookie = new Cookie("kn.lang", knLanguage);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		return cookie;
	}

	public static void setCookie(HttpServletResponse resp, String knLanguage) {
		Cookie c = getCookie(knLanguage);
		resp.addCookie(c);
	}

}