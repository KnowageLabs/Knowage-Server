/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
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
