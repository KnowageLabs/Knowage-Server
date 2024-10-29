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
import java.net.URL;
import java.security.interfaces.RSAPublicKey;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;

import it.eng.spagobi.services.oauth2.Oauth2SsoService;
import it.eng.spagobi.tools.dataset.notifier.fiware.OAuth2Utils;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This filter forwards incoming requests into /oauth2/authorization_code/flow.jsp (where OAuth2 standard authorization code flow actually occurs), until
 * request contains OAuth2 access token (propagated by the above jsp file itself); then access token is set into session.
 *
 * @author Davide Zerbetto
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 */
public class OAuth2Filter implements Filter {

	private static final Logger LOGGER = LogManager.getLogger(OAuth2Filter.class);

	private OAuth2FlowManager flowManager = new NoFlowManager();

	private interface OAuth2FlowManager {
		void manage(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException;
	}

	private static class ImplicitFlowManager implements OAuth2FlowManager {

		@Override
		public void manage(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

			LOGGER.debug("Managing OAuth2 in implicit way");

			HttpSession session = request.getSession();
			OAuth2Config config = OAuth2Config.getInstance();
			String idToken = request.getParameter(Oauth2SsoService.ID_TOKEN);

			if (idToken != null) {
				// request contains id token --> set it in session and continue with filters chain
				LOGGER.info("ID token found: [{}]", idToken);
				validateIdToken(idToken, session);
				LOGGER.info("ID token validated successfully");
				session.setAttribute(Oauth2SsoService.ID_TOKEN, idToken);
				chain.doFilter(request, response);
			} else {
				if (session.isNew() || session.getAttribute(Oauth2SsoService.ID_TOKEN) == null) {
					// OAuth2 flow must take place --> stop filters chain
					manageNonce(session);
					LOGGER.info("ID token not found, starting OIDC flow...");
					request.getRequestDispatcher(config.getFlowJSPPath()).forward(request, response);
				} else {
					// session is already initialized --> continue with filters chain
					chain.doFilter(request, response);
				}
			}
		}

		private void manageNonce(HttpSession session) {
			Object nonceFromSession = session.getAttribute(Oauth2SsoService.NONCE);
			// in case nonce is not defined, generate it and put it in session
			if (nonceFromSession == null) {
				String nonce = OAuth2Utils.createNonce();
				LOGGER.info("Nonce generated : [{}]", nonce);
				session.setAttribute(Oauth2SsoService.NONCE, nonce);
			}

		}

		private void validateIdToken(String idToken, HttpSession session) {
			LOGGER.info("Input JWT token is [{}]", idToken);
			try {
				DecodedJWT decodedJWT = JWT.decode(idToken);
				LOGGER.info("JWT token properly decoded");
				// verify token
				JwkProvider provider = new JwkProviderBuilder(new URL(OAuth2Config.getInstance().getJWKSUrl())).build();
				Jwk jwk = provider.get(decodedJWT.getKeyId());
				Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
				Verification verifier = JWT.require(algorithm);
				verifier.build().verify(idToken);
				// check that issuer matches the configured one
				if (!decodedJWT.getIssuer().equals(OAuth2Config.getInstance().getJwtTokenIssuer())) {
					LOGGER.error("JWT token issuer [{}] does not match the configured one, that is [{}]", decodedJWT.getIssuer(),
							OAuth2Config.getInstance().getJwtTokenIssuer());
					throw new SpagoBIRuntimeException("JWT token issuer does not match the configured one");
				}
				// check that aud matches client id
				if (!decodedJWT.getAudience().get(0).equals(OAuth2Config.getInstance().getClientId())) {
					LOGGER.error("JWT token aud [{}] does not match the client id, that is [{}]", decodedJWT.getAudience().get(0),
							OAuth2Config.getInstance().getClientId());
					throw new SpagoBIRuntimeException("JWT token aud does not match the client id");
				}
				// check that nonce matches the generated one
				String generatedNonce = (String) session.getAttribute(Oauth2SsoService.NONCE);
				LOGGER.info("generatedNonce [{}]",generatedNonce);
				Claim nonceClaim = decodedJWT.getClaim(Oauth2SsoService.NONCE);

				if (nonceClaim.isNull() || !nonceClaim.asString().equals(generatedNonce)) {
					LOGGER.info("nonceClaim.asString() [{}]",nonceClaim.asString());
					LOGGER.error("JWT token nonce [{}] does not match the generated nonce, that is [{}]", nonceClaim, generatedNonce);
					throw new SpagoBIRuntimeException("JWT token nonce does not match the generated nonce");
				}
				LOGGER.info("JWT token verified.");
			} catch (Exception e) {
				LOGGER.error("An error occurred while verifying ID TOKEN", e);
				throw new SpagoBIRuntimeException("An error occurred while verifying ID TOKEN", e);
			}

		}

	}

	private static class ClassicFlowManager implements OAuth2FlowManager {

		@Override
		public void manage(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

			LOGGER.info("Managing OAuth2 in a classic way");

			HttpSession session = request.getSession();
			OAuth2Config config = OAuth2Config.getInstance();
			String accessToken = request.getParameter("access_token");

			if (accessToken != null) {
				// request contains access token --> set it in session and continue with filters chain
				LOGGER.info("Access token found: [{}]", accessToken);
				session.setAttribute(Oauth2SsoService.ACCESS_TOKEN, accessToken);
				chain.doFilter(request, response);
			} else {
				if (session.isNew() || session.getAttribute(Oauth2SsoService.ACCESS_TOKEN) == null) {
					// OAuth2 flow must take place --> stop filters chain
					LOGGER.info("Access token not found, starting OAuth2 flow...");
					request.getRequestDispatcher(config.getFlowJSPPath()).forward(request, response);
				} else {
					// session is already initialized --> continue with filters chain
					chain.doFilter(request, response);
				}
			}
		}

	}

	private static class NoFlowManager implements OAuth2FlowManager {

		@Override
		public void manage(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

			LOGGER.debug("No OAuth2 management");

			chain.doFilter(request, response);
		}

	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		OAuth2Config config = OAuth2Config.getInstance();
		OAuth2Config.FlowType flowType = config.getFlowType();

		switch (flowType) {
		case OIDC_IMPLICIT:
			flowManager = new ImplicitFlowManager();
			break;
		case PKCE:
		case AUTHORIZATION_CODE:
			flowManager = new ClassicFlowManager();
			break;
		case NONE:
		default:
			flowManager = new NoFlowManager();
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		LOGGER.debug("Executing OAuth2 filter");

		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			flowManager.manage(httpRequest, httpResponse, chain);

		} else {
			LOGGER.warn("Non HTTP request. We ignore it...");
		}

		LOGGER.debug("Ending OAuth2 filter");
	}

}
