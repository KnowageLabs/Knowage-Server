/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
