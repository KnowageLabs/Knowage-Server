/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.birt.exceptions;

public class ConnectionParameterNotValidException extends ConnectionDefinitionException {
	
	/**
	 * Instantiates a new connection parameter not valid exception.
	 * 
	 * @param msg the msg
	 */
	public ConnectionParameterNotValidException(String msg) {
		super(msg);
	}
	
}