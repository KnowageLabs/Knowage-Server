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

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jeremy Branham (jeremy@savantly.net), Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class OAuth2Config {

	public enum FlowType {
		PKCE, AUTHORIZATION_CODE, OIDC_IMPLICIT, NONE;
	}

	public enum ClientAuthenticationMethod {
		CLIENT_SECRET_BASIC, CLIENT_SECRET_POST
	}

	private static final Logger LOGGER = LogManager.getLogger(OAuth2Config.class);
	private static final OAuth2Config INSTANCE = new OAuth2Config();

	private final FlowType type;
	private final String authorizeUrl;
	private final String redirectUrl;
	private final String clientId;
	private final String clientSecret;
	private final String accessTokenUrl;
	private final ClientAuthenticationMethod clientAuthenticationMethod;
	private final String jwksUrl;
	private final String userInfoUrl;
	private final String adminId;
	private final String adminEmail;
	private final String adminPassword;
	private final String scopes;
	private final String userIdClaim;
	private final String userNameClaim;
	private final String userEmailClaim;
	private final List<String> profileAttributes = new ArrayList<>();
	private final String restApiBaseUrl;
	private final String organizationInfoPath;
	private final String rolesPath;
	private final String applicationId;
	private final String tokenPath;
	private final String tokenBody;
	private final String jwtTokenIssuer;
	private final String idTokenJsonRolesPath;
	private final boolean nonProfiledUserAllowed;

	public OAuth2Config() {

		String typeStr = Optional.ofNullable(System.getProperty("oauth2_flow_type", System.getenv("OAUTH2_FLOW_TYPE"))).orElse("").toUpperCase();

		if (EnumUtils.isValidEnum(FlowType.class, typeStr) && FlowType.valueOf(typeStr) != FlowType.NONE) {
			type = FlowType.valueOf(typeStr);

			clientId = Optional.ofNullable(System.getProperty("oauth2_client_id", System.getenv("OAUTH2_CLIENT_ID")))
					.orElseThrow(() -> new RuntimeException("Missing OAUTH2_CLIENT_ID"));

			clientSecret = System.getProperty("oauth2_client_secret", System.getenv("OAUTH2_CLIENT_SECRET"));

			authorizeUrl = Optional.ofNullable(System.getProperty("oauth2_authorize_url", System.getenv("OAUTH2_AUTHORIZE_URL")))
					.orElseThrow(() -> new RuntimeException("Missing OAUTH2_AUTHORIZE_URL"));

			redirectUrl = Optional.ofNullable(System.getProperty("oauth2_redirect_url", System.getenv("OAUTH2_REDIRECT_URL")))
					.orElseThrow(() -> new RuntimeException("Missing OAUTH2_REDIRECT_URL"));

			accessTokenUrl = Optional.ofNullable(System.getProperty("oauth2_access_token_url", System.getenv("OAUTH2_ACCESS_TOKEN_URL")))
					.orElseThrow(() -> new RuntimeException("Missing OAUTH2_TOKEN_URL"));

			clientAuthenticationMethod = ClientAuthenticationMethod.valueOf(
					Optional.ofNullable(System.getProperty("oauth2_client_authentication_method", System.getenv("OAUTH2_CLIENT_AUTHENTICATION_METHOD")))
							.orElse(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.name()).toUpperCase());

			jwksUrl = System.getProperty("oauth2_jwks_url", System.getenv("OAUTH2_JWKS_URL"));

//	userInfoUrl = Optional.ofNullable(System.getProperty("oauth2_user_info_url", System.getenv("OAUTH2_USER_INFO_URL")))
//			.orElseThrow(() -> new RuntimeException("missing OAUTH2_USER_INFO_URL"));

			userInfoUrl = System.getProperty("oauth2_user_info_url", System.getenv("OAUTH2_USER_INFO_URL"));

//	adminEmail = Optional.ofNullable(System.getProperty("oauth2_admin_email", System.getenv("OAUTH2_ADMIN_EMAIL")))
//			.orElseThrow(() -> new RuntimeException("missing OAUTH2_ADMIN_EMAIL"));

			adminId = System.getProperty("oauth2_admin_id", System.getenv("OAUTH2_ADMIN_ID"));

			adminEmail = System.getProperty("oauth2_admin_email", System.getenv("OAUTH2_ADMIN_EMAIL"));

			adminPassword = System.getProperty("oauth2_admin_password", System.getenv("OAUTH2_ADMIN_PASSWORD"));

			scopes = Optional.ofNullable(System.getProperty("oauth2_scopes", System.getenv("OAUTH2_SCOPES"))).orElse("openid profile");

			userIdClaim = Optional.ofNullable(System.getProperty("oauth2_user_id_claim", System.getenv("OAUTH2_USER_ID_CLAIM"))).orElse("sub");

			userNameClaim = Optional.ofNullable(System.getProperty("oauth2_user_name_claim", System.getenv("OAUTH2_USER_NAME_CLAIM")))
					.orElse("preferred_username");

			userEmailClaim = Optional.ofNullable(System.getProperty("oauth2_user_email", System.getenv("OAUTH2_USER_EMAIL"))).orElse("email");

			final Optional<String> attributes = Optional
					.ofNullable(System.getProperty("oauth2_profile_attributes", System.getenv("OAUTH2_PROFILE_ATTRIBUTES")));
			if (attributes.isPresent()) {
				String[] parts = attributes.get().split(",");
				for (int i = 0; i < parts.length; i++) {
					profileAttributes.add(parts[i]);
				}
			}

			restApiBaseUrl = System.getProperty("oauth2_rest_base_url", System.getenv("OAUTH2_REST_BASE_URL"));

			organizationInfoPath = Optional.ofNullable(System.getProperty("oauth2_organization_info_path", System.getenv("OAUTH2_ORGANIZATION_INFO_PATH")))
					.orElse("projects/");

			rolesPath = Optional.ofNullable(System.getProperty("oauth2_roles_path", System.getenv("OAUTH2_ROLES_PATH"))).orElse("applications/{0}/roles");

			applicationId = System.getProperty("oauth2_application_id", System.getenv("OAUTH2_APPLICATION_ID"));

			tokenPath = Optional.ofNullable(System.getProperty("oauth2_token_path", System.getenv("OAUTH2_TOKEN_PATH"))).orElse("auth/tokens");

			tokenBody = Optional.ofNullable(System.getProperty("oauth2_token_body", System.getenv("OAUTH2_TOKEN_BODY")))
					.orElse("{\"name\": \"{0}\",\"password\": \"{1}\"}");

			jwtTokenIssuer = System.getProperty("oauth2_jwt_token_issuer", System.getenv("OAUTH2_JWT_TOKEN_ISSUER"));

			idTokenJsonRolesPath = System.getProperty("oauth2_id_token_roles_json_path", System.getenv("OAUTH2_ID_TOKEN_ROLES_JSON_PATH"));

			nonProfiledUserAllowed = Boolean.parseBoolean(
					Optional.ofNullable(System.getProperty("allow_non_profiled_users", System.getenv("ALLOW_NON_PROFILED_USERS"))).orElse("true"));

		} else {
			type = FlowType.NONE;
			authorizeUrl = null;
			redirectUrl = null;
			clientId = null;
			clientSecret = null;
			accessTokenUrl = null;
			clientAuthenticationMethod = ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
			jwksUrl = null;
			userInfoUrl = null;
			adminId = null;
			adminEmail = null;
			adminPassword = null;
			scopes = null;
			userIdClaim = null;
			userNameClaim = null;
			userEmailClaim = null;
			restApiBaseUrl = null;
			organizationInfoPath = null;
			rolesPath = null;
			applicationId = null;
			tokenPath = null;
			tokenBody = null;
			jwtTokenIssuer = null;
			idTokenJsonRolesPath = null;
			nonProfiledUserAllowed = true;
		}

		LOGGER.debug("Constructed OAuth2Config: {}", this);
	}

	public static OAuth2Config getInstance() {
		return INSTANCE;
	}

	public FlowType getFlowType() {
		return type;
	}

	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}

	public String getUserInfoUrl() {
		return userInfoUrl;
	}

	public ClientAuthenticationMethod getClientAuthenticationMethod() {
		return clientAuthenticationMethod;
	}

	public String getJWKSUrl() {
		return jwksUrl;
	}

	public String getScopes() {
		return scopes;
	}

	public String getAdminId() {
		return adminId;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public String getUserIdClaim() {
		return userIdClaim;
	}

	public String getUserNameClaim() {
		return userNameClaim;
	}
	
	public String getUserEmailClaim() {
		return userEmailClaim;
	}

	public List<String> getProfileAttributes() {
		return profileAttributes;
	}

	public String getRestAPIBaseUrl() {
		return restApiBaseUrl;
	}

	public String getOrganizationInfoPath() {
		return organizationInfoPath;
	}

	public String getRolesPath() {
		return rolesPath;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public String getTokenPath() {
		return tokenPath;
	}

	public String getTokenBody() {
		return tokenBody;
	}

	public boolean hasUserInfoUrl() {
		return StringUtils.isNotBlank(userInfoUrl);
	}

	public String getJwtTokenIssuer() {
		return jwtTokenIssuer;
	}

	public String getIdTokenJsonRolesPath() {
		return idTokenJsonRolesPath;
	}

	public boolean isNonProfiledUserAllowed() {
		return nonProfiledUserAllowed;
	}

	@Override
	public String toString() {
		return "OAuth2Config [type=" + type + ", authorizeUrl=" + authorizeUrl + ", redirectUrl=" + redirectUrl + ", clientId=" + clientId + ", clientSecret="
				+ clientSecret + ", accessTokenUrl=" + accessTokenUrl + ", clientAuthenticationMethod=" + clientAuthenticationMethod + ", jwksUrl=" + jwksUrl
				+ ", userInfoUrl=" + userInfoUrl + ", adminId=" + adminId + ", adminEmail=" + adminEmail + ", adminPassword=" + adminPassword + ", scopes="
				+ scopes + ", userIdClaim=" + userIdClaim + ", userNameClaim=" + userNameClaim + ", profileAttributes=" + profileAttributes
				+ ", restApiBaseUrl=" + restApiBaseUrl + ", organizationInfoPath=" + organizationInfoPath + ", rolesPath=" + rolesPath + ", applicationId="
				+ applicationId + ", tokenPath=" + tokenPath + ", tokenBody=" + tokenBody + ", jwtTokenIssuer=" + jwtTokenIssuer + ", idTokenJsonRolesPath="
				+ idTokenJsonRolesPath + ", nonProfiledUserAllowed=" + nonProfiledUserAllowed + "]";
	}

	public String getFlowJSPPath() {
		String ret = null;
		switch (type) {
		case AUTHORIZATION_CODE:
			ret = "/oauth2/authorization_code/flow.jsp";
			break;
		case PKCE:
			ret = "/oauth2/pkce/flow.jsp";
			break;
		case OIDC_IMPLICIT:
			ret = "/oauth2/oidc_implicit/flow.jsp";
			break;
		case NONE:
		default:
			ret = null;
		}
		return ret;
	}
}