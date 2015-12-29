/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.writeback4j.sql;

import java.util.Map;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 * @class a table
 */
public class TableEntry {
	String column;
	String table;
	boolean isCubeDimension;

	public TableEntry(String column, String table) {
		super();
		this.column = column;
		this.table = table;
		isCubeDimension = false;
	}

	public TableEntry(String column, String table, boolean isCubeDimension) {
		super();
		this.column = column;
		this.table = table;
		this.isCubeDimension = isCubeDimension;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String toString(Map<String, String> table2Alias, AbstractSqlSchemaManager qb) {

		return qb.getTableAlias(table2Alias, table) + "." + column;

	}

	@Override
	public String toString() {

		if (table == null || column == null) {
			return "";
		} else {
			return table + "." + column;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + (isCubeDimension ? 1231 : 1237);
		result = prime * result + ((table == null) ? 0 : table.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TableEntry other = (TableEntry) obj;
		if (column == null) {
			if (other.column != null) {
				return false;
			}
		} else if (!column.equals(other.column)) {
			return false;
		}
		if (isCubeDimension != other.isCubeDimension) {
			return false;
		}
		if (table == null) {
			if (other.table != null) {
				return false;
			}
		} else if (!table.equals(other.table)) {
			return false;
		}
		return true;
	}

}