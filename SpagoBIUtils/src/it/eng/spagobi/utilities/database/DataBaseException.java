/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.database;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DataBaseException extends SpagoBIRuntimeException {
	
	/**
	 * Builds a <code>CacheException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public DataBaseException(String message) {
    	super(message);
    }
	
    /**
     * Builds a <code>CacheException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public DataBaseException(String message, Throwable ex) {
    	super(message, ex);
    }
    
    /**
     * Builds a <code>CacheException</code>.
     * 
     * @param ex previous Throwable object
     */
    public DataBaseException(Throwable ex) {
    	super(ex);
    }
}
