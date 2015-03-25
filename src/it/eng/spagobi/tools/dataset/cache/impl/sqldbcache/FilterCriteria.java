/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.dataset.cache.impl.sqldbcache;


/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class FilterCriteria {

	//Filter for the WHERE clause
	
	Operand leftOperand;
	String operator;
	Operand rightOperand;
	
	/**
	 * @param leftOperand
	 * @param operator
	 * @param rightOperand
	 */
	public FilterCriteria(Operand leftOperand, String operator,
			Operand rightOperand) {
		this.leftOperand = leftOperand;
		this.operator = operator;
		this.rightOperand = rightOperand;
	}

	/**
	 * @return the leftOperand
	 */
	public Operand getLeftOperand() {
		return leftOperand;
	}

	/**
	 * @param leftOperand the leftOperand to set
	 */
	public void setLeftOperand(Operand leftOperand) {
		this.leftOperand = leftOperand;
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * @return the rightOperand
	 */
	public Operand getRightOperand() {
		return rightOperand;
	}

	/**
	 * @param rightOperand the rightOperand to set
	 */
	public void setRightOperand(Operand rightOperand) {
		this.rightOperand = rightOperand;
	}
	
	
	
	
}
