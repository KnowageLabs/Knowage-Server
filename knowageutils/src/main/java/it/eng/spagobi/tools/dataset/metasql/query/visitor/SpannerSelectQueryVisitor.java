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
import java.time.Instant;
import java.util.Date;

import it.eng.spagobi.utilities.database.IDataBase;

public class SpannerSelectQueryVisitor extends AbstractSelectQueryVisitor {

	public SpannerSelectQueryVisitor(IDataBase database) {
		super(database);
	}

	@Override
	protected String getFormattedTimestamp(Timestamp timestamp) {
		StringBuilder sb = new StringBuilder();
		Instant instant = timestamp.toInstant();
		String timestampAsString = null;

		timestampAsString = instant.toString();

		sb.append("'").append(timestampAsString).append("'");

		return sb.toString();
	}

	@Override
	protected String getFormattedDate(Date date) {
		StringBuilder sb = new StringBuilder();
		Instant instant = date.toInstant();
		String dateAsString = null;

		dateAsString = instant.toString();

		sb.append("'").append(dateAsString).append("'");

		return sb.toString();
	}

}
