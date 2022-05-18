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
package it.eng.spagobi.security.OAuth2;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.services.oauth2.Oauth2SsoService;

/**
 * This filter forwards incoming requests into /oauth2/pkce/flow.jsp (where OAuth2 PKCE flow actually occurs by plain JavaScript code), until request contains
 * OAuth2 access token (propagated by the above jsp file itself); then access token is set into session.
 *
 * @author Davide Zerbetto
 *
 */
public class OAuth2PKCEFilter implements Filter {

	static private Logger logger = Logger.getLogger(OAuth2PKCEFilter.class);

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

		HttpSession session = ((HttpServletRequest) request).getSession();
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		String accessToken = httpRequest.getParameter("access_token");

		if (accessToken != null) {
			// request contains access token --> set it in session and continue with filters chain
			LogMF.debug(logger, "Access token found: [{0}]", accessToken);
			session.setAttribute(Oauth2SsoService.ACCESS_TOKEN, accessToken);
			chain.doFilter(request, response);
		} else {
			if (session.isNew() || session.getAttribute(Oauth2SsoService.ACCESS_TOKEN) == null) {
				// OAuth2 PKCE must take place --> stop filters chain
				logger.debug("Access token not found, starting OAuth2 PKCE flow...");
				request.getRequestDispatcher("/oauth2/pkce/flow.jsp").forward(request, response);
			} else {
				// session is already initialized --> continue with filters chain
				chain.doFilter(request, response);
			}
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
