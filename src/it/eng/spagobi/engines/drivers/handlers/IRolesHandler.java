/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.drivers.handlers;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;

import java.util.List;

public interface IRolesHandler {

	/**
	 * Calculate roles.
	 * 
	 * @param parameters the parameters
	 * 
	 * @return the list
	 * 
	 * @throws EMFInternalError the EMF internal error
	 * @throws EMFUserError the EMF user error
	 */
	public List calculateRoles(String parameters) throws EMFInternalError, EMFUserError;
	
}
