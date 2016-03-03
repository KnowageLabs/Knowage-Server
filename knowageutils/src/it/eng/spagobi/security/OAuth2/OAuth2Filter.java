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

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
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

import sun.misc.BASE64Encoder;

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
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		logger.debug("IN");

		HttpSession session = ((HttpServletRequest) request).getSession();

		if (session.isNew() || session.getAttribute("access_token") == null) {
			if (((HttpServletRequest) request).getParameter("code") == null) {
				String url = "https://account.lab.fiware.org/authorize?response_type=code&client_id=" + clientId;
				((HttpServletResponse) response).sendRedirect(url);
			} else {
				String authorizationCredentials = clientId + ":" + secret;
				String encoded = new String(new BASE64Encoder().encode(authorizationCredentials.getBytes()));
				encoded = encoded.replaceAll("\n", "");

				URL url = new URL("https://account.lab.fiware.org/token");

				// HttpsURLConnection
				HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

				// Add request header
				connection.setDoOutput(true);
				connection.setRequestProperty("Authorization", "Basic " + encoded);
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestMethod("POST");

				connection.setConnectTimeout(10000);
				connection.setReadTimeout(10000);

				// Add request body
				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				String body = "grant_type=authorization_code&code=" + ((HttpServletRequest) request).getParameter("code") + "&redirect_uri=" + redirectUri;
				out.write(body);
				out.close();

				JsonReader jsonReader = Json.createReader(connection.getInputStream());
				JsonObject jsonObject = jsonReader.readObject();
				jsonReader.close();
				connection.disconnect();

				String accessToken = jsonObject.getString("access_token");

				session = ((HttpServletRequest) request).getSession();
				session.setAttribute("access_token", accessToken);
				((HttpServletResponse) response).sendRedirect("http://localhost:8080/SpagoBI/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE");
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
	public void init(FilterConfig fConfig) throws ServletException {
		logger.debug("IN");

		ResourceBundle resourceBundle = null;
		String configFile = "it.eng.spagobi.security.OAuth2.configs";

		try {
			resourceBundle = ResourceBundle.getBundle(configFile);

			clientId = resourceBundle.getString("CLIENT_ID");
			secret = resourceBundle.getString("SECRET");
			redirectUri = resourceBundle.getString("REDIRECT_URI");
		} catch (MissingResourceException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRuntimeException("Impossible to find configurations file [" + configFile + "]", e);
		} finally {
			logger.debug("OUT");
		}
	}
}
