package it.eng.knowage.boot.filter.bean;

import java.security.Principal;

import it.eng.spagobi.services.security.SpagoBIUserProfile;

public class KnowageUserPrincipal implements Principal {

	private final SpagoBIUserProfile userProfile;

	public KnowageUserPrincipal(SpagoBIUserProfile userProfile) {
		this.userProfile = userProfile;
	}

	@Override
	public String getName() {
		return userProfile.getUniqueIdentifier();
	}

	public SpagoBIUserProfile getProfile() {
		return userProfile;
	}

}
