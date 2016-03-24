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

import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;


public class BusinessRelationshipDescriptor {
	
	private String relationshipName;
	private BusinessColumnSet sourceTable, destinationTable;
	private java.util.List<BusinessColumn> sourceColumns, destinationColumns;
	private String relationCardinality;
	
	
	public BusinessRelationshipDescriptor(BusinessColumnSet source, BusinessColumnSet destination, java.util.List<BusinessColumn> sourceCol, java.util.List<BusinessColumn> destinationCol, String cardinality, String relationshipName){
		sourceTable = source;
		destinationTable = destination;
		sourceColumns = sourceCol;
		destinationColumns = destinationCol;
		relationCardinality = cardinality;
		this.relationshipName = relationshipName;
	}

	/**
	 * @param sourceTable the sourceTable to set
	 */
	public void setSourceTable(BusinessColumnSet sourceTable) {
		this.sourceTable = sourceTable;
	}

	/**
	 * @return the sourceTable
	 */
	public BusinessColumnSet getSourceTable() {
		return sourceTable;
	}

	/**
	 * @param destinationTable the destinationTable to set
	 */
	public void setDestinationTable(BusinessColumnSet destinationTable) {
		this.destinationTable = destinationTable;
	}

	/**
	 * @return the destinationTable
	 */
	public BusinessColumnSet getDestinationTable() {
		return destinationTable;
	}

	/**
	 * @param sourceColumns the sourceColumns to set
	 */
	public void setSourceColumns(java.util.List<BusinessColumn> sourceColumns) {
		this.sourceColumns = sourceColumns;
	}

	/**
	 * @return the sourceColumns
	 */
	public java.util.List<BusinessColumn> getSourceColumns() {
		return sourceColumns;
	}

	/**
	 * @param destinationColumns the destinationColumns to set
	 */
	public void setDestinationColumns(java.util.List<BusinessColumn> destinationColumns) {
		this.destinationColumns = destinationColumns;
	}

	/**
	 * @return the destinationColumns
	 */
	public java.util.List<BusinessColumn> getDestinationColumns() {
		return destinationColumns;
	}

	/**
	 * @param relationCardinality the relationCardinality to set
	 */
	public void setRelationCardinality(String relationCardinality) {
		this.relationCardinality = relationCardinality;
	}

	/**
	 * @return the relationCardinality
	 */
	public String getRelationCardinality() {
		return relationCardinality;
	}

	/**
	 * @param relationshipName the relationshipName to set
	 */
	public void setRelationshipName(String relationshipName) {
		this.relationshipName = relationshipName;
	}

	/**
	 * @return the relationshipName
	 */
	public String getRelationshipName() {
		return relationshipName;
	}
	
}