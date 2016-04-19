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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
		Set<String> contextNames = getContextNames();
		// TODO: The context name is not a fixed value and must be selected by the user
		Set<ETLRDBMSSource> rdbmsDataSources = getRDBMSDataSourcesObjects("Default");
		Set<ETLComponent> rdbmsSourceTables = getRDBMSSourceComponentsTables();
		Set<ETLComponent> rdbmsTargetTables = getRDBMSTargetComponentsTables();
		Set<String> fileSourceTables = getFileSourceComponentsLocations();
		Set<String> fileTargetTables = getFileTargetComponentsLocations();
		Set<ETLComponent> rdbmsMixedSourceTables = getRDBMSSourceMixedComponentsTables();
		Set<ETLComponent> rdbmsMixedTargetTables = getRDBMSTargetMixedComponentsTables();

		print("Context Names", contextNames);
		printETLRDBMSSource("Data Sources", rdbmsDataSources);
		printETLComponent("Source Tables", rdbmsSourceTables);
		printETLComponent("Target Tables", rdbmsTargetTables);
		printETLComponent("Generic Component Source Tables", rdbmsMixedSourceTables);
		printETLComponent("Generic Component Target Tables", rdbmsMixedTargetTables);
		print("Input Files", fileSourceTables);
		print("Output Files", fileTargetTables);

	}

	/**
	 * Get all tables of Source Components of type RDBMS
	 */
	public Set<ETLComponent> getRDBMSSourceComponentsTables() {
		Set<ETLComponent> tables = new HashSet<ETLComponent>();
		for (int i = 0; i < rdbmsSourceComponents.length; i++) {
			tables.addAll(getRDBMSComponentTables(rdbmsSourceComponents[i]));
		}
		return tables;
	}

	/**
	 * Get all tables of Target Components of type RDBMS
	 */
	public Set<ETLComponent> getRDBMSTargetComponentsTables() {
		Set<ETLComponent> tables = new HashSet<ETLComponent>();
		for (int i = 0; i < rdbmsTargetComponents.length; i++) {
			tables.addAll(getRDBMSComponentTables(rdbmsTargetComponents[i]));
		}
		return tables;
	}

	/**
	 * Get all SOURCE tables of Mixed Components of type RDBMS
	 */
	public Set<ETLComponent> getRDBMSTargetMixedComponentsTables() {
		Set<ETLComponent> tables = new HashSet<ETLComponent>();
		for (int i = 0; i < rdbmsMixedComponents.length; i++) {
			tables.addAll(getMixedComponentInformations(rdbmsMixedComponents[i], "DBTABLE", "TABLE", "TARGET"));
		}
		return tables;
	}

	/**
	 * Get all TARGET tables of Mixed Components of type RDBMS
	 */
	public Set<ETLComponent> getRDBMSSourceMixedComponentsTables() {
		Set<ETLComponent> tables = new HashSet<ETLComponent>();
		for (int i = 0; i < rdbmsMixedComponents.length; i++) {
			tables.addAll(getMixedComponentInformations(rdbmsMixedComponents[i], "DBTABLE", "TABLE", "SOURCE"));
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
	 * Get all the tables used by a specific RDBMS component type
	 */
	public Set<ETLComponent> getRDBMSComponentTables(String componentType) {
		return getExtendedComponentInformations(componentType, "DBTABLE", "TABLE");
	}

	/**
	 * Get all the tables used by a specific FILE component type
	 */
	public Set<String> getFileComponentLocations(String componentType) {
		return getComponentInformations(componentType, "FILE", "FILENAME");
	}

	/**
	 * Get the values used by a specific component type for a specific field and name
	 */
	public Set<String> getComponentInformations(String componentTypeName, String fieldValue, String nameValue) {

		Set<String> informations = new HashSet<String>();

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile("//node[@componentName='" + componentTypeName + "']");
			NodeList nList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				String componentValue = xpath.evaluate("elementParameter[@field='" + fieldValue + "' and @name='" + nameValue + "']/@value", node);
				if (!componentValue.isEmpty()) {
					informations.add(componentValue.replaceAll("\"", ""));
				}

			}

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return informations;

	}

	/**
	 * Get the values and the connection used by a specific component type for a specific field and name
	 */
	public Set<ETLComponent> getExtendedComponentInformations(String componentTypeName, String fieldValue, String nameValue) {

		Set<ETLComponent> informations = new HashSet<ETLComponent>();

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile("//node[@componentName='" + componentTypeName + "']");
			NodeList nList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				ETLComponent etlComponent = new ETLComponent();
				String componentValue = xpath.evaluate("elementParameter[@field='" + fieldValue + "' and @name='" + nameValue + "']/@value", node);
				if (!componentValue.isEmpty()) {
					etlComponent.setValue(componentValue.replaceAll("\"", ""));
				}
				String connectionValue = xpath.evaluate("elementParameter[@field='COMPONENT_LIST' and @name='CONNECTION']/@value", node);
				if (!connectionValue.isEmpty()) {
					etlComponent.setConnectionComponentName(connectionValue);
				}

				if (!etlComponent.getValue().isEmpty()) {
					informations.add(etlComponent);
				}
			}

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return informations;

	}

	/**
	 * Get the values used by a specific Mixed component type for a specific field,name and role (target or source)
	 */
	public Set<ETLComponent> getMixedComponentInformations(String componentTypeName, String fieldValue, String nameValue, String role) {

		Set<ETLComponent> informations = new HashSet<ETLComponent>();

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile("//node[@componentName='" + componentTypeName + "']");
			NodeList nList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				ETLComponent etlComponent = new ETLComponent();
				String componentValue = xpath.evaluate("elementParameter[@field='" + fieldValue + "' and @name='" + nameValue + "']/@value", node);
				if (!componentValue.isEmpty()) {
					componentValue = componentValue.replaceAll("\"", "");
				}

				String queryText = xpath.evaluate("elementParameter[@field='MEMO_SQL' and @name='QUERY']/@value", node);
				if (!queryText.isEmpty()) {
					// check if query text contains INSERT, UPDATE, DELETE or MERGE
					if (queryText.toLowerCase().contains("insert") || queryText.toLowerCase().contains("update") || queryText.toLowerCase().contains("delete")
							|| queryText.toLowerCase().contains("merge")) {
						// It's a TARGET
						if (role.equalsIgnoreCase("target")) {
							if (!componentValue.isEmpty()) {
								etlComponent.setValue(componentValue);
							}
						}

					} else if (queryText.toLowerCase().contains("select")) {
						// if only contains SELECT, it' a SOURCE
						if (role.equalsIgnoreCase("source")) {
							if (!componentValue.isEmpty()) {
								etlComponent.setValue(componentValue);
							}
						}
					}
				}
				String connectionValue = xpath.evaluate("elementParameter[@field='COMPONENT_LIST' and @name='CONNECTION']/@value", node);
				if (!connectionValue.isEmpty()) {
					etlComponent.setConnectionComponentName(connectionValue);
				}

				if (etlComponent.getValue() != null && !etlComponent.getValue().isEmpty()) {
					informations.add(etlComponent);
				}

			}

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return informations;

	}

	/**
	 * Get RDBMS Sources
	 */
	public Set<ETLRDBMSSource> getRDBMSDataSourcesObjects(String contextName) {
		Set<ETLRDBMSSource> sources = new HashSet<ETLRDBMSSource>();
		for (int i = 0; i < rdbmsDataSourceComponents.length; i++) {
			sources.addAll(getRDBSMSDataSource(rdbmsDataSourceComponents[i], contextName));
		}
		return sources;

	}

	public Set<ETLRDBMSSource> getRDBSMSDataSource(String componentType, String contextName) {
		Set<ETLRDBMSSource> sources = new HashSet<ETLRDBMSSource>();
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		try {
			XPathExpression expr = xpath.compile("//node[@componentName='" + componentType + "']");
			NodeList nList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				String componentName = xpath.evaluate("elementParameter[@name='UNIQUE_NAME']/@value", node);
				String label = xpath.evaluate("elementParameter[@name='LABEL']/@value", node);
				String hostRef = xpath.evaluate("elementParameter[@name='HOST']/@value", node);
				String schemaRef = xpath.evaluate("elementParameter[@name='SCHEMA_DB']/@value", node);
				String dbNameRef = xpath.evaluate("elementParameter[@name='DBNAME']/@value", node);
				String jdbcUrl = xpath.evaluate("elementParameter[@name='JDBC_URL']/@value", node);
				String host = null, schema = null, dbName = null;
				if (hostRef != null) {
					if (hostRef.contains("context")) {
						host = getContextParameter(contextName, hostRef.replaceFirst("context.", ""));
					} else {
						host = hostRef.replaceAll("\"", "");
					}
				}
				if (schemaRef != null) {
					if (schemaRef.contains("context")) {
						schema = getContextParameter(contextName, schemaRef.replaceFirst("context.", ""));
					} else {
						schema = schemaRef.replaceAll("\"", "");
					}
				}
				if (dbNameRef != null) {
					if (dbNameRef.contains("context")) {
						dbName = getContextParameter(contextName, dbNameRef.replaceFirst("context.", ""));
					} else {
						dbName = dbNameRef.replaceAll("\"", "");
					}
				}

				ETLRDBMSSource source = new ETLRDBMSSource(componentName, label, host, schema, dbName, jdbcUrl);
				sources.add(source);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sources;
	}

	public String getComponentElementParameter(String componentType, String elementParameterName) {
		try {
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("//node[@componentName='" + componentType + "']/elementParameter[@name='" + elementParameterName + "']");

			NodeList nList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element nodeElement = (Element) node;
					String parameterValue = nodeElement.getAttribute("value");
					return parameterValue;
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the names of the contexts in the job
	 */
	public Set<String> getContextNames() {
		Set<String> contextNames = new HashSet<String>();
		NodeList nList = document.getElementsByTagName("context");
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element nodeElement = (Element) node;
				String contextName = nodeElement.getAttribute("name");
				contextNames.add(contextName);
			}
		}
		return contextNames;
	}

	public String getContextParameter(String contextName, String parameterName) {

		try {
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("//context[@name='" + contextName + "']/contextParameter[@name='" + parameterName + "']");

			NodeList nList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element nodeElement = (Element) node;
					String parameterValue = nodeElement.getAttribute("value");
					return parameterValue;
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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

	/**
	 * Print contents of a ETLRDBMSDataSource Set to console
	 */
	public void printETLRDBMSSource(String label, Set<ETLRDBMSSource> contents) {
		System.out.println(label + ":");
		System.out.println("-------------------------");
		for (ETLRDBMSSource content : contents) {
			System.out.println("Component Name: " + content.getComponentName() + " DB Name: " + content.getDatabaseName() + " Host: " + content.getHost()
					+ " JDBC Url: " + content.getJdbcUrl() + " Label: " + content.getLabel() + " Schema: " + content.getSchema());
		}
		System.out.println("");

	}

	/**
	 * Print contents of a ETLComponenet Set to console
	 */
	public void printETLComponent(String label, Set<ETLComponent> contents) {
		System.out.println(label + ":");
		System.out.println("-------------------------");
		for (ETLComponent content : contents) {
			System.out.println(content.getValue() + " -> " + content.getConnectionComponentName());
		}
		System.out.println("");

	}

}
