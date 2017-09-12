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
package it.eng.spagobi.analiticalmodel.documentsbrowser.service;

import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class CreateDocumentAction extends AbstractSpagoBIAction {

	public static final String SERVICE_NAME = "CREATE_DOCUMENT_ACTION";
	public static final String URL_TO_CALL_BASE = "urlToCallBase";
	public static final String URL_TO_CALL_PARAMS = "urlToCallParams";
	
	

	// logger component
	private static Logger logger = Logger.getLogger(CreateDocumentAction.class);

	public void doService() {
		logger.debug("IN");

		try {
			
			String urlToCallBase = this.getAttributeAsString(URL_TO_CALL_BASE);
			logger.debug("Parameter [" + URL_TO_CALL_BASE + "] is equal to [" + urlToCallBase + "]");
			
			String urlToCallParams = this.getAttributeAsString(URL_TO_CALL_PARAMS);
			logger.debug("Parameter [" + URL_TO_CALL_PARAMS + "] is equal to [" + urlToCallParams + "]");
			
			setAttribute(URL_TO_CALL_BASE, urlToCallBase);
			setAttribute(URL_TO_CALL_PARAMS, urlToCallParams);
			
		} catch (SpagoBIServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An internal error has occured", e);
		} finally {
			logger.debug("OUT");
		}
	}

}
