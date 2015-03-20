/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.georeport.features.provider;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

public class FeaturesProviderRuntimeException extends SpagoBIEngineRuntimeException {
    	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Builds a <code>GeoEngineException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public FeaturesProviderRuntimeException(String message) {
    	super(message);
    }
	
    /**
     * Builds a <code>GeoEngineException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public FeaturesProviderRuntimeException(String message, Throwable ex) {
    	super(message, ex);
    }

}

