/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.birt.exceptions;

public class ConnectionDefinitionException extends Exception {
	
	protected String description;
	
	/**
	 * Instantiates a new connection definition exception.
	 */
	public ConnectionDefinitionException() {
		super();
	}
	
	/**
	 * Instantiates a new connection definition exception.
	 * 
	 * @param msg the msg
	 */
	public ConnectionDefinitionException(String msg) {
		super(msg);
		this.description = msg;
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description.
	 * 
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
}
