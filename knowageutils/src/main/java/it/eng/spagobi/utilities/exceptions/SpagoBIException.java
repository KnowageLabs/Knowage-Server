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

import java.util.ArrayList;
import java.util.List;

public class SpagoBIException extends Exception {
    
		
	/* 
	 * User oriented description of the exception. It is usually prompted to the user.
	 * Instead the message passed to the constructor is developer oriented and it should be just logged. 
	 */
	private String description;
	
	/*
	 * A list of possible solutions to the problem that have caused the exception
	 */
	private List hints;
	
	
	/**
	 * Builds a <code>SpagoBIException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public SpagoBIException(String message) {
    	super(message);
    }
	
    /**
     * Builds a <code>SpagoBIException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public SpagoBIException(String message, Throwable ex) {
    	super(message, ex);
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List getHints() {
		if(hints == null) {
			hints = new ArrayList();
		}
		return hints;
	}

	public void addHint(String hint) {
		getHints().add(hint);
	}
	
}

