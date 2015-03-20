/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.exceptions;

public class OpenOfficeConnectionException extends Exception {

	/**
	 * Instantiates a new open office connection exception.
	 */
	public OpenOfficeConnectionException() {
		super();
	}
	
	/**
	 * Instantiates a new open office connection exception.
	 * 
	 * @param message the message
	 */
	public OpenOfficeConnectionException(String message) {
	 	super(message);
	}
	 
	/**
	 * Instantiates a new open office connection exception.
	 * 
	 * @param message the message
	 * @param cause the cause
	 */
	public OpenOfficeConnectionException(String message, Throwable cause) {
	    super(message, cause);
	} 
	
	/**
	 * Instantiates a new open office connection exception.
	 * 
	 * @param cause the cause
	 */
	public OpenOfficeConnectionException(Throwable cause) {
	    super(cause);
	}
	
}
