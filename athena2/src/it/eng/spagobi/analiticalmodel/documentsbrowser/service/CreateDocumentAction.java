/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
