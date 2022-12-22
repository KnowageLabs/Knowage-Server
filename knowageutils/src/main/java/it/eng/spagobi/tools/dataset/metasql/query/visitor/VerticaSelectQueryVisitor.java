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

import java.text.SimpleDateFormat;
import java.util.Date;

import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.utilities.database.IDataBase;

public class VerticaSelectQueryVisitor extends AbstractSelectQueryVisitor {

	private static final String DATE_FORMAT = CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT.replace("yyyy", "YYYY").replace("dd", "DD").replace("mm", "MI")
			.replace("ss", "SS");

	public VerticaSelectQueryVisitor(IDataBase database) {
		super(database);
	}

	@Override
	protected void append(Object operand) {
		if (operand == null) {
			queryBuilder.append("NULL");
		} else {
			if (buildPreparedStatement) {
				queryBuilder.append("?");
				queryParameters.add(operand);
			} else {
				if (operand.getClass().toString().toLowerCase().contains("timestamp")) {
					queryBuilder.append(getFormattedTimestamp(operand.toString()));
				} else if (Date.class.isAssignableFrom(operand.getClass())) {
					queryBuilder.append(getFormattedDate((Date) operand));
				} else if (String.class.isAssignableFrom(operand.getClass())) {
					queryBuilder.append("'");
					queryBuilder.append(((String) operand).replaceAll("'", "''"));
					queryBuilder.append("'");
				} else {
					queryBuilder.append(operand);
				}
			}
		}
	}

	public String getFormattedTimestamp(String timestamp) {
		StringBuilder sb = new StringBuilder();

		sb.append("TO_DATE('");
		sb.append(timestamp);
		sb.append("','");
		sb.append(DATE_FORMAT);
		sb.append("')");

		return sb.toString();
	}

	@Override
	public String getFormattedDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT);

		StringBuilder sb = new StringBuilder();
		sb.append("TO_DATE('");
		sb.append(dateFormat.format(date));
		sb.append("','");
		sb.append(DATE_FORMAT);
		sb.append("')");

		return sb.toString();
	}

}
