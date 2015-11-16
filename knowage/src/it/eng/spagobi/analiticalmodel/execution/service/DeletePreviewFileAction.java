/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class DeletePreviewFileAction extends AbstractHttpAction {

	private static transient Logger logger = Logger.getLogger(DeletePreviewFileAction.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.commons.services.BaseProfileAction#service(it.eng.spago
	 * .base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		String documentId = (String) request.getAttribute("DOCUMENT_ID");
		boolean deleted = false;

		EMFErrorHandler errorHandler = getErrorHandler();
		try {

			// Delete the resource
			BIObject biObject = DAOFactory.getBIObjectDAO().loadBIObjectById(new Integer(documentId));
			String previewFileName = biObject.getPreviewFile();
			if (previewFileName != null) {
				deleted = deleteFile(previewFileName);
			}
			logger.debug("file is deleted " + deleted);
			biObject.setPreviewFile(null);
			DAOFactory.getBIObjectDAO().modifyBIObject(biObject);
			logger.debug("association object file deleted");

		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			return;
		}

		logger.debug("OUT");
	}

	private boolean deleteFile(String fileName) throws Exception {
		logger.debug("IN");
		boolean toReturn = false;

		File targetDirectory = GeneralUtilities.getPreviewFilesStorageDirectoryPath();

		logger.debug("Deleting file...");

		File file = new File(targetDirectory, fileName);
		if (file.exists()) {
			logger.debug("file to delete exists...");
			toReturn = file.delete();
			logger.debug("File deleted");
		} else {
			logger.warn("file to delete does not exist...");
		}

		return toReturn;
	}

}
