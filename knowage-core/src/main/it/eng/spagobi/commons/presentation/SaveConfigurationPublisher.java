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
