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
import it.eng.knowage.meta.model.physical.PhysicalTable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cortella
 * 
 */
public class BusinessViewInnerJoinRelationshipDescriptor {
	private String relationshipName;
	private PhysicalTable sourceTable, destinationTable;
	private List<PhysicalColumn> sourceColumns = new ArrayList<>();
	private List<PhysicalColumn> destinationColumns = new ArrayList<>();
	private int relationCardinality;

	public BusinessViewInnerJoinRelationshipDescriptor(PhysicalTable source, PhysicalTable destination) {
		sourceTable = source;
		destinationTable = destination;
	}

	public BusinessViewInnerJoinRelationshipDescriptor(PhysicalTable source, PhysicalTable destination, java.util.List<PhysicalColumn> sourceCol,
			java.util.List<PhysicalColumn> destinationCol, int cardinality, String relationshipName) {
		sourceTable = source;
		destinationTable = destination;
		sourceColumns = new ArrayList<PhysicalColumn>();
		sourceColumns.addAll(sourceCol);
		destinationColumns = new ArrayList<PhysicalColumn>();
		destinationColumns.addAll(destinationCol);
		relationCardinality = cardinality;
		this.relationshipName = relationshipName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destinationTable == null) ? 0 : destinationTable.hashCode());
		result = prime * result + ((sourceTable == null) ? 0 : sourceTable.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BusinessViewInnerJoinRelationshipDescriptor other = (BusinessViewInnerJoinRelationshipDescriptor) obj;
		if (destinationTable == null) {
			if (other.destinationTable != null)
				return false;
		} else if (!destinationTable.equals(other.destinationTable))
			return false;
		if (sourceTable == null) {
			if (other.sourceTable != null)
				return false;
		} else if (!sourceTable.equals(other.sourceTable))
			return false;
		return true;
	}

	/**
	 * @param sourceTable
	 *            the sourceTable to set
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
	 * @param destinationTable
	 *            the destinationTable to set
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
	 * @param sourceColumns
	 *            the sourceColumns to set
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
	 * @param destinationColumns
	 *            the destinationColumns to set
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
	 * @param relationCardinality
	 *            the relationCardinality to set
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
	 * @param relationshipName
	 *            the relationshipName to set
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
