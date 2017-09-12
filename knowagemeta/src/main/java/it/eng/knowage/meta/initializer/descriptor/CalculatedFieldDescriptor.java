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
package it.eng.knowage.meta.initializer.descriptor;

import it.eng.knowage.meta.model.business.BusinessColumnSet;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class CalculatedFieldDescriptor {
	
	private String name;
	private String expression;
	private String dataType;
	private BusinessColumnSet businessColumnSet;
	
	public CalculatedFieldDescriptor(String name, String expression, String dataType, BusinessColumnSet businessColumnSet){
		this.name = name;
		this.expression = expression;
		this.businessColumnSet = businessColumnSet;
		this.dataType = dataType;
	}
	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}
	/**
	 * @param expression the expression to set
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}


	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}


	/**
	 * @return the businessColumnSet
	 */
	public BusinessColumnSet getBusinessColumnSet() {
		return businessColumnSet;
	}
	/**
	 * @param businessColumnSet the businessColumnSet to set
	 */
	public void setBusinessColumnSet(BusinessColumnSet businessColumnSet) {
		this.businessColumnSet = businessColumnSet;
	}
	

}
