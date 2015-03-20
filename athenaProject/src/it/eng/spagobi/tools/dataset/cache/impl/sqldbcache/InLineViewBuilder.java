/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
