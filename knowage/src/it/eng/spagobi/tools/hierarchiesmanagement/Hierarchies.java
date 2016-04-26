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
package it.eng.spagobi.tools.hierarchiesmanagement;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Dimension;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Filter;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Hierarchy;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
	 * Get the hierarchy table name of the passed dimension
	 *
	 * @param dimension
	 *            the dimension name
	 * @return the name of the table that contains hierarchies
	 */
	public String getTablePrimaryKey(String dimension) {
		SourceBean sb = getTemplate();
		SourceBean dimensions = (SourceBean) sb.getAttribute(HierarchyConstants.DIMENSIONS);

		List lst = dimensions.getAttributeAsList(HierarchyConstants.DIMENSION);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			SourceBean sbRow = (SourceBean) iterator.next();
			// String dimensionName = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
			String dimensionLabel = sbRow.getAttribute(HierarchyConstants.LABEL) != null ? sbRow.getAttribute(HierarchyConstants.LABEL).toString() : null;
			if (dimensionLabel.equalsIgnoreCase(dimension)) {
				SourceBean sbHierarchy = (SourceBean) sbRow.getAttribute(HierarchyConstants.HIERARCHY_TABLE);
				String primaryKeyName = sbHierarchy.getAttribute(HierarchyConstants.PRIMARY_KEY) != null ? sbHierarchy.getAttribute(
						HierarchyConstants.PRIMARY_KEY).toString() : null;
				return primaryKeyName;
			}
		}
		return null;
	}

	/**
	 * Get the prefix for the passed dimension
	 *
	 * @param dimension
	 *            the dimension name
	 * @return the prefix of the tables and columns
	 */
	public String getPrefix(String dimension) {
		SourceBean sb = getTemplate();
		SourceBean dimensions = (SourceBean) sb.getAttribute(HierarchyConstants.DIMENSIONS);

		List lst = dimensions.getAttributeAsList(HierarchyConstants.DIMENSION);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			SourceBean sbRow = (SourceBean) iterator.next();
			// String dimensionName = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
			String dimensionLabel = sbRow.getAttribute(HierarchyConstants.LABEL) != null ? sbRow.getAttribute(HierarchyConstants.LABEL).toString() : null;
			if (dimensionLabel.equalsIgnoreCase(dimension)) {
				String prefix = sbRow.getAttribute(HierarchyConstants.PREFIX) != null ? sbRow.getAttribute(HierarchyConstants.PREFIX).toString() : null;
				return prefix;
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
				// set dimension fields metadata
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
					String fixValue = sbField.getAttribute(HierarchyConstants.FIELD_FIX_VALUE) != null ? sbField.getAttribute(
							HierarchyConstants.FIELD_FIX_VALUE).toString() : null;
					boolean fieldIsVisible = sbField.getAttribute(HierarchyConstants.FIELD_VISIBLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_VISIBLE)) : false;
					boolean fieldIsEditable = sbField.getAttribute(HierarchyConstants.FIELD_EDITABLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_EDITABLE)) : false;
					boolean fieldIsRequired = sbField.getAttribute(HierarchyConstants.FIELD_REQUIRED) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_REQUIRED)) : false;
					boolean fieldIsSingleValue = sbField.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE)) : true;
					boolean fieldIsParent = sbField.getAttribute(HierarchyConstants.FIELD_PARENT) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_PARENT)) : false;
					boolean fieldIsUnique = sbField.getAttribute(HierarchyConstants.FIELD_UNIQUE_CODE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_UNIQUE_CODE)) : false;
					boolean fieldIsOrder = sbField.getAttribute(HierarchyConstants.FIELD_IS_ORDER) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_IS_ORDER)) : false;

					Field field = new Field(fieldId, fieldName, fieldType, fixValue, fieldIsVisible, fieldIsEditable, fieldIsRequired, fieldIsSingleValue,
							fieldIsParent, fieldIsUnique, fieldIsOrder);
					metadataDimension.add(field);
				}
				toReturn.setMetadataFields(metadataDimension);
				// set dimension filters metadata
				List lstFilters = sbRow.getAttributeAsList(HierarchyConstants.DIM_FILTERS + "." + HierarchyConstants.FILTER);
				ArrayList<Filter> metadataFilterDim = new ArrayList<Filter>();
				for (Iterator iter = lstFilters.iterator(); iter.hasNext();) {
					SourceBean sbFilter = (SourceBean) iter.next();
					String filterName = sbFilter.getAttribute(HierarchyConstants.FILTER_NAME) != null ? sbFilter.getAttribute(HierarchyConstants.FILTER_NAME)
							.toString() : null;
					String filterType = sbFilter.getAttribute(HierarchyConstants.FILTER_TYPE) != null ? sbFilter.getAttribute(HierarchyConstants.FILTER_TYPE)
							.toString() : null;
					String filterDefault = sbFilter.getAttribute(HierarchyConstants.FILTER_DEFAULT) != null ? sbFilter.getAttribute(
							HierarchyConstants.FILTER_DEFAULT).toString() : null;

					boolean checkCondition = true;
					int idx = 0;
					LinkedHashMap<String, String> conditions = new LinkedHashMap<String, String>();
					while (checkCondition) {
						idx++;
						if (sbFilter.getAttribute(HierarchyConstants.FILTER_CONDITION + idx) != null) {
							String condition = sbFilter.getAttribute(HierarchyConstants.FILTER_CONDITION + idx).toString();
							conditions.put(HierarchyConstants.FILTER_CONDITION + idx, condition);
						} else {
							// there aren't conditions for the filter
							if (conditions.size() == 0) {
								logger.error("The dimension has the filter " + filterName
										+ " without valid conditions! No optional filter will be added on the GUI. Check the template!! ");
							}
							break;
						}

					}
					Filter filter = new Filter(filterName, filterType, filterDefault, conditions);
					metadataFilterDim.add(filter);
				}
				toReturn.setMetadataFilters(metadataFilterDim);

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
					String fieldFixValue = sbField.getAttribute(HierarchyConstants.FIELD_FIX_VALUE) != null ? sbField.getAttribute(
							HierarchyConstants.FIELD_FIX_VALUE).toString() : null;
					boolean fieldIsVisible = sbField.getAttribute(HierarchyConstants.FIELD_VISIBLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_VISIBLE)) : false;
					boolean fieldIsEditable = sbField.getAttribute(HierarchyConstants.FIELD_EDITABLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_EDITABLE)) : false;
					boolean fieldIsRequired = sbField.getAttribute(HierarchyConstants.FIELD_REQUIRED) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_REQUIRED)) : false;
					boolean fieldIsSingleValue = sbField.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE)) : true;
					boolean fieldIsParent = sbField.getAttribute(HierarchyConstants.FIELD_PARENT) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_PARENT)) : false;
					boolean fieldIsUnique = sbField.getAttribute(HierarchyConstants.FIELD_UNIQUE_CODE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_UNIQUE_CODE)) : false;
					boolean fieldIsOrder = sbField.getAttribute(HierarchyConstants.FIELD_IS_ORDER) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_IS_ORDER)) : false;

					Field field = new Field(fieldId, fieldName, fieldType, fieldFixValue, fieldIsVisible, fieldIsEditable, fieldIsRequired, fieldIsSingleValue,
							fieldIsParent, fieldIsUnique, fieldIsOrder);
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
					String fieldFixValue = sbField.getAttribute(HierarchyConstants.FIELD_FIX_VALUE) != null ? sbField.getAttribute(
							HierarchyConstants.FIELD_FIX_VALUE).toString() : null;
					boolean fieldIsVisible = sbField.getAttribute(HierarchyConstants.FIELD_VISIBLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_VISIBLE)) : false;
					boolean fieldIsEditable = sbField.getAttribute(HierarchyConstants.FIELD_EDITABLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_EDITABLE)) : false;
					boolean fieldIsRequired = sbField.getAttribute(HierarchyConstants.FIELD_REQUIRED) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_REQUIRED)) : false;
					boolean fieldIsSingleValue = sbField.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE)) : true;
					boolean fieldIsParent = sbField.getAttribute(HierarchyConstants.FIELD_PARENT) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_PARENT)) : false;
					boolean fieldIsUnique = sbField.getAttribute(HierarchyConstants.FIELD_UNIQUE_CODE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_UNIQUE_CODE)) : false;
					boolean fieldIsOrder = sbField.getAttribute(HierarchyConstants.FIELD_IS_ORDER) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_IS_ORDER)) : false;

					Field field = new Field(fieldId, fieldName, fieldType, fieldFixValue, fieldIsVisible, fieldIsEditable, fieldIsRequired, fieldIsSingleValue,
							fieldIsParent, fieldIsUnique, fieldIsOrder);
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
					String fieldFixValue = sbField.getAttribute(HierarchyConstants.FIELD_FIX_VALUE) != null ? sbField.getAttribute(
							HierarchyConstants.FIELD_FIX_VALUE).toString() : null;
					boolean fieldIsVisible = sbField.getAttribute(HierarchyConstants.FIELD_VISIBLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_VISIBLE)) : false;
					boolean fieldIsEditable = sbField.getAttribute(HierarchyConstants.FIELD_EDITABLE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_EDITABLE)) : false;
					boolean fieldIsRequired = sbField.getAttribute(HierarchyConstants.FIELD_REQUIRED) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_REQUIRED)) : false;
					boolean fieldIsSingleValue = sbField.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_SINGLE_VALUE)) : true;
					boolean fieldParent = sbField.getAttribute(HierarchyConstants.FIELD_PARENT) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_PARENT)) : false;
					boolean fieldIsUnique = sbField.getAttribute(HierarchyConstants.FIELD_UNIQUE_CODE) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_UNIQUE_CODE)) : false;
					boolean fieldIsOrder = sbField.getAttribute(HierarchyConstants.FIELD_IS_ORDER) != null ? Boolean.parseBoolean((String) sbField
							.getAttribute(HierarchyConstants.FIELD_IS_ORDER)) : false;

					Field field = new Field(fieldId, fieldName, fieldType, fieldFixValue, fieldIsVisible, fieldIsEditable, fieldIsRequired, fieldIsSingleValue,
							fieldParent, fieldIsUnique, fieldIsOrder);
					metadataLeafHierarchy.add(field);
				}
				toReturn.setMetadataLeafFields(metadataLeafHierarchy);
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
	public HashMap<String, Object> getConfig(String dimension) {
		HashMap<String, Object> toReturn = new HashMap<String, Object>();

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
					if (null != sbConfig.getAttribute(HierarchyConstants.TREE_NODE_ID))
						toReturn.put(HierarchyConstants.TREE_NODE_ID, sbConfig.getAttribute(HierarchyConstants.TREE_NODE_ID));
					if (null != sbConfig.getAttribute(HierarchyConstants.TREE_NODE_CD))
						toReturn.put(HierarchyConstants.TREE_NODE_CD, sbConfig.getAttribute(HierarchyConstants.TREE_NODE_CD));
					if (null != sbConfig.getAttribute(HierarchyConstants.TREE_NODE_NM))
						toReturn.put(HierarchyConstants.TREE_NODE_NM, sbConfig.getAttribute(HierarchyConstants.TREE_NODE_NM));
					if (null != sbConfig.getAttribute(HierarchyConstants.TREE_LEAF_ID))
						toReturn.put(HierarchyConstants.TREE_LEAF_ID, sbConfig.getAttribute(HierarchyConstants.TREE_LEAF_ID));
					if (null != sbConfig.getAttribute(HierarchyConstants.TREE_LEAF_CD))
						toReturn.put(HierarchyConstants.TREE_LEAF_CD, sbConfig.getAttribute(HierarchyConstants.TREE_LEAF_CD));
					if (null != sbConfig.getAttribute(HierarchyConstants.TREE_LEAF_NM))
						toReturn.put(HierarchyConstants.TREE_LEAF_NM, sbConfig.getAttribute(HierarchyConstants.TREE_LEAF_NM));
					if (null != sbConfig.getAttribute(HierarchyConstants.DIMENSION_ID))
						toReturn.put(HierarchyConstants.DIMENSION_ID, sbConfig.getAttribute(HierarchyConstants.DIMENSION_ID));
					if (null != sbConfig.getAttribute(HierarchyConstants.DIMENSION_CD))
						toReturn.put(HierarchyConstants.DIMENSION_CD, sbConfig.getAttribute(HierarchyConstants.DIMENSION_CD));
					if (null != sbConfig.getAttribute(HierarchyConstants.DIMENSION_NM))
						toReturn.put(HierarchyConstants.DIMENSION_NM, sbConfig.getAttribute(HierarchyConstants.DIMENSION_NM));
					if (null != sbConfig.getAttribute(HierarchyConstants.FILL_EMPTY))
						toReturn.put(HierarchyConstants.FILL_EMPTY, sbConfig.getAttribute(HierarchyConstants.FILL_EMPTY));
					if (null != sbConfig.getAttribute(HierarchyConstants.FILL_VALUE))
						toReturn.put(HierarchyConstants.FILL_VALUE, sbConfig.getAttribute(HierarchyConstants.FILL_VALUE));
					if (null != sbConfig.getAttribute(HierarchyConstants.UNIQUE_NODE))
						toReturn.put(HierarchyConstants.UNIQUE_NODE, sbConfig.getAttribute(HierarchyConstants.UNIQUE_NODE));
					if (null != sbConfig.getAttribute(HierarchyConstants.FORCE_NAME_AS_LEVEL))
						toReturn.put(HierarchyConstants.FORCE_NAME_AS_LEVEL, sbConfig.getAttribute(HierarchyConstants.FORCE_NAME_AS_LEVEL));
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
