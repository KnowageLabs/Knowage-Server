/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.security.oauth2;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Servlet Filter implementation class OAuthFilter
 *
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 * @author Jeremy Branham (jeremy@savantly.net)
 */
public class OAuth2Filter implements Filter {
	static private Logger logger = Logger.getLogger(OAuth2Filter.class);

	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		logger.debug("IN");

		HttpServletRequest httpServletRequest = ((HttpServletRequest) request);
		HttpSession session = httpServletRequest.getSession();

		OAuth2Config oauth2Config = OAuth2Config.getInstance();

		if (session.isNew() || session.getAttribute("access_token") == null) {
			final String oauth2RedirectUrl = oauth2Config.getRedirectUrl();
			if (httpServletRequest.getParameter("code") == null) {

				// Store the currently requested url in session so we can redirect after authentication
				final String postAuthRedirectUrl = oauth2Config.getRedirectAddress() + httpServletRequest.getRequestURI() + "?" + httpServletRequest.getQueryString();
				session.setAttribute("redirectUrl", postAuthRedirectUrl);
				
				// We have to retrieve the Oauth2's code redirecting the browser
				// to the OAuth2 provider
				
				String url = oauth2Config.getAuthorizeUrl();
				url += "?response_type=code&client_id=" + oauth2Config.getClientId();
				url += "&scope=" + OAuth2Config.getInstance().getScopes();
				url += "&redirect_uri=" + URLEncoder.encode(oauth2RedirectUrl + "", "UTF-8");
				url += "&state=" + URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8");
				logger.debug("redirecting to login: " + url);
				((HttpServletResponse) response).sendRedirect(url);
			} else {
				// Using the code we get the access token and put it in session
				final OAuth2Client client = new OAuth2Client();
				final String code = ((HttpServletRequest) request).getParameter("code");
				logger.debug("using authorization code to fetch access token: " + code);
				final String accessToken = client.getAccessTokenWithRedirect(code, oauth2RedirectUrl);
				session.setAttribute("access_token", accessToken);

				final String postAuthRedirectUrl = Optional.ofNullable(session.getAttribute("redirectUrl"))
						.orElse(oauth2RedirectUrl).toString();
				logger.debug("redirecting to initially requested page: " + postAuthRedirectUrl);
				((HttpServletResponse) response).sendRedirect(postAuthRedirectUrl);
			}
		} else {
			// pass the request along the filter chain
			chain.doFilter(request, response);
		}

		logger.debug("OUT");
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(FilterConfig fConfig) throws ServletException {

	}

}
