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
package it.eng.spagobi.utilities.engines;


/**
 * 
 * This exception is thrown every time an error occurs during the startup 
 * of a new engine execution (EngineStartAction)
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIEngineStartupException extends SpagoBIEngineRuntimeException {
	
	private String engineName;
	
	/**
	 * Builds a <code>SpagoBIServiceException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public SpagoBIEngineStartupException(String engineName, String message) {
    	super(message);
    	setEngineName(engineName);
    }
	
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public SpagoBIEngineStartupException(String engineName, String message, Throwable ex) {
    	super(message, ex);
    	setEngineName(engineName);
    }
    
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public SpagoBIEngineStartupException(String engineName, Throwable ex) {
    	super("An unpredicted error occurred while executing " + engineName + " service.", ex); 
    	setEngineName(engineName);
    }

	public String getEngineName() {
		return engineName;
	}

	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}
	
	
}
