/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.utilities.engines;

import it.eng.spagobi.utilities.exceptions.SpagoBIException;

public class SpagoBIEngineException extends SpagoBIException {
	
	
	private IEngineInstance engineInstance;
	private String errorDescription; // for the final user (better if localized)
    
	/**
	 * Builds a <code>SpagoBIEngineException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public SpagoBIEngineException(String message) {
    	super(message);
    }
    
    public SpagoBIEngineException(String message, String description) {
    	super(message);
    	setDescription(description);
    }
	
    /**
     * Builds a <code>SpagoBIEngineException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public SpagoBIEngineException(String message, Throwable ex) {
    	super(message, ex);
    }
    
    public SpagoBIEngineException(String message, String description, Throwable ex) {
    	super(message, ex);
    	setDescription(description);
    }

	public IEngineInstance getEngineInstance() {
		return engineInstance;
	}

	public void setEngineInstance(IEngineInstance engineInstance) {
		this.engineInstance = engineInstance;
	}
}

