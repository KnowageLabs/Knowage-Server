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

package org.hibernate.dialect;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.dialect.function.VarArgsSQLFunction;

import it.eng.qbe.utility.CustomizedFunctionsReader;
import it.eng.qbe.utility.ProfileDialectThreadLocal;
import it.eng.qbe.utility.bo.CustomizedFunction;
import it.eng.spagobi.commons.bo.UserProfile;

public class ExtendedMySQLDialect extends MySQLInnoDBDialect {

	static protected Logger logger = Logger.getLogger(ExtendedMySQLDialect.class);

	public ExtendedMySQLDialect() {
		super();
		logger.debug("IN");
		try {
			// UserProfile userProfile = ProfileSingleton.getInstance().getUserProfile();
			UserProfile userProfile = ProfileDialectThreadLocal.getUserProfile();
			if (userProfile == null) {
				logger.error("no profile could be get from singleton; stop adding custom functions");
			}

			List<CustomizedFunction> customizedFunctions = new CustomizedFunctionsReader("mysql").getCustomDefinedFunctionList(userProfile);

			if (customizedFunctions != null) {
				logger.debug("converting custom functions");
				for (Iterator<CustomizedFunction> iterator = customizedFunctions.iterator(); iterator.hasNext();) {
					CustomizedFunction customizedFunction = iterator.next();
					logger.debug("register function " + customizedFunction);

					registerFunction(customizedFunction.getFunction(), new VarArgsSQLFunction(customizedFunction.getFunction() + "(", ",", ")"));

					// registerFunction(customizedFunction.getFunction(), new StandardSQLFunction(customizedFunction.getFunction()));

				}
			} else {
				logger.debug("no custom functions defined for current db type");
			}
		} finally {
			ProfileDialectThreadLocal.unset();
		}

		logger.debug("OUT");
	}

}
