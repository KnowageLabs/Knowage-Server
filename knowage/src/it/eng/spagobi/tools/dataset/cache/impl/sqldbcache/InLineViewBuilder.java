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
package it.eng.spagobi.tools.dataset.cache.impl.sqldbcache;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.List;

public class InLineViewBuilder extends SelectBuilder {

	private String inLineViewSQL;

	private String inLineViewAlias;

	public InLineViewBuilder(String inLineView, String inLineViewAlias) {
		this.inLineViewSQL = inLineView;
		this.inLineViewAlias = inLineViewAlias;
	}

	@Override
	public SelectBuilder from(String inLineView) {
		if (this.inLineViewSQL != null) {
			throw new SpagoBIRuntimeException("InLineViewBuilder can query only one in-line view");
		}
		this.inLineViewSQL = inLineView;
		return this;
	}

	@Override
	public SelectBuilder join(String join) {
		throw new SpagoBIRuntimeException("InLineViewBuilder does not support joins");
	}

	@Override
	public SelectBuilder leftJoin(String join) {
		throw new SpagoBIRuntimeException("InLineViewBuilder does not support joins");
	}

	@Override
	public String toString() {

		StringBuilder sql = new StringBuilder("select ");

		if (columns.size() == 0) {
			sql.append(inLineViewAlias + ".*");
		} else {
			appendList(sql, columns, "", ", ");
		}

		sql.append(" from (" + inLineViewSQL + ") " + inLineViewAlias + " ");
		appendList(sql, wheres, " where ", " and ");
		appendList(sql, groupBys, " group by ", ", ");
		appendList(sql, havings, " having ", " and ");
		appendList(sql, orderBys, " order by ", ", ");

		return sql.toString();
	}

	@Override
	protected void appendList(StringBuilder sql, List<String> list, String init, String sep) {
		boolean first = true;
		for (String s : list) {
			if (first) {
				sql.append(init);
			} else {
				sql.append(sep);
			}
			sql.append(s);
			first = false;
		}
	}
}
