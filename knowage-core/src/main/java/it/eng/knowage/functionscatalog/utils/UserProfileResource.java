package it.eng.knowage.functionscatalog.utils;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;

public class UserProfileResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(UserProfileResource.class);

	protected UserProfileResource() {
		super();
	}

	protected UserProfile getUserProfileForFunctionsCatalog() {
		return getUserProfile();
	}
}
