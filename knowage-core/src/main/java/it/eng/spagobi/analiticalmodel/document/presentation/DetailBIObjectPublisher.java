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
package it.eng.spagobi.analiticalmodel.document.presentation;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.presentation.PublisherDispatcherIFace;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
/**
 * Publishes the results of a detail request for a BI object into the correct 
 * jsp page according to what contained into request. If Any errors occurred during the 
 * execution of the <code>DetailBIObjectsModule</code> class, the publisher
 * is able to call the error page with the error message caught before and put into 
 * the error handler. If the input information don't fall into any of the cases declared,
 * another error is generated. 
 * 
 * @author sulis
 */
public class DetailBIObjectPublisher implements PublisherDispatcherIFace {

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
	public String getPublisherName(RequestContainer requestContainer, ResponseContainer responseContainer) {

		String publisher = "";
		
		SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, 
	            "DetailBIObjectPublisher", 
	            "getPublisherName", 
	            "[BEGIN]");
		
		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
		
		// get the module response
		SourceBean moduleResponse = (SourceBean)responseContainer.getServiceResponse().getAttribute("DetailBIObjectModule");
		
		// if the module response is null throws an error and return the name of the errors publisher
		if(moduleResponse==null) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, 
		            "DetailBIObjectPublisher", 
		            "getPublisherName", 
		            "Module response null");
			EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10 );
			errorHandler.addError(error);
			//publisher = new String("error");
			return new String("error");
		}
		
		// if there are errors and they are only validation errors return the name for the detail publisher
		if(!errorHandler.isOK()) {
			if(GeneralUtilities.isErrorHandlerContainingOnlyValidationError(errorHandler)) {
				return "detailBIObject";
			}
		}		
		
		// if there are some errors into the errorHandler (not validation errors), return the name for the errors publisher
		if(!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
			return new String("error");
		}
				
        Object loop = moduleResponse.getAttribute("loopback");
        Object parametersLookupLoop = moduleResponse.getAttribute("parametersLookup");
        Object linksLookupLoop = moduleResponse.getAttribute("linksLookup");
        Object dependenciesLookup = moduleResponse.getAttribute("dependenciesLookup");
        Object saveLoop = moduleResponse.getAttribute("saveLoop");
        
               
        if(loop != null) {
        	publisher = new String("detailBIObjectLoop");
		} else if (parametersLookupLoop != null){
			publisher = new String("parametersLookupLoop");
		} else if (linksLookupLoop != null){
			publisher = new String("linksLookupLoop");
		} else if (dependenciesLookup != null){
			publisher = new String("dependenciesLookup");
		} else if (saveLoop != null){
			publisher = new String("detailBIObjectSaveLoop");
		} else {
			publisher = new String("detailBIObject");
		}
        
        SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, 
	            "DetailBIObjectPublisher", 
	            "getPublisherName", 
	            "redirect to publisher: " + publisher);
        
		return publisher;
	}

}
