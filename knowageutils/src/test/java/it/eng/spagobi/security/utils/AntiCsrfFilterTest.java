package it.eng.spagobi.security.utils;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Proxy;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;

public class AntiCsrfFilterTest {

	@Test
	public void shouldAllowBrandingImageReadsWithAnExistingSessionAndNoCsrfHeader() throws Exception {
		assertEquals(200, filter("GET", "/loginconfig/images/logo.png", null, null));
		assertEquals(200, filter("GET", "/loginconfig/images/background.jpg", "token", null));
		assertEquals(200, filter("GET", "/loginconfig/images/favicon.ico", "token", "stale-token"));
	}

	@Test
	public void shouldKeepBrandingUploadsAndWritesProtected() throws Exception {
		assertEquals(403, filter("POST", "/loginconfig/images", null, null));
		assertEquals(403, filter("PUT", "/loginconfig/branding", "token", null));
		assertEquals(403, filter("POST", "/loginconfig/images/logo.png", "token", null));
		assertEquals(403, filter("DELETE", "/loginconfig/images/logo.png", "token", null));
	}

	@Test
	public void shouldNotExemptOtherLoginConfigurationPaths() throws Exception {
		assertEquals(403, filter("GET", "/loginconfig", "token", null));
		assertEquals(403, filter("GET", "/loginconfig/images", "token", null));
		assertEquals(403, filter("GET", "/loginconfig/images-other/logo.png", "token", null));
		assertEquals(403, filter("GET", null, "token", null));
	}

	@Test
	public void shouldStillValidateCsrfTokensForBrandingWrites() throws Exception {
		assertEquals(200, filter("POST", "/loginconfig/images", "token", "token"));
		assertEquals(200, filter("PUT", "/loginconfig/branding", "token", "token"));
		assertEquals(403, filter("POST", "/loginconfig/images", "token", "wrong-token"));
		assertEquals(403, filter("PUT", "/loginconfig/branding", "token", "wrong-token"));
	}

	private int filter(String method, String path, String cookieToken, String headerToken) throws Exception {
		String tokenName = System.getProperty("csrfTokenName", System.getenv().getOrDefault("csrfTokenName", "X-CSRF-TOKEN"));
		HttpSession session = (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(),
				new Class<?>[] { HttpSession.class }, (proxy, invoked, args) -> null);
		HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, (proxy, invoked, args) -> {
					switch (invoked.getName()) {
					case "getMethod":
						return method;
					case "getPathInfo":
						return path;
					case "getSession":
						return session;
					case "getCookies":
						return cookieToken == null ? null : new Cookie[] { new Cookie(tokenName, cookieToken) };
					case "getHeader":
						return tokenName.equals(args[0]) ? headerToken : null;
					default:
						throw new AssertionError("Unexpected request method: " + invoked.getName());
					}
				});
		int[] status = { 0 };
		HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
				new Class<?>[] { HttpServletResponse.class }, (proxy, invoked, args) -> {
					if (!"sendError".equals(invoked.getName())) {
						throw new AssertionError("Unexpected response method: " + invoked.getName());
					}
					status[0] = (Integer) args[0];
					return null;
				});
		FilterChain chain = (req, resp) -> status[0] = 200;
		AntiCsrfFilter filter = new AntiCsrfFilter();
		filter.init(null);
		filter.doFilter(request, response, chain);
		return status[0];
	}
}
