/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.knowage.security.oauth2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

/**
 * @author Jeremy Branham (jeremy@savantly.net)
 *
 */
public class OAuth2Config {
	static private Logger logger = Logger.getLogger(OAuth2Config.class);
	private static OAuth2Config instance = null;
	private final String authorizeUrl;
	private final String redirectAddress;
	private final String redirectPath;
	private final String clientId;
	private final String clientSecret;
	private final String tokenUrl;
	private final String userInfoUrl;
	private final String adminEmail;
	private final String scopes;
	private final String userIdClaim;
	private final String userNameClaim;
	private final List<String> profileAttributes = new ArrayList<>();
	
	public OAuth2Config() {
		this.authorizeUrl = Optional.ofNullable(System.getProperty("oauth_authorize_url", System.getenv("OAUTH2_AUTHORIZE_URL")))
				.orElseThrow(() -> new RuntimeException("missing OAUTH2_AUTHORIZE_URL"));
		
		this.clientId = System.getProperty("oauth_client_id", System.getenv("OAUTH2_CLIENT_ID"));
		this.clientSecret = System.getProperty("oauth_client_secret", System.getenv("OAUTH2_CLIENT_SECRET"));
		
		this.redirectAddress = Optional.ofNullable(System.getProperty("oauth_redirect_address", System.getenv("OAUTH2_REDIRECT_ADDRESS")))
				.orElse("http://localhost:8080");
		
		this.redirectPath = Optional.ofNullable(System.getProperty("oauth_redirect_path", System.getenv("OAUTH2_REDIRECT_PATH")))
				.orElse("/knowage/servlet/AdapterHTTP?PAGE=LoginPage");
		
		this.tokenUrl = Optional.ofNullable(System.getProperty("oauth_token_url", System.getenv("OAUTH2_TOKEN_URL")))
				.orElseThrow(() -> new RuntimeException("missing OAUTH2_TOKEN_URL"));
		
		this.userInfoUrl = Optional.ofNullable(System.getProperty("oauth_user_info_url", System.getenv("OAUTH2_USER_INFO_URL")))
				.orElseThrow(() -> new RuntimeException("missing OAUTH2_USER_INFO_URL"));
		
		this.adminEmail = Optional.ofNullable(System.getProperty("oauth_admin_email", System.getenv("OAUTH2_ADMIN_EMAIL")))
				.orElseThrow(() -> new RuntimeException("missing OAUTH2_ADMIN_EMAIL"));
		
		this.scopes = Optional.ofNullable(System.getProperty("oauth_scopes", System.getenv("OAUTH2_SCOPES")))
				.orElse("openid profile");

		this.userIdClaim = Optional.ofNullable(System.getProperty("oauth_user_id_claim", System.getenv("OAUTH2_USER_ID_CLAIM")))
				.orElse("sub");

		this.userNameClaim = Optional.ofNullable(System.getProperty("oauth_user_name_claim", System.getenv("OAUTH2_USER_NAME_CLAIM")))
				.orElse("preferred_username");
		
		final Optional<String> _attributes = Optional.ofNullable(System.getProperty("oauth_profile_attributes", System.getenv("OAUTH2_PROFILE_ATTRIBUTES")));
		if(_attributes.isPresent()) {
			String[] parts = _attributes.get().split(",");
			for (int i = 0; i < parts.length; i++) {
				this.profileAttributes.add(parts[i]);
			}
		}
		
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
	
	public String getRedirectAddress() {
		return this.redirectAddress;
	}

	public String getRedirectUrl() {
		return this.redirectAddress + this.redirectPath;
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

	public String getUserIdClaim() {
		return userIdClaim;
	}

	public String getUserNameClaim() {
		return userNameClaim;
	}
	
	public List<String> getProfileAttributes() {
		return this.profileAttributes;
	}

	@Override
	public String toString() {
		return "OAuth2Config [authorizeUrl=" + authorizeUrl + ", redirectAddress=" + redirectAddress + ", redirectPath="
				+ redirectPath + ", clientId=" + clientId + ", clientSecret=" + clientSecret + ", tokenUrl=" + tokenUrl
				+ ", userInfoUrl=" + userInfoUrl + ", adminEmail=" + adminEmail + ", scopes=" + scopes
				+ ", userIdClaim=" + userIdClaim + ", userNameClaim=" + userNameClaim + ", profileAttributes="
				+ profileAttributes + ", getRedirectUrl()=" + getRedirectUrl() + "]";
	}
	

}