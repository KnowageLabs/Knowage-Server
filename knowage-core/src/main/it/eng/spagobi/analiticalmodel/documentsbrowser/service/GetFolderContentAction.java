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

import it.eng.spago.base.SessionContainer;
import it.eng.spagobi.analiticalmodel.documentsbrowser.utils.FolderContentUtil;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONObject;


/**
 *
 */
public class GetFolderContentAction extends AbstractSpagoBIAction {

	// REQUEST PARAMETERS
	public static final String FOLDER_ID = "folderId";

	// logger component
	private static Logger logger = Logger.getLogger(GetFolderContentAction.class);

	public void doService() {

		logger.debug("IN");

		try {

			String folderIdStr = getAttributeAsString(FOLDER_ID);
			logger.debug("Parameter [" + FOLDER_ID + "] is equal to [" + folderIdStr + "]");
			
			FolderContentUtil fcUtil = new FolderContentUtil();
			LowFunctionality folder = null;
			if (folderIdStr != null && !folderIdStr.equalsIgnoreCase(FolderContentUtil.ROOT_NODE_ID)) {
				int folderId = new Integer(folderIdStr);
				logger.debug("Folder id is " + folderId);
				folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(folderId, false);
				logger.debug("Folder is " + folder);
				boolean canSee = fcUtil.checkRequiredFolder(folder, this.getUserProfile());
				if (!canSee) {
					logger.error("Required folder does not exist or you don't have priviledges to see it");
					throw new SpagoBIServiceException(SERVICE_NAME, "Required folder does not exist or you don't have priviledges to see it");
				}
			}
			
			HttpServletRequest httpRequest = getHttpRequest();
			SessionContainer sessCont = getSessionContainer();

			JSONObject folderContent = fcUtil.getFolderContent(folder, this.getServiceRequest(), this.getServiceResponse(), httpRequest, sessCont);
			try {
				writeBackToClient(new JSONSuccess(folderContent));
			} catch (IOException e) {
				throw new SpagoBIException("Impossible to write back the responce to the client", e);
			}

		} catch (SpagoBIServiceException e) {
			throw e;
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get folder content", t);
		} finally {
			logger.debug("OUT");
		}
	}

}
