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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

/**
 * @author Jeremy Branham (jeremy@savantly.net), Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class OAuth2Config {

	static private Logger logger = Logger.getLogger(OAuth2Config.class);

	private static OAuth2Config INSTANCE = new OAuth2Config();

	private final String authorizeUrl;
	private final String redirectUrl;
	private final String clientId;
	private final String clientSecret;
	private final String accessTokenUrl;
	private final String jwksUrl;
	private final String userInfoUrl;
	private final String adminId;
	private final String adminEmail;
	private final String adminPassword;
	private final String scopes;
	private final String userIdClaim;
	private final String userNameClaim;
	private final List<String> profileAttributes = new ArrayList<>();
	private final String restApiBaseUrl;
	private final String organizationInfoPath;
	private final String rolesPath;
	private final String applicationId;
	private final String tokenPath;
	private final String tokenBody;

	public OAuth2Config() {

		this.clientId = Optional.ofNullable(System.getProperty("oauth2_client_id", System.getenv("OAUTH2_CLIENT_ID")))
				.orElseThrow(() -> new RuntimeException("Missing OAUTH2_CLIENT_ID"));

		this.clientSecret = System.getProperty("oauth2_client_secret", System.getenv("OAUTH2_CLIENT_SECRET"));

		this.authorizeUrl = Optional.ofNullable(System.getProperty("oauth2_authorize_url", System.getenv("OAUTH2_AUTHORIZE_URL")))
				.orElseThrow(() -> new RuntimeException("Missing OAUTH2_AUTHORIZE_URL"));

		this.redirectUrl = Optional.ofNullable(System.getProperty("oauth2_redirect_url", System.getenv("OAUTH2_REDIRECT_URL")))
				.orElseThrow(() -> new RuntimeException("Missing OAUTH2_REDIRECT_URL"));

		this.accessTokenUrl = Optional.ofNullable(System.getProperty("oauth2_access_token_url", System.getenv("OAUTH2_ACCESS_TOKEN_URL")))
				.orElseThrow(() -> new RuntimeException("Missing OAUTH2_TOKEN_URL"));

		this.jwksUrl = System.getProperty("oauth2_jwks_url", System.getenv("OAUTH2_JWKS_URL"));

//		this.userInfoUrl = Optional.ofNullable(System.getProperty("oauth2_user_info_url", System.getenv("OAUTH2_USER_INFO_URL")))
//				.orElseThrow(() -> new RuntimeException("missing OAUTH2_USER_INFO_URL"));

		this.userInfoUrl = System.getProperty("oauth2_user_info_url", System.getenv("OAUTH2_USER_INFO_URL"));

//		this.adminEmail = Optional.ofNullable(System.getProperty("oauth2_admin_email", System.getenv("OAUTH2_ADMIN_EMAIL")))
//				.orElseThrow(() -> new RuntimeException("missing OAUTH2_ADMIN_EMAIL"));

		this.adminId = System.getProperty("oauth2_admin_id", System.getenv("OAUTH2_ADMIN_ID"));

		this.adminEmail = System.getProperty("oauth2_admin_email", System.getenv("OAUTH2_ADMIN_EMAIL"));

		this.adminPassword = System.getProperty("oauth2_admin_password", System.getenv("OAUTH2_ADMIN_PASSWORD"));

		this.scopes = Optional.ofNullable(System.getProperty("oauth2_scopes", System.getenv("OAUTH2_SCOPES"))).orElse("openid profile");

		this.userIdClaim = Optional.ofNullable(System.getProperty("oauth2_user_id_claim", System.getenv("OAUTH2_USER_ID_CLAIM"))).orElse("sub");

		this.userNameClaim = Optional.ofNullable(System.getProperty("oauth2_user_name_claim", System.getenv("OAUTH2_USER_NAME_CLAIM")))
				.orElse("preferred_username");

		final Optional<String> _attributes = Optional.ofNullable(System.getProperty("oauth2_profile_attributes", System.getenv("OAUTH2_PROFILE_ATTRIBUTES")));
		if (_attributes.isPresent()) {
			String[] parts = _attributes.get().split(",");
			for (int i = 0; i < parts.length; i++) {
				this.profileAttributes.add(parts[i]);
			}
		}

		this.restApiBaseUrl = System.getProperty("oauth2_rest_base_url", System.getenv("OAUTH2_REST_BASE_URL"));

		this.organizationInfoPath = Optional.ofNullable(System.getProperty("oauth2_organization_info_path", System.getenv("OAUTH2_ORGANIZATION_INFO_PATH")))
				.orElse("projects/");

		this.rolesPath = Optional.ofNullable(System.getProperty("oauth2_roles_path", System.getenv("OAUTH2_ROLES_PATH"))).orElse("applications/{0}/roles");

		this.applicationId = System.getProperty("oauth2_application_id", System.getenv("OAUTH2_APPLICATION_ID"));

		this.tokenPath = Optional.ofNullable(System.getProperty("oauth2_token_path", System.getenv("OAUTH2_TOKEN_PATH"))).orElse("auth/tokens");

		this.tokenBody = Optional.ofNullable(System.getProperty("oauth2_token_body", System.getenv("OAUTH2_TOKEN_BODY")))
				.orElse("{\"name\": \"{0}\",\"password\": \"{1}\"}");

		logger.debug("constructed OAuth2Config: " + this.toString());
	}

	public static OAuth2Config getInstance() {
		return INSTANCE;
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

	public String getAccessTokenUrl() {
		return this.accessTokenUrl;
	}

	public String getUserInfoUrl() {
		return this.userInfoUrl;
	}

	public String getJWKSUrl() {
		return this.jwksUrl;
	}

	public String getScopes() {
		return this.scopes;
	}

	public String getAdminId() {
		return this.adminId;
	}

	public String getAdminEmail() {
		return this.adminEmail;
	}

	public String getAdminPassword() {
		return this.adminPassword;
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

	public String getRestAPIBaseUrl() {
		return this.restApiBaseUrl;
	}

	public String getOrganizationInfoPath() {
		return this.organizationInfoPath;
	}

	public String getRolesPath() {
		return this.rolesPath;
	}

	public String getApplicationId() {
		return this.applicationId;
	}

	public String getTokenPath() {
		return this.tokenPath;
	}

	public String getTokenBody() {
		return this.tokenBody;
	}

	@Override
	public String toString() {
		return "OAuth2Config [authorizeUrl=" + authorizeUrl + ", redirectUrl=" + redirectUrl + ", clientId=" + clientId + ", clientSecret=" + clientSecret
				+ ", accessTokenUrl=" + accessTokenUrl + ", jwksUrl=" + jwksUrl + ", userInfoUrl=" + userInfoUrl + ", adminId=" + adminId + ", adminEmail="
				+ adminEmail + ", adminPassword=" + adminPassword + ", scopes=" + scopes + ", userIdClaim=" + userIdClaim + ", userNameClaim=" + userNameClaim
				+ ", profileAttributes=" + profileAttributes + ", restApiBaseUrl=" + restApiBaseUrl + ", organizationInfoPath=" + organizationInfoPath
				+ ", rolesPath=" + rolesPath + ", applicationId=" + applicationId + ", tokenPath=" + tokenPath + ", tokenBody=" + tokenBody + "]";
	}

}