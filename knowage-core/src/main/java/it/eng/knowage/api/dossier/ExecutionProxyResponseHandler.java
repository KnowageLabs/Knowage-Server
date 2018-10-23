/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.knowage.api.dossier;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.knowage.api.dossier.utils.FileUtilities;
import it.eng.knowage.engines.dossier.template.placeholder.PlaceHolder;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.exceptions.SpagoBIEmptyFileExeception;
import it.eng.spagobi.commons.exceptions.SpagoBIResponseHasErrorsExeception;


public class ExecutionProxyResponseHandler {
	
	private static transient Logger logger = Logger.getLogger(ExecutionProxyResponseHandler.class);
	
	public  void handleByteResponse(byte[] response,BIObject biObject,String randomKey,List<PlaceHolder> placeHolders) {
		

		if(isResponseEmpty(response)){
			handleEmptyFile(biObject,randomKey,placeHolders);
			throw new SpagoBIEmptyFileExeception("Response file is empty"); 
		}else if (hasErrors(response)){
			handleOnError(biObject,randomKey,placeHolders);
			throw new SpagoBIResponseHasErrorsExeception("respopnse has errors");
		}else{
			handleOnSuccess(response,biObject,randomKey,placeHolders);
			
		}

	}
	
	private void handleOnSuccess(byte[] response,BIObject biObject,String randomKey,List<PlaceHolder> placeHolders) {
		logger.debug("Export ok for biObj with label " + biObject.getLabel());
		String cleanLabel = FileUtilities.cleanFileName(biObject.getLabel());
		String cleanName = FileUtilities.cleanFileName(biObject.getName());
		String fileName = cleanLabel + "-" + cleanName;
		
		File file = FileUtilities.createFile(fileName, ".pdf", randomKey, placeHolders);
		
		FileUtilities.writeFile(file, response);
		
	}

	private void handleOnError(BIObject biObject,String randomKey,List<PlaceHolder> placeHolders) {
		logger.error("Error found in execution, make txt file");
		String cleanLabel = FileUtilities.cleanFileName(biObject.getLabel());
		String cleanName = FileUtilities.cleanFileName(biObject.getName());
		String fileName = "Error " + cleanLabel + "-" + cleanName;
		
		FileUtilities.createFile(fileName, ".txt", randomKey, placeHolders);
		
	}

	private void handleEmptyFile(BIObject biObject,String randomKey,List<PlaceHolder> placeHolders) {
		FileUtilities.createErrorFile(biObject, null, placeHolders,randomKey);
		
	}
	
	private  boolean isResponseEmpty(byte[] response){
		
		return response.length==0||response == null;
	}
	
	private boolean hasErrors(byte[] response){
		String checkerror = new String(response); 
		return checkerror.startsWith("error") || checkerror.startsWith("{\"errors\":");
	}

}
