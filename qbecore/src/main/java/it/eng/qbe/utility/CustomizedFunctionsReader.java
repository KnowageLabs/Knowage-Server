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
package it.eng.qbe.utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.qbe.utility.bo.CustomizedFunction;
import it.eng.spagobi.commons.bo.UserProfile;

public class CustomizedFunctionsReader {

	static protected Logger logger = Logger.getLogger(CustomizedFunctionsReader.class);

	String databaseName;

	public CustomizedFunctionsReader(String dialect) {

		String upperCaseDialect = dialect.toUpperCase();
		if (upperCaseDialect.contains("MYSQL")) {
			databaseName = "mysql";
		} else if (upperCaseDialect.contains("ORACLE")) {
			databaseName = "oracle";
		} else if (upperCaseDialect.contains("POSTGRES")) {
			databaseName = "postgres";
		} else if (upperCaseDialect.contains("SQLSERVER")) {
			databaseName = "sqlserver";
		}
	}

	public String getCustomDefinedFunctionString(UserProfile userProfile) {
		logger.debug("IN");
		String toReturn = "";
		List<CustomizedFunction> returned = getCustomDefinedFunctionList(userProfile);

		for (Iterator iterator = returned.iterator(); iterator.hasNext();) {
			CustomizedFunction customizedFunction = (CustomizedFunction) iterator.next();
			toReturn += "|" + customizedFunction.getFunction();
		}
		logger.debug("String returned " + toReturn);
		logger.debug("OUT");
		return toReturn;
	}

	public List<CustomizedFunction> getCustomDefinedFunctionList(UserProfile userProfile) {
		logger.debug("IN");

		List<CustomizedFunction> toReturn = new ArrayList<CustomizedFunction>();

		try {

			ConfigReader configReader = new ConfigReader(userProfile);
			String propertyValue = configReader.readCustom();

			if (propertyValue == null) {
				logger.error("Config property KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS no defined");
				return null;
			} else {
				logger.debug("found KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS property with value: " + propertyValue);
			}

			JSONObject jsonObj = new JSONObject(propertyValue);
			JSONArray funcArray = jsonObj.optJSONArray(databaseName);

			if (funcArray != null) {
				for (int i = 0; i < funcArray.length(); i++) {
					JSONObject func = funcArray.optJSONObject(i);
					CustomizedFunction cust = new CustomizedFunction(func);
					toReturn.add(cust);
				}
			}
		} catch (Exception e) {
			logger.error("Error in reading KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS config value and transforming it into custom functions", e);
		}

		logger.debug("OUT");

		return toReturn;
	}

}
