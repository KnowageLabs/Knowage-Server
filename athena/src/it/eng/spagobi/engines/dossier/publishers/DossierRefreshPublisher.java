/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.publishers;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.presentation.PublisherDispatcherIFace;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.engines.dossier.constants.DossierConstants;

/**
 * @author Andrea Gioia
 *
 */
public class DossierRefreshPublisher implements PublisherDispatcherIFace {
	
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
		SourceBean serviceResp = responseContainer.getServiceResponse();
		SourceBean moduleResponse = (SourceBean)serviceResp.getAttribute(DossierConstants.DOSSIER_REFRESH_MODULE);
		if(moduleResponse==null) {
			SpagoBITracer.major(DossierConstants.NAME_MODULE, this.getClass().getName(), 
					            "getPublisherName", "Module response null");
			EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, "100", "component_dossier_messages");
			errorHandler.addError(error);
		}
		// get publisher name from response
		String pubName = (String)moduleResponse.getAttribute(DossierConstants.PUBLISHER_NAME);
		// if publisher name is not setted generate error
		if((pubName==null) || pubName.trim().equals("")) {
			SpagoBITracer.major(DossierConstants.NAME_MODULE, this.getClass().getName(), 
		                       "getPublisherName", "Module Response doesn't contain a publisher name");
			EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, "100", "component_dossier_messages");
			errorHandler.addError(error);
		}
		
		
		//	if there are errors and they are only validation errors return the name for the detail publisher
		if(!errorHandler.isOK()) {
			if(GeneralUtilities.isErrorHandlerContainingOnlyValidationError(errorHandler)) {
				return pubName;
			} else {
				return new String("error");
			}
		} else {
			return pubName;
		}		
	}
}
