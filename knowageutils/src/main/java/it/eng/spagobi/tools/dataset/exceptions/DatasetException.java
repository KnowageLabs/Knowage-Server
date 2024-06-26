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
package it.eng.spagobi.tools.dataset.exceptions;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.List;


/** This class is extended by  other classes representing particular datasetExceptions
 * 
 * @author gavardi
 *
 */

public class DatasetException extends SpagoBIRuntimeException {

	private static final long serialVersionUID = 1L;
	
	// this field is filled with a message for the user
	String userMessage;
	String fullMessage;
	
	public static final String USER_MESSAGE = "DataSet Exception";
	
	/**
	 * Builds a <code>SpagoBIRuntimeException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public DatasetException(String message) {
    	super(message);
    	this.userMessage = USER_MESSAGE;
    	this.fullMessage = message;    	
    }
	
    /**
     * Builds a <code>SpagoBIRuntimeException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public DatasetException(String message, Throwable ex) {
    	super(message, ex);
    	this.userMessage = USER_MESSAGE;
    	this.fullMessage = message;    
    }

	public String getUserMessage() {
		return userMessage;
	}

	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}
	
	public String getFullMessage() {
		return fullMessage;
	}

	public void setFullMessage(String fullMessage) {
		this.fullMessage = fullMessage;
	}

	

}
