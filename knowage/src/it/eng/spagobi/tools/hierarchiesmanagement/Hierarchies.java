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
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Dimension;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Hierarchy;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

	private SourceBean template;

	public Hierarchies() {
		loadDefinitionFile();
	}

	public void loadDefinitionFile() {
		// Load the XML file definition used for the hierarchies
		File definitionFile = new File(getResourcePath() + File.separator + "hierarchies" + File.separator + HierarchyConstants.HIERARCHIES_FILE_NAME + ".xml");
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
	 * Get the hierarchy table name of the passed dimension
	 *
	 * @param dimension
	 *            the dimension name
	 * @return the name of the table that contains hierarchies
	 */
	public String getHierarchyTableName(String dimension) {
		SourceBean sb = getTemplate();
		SourceBean dimensions = (SourceBean) sb.getAttribute(HierarchyConstants.DIMENSIONS);

		List lst = dimensions.getAttributeAsList(HierarchyConstants.DIMENSION);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			SourceBean sbRow = (SourceBean) iterator.next();
			// String dimensionName = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
			String dimensionLabel = sbRow.getAttribute(HierarchyConstants.LABEL) != null ? sbRow.getAttribute(HierarchyConstants.LABEL).toString() : null;
			if (dimensionLabel.equalsIgnoreCase(dimension)) {
				SourceBean sbHierarchy = (SourceBean) sbRow.getAttribute(HierarchyConstants.HIERARCHY_TABLE);
				String hierarchyName = sbHierarchy.getAttribute(HierarchyConstants.NAME) != null ? sbHierarchy.getAttribute(HierarchyConstants.NAME).toString()
						: null;
				return hierarchyName;
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
		SourceBean dimensions = (SourceBean) sb.getAttribute(HierarchyConstants.DIMENSIONS);

		List lst = dimensions.getAttributeAsList(HierarchyConstants.DIMENSION);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			SourceBean sbRow = (SourceBean) iterator.next();
			// String dimensionName = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
			String dimensionLabel = sbRow.getAttribute(HierarchyConstants.LABEL) != null ? sbRow.getAttribute(HierarchyConstants.LABEL).toString() : null;
			if (dimensionLabel.equalsIgnoreCase(dimension)) {
				SourceBean sbHierarchyPrefix = (SourceBean) sbRow.getAttribute(HierarchyConstants.HIERARCHY_FK);
				String hierarchyName = sbHierarchyPrefix.getAttribute(HierarchyConstants.NAME) != null ? sbHierarchyPrefix
						.getAttribute(HierarchyConstants.NAME).toString() : null;
				return hierarchyName;
			}
		}
		return null;
	}

	/**
	 * Get the dimension properties of the passed dimension
	 *
	 * @param dimension
	 *            the dimension name
	 * @return the dimension object with all attributes
	 */
	public Dimension getDimension(String dimension) {
		Dimension toReturn = new Dimension(dimension);
		SourceBean sb = getTemplate();
		SourceBean dimensions = (SourceBean) sb.getAttribute(HierarchyConstants.DIMENSIONS);

		List lst = dimensions.getAttributeAsList(HierarchyConstants.DIMENSION);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			SourceBean sbRow = (SourceBean) iterator.next();
			String dimensionLabel = sbRow.getAttribute(HierarchyConstants.LABEL) != null ? sbRow.getAttribute(HierarchyConstants.LABEL).toString() : null;
			if (dimensionLabel.equalsIgnoreCase(dimension)) {
				toReturn.setName(sbRow.getAttribute(HierarchyConstants.NAME) != null ? sbRow.getAttribute(HierarchyConstants.NAME).toString() : null);
				List lstFields = sbRow.getAttributeAsList(HierarchyConstants.DIM_FIELDS + "." + HierarchyConstants.FIELD);
				ArrayList<Field> metadataDimension = new ArrayList<Field>();
				for (Iterator iter = lstFields.iterator(); iter.hasNext();) {
					SourceBean sbField = (SourceBean) iter.next();
					String fieldId = sbField.getAttribute(HierarchyConstants.FIELD_ID) != null ? sbField.getAttribute(HierarchyConstants.FIELD_ID).toString()
							: null;
					String fieldName = sbField.getAttribute(HierarchyConstants.FIELD_NAME) != null ? sbField.getAttribute(HierarchyConstants.FIELD_NAME)
							.toString() : null;
					String fieldType = sbField.getAttribute(HierarchyConstants.FIELD_TYPE) != null ? sbField.getAttribute(HierarchyConstants.FIELD_TYPE)
							.toString() : null;
					boolean fieldIsVisible = sbField.getAttribute(HierarchyConstants.FIELD_VISIBLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_VISIBLE)) : false;
					boolean fieldIsEditable = sbField.getAttribute(HierarchyConstants.FIELD_EDITABLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_EDITABLE)) : false;
					boolean fieldIsRequired = sbField.getAttribute(HierarchyConstants.FIELD_REQUIRED) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_REQUIRED)) : false;
					boolean fieldIsSingleValue = sbField.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE)) : false;

					Field field = new Field(fieldId, fieldName, fieldType, fieldIsVisible, fieldIsEditable, fieldIsRequired, fieldIsSingleValue);
					metadataDimension.add(field);
				}
				toReturn.setMetadataFields(metadataDimension);
			}
		}
		return toReturn;
	}

	/**
	 * Get the hierarchy properties of the passed dimension
	 *
	 * @param dimension
	 *            the dimension name
	 * @return the hierarchy object linked to the input dimension with all attributes
	 */
	public Hierarchy getHierarchy(String dimension) {
		Hierarchy toReturn = new Hierarchy(dimension);
		SourceBean sb = getTemplate();
		SourceBean dimensions = (SourceBean) sb.getAttribute(HierarchyConstants.DIMENSIONS);

		List lst = dimensions.getAttributeAsList(HierarchyConstants.DIMENSION);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			SourceBean sbRow = (SourceBean) iterator.next();
			String dimensionLabel = sbRow.getAttribute(HierarchyConstants.LABEL) != null ? sbRow.getAttribute(HierarchyConstants.LABEL).toString() : null;
			if (dimensionLabel.equalsIgnoreCase(dimension)) {
				// GENERAL_FIELDS
				List lstGeneralFields = sbRow.getAttributeAsList(HierarchyConstants.HIER_FIELDS + "." + HierarchyConstants.GENERAL_FIELDS + "."
						+ HierarchyConstants.FIELD);
				ArrayList<Field> metadataGeneralHierarchy = new ArrayList<Field>();
				for (Iterator iter = lstGeneralFields.iterator(); iter.hasNext();) {
					SourceBean sbField = (SourceBean) iter.next();
					String fieldId = sbField.getAttribute(HierarchyConstants.FIELD_ID) != null ? sbField.getAttribute(HierarchyConstants.FIELD_ID).toString()
							: null;
					String fieldName = sbField.getAttribute(HierarchyConstants.FIELD_NAME) != null ? sbField.getAttribute(HierarchyConstants.FIELD_NAME)
							.toString() : null;
					String fieldType = sbField.getAttribute(HierarchyConstants.FIELD_TYPE) != null ? sbField.getAttribute(HierarchyConstants.FIELD_TYPE)
							.toString() : null;
					boolean fieldIsVisible = sbField.getAttribute(HierarchyConstants.FIELD_VISIBLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_VISIBLE)) : false;
					boolean fieldIsEditable = sbField.getAttribute(HierarchyConstants.FIELD_EDITABLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_EDITABLE)) : false;
					boolean fieldIsRequired = sbField.getAttribute(HierarchyConstants.FIELD_REQUIRED) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_REQUIRED)) : false;
					boolean fieldIsSingleValue = sbField.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE)) : false;

					Field field = new Field(fieldId, fieldName, fieldType, fieldIsVisible, fieldIsEditable, fieldIsRequired, fieldIsSingleValue);
					metadataGeneralHierarchy.add(field);
				}
				toReturn.setMetadataGeneralFields(metadataGeneralHierarchy);
				// NODE_FIELDS
				List lstNodeFields = sbRow.getAttributeAsList(HierarchyConstants.HIER_FIELDS + "." + HierarchyConstants.NODE_FIELDS + "."
						+ HierarchyConstants.FIELD);
				ArrayList<Field> metadataNodeHierarchy = new ArrayList<Field>();
				for (Iterator iter = lstNodeFields.iterator(); iter.hasNext();) {
					SourceBean sbField = (SourceBean) iter.next();
					String fieldId = sbField.getAttribute(HierarchyConstants.FIELD_ID) != null ? sbField.getAttribute(HierarchyConstants.FIELD_ID).toString()
							: null;
					String fieldName = sbField.getAttribute(HierarchyConstants.FIELD_NAME) != null ? sbField.getAttribute(HierarchyConstants.FIELD_NAME)
							.toString() : null;
					String fieldType = sbField.getAttribute(HierarchyConstants.FIELD_TYPE) != null ? sbField.getAttribute(HierarchyConstants.FIELD_TYPE)
							.toString() : null;
					boolean fieldIsVisible = sbField.getAttribute(HierarchyConstants.FIELD_VISIBLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_VISIBLE)) : false;
					boolean fieldIsEditable = sbField.getAttribute(HierarchyConstants.FIELD_EDITABLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_EDITABLE)) : false;
					boolean fieldIsRequired = sbField.getAttribute(HierarchyConstants.FIELD_REQUIRED) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_REQUIRED)) : false;
					boolean fieldIsSingleValue = sbField.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE)) : false;

					Field field = new Field(fieldId, fieldName, fieldType, fieldIsVisible, fieldIsEditable, fieldIsRequired, fieldIsSingleValue);
					metadataNodeHierarchy.add(field);
				}
				toReturn.setMetadataNodeFields(metadataNodeHierarchy);
				// LEAF_FIELDS
				List lstLeafFields = sbRow.getAttributeAsList(HierarchyConstants.HIER_FIELDS + "." + HierarchyConstants.LEAF_FIELDS + "."
						+ HierarchyConstants.FIELD);
				ArrayList<Field> metadataLeafHierarchy = new ArrayList<Field>();
				for (Iterator iter = lstLeafFields.iterator(); iter.hasNext();) {
					SourceBean sbField = (SourceBean) iter.next();
					String fieldId = sbField.getAttribute(HierarchyConstants.FIELD_ID) != null ? sbField.getAttribute(HierarchyConstants.FIELD_ID).toString()
							: null;
					String fieldName = sbField.getAttribute(HierarchyConstants.FIELD_NAME) != null ? sbField.getAttribute(HierarchyConstants.FIELD_NAME)
							.toString() : null;
					String fieldType = sbField.getAttribute(HierarchyConstants.FIELD_TYPE) != null ? sbField.getAttribute(HierarchyConstants.FIELD_TYPE)
							.toString() : null;
					boolean fieldIsVisible = sbField.getAttribute(HierarchyConstants.FIELD_VISIBLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_VISIBLE)) : false;
					boolean fieldIsEditable = sbField.getAttribute(HierarchyConstants.FIELD_EDITABLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_EDITABLE)) : false;
					boolean fieldIsRequired = sbField.getAttribute(HierarchyConstants.FIELD_REQUIRED) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_REQUIRED)) : false;
					boolean fieldIsSingleValue = sbField.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE)) : false;

					Field field = new Field(fieldId, fieldName, fieldType, fieldIsVisible, fieldIsEditable, fieldIsRequired, fieldIsSingleValue);
					metadataLeafHierarchy.add(field);
				}
				toReturn.setMetadataNodeFields(metadataLeafHierarchy);
			}
		}
		return toReturn;
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

		SourceBean dimensions = (SourceBean) sb.getAttribute(HierarchyConstants.DIMENSIONS);

		List lst = dimensions.getAttributeAsList(HierarchyConstants.DIMENSION);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			JSONObject hierarchy = new JSONObject();
			SourceBean sbRow = (SourceBean) iterator.next();
			String label = sbRow.getAttribute(HierarchyConstants.LABEL) != null ? sbRow.getAttribute(HierarchyConstants.LABEL).toString() : null;
			if (label.equalsIgnoreCase(dimension)) {
				String datasource = sbRow.getAttribute(HierarchyConstants.DATASOURCE) != null ? sbRow.getAttribute(HierarchyConstants.DATASOURCE).toString()
						: null;
				return datasource;
			}
		}

		return null;
	}

	/**
	 * Get the corresponding general config for the dimension's hierarchy
	 *
	 * @param dimension
	 *            the dimension name
	 * @return the hashmap config
	 */
	public HashMap getConfig(String dimension) {
		HashMap toReturn = new HashMap();

		SourceBean sb = getTemplate();
		SourceBean dimensions = (SourceBean) sb.getAttribute(HierarchyConstants.DIMENSIONS);

		List lst = dimensions.getAttributeAsList(HierarchyConstants.DIMENSION);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			JSONObject hierarchy = new JSONObject();
			SourceBean sbRow = (SourceBean) iterator.next();
			String label = sbRow.getAttribute(HierarchyConstants.LABEL) != null ? sbRow.getAttribute(HierarchyConstants.LABEL).toString() : null;
			if (label.equalsIgnoreCase(dimension)) {

				List lstConfigFields = sbRow.getAttributeAsList(HierarchyConstants.CONFIGS + "." + HierarchyConstants.CONFIG);
				for (Iterator iter = lstConfigFields.iterator(); iter.hasNext();) {
					SourceBean sbConfig = (SourceBean) iter.next();
					if (null != sbConfig.getAttribute(HierarchyConstants.NUM_LEVELS))
						toReturn.put(HierarchyConstants.NUM_LEVELS, sbConfig.getAttribute(HierarchyConstants.NUM_LEVELS));
					if (null != sbConfig.getAttribute(HierarchyConstants.ALLOW_DUPLICATE))
						toReturn.put(HierarchyConstants.ALLOW_DUPLICATE, sbConfig.getAttribute(HierarchyConstants.ALLOW_DUPLICATE));
					if (null != sbConfig.getAttribute(HierarchyConstants.NODE))
						toReturn.put(HierarchyConstants.NODE, sbConfig.getAttribute(HierarchyConstants.NODE));
					if (null != sbConfig.getAttribute(HierarchyConstants.LEAF))
						toReturn.put(HierarchyConstants.LEAF, sbConfig.getAttribute(HierarchyConstants.LEAF));
					if (null != sbConfig.getAttribute(HierarchyConstants.ORIG_NODE))
						toReturn.put(HierarchyConstants.ORIG_NODE, sbConfig.getAttribute(HierarchyConstants.ORIG_NODE));

				}
			}
		}

		return toReturn;
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
