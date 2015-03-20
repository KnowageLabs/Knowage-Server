/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engine.cockpit;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * The Class CockpitEngineRuntimeException.
 */
public class CockpitEngineRuntimeException extends SpagoBIEngineRuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * The hints. List hints;
	 */

	CockpitEngineInstance engineInstance;

	/**
	 * Builds a <code>CockpitEngineRuntimeException</code>.
	 * 
	 * @param message
	 *            Text of the exception
	 */
	public CockpitEngineRuntimeException(String message) {
		super(message);
	}

	/**
	 * Builds a <code>CockpitEngineRuntimeException</code>.
	 * 
	 * @param message
	 *            Text of the exception
	 * @param ex
	 *            previous Exception object
	 */
	public CockpitEngineRuntimeException(String message, Exception ex) {
		super(message, ex);
	}

	@Override
	public CockpitEngineInstance getEngineInstance() {
		return engineInstance;
	}

	public void setEngineInstance(CockpitEngineInstance engineInstance) {
		this.engineInstance = engineInstance;
	}
}
