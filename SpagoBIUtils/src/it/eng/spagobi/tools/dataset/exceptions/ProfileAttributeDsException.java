/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.exceptions;

public class ProfileAttributeDsException extends DatasetException {

	private static final long serialVersionUID = 1L;
	public static final String USER_MESSAGE = "Check that all following profile attributes have value: ";


	/**
	 * Builds a <code>SpagoBIRuntimeException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public ProfileAttributeDsException(String message) {
    	super(message);  	
    }
	
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public ProfileAttributeDsException(String message, Throwable ex) {
    	super(message, ex);
    }


}
