/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package it.eng.spagobi.engines.network;

import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

/**
 * The Class NetworkEngineException.
 */
public class NetworkEngineException extends SpagoBIEngineException {
    
	/** The hints. 
	List hints;
	*/
	
	NetworkEngineInstance engineInstance;
	
	
	/**
	 * Builds a <code>NetworkEngineException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public NetworkEngineException(String message) {
    	super(message);
    }
	
    /**
     * Builds a <code>NetworkEngineException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public NetworkEngineException(String message, Throwable ex) {
    	super(message, ex);
    }
    
    public NetworkEngineInstance getEngineInstance() {
		return engineInstance;
	}

	public void setEngineInstance(NetworkEngineInstance engineInstance) {
		this.engineInstance = engineInstance;
	}
    
   


}

