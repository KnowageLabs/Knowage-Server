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

package it.eng.spagobi.utilities.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

public class VerticaDataBase extends AbstractDataBase implements MetaDataBase {

	private static String CURRENT_SCHEMA_SQL_QUERY = "select CURRENT_SCHEMA()";

	public VerticaDataBase(IDataSource dataSource) {
		super(dataSource);
	}

	@Override
	public String getSchema(Connection conn) throws SQLException {
		ResultSet rs = conn.createStatement().executeQuery(CURRENT_SCHEMA_SQL_QUERY);
		return rs.next() ? rs.getString(1) : null;
	}

	@Override
	public String getCatalog(Connection conn) throws SQLException {
		return conn.getCatalog();
	}
}
