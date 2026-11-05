package it.eng.spagobi.security.saml;

import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

public interface ProjectProfilingService {
	SpagoBIUserProfile resolve(SamlIdentity identity) throws Exception;
}
