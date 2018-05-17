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
import java.net.URLEncoder;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

/**
 * Servlet Filter implementation class OAuthFilter
 *
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 */
public class OAuth2Filter implements Filter {
	static private Logger logger = Logger.getLogger(OAuth2Filter.class);

	String clientId;
	String secret;
	String redirectUri;

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

		Properties oauth2Config = OAuth2Config.getInstance().getConfig();

		if (session.isNew() || session.getAttribute("access_token") == null) {
			if (((HttpServletRequest) request).getParameter("code") == null) {
				// We have to retrieve the Oauth2's code redirecting the browser
				// to the OAuth2 provider
				String url = oauth2Config.getProperty("AUTHORIZE_URL");
				url += "?response_type=code&client_id=" + OAuth2Config.getInstance().getConfig().getProperty("CLIENT_ID");
				url += "&redirect_uri=" + URLEncoder.encode(oauth2Config.getProperty("REDIRECT_URI"), "UTF-8");
				if (oauth2Config.containsKey("STATE")) {
					url += "&state=" + URLEncoder.encode(oauth2Config.getProperty("STATE"), "UTF-8");
				}
				((HttpServletResponse) response).sendRedirect(url);
			} else {
				// Using the code we get the access token and put it in session
				OAuth2Client client = new OAuth2Client();
				String accessToken = client.getAccessToken(((HttpServletRequest) request).getParameter("code"));
				/*
				 * author radmila.selakovic@mht.net setting access token in request header
				 */
				String url = oauth2Config.getProperty("REST_BASE_URL") + oauth2Config.getProperty("ROLES_PATH") + "?application_id="
						+ oauth2Config.getProperty("APPLICATION_ID");
				GetMethod httpget = new GetMethod(url);
				httpget.addRequestHeader("X-Auth-Token", accessToken);
				session.setAttribute("access_token", accessToken);

				((HttpServletResponse) response).sendRedirect(((HttpServletRequest) request).getContextPath());
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
