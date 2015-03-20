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
package it.eng.spagobi.tools.hierarchiesmanagement;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * 
 */
public class Hierarchies {

	public static transient Logger logger = Logger.getLogger(Hierarchies.class);
	private static String HIERARCHIES_FILE_NAME = "hierarchies"; // for now is a
																	// constant

	// XML TAGS
	private static String DIMENSIONS = "DIMENSIONS";
	private static String DIMENSION = "DIMENSION";
	private static String NAME = "NAME";
	private static String HIERARCHY_PREFIX = "HIERARCHY_PREFIX";
	private static String HIERARCHY_FK = "HIERARCHY_FK";
	private static String DATASOURCE = "DATASOURCE";
	private SourceBean template;

	public Hierarchies() {
		loadDefinitionFile();
	}

	public void loadDefinitionFile() {
		// Load the XML file definition used for the hierarchies
		File definitionFile = new File(getResourcePath() + File.separator + "hierarchies" + File.separator + HIERARCHIES_FILE_NAME + ".xml");
		boolean fileExists = definitionFile.exists();
		Assert.assertTrue("The model with the definition of the hierarchies must be uploaded in the server. ", fileExists);

		if (!definitionFile.isFile()) {
			logger.error("No hierarchies definitions file loaded");
		} else {
			logger.debug("Hierarchies file name is equal to [" + definitionFile + "]");

			try {
				FileInputStream is = new FileInputStream(definitionFile.getAbsolutePath());
				String xmlContent = IOUtils.toString(is);
				template = SourceBean.fromXMLString(xmlContent);
			} catch (SourceBeanException e) {
				logger.error("Error reading Hierarchies file [" + definitionFile + "]");
			} catch (IOException e) {
				logger.error("Error reading Hierarchies file [" + definitionFile + "]");
			}

		}
	}

	/**
	 * @return the template
	 */
	public SourceBean getTemplate() {
		return template;
	}

	/**
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(SourceBean template) {
		this.template = template;
	}

	/**
	 * Get the hierarchy table name prefix of the passed dimension
	 * 
	 * @param dimension
	 *            the dimension name
	 * @return the prefix name of the table that contains hierarchies
	 */
	public String getHierarchyTablePrefixName(String dimension) {
		SourceBean sb = getTemplate();
		SourceBean dimensions = (SourceBean) sb.getAttribute(DIMENSIONS);

		List lst = dimensions.getAttributeAsList(DIMENSION);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			SourceBean sbRow = (SourceBean) iterator.next();
			String dimensionName = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
			if (dimensionName.equalsIgnoreCase(dimension)) {
				SourceBean sbHierarchyPrefix = (SourceBean) sbRow.getAttribute(HIERARCHY_PREFIX);
				String hierarchyPrefix = sbHierarchyPrefix.getAttribute(NAME) != null ? sbHierarchyPrefix.getAttribute(NAME).toString() : null;
				return hierarchyPrefix;
			}
		}
		return null;
	}

	/**
	 * Get the hierarchy table foreign key name of the passed dimension
	 * 
	 * @param dimension
	 *            the dimension name
	 * @return the foreign key name of the table that contains hierarchies
	 */
	public String getHierarchyTableForeignKeyName(String dimension) {
		SourceBean sb = getTemplate();
		SourceBean dimensions = (SourceBean) sb.getAttribute(DIMENSIONS);

		List lst = dimensions.getAttributeAsList(DIMENSION);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			SourceBean sbRow = (SourceBean) iterator.next();
			String dimensionName = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
			if (dimensionName.equalsIgnoreCase(dimension)) {
				SourceBean sbHierarchyPrefix = (SourceBean) sbRow.getAttribute(HIERARCHY_FK);
				String hierarchyPrefix = sbHierarchyPrefix.getAttribute(NAME) != null ? sbHierarchyPrefix.getAttribute(NAME).toString() : null;
				return hierarchyPrefix;
			}
		}
		return null;
	}

	/**
	 * Get the corresponding datasource name for the dimension
	 * 
	 * @param dimension
	 *            the dimension name
	 * @return the datasource name
	 */
	public String getDataSourceOfDimension(String dimension) {
		SourceBean sb = getTemplate();

		SourceBean dimensions = (SourceBean) sb.getAttribute(DIMENSIONS);

		List lst = dimensions.getAttributeAsList(DIMENSION);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			JSONObject hierarchy = new JSONObject();
			SourceBean sbRow = (SourceBean) iterator.next();
			String name = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
			if (name.equalsIgnoreCase(dimension)) {
				String datasource = sbRow.getAttribute(DATASOURCE) != null ? sbRow.getAttribute(DATASOURCE).toString() : null;
				return datasource;
			}
		}

		return null;
	}

	// *************************************************
	// Utilities
	// *************************************************

	/**
	 * Gets the path to the model with the hierarchies
	 * 
	 * @return
	 */

	private String getResourcePath() {
		String resPath;
		try {
			String jndiName = SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
			resPath = SpagoBIUtilities.readJndiResource(jndiName);
		} catch (Throwable t) {
			logger.debug(t);
			resPath = EnginConf.getInstance().getResourcePath();
		}

		if (resPath == null) {
			throw new SpagoBIRuntimeException("Resource path not found.");
		}
		return resPath;
	}

}
