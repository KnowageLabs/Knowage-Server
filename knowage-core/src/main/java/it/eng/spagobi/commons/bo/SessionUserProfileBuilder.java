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

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SessionUserProfileBuilder {

	static Logger logger = Logger.getLogger(SessionUserProfileBuilder.class);

	public static String getDefaultRole(UserProfile userProfile) {
		logger.debug("IN");
		String defaultRole = null;
		try {
			SbiUser sbiUserDB = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userProfile.getUserId().toString());
			/* If you use an external profiling service, sbiUserDB will be null. */
			if (sbiUserDB != null) {
				Integer defaultRoleId = sbiUserDB.getDefaultRoleId();
				logger.debug("defaultRoleId: " + defaultRoleId == null ? "null" : defaultRoleId);
				if (defaultRoleId != null) {
					defaultRole = DAOFactory.getRoleDAO().loadByID(defaultRoleId).getName();
					logger.debug("Found defaultRole: " + defaultRole);
				}
			}
		} catch (EMFUserError error) {
			throw new SpagoBIRuntimeException("Error while getting default role", error);
		}
		logger.debug("OUT: returning " + defaultRole);
		return defaultRole;
	}

	public static SessionUserProfile getDefaultUserProfile(UserProfile completeProfile, String defaultRole) {
		SessionUserProfile toReturn;
		logger.debug("User [" + completeProfile.getUserId() + "] has default role [" + defaultRole + "].");
		SpagoBIUserProfile clone = UserUtilities.clone(completeProfile.getSpagoBIUserProfile());
		clone.setRoles(new String[] { defaultRole });
		// recalculating available functionalities for the user considering only the default role
		clone.setFunctions(UserUtilities.readFunctionality(clone));
		logger.debug("Re-creating user profile object for user  [" + completeProfile.getUserId() + "] considering its default role [" + defaultRole + "]....");
		toReturn = new SessionUserProfile(clone);
		// restoring initial roles, otherwise the user will not be able to switch between them
		try {
			toReturn.setRoles(completeProfile.getRoles());
		} catch (EMFInternalError e) {
			throw new SpagoBIRuntimeException("Error while getting user roles", e);
		}
		// default role is NOT supposed to change
		toReturn.setDefaultRole(defaultRole);
		// setting session role as initial state, it may change
		toReturn.setSessionRole(defaultRole);
		return toReturn;
	}

	public static SessionUserProfile getDefaultUserProfile(UserProfile completeProfile) {
		logger.debug("IN");
		SessionUserProfile userProfile = null;

		String defaultRole = getDefaultRole(completeProfile);
		if (defaultRole != null) {
			logger.debug("Detected default role for user [" + completeProfile.getUserId() + "], re-creating user profile considering only that role");
			// we get another UserProfile instance detach it from cache, otherwise we can have errors in case cache is expired (see KNOWAGE-4795)
			userProfile = getDefaultUserProfile(completeProfile, defaultRole);
		} else {
			logger.debug("No default role set for user [" + completeProfile.getUserId() + "]");
			// we clone the object to detach it from cache, otherwise we can have errors in case cache is expired (see KNOWAGE-4795)
			SpagoBIUserProfile clone = UserUtilities.clone(completeProfile.getSpagoBIUserProfile());
			userProfile = new SessionUserProfile(clone);
		}
		logger.debug("OUT");
		return userProfile;
	}

}
