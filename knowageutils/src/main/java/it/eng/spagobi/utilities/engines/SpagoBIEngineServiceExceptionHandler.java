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
