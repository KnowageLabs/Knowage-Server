/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.massiveExport.services;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.tools.massiveExport.bo.ProgressThread;
import it.eng.spagobi.tools.massiveExport.dao.IProgressThreadDAO;
import it.eng.spagobi.tools.massiveExport.utils.Utilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class DeleteMassiveExportZip extends AbstractSpagoBIAction {

	private final String SERVICE_NAME = "DOWNLOAD_MASSIVE_EXPORT_ZIP";

	// logger component
	private static Logger logger = Logger.getLogger(DeleteMassiveExportZip.class);


	public static final String  RANDOM_KEY = "RANDOM_KEY";
	public static final String  PROGRESS_THREAD_ID = "PROGRESS_THREAD_ID";
	public static final String  FUNCT_CD = "FUNCT_CD";
	public static final String  PROGRESS_THREAD_TYPE = "PROGRESS_THREAD_TYPE";

	@Override
	public void doService() {
		logger.debug("IN");

		IEngUserProfile profile = getUserProfile();

		String zipFileName = null;
		String folderLabel = null;
		Integer progressThreadId = null;
		String progressThreadType = null;
		File zipFile = null;
		
		
		try{

			zipFileName = getAttributeAsString( RANDOM_KEY);
			folderLabel = getAttributeAsString( FUNCT_CD);
			progressThreadId = getAttributeAsInteger( PROGRESS_THREAD_ID);
			progressThreadType = getAttributeAsString( PROGRESS_THREAD_TYPE );
			
			logger.debug(RANDOM_KEY+": "+zipFileName);
			logger.debug(FUNCT_CD+": "+folderLabel);
			logger.debug(PROGRESS_THREAD_ID+": "+progressThreadId);
			logger.debug(PROGRESS_THREAD_TYPE + ": " + progressThreadType);


			// delete the record
			IProgressThreadDAO progressThreadDAO = DAOFactory.getProgressThreadDAO();
			boolean progressThreadDeleted = progressThreadDAO.deleteProgressThread(progressThreadId);
			if(!progressThreadDeleted){
				logger.warn("progress thread with id "+progressThreadId+" was not deleted, probably due to asynchron call");
				return;
			}
			logger.debug("progress thread with id "+progressThreadId+" has been deleted");

			logger.debug("Delete zipFile RandomKey = "+zipFileName+" FunctCd = "+folderLabel+ " ProgressThreadId = "+progressThreadId);

			if(ProgressThread.TYPE_MASSIVE_EXPORT.equalsIgnoreCase(progressThreadType)) {
				zipFile = Utilities.getMassiveExportZipFile(folderLabel, zipFileName);
			} else if(ProgressThread.TYPE_MASSIVE_SCHEDULE.equalsIgnoreCase(progressThreadType)) {
				zipFile = Utilities.getMassiveScheduleZipFile((String)getUserProfile().getUserUniqueIdentifier(), folderLabel, zipFileName);				
			}
		
			if(zipFile.exists()) {
				boolean zipFileDeleted = zipFile.delete();
				if(zipFileDeleted){
					logger.debug("File [" + zipFile + "] has been deleted");
				} else{
					logger.debug("could not delete file [" + zipFile + "]");
				} 
			} else {
				logger.warn("File [" + zipFile + "] does not exist");
			}
			
			//delte folder directory if no more used
			Utilities.deleteMassiveExportFolderIfEmpty(folderLabel);

			writeBackToClient(new JSONSuccess(new JSONObject()));

		}
		catch (Exception err) {
			logger.error("Error in retrieving file", err);
			throw new SpagoBIServiceException("Error during delete: cannot retrieve zip file ["+ zipFile + "]", err);
		}
	}




}
