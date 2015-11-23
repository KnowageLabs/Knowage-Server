/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query.serializer.json;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.CalculatedSelectField;
import it.eng.qbe.query.ExpressionNode;
import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.InLineCalculatedSelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.serializer.IQuerySerializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.qbe.statement.StatementTockenizer;
import it.eng.qbe.statement.graph.GraphUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QueryJSONSerializer implements IQuerySerializer {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(QueryJSONSerializer.class);


	@Override
	public Object serialize(Query query, IDataSource dataSource, Locale locale) throws SerializationException {
		JSONObject  result = null;

		JSONArray recordsJSON;
		JSONArray filtersJSON;
		JSONArray havingsJSON;
		JSONObject filterExpJOSN;
		JSONArray subqueriesJSON;
		JSONObject subqueryJSON;
		Iterator subqueriesIterator;
		Query subquery;

		Assert.assertNotNull(query, "Query cannot be null");
		Assert.assertNotNull(query.getId(), "Query id cannot be null");
		Assert.assertNotNull(dataSource, "DataMartModel cannot be null");

		try {
			recordsJSON = serializeFields(query, dataSource, locale);
			filtersJSON = serializeFilters(query, dataSource, locale);
			filterExpJOSN = encodeFilterExp( query.getWhereClauseStructure() );
			havingsJSON = serializeHavings(query, dataSource, locale);

			subqueriesJSON = new JSONArray();
			subqueriesIterator = query.getSubqueryIds().iterator();
			while(subqueriesIterator.hasNext()) {
				String id = (String)subqueriesIterator.next();
				subquery = query.getSubquery(id);
				subqueryJSON = (JSONObject)serialize(subquery, dataSource, locale);
				subqueriesJSON.put(subqueryJSON);
			}

			result = new JSONObject();
			result.put(QuerySerializationConstants.ID, query.getId());
			result.put(QuerySerializationConstants.NAME, query.getName());
			result.put(QuerySerializationConstants.DESCRIPTION, query.getDescription());
			result.put(QuerySerializationConstants.DISTINCT, query.isDistinctClauseEnabled());
			result.put(QuerySerializationConstants.IS_NESTED_EXPRESSION, query.isNestedExpression());

			result.put(QuerySerializationConstants.RELATIONS_ROLES, query.getRelationsRoles());
			JSONArray graphJSON = GraphUtilities.serializeGraph(query);
			result.put("graph", graphJSON);

			result.put(QuerySerializationConstants.FIELDS, recordsJSON);
			result.put(QuerySerializationConstants.FILTERS, filtersJSON);
			result.put(QuerySerializationConstants.EXPRESSION, filterExpJOSN);
			result.put(QuerySerializationConstants.HAVINGS, havingsJSON);
			result.put(QuerySerializationConstants.SUBQUERIES, subqueriesJSON);




		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + query, t);
		} finally {

		}

		return result;
	}


	/*
	 {
	  "id" : "it.eng.spagobi.ProductClass:productClassId",
	  "entity" : "ProductClass",
	  "field"  : "productClassId",
	  "alias"  : "",
	  "group"  : "undefined",
	  "order"  : "",
	  "funct"  : "",
	  "visible" : "si"
	 }
	 */
	private JSONArray serializeFields(Query query, IDataSource dataSource, Locale locale) throws SerializationException {
		JSONArray result;

		List fields;
		ISelectField field;
		String fieldUniqueName;
		IModelField datamartField;
		JSONObject fieldJSON;
		Iterator it;
		IModelProperties datamartLabels;
		String label, longDescription;

		Map<String, String> aliasSelectedFields = getFieldsNature(query, dataSource);


		logger.debug("IN");

		try {
			datamartLabels = null;
			if(locale != null) {
				//datamartLabels =  QbeCacheManager.getInstance().getLabels( dataSource , locale );
				datamartLabels = dataSource.getModelI18NProperties(locale);
			}

			fields = query.getSelectFields(false);
			Assert.assertNotNull(fields, "Fields cannot be null");
			logger.debug("Query [" + query.getId() + "] have [" + fields.size() + "] field/s to serialize");

			result = new JSONArray();
			it = fields.iterator();
			while( it.hasNext() ) {
				field = (ISelectField)it.next();
				logger.debug("Serializing filed [" + field.getAlias() + "]");
				try {
					fieldJSON = new JSONObject();

					fieldJSON.put(QuerySerializationConstants.FIELD_ALIAS, field.getAlias());

					fieldJSON.put(QuerySerializationConstants.FIELD_VISIBLE, field.isVisible());
					fieldJSON.put(QuerySerializationConstants.FIELD_INCLUDE, field.isIncluded());

					// field nature can me "measure" or "attribute"
					String nature = null;

					if (field.isSimpleField()) {
						SimpleSelectField dataMartSelectField = (SimpleSelectField)field;

						fieldJSON.put(QuerySerializationConstants.FIELD_TYPE, field.SIMPLE_FIELD);

						fieldUniqueName = dataMartSelectField.getUniqueName();
						datamartField = dataSource.getModelStructure().getField( fieldUniqueName );
						Assert.assertNotNull(datamartField, "A filed named [" + fieldUniqueName + "] does not exist in the datamart model");

						fieldJSON.put(QuerySerializationConstants.FIELD_ID, datamartField.getUniqueName());

						// localize entity name
						label = null;
						if(datamartLabels != null) {
							label = datamartLabels.getProperty(datamartField.getParent(), "label");
						}

						String parentLabel= datamartField.getParent().getName();
						if(datamartField.getParent().getProperties()!= null && datamartField.getParent().getProperty("label")!=null){
							parentLabel = (String) datamartField.getParent().getProperty("label");
						}

						label = StringUtilities.isEmpty(label)? parentLabel: label;
						fieldJSON.put(QuerySerializationConstants.FIELD_ENTITY, label);

						// localize field name
						label = null;
						if(datamartLabels != null) {
							label = datamartLabels.getProperty(datamartField, "label");
						}
						label = StringUtilities.isEmpty(label)? datamartField.getName(): label;
						fieldJSON.put(QuerySerializationConstants.FIELD_NAME, label);
						longDescription = getFieldLongDescription(datamartField, datamartLabels, null );
						fieldJSON.put(QuerySerializationConstants.FIELD_LONG_DESCRIPTION, longDescription);

						if( dataMartSelectField.isGroupByField() ) {
							fieldJSON.put(QuerySerializationConstants.FIELD_GROUP, "true");
						} else {
							fieldJSON.put(QuerySerializationConstants.FIELD_GROUP, "");
						}
						fieldJSON.put(QuerySerializationConstants.FIELD_ORDER, dataMartSelectField.getOrderType());
						fieldJSON.put(QuerySerializationConstants.FIELD_AGGREGATION_FUNCTION, dataMartSelectField.getFunction().getName());

						//DatamartProperties datamartProperties = dataSource.getDataMartProperties();
						String iconCls = datamartField.getPropertyAsString("type");
						fieldJSON.put(QuerySerializationConstants.FIELD_ICON_CLS, iconCls);

						fieldJSON.put(QuerySerializationConstants.TEMPORAL_OPERAND, dataMartSelectField.getTemporalOperand());
						fieldJSON.put(QuerySerializationConstants.TEMPORAL_OPERAND_PARAMETER, dataMartSelectField.getTemporalOperandParameter());

						// if an aggregation function is defined or if the field is declared as "measure" into property file,
						// then it is a measure, elsewhere it is an attribute
						nature = dataMartSelectField.getNature();


					} else if (field.isCalculatedField()){
						CalculatedSelectField calculatedSelectField = (CalculatedSelectField)field;

						fieldJSON.put(QuerySerializationConstants.FIELD_TYPE, field.CALCULATED_FIELD);

						JSONObject fieldCalculationDescriptor = new JSONObject();
						fieldCalculationDescriptor.put(QuerySerializationConstants.FIELD_TYPE, calculatedSelectField.getType());
						fieldCalculationDescriptor.put(QuerySerializationConstants.FIELD_EXPRESSION, calculatedSelectField.getExpression());
						fieldJSON.put(QuerySerializationConstants.FIELD_ID, fieldCalculationDescriptor);

						fieldJSON.put(QuerySerializationConstants.FIELD_ICON_CLS, "calculation");

						nature = QuerySerializationConstants.FIELD_NATURE_POST_LINE_CALCULATED;

					} else if (field.isInLineCalculatedField()) {
						InLineCalculatedSelectField calculatedSelectField = (InLineCalculatedSelectField)field;

						fieldJSON.put(QuerySerializationConstants.FIELD_TYPE, field.IN_LINE_CALCULATED_FIELD);

						JSONObject fieldCalculationDescriptor = new JSONObject();
						fieldCalculationDescriptor.put(QuerySerializationConstants.FIELD_ALIAS, calculatedSelectField.getAlias());
						fieldCalculationDescriptor.put(QuerySerializationConstants.FIELD_TYPE, calculatedSelectField.getType());
						fieldCalculationDescriptor.put(QuerySerializationConstants.FIELD_EXPRESSION, calculatedSelectField.getExpression());
						fieldJSON.put(QuerySerializationConstants.FIELD_ID, fieldCalculationDescriptor);
						fieldJSON.put(QuerySerializationConstants.FIELD_LONG_DESCRIPTION, calculatedSelectField.getExpression());

						if ( calculatedSelectField.isGroupByField() ) {
							fieldJSON.put(QuerySerializationConstants.FIELD_GROUP, "true");
						} else {
							fieldJSON.put(QuerySerializationConstants.FIELD_GROUP, "");
						}

						fieldJSON.put(QuerySerializationConstants.FIELD_AGGREGATION_FUNCTION, calculatedSelectField.getFunction().getName());
						fieldJSON.put(QuerySerializationConstants.FIELD_ORDER, calculatedSelectField.getOrderType());

						//fieldJSON.put(SerializationConstants.FIELD_GROUP, "");
						fieldJSON.put(QuerySerializationConstants.FIELD_ORDER, "");
						//fieldJSON.put(SerializationConstants.FIELD_AGGREGATION_FUNCTION, "");

						fieldJSON.put(QuerySerializationConstants.FIELD_ICON_CLS, "calculation");

						nature = calculatedSelectField.getNature();
						if(nature == null) {
							nature = getInLinecalculatedFieldNature(calculatedSelectField.getExpression(), aliasSelectedFields);
						}

					}

					fieldJSON.put(QuerySerializationConstants.FIELD_NATURE, nature);

				} catch(Throwable t) {
					throw new SerializationException("An error occurred while serializing field: " + field.getAlias(), t);
				}
				logger.debug("Filed [" + field.getAlias() + "] serialized succesfully: [" + fieldJSON.toString() + "]");
				result.put(fieldJSON);
			}

		}catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing select clause of query: " + query.getId(), t);
		} finally {
			logger.debug("OUT");
		}

		return result;
	}

	/**
	 * Get the nature of calculated field: MEASURE/ATTRIBUTE
	 * @param expr the expression of the calculated fields
	 * @param datamartFields the map <DatamartFieldAlias, DatamartFieldNature>
	 * @return the nature of the calculated field
	 */
	public static String getInLinecalculatedFieldNature(String expr, Map<String, String> datamartFields){

		StatementTockenizer stk = new StatementTockenizer(expr);
		while(stk.hasMoreTokens()){
			String alias = stk.nextTokenInStatement().trim();
			// alias can contain "DISTINCT" HQL/SQL key: we have to remove it
			if (alias.toUpperCase().startsWith("DISTINCT ")) {
				alias = alias.substring("DISTINCT ".length());
			}
			if(datamartFields.get(alias) == null) continue;
			if( (!(datamartFields.get(alias)).equals( QuerySerializationConstants.FIELD_NATURE_MEASURE) && !(datamartFields.get(alias)).equals( QuerySerializationConstants.FIELD_NATURE_MANDATORY_MEASURE))){
				return QuerySerializationConstants.FIELD_NATURE_ATTRIBUTE;
			}
		}
		return QuerySerializationConstants.FIELD_NATURE_MEASURE;
	}

	/**
	 * Get the map  <DatamartFieldAlias, DatamartFieldNature> from the selected fields list
	 * @param query
	 * @param dataSource
	 * @return <DatamartFieldAlias, DatamartFieldNature>
	 */
	public static Map<String, String> getFieldsNature(Query query,  IDataSource dataSource){
		Map<String, String> feldsNatureMap = new HashMap<String, String>();
		String nature;
		IModelField datamartField;
		SimpleSelectField dataMartSelectField;
		List<ISelectField> fields = query.getSelectFields(false);

		for(int i=0; i<fields.size(); i++){
			if ((fields.get(i)).isSimpleField()) {
				dataMartSelectField = (SimpleSelectField)fields.get(i);
				datamartField = dataSource.getModelStructure().getField( dataMartSelectField.getUniqueName() );
				String iconCls = datamartField.getPropertyAsString("type");
				nature = dataMartSelectField.getNature();
				if(nature==null){
					nature = dataMartSelectField.updateNature(iconCls);
				}
				feldsNatureMap.put(dataMartSelectField.getAlias(), nature);;
			}
		}

		return feldsNatureMap;
	}



	public static String getFieldLongDescription(IModelField field, IModelProperties datamartLabels, String alias) {
		String label = field.getName();
		if (datamartLabels != null) {
			label = datamartLabels.getProperty(field, "label");
		}
		String extendedLabel = StringUtilities.isEmpty(label)? field.getName(): label;
		if(alias!=null){
			extendedLabel=alias;
		}
		IModelEntity parent = field.getParent();
		if (parent == null) return extendedLabel;
		else return getEntityLongDescription(parent, datamartLabels) + " : " + extendedLabel;
	}

	public static String getEntityLongDescription(IModelEntity entity, IModelProperties datamartLabels) {
		String label = entity.getName();
		if (datamartLabels != null) {
			label = datamartLabels.getProperty(entity, "label");
		}
		String extendedLabel = StringUtilities.isEmpty(label)? entity.getName(): label;
		IModelEntity parent = entity.getParent();
		if (parent == null) return extendedLabel;
		else return getEntityLongDescription(parent, datamartLabels) + " / " + extendedLabel;
	}

	/*

				Iterator it = query.getSelectFields().iterator();
				while( it.hasNext() ) {
					SelectField selectField = (SelectField)it.next();
					DataMartField datamartField = getDatamartModel().getDataMartModelStructure().getField(selectField.getUniqueName());
					String label;
					label = datamartLabels.getLabel(datamartField);
					label =  StringUtilities.isEmpty(label)? datamartField.getName(): label;
				}
	 */

	/*
	{
	  "id" : "it.eng.spagobi.ProductClass:productClassId",
	  "entity" : "ProductClass",
	  "field"  : "productClassId",
	  //"alias"  : "",
	  "operator"  : "GREATER THAN",
	  "value"  : "5",
	  "type"  : "Static Value"
	  }
	 */
	private JSONArray serializeFilters(Query query, IDataSource dataSource, Locale locale) throws SerializationException {
		JSONArray filtersJSON = new JSONArray();

		List filters;
		WhereField filter;
		WhereField.Operand operand;
		JSONObject filterJSON;
		IModelField datamartFilter;
		String fieldUniqueName;
		Iterator it;
		IModelProperties datamartLabels;
		IModelField datamartField;

		filters = query.getWhereFields();
		Assert.assertNotNull(filters, "Filters cannot be null");

		datamartLabels = null;
		if(locale != null) {
			//datamartLabels =  QbeCacheManager.getInstance().getLabels( dataSource , locale );
			datamartLabels = dataSource.getModelI18NProperties(locale);
		}

		it = filters.iterator();
		while( it.hasNext() ) {
			filter = (WhereField)it.next();

			filterJSON = new JSONObject();
			try {
				filterJSON.put(QuerySerializationConstants.FILTER_ID, filter.getName());
				filterJSON.put(QuerySerializationConstants.FILTER_DESCRIPTION, filter.getDescription());
				filterJSON.put(QuerySerializationConstants.FILTER_PROMPTABLE, filter.isPromptable());

				// left operand
				operand = filter.getLeftOperand();
				filterJSON.put(QuerySerializationConstants.FILTER_LO_VALUE, operand.values[0]);
				if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_CALCULATED_FIELD)
						|| operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_INLINE_CALCULATED_FIELD)) {

						filterJSON.put(QuerySerializationConstants.FILTER_LO_DESCRIPTION, operand.description );
						String description = operand.values[0].substring(operand.values[0].indexOf("\"expression\":\"")+14);
						description.substring(0, description.indexOf("\""));
						filterJSON.put(QuerySerializationConstants.FILTER_LO_LONG_DESCRIPTION, description);
				} else if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD)) {
						datamartField = dataSource.getModelStructure().getField( operand.values[0] );

						String labelF, labelE;
						labelE = null;
						if(datamartLabels != null) {
							labelE = datamartLabels.getProperty(datamartField.getParent(), "label");
						}
						labelE = StringUtilities.isEmpty(labelE)? datamartField.getParent().getName(): labelE;


						labelF = null;
						if(datamartLabels != null) {
							labelF = datamartLabels.getProperty(datamartField, "label");
						}
						labelF = StringUtilities.isEmpty(labelF)? datamartField.getName(): labelF;

						filterJSON.put(QuerySerializationConstants.FILTER_LO_DESCRIPTION, labelE  + " : " + labelF );

						String loLongDescription = getFieldLongDescription(datamartField, datamartLabels, operand.alias);
						if(QuerySerializationConstants.TEMPORAL.equals(filter.getDescription())) {
							loLongDescription = operand.description;
						}
						filterJSON.put(QuerySerializationConstants.FILTER_LO_LONG_DESCRIPTION, loLongDescription);
				} else if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_SUBQUERY)) {
					String loLongDescription = "Subquery " + operand.description;
					filterJSON.put(QuerySerializationConstants.FILTER_LO_LONG_DESCRIPTION, loLongDescription);

					filterJSON.put(QuerySerializationConstants.FILTER_LO_DESCRIPTION, operand.description);
				} else if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_PARENT_FIELD)) {
					String[] chunks = operand.values[0].split(" ");
					String parentQueryId = chunks[0];
					String fieldName = chunks[1];
					datamartField = dataSource.getModelStructure().getField( fieldName );
					String datamartFieldLongDescription = getFieldLongDescription(datamartField, datamartLabels, operand.alias);
					String loLongDescription = "Query " + parentQueryId + ", " + datamartFieldLongDescription;
					filterJSON.put(QuerySerializationConstants.FILTER_LO_LONG_DESCRIPTION, loLongDescription);

					filterJSON.put(QuerySerializationConstants.FILTER_LO_DESCRIPTION, operand.description);
				} else {
					filterJSON.put(QuerySerializationConstants.FILTER_LO_DESCRIPTION, operand.description);
				}



				filterJSON.put(QuerySerializationConstants.FILTER_LO_TYPE, operand.type);
				filterJSON.put(QuerySerializationConstants.FILTER_LO_DEFAULT_VALUE, operand.defaulttValues[0]);
				filterJSON.put(QuerySerializationConstants.FILTER_LO_LAST_VALUE, operand.lastValues[0]);
				filterJSON.put(QuerySerializationConstants.FILTER_LO_ALIAS, operand.alias);
				filterJSON.put(QuerySerializationConstants.FILTER_OPERATOR, filter.getOperator());

				// right perand
				operand = filter.getRightOperand();
				filterJSON.put(QuerySerializationConstants.FILTER_RO_VALUE, JSONUtils.asJSONArray(operand.values));
				// TODO must be possible to use calculated field also as right hand operand
				if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD)) {
					datamartField = dataSource.getModelStructure().getField( operand.values[0] );

					String labelF, labelE;
					labelE = null;
					if(datamartLabels != null) {
						labelE = datamartLabels.getProperty(datamartField.getParent(), "label");
					}
					labelE = StringUtilities.isEmpty(labelE)? datamartField.getParent().getName(): labelE;


					labelF = null;
					if(datamartLabels != null) {
						labelF = datamartLabels.getProperty(datamartField, "label");
					}
					labelF = StringUtilities.isEmpty(labelF)? datamartField.getName(): labelF;

					filterJSON.put(QuerySerializationConstants.FILTER_RO_DESCRIPTION, labelE  + " : " + labelF );

					String roLongDescription = getFieldLongDescription(datamartField, datamartLabels, operand.alias);
					filterJSON.put(QuerySerializationConstants.FILTER_RO_LONG_DESCRIPTION, roLongDescription);
				} else if(operand.type.equalsIgnoreCase("Subquery")) {
					String roLongDescription = "Subquery " + operand.description;
					filterJSON.put(QuerySerializationConstants.FILTER_RO_LONG_DESCRIPTION, roLongDescription);

					filterJSON.put(QuerySerializationConstants.FILTER_RO_DESCRIPTION, operand.description);
				} else if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_PARENT_FIELD)) {
					String[] chunks = operand.values[0].split(" ");
					String parentQueryId = chunks[0];
					String fieldName = chunks[1];
					datamartField = dataSource.getModelStructure().getField( fieldName );
					String datamartFieldLongDescription = getFieldLongDescription(datamartField, datamartLabels, operand.alias);
					String loLongDescription = "Query " + parentQueryId + ", " + datamartFieldLongDescription;
					filterJSON.put(QuerySerializationConstants.FILTER_RO_LONG_DESCRIPTION, loLongDescription);

					filterJSON.put(QuerySerializationConstants.FILTER_RO_DESCRIPTION, operand.description);
				} else {
					filterJSON.put(QuerySerializationConstants.FILTER_RO_DESCRIPTION, operand.description);
				}
				filterJSON.put(QuerySerializationConstants.FILTER_RO_TYPE, operand.type);
				filterJSON.put(QuerySerializationConstants.FILTER_RO_DEFAULT_VALUE, JSONUtils.asJSONArray(operand.defaulttValues));
				filterJSON.put(QuerySerializationConstants.FILTER_RO_LAST_VALUE, JSONUtils.asJSONArray(operand.lastValues));
				filterJSON.put(QuerySerializationConstants.FILTER_RO_ALIAS, operand.alias);
				filterJSON.put(QuerySerializationConstants.FILTER_BOOLEAN_CONNETOR, filter.getBooleanConnector());

			} catch(JSONException e) {
				throw new SerializationException("An error occurred while serializing filter: " + filter.getName(), e);
			}
			filtersJSON.put(filterJSON);
		}

		return filtersJSON;
	}

	private JSONArray serializeHavings(Query query, IDataSource dataSource, Locale locale) throws SerializationException {
		JSONArray havingsJSON = new JSONArray();

		List havings;
		HavingField filter;
		HavingField.Operand operand;
		JSONObject havingJSON;
		IModelField datamartFilter;
		String fieldUniqueName;
		Iterator it;
		IModelProperties datamartLabels;
		IModelField datamartField;

		havings = query.getHavingFields();
		Assert.assertNotNull(havings, "Filters cannot be null");

		datamartLabels = null;
		if(locale != null) {
			//datamartLabels =  QbeCacheManager.getInstance().getLabels( dataSource , locale );
			datamartLabels = dataSource.getModelI18NProperties(locale);
		}

		it = havings.iterator();
		while( it.hasNext() ) {
			filter = (HavingField)it.next();

			havingJSON = new JSONObject();
			try {
				havingJSON.put(QuerySerializationConstants.FILTER_ID, filter.getName());
				havingJSON.put(QuerySerializationConstants.FILTER_DESCRIPTION, filter.getDescription());
				havingJSON.put(QuerySerializationConstants.FILTER_PROMPTABLE, filter.isPromptable());

				// left operand
				operand = filter.getLeftOperand();
				havingJSON.put(QuerySerializationConstants.FILTER_LO_VALUE, operand.values[0]);
				if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_CALCULATED_FIELD)
						|| operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_INLINE_CALCULATED_FIELD)) {

					havingJSON.put(QuerySerializationConstants.FILTER_LO_DESCRIPTION, operand.description );
					String description = operand.values[0].substring(operand.values[0].indexOf("\"expression\":\"")+14);
					description.substring(0, description.indexOf("\""));
					havingJSON.put(QuerySerializationConstants.FILTER_LO_LONG_DESCRIPTION, description);
				} else if (operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD)) {

						datamartField = dataSource.getModelStructure().getField( operand.values[0] );

						String labelF, labelE;
						labelE = null;
						if(datamartLabels != null) {
							labelE = datamartLabels.getProperty(datamartField.getParent(), "label");
						}
						labelE = StringUtilities.isEmpty(labelE)? datamartField.getParent().getName(): labelE;


						labelF = null;
						if(datamartLabels != null) {
							labelF = datamartLabels.getProperty(datamartField, "label");
						}
						labelF = StringUtilities.isEmpty(labelF)? datamartField.getName(): labelF;

						havingJSON.put(QuerySerializationConstants.FILTER_LO_DESCRIPTION, labelE  + " : " + labelF );

						String loLongDescription = getFieldLongDescription(datamartField, datamartLabels,operand.alias);
						havingJSON.put(QuerySerializationConstants.FILTER_LO_LONG_DESCRIPTION, loLongDescription);

				} else if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_SUBQUERY)) {
					String loLongDescription = "Subquery " + operand.description;
					havingJSON.put(QuerySerializationConstants.FILTER_LO_LONG_DESCRIPTION, loLongDescription);

					havingJSON.put(QuerySerializationConstants.FILTER_LO_DESCRIPTION, operand.description);
				} else if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_PARENT_FIELD)) {
					String[] chunks = operand.values[0].split(" ");
					String parentQueryId = chunks[0];
					String fieldName = chunks[1];
					datamartField = dataSource.getModelStructure().getField( fieldName );
					String datamartFieldLongDescription = getFieldLongDescription(datamartField, datamartLabels,operand.alias);
					String loLongDescription = "Query " + parentQueryId + ", " + datamartFieldLongDescription;
					havingJSON.put(QuerySerializationConstants.FILTER_LO_LONG_DESCRIPTION, loLongDescription);

					havingJSON.put(QuerySerializationConstants.FILTER_LO_DESCRIPTION, operand.description);
				} else {
					havingJSON.put(QuerySerializationConstants.FILTER_LO_DESCRIPTION, operand.description);
				}



				havingJSON.put(QuerySerializationConstants.FILTER_LO_TYPE, operand.type);
				havingJSON.put(QuerySerializationConstants.FILTER_LO_FUNCTION, operand.function.getName());
				havingJSON.put(QuerySerializationConstants.FILTER_LO_DEFAULT_VALUE, operand.defaulttValues[0]);
				havingJSON.put(QuerySerializationConstants.FILTER_LO_LAST_VALUE, operand.lastValues[0]);

				havingJSON.put(QuerySerializationConstants.FILTER_OPERATOR, filter.getOperator());

				operand = filter.getRightOperand();
				havingJSON.put(QuerySerializationConstants.FILTER_RO_VALUE, JSONUtils.asJSONArray(operand.values));
				// TODO must be possible to use calculated field also as right hand operand
				if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD)) {
					datamartField = dataSource.getModelStructure().getField( operand.values[0] );

					String labelF, labelE;
					labelE = null;
					if(datamartLabels != null) {
						labelE = datamartLabels.getProperty(datamartField.getParent(), "label");
					}
					labelE = StringUtilities.isEmpty(labelE)? datamartField.getParent().getName(): labelE;


					labelF = null;
					if(datamartLabels != null) {
						labelF = datamartLabels.getProperty(datamartField, "label");
					}
					labelF = StringUtilities.isEmpty(labelF)? datamartField.getName(): labelF;

					havingJSON.put(QuerySerializationConstants.FILTER_RO_DESCRIPTION, labelE  + " : " + labelF );

					String roLongDescription = getFieldLongDescription(datamartField, datamartLabels, operand.alias);
					havingJSON.put(QuerySerializationConstants.FILTER_RO_LONG_DESCRIPTION, roLongDescription);
				} else if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_SUBQUERY)) {
					String roLongDescription = "Subquery " + operand.description;
					havingJSON.put(QuerySerializationConstants.FILTER_RO_LONG_DESCRIPTION, roLongDescription);

					havingJSON.put(QuerySerializationConstants.FILTER_RO_DESCRIPTION, operand.description);
				} else if(operand.type.equalsIgnoreCase(AbstractStatement.OPERAND_TYPE_PARENT_FIELD)) {
					String[] chunks = operand.values[0].split(" ");
					String parentQueryId = chunks[0];
					String fieldName = chunks[1];
					datamartField = dataSource.getModelStructure().getField( fieldName );
					String datamartFieldLongDescription = getFieldLongDescription(datamartField, datamartLabels, operand.alias);
					String loLongDescription = "Query " + parentQueryId + ", " + datamartFieldLongDescription;
					havingJSON.put(QuerySerializationConstants.FILTER_RO_LONG_DESCRIPTION, loLongDescription);

					havingJSON.put(QuerySerializationConstants.FILTER_RO_DESCRIPTION, operand.description);
				} else {
					havingJSON.put(QuerySerializationConstants.FILTER_RO_DESCRIPTION, operand.description);
				}
				havingJSON.put(QuerySerializationConstants.FILTER_RO_TYPE, operand.type);
				havingJSON.put(QuerySerializationConstants.FILTER_RO_FUNCTION, operand.function.getName());
				havingJSON.put(QuerySerializationConstants.FILTER_RO_DEFAULT_VALUE, JSONUtils.asJSONArray(operand.defaulttValues));
				havingJSON.put(QuerySerializationConstants.FILTER_RO_LAST_VALUE, JSONUtils.asJSONArray(operand.lastValues));

				havingJSON.put(QuerySerializationConstants.FILTER_BOOLEAN_CONNETOR, filter.getBooleanConnector());

			} catch(JSONException e) {
				throw new SerializationException("An error occurred while serializing filter: " + filter.getName(), e);
			}
			havingsJSON.put(havingJSON);
		}

		return havingsJSON;
	}

	private JSONObject encodeFilterExp(ExpressionNode filterExp) throws SerializationException {
		JSONObject exp = new JSONObject();
		JSONArray childsJSON = new JSONArray();

		if(filterExp == null) return exp;

		try {
			exp.put(QuerySerializationConstants.EXPRESSION_TYPE, filterExp.getType()) ;
			exp.put(QuerySerializationConstants.EXPRESSION_VALUE, filterExp.getValue());

			for(int i = 0; i < filterExp.getChildNodes().size(); i++) {
				ExpressionNode child = (ExpressionNode)filterExp.getChildNodes().get(i);
				JSONObject childJSON = encodeFilterExp(child);
				childsJSON.put(childJSON);
			}

			exp.put(QuerySerializationConstants.EXPRESSION_CHILDREN, childsJSON);
		} catch(JSONException e) {
			throw new SerializationException("An error occurred while serializing filter expression", e);
		}

		return exp;
	}

}
