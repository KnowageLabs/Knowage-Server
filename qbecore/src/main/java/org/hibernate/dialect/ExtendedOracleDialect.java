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
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.json.JSONObject;

import it.eng.qbe.utility.CustomFunctionsSingleton;
import it.eng.qbe.utility.CustomizedFunctionsReader;
import it.eng.qbe.utility.bo.CustomizedFunction;

public class ExtendedOracleDialect extends Oracle10gDialect {

	static protected Logger logger = Logger.getLogger(ExtendedOracleDialect.class);

	public ExtendedOracleDialect() {
		super();
		logger.debug("IN");
		// try {

		// UserProfile userProfile = ProfileSingleton.getInstance().getUserProfile();

		// List<CustomizedFunction> customizedFunctions = new CustomizedFunctionsReader("mysql").getCustomDefinedFunctionList(userProfile);

		InlineFunctionRegistrationManager.registerInlineFunctions(this);

		JSONObject jsonObject = CustomFunctionsSingleton.getInstance().getCustomizedFunctionsJSON();
		List<CustomizedFunction> customizedFunctions = new CustomizedFunctionsReader().getCustomDefinedFunctionListFromJSON(jsonObject, "oracle");

		if (customizedFunctions != null) {
			logger.debug("converting custom functions");
			for (Iterator<CustomizedFunction> iterator = customizedFunctions.iterator(); iterator.hasNext();) {
				CustomizedFunction customizedFunction = iterator.next();
				logger.debug("register function " + customizedFunction);
				logger.debug("register function with label " + customizedFunction.getLabel());
				logger.debug("register function with name " + customizedFunction.getName());

				if (!customizedFunction.getParameters().isEmpty()) {
					logger.debug("fuction with parameters");
					VarArgsSQLFunction sqlFunction = new VarArgsSQLFunction(customizedFunction.getName() + "(", ",", ")");
					registerFunction(customizedFunction.getName(), sqlFunction);
				} else {
					logger.debug("fuction without parameters");
					StandardSQLFunction sqlFunction = new StandardSQLFunction(customizedFunction.getName());
					registerFunction(customizedFunction.getName(), sqlFunction);
				}

			}
		} else {
			logger.debug("no custom functions defined for current db type");
		}

		logger.debug("OUT");
	}

}
