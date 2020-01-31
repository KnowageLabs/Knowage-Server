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
package it.eng.spagobi.commons.bo;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

/**
 * This class represents an user profile within a HTTP session in knowage core (it does not apply, for now, on external engines). It is different from
 * UserProfile because it represents a state that may vary during HTTP session (since the session role can be changed during session), while UserProfile is NOT
 * supposed to change during session. The UserProfile object is cached and it can be reused by backend services, and therefore it may be sent to external
 * engines. The SessionUserProfile cannot be cached because, in case the cache is expired, at the moment there is no possibility to create the user profile
 * object with its state (the current role selected in session), since state is not propagated when invoking backend services.
 *
 */
public class SessionUserProfile extends UserProfile {

	private static final long serialVersionUID = -8365797383215928448L;

	private static transient Logger logger = Logger.getLogger(UserProfile.class);

	// defaultRole is the default role as it is set on metadata database
	private String defaultRole = null;

	// sessionRole is the session role, set by the user using the web GUI change role functionality: when the user profile is created from scratch, the
	// defaultRole is set as sessionRole as well, but the user can change it, therefore defaultRole and sessionRole will differ
	private String sessionRole = null;

	public SessionUserProfile(SpagoBIUserProfile spagoBIUserProfile) {
		super(spagoBIUserProfile);
	}

	/*
	 * if a session role is set, it is returned, otherwise all roles are returned
	 */
	@Override
	public Collection getRolesForUse() throws EMFInternalError {
		logger.debug("IN");
		Collection toReturn = null;
		logger.debug("Looking if session role is selected");
		String sessionRole = this.getSessionRole();
		if (sessionRole != null) {
			logger.debug("Session role selected is " + sessionRole);
			toReturn = new ArrayList<String>();
			toReturn.add(sessionRole);
		} else {
			logger.debug("Session role not selected");
			toReturn = this.getRoles();
		}
		logger.debug("OUT");
		return toReturn;
	}

	public String getDefaultRole() {
		return defaultRole;
	}

	public void setDefaultRole(String defaultRole) {
		logger.debug("IN " + defaultRole);
		this.defaultRole = defaultRole;
		logger.debug("OUT");
	}

	public String getSessionRole() {
		return sessionRole;
	}

	public void setSessionRole(String sessionRole) {
		logger.debug("IN " + sessionRole);
		this.sessionRole = sessionRole;
		logger.debug("OUT");
	}

	@Override
	public String toString() {
		return super.toString() + "; SessionUserProfile [defaultRole=" + defaultRole + ", sessionRole=" + sessionRole + "]";
	}

}
