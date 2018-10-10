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
import java.sql.Statement;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

public class TeradataDataBase extends AbstractDataBase implements MetaDataBase {

	public TeradataDataBase(IDataSource dataSource) {
		super(dataSource);
	}

	// https://community.teradata.com/t5/Blog/How-to-determine-or-switch-the-current-database-using-the/ba-p/66995
	@Override
	public String getSchema(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("help session");
			try {
				rs.next();
				return rs.getString(5);
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
	}

	/*
	 * https://developer.teradata.com/doc/connectivity/jdbc/reference/current/jdbcug_chapter_2.html Retrieves a description of the given attribute of the given
	 * type for a UDT that is available in the given schema and catalog (catalog should be null because *no catalog is supported in Teradata*.
	 */
	@Override
	public String getCatalog(Connection conn) throws SQLException {
		return null;
	}
}
