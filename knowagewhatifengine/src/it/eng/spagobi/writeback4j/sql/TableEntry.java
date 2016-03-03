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