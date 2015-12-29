/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.whatif;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it), Alberto Ghedin (alberto.ghedin@eng.it)
 */

/**
 * The Class WhatIfEngineException.
 */
public class WhatIfEngineRuntimeException extends SpagoBIEngineRuntimeException {

	/**
	 * The hints. List hints;
	 */

	WhatIfEngineInstance engineInstance;

	/**
	 * Builds a <code>WhatIfException</code>.
	 * 
	 * @param message
	 *            Text of the exception
	 */
	public WhatIfEngineRuntimeException(String message) {
		super(message);
	}

	/**
	 * Builds a <code>WhatIfEngineException</code>.
	 * 
	 * @param message
	 *            Text of the exception
	 * @param ex
	 *            previous Exception object
	 */
	public WhatIfEngineRuntimeException(String message, Exception ex) {
		super(message, ex);
	}

	@Override
	public WhatIfEngineInstance getEngineInstance() {
		return engineInstance;
	}

	public void setEngineInstance(WhatIfEngineInstance engineInstance) {
		this.engineInstance = engineInstance;
	}

}
