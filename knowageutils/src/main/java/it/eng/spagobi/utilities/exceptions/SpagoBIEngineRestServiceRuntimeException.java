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
