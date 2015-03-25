/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;

import org.apache.log4j.Logger;

public class ChangeLanguagePublisher extends GenericPublisher {

    static Logger logger = Logger.getLogger(LoginPublisher.class);

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.spago.presentation.PublisherDispatcherIFace#getPublisherName(it.eng.spago.base.RequestContainer,
     *      it.eng.spago.base.ResponseContainer)
     */
    public String getPublisherName(RequestContainer requestContainer, ResponseContainer responseContainer) {
	logger.debug("IN");

	SourceBean serviceResp = responseContainer.getServiceResponse();
	// get the response of the module
	String publisherName = (String) serviceResp.getAttribute("PUBLISHER_NAME");

	/*
	 * if(!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) { return new
	 * String("error"); }
	 */
	if (publisherName != null) {
	    logger.debug("OUT.publisherName=" + publisherName);
	    return publisherName;
	} else {
	    logger.debug("OUT.publisherName=login");
	    return new String("login");
	}
    }

}