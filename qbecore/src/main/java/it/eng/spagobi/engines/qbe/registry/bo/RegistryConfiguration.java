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
package it.eng.spagobi.engines.qbe.registry.bo;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class RegistryConfiguration {

	public static transient Logger logger = Logger.getLogger(RegistryConfiguration.class);

	private String keyField = null;
	private boolean pagination = true;
	private String summaryColor = null;

	private List<Filter> filters = null;
	private List<Column> columns = null;
	private List<Configuration> configurations = null;

	private String entity = null;
	private String columnsMaxSize = null;

	public boolean isPagination() {
		return pagination;
	}

	public void setPagination(boolean pagination) {
		this.pagination = pagination;
	}

	public String getSummaryColor() {
		return summaryColor;
	}

	public void setSummaryColor(String summaryColor) {
		this.summaryColor = summaryColor;
	}

	public String getColumnsMaxSize() {
		return columnsMaxSize;
	}

	public void setColumnsMaxSize(String columnsMaxSize) {
		this.columnsMaxSize = columnsMaxSize;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public List<Configuration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<Configuration> configurations) {
		this.configurations = configurations;
	}

	public String getKeyField() {
		return keyField;
	}

	public void setKeyField(String keyField) {
		this.keyField = keyField;
	}

	public Column getColumnConfiguration(String fieldName) {
		if (this.columns == null || this.columns.size() == 0) {
			logger.warn("No columns are defined. Column for field " + fieldName + " not found");
			return null;
		}
		Iterator<Column> it = columns.iterator();
		while (it.hasNext()) {
			Column c = it.next();
			if (c.getField().equals(fieldName)) {
				return c;
			}
		}
		logger.warn("Column for field " + fieldName + " not found");
		return null;
	}

	public String getConfiguration(String name) {
		String toReturn = null;
		boolean found = false;
		for (Iterator iterator = configurations.iterator(); iterator.hasNext() && !found;) {
			Configuration conf = (Configuration) iterator.next();
			if (conf.getName().equalsIgnoreCase(name)) {
				found = true;
				toReturn = conf.getValue();
			}
		}
		return toReturn;
	}

	public class Filter {

		public static final String PRESENTATION_TYPE_MANUAL = "MANUAL";

		public static final String PRESENTATION_TYPE_COMBO = "COMBO";

		public static final String PRESENTATION_TYPE_DRIVER = "DRIVER";

		private String title = null;

		private String presentationType = PRESENTATION_TYPE_MANUAL;

		private String field = null;

		private String driverName = null;

		public static final String ATTRIBUTE_STATIC_FILTER = "static";

		private boolean isStatic = false;

		private String filterValue = null;

		public static final String ATTRIBUTE_VISIBLE_FILTER = "visible";
		private boolean isVisible = false;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getPresentationType() {
			return presentationType;
		}

		public void setPresentationType(String presentationType) {
			this.presentationType = presentationType;
		}

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public String getDriverName() {
			return driverName;
		}

		public void setDriverName(String driverName) {
			this.driverName = driverName;
		}

		public boolean isStatic() {
			return isStatic;
		}

		public void setStatic(boolean isStatic) {
			this.isStatic = isStatic;
		}

		public String getFilterValue() {
			return filterValue;
		}

		public void setFilterValue(String filterValue) {
			this.filterValue = filterValue;
		}

		public boolean isVisible() {
			return isVisible;
		}

		public void setVisible(boolean isVisible) {
			this.isVisible = isVisible;
		}

	}

	public class Configuration {

		public static final String ENABLE_BUTTONs = "enableButtons";

		public static final String ENABLE_DELETE_RECORDS = "enableDeleteRecords";

		public static final String ENABLE_ADD_RECORDS = "enableAddRecords";

		public static final String IS_PK_AUTO_LOAD = "isPkAutoLoad";

		public static final String TABLE_FOR_PK_MAX = "tableForPKMax";
		public static final String COLUMN_FOR_PK_MAX = "columnForPKMax";

		private String name = null;

		private String value = null;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	public class Column {

		public static final String EDITOR_TYPE_TEXT = "TEXT";

		public static final String EDITOR_TYPE_COMBO = "COMBO";

		public static final String EDITOR_TYPE_PICKER = "PICKER";

		public static final String EDITOR_TYPE_POPUP = "POPUP";

		private String field = null;

		private String subEntity = null;

		private String foreignKey = null;

		private String editorType = EDITOR_TYPE_TEXT;

		private boolean isEditable = true;

		private String color = "#FFFFFF";

		private String summaryFunction = null;

		private String title = null;

		// this is the entity (table::column) on which to order in case of
		// referring to other table
		private String orderBy = null;

		// a column that in update must not be considered, for example because
		// it is a column referred from another table that is not its natural
		// key
		private boolean infoColumn = false;

		private boolean isVisible = true;
		// mandatory depending on another column value
		private String mandatoryColumn = null;
		// sets the column width
		private Integer size = null;
		// sets if the result set is ordered by this column and can assume
		// values "asc" or "desc"
		private String sorter = null;
		// sets if the column of type number must be signed or unsigned (only
		// positive numbers) by false or true values
		private boolean unsigned = false;

		private String format = null;

		// sets if cells in column must be merged
		private String type = null; // optional, can be merge, measure
		public static final String COLUMN_TYPE_MERGE = "merge";
		public static final String COLUMN_TYPE_MEASURE = "measure";

		// sets if the column is correlated to other columns (one or more)
		// visualized before (pre-requisite)
		// optional, can be a simple value or a list:
		// dependsFrom="field1,field2"
		private String dependences = null;

		private String dependencesEntity = null;

		private Object defaultValue = null;

		private boolean isAudit = false;

		private AuditColumnType auditColumnType = null;

		private Integer precision = null;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public boolean isUnsigned() {
			return unsigned;
		}

		public void setUnsigned(boolean unsigned) {
			this.unsigned = unsigned;
		}

		public Integer getSize() {
			return size;
		}

		public void setSize(Integer size) {
			this.size = size;
		}

		public String getSorter() {
			return sorter;
		}

		public void setSorter(String sorter) {
			this.sorter = sorter;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public String getSummaryFunction() {
			return summaryFunction;
		}

		public void setSummaryFunction(String summaryFunction) {
			this.summaryFunction = summaryFunction;
		}

		public String getMandatoryColumn() {
			return mandatoryColumn;
		}

		public void setMandatoryColumn(String mandatoryColumn) {
			this.mandatoryColumn = mandatoryColumn;
		}

		public String getMandatoryValue() {
			return mandatoryValue;
		}

		public void setMandatoryValue(String mandatoryValue) {
			this.mandatoryValue = mandatoryValue;
		}

		private String mandatoryValue = null;

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public String getEditorType() {
			return editorType;
		}

		public void setEditorType(String editorType) {
			this.editorType = editorType;
		}

		public boolean isEditable() {
			return isEditable;
		}

		public void setEditable(boolean isEditable) {
			this.isEditable = isEditable;
		}

		public boolean isVisible() {
			return isVisible;
		}

		public void setVisible(boolean isVisible) {
			this.isVisible = isVisible;
		}

		public String getSubEntity() {
			return subEntity;
		}

		public void setSubEntity(String subEntity) {
			this.subEntity = subEntity;
		}

		public String getForeignKey() {
			return foreignKey;
		}

		public void setForeignKey(String foreignKey) {
			this.foreignKey = foreignKey;
		}

		public boolean isMerge() {
			return (type != null && type.equalsIgnoreCase(COLUMN_TYPE_MERGE));
		}

		public boolean isMeasure() {
			return (type != null && type.equalsIgnoreCase(COLUMN_TYPE_MEASURE));
		}

		public String getOrderBy() {
			return orderBy;
		}

		public void setOrderBy(String orderBy) {
			this.orderBy = orderBy;
		}

		public boolean isInfoColumn() {
			return infoColumn;
		}

		public void setInfoColumn(boolean infoColumn) {
			this.infoColumn = infoColumn;
		}

		public String getDependences() {
			return dependences;
		}

		public void setDependences(String dependences) {
			this.dependences = dependences;
		}

		public String getDependencesEntity() {
			return dependencesEntity;
		}

		public void setDependencesEntity(String dependencesEntity) {
			this.dependencesEntity = dependencesEntity;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(Object defaultValue) {
			this.defaultValue = defaultValue;
		}

		public boolean isAudit() {
			return isAudit;
		}

		public void setAudit(boolean isAudit) {
			this.isAudit = isAudit;
		}

		public AuditColumnType getAuditColumnType() {
			return auditColumnType;
		}

		public void setAuditColumnType(AuditColumnType auditColumnType) {
			this.auditColumnType = auditColumnType;
		}

		public Integer getPrecision() {
			return precision;
		}

		public void setPrecision(Integer precision) {
			this.precision = precision;
		}

	}

	public enum AuditColumnType {

		// @formatter:off
		USER_INSERT, USER_UPDATE, USER_DELETE,
		TIME_INSERT, TIME_UPDATE, TIME_DELETE,
		IS_DELETED;
		// @formatter:on

	}

	public Optional<Column> getAuditColumn(AuditColumnType auditColumnType) {
		// @formatter:off
		return this.columns.stream()
				.filter(column -> column.isAudit() && column.getAuditColumnType().equals(auditColumnType))
				.findFirst();
		// @formatter:on
	}

}
