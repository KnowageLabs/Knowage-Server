/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.dao;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIDOAException extends SpagoBIRuntimeException {

	private static final long serialVersionUID = 1L;
	
	public SpagoBIDOAException(String message) {
		super(message);
	}
	
	public SpagoBIDOAException(Throwable ex) {
		super(ex);
	}

	
	public SpagoBIDOAException(String message, Throwable ex) {
		super(message, ex);
	}
}
