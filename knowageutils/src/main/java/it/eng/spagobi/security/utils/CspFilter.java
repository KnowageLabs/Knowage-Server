package it.eng.spagobi.security.utils;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.eng.spagobi.utilities.csp.CSPSingleton;

public class CspFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO document why this method is empty
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String cspPolicy = CSPSingleton.getInstance().getCspPolicy();
		if (cspPolicy != null) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			// Generate a secure random nonce
			String nonce = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
			// Store the nonce in the request so it can be used in JSP or templates
			httpRequest.setAttribute("cspNonce", nonce);

			// Replace the placeholder with the actual nonce
			String updatedPolicy = cspPolicy.replace("rAnd0m", nonce);

			// Set the updated CSP header in the response
			httpResponse.setHeader("Content-Security-Policy", updatedPolicy);
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// TODO document why this method is empty
	}

}
