/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
