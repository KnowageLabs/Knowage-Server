/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
package it.eng.spagobi.utilities.exceptions;

import java.util.Locale;

import it.eng.spagobi.utilities.messages.EngineMessageBundle;


public class SpagoBIEngineRestServiceRuntimeException extends SpagoBIRuntimeException {


	private String localizationCode = "generic.error";
	private Locale locale = Locale.US;
	
	private static final long serialVersionUID = 7238971352468593356L;

    
	/**
	 * Builds a <code>SpagoBIEngineRestServiceException</code>.
	 * @param locale the locale
	 * @param ex the parent exception
	 */
    public SpagoBIEngineRestServiceRuntimeException( Locale locale, Throwable ex) {
    	super( ex) ;
    	this.locale = locale;
    }
	
	
	/**
	 * Builds a <code>SpagoBIEngineRestServiceException</code>.
	 * @param localizationCode the error code for localization
	 * @param locale the locale
	 * @param ex the parent exception
	 */
    public SpagoBIEngineRestServiceRuntimeException(String localizationCode, Locale locale, Throwable ex) {
    	super( ex) ;
    	setLocalizationCode(localizationCode);
    	this.locale = locale;
    }
	
	/**
	 * Builds a <code>SpagoBIEngineRestServiceException</code>.
	 * @param localizationCode the error code for localization
	 * @param locale the locale
	 * @param message a message for the exception
	 * @param ex the parent exception
	 */
    public SpagoBIEngineRestServiceRuntimeException(String localizationCode, Locale locale, String message, Throwable ex) {
    	super(message, ex) ;
    	setLocalizationCode(localizationCode);
    	this.locale = locale;
    }
    
    
	/**
	 * Builds a <code>SpagoBIEngineRestServiceException</code>.
	 * @param localizationCode the error code for localization
	 * @param locale the locale
	 * @param message a message for the exception
	 */
    public SpagoBIEngineRestServiceRuntimeException(String localizationCode, Locale locale, String message) {
    	super( message) ;
    	this.locale = locale;
    	setLocalizationCode(localizationCode);
    }

	public String getLocalizationCode() {
		return localizationCode;
	}

	public void setLocalizationCode(String localizationCode) {
		this.localizationCode = localizationCode;
	}

	@Override
	public String getLocalizedMessage() {
		String localizedMessage = EngineMessageBundle.getMessage(getLocalizationCode(), getLocale());
		return localizedMessage;
	}

	public Locale getLocale() {
		if(locale ==null){
			locale = Locale.US;
		}
		return locale;
	}
	
	

	
}
