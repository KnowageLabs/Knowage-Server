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
package it.eng.spagobi.analiticalmodel.documentsbrowser.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Bernabei Angelo (angelo.bernabei@eng.it)
 */
public class DeleteObjectAction extends AbstractSpagoBIAction {

	public static final String SERVICE_NAME = "DELETE_OBJECT_ACTION";
	public static final String OBJECT_ID = "docId";
	public static final String FUNCT_ID = "folderId";
	public static final String FROM_MY_ANALYSIS = "fromMyAnalysis";
	public static final String DELETE_ONLY_FROM_PERSONAL_FOLDER = "deleteOnlyFromPersonalFolder";


	// logger component
	private static Logger logger = Logger.getLogger(DeleteObjectAction.class);

	public void doService() {
		logger.debug("IN");

		try {
			// BIObject obj = executionInstance.getBIObject();
			UserProfile userProfile = (UserProfile) this.getUserProfile();
			IBIObjectDAO dao = null;
			ILowFunctionalityDAO functDAO = null;
			try {
				dao = DAOFactory.getBIObjectDAO();
				functDAO = DAOFactory.getLowFunctionalityDAO();
			} catch (EMFUserError e) {
				logger.error("Error while istantiating DAO", e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot access database", e);
			}
			String ids = this.getAttributeAsString(OBJECT_ID);
			
			Object folder = this.getAttribute(FUNCT_ID);
			
			Boolean isFromMyAnalysis = this.getAttributeAsBoolean(FROM_MY_ANALYSIS);
			
			Boolean deleteOnlyFromPersonalFolder = this.getAttributeAsBoolean(DELETE_ONLY_FROM_PERSONAL_FOLDER);

			
			Integer folderId = null;
			if (folder != null){
				if (folder instanceof Integer){
					folderId = this.getAttributeAsInteger(FUNCT_ID);
					logger.debug("Input Folder:" + folderId);
				} else if (folder instanceof String){
					//TODO: to fix
				}
			}

			logger.debug("Input Object:" + ids);
			String userId = ((UserProfile)userProfile).getUserId().toString();
			logger.debug("User id:" + userId);
			
			// ids contains the id of the object to be deleted separated by ,
			String[] idArray = ids.split(",");
			for (int i = 0; i < idArray.length; i++) {
				Integer id = new Integer(idArray[i]);
				BIObject biObject = dao.loadBIObjectById(id);;
				Assert.assertNotNull(biObject, "Document with id [" + id + "] not found");
				LowFunctionality lowFunctionality = null;
				if (folderId == null){
					lowFunctionality = functDAO.loadRootLowFunctionality(false); //TODO: to fix
				} else {
					lowFunctionality = functDAO.loadLowFunctionalityByID(folderId, false);
				}
				
				if ((isFromMyAnalysis == true) && (deleteOnlyFromPersonalFolder == true)){
					//for deleting only inside user personal folder
					lowFunctionality = functDAO.loadLowFunctionalityByPath("/"+userProfile.getUserUniqueIdentifier(),false);
					folderId = lowFunctionality.getId();
				}
				
				Assert.assertNotNull(lowFunctionality, "Folder with id [" + folderId + "] not found");
				
				if (ObjectsAccessVerifier.canDeleteBIObject(id, userProfile, lowFunctionality)) {
					// delete document
					try {
						dao.eraseBIObject(biObject, folderId);
						logger.debug("Object deleted succesfully");
					} catch (EMFUserError e) {
						logger.error("Error deleting document", e);
						throw new SpagoBIServiceException(SERVICE_NAME, "Error deleting document", e);
					}
				} else {
					logger.error("User [" + userId + "] cannot delete document with label [" + biObject.getLabel() + "]");
					throw new SpagoBIServiceException(SERVICE_NAME, "User [" + userId + "] cannot delete document with label [" + biObject.getLabel() + "]");
				}

			}
			try {
				JSONObject results = new JSONObject();
				results.put("result", "OK");
				writeBackToClient(new JSONSuccess(results));
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			}
		} catch (SpagoBIServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An internal error has occured", e);
		} finally {
			logger.debug("OUT");
		}
	}

}
