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
package it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;


public class SbiObjParviewId  implements java.io.Serializable {

	// Fields    
	private SbiObjPar sbiObjPar;
	private SbiObjPar sbiObjParFather;
	private String operation;
	private String compareValue;

	// Constructors
	/**
	 * default constructor.
	 */
	public SbiObjParviewId() {
	}


	// Getter and Setter
	/**
	 * Gets the operation.
	 * 
	 * @return the filter operation
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * Sets the operation.
	 * 
	 * @param operation the new operation
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}

	/**
	 * Gets the sbi obj par.
	 * 
	 * @return the sbi obj par
	 */
	public SbiObjPar getSbiObjPar() {
		return sbiObjPar;
	}

	/**
	 * Sets the sbi obj par.
	 * 
	 * @param sbiObjPar the new sbi obj par
	 */
	public void setSbiObjPar(SbiObjPar sbiObjPar) {
		this.sbiObjPar = sbiObjPar;
	}

	/**
	 * Gets the sbi obj par father.
	 * 
	 * @return the sbi obj par father
	 */
	public SbiObjPar getSbiObjParFather() {
		return sbiObjParFather;
	}

	/**
	 * Sets the sbi obj par father.
	 * 
	 * @param sbiObjParFather the new sbi obj par father
	 */
	public void setSbiObjParFather(SbiObjPar sbiObjParFather) {
		this.sbiObjParFather = sbiObjParFather;
	}


	

	public String getCompareValue() {
		return compareValue;
	}


	public void setCompareValue(String compareValue) {
		this.compareValue = compareValue;
	}


	// hashcode generator
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int result = 17;
		result = 37 * result + this.getCompareValue().hashCode();
		result = 37 * result + this.getSbiObjPar().hashCode();
		result = 37 * result + this.getSbiObjParFather().hashCode();
		result = 37 * result + this.getOperation().hashCode();
		return result;
	}

	// override equals method
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if ( (this == other ) ) return true;
		if ( (other == null ) ) return false;
		if ( !(other instanceof SbiObjParuseId) ) return false;
		SbiObjParuseId castOther = ( SbiObjParuseId ) other; 
		return (this.getSbiObjPar()==castOther.getSbiObjPar()) || 
		(this.getSbiObjPar()!=null && castOther.getSbiObjPar()!=null && this.getSbiObjPar().equals(castOther.getSbiObjPar()) &&
				(this.getSbiObjParFather()==castOther.getSbiObjParFather()) || 
				(this.getSbiObjParFather()!=null && castOther.getSbiObjParFather()!=null && this.getSbiObjParFather().equals(castOther.getSbiObjParFather()) ) &&
				(this.getOperation()==castOther.getFilterOperation()) || 
				(this.getOperation()!=null && castOther.getFilterOperation()!=null && this.getOperation().equals(castOther.getFilterOperation()) ) );	
	}











}