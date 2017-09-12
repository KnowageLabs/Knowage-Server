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

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.presentation.PublisherDispatcherIFace;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;

import org.apache.log4j.Logger;

public abstract class GenericPublisher implements PublisherDispatcherIFace {

    static Logger logger = Logger.getLogger(GenericPublisher.class);
    
    protected String getPublisherName(RequestContainer requestContainer, ResponseContainer responseContainer,SourceBean moduleResponse) {
	logger.debug("IN");
	EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
	
	// if there are some errors (not validation error) into the errorHandler
	// return the name for the errors publisher
	if (!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
	    if (!GeneralUtilities.isErrorHandlerContainingOnlyValidationError(errorHandler)) {
		logger.debug("OUT");
		return "error";
	    }
	}
	
	// if the module response is null throws an error and return the name of
	// the errors publisher
	if (moduleResponse == null) {
	    logger.error("Module response null");
	    EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10);
	    errorHandler.addError(error);
	    logger.debug("OUT");
	    return "error";
	}
	// get the value of the publisher name attribute
	String publisherName = (String) moduleResponse.getAttribute(SpagoBIConstants.PUBLISHER_NAME);
	logger.debug("publisherName="+publisherName);
	if (publisherName != null && !publisherName.trim().equals("")) {
	    logger.debug("OUT");
	    return publisherName;
	} else {
	   // logger.error("Publisher name attribute not found");
	    EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10);
	    errorHandler.addError(error);
	    logger.debug("OUT");
	    return "error";
	}
    }

}