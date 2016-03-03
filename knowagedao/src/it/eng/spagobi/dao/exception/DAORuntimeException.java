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

package it.eng.spagobi.dao.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DAORuntimeException extends RuntimeException {
    
	/**
	 * Keep compiler happy
	 */
	private static final long serialVersionUID = 1L;
	
	/* 
	 * User oriented description of the exception. It is usually prompted to the user.
	 * Instead the message passed to the constructor is developer oriented and it should be just logged. 
	 */
	private String description;
	
	
	/**
	 * Builds a <code>SpagoBIRuntimeException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public DAORuntimeException(String message) {
    	super(message);
    }
	
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public DAORuntimeException(String message, Throwable ex) {
    	super(message, ex);
    }
    
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param ex previous Throwable object
     */
    public DAORuntimeException(Throwable ex) {
    	super(ex);
    }
    
    public String getRootCause() {
		String rootCause;		
		Throwable rootException;
		
		rootException = this;
		while(rootException.getCause() != null) {
			rootException = rootException.getCause();
		}
		
		rootCause = rootException.getMessage()!=null
			? rootException.getClass().getName() + ": " + rootException.getMessage()
			: rootException.getClass().getName();
		
		return rootCause;
	}
    
    public String getStackTraceDump() {
    	StringWriter buffer = new StringWriter();
    	this.printStackTrace(new PrintWriter(buffer));
    	return buffer.toString();
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

