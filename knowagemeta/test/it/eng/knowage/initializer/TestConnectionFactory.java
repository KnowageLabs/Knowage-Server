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

package it.eng.knowage.initializer;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class TestConnectionFactory {

	public static Connection createConnection(String url, String usr, String pwd, String driver) {
		Connection connection;

		connection = null;
		try {
			java.sql.Driver o = (java.sql.Driver) Class.forName(driver).newInstance();
			boolean b = o.acceptsURL(url);
			connection = DriverManager.getConnection(url, usr, pwd);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to create connection [url: " + url + "; usr: " + usr + "; pwd: " + pwd + "; driver: " + driver + "]", t);
		}
		return connection;
	}
}
