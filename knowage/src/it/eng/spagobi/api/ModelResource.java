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

package it.eng.spagobi.api;

/**
 * @author MarcoCortella (marco.cortella@eng.it)
 *
 * @class ModelResource
 *
 * Provides services to lock of the model
 *
 */

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.log4j.Logger;
import org.json.JSONObject;

@Path("/1.0/modellocker")
public class ModelResource extends AbstractSpagoBIResource {

	public static transient Logger logger = Logger.getLogger(ModelResource.class);

	/**
	 * Service to lock the meta model
	 *
	 * @return
	 *
	 */
	@POST
	@Path("/{metaModelId}/lock")
	public String lockMetaModel(@PathParam("metaModelId") int metaModelId) {
		logger.debug("IN");

		Object profileO = getAttributeFromHttpSession(IEngUserProfile.ENG_USER_PROFILE);
		if (profileO == null) {
			return ExceptionUtilities.serializeException("Profile not found when executing service", null);

		}
		String userId = ((IEngUserProfile) profileO).getUserUniqueIdentifier().toString();

		logger.debug("User Id is " + userId);
		logger.debug("MetaModel Id is " + metaModelId);

		IMetaModelsDAO modelDAO = DAOFactory.getMetaModelsDAO();

		MetaModel model = modelDAO.loadMetaModelById(metaModelId);

		if (model == null) {
			logger.error("MetaModel referring to id [" + metaModelId + "] could not be loaded");
			return ExceptionUtilities.serializeException("MetaModel with id [" + metaModelId + "] could not be loaded", null);
		}

		logger.debug("MetaMOdel id is " + metaModelId);

		String locker = modelDAO.lockMetaModel(metaModelId, userId);
		String status = null;
		if (locker != null && locker.equals(userId)) {
			logger.debug("MetaModel with artifact " + metaModelId + " was locked by current user " + locker);
			status = SpagoBIConstants.SBI_META_MODEL_VALUE_LOCKED_BY_USER;
		} else if (locker != null) {
			logger.debug("MetaModel with artifact " + metaModelId + " was already locked by user " + locker);
			status = SpagoBIConstants.SBI_META_MODEL_VALUE_LOCKED_BY_OTHER;
		} else {
			logger.debug("MetaModel with artifact " + metaModelId + " was not locked");
			status = SpagoBIConstants.SBI_META_MODEL_VALUE_UNLOCKED;
		}

		logger.debug("Artifact with artifact " + metaModelId + " is in status " + status + " ");
		try {
			JSONObject resultsJSON = new JSONObject();
			resultsJSON.put("status", status);
			if (locker != null)
				resultsJSON.put("locker", locker);
			return resultsJSON.toString();
		} catch (Throwable t) {
			return ExceptionUtilities.serializeException("An unexpected error occured while executing service", null);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Service to unlock the meta model
	 *
	 * @return
	 *
	 */
	@POST
	@Path("/{metaModelId}/unlock")
	public String unlockMetaModel(@PathParam("metaModelId") int metaModelId) {
		logger.debug("IN");

		Object profileO = getAttributeFromHttpSession(IEngUserProfile.ENG_USER_PROFILE);
		if (profileO == null) {
			return ExceptionUtilities.serializeException("Profile not found when executing service", null);

		}
		String userId = ((IEngUserProfile) profileO).getUserUniqueIdentifier().toString();

		logger.debug("User Id is " + userId);
		logger.debug("MetaModel Id is " + metaModelId);

		IMetaModelsDAO modelDAO = DAOFactory.getMetaModelsDAO();

		MetaModel model = modelDAO.loadMetaModelById(metaModelId);

		if (model == null) {
			logger.error("MetaModel referring to id [" + metaModelId + "] could not be loaded");
			return ExceptionUtilities.serializeException("MetaModel with id [" + metaModelId + "] could not be loaded", null);
		}

		logger.debug("MetaModel id is " + metaModelId);

		String locker = modelDAO.unlockMetaModel(metaModelId, userId);
		String status = null;

		if (locker == null) {
			logger.debug("MetaModel was unlocked");
			status = SpagoBIConstants.SBI_META_MODEL_VALUE_UNLOCKED;
		} else {
			logger.warn("MetaModel was not unlocked and is hold by locker " + locker);
			if (locker != null && userId.equals(locker)) {
				status = SpagoBIConstants.SBI_META_MODEL_VALUE_LOCKED_BY_USER;
			} else {
				status = SpagoBIConstants.SBI_META_MODEL_VALUE_LOCKED_BY_OTHER;

			}
		}

		logger.debug("MetaModel with id " + metaModelId + " is in status " + status + " ");

		try {
			JSONObject resultsJSON = new JSONObject();
			resultsJSON.put("status", status);
			if (locker != null)
				resultsJSON.put("locker", locker);
			return resultsJSON.toString();
		} catch (Throwable t) {
			return ExceptionUtilities.serializeException("An unexpected error occured while executing service", null);
		} finally {
			logger.debug("OUT");
		}

	}

}
