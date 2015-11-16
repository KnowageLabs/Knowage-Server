/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.registry.parser;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration.Column;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration.Configuration;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration.Filter;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 * 
 */
public class RegistryConfigurationXMLParser {

	public static transient Logger logger = Logger.getLogger(RegistryConfigurationXMLParser.class);

	public static String TAG_ENTITY = "ENTITY";
	public static String TAG_FILTERS = "FILTERS";
	public static String TAG_FILTER = "FILTER";
	public static String TAG_COLUMNS = "COLUMNS";
	public static String TAG_COLUMN = "COLUMN";
	public static String TAG_CONFIGURATIONS = "CONFIGURATIONS";
	public static String TAG_CONFIGURATION = "CONFIGURATION";

	public static String ATTRIBUTE_PAGINATION = "pagination";
	public static String ATTRIBUTE_NAME = "name";
	public static String ATTRIBUTE_TITLE = "title";
	public static String ATTRIBUTE_FIELD = "field";
	public static String ATTRIBUTE_PRESENTATION = "presentation";
	public static String ATTRIBUTE_VALUE = "value";
	public static String ATTRIBUTE_SUMMARY_COLOR = "summaryColor";

	public static String ATTRIBUTE_EDITOR = "editor";
	public static String ATTRIBUTE_EDITABLE = "editable";
	public static String ATTRIBUTE_VISIBLE = "visible";
	public static String ATTRIBUTE_SUBENTITY = "subEntity";
	public static String ATTRIBUTE_FORMAT = "format";
	public static String ATTRIBUTE_COLOR = "color";
	public static String ATTRIBUTE_SUMMARY_FUNCTION = "summaryFunction";
	public static String ATTRIBUTE_TYPE = "type";
	public static String ATTRIBUTE_ORDER_BY = "orderBy";
	public static String ATTRIBUTE_INFO_COLUMN = "infoColumn";

	public static String ATTRIBUTE_FOREIGNKEY = "foreignKey";
	public static String ATTRIBUTE_MANDATORY_COLUMN = "mandatoryColumn";
	public static String ATTRIBUTE_MANDATORY_VALUE = "mandatoryValue";
	public static String ATTRIBUTE_COLUMNS_MAX_SIZE = "maxSize";
	public static String ATTRIBUTE_COLUMN_SIZE = "size";
	public static String ATTRIBUTE_COLUMN_TITLE = "title";
	public static String ATTRIBUTE_COLUMN_DEPENDENCES = "dependsFrom";
	public static String ATTRIBUTE_COLUMN_DEPENDENCES_ENTITY = "dependsFromEntity";
	public static String ATTRIBUTE_SORTER = "sorter";
	public static String ATTRIBUTE_UNSIGNED = "unsigned";
	public static String ATTRIBUTE_DRIVER_NAME = "driverName";

	public static String PRESENTATION_TYPE_MANUAL = "MANUAL";
	public static String PRESENTATION_TYPE_COMBO = "COMBO";
	public static String PRESENTATION_TYPE_DRIVER = "DRIVER";

	public static final String EDITOR_TYPE_TEXT = "TEXT";
	public static final String EDITOR_TYPE_COMBO = "COMBO";
	public static final String EDITOR_TYPE_PICKER = "PICKER";

	public RegistryConfiguration parse(SourceBean registryConf) {
		logger.debug("IN");
		RegistryConfiguration toReturn = null;
		try {
			toReturn = new RegistryConfiguration();

			SourceBean entitySB = (SourceBean) registryConf.getAttribute(TAG_ENTITY);
			Assert.assertNotNull(entitySB, "TAG " + TAG_ENTITY + " not found");
			String entity = (String) entitySB.getAttribute(ATTRIBUTE_NAME);
			logger.debug("Entity name is " + entity);
			Assert.assertNotNull(entity, "Entity " + ATTRIBUTE_NAME + " attribute not specified.");
			toReturn.setEntity(entity);

			List<RegistryConfiguration.Filter> filters = parseFilters(entitySB, toReturn);
			List<RegistryConfiguration.Column> columns = parseColumns(entitySB, toReturn);
			List<RegistryConfiguration.Configuration> configurations = parseConfigurations(entitySB, toReturn);
			toReturn.setFilters(filters);
			toReturn.setColumns(columns);
			toReturn.setConfigurations(configurations);

			toReturn.setSummaryColor(registryConf.getAttribute(ATTRIBUTE_SUMMARY_COLOR) != null ? registryConf.getAttribute(ATTRIBUTE_SUMMARY_COLOR).toString()
					: null);

			// default
			toReturn.setPagination(true);
			if (registryConf.getAttribute(ATTRIBUTE_PAGINATION) != null) {
				if (registryConf.getAttribute(ATTRIBUTE_PAGINATION).toString().equals("false")) {
					toReturn.setPagination(false);
				}
			}

		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	private List<Filter> parseFilters(SourceBean entitySB, RegistryConfiguration toReturn) {
		List<RegistryConfiguration.Filter> list = new ArrayList<RegistryConfiguration.Filter>();
		SourceBean filtersSB = (SourceBean) entitySB.getAttribute(TAG_FILTERS);
		List filters = filtersSB == null ? null : filtersSB.getAttributeAsList(TAG_FILTER);
		if (filters != null && filters.size() > 0) {
			Iterator it = filters.iterator();
			while (it.hasNext()) {
				SourceBean aFilter = (SourceBean) it.next();
				RegistryConfiguration.Filter filter = toReturn.new Filter();
				String field = (String) aFilter.getAttribute(ATTRIBUTE_FIELD);
				String title = (String) aFilter.getAttribute(ATTRIBUTE_TITLE);
				String presentationType = PRESENTATION_TYPE_COMBO.equalsIgnoreCase((String) aFilter.getAttribute(ATTRIBUTE_PRESENTATION)) ? Filter.PRESENTATION_TYPE_COMBO
						: (PRESENTATION_TYPE_DRIVER.equalsIgnoreCase((String) aFilter.getAttribute(ATTRIBUTE_PRESENTATION)) ? Filter.PRESENTATION_TYPE_DRIVER
								: Filter.PRESENTATION_TYPE_MANUAL);

				String driverName = null;
				if (presentationType.equals(Filter.PRESENTATION_TYPE_DRIVER)) {
					driverName = aFilter.getAttribute(ATTRIBUTE_DRIVER_NAME) != null ? aFilter.getAttribute(ATTRIBUTE_DRIVER_NAME).toString() : null;
				}

				logger.debug("Filter: title " + title + ", field " + field + ", presentation " + presentationType + "");
				Assert.assertTrue(field != null && title != null, "A filter must contain at least attributes " + ATTRIBUTE_TITLE + " and " + ATTRIBUTE_FIELD);
				filter.setField(field);
				filter.setTitle(title);
				filter.setPresentationType(presentationType);
				filter.setDriverName(driverName);

				list.add(filter);
			}
		}
		return list;
	}

	private List<Column> parseColumns(SourceBean entitySB, RegistryConfiguration toReturn) {
		List<RegistryConfiguration.Column> list = new ArrayList<RegistryConfiguration.Column>();
		SourceBean filtersSB = (SourceBean) entitySB.getAttribute(TAG_COLUMNS);
		// columns max size
		String columnsMaxSize = (String) filtersSB.getAttribute(ATTRIBUTE_COLUMNS_MAX_SIZE);
		toReturn.setColumnsMaxSize(columnsMaxSize);
		List filters = filtersSB == null ? null : filtersSB.getAttributeAsList(TAG_COLUMN);
		if (filters != null && filters.size() > 0) {
			Iterator it = filters.iterator();
			while (it.hasNext()) {
				SourceBean aColumn = (SourceBean) it.next();
				RegistryConfiguration.Column column = toReturn.new Column();
				String field = (String) aColumn.getAttribute(ATTRIBUTE_FIELD);
				String subEntity = (String) aColumn.getAttribute(ATTRIBUTE_SUBENTITY);
				if (subEntity != null && subEntity.trim().equals("")) {
					subEntity = null;
				}

				String size = (String) aColumn.getAttribute(ATTRIBUTE_COLUMN_SIZE);
				Integer intSize = null;
				try {
					intSize = Integer.parseInt(size);
				} catch (NumberFormatException e) {
					logger.debug("Column size not integer");
				}
				String title = (String) aColumn.getAttribute(ATTRIBUTE_COLUMN_TITLE);

				String sorter = (String) aColumn.getAttribute(ATTRIBUTE_SORTER);
				boolean unsigned = false;
				if (aColumn.getAttribute(ATTRIBUTE_UNSIGNED) != null) {
					try {
						unsigned = Boolean.parseBoolean((String) aColumn.getAttribute(ATTRIBUTE_UNSIGNED));
					} catch (Exception e) {
						logger.debug("Column unsigned not boolean");
					}
				}

				String foreignKey = (String) aColumn.getAttribute(ATTRIBUTE_FOREIGNKEY);
				boolean isEditable = !"false".equalsIgnoreCase((String) aColumn.getAttribute(ATTRIBUTE_EDITABLE));
				boolean isVisible = !"false".equalsIgnoreCase((String) aColumn.getAttribute(ATTRIBUTE_VISIBLE));

				String format = aColumn.getAttribute(ATTRIBUTE_FORMAT) != null ? (String) aColumn.getAttribute(ATTRIBUTE_FORMAT) : null;

				String color = aColumn.getAttribute(ATTRIBUTE_COLOR) != null ? (String) aColumn.getAttribute(ATTRIBUTE_COLOR) : null;

				String summaryFunction = aColumn.getAttribute(ATTRIBUTE_SUMMARY_FUNCTION) != null ? (String) aColumn.getAttribute(ATTRIBUTE_SUMMARY_FUNCTION)
						: null;

				String orderBy = aColumn.getAttribute(ATTRIBUTE_ORDER_BY) != null ? (String) aColumn.getAttribute(ATTRIBUTE_ORDER_BY) : null;

				boolean infoColumn = aColumn.getAttribute(ATTRIBUTE_INFO_COLUMN) != null
						&& aColumn.getAttribute(ATTRIBUTE_INFO_COLUMN).toString().equalsIgnoreCase("true") ? true : false;

				String type = aColumn.getAttribute(ATTRIBUTE_TYPE) != null ? (String) aColumn.getAttribute(ATTRIBUTE_TYPE) : null;
				if (type != null
						&& (type.equalsIgnoreCase(RegistryConfiguration.Column.COLUMN_TYPE_MERGE) || type
								.equalsIgnoreCase(RegistryConfiguration.Column.COLUMN_TYPE_MEASURE))) {
					if (type.equalsIgnoreCase("merge")) {
						// if is merge column set it is not editable and set
						// default color if not specified to white
						isEditable = false;
						if (color == null)
							color = "#FFFFFF";
						sorter = "ASC";
					}
				}

				// String editorType = EDITOR_TYPE_COMBO
				// .equalsIgnoreCase((String) aColumn
				// .getAttribute(ATTRIBUTE_EDITOR)) ? Column.EDITOR_TYPE_COMBO
				// : Column.EDITOR_TYPE_TEXT;
				String editorType = EDITOR_TYPE_TEXT;
				if (EDITOR_TYPE_COMBO.equalsIgnoreCase((String) aColumn.getAttribute(ATTRIBUTE_EDITOR)))
					editorType = Column.EDITOR_TYPE_COMBO;
				else if (EDITOR_TYPE_PICKER.equalsIgnoreCase((String) aColumn.getAttribute(ATTRIBUTE_EDITOR)))
					editorType = Column.EDITOR_TYPE_PICKER;

				String dependences = (String) aColumn.getAttribute(ATTRIBUTE_COLUMN_DEPENDENCES);

				String dependencesEntity = (String) aColumn.getAttribute(ATTRIBUTE_COLUMN_DEPENDENCES_ENTITY);
				logger.debug("Column: field " + field + ", subEntity " + subEntity + ", isEditable " + isEditable + ", isVisible " + isVisible + ", editor "
						+ editorType + ", dependsFrom " + dependences + ", dependsFromEntity " + dependencesEntity);

				Assert.assertTrue(field != null, "A column must contain at least attributes " + ATTRIBUTE_FIELD);
				Assert.assertTrue(subEntity == null || foreignKey != null, "If a " + ATTRIBUTE_SUBENTITY + " attribute is specified, the attribute "
						+ ATTRIBUTE_FOREIGNKEY + " is also requested.");
				column.setField(field);
				column.setSize(intSize);
				column.setTitle(title);
				column.setSorter(sorter);
				column.setUnsigned(unsigned);
				column.setSubEntity(subEntity);
				column.setForeignKey(foreignKey);
				column.setEditable(isEditable);
				column.setVisible(isVisible);
				column.setEditorType(editorType);
				column.setFormat(format);
				column.setColor(color);
				column.setSummaryFunction(summaryFunction);
				column.setType(type);
				column.setOrderBy(orderBy);
				column.setInfoColumn(infoColumn);
				column.setDependences(dependences);
				column.setDependencesEntity(dependencesEntity);

				String mandatoryColumn = (String) aColumn.getAttribute(ATTRIBUTE_MANDATORY_COLUMN);
				if (mandatoryColumn != null) {
					column.setMandatoryColumn(mandatoryColumn);
				}
				String mandatoryValue = (String) aColumn.getAttribute(ATTRIBUTE_MANDATORY_VALUE);
				if (mandatoryValue != null) {
					column.setMandatoryValue(mandatoryValue);
				}
				if (subEntity != null) { // if a column is a subEntity
											// reference, the editor is a
											// combo-box
					if (editorType != null && !editorType.trim().equals(EDITOR_TYPE_COMBO)) {
						logger.warn("For sub-entity references, only " + EDITOR_TYPE_COMBO + " is admissible as editor type");
					}
					column.setEditorType(EDITOR_TYPE_COMBO);
				}

				list.add(column);
			}
		}
		return list;
	}

	private List<Configuration> parseConfigurations(SourceBean entitySB, RegistryConfiguration toReturn) {
		List<RegistryConfiguration.Configuration> list = new ArrayList<RegistryConfiguration.Configuration>();

		SourceBean configurationsSB = (SourceBean) entitySB.getAttribute(TAG_CONFIGURATIONS);
		List configurations = configurationsSB == null ? null : configurationsSB.getAttributeAsList(TAG_CONFIGURATION);
		if (configurations != null && configurations.size() > 0) {
			Iterator it = configurations.iterator();
			while (it.hasNext()) {
				SourceBean aFilter = (SourceBean) it.next();
				RegistryConfiguration.Configuration configuration = toReturn.new Configuration();
				String name = (String) aFilter.getAttribute(ATTRIBUTE_NAME);
				String value = (String) aFilter.getAttribute(ATTRIBUTE_VALUE);

				logger.debug("Configuration: name " + name + ", value " + value);
				Assert.assertTrue(name != null && value != null, "A configuration must contain a name and a value");
				configuration.setName(name);
				configuration.setValue(value);
				list.add(configuration);
			}
		}
		return list;
	}

}
