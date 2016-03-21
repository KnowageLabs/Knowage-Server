/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/

/**
*  Utility Class used by the UI for creating BusinessRelationship
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