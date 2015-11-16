/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.events.presentation;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.presentation.PublisherDispatcherIFace;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

/**
 * @author Gioia
 *
 */
public class DetailEventLogPublisher implements PublisherDispatcherIFace {
	
	public static final String DEFAULT_EVENTLOG_DETAIL_PUBLISHER = "defaultEventLogDetailPublisher";
	
	/**
	 * Class constructor.
	 */
	public DetailEventLogPublisher() {
		super();
	}
	
	/**
	 * Given the request at input, gets the name of the reference publisher,driving
	 * the execution into the correct jsp page, or jsp error page, if any error occurred.
	 * 
	 * @param requestContainer the request container
	 * @param responseContainer the response container
	 * 
	 * @return A string representing the name of the correct publisher, which will
	 * call the correct jsp reference.
	 */
	public String getPublisherName(RequestContainer requestContainer,
			ResponseContainer responseContainer) {
		
		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
		
		// get the module response
		SourceBean moduleResponse = (SourceBean) responseContainer.getServiceResponse().getAttribute("DetailEventLogModule");
		
		// if the module response is null throws an error and return the name of the errors publisher
		if (moduleResponse == null) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, 
		            this.getClass().getName(), 
		            "getPublisherName", 
		            "Module response null");
			EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10);
			errorHandler.addError(error);
			return "error";
		}
		
		// if there are some errors into the errorHandler (not validation errors), return the name for the errors publisher
		if (!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
			return new String("error");
		}
		
		String publisherName = (String) moduleResponse.getAttribute("PUBLISHER_NAME");
		
		SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "service",
				" PUBLISHER_NAME = "  + publisherName);
		
		if (publisherName != null) {
			return publisherName;
		} else {
			return DEFAULT_EVENTLOG_DETAIL_PUBLISHER;
		}
	}
}
