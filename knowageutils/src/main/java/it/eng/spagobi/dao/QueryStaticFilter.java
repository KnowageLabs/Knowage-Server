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
package it.eng.spagobi.dao;

/**
 * 
 * @author zerbetto
 *
 */
public class QueryStaticFilter extends QueryFilter {
	
	private final String field;
	private final Object value;
	private boolean ignoreCase;
	private final String operator; // see it.eng.qbe.query.CriteriaConstants for admissible values

	
	public QueryStaticFilter(String field, Object value, String operator) {
		this.field = field;
		this.value = value;
		this.operator = operator;
	}

	public QueryStaticFilter(String field, Object value, String operator, boolean ignoreCase) {
		this.field = field;
		this.value = value;
		this.operator = operator;
		this.ignoreCase = ignoreCase;
	}

	public String getField() {
		return field;
	}

	public Object getValue() {
		return value;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public String getOperator() {
		return operator;
	}
	
	@Override
	public String toString() {
		return "QueryStaticFilter [field=" + field + ", value=" + value
				+ ", ignoreCase=" + ignoreCase + ", operator=" + operator + "]";
	}
	
}
