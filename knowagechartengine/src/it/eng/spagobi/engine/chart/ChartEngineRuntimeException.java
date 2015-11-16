/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engine.chart;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * The Class CockpitEngineRuntimeException.
 */
public class ChartEngineRuntimeException extends SpagoBIEngineRuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * The hints. List hints;
	 */

	ChartEngineInstance engineInstance;

	/**
	 * Builds a <code>CockpitEngineRuntimeException</code>.
	 * 
	 * @param message
	 *            Text of the exception
	 */
	public ChartEngineRuntimeException(String message) {
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
	public ChartEngineRuntimeException(String message, Exception ex) {
		super(message, ex);
	}

	@Override
	public ChartEngineInstance getEngineInstance() {
		return engineInstance;
	}

	public void setEngineInstance(ChartEngineInstance engineInstance) {
		this.engineInstance = engineInstance;
	}
}
