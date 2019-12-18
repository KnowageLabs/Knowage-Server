/*
 * Knowage, Open Source Business Intelligence suite Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.hibernate.dialect;

import org.apache.log4j.Logger;

public class ExtendedPostgresDialect extends PostgreSQLDialect {

	static protected Logger logger = Logger.getLogger(ExtendedPostgresDialect.class);

	public ExtendedPostgresDialect() {
		super();
		logger.debug("IN"); // try {
		InlineFunctionRegistrationManager.registerInlineFunctions(this);
		// UserProfile userProfile = ProfileSingleton.getInstance().getUserProfile();

		// List<CustomizedFunction> customizedFunctions = new CustomizedFunctionsReader("mysql").getCustomDefinedFunctionList(userProfile);
		/*
		 * registerFunction("date_add_interval", new SQLFunctionTemplate(StandardBasicTypes.TIMESTAMP, "(?1 + INTERVAL '?2 ?3')"));
		 * registerFunction("date_diff", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, " date_part(?3, ?1 - ?2)"));
		 */

		logger.debug("OUT");
	}

}
