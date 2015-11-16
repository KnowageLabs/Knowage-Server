/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities;

public class GenericSavingException extends Exception {
	
	private String description;
	
	/**
	 * Instantiates a new generic saving exception.
	 */
	public GenericSavingException() {
		super();
	}
	
	/**
	 * Instantiates a new generic saving exception.
	 * 
	 * @param msg the msg
	 */
	public GenericSavingException(String msg) {
		super();
		this.setLocalizedMessage(msg);
	}
	
    /* (non-Javadoc)
     * @see java.lang.Throwable#getLocalizedMessage()
     */
    public String getLocalizedMessage() {
        return description;
    }
    
    /**
     * Sets the localized message.
     * 
     * @param msg the new localized message
     */
    public void setLocalizedMessage(String msg) {
        this.description = msg;
    }
}
