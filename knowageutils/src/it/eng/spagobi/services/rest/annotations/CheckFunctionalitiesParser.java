/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
