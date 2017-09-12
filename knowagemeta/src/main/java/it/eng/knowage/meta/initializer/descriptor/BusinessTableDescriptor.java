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
