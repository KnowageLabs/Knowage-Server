/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.presentation.PublisherDispatcherIFace;

import org.apache.log4j.Logger;

public class DynamicPublisher implements PublisherDispatcherIFace {
	
	public static final String PUBLISHER_NAME = "PUBLISHER_NAME";
	
	private static transient Logger logger = Logger.getLogger(DynamicPublisher.class);
	
	/**
	 * Class constructor.
	 */
	public DynamicPublisher() {
		super();
	}
	
	public String getPublisherName(RequestContainer request,
			ResponseContainer response) {
		logger.debug("IN");
		String publisherName = null;
		SourceBean serviceResponse = response.getServiceResponse();
		publisherName = (String) serviceResponse.getAttribute(PUBLISHER_NAME);
		logger.debug("OUT: publisherName = " + publisherName);
		return publisherName;
	}
}

