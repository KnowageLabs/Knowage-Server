/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.error.EMFAbstractError;
import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.utilities.exceptions.CannotWriteErrorsToClientException;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class ServiceExceptionAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "SERVICE_EXCEPTION_ACTION";
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(ServiceExceptionAction.class);
    
	public void doService()  {
		logger.debug("IN");
		try {
			Collection<EMFAbstractError> errors = getErrorHandler().getErrors();
			Iterator it = errors.iterator();
			// if there is a CannotWriteErrorsToClient exception, CANNOT SEND ERRORS TO CLIENT
			while (it.hasNext()) {
				EMFAbstractError error = (EMFAbstractError) it.next();
				if (error instanceof EMFInternalError) {
					EMFInternalError internalError = (EMFInternalError) error;
					Exception e = internalError.getNativeException();
					if(e instanceof CannotWriteErrorsToClientException) {
						logger.error(e);
						return;
					}
				}
			}
			writeErrorsBackToClient();
		} finally {
			logger.debug("OUT");
		}
	}

}
