package it.eng.spagobi.security.saml;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.auth0.jwt.exceptions.JWTVerificationException;

import it.eng.spago.security.IEngUserProfile;

public class FederatedSessionFilter implements Filter {
	@Override
	public void init(FilterConfig config) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		HttpSession session = req.getSession(false);
		String path = req.getRequestURI().substring(req.getContextPath().length());
		boolean callback = "POST".equals(req.getMethod()) && ("/restful-services/saml/acs".equals(path)
				|| "/restful-services/saml/slo".equals(path) || "/restful-services/saml/exchange".equals(path));
		try {
			if (session != null) {
				Object profile = session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
				if (profile instanceof IEngUserProfile) {
					Object token = ((IEngUserProfile) profile).getUserUniqueIdentifier();
					FederatedTokenGuard.check(token == null ? null : token.toString());
				}
			}
			FederatedTokenGuard.check(req.getParameter("user_id"));
			FederatedTokenGuard.check(req.getParameter("userId"));
			for (String name : Collections.list(req.getHeaderNames())) {
				String value = req.getHeader(name);
				if (value != null && value.regionMatches(true, 0, "Bearer ", 0, 7)) {
					FederatedTokenGuard.check(value.substring(7));
				} else if (FederatedTokenGuard.isFederated(value)) {
					FederatedTokenGuard.check(value);
				}
			}
		} catch (JWTVerificationException e) {
			if (session != null) {
				session.invalidate();
			}
			if (!callback) {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Federated session expired or revoked");
				return;
			}
		}
		chain.doFilter(request, response);
	}
}
