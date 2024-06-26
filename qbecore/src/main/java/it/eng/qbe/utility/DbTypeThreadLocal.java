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

/**
 * @author Giulio Gavardi (giulio.gavardi@eng.it)
 */
public class DbTypeThreadLocal {

	private static final ThreadLocal<String> _dbType = new ThreadLocal<String>();

	public static void setDbType(String dbType) {
		_dbType.set(dbType);
	}

	public static String getDbType() {
		return _dbType.get();
	}

	public static void unset() {
		_dbType.remove();

	}

}
