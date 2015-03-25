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
import it.eng.spagobi.commons.utilities.SpagoBITracer;

public class DocumentTemplateBuildPublisher implements PublisherDispatcherIFace {
	
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

		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();

		// get the module response
		SourceBean moduleResponse = (SourceBean) responseContainer
				.getServiceResponse().getAttribute("DocumentTemplateBuildModule");
		
		// if the module response is null throws an error and return the name of
		// the errors publisher
		if (moduleResponse == null) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE,
					this.getClass().getName(), "getPublisherName",
					"Module response null");
			EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10);
			errorHandler.addError(error);
			return new String("error");
		}
		
		// if there are some errors into the errorHandler return the name for
		// the errors publisher
		if (!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
			return "error";
		}
		
		// if there are some error with INFORMATION severity, 
		// returns the name of the publishers that will display them
		if (!errorHandler.isOKBySeverity(EMFErrorSeverity.INFORMATION)) {
			return "DocumentTemplateErrorInformationPublisher";
		}

		String publisherName = (String) moduleResponse.getAttribute(SpagoBIConstants.PUBLISHER_NAME);
		if (publisherName == null || publisherName.trim().equals("")) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE,
					this.getClass().getName(), "getPublisherName",
					"Publisher name not set");
			EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10);
			errorHandler.addError(error);
			return "error";
		} else return publisherName;

	}

}
