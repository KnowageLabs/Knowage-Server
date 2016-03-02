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
package it.eng.spagobi.tools.hierarchiesmanagement.metadata;

import java.util.ArrayList;

/**
 * @author Antonella Giachino (giachino.antonella@eng.it)
 *
 */
public class Dimension {

	String name;
	String label;
	String datasource;
	String hierarchyTable;
	String fkName;
	ArrayList<Field> metadataFields;
	ArrayList<Filter> metadataFilters;

	/**
	 * @param nodeCode
	 * @param nodeName
	 */
	public Dimension(String label) {
		this(label, "", "", "", "", new ArrayList<Field>(), new ArrayList<Filter>());
	}

	public Dimension(String label, String name, String datasource, String hierarchyTable, String fkName, ArrayList<Field> metadataFields,
			ArrayList<Filter> metadataFilters) {
		super();
		this.label = label;
		this.name = name;
		this.datasource = datasource;
		this.hierarchyTable = hierarchyTable;
		this.fkName = fkName;
		this.metadataFields = metadataFields;
		this.metadataFilters = metadataFilters;
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
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
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

	/**
	 * @return the metadataFilters
	 */
	public ArrayList<Filter> getMetadataFilters() {
		return this.metadataFilters;
	}

	/**
	 * @param metadataFilters
	 *            the metadataFilters to set
	 */
	public void setMetadataFilters(ArrayList<Filter> metadataFilters) {
		this.metadataFilters = metadataFilters;
	}
}
