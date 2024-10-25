package it.eng.spagobi.security.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AntiCsrfFilter implements Filter {

	private static final String DEFAULT_TOKEN_NAME = "X-CSRF-TOKEN";
	private static final String TOKEN_NAME_PROP = "csrfTokenName";

	private String csrfTokenName;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// recupero il token name dalle cponfigurazioni, properties, env oppure uso default
		// @formatter:off
		csrfTokenName = Stream.of(System.getProperty(TOKEN_NAME_PROP), System.getenv(TOKEN_NAME_PROP))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(DEFAULT_TOKEN_NAME);
		// @formatter:on
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// controllo per escludere le login dal check csrf

		HttpServletRequest req = ((HttpServletRequest) request);

		if (req.getSession(false) != null && !this.excludeCheck(req.getPathInfo())) {

			String cookieVal = "";
			String paramVal;
			// getCookie

			req.getSession(false);
			Cookie[] cookies = req.getCookies();
			boolean cFound = false;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(csrfTokenName)) {
						cFound = true;
						cookieVal = cookie.getValue();
					}
				}
			}
			paramVal = req.getHeader(csrfTokenName);

			if ((!(cFound && paramVal != null)) || (!cookieVal.equals(paramVal))) {
				HttpServletResponse resp = (HttpServletResponse) response;
				// sparo direttamente 403
				resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
				return;
			}
		}
		chain.doFilter(request, response);
	}

	private boolean excludeCheck(String path) {
		List<String> urlToExclude = new ArrayList<>();
		urlToExclude.add("/signup/prepare");

		if (urlToExclude.contains(path)) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public void destroy() {

	}

}
