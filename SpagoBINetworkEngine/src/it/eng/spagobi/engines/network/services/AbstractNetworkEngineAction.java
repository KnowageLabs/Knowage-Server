/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.services;

import it.eng.spagobi.engines.network.NetworkEngineInstance;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;

import org.apache.log4j.Logger;

/**
 * The Class AbstractConsoleEngineAction.
 * 
 * @author ...
 */
public class AbstractNetworkEngineAction extends AbstractEngineAction {
	
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(AbstractNetworkEngineAction.class);
    	
		
	/**
	 * Gets the console engine instance.
	 * 
	 * @return the console engine instance
	 */
	public NetworkEngineInstance getNetworkEngineInstance() {
		return (NetworkEngineInstance)getEngineInstance();
	}
	
	
}
