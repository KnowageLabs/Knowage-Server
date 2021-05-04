package it.eng.knowage.knowageapi.utils;

public class ConfigSingleton {

	private static final String KNOWAGE_AUTHORIZATION_HEADER_NAME = "KNOWAGE_AUTHORIZATION_HEADER_NAME";

	private static final ConfigSingleton INSTANCE = new ConfigSingleton();

	private String authorizationHeaderName;

	private ConfigSingleton() {

	}

	public String getAuthorizationHeaderName() {
		if (authorizationHeaderName == null) {
			if (System.getenv().containsKey(KNOWAGE_AUTHORIZATION_HEADER_NAME)) {
				authorizationHeaderName = System.getenv(KNOWAGE_AUTHORIZATION_HEADER_NAME);
			} else {
				authorizationHeaderName = "X-Kn-Authorization";
			}
		}
		return authorizationHeaderName;
	}

	public static ConfigSingleton getInstance() {
		return INSTANCE;
	}
}
