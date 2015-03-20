/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet;

import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorksheetEngineException extends SpagoBIEngineException {

	private static final long serialVersionUID = -2125969589281229800L;
	WorksheetEngineInstance engineInstance;


	/**
	 * Builds a <code>WorksheetEngineException</code>.
	 * 
	 * @param message Text of the exception
	 */
	public WorksheetEngineException(String message) {
		super(message);
	}

	/**
	 * Builds a <code>WorksheetEngineException</code>.
	 * 
	 * @param message Text of the exception
	 * @param ex previous Throwable object
	 */
	public WorksheetEngineException(String message, Throwable ex) {
		super(message, ex);
	}

}

