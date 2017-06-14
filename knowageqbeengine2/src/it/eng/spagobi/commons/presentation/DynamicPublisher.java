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

