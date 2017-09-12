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
package it.eng.spagobi.utilities.exceptions;

/**
 * This exception is thrown when parameter passed in to a service are non correct 
 * (i.e. missing or not properly valorized).
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIServiceParameterException extends SpagoBIServiceException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Builds a <code>SpagoBIServiceException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public SpagoBIServiceParameterException(String serviceName, String message) {
    	super(serviceName, message);
    }
	
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public SpagoBIServiceParameterException(String serviceName, String message, Throwable ex) {
    	super(serviceName, message, ex);
    }
    
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public SpagoBIServiceParameterException(String serviceName, Throwable ex) {
    	super(serviceName, ex); 
    }
}
