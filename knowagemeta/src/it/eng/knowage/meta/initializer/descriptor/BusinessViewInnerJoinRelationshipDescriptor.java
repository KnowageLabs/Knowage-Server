/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.initializer.descriptor;


import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalTable;

import java.util.ArrayList;

/**
 * @author cortella
 *
 */
public class BusinessViewInnerJoinRelationshipDescriptor {
	private String relationshipName;
	private PhysicalTable sourceTable, destinationTable;
	private java.util.List<PhysicalColumn> sourceColumns, destinationColumns;
	private int relationCardinality;
	
	
	public BusinessViewInnerJoinRelationshipDescriptor(PhysicalTable source, PhysicalTable destination, java.util.List<PhysicalColumn> sourceCol, java.util.List<PhysicalColumn> destinationCol, int cardinality, String relationshipName){
		sourceTable = source;
		destinationTable = destination;
		sourceColumns = new ArrayList<PhysicalColumn>();
		sourceColumns.addAll(sourceCol);
		destinationColumns = new ArrayList<PhysicalColumn>();
		destinationColumns.addAll(destinationCol);
		relationCardinality = cardinality;
		this.relationshipName = relationshipName;
	}

	/**
	 * @param sourceTable the sourceTable to set
	 */
	public void setSourceTable(PhysicalTable sourceTable) {
		this.sourceTable = sourceTable;
	}

	/**
	 * @return the sourceTable
	 */
	public PhysicalTable getSourceTable() {
		return sourceTable;
	}

	/**
	 * @param destinationTable the destinationTable to set
	 */
	public void setDestinationTable(PhysicalTable destinationTable) {
		this.destinationTable = destinationTable;
	}

	/**
	 * @return the destinationTable
	 */
	public PhysicalTable getDestinationTable() {
		return destinationTable;
	}

	/**
	 * @param sourceColumns the sourceColumns to set
	 */
	public void setSourceColumns(java.util.List<PhysicalColumn> sourceColumns) {
		this.sourceColumns = sourceColumns;
	}

	/**
	 * @return the sourceColumns
	 */
	public java.util.List<PhysicalColumn> getSourceColumns() {
		return sourceColumns;
	}

	/**
	 * @param destinationColumns the destinationColumns to set
	 */
	public void setDestinationColumns(java.util.List<PhysicalColumn> destinationColumns) {
		this.destinationColumns = destinationColumns;
	}

	/**
	 * @return the destinationColumns
	 */
	public java.util.List<PhysicalColumn> getDestinationColumns() {
		return destinationColumns;
	}

	/**
	 * @param relationCardinality the relationCardinality to set
	 */
	public void setRelationCardinality(int relationCardinality) {
		this.relationCardinality = relationCardinality;
	}

	/**
	 * @return the relationCardinality
	 */
	public int getRelationCardinality() {
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
