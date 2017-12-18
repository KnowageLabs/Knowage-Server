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
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class DataSetVariable {
	private String name;
	private String expression;
	private Object value;
	private String type;
	
	private int resetType;
	private Object initialValue;
	private int incrementType;
	
	static Map typeClassMap;
	
	public static final String HTML = "HTML";
	public static final String STRING = "STRING";
	public static final String NUMBER = "NUMBER";
	public static final String DATE = "DATE";
	static {
		typeClassMap = new HashMap();
		typeClassMap.put(HTML, String.class);
		typeClassMap.put(STRING, String.class);
		typeClassMap.put(NUMBER, Double.class);
		typeClassMap.put(DATE, String.class);
		
	}
	
	public static final int RESET_TYPE_RECORD = 1;
	public static final int RESET_TYPE_DATASET = 2;
	
	public static final int INCREMENT_TYPE_SUM = 1;
	public static final int INCREMENT_TYPE_MAX = 2;
	public static final int INCREMENT_TYPE_MIN = 3;
	
	public DataSetVariable(String name, String type, String expression) {
		setName(name);
		setExpression(expression);
		setResetType(RESET_TYPE_RECORD);
		setInitialValue(null);
		setType(type);
	}
	
	public DataSetVariable(String name, String type, String expression, int resetType, Object initialValue) {
		setName(name);
		setExpression(expression);
		setResetType(resetType);
		setInitialValue(initialValue);
		setType(type);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		Assert.assertTrue(typeClassMap.containsKey(type), "Unsupportd type [" + type + "] for variable [" + name + "]");
		this.type = type;
	}

	public Class getTypeClass() {
		return (Class)typeClassMap.get( getType() );
	}

	public int getResetType() {
		return resetType;
	}

	public void reset() {
		setValue( getInitialValue() );
	}
	
	public void setResetType(int resetType) {
		this.resetType = resetType;
	}

	public Object getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(Object initialValue) {
		this.initialValue = initialValue;
	}

	public int getIncrementType() {
		return incrementType;
	}

	public void setIncrementType(int incrementType) {
		this.incrementType = incrementType;
	}
}
