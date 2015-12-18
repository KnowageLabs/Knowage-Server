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
public class Hierarchy {

	String name;
	String datasource;
	String dimensionTable;
	String fkName;
	String nodeStructure;
	String leafStructure;
	int numLevels;
	boolean allowDuplicate;

	ArrayList<Field> metadataGeneralFields;
	ArrayList<Field> metadataNodeFields;
	ArrayList<Field> metadataLeafFields;

	/**
	 * @param name
	 */
	public Hierarchy(String name) {
		this(name, "", "", "", "", "", 0, false, new ArrayList<Field>(), new ArrayList<Field>(), new ArrayList<Field>());
	}

	public Hierarchy(String name, String datasource, String dimensionTable, String fkName, String nodeStructure, String leafStructure, int numLevels,
			boolean allowDuplicate, ArrayList<Field> metadataGeneralFields, ArrayList<Field> metadataNodeFields, ArrayList<Field> metadataLeafFields) {
		super();
		this.name = name;
		this.datasource = datasource;
		this.dimensionTable = dimensionTable;
		this.fkName = fkName;
		this.nodeStructure = nodeStructure;
		this.leafStructure = leafStructure;
		this.numLevels = numLevels;
		this.allowDuplicate = allowDuplicate;
		this.metadataGeneralFields = metadataGeneralFields;
		this.metadataNodeFields = metadataNodeFields;
		this.metadataLeafFields = metadataLeafFields;
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
	 * @return the dimensionTable
	 */
	public String getDimensionTable() {
		return dimensionTable;
	}

	/**
	 * @param dimensionTable
	 *            the dimensionTable to set
	 */
	public void setDimensionTable(String dimensionTable) {
		this.dimensionTable = dimensionTable;
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
	 * @return the nodeStructure
	 */
	public String getNodeStructure() {
		return nodeStructure;
	}

	/**
	 * @param nodeStructure
	 *            the nodeStructure to set
	 */
	public void setNodeStructure(String nodeStructure) {
		this.nodeStructure = nodeStructure;
	}

	/**
	 * @return the leafStructure
	 */
	public String getLeafStructure() {
		return leafStructure;
	}

	/**
	 * @param leafStructure
	 *            the leafStructure to set
	 */
	public void setLeafStructure(String leafStructure) {
		this.leafStructure = leafStructure;
	}

	/**
	 * @return the numLevels
	 */
	public int getNumLevels() {
		return numLevels;
	}

	/**
	 * @param numLevels
	 *            the numLevels to set
	 */
	public void setNumLevels(int numLevels) {
		this.numLevels = numLevels;
	}

	/**
	 * @return the allowDuplicate
	 */
	public boolean isAllowDuplicate() {
		return allowDuplicate;
	}

	/**
	 * @param allowDuplicate
	 *            the allowDuplicate to set
	 */
	public void setAllowDuplicate(boolean allowDuplicate) {
		this.allowDuplicate = allowDuplicate;
	}

	/**
	 * @return the metadataGeneralFields
	 */
	public ArrayList<Field> getMetadataGeneralFields() {
		return metadataGeneralFields;
	}

	/**
	 * @param metadataGeneralFields
	 *            the metadataGeneralFields to set
	 */
	public void setMetadataGeneralFields(ArrayList<Field> metadataGeneralFields) {
		this.metadataGeneralFields = metadataGeneralFields;
	}

	/**
	 * @return the metadataNodeFields
	 */
	public ArrayList<Field> getMetadataNodeFields() {
		return metadataNodeFields;
	}

	/**
	 * @param metadataNodeFields
	 *            the metadataNodeFields to set
	 */
	public void setMetadataNodeFields(ArrayList<Field> metadataNodeFields) {
		this.metadataNodeFields = metadataNodeFields;
	}

	/**
	 * @return the metadataLeafFields
	 */
	public ArrayList<Field> getMetadataLeafFields() {
		return metadataLeafFields;
	}

	/**
	 * @param metadataLeafFields
	 *            the metadataLeafFields to set
	 */
	public void setMetadataLeafFields(ArrayList<Field> metadataLeafFields) {
		this.metadataLeafFields = metadataLeafFields;
	}

}
