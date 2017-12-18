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
package it.eng.spagobi.analiticalmodel.execution.service;


import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.BIObjectDAOHibImpl;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.LowFunctionalityDAOHibImpl;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


public class SaveToPersonalFolderAction extends AbstractHttpAction {

	private static final long serialVersionUID = 1L;
	
	private static transient Logger logger=Logger.getLogger(SaveToPersonalFolderAction.class);

	/* (non-Javadoc)
	 * @see it.eng.spagobi.commons.services.BaseProfileAction#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean responseSb) throws Exception {
		logger.debug("IN");

		final String OK = "sbi.execution.stpf.ok";
		final String ERROR = "sbi.execution.stpf.error";
		final String ALREADYPRESENT = "sbi.execution.stpf.alreadyPresent";

		String retCode = "";

		HttpServletResponse response = getHttpResponse();

		try {
			SessionContainer permSession = this.getRequestContainer().getSessionContainer().getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile) permSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			if (profile == null) {
				logger.error("User profile not found");
				throw new Exception("User profile not found");
			}
			String userId = ((UserProfile)profile).getUserId().toString();
			logger.debug("*** savePersonal - userId: " + userId);
			String documentIdStr = (String) request.getAttribute("documentId");
			if (documentIdStr == null || documentIdStr.trim().equals("")){
				logger.error("Document id not specified!!");
				throw new Exception("Document id not specified!!");
			}
			
			logger.debug("Access to the database");
			LowFunctionalityDAOHibImpl lowFunctionalityDAOHibImpl = new LowFunctionalityDAOHibImpl();
			boolean exists = lowFunctionalityDAOHibImpl.checkUserRootExists(userId);
			LowFunctionality lf = null;
			if (exists) {
				logger.debug("Personal Folder found");	
			}
			else {
				logger.debug("Personal Folder not found, now will be created");	
				UserUtilities.createUserFunctionalityRoot(profile);
				logger.debug("Personal Folder created");	
				if(!(lowFunctionalityDAOHibImpl.checkUserRootExists(userId)))
				throw new Exception("Personal Folder doesn't exists or could not be created");

			}
			lf = lowFunctionalityDAOHibImpl.loadLowFunctionalityByPath("/" + userId, false);
			
			Integer idFunction = lf.getId();
			if (idFunction == null) {
				logger.error("No function associated");
				throw new Exception("No function associated");
			}

			// Load document
			IBIObjectDAO biObjectDAOHibImpl = DAOFactory.getBIObjectDAO();
			biObjectDAOHibImpl.setUserProfile(profile);
			BIObject biObject = biObjectDAOHibImpl.loadBIObjectById(Integer.valueOf(documentIdStr));
			if (biObject == null) {
				logger.error("Could not load document");
				throw new Exception("Could not load document");
			}

			List funcs = biObject.getFunctionalities();
			if (funcs == null) {
				logger.error("BIObject with label " + biObject.getLabel() + " has no functionalities associated!!!");
				throw new Exception("BIObject with label " + biObject.getLabel() + " has no functionalities associated!!!");
			}
			if (!funcs.contains(idFunction)){
				funcs.add(idFunction);
				biObject.setFunctionalities(funcs);
				biObjectDAOHibImpl.modifyBIObject(biObject);
				logger.debug("Object modified");
				retCode = OK;
			}
			else {
				logger.warn("the object is already associated to the functionality");
				retCode = ALREADYPRESENT;
			}
		} catch (Exception e) {
			logger.error("Error while modifying object");
			if (retCode.equals("")) {
				retCode = ERROR;
			}
		} finally {			
			try {
				response.getOutputStream().write(retCode.getBytes());
				response.getOutputStream().flush();	
			} catch (Exception ex) {
				logger.error("Error while sending response to client");
			}
			logger.debug("OUT");
		}		
	}


}	


