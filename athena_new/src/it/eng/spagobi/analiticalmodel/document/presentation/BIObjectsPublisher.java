/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.presentation;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.presentation.PublisherDispatcherIFace;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
/**
 * Publishes the results of a list information request for parameter use modes
 * into the correct jsp page according to what contained into request. If Any errors occurred during the 
 * execution of the <code>TreeObjectsModule</code> class, the publisher
 * is able to call the error page with the error message caught before and put into 
 * the error handler. If the input information don't fall into any of the cases declared,
 * another error is generated. 
 * 
 * @author sulis
 */
public class BIObjectsPublisher implements PublisherDispatcherIFace {

	/**
	 * Given the request at input, gets the name of the reference publisher,driving
	 * the execution into the correct jsp page, or jsp error page, if any error occurred.
	 * 
	 * @param requestContainer The object containing all request information
	 * @param responseContainer The object containing all response information
	 * 
	 * @return A string representing the name of the correct publisher, which will
	 * call the correct jsp reference.
	 */

	public String getPublisherName(RequestContainer requestContainer, ResponseContainer responseContainer){

		String pubName = ""; 
		SourceBean serviceRequest = requestContainer.getServiceRequest();
		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
		// if there are some errors into the errorHandler  return the name for the errors publisher
		if(!GeneralUtilities.isErrorHandlerContainingOnlyValidationError(errorHandler)) {
			if(!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
				return "error";
			}
		}
		SourceBean moduleResponse = (SourceBean) responseContainer.getServiceResponse().getAttribute("BIObjectsModule");
		// if the module response is null throws an error and return the name of the errors publisher
		if (moduleResponse == null) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, 
					            "BIObjectsPublisher", 
					            "getPublisherName", 
					            "Module response null");
			EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10 );
			errorHandler.addError(error);
			return "error";
		}
		
	    String publisherName = (String) moduleResponse.getAttribute(SpagoBIConstants.PUBLISHER_NAME );
	    if (publisherName != null &&  !publisherName.trim().equals("")) {
	       	return publisherName;
		} 
		
		String objectView = (String) moduleResponse.getAttribute(SpagoBIConstants.OBJECTS_VIEW);
		
		if (SpagoBIConstants.VIEW_OBJECTS_AS_TREE.equalsIgnoreCase(objectView)) {
	        // if passed the error check get the response of the module
			SourceBean treeModuleResponse = (SourceBean)responseContainer.getServiceResponse().getAttribute("TreeObjectsModule");
			// if the module response is null throws an error and return the name of the errors publisher
			if(treeModuleResponse==null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, 
						            "BIObjectsPublisher", 
						            "getPublisherName", 
						            "Tree module response null");
				EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10 );
				errorHandler.addError(error);
				return "error";
			}
			
			publisherName = (String) treeModuleResponse.getAttribute(SpagoBIConstants.PUBLISHER_NAME );
			if (publisherName != null &&  !publisherName.trim().equals("")) {
			   	return publisherName;
			} 
			
			//gets the profile 
			SessionContainer sessionContainer = requestContainer.getSessionContainer();
			SessionContainer permanentSession = sessionContainer.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			
			try{
				if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)){
					String operation = (String)serviceRequest.getAttribute(SpagoBIConstants.OPERATION);
					if( (operation!=null) && (operation.equals(SpagoBIConstants.FUNCTIONALITIES_OPERATION)) ) {
						pubName = "treeFunctionalities";
					} else {
						pubName = "treeAdminObjects";
					}
				}
				else if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV))
					pubName = "treeDevObjects";
				else if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_USER))
					pubName = "treeExecObjects";
				else if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_TEST))
					pubName = "treeExecObjects";
				else pubName = "treeExecObjects";
			}
			catch (Exception e){
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, 
			            "BIObjectsPublisher", 
			            "getPublisherName", 
			            "Error would be defining pubName");
				EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10 );
				errorHandler.addError(error);
				return "error";
			}
		} else {
			pubName = "listBIObjects";
		}

		return pubName;
	}

}