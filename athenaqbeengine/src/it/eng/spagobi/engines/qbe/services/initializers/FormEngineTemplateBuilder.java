/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.initializers;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.presentation.DynamicPublisher;
import it.eng.spagobi.utilities.engines.AbstractEngineStartAction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class FormEngineTemplateBuilder extends AbstractEngineStartAction {	
	
	// INPUT PARAMETERS
	private final static String PARAM_MODALITY = "MODALITY";
	
	// OUTPUT PARAMETERS
	public static final String LANGUAGE = "LANGUAGE";
	public static final String COUNTRY = "COUNTRY";
	
	// SESSION PARAMETRES	


	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(FormEngineStartAction.class);
    
    public static final String ENGINE_NAME = "SpagoBIFormEngine";
		
    public void service(SourceBean serviceRequest, SourceBean serviceResponse) {
    	
    	Locale locale;
    	
    	
    	logger.debug("IN");
       
    	try {
    		setEngineName(ENGINE_NAME);
			super.service(serviceRequest, serviceResponse);			
			
			locale = this.getLocale();
					
			setAttribute(LANGUAGE, locale.getLanguage());
			setAttribute(COUNTRY, locale.getCountry());
			
			String publisherName = "NEW_FORM_ENGINE_TEMPLATE_BUILD_ACTION_PUBLISHER";
			
			String modality = this.getAttributeAsString(PARAM_MODALITY);
			logger.debug("Input " + PARAM_MODALITY + " parameter is " + modality);
			if (modality != null && modality.trim().equalsIgnoreCase("EDIT")) {
				// edit template
				if (this.requestContainsAttribute( QbeEngineFromDatasetStartAction.DATASET_LABEL )) {
					publisherName = "EDIT_FORM_ENGINE_TEMPLATE_BUILD_FROM_DATASET_ACTION_PUBLISHER";
				} else {
					publisherName = "EDIT_FORM_ENGINE_TEMPLATE_BUILD_ACTION_PUBLISHER";
				}
			}
			
			serviceResponse.setAttribute(DynamicPublisher.PUBLISHER_NAME, publisherName);
			
		} catch (Throwable e) {
			SpagoBIEngineStartupException serviceException = null;
			
			if(e instanceof SpagoBIEngineStartupException) {
				serviceException = (SpagoBIEngineStartupException)e;
			} else {
				Throwable rootException = e;
				while(rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String str = rootException.getMessage()!=null? rootException.getMessage(): rootException.getClass().getName();
				String message = "An unpredicted error occurred while executing " + getEngineName() + " service."
								 + "\nThe root cause of the error is: " + str;
				
				serviceException = new SpagoBIEngineStartupException(getEngineName(), message, e);
			}
			
			throw serviceException;
		} finally {
			logger.debug("OUT");
		}		

		
	}    
}
