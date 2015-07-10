/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.rest.annotations;

import it.eng.spagobi.commons.bo.UserProfile;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * @author Giorgio Federici (giorgio.federici@eng.it)
 */
public class CheckFunctionalitiesParser {

	static private Logger logger = Logger.getLogger(CheckFunctionalitiesParser.class);

	public boolean isPublicService(Method method) throws Exception {

		logger.debug("Method isPublicService: Start");

		if (method.isAnnotationPresent(UserConstraint.class)) {
			logger.debug("Method isPublicService: The service is not public");
			return false;
		} else {
			logger.debug("Method isPublicService: The service is public");
			return true;
		}
	}

	public boolean checkFunctionalitiesByAnnotation(Method method, UserProfile profile) throws Exception {

		logger.debug("Method checkFunctionalitiesByAnnotation: Start");
		boolean authorized = false;

		if (method.isAnnotationPresent(UserConstraint.class)) {

			Collection functionalities = profile.getFunctionalities();

			UserConstraint checkFuncs = method.getAnnotation(UserConstraint.class);
			String[] funcsAnnotated = checkFuncs.functionalities();

			for (int i = 0; i < funcsAnnotated.length; i++) {
				if (functionalities.contains(funcsAnnotated[i])) {
					authorized = true;
					logger.debug("Method checkFunctionalitiesByAnnotation: Functionality found");
					break;
				}
			}
		} else {
			authorized = true;
		}

		logger.debug("Method checkFunctionalitiesByAnnotation: End");
		return authorized;
	}
}
