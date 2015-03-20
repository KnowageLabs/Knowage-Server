/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.exceptions;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIServiceException extends SpagoBIRuntimeException {
	
	private String serviceName;
	/**
	 * Builds a <code>SpagoBIServiceException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public SpagoBIServiceException(String serviceName, String message) {
    	super(message);
    	setServiceName(serviceName);
    }
	
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public SpagoBIServiceException(String serviceName, String message, Throwable ex) {
    	super(message, ex);
    	setServiceName(serviceName);
    }
    
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public SpagoBIServiceException(String serviceName, Throwable ex) {
    	super("An unpredicted error occurred while executing " + serviceName + " service.", ex); 
    	setServiceName(serviceName);
    }

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
}
