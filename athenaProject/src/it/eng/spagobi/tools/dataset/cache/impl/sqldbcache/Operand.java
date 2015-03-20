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

import java.util.List;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class Operand {
	
	private String operandDataSet;
	private Object operandValue;
	private boolean isCostant; //operand type, if true is a constant value
	
	
	public Operand(Object operandValue) {
		this.operandValue = operandValue;
		this.isCostant = true;
		this.operandDataSet = null;
	}
	
	public Operand(String operandDataSet,  String operandValue) {
		this.operandValue = operandValue;
		this.isCostant = false;
		this.operandDataSet = operandDataSet;
	}

	
	public Object getOperandValue() {
		return operandValue;
	}
	
	public String getOperandValueAsString() {
		return (String)operandValue;
	}
	
	public List<String> getOperandValueAsList() {
		return (List<String>)operandValue;
	}

	
	public void setOperandValue(Object operandValue) {
		this.operandValue = operandValue;
	}

	/**
	 * @return the isCostant
	 */
	public boolean isCostant() {
		return isCostant;
	}

	/**
	 * @param isCostant the isCostant to set
	 */
	public void setCostant(boolean isCostant) {
		this.isCostant = isCostant;
	}
	
	public String getOperandDataSet() {
		return operandDataSet;
	}

	public void setOperandDataSet(String operandDataSet) {
		this.operandDataSet = operandDataSet;
	}

	/**
	 * @return
	 */
	public boolean isMultivalue() {
		// TODO Auto-generated method stub
		return (operandValue instanceof List);
	}
	
	
}
