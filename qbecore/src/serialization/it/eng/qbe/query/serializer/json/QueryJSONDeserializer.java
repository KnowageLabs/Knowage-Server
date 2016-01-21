/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query.serializer.json;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.ExpressionNode;
import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.serializer.IQueryDeserializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.statement.graph.GraphUtilities;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QueryJSONDeserializer implements IQueryDeserializer {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(QueryJSONDeserializer.class);

	@Override
	public Query deserializeQuery(Object o, IDataSource dataSource) throws SerializationException {
		Query query;
		JSONObject queryJSON = null;
		JSONArray fieldsJSON = null;
		JSONArray filtersJSON = null;
		JSONArray havingsJSON = null;
		JSONObject expressionJSON = null;
		JSONArray subqueriesJSON = null;
		Query subquery;

		logger.debug("IN");

		try {
			Assert.assertNotNull(o, "Object to be deserialized cannot be null");

			if (o instanceof String) {
				logger.debug("Deserializing string [" + (String) o + "]");
				try {
					queryJSON = new JSONObject((String) o);
				} catch (Throwable t) {
					logger.debug("Object to be deserialized must be string encoding a JSON object");
					throw new SerializationException("An error occurred while deserializing query: " + (String) o, t);
				}
			} else if (o instanceof JSONObject) {
				queryJSON = (JSONObject) o;
			} else {
				Assert.assertUnreachable("Object to be deserialized must be of type string or of type JSONObject, not of type [" + o.getClass().getName() + "]");
			}

			query = new Query();

			try {
				query.setId(queryJSON.getString(QuerySerializationConstants.ID));
				query.setName(queryJSON.optString(QuerySerializationConstants.NAME));
				query.setDescription(queryJSON.optString(QuerySerializationConstants.DESCRIPTION));
				query.setDistinctClauseEnabled(queryJSON.optBoolean(QuerySerializationConstants.DISTINCT));
				query.setRelationsRoles(queryJSON.optString(QuerySerializationConstants.RELATIONS_ROLES));
				QueryGraph graph = GraphUtilities.deserializeGraph((JSONArray) queryJSON.opt("graph"), query, dataSource);
				query.setQueryGraph(graph);

				// TODO: move this in AnalysisStateLoader class
				try {
					query.setNestedExpression(queryJSON.getBoolean(QuerySerializationConstants.IS_NESTED_EXPRESSION));
				} catch (Exception e) {
					query.setNestedExpression(false);
				}
				fieldsJSON = queryJSON.getJSONArray(QuerySerializationConstants.FIELDS);
				filtersJSON = queryJSON.getJSONArray(QuerySerializationConstants.FILTERS);
				expressionJSON = queryJSON.getJSONObject(QuerySerializationConstants.EXPRESSION);
				havingsJSON = queryJSON.getJSONArray(QuerySerializationConstants.HAVINGS);
				subqueriesJSON = queryJSON.getJSONArray(QuerySerializationConstants.SUBQUERIES);

				if (queryJSON.has(QuerySerializationConstants.CALENDAR)) {
					JSONObject calObj = queryJSON.getJSONObject(QuerySerializationConstants.CALENDAR);
					if (calObj.has(QuerySerializationConstants.FIELDS)) {
						fieldsJSON.put(calObj.getJSONObject(QuerySerializationConstants.FIELDS));
					}
					if (calObj.has(QuerySerializationConstants.FILTERS)) {
						filtersJSON.put(calObj.getJSONObject(QuerySerializationConstants.FILTERS));
					}
				}

			} catch (JSONException e) {
				throw new SerializationException("An error occurred while deserializing query: " + queryJSON.toString(), e);
			}

			deserializeFields(fieldsJSON, dataSource, query);
			deserializeFilters(filtersJSON, dataSource, query);
			deserializeExpression(expressionJSON, dataSource, query);
			deserializeHavings(havingsJSON, dataSource, query);

			for (int i = 0; i < subqueriesJSON.length(); i++) {
				try {
					subquery = deserializeQuery(subqueriesJSON.get(i), dataSource);
				} catch (JSONException e) {
					throw new SerializationException("An error occurred while deserializing subquery number [" + (i + 1) + "]: " + subqueriesJSON.toString(), e);
				}

				query.addSubquery(subquery);
			}
		} finally {
			logger.debug("OUT");
		}

		return query;
	}

	private void deserializeFields(JSONArray fieldsJSON, IDataSource dataSource, Query query) throws SerializationException {
		JSONObject fieldJSON;
		IModelField field;
		String alias;
		String fieldType;

		String fieldUniqueName;
		String group;
		String order;
		String funct;
		String pattern;

		String temporalOperand;
		String temporalOperandParameter;

		JSONObject fieldClaculationDescriptor;
		String type;
		String nature;
		String expression;
		String slots;

		boolean visible;
		boolean included;

		logger.debug("IN");

		try {

			logger.debug("Query [" + query.getId() + "] have [" + fieldsJSON.length() + "] to deserialize");
			for (int i = 0; i < fieldsJSON.length(); i++) {
				try {
					fieldJSON = fieldsJSON.getJSONObject(i);

					alias = fieldJSON.getString(QuerySerializationConstants.FIELD_ALIAS);
					fieldType = fieldJSON.getString(QuerySerializationConstants.FIELD_TYPE);
					logger.debug("Deserializing field [" + alias + "] of type [" + fieldType + "]...");

					included = fieldJSON.getBoolean(QuerySerializationConstants.FIELD_INCLUDE);
					visible = fieldJSON.getBoolean(QuerySerializationConstants.FIELD_VISIBLE);

					temporalOperand = fieldJSON.optString(QuerySerializationConstants.TEMPORAL_OPERAND);
					temporalOperandParameter = fieldJSON.optString(QuerySerializationConstants.TEMPORAL_OPERAND_PARAMETER);
					if (StringUtilities.isEmpty(temporalOperandParameter)) {
						temporalOperandParameter = "0";
					}

					if (ISelectField.SIMPLE_FIELD.equalsIgnoreCase(fieldType)) {

						fieldUniqueName = fieldJSON.getString(QuerySerializationConstants.FIELD_ID);
						Assert.assertNotNull(fieldUniqueName, "Field name connot be null");

						field = dataSource.getModelStructure().getField(fieldUniqueName);
						Assert.assertNotNull(field, "Inpossible to retrive from datamart-structure a fild named " + fieldUniqueName
								+ ". Please check select clause: " + fieldsJSON.toString());
						if (StringUtilities.isEmpty(alias))
							alias = "Column_" + (i + 1);

						group = fieldJSON.optString(QuerySerializationConstants.FIELD_GROUP);
						order = fieldJSON.optString(QuerySerializationConstants.FIELD_ORDER);
						funct = fieldJSON.optString(QuerySerializationConstants.FIELD_AGGREGATION_FUNCTION);

						if (AggregationFunctions.get(funct).equals(AggregationFunctions.NONE_FUNCTION)) {
							pattern = null;// pattern = field.getPropertyAsString("format");
						} else {
							pattern = null;
						}

						// if (StringUtilities.isEmpty(temporalOperand)) {
						query.addSelectFiled(field.getUniqueName(), funct, alias, included, visible, group.equalsIgnoreCase("true"), order, pattern,
								temporalOperand, temporalOperandParameter);
						// }
						// Handle temporal operands
						// else {
						/*
						 * IModelEntity temporalDimension = getTemporalDimension(dataSource); HierarchicalDimensionField hierarchicalDimensionByEntity =
						 * temporalDimension.getHierarchicalDimensionByEntity(temporalDimension.getType()); Hierarchy defaultHierarchy =
						 * hierarchicalDimensionByEntity.getDefaultHierarchy();
						 *
						 * String entity = temporalDimension.getType(); String temporalCondition = " 1 = 1 "; String year = entity + ":" +
						 * defaultHierarchy.getLevelByType("YEAR"); String month = entity + ":" + defaultHierarchy.getLevelByType("MONTH"); String
						 * temporalDimensionDateField = "the_date";
						 *
						 *
						 * if (temporalOperand.equals("YTD")) { Calendar startDate = new GregorianCalendar(); startDate.set(startDate.get(Calendar.YEAR), 0, 1,
						 * 0, 0); Date today = new Date();
						 *
						 * temporalCondition = year + " = (select "+year+" from "+entity+" where "+temporalDimensionDateField+
						 * " between :startDate and :endDate ) ";
						 *
						 * } else if (temporalOperand.equals("MTD")) {
						 *
						 * temporalCondition = year + " = 2015 " + " AND " + month + " = 'October'";
						 *
						 * }
						 *
						 * type = "STRING"; nature = "ATTRIBUTE";
						 *
						 * expression = "CASE WHEN " + temporalCondition + "THEN " + field.getUniqueName() + " ELSE NULL END"; slots = "";
						 *
						 * query.addInLineCalculatedFiled(alias, expression, slots, type, nature, included, visible, group.equalsIgnoreCase("true"), order,
						 * funct);
						 */

						// }

					} else if (ISelectField.CALCULATED_FIELD.equalsIgnoreCase(fieldType)) {

						fieldClaculationDescriptor = fieldJSON.getJSONObject(QuerySerializationConstants.FIELD_ID);
						type = fieldClaculationDescriptor.getString(QuerySerializationConstants.FIELD_TYPE);
						expression = fieldClaculationDescriptor.getString(QuerySerializationConstants.FIELD_EXPRESSION);

						query.addCalculatedFiled(alias, expression, type, included, visible);
					} else if (ISelectField.IN_LINE_CALCULATED_FIELD.equalsIgnoreCase(fieldType)) {

						fieldClaculationDescriptor = fieldJSON.getJSONObject(QuerySerializationConstants.FIELD_ID);
						type = fieldClaculationDescriptor.getString(QuerySerializationConstants.FIELD_TYPE);
						nature = null;
						if (fieldClaculationDescriptor.has(QuerySerializationConstants.FIELD_NATURE)) {
							nature = fieldClaculationDescriptor.getString(QuerySerializationConstants.FIELD_NATURE);
						}
						expression = "("+fieldClaculationDescriptor.getString(QuerySerializationConstants.FIELD_EXPRESSION)+")";
						slots = fieldClaculationDescriptor.optString(QuerySerializationConstants.FIELD_SLOTS);
						group = fieldJSON.getString(QuerySerializationConstants.FIELD_GROUP);
						order = fieldJSON.getString(QuerySerializationConstants.FIELD_ORDER);
						funct = fieldJSON.getString(QuerySerializationConstants.FIELD_AGGREGATION_FUNCTION);

						query.addInLineCalculatedFiled(alias, expression, slots, type, nature, included, visible, group.equalsIgnoreCase("true"), order, funct);
					} else {
						Assert.assertUnreachable("Type [" + fieldType + "] of field [" + alias + "] is not valid");
					}

					logger.debug("Field [" + alias + "] succefully deserialized");
				} catch (Throwable t) {
					throw new SerializationException("An error occurred while deserializing field [" + fieldsJSON.toString() + "] of query [" + query.getId()
							+ "]", t);
				}
			}
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while deserializing select clause: " + fieldsJSON.toString(), t);
		}

	}

	private void deserializeFilters(JSONArray filtersJOSN, IDataSource dataSource, Query query) throws SerializationException {

		JSONObject filterJSON;

		String filterId;
		String filterDescription;
		boolean promptable;

		String[] operandValues;
		String operandDescription;
		String operandType;
		String[] operandLasDefaulttValues;
		String[] operandLastValues;
		String operandAlias;
		JSONArray operandValuesJSONArray;
		JSONArray operandDefaultValuesJSONArray;
		JSONArray operandLastValuesJSONArray;
		WhereField.Operand leftOperand;
		WhereField.Operand rightOperand;
		String operator;
		String booleanConnector;

		logger.debug("IN");

		try {
			logger.debug("Query [" + query.getId() + "] have [" + filtersJOSN.length() + "] to deserialize");
			for (int i = 0; i < filtersJOSN.length(); i++) {

				try {
					filterJSON = filtersJOSN.getJSONObject(i);
					filterId = filterJSON.getString(QuerySerializationConstants.FILTER_ID);
					filterDescription = filterJSON.getString(QuerySerializationConstants.FILTER_DESCRIPTION);
					promptable = filterJSON.getBoolean(QuerySerializationConstants.FILTER_PROMPTABLE);

					operandValues = new String[] { filterJSON.getString(QuerySerializationConstants.FILTER_LO_VALUE) };
					operandDescription = filterJSON.getString(QuerySerializationConstants.FILTER_LO_DESCRIPTION);
					operandType = filterJSON.getString(QuerySerializationConstants.FILTER_LO_TYPE);
					operandLasDefaulttValues = new String[] { filterJSON.getString(QuerySerializationConstants.FILTER_LO_DEFAULT_VALUE) };
					operandLastValues = new String[] { filterJSON.getString(QuerySerializationConstants.FILTER_LO_LAST_VALUE) };
					operandAlias = filterJSON.getString(QuerySerializationConstants.FILTER_LO_ALIAS);
					leftOperand = new WhereField.Operand(operandValues, operandDescription, operandType, operandLasDefaulttValues, operandLastValues,
							operandAlias);

					operator = filterJSON.getString(QuerySerializationConstants.FILTER_OPERATOR);

					operandValuesJSONArray = filterJSON.getJSONArray(QuerySerializationConstants.FILTER_RO_VALUE);
					operandValues = JSONUtils.asStringArray(operandValuesJSONArray);
					operandDescription = filterJSON.getString(QuerySerializationConstants.FILTER_RO_DESCRIPTION);
					operandType = filterJSON.getString(QuerySerializationConstants.FILTER_RO_TYPE);
					operandDefaultValuesJSONArray = filterJSON.optJSONArray(QuerySerializationConstants.FILTER_RO_DEFAULT_VALUE);
					operandLasDefaulttValues = JSONUtils.asStringArray(operandDefaultValuesJSONArray);
					operandLastValuesJSONArray = filterJSON.optJSONArray(QuerySerializationConstants.FILTER_RO_LAST_VALUE);
					operandLastValues = JSONUtils.asStringArray(operandLastValuesJSONArray);
					operandAlias = filterJSON.getString(QuerySerializationConstants.FILTER_RO_ALIAS);

					JSONObject temporalOperand = filterJSON.optJSONObject("temporalOperand");

					rightOperand = new WhereField.Operand(operandValues, operandDescription, operandType, operandLasDefaulttValues, operandLastValues,
							operandAlias);

					booleanConnector = filterJSON.getString(QuerySerializationConstants.FILTER_BOOLEAN_CONNETOR);

					Assert.assertTrue(!StringUtilities.isEmpty(operator), "Undefined operator for filter: " + filterJSON.toString());
					Assert.assertTrue(!"NONE".equalsIgnoreCase(operator), "Undefined operator NONE for filter: " + filterJSON.toString());

					query.addWhereField(filterId, filterDescription, promptable, leftOperand, operator, rightOperand, booleanConnector, temporalOperand);

				} catch (JSONException e) {
					throw new SerializationException("An error occurred while filter [" + filtersJOSN.toString() + "] of query [" + query.getId() + "]", e);
				}
			}
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while deserializing field of query [" + query.getId() + "]", t);
		} finally {
			logger.debug("OUT");
		}

	}

	private void deserializeHavings(JSONArray havingsJOSN, IDataSource dataSource, Query query) throws SerializationException {

		JSONObject havingJSON;
		IModelField field;

		String filterId;
		String filterDescription;
		boolean promptable;

		String[] operandValues;
		String operandDescription;
		String operandType;
		String operandFunction;
		String[] operandLasDefaulttValues;
		String[] operandLastValues;
		JSONArray operandValuesJSONArray;
		JSONArray operandDefaultValuesJSONArray;
		JSONArray operandLastValuesJSONArray;
		HavingField.Operand leftOperand;
		HavingField.Operand rightOperand;
		String operator;
		String booleanConnector;
		IAggregationFunction function;

		logger.debug("IN");

		try {

			logger.debug("Query [" + query.getId() + "] have [" + havingsJOSN.length() + "] to deserialize");
			for (int i = 0; i < havingsJOSN.length(); i++) {

				try {
					havingJSON = havingsJOSN.getJSONObject(i);
					filterId = havingJSON.getString(QuerySerializationConstants.FILTER_ID);
					filterDescription = havingJSON.getString(QuerySerializationConstants.FILTER_DESCRIPTION);
					promptable = havingJSON.getBoolean(QuerySerializationConstants.FILTER_PROMPTABLE);

					operandValues = new String[] { havingJSON.getString(QuerySerializationConstants.FILTER_LO_VALUE) };
					operandDescription = havingJSON.getString(QuerySerializationConstants.FILTER_LO_DESCRIPTION);
					operandType = havingJSON.getString(QuerySerializationConstants.FILTER_LO_TYPE);
					operandLasDefaulttValues = new String[] { havingJSON.getString(QuerySerializationConstants.FILTER_LO_DEFAULT_VALUE) };
					operandLastValues = new String[] { havingJSON.getString(QuerySerializationConstants.FILTER_LO_LAST_VALUE) };
					operandFunction = havingJSON.getString(QuerySerializationConstants.FILTER_LO_FUNCTION);
					function = AggregationFunctions.get(operandFunction);
					leftOperand = new HavingField.Operand(operandValues, operandDescription, operandType, operandLasDefaulttValues, operandLastValues, function);

					operator = havingJSON.getString(QuerySerializationConstants.FILTER_OPERATOR);

					operandValuesJSONArray = havingJSON.getJSONArray(QuerySerializationConstants.FILTER_RO_VALUE);
					operandValues = JSONUtils.asStringArray(operandValuesJSONArray);
					operandDescription = havingJSON.getString(QuerySerializationConstants.FILTER_RO_DESCRIPTION);
					operandType = havingJSON.getString(QuerySerializationConstants.FILTER_RO_TYPE);
					operandDefaultValuesJSONArray = havingJSON.optJSONArray(QuerySerializationConstants.FILTER_RO_DEFAULT_VALUE);
					operandLasDefaulttValues = JSONUtils.asStringArray(operandDefaultValuesJSONArray);
					operandLastValuesJSONArray = havingJSON.optJSONArray(QuerySerializationConstants.FILTER_RO_LAST_VALUE);
					operandLastValues = JSONUtils.asStringArray(operandLastValuesJSONArray);
					operandFunction = havingJSON.getString(QuerySerializationConstants.FILTER_RO_FUNCTION);
					function = AggregationFunctions.get(operandFunction);
					rightOperand = new HavingField.Operand(operandValues, operandDescription, operandType, operandLasDefaulttValues, operandLastValues,
							function);

					booleanConnector = havingJSON.getString(QuerySerializationConstants.FILTER_BOOLEAN_CONNETOR);

					Assert.assertTrue(!StringUtilities.isEmpty(operator), "Undefined operator for filter: " + havingJSON.toString());
					Assert.assertTrue(!"NONE".equalsIgnoreCase(operator), "Undefined operator NONE for filter: " + havingJSON.toString());

					query.addHavingField(filterId, filterDescription, promptable, leftOperand, operator, rightOperand, booleanConnector);

				} catch (JSONException e) {
					throw new SerializationException("An error occurred while deserializing filter [" + havingsJOSN.toString() + "] of query [" + query.getId()
							+ "]", e);
				}

			}
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while deserializing filters of query [" + query.getId() + "]", t);
		} finally {
			logger.debug("OUT");
		}

	}

	/*
	 * private void deserializeFilters(JSONArray filtersJOSN, DataMartModel datamartModel, Query query) throws SerializationException {
	 *
	 * JSONObject filterJSON; DataMartField field; String fname; String fdesc; String fieldUniqueName; String operator; String operand; boolean isFree; String
	 * operandDesc; String operandType; String boperator; String defaultValue; String lastValue;
	 *
	 * logger.debug("IN");
	 *
	 * try {
	 *
	 * logger.debug("Query [" + query.getId() + "] have [" + filtersJOSN.length() + "] to deserialize"); for(int i = 0; i < filtersJOSN.length(); i++) {
	 *
	 * try { filterJSON = filtersJOSN.getJSONObject(i); fieldUniqueName = filterJSON.getString(SerializationConstants.FILTER_ID); field =
	 * datamartModel.getDataMartModelStructure().getField(fieldUniqueName); Assert.assertNotNull(field, "Impossible to load a field named [" + fieldUniqueName +
	 * "] from datamart"); } catch (JSONException e) { throw new SerializationException("An error occurred while filter [" + filtersJOSN.toString() +
	 * "] of query [" + query.getId() + "]", e); }
	 *
	 * try { fname = filterJSON.getString(SerializationConstants.FILTER_NAME); fdesc = filterJSON.getString( SerializationConstants.FILTER_NAME);
	 *
	 * operator = filterJSON.getString(SerializationConstants.FILTER_OPEARTOR); operand = filterJSON.getString(SerializationConstants.FILTER_OPEARND); isFree =
	 * filterJSON.getBoolean(SerializationConstants.FILTER_IS_FREE); operandDesc = filterJSON.getString(SerializationConstants.FILTER_OPEARND_DESCRIPTION);
	 * operandType = filterJSON.getString(SerializationConstants.FILTER_OPEARND_TYPE); boperator =
	 * filterJSON.getString(SerializationConstants.FILTER_BOOLEAN_CONNETOR); defaultValue = filterJSON.getString(SerializationConstants.FILTER_DEFAULT_VALUE);
	 * lastValue = filterJSON.getString(SerializationConstants.FILTER_LAST_VALUE); } catch (JSONException e) { throw new SerializationException(
	 * "An error occurred while filter [" + filtersJOSN.toString() + "] of query [" + query.getId() + "]", e); }
	 *
	 *
	 * Assert.assertTrue(!StringUtilities.isEmpty(operator), "Undefined operator for filter: " + filterJSON.toString());
	 * Assert.assertTrue(!"NONE".equalsIgnoreCase(operator), "Undefined operator NONE for filter: " + filterJSON.toString());
	 *
	 *
	 * query.addWhereFiled(fname, fdesc,field.getUniqueName(), operator, operand, operandType, operandDesc, boperator, isFree, defaultValue, lastValue); } }
	 * catch(Throwable t) { throw new SerializationException("An error occurred while deserializing filters of query [" + query.getId() +"]", t); } finally {
	 * logger.debug("OUT"); }
	 *
	 * }
	 */

	private void deserializeExpression(JSONObject expressionJOSN, IDataSource dataSource, Query query) throws SerializationException {
		ExpressionNode filterExp;

		// start recursion
		filterExp = getFilterExpTree(expressionJOSN);

		query.setWhereClauseStructure(filterExp);
	}

	public static ExpressionNode getFilterExpTree(JSONObject nodeJSON) throws SerializationException {
		ExpressionNode node = null;
		String nodeType;
		String nodeValue;
		JSONArray childNodesJSON;

		if (nodeJSON.has("type") && nodeJSON.has("value")) {
			try {
				nodeType = nodeJSON.getString("type");
				nodeValue = nodeJSON.getString("value");
				node = new ExpressionNode(nodeType, nodeValue);

				childNodesJSON = nodeJSON.getJSONArray("childNodes");
				for (int i = 0; i < childNodesJSON.length(); i++) {
					JSONObject childNodeJSON = childNodesJSON.getJSONObject(i);
					node.addChild(getFilterExpTree(childNodeJSON));
				}
			} catch (JSONException e) {
				throw new SerializationException("An error occurred while deserializing where clause structure: " + nodeJSON.toString(), e);
			}
		}
		return node;
	}

	private static String getFilterExpAsString(ExpressionNode filterExp) {
		String str = "";

		String type = filterExp.getType();
		if ("NODE_OP".equalsIgnoreCase(type)) {
			for (int i = 0; i < filterExp.getChildNodes().size(); i++) {
				ExpressionNode child = (ExpressionNode) filterExp.getChildNodes().get(i);
				String childStr = getFilterExpAsString(child);
				if ("NODE_OP".equalsIgnoreCase(child.getType())) {
					childStr = "(" + childStr + ")";
				}
				str += (i == 0 ? "" : " " + filterExp.getValue());
				str += " " + childStr;
			}
		} else {
			str += filterExp.getValue();
		}

		return str;
	}

	private IModelEntity getTemporalDimension(IDataSource dataSource) {
		IModelEntity temporalDimension = null;
		Iterator<String> it = dataSource.getModelStructure().getModelNames().iterator();
		while (it.hasNext()) {
			String modelName = it.next();
			List<IModelEntity> rootEntities = dataSource.getModelStructure().getRootEntities(modelName);
			for (IModelEntity bc : rootEntities) {
				if ("temporal_dimension".equals(bc.getProperty("type"))) {
					temporalDimension = bc;
					break;
				}
			}
		}
		return temporalDimension;
	}

	private IModelEntity getTimeDimension(IDataSource dataSource) {
		IModelEntity timeDimension = null;
		Iterator<String> it = dataSource.getModelStructure().getModelNames().iterator();
		while (it.hasNext()) {
			String modelName = it.next();
			List<IModelEntity> rootEntities = dataSource.getModelStructure().getRootEntities(modelName);
			for (IModelEntity bc : rootEntities) {
				if ("time_dimension".equals(bc.getProperty("type"))) {
					timeDimension = bc;
					break;
				}
			}
		}
		return timeDimension;
	}

}
