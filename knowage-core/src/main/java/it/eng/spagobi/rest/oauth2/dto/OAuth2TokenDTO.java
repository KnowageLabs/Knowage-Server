package it.eng.spagobi.rest.oauth2.dto;

public class OAuth2TokenDTO {

	
	private String idToken;
	private String accessToken;
	public String getIdToken() {
		return idToken;
	}
	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	
	
}
