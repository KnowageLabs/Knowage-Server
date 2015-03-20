/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class CalculatedSelectField extends AbstractSelectField {
	
	private String expression;
	private String type;

	
	public CalculatedSelectField(String alias, String expression, String type, boolean included, boolean visible) {
		super(alias, ISelectField.CALCULATED_FIELD, included, visible);
		this.expression = expression;
		this.type = type;
	}
	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public ISelectField copy() {
		return null;
	}

	public boolean isCalculatedField() {
		return true;
	}

	public String getName() {
		return getAlias();
	}

	public void setName(String alias) {
		setAlias(alias);
	}

}
