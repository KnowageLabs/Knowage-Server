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
package it.eng.spagobi.tools.hierarchiesmanagement.metadata;

import java.util.ArrayList;

/**
 * @author Antonella Giachino (giachino.antonella@eng.it)
 *
 */
public class Dimension {

	String name;
	String datasource;
	String hierarchyTable;
	String fkName;
	ArrayList<Field> metadataFields;

	/**
	 * @param nodeCode
	 * @param nodeName
	 */
	public Dimension(String name) {
		this(name, "", "", "", new ArrayList<Field>());
	}

	public Dimension(String name, String datasource, String hierarchyTable, String fkName, ArrayList<Field> metadataFields) {
		super();
		this.name = name;
		this.datasource = datasource;
		this.hierarchyTable = hierarchyTable;
		this.fkName = fkName;
		this.metadataFields = metadataFields;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the datasource
	 */
	public String getDatasource() {
		return datasource;
	}

	/**
	 * @param datasource
	 *            the datasource to set
	 */
	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	/**
	 * @return the hierarchyTabel
	 */
	public String getHierarchyTable() {
		return hierarchyTable;
	}

	/**
	 * @param hierarchyTabel
	 *            the hierarchyTabel to set
	 */
	public void setHierarchyTable(String hierarchyTable) {
		this.hierarchyTable = hierarchyTable;
	}

	/**
	 * @return the fkName
	 */
	public String getFkName() {
		return fkName;
	}

	/**
	 * @param fkName
	 *            the fkName to set
	 */
	public void setFkName(String fkName) {
		this.fkName = fkName;
	}

	/**
	 * @return the metadataFields
	 */
	public ArrayList<Field> getMetadataFields() {
		return metadataFields;
	}

	/**
	 * @param metadataFields
	 *            the metadataFields to set
	 */
	public void setMetadataFields(ArrayList<Field> metadataFields) {
		this.metadataFields = metadataFields;
	}

}
