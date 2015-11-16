/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
