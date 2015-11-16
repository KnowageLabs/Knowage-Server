/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.drivers.talend;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.drivers.handlers.IRolesHandler;

import java.util.List;

import org.apache.log4j.Logger;


public class TalendRolesHandler implements IRolesHandler {
	
	   private transient Logger logger = Logger.getLogger(TalendRolesHandler.class);
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.drivers.handlers.IRolesHandler#calculateRoles(java.lang.String)
	 */
	public List calculateRoles(String parameters) throws EMFInternalError, EMFUserError {
		String[] splittedParameters = parameters.split("&");
		if (splittedParameters == null || splittedParameters.length == 0) {
			logger.error("Missing parameters for roles retrieval");
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "Missing parameters for roles retrieval");
		}
		String biobjectIdStr = null;
		for (int i = 0; i < splittedParameters.length; i++) {
			String parameter = splittedParameters[i].trim();
			String[] splittedParameter = parameter.split("=");
			String parameterName = splittedParameter[0];
			if (parameterName.trim().equalsIgnoreCase("biobjectId")) {
				if (splittedParameter.length != 2) {
					logger.error("Malformed parameter for roles retrieval");
					throw new EMFInternalError(EMFErrorSeverity.ERROR, "Malformed parameter for roles retrieval");
				}
				biobjectIdStr = splittedParameter[1];
				break;
			}
		}
		if (biobjectIdStr == null) {
			logger.error("Missing parameters for roles retrieval");

			throw new EMFInternalError(EMFErrorSeverity.ERROR, "Missing parameters for roles retrieval");
		}
		Integer biobjectId = null;
		try {
			biobjectId = new Integer(biobjectIdStr);
		} catch (Exception e) {
			logger.error("Malformed BIObject id: " + biobjectIdStr,e);
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "Malformed BIObject id: " + biobjectIdStr);
		}
		List roles = null;
		try {
			roles = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(biobjectId);
		} catch (EMFUserError e) {
			logger.error("Error while loading correct roles for execution for document with id = " + biobjectId);
			throw e;
		}
		return roles;
	}

}
