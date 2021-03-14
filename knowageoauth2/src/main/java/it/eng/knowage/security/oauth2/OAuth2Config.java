/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.knowage.security.oauth2;

import java.util.Objects;

import org.apache.log4j.Logger;

/**
 * @author Jeremy Branham (jeremy@savantly.net)
 *
 */
public class OAuth2Config {
	static private Logger logger = Logger.getLogger(OAuth2Config.class);
	private static OAuth2Config instance = null;
	private final String authorizeUrl;
	private final String redirectUrl;
	private final String clientId;
	private final String clientSecret;
	private final String tokenUrl;
	private final String userInfoUrl;
	private final String adminEmail;
	private final String scopes;
	
	public OAuth2Config() {
		this.authorizeUrl = System.getProperty("oauth_authorize_url", System.getenv("OAUTH2_AUTHORIZE_URL"));
		this.clientId = System.getProperty("oauth_client_id", System.getenv("OAUTH2_CLIENT_ID"));
		this.clientSecret = System.getProperty("oauth_client_secret", System.getenv("OAUTH2_CLIENT_SECRET"));
		this.redirectUrl = System.getProperty("oauth_redirect_url", System.getenv("OAUTH2_REDIRECT_URL"));
		this.tokenUrl = System.getProperty("oauth_token_url", System.getenv("OAUTH2_TOKEN_URL"));
		this.userInfoUrl = System.getProperty("oauth_user_info_url", System.getenv("OAUTH2_USER_INFO_URL"));
		this.adminEmail = System.getProperty("oauth_admin_email", System.getenv("OAUTH2_ADMIN_EMAIL"));
		
		String _scopes = System.getProperty("oauth_scopes", System.getenv("OAUTH2_SCOPES"));
		this.scopes = Objects.isNull(_scopes) ? "openid,profile" : _scopes;
		
		logger.debug("constructed OAuth2Config: " + this.toString());
	}

	public static OAuth2Config getInstance() {
		if (instance == null) {
			instance = new OAuth2Config();
		}
		return instance;
	}

	public String getAuthorizeUrl() {
		return this.authorizeUrl;
	}

	public String getRedirectUrl() {
		return this.redirectUrl;
	}

	public String getClientId() {
		return this.clientId;
	}

	public String getClientSecret() {
		return this.clientSecret;
	}

	public String getTokenUrl() {
		return this.tokenUrl;
	}
	
	public String getUserInfoUrl() {
		return this.userInfoUrl;
	}
	
	public String getScopes() {
		return this.scopes;
	}

	public String getAdminEmail() {
		return this.adminEmail;
	}

	@Override
	public String toString() {
		return "OAuth2Config [authorizeUrl=" + authorizeUrl + ", redirectUrl=" + redirectUrl + ", clientId=" + clientId
				+ ", clientSecret=" + clientSecret + ", tokenUrl=" + tokenUrl + ", userInfoUrl=" + userInfoUrl
				+ ", adminEmail=" + adminEmail + ", scopes=" + scopes + "]";
	}
	
	
}