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
package it.eng.spagobi.metadata.etl;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML Parser for Talend .item files that extract metadata informations
 *
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class ETLParser {
	private Document document;

	private String[] rdbmsDataSourceComponents = { "tDB2Connection", "tOracleConnection" };

	private String[] rdbmsSourceComponents = { "tELTOracleInput", "tOracleInput", };
	private String[] rdbmsTargetComponents = { "tELTOracleOutput", "tOracleOutput", "tOracleOutputBulk", "tOracleOutputBulkExec", "tOracleSCD", "tOracleSCDELT" };

	private String[] rdbmsMixedComponents = { "tOracleRow" };

	private String[] fileSourceComponents = { "tFileInputDelimited", "tFileInputExcel" };
	private String[] fileTargetComponents = { "tFileOutputDelimited", "tFileOutputExcel" };

	public ETLParser(Document document) {
		this.document = document;
	}

	public void extractAll() {
		Set<String> dataSources = getRDBMSDataSources();
		Set<String> rdbmsSourceTables = getRDBMSSourceComponentsTables();
		Set<String> rdbmsTargetTables = getRDBMSTargetComponentsTables();
		Set<String> fileSourceTables = getFileSourceComponentsLocations();
		Set<String> fileTargetTables = getFileTargetComponentsLocations();
		Set<String> rdbmsMixedSourceTables = getRDBMSSourceMixedComponentsTables();
		Set<String> rdbmsMixedTargetTables = getRDBMSTargetMixedComponentsTables();

		print("Data Sources", dataSources);
		print("Source Tables", rdbmsSourceTables);
		print("Target Tables", rdbmsTargetTables);
		print("Generic Component Source Tables", rdbmsMixedSourceTables);
		print("Generic Component Target Tables", rdbmsMixedTargetTables);
		print("Input Files", fileSourceTables);
		print("Output Files", fileTargetTables);

	}

	/**
	 * Get all tables of Source Components of type RDBMS
	 */
	public Set<String> getRDBMSDataSources() {
		Set<String> dataSources = new HashSet<String>();
		for (int i = 0; i < rdbmsDataSourceComponents.length; i++) {
			dataSources.addAll(getRDBMSComponentDataSources(rdbmsDataSourceComponents[i]));
		}
		return dataSources;
	}

	/**
	 * Get all tables of Source Components of type RDBMS
	 */
	public Set<String> getRDBMSSourceComponentsTables() {
		Set<String> tables = new HashSet<String>();
		for (int i = 0; i < rdbmsSourceComponents.length; i++) {
			tables.addAll(getRDBMSComponentTables(rdbmsSourceComponents[i]));
		}
		return tables;
	}

	/**
	 * Get all tables of Target Components of type RDBMS
	 */
	public Set<String> getRDBMSTargetComponentsTables() {
		Set<String> tables = new HashSet<String>();
		for (int i = 0; i < rdbmsTargetComponents.length; i++) {
			tables.addAll(getRDBMSComponentTables(rdbmsTargetComponents[i]));
		}
		return tables;
	}

	/**
	 * Get all SOURCE tables of Mixed Components of type RDBMS
	 */
	public Set<String> getRDBMSTargetMixedComponentsTables() {
		Set<String> tables = new HashSet<String>();
		for (int i = 0; i < rdbmsMixedComponents.length; i++) {
			tables.addAll(getMixedComponentInformations(rdbmsMixedComponents[i], "dbtable", "table", "target"));
		}
		return tables;
	}

	/**
	 * Get all TARGET tables of Mixed Components of type RDBMS
	 */
	public Set<String> getRDBMSSourceMixedComponentsTables() {
		Set<String> tables = new HashSet<String>();
		for (int i = 0; i < rdbmsMixedComponents.length; i++) {
			tables.addAll(getMixedComponentInformations(rdbmsMixedComponents[i], "dbtable", "table", "source"));
		}
		return tables;
	}

	/**
	 * Get all locations of Source Components of type File
	 */
	public Set<String> getFileSourceComponentsLocations() {
		Set<String> locations = new HashSet<String>();
		for (int i = 0; i < fileSourceComponents.length; i++) {
			locations.addAll(getFileComponentLocations(fileSourceComponents[i]));
		}
		return locations;
	}

	/**
	 * Get all locations of Target Components of type File
	 */
	public Set<String> getFileTargetComponentsLocations() {
		Set<String> locations = new HashSet<String>();
		for (int i = 0; i < fileTargetComponents.length; i++) {
			locations.addAll(getFileComponentLocations(fileTargetComponents[i]));
		}
		return locations;
	}

	/**
	 * Get all the names of connections used by a specific RDBMS component type
	 */
	public Set<String> getRDBMSComponentDataSources(String componentType) {
		return getComponentInformations(componentType, "text", "label");
	}

	/**
	 * Get all the tables used by a specific RDBMS component type
	 */
	public Set<String> getRDBMSComponentTables(String componentType) {
		return getComponentInformations(componentType, "dbtable", "table");
	}

	/**
	 * Get all the tables used by a specific FILE component type
	 */
	public Set<String> getFileComponentLocations(String componentType) {
		return getComponentInformations(componentType, "file", "filename");
	}

	/**
	 * Get the values used by a specific component type for a specific field and name
	 */
	public Set<String> getComponentInformations(String componentTypeName, String fieldValue, String nameValue) {

		Set<String> informations = new HashSet<String>();
		NodeList nList = document.getElementsByTagName("node");

		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element nodeElement = (Element) node;
				String currentNodeComponentName = nodeElement.getAttribute("componentName");
				if (currentNodeComponentName.equals(componentTypeName)) {
					// found searched component
					NodeList elementParameters = nodeElement.getElementsByTagName("elementParameter");
					for (int j = 0; j < elementParameters.getLength(); j++) {
						Node elementParameterNode = elementParameters.item(j);
						if (elementParameterNode.getNodeType() == Node.ELEMENT_NODE) {
							Element elementParameterNodeElement = (Element) elementParameterNode;
							String elementParameterField = elementParameterNodeElement.getAttribute("field");
							String elementParameterName = elementParameterNodeElement.getAttribute("name");
							if (elementParameterField.equalsIgnoreCase(fieldValue) && elementParameterName.equalsIgnoreCase(nameValue)) {
								// this elementParameter tag contains the name of a table/file
								String value = elementParameterNodeElement.getAttribute("value").replaceAll("\"", "");
								if (!value.isEmpty()) {
									informations.add(value);
								}
							}

						}

					}
				}
			}
		}

		return informations;

	}

	/**
	 * Get the values used by a specific Mixed component type for a specific field,name and role (target or source)
	 */
	public Set<String> getMixedComponentInformations(String componentTypeName, String fieldValue, String nameValue, String role) {

		Set<String> informations = new HashSet<String>();
		NodeList nList = document.getElementsByTagName("node");

		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element nodeElement = (Element) node;
				String currentNodeComponentName = nodeElement.getAttribute("componentName");
				if (currentNodeComponentName.equals(componentTypeName)) {
					// found searched component
					NodeList elementParameters = nodeElement.getElementsByTagName("elementParameter");
					String information = null;
					for (int j = 0; j < elementParameters.getLength(); j++) {
						Node elementParameterNode = elementParameters.item(j);
						if (elementParameterNode.getNodeType() == Node.ELEMENT_NODE) {
							Element elementParameterNodeElement = (Element) elementParameterNode;
							String elementParameterField = elementParameterNodeElement.getAttribute("field");
							String elementParameterName = elementParameterNodeElement.getAttribute("name");
							if (elementParameterField.equalsIgnoreCase(fieldValue) && elementParameterName.equalsIgnoreCase(nameValue)) {
								// this elementParameter tag contains the name of a table
								// temporary save the information
								information = elementParameterNodeElement.getAttribute("value").replaceAll("\"", "");

							} else if (elementParameterField.equalsIgnoreCase("MEMO_SQL") && elementParameterName.equalsIgnoreCase("QUERY")) {

								// check the content of the query field
								String queryText = elementParameterNodeElement.getAttribute("value");

								// check if query text contains INSERT, UPDATE or DELETE
								if (queryText.toLowerCase().contains("insert") || queryText.toLowerCase().contains("update")
										|| queryText.toLowerCase().contains("delete")) {
									// It's a TARGET
									if (role.equalsIgnoreCase("target")) {
										if (!information.isEmpty()) {
											informations.add(information);
										}
									}

								} else if (queryText.toLowerCase().contains("truncate") || queryText.toLowerCase().contains("analyse")) {
									// Cannot determine
									// do nothing

								} else if (queryText.toLowerCase().contains("select")) {
									// if only contains SELECT, it' a SOURCE
									if (role.equalsIgnoreCase("source")) {
										if (!information.isEmpty()) {
											informations.add(information);
										}
									}
								}
							}

						}

					}
				}
			}
		}

		return informations;

	}

	// -----------------------------------------------------------------
	// Utility Methods
	// -----------------------------------------------------------------

	/**
	 * Print contents of a String Set to console
	 */
	public void print(String label, Set<String> contents) {
		System.out.println(label + ":");
		System.out.println("-------------------------");
		for (String content : contents) {
			System.out.println(content);
		}
		System.out.println("");

	}

}
