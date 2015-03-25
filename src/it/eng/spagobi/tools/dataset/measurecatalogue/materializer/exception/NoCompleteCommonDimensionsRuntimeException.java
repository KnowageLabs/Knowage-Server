/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.measurecatalogue.materializer.exception;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class NoCompleteCommonDimensionsRuntimeException extends SpagoBIRuntimeException{

	private static final long serialVersionUID = 4985788892142987771L;

	public NoCompleteCommonDimensionsRuntimeException(String message) {
		super(message);
	}
	
	public NoCompleteCommonDimensionsRuntimeException(String message, Throwable e) {
		super(message, e);
	}
}



	
	