/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.initializer.descriptor;

import it.eng.knowage.meta.model.physical.PhysicalColumn;

import java.util.List;

/**
 * @author cortella
 *
 */
public class BusinessTableDescriptor {
	private String businessTableName;
	private String businessTableDescription;
	private List<PhysicalColumn> physicalColumns;
	
	public BusinessTableDescriptor(){
		businessTableName = null;
		businessTableDescription = null;
		physicalColumns = null;
	}

	/**
	 * @param businessTableName the businessTableName to set
	 */
	public void setBusinessTableName(String businessTableName) {
		this.businessTableName = businessTableName;
	}

	/**
	 * @return the businessTableName
	 */
	public String getBusinessTableName() {
		return businessTableName;
	}

	/**
	 * @param businessTableDescription the businessTableDescription to set
	 */
	public void setBusinessTableDescription(String businessTableDescription) {
		this.businessTableDescription = businessTableDescription;
	}

	/**
	 * @return the businessTableDescription
	 */
	public String getBusinessTableDescription() {
		return businessTableDescription;
	}

	/**
	 * @param physicalColumns the physicalColumns to set
	 */
	public void setPhysicalColumns(List<PhysicalColumn> physicalColumns) {
		this.physicalColumns = physicalColumns;
	}

	/**
	 * @return the physicalColumns
	 */
	public List<PhysicalColumn> getPhysicalColumns() {
		return physicalColumns;
	}
}
