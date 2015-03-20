/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core;


import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.service.JSONFailure;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONException;

/**
 * The Class ServiceExceptionAction.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class ServiceExceptionAction extends AbstractQbeEngineAction {
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(ServiceExceptionAction.class);
	
   
	public void service(SourceBean serviceRequest, SourceBean serviceResponse)  {
		
		EMFErrorHandler errorHandler;
		Collection errors ;
		Iterator it;
		
		logger.debug("IN");
		try {
		
			errorHandler = getErrorHandler();
			Assert.assertNotNull(errorHandler, "error handler cannot be null");
			
			errors = errorHandler.getErrors();
			logger.debug("error handler contains [" + errors.size() + "] error/s");
			
			it = errors.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				logger.debug("Error type [" + o.getClass().getName()+ "]");
				if(o instanceof EMFInternalError) {
					EMFInternalError error = (EMFInternalError)o;
					Exception e = error.getNativeException();
					if(e instanceof SpagoBIEngineServiceException) {
						SpagoBIEngineServiceException serviceError = (SpagoBIEngineServiceException)e;
						logError(serviceError);
						
						try {
							writeBackToClient( new JSONFailure( serviceError ) );
						} catch (IOException ioe) {
							String message = "Impossible to write back the responce to the client";
							throw new SpagoBIEngineServiceException(getActionName(), message, e);
						} catch (JSONException je) {
							String message = "Error while serializing error into JSON object";
							throw new SpagoBIEngineServiceException(getActionName(), message, je);
						}
					} else {
						logger.error("Unespected exception",e);		
					}
				} else {
					logger.error(o.toString());
				}
			} 
		} catch(Throwable t) {
			logger.error("An error occurred while handling a previously thrown Exception");
		} finally {
			logger.debug("OUT");
		}
	}


	private void logError(SpagoBIEngineServiceException serviceError) {
		logger.error(serviceError.getMessage());
		logger.error("The error root cause is: " + serviceError.getRootCause());	
		if(serviceError.getHints().size() > 0) {
			Iterator hints = serviceError.getHints().iterator();
			while(hints.hasNext()) {
				String hint = (String)hints.next();
				logger.info("hint: " + hint);
			}
			
		}
		logger.error("The error root cause stack trace is:",  serviceError.getCause());	
		logger.error("The error full stack trace is:", serviceError);			
	}
}
