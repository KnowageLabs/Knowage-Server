/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.engines;


/**
 * 
 * This exception is thrown every time an error occurs during the startup 
 * of a new engine execution (EngineStartAction)
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIEngineStartupException extends SpagoBIEngineRuntimeException {
	
	private String engineName;
	
	/**
	 * Builds a <code>SpagoBIServiceException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public SpagoBIEngineStartupException(String engineName, String message) {
    	super(message);
    	setEngineName(engineName);
    }
	
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public SpagoBIEngineStartupException(String engineName, String message, Throwable ex) {
    	super(message, ex);
    	setEngineName(engineName);
    }
    
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public SpagoBIEngineStartupException(String engineName, Throwable ex) {
    	super("An unpredicted error occurred while executing " + engineName + " service.", ex); 
    	setEngineName(engineName);
    }

	public String getEngineName() {
		return engineName;
	}

	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}
	
	
}
