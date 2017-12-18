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