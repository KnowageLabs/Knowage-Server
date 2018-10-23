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
import java.util.List;

import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.tools.dataset.metasql.query.item.InFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnaryFilter;
import it.eng.spagobi.utilities.database.IDataBase;

public class OrientDbSelectQueryVisitor extends AbstractSelectQueryVisitor {
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	public OrientDbSelectQueryVisitor(IDataBase database) {
		super(database);
	}

	@Override
	protected void append(InFilter item) {
		List<Projection> projections = item.getProjections();
		String openBracket = projections.size() > 1 ? "(" : "";
		String closeBracket = projections.size() > 1 ? ")" : "";

		queryBuilder.append(openBracket);

		append(projections.get(0), false);
		for (int i = 1; i < projections.size(); i++) {
			queryBuilder.append(",");
			append(projections.get(i), false);
		}

		queryBuilder.append(closeBracket);

		queryBuilder.append(" ");
		queryBuilder.append(item.getOperator());
		queryBuilder.append(" [");

		List<Object> operands = item.getOperands();
		for (int i = 0; i < operands.size(); i++) {
			if (i % projections.size() == 0) { // 1st item of tuple of values
				if (i >= projections.size()) { // starting from 2nd tuple of values
					queryBuilder.append(",");
				}
				queryBuilder.append(openBracket);
			}
			if (i % projections.size() != 0) {
				queryBuilder.append(",");
			}
			append(operands.get(i));
			if (i % projections.size() == projections.size() - 1) { // last item of tuple of values
				queryBuilder.append(closeBracket);
			}
		}

		queryBuilder.append("]");
	}

	@Override
	public void visit(UnaryFilter item) {
		if (Date.class.isAssignableFrom(item.getOperand().getClass())) {
			append(item.getProjection(), false);
			queryBuilder.append(".format('");
			queryBuilder.append(DATE_FORMAT);
			queryBuilder.append("') ");

		} else if (Timestamp.class.isAssignableFrom(item.getOperand().getClass())) {
			append(item.getProjection(), false);
			queryBuilder.append(".format('");
			queryBuilder.append(DATE_TIME_FORMAT);
			queryBuilder.append("') ");
		} else {
			append(item.getProjection(), false);
		}
		queryBuilder.append(" ");
		queryBuilder.append(item.getOperator());
		queryBuilder.append(" ");
		append(item.getOperand());
	}

	@Override
	public String getFormattedDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT);

		StringBuilder sb = new StringBuilder();
		sb.append("'");
		sb.append(dateFormat.format(date));
		sb.append("'");

		return sb.toString();
	}

	public String getFormattedTimestamp(String timestamp) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(CockpitJSONDataWriter.CACHE_TIMESTAMP_FORMAT);

		StringBuilder sb = new StringBuilder();
		sb.append("'");
		sb.append(dateFormat.format(timestamp));
		sb.append("'");

		return sb.toString();
	}

}
