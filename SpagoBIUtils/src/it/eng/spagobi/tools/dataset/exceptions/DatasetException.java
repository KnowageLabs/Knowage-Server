/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
