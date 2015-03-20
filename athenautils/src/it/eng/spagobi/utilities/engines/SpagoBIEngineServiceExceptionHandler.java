/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.engines;





/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIEngineServiceExceptionHandler {
	private static  SpagoBIEngineServiceExceptionHandler instance;
	
	public static SpagoBIEngineServiceExceptionHandler getInstance() {
		if(instance == null) {
			instance = new SpagoBIEngineServiceExceptionHandler();
		}
		
		return instance;
	}
	
	private SpagoBIEngineServiceExceptionHandler() {
		
	}
	
	
	/**
	 * 
	 * @param serviceName
	 * <code>
	 * public void service(request, response) {
	 * 		
	 * 		logger.debug("IN");
	 * 
	 * 		try {
	 * 			...
	 * 		} catch (Throwable t) {
	 * 			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(serviceName, t);
	 * 		} finally {
	 * 			// relese resurces if needed
	 * 		}
	 * 
	 * 		logger.debug("OUT");
	 * }
	 * </code>
	 * 
	 * 
	 * @param e
	 * @return
	 */
	public SpagoBIEngineServiceException getWrappedException(String serviceName, IEngineInstance engineInstance,  Throwable e) {
		SpagoBIEngineServiceException serviceException = null;
		
		if(e instanceof SpagoBIEngineServiceException) {
			// this mean that the service have catched the exception nicely
			serviceException = (SpagoBIEngineServiceException)e;
		} else {
			// otherwise an unpredicted exception has been raised. 	
			
			// This is the last line of defense against exceptions. By the way all exceptions that are caught 
			// only here for the first time can be considered as bugs in the exception handling mechanism. When
			// such an exception is raised the code in the service should be fixed in order to catch it before and 
			// add some meaningful informations on what have caused it.
			Throwable rootException = e;
			while(rootException.getCause() != null) {
				rootException = rootException.getCause();
			}
			String str = rootException.getMessage()!=null? rootException.getMessage(): rootException.getClass().getName();
			String message = "An unpredicted error occurred while executing " + serviceName + " service."
							 + "\nThe root cause of the error is: " + str;
			
			serviceException = new SpagoBIEngineServiceException(serviceName, message, e);
		}
		
		serviceException.setEngineInstance(engineInstance);

		return serviceException;
	}
}
