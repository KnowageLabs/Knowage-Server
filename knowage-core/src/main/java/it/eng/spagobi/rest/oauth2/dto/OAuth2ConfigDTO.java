package it.eng.spagobi.rest.oauth2.dto;

public class OAuth2ConfigDTO {
	
    private String authorizeUrl;
	private String accessTokenUrl;
	private String clientId;
	private String redirectUrl;
	private String scopes;
	public String getAuthorizeUrl() {
		return authorizeUrl;
	}
	public void setAuthorizeUrl(String authorizeUrl) {
		this.authorizeUrl = authorizeUrl;
	}
	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}
	public void setAccessTokenUrl(String accessTokenUrl) {
		this.accessTokenUrl = accessTokenUrl;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getRedirectUrl() {
		return redirectUrl;
	}
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	public String getScopes() {
		return scopes;
	}
	public void setScopes(String scopes) {
		this.scopes = scopes;
	}

	
}
