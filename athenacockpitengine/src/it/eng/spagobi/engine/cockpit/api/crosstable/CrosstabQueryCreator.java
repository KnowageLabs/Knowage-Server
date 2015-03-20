/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engine.cockpit.api.crosstable;

import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Creates the crosstab query
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 * 
 */
public class CrosstabQueryCreator {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(CrosstabQueryCreator.class);

	public static final String QBE_SMARTFILTER_COUNT = "qbe_smartfilter_count";

	public static final String DEFAULT_ORDER_TYPE = "ASC";

	public static String getCrosstabQuery(CrosstabDefinition crosstabDefinition, IDataSetTableDescriptor descriptor, List<WhereField> whereFields,
			IDataSource dataSource) {
		logger.debug("IN");
		StringBuffer buffer = new StringBuffer();

		putSelectClause(buffer, crosstabDefinition, descriptor, dataSource);

		putFromClause(buffer, descriptor);

		putWhereClause(buffer, whereFields, descriptor, dataSource);

		putGroupByClause(buffer, crosstabDefinition, descriptor, dataSource);

		putOrderByClause(buffer, crosstabDefinition, descriptor, dataSource);

		String toReturn = buffer.toString();
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	private static void putFromClause(StringBuffer buffer, IDataSetTableDescriptor descriptor) {
		buffer.append(" FROM " + descriptor.getTableName() + " ");
	}

	private static void putSelectClause(StringBuffer toReturn, CrosstabDefinition crosstabDefinition, IDataSetTableDescriptor descriptor, IDataSource dataSource) {
		logger.debug("IN");
		List<CrosstabDefinition.Row> rows = crosstabDefinition.getRows();
		List<CrosstabDefinition.Column> colums = crosstabDefinition.getColumns();
		List<Measure> measures = crosstabDefinition.getMeasures();

		toReturn.append("SELECT ");

		// appends columns
		Iterator<CrosstabDefinition.Column> columsIt = colums.iterator();
		while (columsIt.hasNext()) {
			CrosstabDefinition.Column aColumn = columsIt.next();
			String columnName = descriptor.getColumnName(aColumn.getEntityId());
			toReturn.append(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource));
			toReturn.append(", ");
		}
		// appends rows
		Iterator<CrosstabDefinition.Row> rowsIt = rows.iterator();
		while (rowsIt.hasNext()) {
			CrosstabDefinition.Row aRow = rowsIt.next();
			String columnName = descriptor.getColumnName(aRow.getEntityId());
			toReturn.append(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource));
			toReturn.append(", ");
		}

		// appends measures
		Iterator<Measure> measuresIt = measures.iterator();
		while (measuresIt.hasNext()) {
			Measure aMeasure = measuresIt.next();
			IAggregationFunction function = aMeasure.getAggregationFunction();
			String columnName = descriptor.getColumnName(aMeasure.getEntityId());
			if (columnName == null) {
				// when defining a crosstab inside the SmartFilter document, an
				// additional COUNT field with id QBE_SMARTFILTER_COUNT
				// is automatically added inside query fields, therefore the
				// entity id is not found on base query selected fields
				columnName = "Count";
				if (aMeasure.getEntityId().equals(QBE_SMARTFILTER_COUNT)) {
					toReturn.append(AggregationFunctions.COUNT_FUNCTION.apply("*"));
				} else {
					logger.error("Entity id " + aMeasure.getEntityId() + " not found on the base query!!!!");
					throw new RuntimeException("Entity id " + aMeasure.getEntityId() + " not found on the base query!!!!");
				}
			} else {
				if (function != AggregationFunctions.NONE_FUNCTION) {
					toReturn.append(function.apply(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource)));
				} else {
					toReturn.append(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource));
				}
			}
			toReturn.append(" AS " + AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource));
			if (measuresIt.hasNext()) {
				toReturn.append(", ");
			}
		}

		logger.debug("OUT");
	}

	private static void putGroupByClause(StringBuffer toReturn, CrosstabDefinition crosstabDefinition, IDataSetTableDescriptor descriptor,
			IDataSource dataSource) {
		logger.debug("IN");
		List<CrosstabDefinition.Row> rows = crosstabDefinition.getRows();
		List<CrosstabDefinition.Column> colums = crosstabDefinition.getColumns();

		toReturn.append(" GROUP BY ");

		// appends columns
		Iterator<CrosstabDefinition.Column> columsIt = colums.iterator();
		while (columsIt.hasNext()) {
			CrosstabDefinition.Column aColumn = columsIt.next();
			String columnName = descriptor.getColumnName(aColumn.getEntityId());
			toReturn.append(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource));
			if (columsIt.hasNext()) {
				toReturn.append(", ");
			}
		}

		// append an extra comma between grouping on columns and grouping on
		// rows, if necessary
		if (colums.size() > 0 && rows.size() > 0) {
			toReturn.append(", ");
		}

		// appends rows
		Iterator<CrosstabDefinition.Row> rowsIt = rows.iterator();
		while (rowsIt.hasNext()) {
			CrosstabDefinition.Row aRow = rowsIt.next();
			String columnName = descriptor.getColumnName(aRow.getEntityId());
			toReturn.append(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource));
			if (rowsIt.hasNext()) {
				toReturn.append(", ");
			}
		}
		logger.debug("OUT");

	}

	private static void putOrderByClause(StringBuffer toReturn, CrosstabDefinition crosstabDefinition, IDataSetTableDescriptor descriptor,
			IDataSource dataSource) {
		logger.debug("IN");
		List<CrosstabDefinition.Row> rows = crosstabDefinition.getRows();
		List<CrosstabDefinition.Column> colums = crosstabDefinition.getColumns();

		toReturn.append(" ORDER BY ");

		// appends columns
		Iterator<CrosstabDefinition.Column> columsIt = colums.iterator();
		while (columsIt.hasNext()) {
			CrosstabDefinition.Column aColumn = columsIt.next();
			String columnName = descriptor.getColumnName(aColumn.getEntityId());
			toReturn.append(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource));
			toReturn.append(" " + DEFAULT_ORDER_TYPE);
			if (columsIt.hasNext()) {
				toReturn.append(", ");
			}
		}

		// append an extra comma between grouping on columns and grouping on
		// rows, if necessary
		if (colums.size() > 0 && rows.size() > 0) {
			toReturn.append(", ");
		}

		// appends rows
		Iterator<CrosstabDefinition.Row> rowsIt = rows.iterator();
		while (rowsIt.hasNext()) {
			CrosstabDefinition.Row aRow = rowsIt.next();
			String columnName = descriptor.getColumnName(aRow.getEntityId());
			toReturn.append(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource));
			toReturn.append(" " + DEFAULT_ORDER_TYPE);
			if (rowsIt.hasNext()) {
				toReturn.append(", ");
			}
		}

		logger.debug("OUT");
	}

	// public static String getColumnName(String elementElias, Query baseQuery,
	// List baseQuerySelectedFields) {
	// logger.debug("IN");
	// String toReturn = null;
	//
	// List qbeQueryFields = baseQuery.getSelectFields(true);
	// int index = -1;
	// for (int i = 0; i < qbeQueryFields.size(); i++) {
	// ISelectField field = (ISelectField) qbeQueryFields.get(i);
	// if (field.getAlias().equals(elementElias)) {
	// index = i;
	// break;
	// }
	// }
	//
	// if (index > -1) {
	// String[] sqlField = (String[]) baseQuerySelectedFields.get(index);
	// toReturn = sqlField[1] != null ? sqlField[1] : sqlField[0];
	// }
	//
	// logger.debug("OUT: returning " + toReturn);
	// return toReturn;
	// }

	private static void putWhereClause(StringBuffer toReturn, List<WhereField> whereFields, IDataSetTableDescriptor descriptor, IDataSource dataSource) {
		String boundedValue, leftValue, columnName;
		String[] rightValues;

		logger.debug("IN");
		String dialect = dataSource.getHibDialectClass();
		if (whereFields != null && whereFields.size() > 0) {
			toReturn.append(" WHERE ");
			for (int i = 0; i < whereFields.size(); i++) {
				leftValue = whereFields.get(i).getLeftOperand().values[0];
				columnName = descriptor.getColumnName(leftValue);

				rightValues = whereFields.get(i).getRightOperand().values;
				if (rightValues.length == 1) {
					boundedValue = getValueBounded(rightValues[0], descriptor.getColumnType(leftValue), dataSource);
					if (dialect.contains("SQLServerDialect")) {
						if (boundedValue.equals("true")) {
							boundedValue = "1";
						} else if (boundedValue.equals("false")) {
							boundedValue = "0";
						}
					}
					toReturn.append(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource) + " = " + boundedValue);
				} else {
					toReturn.append(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource) + " IN (");
					for (int j = 0; j < rightValues.length; j++) {
						boundedValue = getValueBounded(rightValues[j], descriptor.getColumnType(leftValue), dataSource);
						toReturn.append(boundedValue);
						if (j < rightValues.length - 1) {
							toReturn.append(", ");
						}
					}
					toReturn.append(") ");
				}
				if (i < whereFields.size() - 1) {
					toReturn.append(" AND ");
				}
			}
		}
		logger.debug("OUT: returning " + toReturn);
	}

	public static String getValueBounded(String operandValueToBound, Class clazz, IDataSource dataSource) {
		String boundedValue;
		Date operandValueToBoundDate;

		boundedValue = operandValueToBound;

		if (String.class.isAssignableFrom(clazz)) {
			// if the value is already surrounded by quotes, does not neither
			// add quotes nor escape quotes
			if (StringUtils.isBounded(operandValueToBound, "'")) {
				boundedValue = operandValueToBound;
			} else {
				operandValueToBound = StringUtils.escapeQuotes(operandValueToBound);
				return StringUtils.bound(operandValueToBound, "'");
			}
		} else if (Date.class.isAssignableFrom(clazz)) {
			Long time = null;
			try {
				time = Long.valueOf(operandValueToBound);
			} catch (NumberFormatException nfe) {
				logger.error("Error parsing the date " + operandValueToBound);
				throw new SpagoBIRuntimeException("Error parsing the date " + operandValueToBound + " as a long");
			}
			operandValueToBoundDate = new Date(time);
			String dialect = dataSource.getHibDialectClass();
			if (dialect == null) {
				dialect = dataSource.getHibDialectName();
			}
			boundedValue = composeStringToDt(dialect, operandValueToBoundDate);
		}

		return boundedValue;
	}

	public static String getTableQuery(List<String> fieldsName, boolean distinct, IDataSetTableDescriptor descriptor, List<WhereField> whereFields,
			String orderBy, List<String> orderByFieldsName) {
		logger.debug("IN");

		String query = getTableQuery(fieldsName, distinct, descriptor, whereFields, descriptor.getDataSource());
		StringBuffer buffer = new StringBuffer(query);
		putOrderByClause(buffer, orderByFieldsName, orderBy, descriptor, descriptor.getDataSource());
		String toReturn = buffer.toString();

		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	public static String getTableQuery(List<String> fieldsName, boolean distinct, IDataSetTableDescriptor descriptor, List<WhereField> whereFields,
			IDataSource dataSource) {
		logger.debug("IN");
		StringBuffer buffer = new StringBuffer();

		putSelectClause(buffer, fieldsName, distinct, descriptor, dataSource);

		putFromClause(buffer, descriptor);

		putWhereClause(buffer, whereFields, descriptor, dataSource);

		String toReturn = buffer.toString();
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	private static void putSelectClause(StringBuffer buffer, List<String> fieldsName, boolean distinct, IDataSetTableDescriptor descriptor,
			IDataSource dataSource) {

		logger.debug("IN");

		buffer.append("SELECT ");
		if (distinct) {
			buffer.append("DISTINCT ");
		}

		for (int i = 0; i < fieldsName.size(); i++) {
			String fieldName = fieldsName.get(i);
			String columnName = descriptor.getColumnName(fieldName);
			if (columnName == null) {
				throw new SpagoBIRuntimeException("Field [" + fieldName + "] not found on table descriptor");
			}
			buffer.append(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource));
			if (i < fieldsName.size() - 1) {
				buffer.append(", ");
			}
		}

		logger.debug("OUT");

	}

	private static void putOrderByClause(StringBuffer buffer, List<String> fieldsName, String orderBy, IDataSetTableDescriptor descriptor,
			IDataSource dataSource) {

		logger.debug("IN");

		buffer.append(" ORDER BY ");

		for (int i = 0; i < fieldsName.size(); i++) {
			String fieldName = fieldsName.get(i);
			String columnName = descriptor.getColumnName(fieldName);
			if (columnName == null) {
				throw new SpagoBIRuntimeException("Field [" + fieldName + "] not found on table descriptor");
			}
			buffer.append(AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource));
			buffer.append(" ");
			buffer.append(orderBy);
			if (i < fieldsName.size() - 1) {
				buffer.append(", ");
			}
		}

		logger.debug("OUT");

	}

	public static String composeStringToDt(String dialect, Date date) {
		String toReturn = "";

		DateFormat stagingDataFormat = new SimpleDateFormat("dd/MM/yyyy");
		String dateStr = stagingDataFormat.format(date);

		if (dialect != null) {

			if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_MYSQL)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = " STR_TO_DATE(" + dateStr + ",'%d/%m/%Y %h:%i:%s') ";
				} else {
					toReturn = " STR_TO_DATE('" + dateStr + "','%d/%m/%Y %h:%i:%s') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_HSQL)) {
				try {
					DateFormat df;
					if (StringUtils.isBounded(dateStr, "'")) {
						df = new SimpleDateFormat("'dd/MM/yyyy HH:mm:SS'");
					} else {
						df = new SimpleDateFormat("dd/MM/yyyy HH:mm:SS");
					}

					Date myDate = df.parse(dateStr);
					df = new SimpleDateFormat("yyyy-MM-dd");
					toReturn = "'" + df.format(myDate) + "'";

				} catch (Exception e) {
					toReturn = "'" + dateStr + "'";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_INGRES)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = " STR_TO_DATE(" + dateStr + ",'%d/%m/%Y') ";
				} else {
					toReturn = " STR_TO_DATE('" + dateStr + "','%d/%m/%Y') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + dateStr + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + dateStr + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE9i10g)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + dateStr + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + dateStr + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_POSTGRES)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + dateStr + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + dateStr + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_SQLSERVER)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = dateStr;
				} else {
					toReturn = "'" + dateStr + "'";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_TERADATA)) {
				/*
				 * Unfortunately we cannot use neither CAST(" + dateStr + " AS
				 * DATE FORMAT 'dd/mm/yyyy') nor CAST((" + dateStr + "
				 * (Date,Format 'dd/mm/yyyy')) As Date) because Hibernate does
				 * not recognize (and validate) those SQL functions. Therefore
				 * we must use a predefined date format (yyyy-MM-dd).
				 */
				try {
					DateFormat dateFormat;
					if (StringUtils.isBounded(dateStr, "'")) {
						dateFormat = new SimpleDateFormat("'dd/MM/yyyy'");
					} else {
						dateFormat = new SimpleDateFormat("dd/MM/yyyy");
					}
					Date myDate = dateFormat.parse(dateStr);
					dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					toReturn = "'" + dateFormat.format(myDate) + "'";
				} catch (Exception e) {
					logger.error("Error parsing the date " + dateStr, e);
					throw new SpagoBIRuntimeException("Error parsing the date " + dateStr + ".");
				}
			}
		}

		return toReturn;
	}

}
