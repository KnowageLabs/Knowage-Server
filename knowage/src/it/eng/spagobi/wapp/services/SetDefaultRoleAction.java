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
package it.eng.spagobi.wapp.services;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

public class SetDefaultRoleAction extends AbstractSpagoBIAction {

	static private Logger logger = Logger.getLogger(SetDefaultRoleAction.class);

	UserProfile userProfile = null;

	public static final String SERVICE_NAME = "SET_DEFAULT_ROLE_ACTION";
	// REQUEST PARAMETERS
	public static final String SELECTED_ROLE = "SELECTED_ROLE";

	/**
	 * Returns Default role if present
	 */

	@Override
	public void doService() {
		logger.debug("IN on service");
		try {

			IEngUserProfile profile = this.getUserProfile();

			String defaultRole = this.getAttributeAsString(SELECTED_ROLE);
			logger.debug("Selected role " + defaultRole);

			// check if selected role is part of the user ones
			ArrayList<String> roles = (ArrayList<String>) profile.getRoles();

			for (int i = 0; i < roles.size(); i++) {
				logger.debug("user roles " + roles.get(i));
			}

			if (defaultRole.equals("")) {
				defaultRole = null;
			}

			if (defaultRole != null && !roles.contains(defaultRole)) {
				logger.error("Security alert. Role not among the user ones");
				throw new SpagoBIServiceException(SERVICE_NAME, "Role selected is not permitted for user " + userProfile.getUserId());
			}

			// set this role as default one, or clear default role if not present
			String previousDefault = ((UserProfile) profile).getDefaultRole();
			logger.debug("previous default role " + previousDefault);
			logger.debug("new default role " + defaultRole);
			((UserProfile) profile).setDefaultRole(defaultRole);
			logger.debug("default role set! ");

			// now I must refresh userProfile functionalities

			// if new defaultRole is null refresh all the functionalities!
			if (defaultRole == null) {
				logger.debug("Selected role is null, refresh all functionalities");
				IEngUserProfile newProfile = UserUtilities.getUserProfile(profile.getUserUniqueIdentifier().toString());
				Collection functionalities = newProfile.getFunctionalities();
				LogMF.debug(logger, "User functionalities: {0}", new String[] { functionalities.toString() });
				((UserProfile) profile).setFunctionalities(functionalities);
			} else {
				// there is a default role selected so filter only its functionalities
				logger.debug("Selected role is not null, put right functionality");
				Collection functionalities = this.getFunctionalitiesForDefaultRole(profile, defaultRole);
				LogMF.error(logger, "User functionalities considering default role [{0}]: {1}", new String[] { defaultRole, functionalities.toString() });
				((UserProfile) profile).setFunctionalities(functionalities);
				logger.debug("set functionalities for default role");
			}
			logger.debug("Filtered functionalities for selected role " + defaultRole);

			try {
				writeBackToClient(new JSONAcknowledge());
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Exception occurred while retrieving metadata", e);
		} finally {
			logger.debug("OUT");
		}

	}

	private Collection getFunctionalitiesForDefaultRole(IEngUserProfile engUserProfile, String defaultRole) {
		logger.debug("IN: defaultRole is [" + defaultRole + "]");
		Collection toReturn = null;
		try {
			String[] roles = new String[] { defaultRole };
			SpagoBIUserProfile profile = ((UserProfile) engUserProfile).getSpagoBIUserProfile();
			SpagoBIUserProfile clone = UserUtilities.clone(profile);
			// we limit the roles to the clone object and recalculate functionalities
			clone.setRoles(roles);
			String[] functionalitiesArray = UserUtilities.readFunctionality(clone);
			toReturn = StringUtilities.convertArrayInCollection(functionalitiesArray);
		} finally {
			LogMF.error(logger, "Returning: {0}", new String[] { toReturn.toString() });
		}
		return toReturn;
	}
}
