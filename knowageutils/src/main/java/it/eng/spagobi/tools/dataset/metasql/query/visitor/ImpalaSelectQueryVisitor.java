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

import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.utilities.database.IDataBase;

public class ImpalaSelectQueryVisitor extends AbstractSelectQueryVisitor {

	public ImpalaSelectQueryVisitor(IDataBase database) {
		super(database);
	}

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String TIMESTAMP_FORMAT = DATE_FORMAT + ".sss";

	@Override
	public String getFormattedTimestamp(Timestamp timestamp) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(CockpitJSONDataWriter.CACHE_TIMESTAMP_FORMAT);

		StringBuilder sb = new StringBuilder();

		sb.append("cast(");
		sb.append("unix_timestamp('");
		sb.append(dateFormat.format(timestamp));
		sb.append("','");
		sb.append(TIMESTAMP_FORMAT);
		sb.append("')");
		sb.append(" as timestamp)");

		return sb.toString();
	}

	@Override
	public String getFormattedDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT);

		StringBuilder sb = new StringBuilder();

		sb.append("cast(");
		sb.append("unix_timestamp('");
		sb.append(dateFormat.format(date));
		sb.append("','");
		sb.append(TIMESTAMP_FORMAT);
		sb.append("')");
		sb.append(" as timestamp)");

		return sb.toString();
	}

}
