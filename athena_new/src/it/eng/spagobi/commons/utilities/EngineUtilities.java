/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.config.bo.Engine;

public class EngineUtilities {

	/**
	 * Checks if is internal.
	 * 
	 * @param engine the engine
	 * 
	 * @return true, if is internal
	 */
	public static boolean isInternal(Engine engine) {
		boolean response = false;
		Domain engineType = getEngTypeDom(engine);
		if("INT".equalsIgnoreCase(engineType.getValueCd())) 
			response=true;
		return response;
	}
	
	/**
	 * Checks if is external.
	 * 
	 * @param engine the engine
	 * 
	 * @return true, if is external
	 */
	public static boolean isExternal(Engine engine) {
		boolean response = false;
		Domain engineType = getEngTypeDom(engine);
		if("EXT".equalsIgnoreCase(engineType.getValueCd())) 
			response=true;
		return response;
	}
	
	
	private static Domain getEngTypeDom(Engine engine) {
		Domain engineType = null;
		try {
			engineType = DAOFactory.getDomainDAO().loadDomainById(engine.getEngineTypeId());
		} catch (EMFUserError e) {
			 SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, EngineUtilities.class.getName(), 
		 				        "getEngTypeDom", "Error retrieving engine type domain", e);
		}
		return engineType;
	}
}
