/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.documentsbrowser.service;

import it.eng.spago.base.SessionContainer;
import it.eng.spagobi.analiticalmodel.documentsbrowser.utils.FolderContentUtil;
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
			if (folderIdStr != null && !folderIdStr.equalsIgnoreCase(FolderContentUtil.ROOT_NODE_ID)) {
				boolean canSee = fcUtil.checkRequiredFolder(folderIdStr, this.getUserProfile());
				if (!canSee) {
					logger.error("Required folder does not exist or you don't have priviledges to see it");
					throw new SpagoBIServiceException(SERVICE_NAME, "Required folder does not exist or you don't have priviledges to see it");
				}
			}
			
			HttpServletRequest httpRequest = getHttpRequest();
			SessionContainer sessCont = getSessionContainer();
			
			JSONObject folderContent = fcUtil.getFolderContent(folderIdStr, this.getServiceRequest(), this.getServiceResponse(), httpRequest, sessCont);
			try {
				writeBackToClient( new JSONSuccess( folderContent ) );
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
