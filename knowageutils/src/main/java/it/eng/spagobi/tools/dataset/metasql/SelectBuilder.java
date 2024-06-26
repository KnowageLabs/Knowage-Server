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
package it.eng.spagobi.tools.dataset.metasql;

import java.util.ArrayList;
import java.util.List;

/**
 * @author John Krasnay - original code http://john.krasnay.ca/2010/02/15/building-sql-in-java.html Licensed under the Apache License, Version 2.0
 *         http://www.apache.org/licenses/
 *
 */

public class SelectBuilder {

	protected boolean distinctEnabled = false;

	protected boolean isWhereOrEnabled = false;

	protected List<String> columns = new ArrayList<String>();

	protected List<String> tables = new ArrayList<String>();

	protected List<String> joins = new ArrayList<String>();

	protected List<String> leftJoins = new ArrayList<String>();

	protected List<String> wheres = new ArrayList<String>();

	protected List<String> orderBys = new ArrayList<String>();

	protected List<String> groupBys = new ArrayList<String>();

	protected List<String> havings = new ArrayList<String>();

	protected int limit = -1;

	protected int offset = -1;

	public SelectBuilder() {

	}

	public SelectBuilder(String table) {
		tables.add(table);
	}

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

	public SelectBuilder column(String name) {
		columns.add(name);
		return this;
	}

	public SelectBuilder column(String name, boolean groupBy) {
		columns.add(name);
		if (groupBy) {
			groupBys.add(name);
		}
		return this;
	}

	public SelectBuilder from(String table) {
		tables.add(table);
		return this;
	}

	public SelectBuilder groupBy(String expr) {
		groupBys.add(expr);
		return this;
	}

	public SelectBuilder having(String expr) {
		havings.add(expr);
		return this;
	}

	public SelectBuilder join(String join) {
		joins.add(join);
		return this;
	}

	public SelectBuilder leftJoin(String join) {
		leftJoins.add(join);
		return this;
	}

	public SelectBuilder orderBy(String name) {
		orderBys.add(name);
		return this;
	}

	@Override
	public String toString() {

		StringBuilder sql = new StringBuilder("select ");

		if (isDistinctEnabled()) {
			sql.append(" distinct ");
		}

		if (columns.size() == 0) {
			sql.append("*");
		} else {
			appendList(sql, columns, "", ", ");
		}

		appendList(sql, tables, " from ", ", ");
		appendList(sql, joins, " join ", " join ");
		appendList(sql, leftJoins, " left join ", " left join ");
		if (isWhereOrEnabled) {
			appendList(sql, wheres, " where ", " or ");
		} else {
			appendList(sql, wheres, " where ", " and ");
		}
		appendList(sql, groupBys, " group by ", ", ");
		appendList(sql, havings, " having ", " and ");
		appendList(sql, orderBys, " order by ", ", ");

		if (limit > -1) {
			sql.append(" LIMIT ");
			sql.append(limit);
			if (offset > -1) {
				sql.append(" OFFSET ");
				sql.append(offset);
			}
		}

		return sql.toString();
	}

	public SelectBuilder where(String expr) {
		wheres.add(expr);
		return this;
	}

	public boolean isDistinctEnabled() {
		return distinctEnabled;
	}

	public void setDistinctEnabled(boolean distinctEnabled) {
		this.distinctEnabled = distinctEnabled;
	}

	public boolean isWhereOrEnabled() {
		return isWhereOrEnabled;
	}

	public void setWhereOrEnabled(boolean isWhereOrEnabled) {
		this.isWhereOrEnabled = isWhereOrEnabled;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
}
