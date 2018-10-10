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
package it.eng.spagobi.dataset.federation;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.dataset.federation.exceptions.FederationDefinitionAccessException;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class FederationDefinitionAccessController {

	static protected Logger logger = Logger.getLogger(FederationDefinitionAccessController.class);
	private final UserProfile userProfile;

	public FederationDefinitionAccessController(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public void delete(Integer federationDefinitionId) {

		logger.debug("IN");
		Integer id = federationDefinitionId;
		ISbiFederationDefinitionDAO sfDAO = null;
		FederationDefinition federationDefinition = null;
		boolean isAdmin = isAdmin();
		boolean isOwner = false;

		try {
			sfDAO = DAOFactory.getFedetatedDatasetDAO();
			logger.debug("federation definition DAO is loaded");
			federationDefinition = sfDAO.loadFederationDefinition(id);
			logger.debug("federation definition with id " + id + " is loaded");
			logger.debug("Current user is admin: " + isAdmin);
			isOwner = isOwner(federationDefinition);
			logger.debug("Current user is loaded federation definition owner : " + isOwner);
			if (isAdmin || isOwner) {
				sfDAO.deleteFederatedDatasetById(id);
				logger.debug("Federation definition is deleted");
			} else {
				logger.error("Error while loading deleting federation definition");
				throw new FederationDefinitionAccessException("Federation definition access violation");
			}

		} catch (EMFUserError e) {
			logger.error("Error while loading deleting federation definition", e);
			throw new SpagoBIRuntimeException("Error while deleting federation definition", e);
		} finally {
			logger.debug("OUT");
		}
	}

	public boolean isOwner(FederationDefinition federationDefinition) {
		String currentUserId = (String) userProfile.getUserId();
		String federationDefinitionOwner = federationDefinition.getOwner();
		logger.debug("Current user id is: " + currentUserId);
		logger.debug("Federation definition owner is: " + federationDefinitionOwner);
		return (currentUserId).equals(federationDefinitionOwner);
	}

	private boolean isAdmin() {
		return UserUtilities.isTechnicalUser(userProfile);
	}

}
