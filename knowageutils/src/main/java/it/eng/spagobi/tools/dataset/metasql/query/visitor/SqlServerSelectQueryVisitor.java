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

package it.eng.spagobi.tools.dataset.metasql.query.visitor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.eng.spagobi.utilities.database.IDataBase;

public class SqlServerSelectQueryVisitor extends AbstractSelectQueryVisitor {

	private static final String DATE_TIME_FORMAT_SQLSERVER = "yyyyMMdd HH:mm:ss";
	private static final String TIMESTAMP_FORMAT_SQLSERVER = DATE_TIME_FORMAT_SQLSERVER + ".SSS";

	public SqlServerSelectQueryVisitor(IDataBase database) {
		super(database);
	}

	@Override
	public String getFormattedTimestamp(Timestamp timestamp) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT_SQLSERVER);

		StringBuilder sb = new StringBuilder();
		sb.append("'");
		sb.append(dateFormat.format(timestamp));
		sb.append("'");

		return sb.toString();
	}

	@Override
	public String getFormattedDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT_SQLSERVER);

		StringBuilder sb = new StringBuilder();
		sb.append("'");
		sb.append(dateFormat.format(date));
		sb.append("'");

		return sb.toString();
	}

}
