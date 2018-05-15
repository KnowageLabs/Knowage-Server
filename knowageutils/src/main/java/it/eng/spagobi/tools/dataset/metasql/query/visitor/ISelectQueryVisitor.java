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
import java.util.Date;

import it.eng.spagobi.tools.dataset.metasql.query.PreparedStatementData;
import it.eng.spagobi.tools.dataset.metasql.query.SelectQuery;
import it.eng.spagobi.tools.dataset.metasql.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.BetweenFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.InFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.LikeFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.NullaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.OrFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnsatisfiedFilter;

public interface ISelectQueryVisitor {
	void visit(AndFilter item);

	void visit(BetweenFilter item);

	void visit(InFilter item);

	void visit(LikeFilter item);

	void visit(NullaryFilter item);

	void visit(OrFilter item);

	void visit(UnaryFilter item);

	void visit(UnsatisfiedFilter item);

	String getFormattedTimestamp(Timestamp timestamp);

	String getFormattedDate(Date date);

	String toSql(SelectQuery selectQuery);

	PreparedStatementData getPreparedStatementData(SelectQuery selectQuery);

}
