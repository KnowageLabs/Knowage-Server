/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.whatif.template;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public class WhatIfTemplateParseException extends SpagoBIEngineRuntimeException {

	/**
	 * Builds a <code>SpagoBIRuntimeException</code>.
	 * 
	 * @param message
	 *            Text of the exception
	 */
	public WhatIfTemplateParseException(String message) {
		super(message);
	}

	/**
	 * Builds a <code>SpagoBIRuntimeException</code>.
	 * 
	 * @param message
	 *            Text of the exception
	 * @param ex
	 *            previous Exception object
	 */
	public WhatIfTemplateParseException(String message, Exception ex) {
		super(message, ex);
	}

	/**
	 * Builds a <code>SpagoBIRuntimeException</code>.
	 * 
	 * @param ex
	 *            previous Exception object
	 */
	public WhatIfTemplateParseException(Exception ex) {
		super(ex);
	}

}
