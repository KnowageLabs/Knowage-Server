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
package it.eng.spagobi.engines.kpi.publishers;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.presentation.PublisherDispatcherIFace;
import it.eng.spagobi.commons.utilities.GeneralUtilities;

import org.apache.log4j.Logger;

/**
 * Publishes the results of a detail request for a distributionlist into the correct 
 * jsp page according to what contained into the request. If Any errors occurred during the 
 * execution of the <code>DetailDistributionListModule</code> class, the publisher
 * is able to call the error page with the error message caught before and put into 
 * the error handler. If the input information don't fall into any of the cases declared,
 * another error is generated. 
 * 
 */
public class MetadataPublisher implements PublisherDispatcherIFace{
	static private Logger logger = Logger.getLogger(MetadataPublisher.class);
	
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
		logger.debug("IN");

		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
		
		// get the module response
		SourceBean moduleResponse = (SourceBean)responseContainer.getServiceResponse();
		String publisher_Name = (String)moduleResponse.getAttribute("publisher_Name");
		
		// if there are errors and they are only validation errors return the name for the detail publisher
		if(errorHandler.isOK() && errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
				if(publisher_Name!=null && publisher_Name !="") {				
					logger.info("Publish:" + publisher_Name);		
					logger.debug("OUT");
					return publisher_Name;
				}else{
					logger.error("Publisher name null");
					logger.info("Publish: KPI_METADATA_DEFAULT_PUB"  );
					logger.debug("OUT");
					return "KPI_METADATA_DEFAULT_PUB";						
				}
		}else{
			logger.debug("OUT");
			return "error";
		}
	}
}
