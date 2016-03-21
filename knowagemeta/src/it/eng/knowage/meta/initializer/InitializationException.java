/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.initializer;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class InitializationException extends RuntimeException {
	/**
	 * Builds a <code>InitializationException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public InitializationException(String message) {
    	super(message);
    }
	
    /**
     * Builds a <code>InitializationException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public InitializationException(String message, Throwable ex) {
    	super(message, ex);
    }
}
