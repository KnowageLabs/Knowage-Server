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
package it.eng.spagobi.analiticalmodel.document.handlers;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class OLAPSubobjectExecutionValidationAPI {

	static private Logger logger = Logger.getLogger(OLAPSubobjectExecutionValidationAPI.class);

	private UserProfile profile = null;

	public OLAPSubobjectExecutionValidationAPI(UserProfile profile) {
		this.profile = profile;
	}

	/**
	 * It validates if user can execute a subobject (customized view).
	 *
	 * @throws SecurityException if case user cannot execute input subobject
	 */
	public void checkExecutionPermission(SubObject subobject) throws SecurityException {
		Assert.assertNotNull(subobject, "Input subobject is null");
		BIObject biobj;
		try {
			biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(subobject.getBiobjId());
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Cannot load document information for suboject with id [" + subobject.getId() + "]", e);
		}
		// checking if user can execute main document
		boolean canExec;
		try {
			canExec = ObjectsAccessVerifier.canExec(biobj, profile);
		} catch (EMFInternalError | EMFUserError e) {
			throw new SpagoBIRuntimeException("Cannot load document information for suboject with id [" + subobject.getId() + "]", e);
		}
		if (!canExec) {
			logger.error("Current user cannot execute the suboject with id [" + subobject.getId() + "] because he cannot execute the main document.");
			throw new SecurityException(
					"Current user cannot execute the suboject with id [" + subobject.getId() + "] because he cannot execute the main document.");
		}
		boolean isAdmin = false;
		try {
			isAdmin = profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN);
		} catch (EMFInternalError e) {
			throw new SpagoBIRuntimeException("An error occurred while checking if user is admin", e);
		}
		// subobject is executable if user is admin or subobject is public or user is owner of the subobject
		if (!isAdmin && !subobject.getIsPublic() && !subobject.getOwner().equals(profile.getUserName())) {
			throw new SecurityException("Current user [" + profile + "] cannot see required subobject [" + subobject + "]");
		}
	}

}
