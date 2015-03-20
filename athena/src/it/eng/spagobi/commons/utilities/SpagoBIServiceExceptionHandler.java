/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class SpagoBIServiceExceptionHandler {
	private static  SpagoBIServiceExceptionHandler instance;
		
	private static transient Logger logger = Logger.getLogger(SpagoBIServiceExceptionHandler.class);

	public static SpagoBIServiceExceptionHandler getInstance() {
		if(instance == null) {
			instance = new SpagoBIServiceExceptionHandler();
		}
		
		return instance;
	}
	
	private SpagoBIServiceExceptionHandler() {
		
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
	 * 			throw SpagoBIServiceExceptionHandler.getInstance().getWrappedException(serviceName, t);
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
	public SpagoBIServiceException getWrappedException(String serviceName,  Throwable e) {
		SpagoBIServiceException serviceException = null;
		MessageBuilder msgBuild = new MessageBuilder();
		Locale locale = null;	
		RequestContainer requestContainer=RequestContainer.getRequestContainer();
		if(requestContainer!=null){
			SessionContainer permSess=requestContainer.getSessionContainer().getPermanentContainer();
			String lang=(String)permSess.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String country=(String)permSess.getAttribute(SpagoBIConstants.AF_COUNTRY);
			if(lang!=null && country!=null){
				locale=new Locale(lang,country,"");
			}
		}else{
			locale = GeneralUtilities.getDefaultLocale();	
		}
		
		if(e instanceof SpagoBIServiceException) {
			// this mean that the service have catched the exception nicely
			serviceException = (SpagoBIServiceException)e;
			String sms = serviceException.getMessage();
			sms = msgBuild.getMessage(sms, locale);	
			serviceException = new SpagoBIServiceException(serviceName, sms, e);
		} else {
			// otherwise an unpredicted exception has been raised. 	
			
			// This is the last line of defense against exceptions. Bytheway all exceptions that are catched 
			// only here for the first time can be considered as bugs in the exception handling mechanism. When
			// such an exception is raised the code in the service should be fixed in order to catch it before and add some meaningfull
			// informations on what have caused it.
			Throwable rootException = e;
			while(rootException.getCause() != null) {
				rootException = rootException.getCause();
			}
			String str = rootException.getMessage()!=null? rootException.getMessage(): rootException.getClass().getName();
			str = msgBuild.getMessage(str, locale);	
			String message = "An unexpecetd error occurred while executing service."
							 + "\nThe root cause of the error is: " + str;
			
			serviceException = new SpagoBIServiceException(serviceName, message, e);
			
		}

		logError(serviceException);
		
		return serviceException;
	}
	
	public static void logError(SpagoBIServiceException serviceError) {
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
