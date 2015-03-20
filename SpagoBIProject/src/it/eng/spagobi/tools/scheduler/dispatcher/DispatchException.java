/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.dispatcher;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DispatchException extends SpagoBIRuntimeException {
	/**
	 * Builds a <code>DispatchException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public DispatchException(String message) {
    	super(message);
    }
	
    /**
     * Builds a <code>DispatchException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public DispatchException(String message, Throwable ex) {
    	super(message, ex);
    }
    
    /**
     * Builds a <code>DispatchException</code>.
     * 
     * @param ex previous Throwable object
     */
    public DispatchException(Throwable ex) {
    	super(ex);
    }
}
