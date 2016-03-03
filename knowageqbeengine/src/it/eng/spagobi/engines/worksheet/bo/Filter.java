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
package it.eng.spagobi.engines.worksheet.bo;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class Filter extends Attribute {

	private boolean mandatory;
	private boolean multivalue;
	private boolean splittingFilter;
	
	/**
	 * @param entityId
	 * @param alias
	 * @param iconCls
	 * @param nature
	 * @param values
	 * @param mandatory
	 * @param multivalue
	 */
	public Filter(String entityId, String alias, String iconCls, String nature,	String values, boolean mandatory, boolean multivalue) {
		super(entityId, alias, iconCls, nature, values);
		this.mandatory = mandatory;
		this.multivalue = multivalue;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isMultivalue() {
		return multivalue;
	}

	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	public boolean isSplittingFilter() {
		return splittingFilter;
	}

	public void setSplittingFilter(boolean splittingFilter) {
		this.splittingFilter = splittingFilter;
	}
	
	
	
	

}
