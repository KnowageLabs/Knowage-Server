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
package it.eng.spagobi.tools.datasource.bo.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.spagobi.tools.datasource.bo.JDBCDataSourcePoolConfiguration;

public class JDBCDataSourcePoolConfigurationJSONSerializer {

	public Object serialize(Object obj) {
		String jsonString = null;
		ObjectMapper mapper = new ObjectMapper();

		if (obj == null) {
			return null;
		}
		if (!(obj instanceof JDBCDataSourcePoolConfiguration)) {
			throw new RuntimeException("JDBCDataSourcePoolConfigurationJSONSerializer is not able to serialize: " + obj.getClass().getName());
		}

		try {
			JDBCDataSourcePoolConfiguration jdbcPoolConfiguration = (JDBCDataSourcePoolConfiguration) obj;
			jsonString = mapper.writeValueAsString(jdbcPoolConfiguration);

		} catch (Throwable t) {
			throw new RuntimeException("An Error has occurred while serializing object: " + obj, t);
		}
		return jsonString;
	}

}
