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
