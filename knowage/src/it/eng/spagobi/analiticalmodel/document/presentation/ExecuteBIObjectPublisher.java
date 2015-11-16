/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.presentation;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.presentation.PublisherDispatcherIFace;
import it.eng.spagobi.commons.constants.SpagoBIConstants;

import org.apache.log4j.Logger;

/**
 * Publishes the results of a detail request for executing a BI object into the
 * correct jsp page according to what contained into request. If Any errors
 * occurred during the execution of the <code>ExecuteBIObjectModule</code>
 * class, the publisher is able to call the error page with the error message
 * caught before and put into the error handler. If the input information don't
 * fall into any of the cases declared, another error is generated.
 * 
 * @author sulis
 */
public class ExecuteBIObjectPublisher implements PublisherDispatcherIFace {
	
	static private Logger logger = Logger.getLogger(ExecuteBIObjectPublisher.class);
	
	/**
	 * Given the request at input, gets the name of the reference
	 * publisher,driving the execution into the correct jsp page, or jsp error
	 * page, if any error occurred.
	 * 
	 * @param requestContainer The object containing all request information
	 * @param responseContainer The object containing all response information
	 * 
	 * @return A string representing the name of the correct publisher, which
	 * will call the correct jsp reference.
	 */

	public String getPublisherName(RequestContainer requestContainer,
			ResponseContainer responseContainer) {
		logger.debug("IN");
		try {
			EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
	
			// get the module response
			SourceBean executeModuleResponse = (SourceBean) responseContainer
					.getServiceResponse().getAttribute("ExecuteBIObjectModule");
			/*SourceBean checklistLookupModule = (SourceBean) responseContainer.getServiceResponse().getAttribute(
					"ChecklistLookupModalityValuesModule");*/
			
			// if the module response is null throws an error and return the name of
			// the errors publisher
			//if (executeModuleResponse == null && checklistLookupModule == null) {
			if (executeModuleResponse == null ) {
				logger.error("Module response null");
				EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10);
				errorHandler.addError(error);
				return new String("error");
			}
			
			// if there are some errors into the errorHandler return the name for
			// the errors publisher
			if (!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
				if (executeModuleResponse != null) {
					Object publisherNameSetObj = executeModuleResponse
							.getAttribute(SpagoBIConstants.PUBLISHER_NAME);
					if (publisherNameSetObj != null) {
						String publisherName = (String) publisherNameSetObj;
						return publisherName;
					} else
						return "error";
				/*} else if (checklistLookupModule != null) {
					Object publisherNameSetObj = checklistLookupModule
							.getAttribute(SpagoBIConstants.PUBLISHER_NAME);
					if (publisherNameSetObj != null) {
						String publisherName = (String) publisherNameSetObj;
						return publisherName;
					} else
						return "error";	*/
				} else
					return "error";
			}
	
			boolean isLoop = false;
			if (executeModuleResponse != null && executeModuleResponse.getAttribute("isLoop") != null) {
				try {
					executeModuleResponse.delAttribute("isLoop");
				} catch (Exception e) {
					e.printStackTrace();
				}
				isLoop = true;
			}
	
			boolean publisherNameSet = false;
			Object publisherNameSetObj = executeModuleResponse == null ? null: executeModuleResponse
					.getAttribute(SpagoBIConstants.PUBLISHER_NAME);
			/*if (publisherNameSetObj == null) {
				publisherNameSetObj = checklistLookupModule == null ? null: checklistLookupModule
						.getAttribute(SpagoBIConstants.PUBLISHER_NAME);
			}*/
			if (publisherNameSetObj != null) {
				publisherNameSet = true;
			}
	
			if (publisherNameSet) {
				String publisherName = (String) publisherNameSetObj;
				logger.debug("Publisher name set: [" + publisherName + "]");
				return publisherName;
			}
	
			if (isLoop && executeModuleResponse != null) {
				return new String("LoopTree");
			}
			
			logger.debug("Publisher name not set");
			return "error";	
		
		} finally {
			logger.debug("OUT");
		}

	}

}
