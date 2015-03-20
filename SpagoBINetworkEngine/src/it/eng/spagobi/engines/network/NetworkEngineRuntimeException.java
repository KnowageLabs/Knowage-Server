/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author ...
 */

/**
 * The Class NetworkEngineException.
 */
public class NetworkEngineRuntimeException extends SpagoBIEngineRuntimeException {
    
	/** The hints. 
	List hints;
	*/
	
	NetworkEngineInstance engineInstance;
	
	
	/**
	 * Builds a <code>NetworkException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public NetworkEngineRuntimeException(String message) {
    	super(message);
    }
	
    /**
     * Builds a <code>NetworkEngineException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public NetworkEngineRuntimeException(String message, Throwable ex) {
    	super(message, ex);
    }
    
    public NetworkEngineInstance getEngineInstance() {
		return engineInstance;
	}

	public void setEngineInstance(NetworkEngineInstance engineInstance) {
		this.engineInstance = engineInstance;
	}
    
   


}

