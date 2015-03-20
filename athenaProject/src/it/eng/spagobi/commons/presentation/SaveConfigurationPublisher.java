/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.commons.presentation;

import it.eng.spago.base.PortletAccess;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.presentation.PublisherDispatcherIFace;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
/**
 * A particular publisher used to save configuration.
 * 
 * @author sulis
 */
public class SaveConfigurationPublisher implements PublisherDispatcherIFace {

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

		//SourceBean serviceRequest = requestContainer.getServiceRequest();
		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
		if (errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
			PortletRequest portletRequest = PortletAccess.getPortletRequest();
			PortletMode mode = portletRequest.getPortletMode(); 
			if (PortletMode.EDIT.equals(mode)) return "saveConfiguration";
			if (PortletMode.HELP.equals(mode)) return "saveConfiguration";
			else return "saveConfigurationLoop";
		}
		else
			return new String("error");
	}

}
