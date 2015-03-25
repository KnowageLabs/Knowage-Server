/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.wapp.presentation;

/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.presentation.PublisherDispatcherIFace;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.wapp.services.DetailMenuModule;
/**
 * Publishes the results of a detail request for a functionality into the correct 
 * jsp page according to what contained into request. If Any errors occurred during the 
 * execution of the <code>DetailFunctionalityModule</code> class, the publisher
 * is able to call the error page with the error message caught before and put into 
 * the error handler. If the input information don't fall into any of the cases declared,
 * another error is generated. 
 * 
 * @author sulis
 */
public class DetailMenuPublisher implements PublisherDispatcherIFace {

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

		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
		
		// get the module response
		SourceBean moduleResponse = (SourceBean)responseContainer.getServiceResponse().getAttribute("DetailMenuModule");

		// if the module response is null throws an error and return the name of the errors publisher
		if(moduleResponse==null) {

			EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10 );
			errorHandler.addError(error);
			return new String("error");
		}
		
		
		// if there are errors and they are only validation errors return the name for the detail publisher
		if(!errorHandler.isOK()) {
			if(GeneralUtilities.isErrorHandlerContainingOnlyValidationError(errorHandler)) {
				return "detailMenu";
			}
		}
		
		
		// if there are some errors into the errorHandler (not validation errors), return the name for the errors publisher
		if(!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
			return new String("error");
		}
		
		Object lookup=moduleResponse.getAttribute(DetailMenuModule.LOOKUP);
		
		Object loop = moduleResponse.getAttribute("loopback");
        
		if(loop != null) {
        	return new String("detailMenuLoopPublisher");
		}
		else if(lookup!=null){
        	return new String("documentLookupLoop");        }
		 else {
			return new String("detailMenuPublisher");
		}
		
	}

}
