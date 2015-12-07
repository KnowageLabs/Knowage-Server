/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.listener;

import it.eng.spagobi.utilities.exceptions.SpagoBIException;

@SuppressWarnings("serial")
public class DataSetListenerException extends SpagoBIException {

	public DataSetListenerException(String message) {
		super(message);
	}

	public DataSetListenerException(String message, Throwable ex) {
		super(message, ex);
	}

}
