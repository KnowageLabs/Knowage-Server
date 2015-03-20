/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.formviewer;

import it.eng.qbe.query.CriteriaConstants;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.query.ExpressionNode;
import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.serializer.json.QueryJSONDeserializer;
import it.eng.qbe.query.transformers.AbstractQbeQueryTransformer;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.spagobi.engines.qbe.bo.FormViewerState;
import it.eng.spagobi.engines.qbe.template.QbeJSONTemplateParser;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Transforms the input query starting from document template and form viewer state 
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class FormViewerQueryTransformer extends AbstractQbeQueryTransformer {

	public static transient Logger logger = Logger.getLogger(FormViewerQueryTransformer.class);
	
	private JSONObject template;
	private JSONObject formState;
	private FormViewerState formViewerState = null;
	
	public JSONObject getFormState() {
		return formState;
	}

	public void setFormState(JSONObject formState) {
		this.formState = formState;
	}

	public JSONObject getTemplate() {
		return template;
	}

	public void setTemplate(JSONObject template) {
		this.template = template;
	}

	@Override
	public Query execTransformation(Query query) {
		logger.debug("IN");
		try {
			formViewerState = new FormViewerState(formState);
			applyStaticClosedFilters(query);
			applyStaticOpenFilters(query);
			applyDynamicFilters(query);
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException("Error while trasforming query" ,e);
		} finally {
			logger.debug("OUT");
		}
		return query;
	}

	private void applyStaticClosedFilters(Query query) throws Exception {
		logger.debug("IN");
		JSONArray staticClosedFilters = template.optJSONArray(QbeJSONTemplateParser.STATIC_CLOSED_FILTERS);
		if (staticClosedFilters != null && staticClosedFilters.length() > 0) {
			for (int i = 0; i < staticClosedFilters.length(); i++) {
				JSONObject aStaticClosedFilter = (JSONObject) staticClosedFilters.get(i);
				boolean isSingleSelection = aStaticClosedFilter.optBoolean(QbeJSONTemplateParser.STATIC_CLOSED_FILTER_SINGLE_SELECTION);
				String filterGroupId = aStaticClosedFilter.getString(QbeJSONTemplateParser.ID);
				if (isSingleSelection) {
					String optionId = formViewerState.getXORFilterSelectedOption(filterGroupId);
					if (optionId != null && !optionId.trim().equals("") && !optionId.equalsIgnoreCase(QbeJSONTemplateParser.STATIC_CLOSED_FILTER_NO_SELECTION)) {
						JSONObject option = null;
						JSONArray options = aStaticClosedFilter.getJSONArray(QbeJSONTemplateParser.OPTIONS);
						for (int j = 0; j < options.length(); j++) {
							JSONObject temp = options.getJSONObject(j);
							if (temp.getString(QbeJSONTemplateParser.ID).equals(optionId)) {
								option = temp;
								break;
							}
						}
						if (option != null) {
							ExpressionNode node = applyStaticClosedFilterToWhereClause(query, option);
							updateWhereClauseStructure(query, node, "AND");
						}
					}
				} else {
					JSONArray options = aStaticClosedFilter.getJSONArray(QbeJSONTemplateParser.OPTIONS);
					List<ExpressionNode> nodes = new ArrayList<ExpressionNode>();
					for (int j = 0; j < options.length(); j++) {
						JSONObject option = options.getJSONObject(j);
						boolean isActive = formViewerState.isOnOffFilterActive(filterGroupId, option.getString(QbeJSONTemplateParser.ID));
						if (isActive) {
							ExpressionNode node = applyStaticClosedFilterToWhereClause(query, option);
							nodes.add(node);
						}
					}
					if (!nodes.isEmpty()) {
						String booleanConnector = aStaticClosedFilter.getString("booleanConnector");
						updateWhereClauseStructure(query, nodes, booleanConnector, "AND");
					}
				}
			}
			
		}
		logger.debug("OUT");
	}
	
	
	private ExpressionNode applyStaticClosedFilterToWhereClause(Query query, JSONObject option) throws Exception {
		JSONArray filters = option.getJSONArray("filters");
		// adding filters for the selected option
		for (int i = 0; i < filters.length(); i++) {
			JSONObject filter = filters.getJSONObject(i);
			WhereField.Operand leftOperand = new WhereField.Operand(new String[] {filter.getString("leftOperandValue")}, null, AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD, null, null);
			WhereField.Operand rightOperand = null;
			if (filter.optString("rightOperandValue") != null) {
				logger.debug(filter.toString(3));
				String type = filter.optString("rightOperandType");
				String defType = AbstractStatement.OPERAND_TYPE_STATIC;
				if(type == null || type.equalsIgnoreCase("")) {
					type = defType;
				}
				JSONArray rightOperandValuesJSON = filter.getJSONArray("rightOperandValue");
				String[] rightOperandValues = JSONUtils.asStringArray(rightOperandValuesJSON);
				rightOperand = new WhereField.Operand(rightOperandValues, null, type, null, null);
			}
			query.addWhereField(filter.getString("id"), null, false, leftOperand, filter.getString("operator"), rightOperand, filter.getString("booleanConnector"));
		}
		// updating where clause structure: the new condition must be added with AND boolean connector
		JSONObject expression = option.getJSONObject("expression");
		ExpressionNode node = QueryJSONDeserializer.getFilterExpTree(expression);
		return node;
	}
	
	private void updateWhereClauseStructure(Query query, String filterId,
			String booleanConnector) {
		ExpressionNode node = query.getWhereClauseStructure();
		ExpressionNode newFilterNode = new ExpressionNode("NODE_CONST", "$F{" + filterId + "}");
		if (node == null) {
			node = newFilterNode;
			query.setWhereClauseStructure(node);
		} else {
			if (node.getType() == "NODE_OP" && node.getValue().equals(booleanConnector)) {
				node.addChild(newFilterNode);
			} else {
				ExpressionNode newNode = new ExpressionNode("NODE_OP", booleanConnector);
				newNode.addChild(node);
				newNode.addChild(newFilterNode);
				query.setWhereClauseStructure(newNode);
			}
		}
	}

	private void updateWhereClauseStructure(Query query, ExpressionNode nodeToInsert,
			String booleanConnector) {
		ExpressionNode node = query.getWhereClauseStructure();
		if (node == null) {
			node = nodeToInsert;
			query.setWhereClauseStructure(node);
		} else {
			ExpressionNode newNode = new ExpressionNode("NODE_OP", booleanConnector);
			newNode.addChild(node);
			newNode.addChild(nodeToInsert);
			query.setWhereClauseStructure(newNode);
		}
	}
	
	private void updateWhereClauseStructure(Query query, List<ExpressionNode> list,
			String booleanConnectorBetweenNodes, String booleanConnector) {
		ExpressionNode node = query.getWhereClauseStructure();
		ExpressionNode nodeToInsert = new ExpressionNode("NODE_OP", booleanConnectorBetweenNodes);
		Iterator<ExpressionNode> it = list.iterator();
		while (it.hasNext()) {
			nodeToInsert.addChild(it.next());
		}
		if (node == null) {
			node = nodeToInsert;
			query.setWhereClauseStructure(node);
		} else {
			ExpressionNode newNode = new ExpressionNode("NODE_OP", booleanConnector);
			newNode.addChild(node);
			newNode.addChild(nodeToInsert);
			query.setWhereClauseStructure(newNode);
		}
	}
	
	private void applyStaticOpenFilters(Query query) throws Exception {
		logger.debug("IN");
		JSONArray staticOpenFilters = template.optJSONArray(QbeJSONTemplateParser.STATIC_OPEN_FILTERS);
		if (staticOpenFilters != null && staticOpenFilters.length() > 0) {
			for (int i = 0; i < staticOpenFilters.length(); i++) {
				JSONObject filter = (JSONObject) staticOpenFilters.get(i);
				String id = filter.getString(QbeJSONTemplateParser.ID);
				List<String> values = formViewerState.getOpenFilterValues(id);
				if (values.size() > 0) {
					String operator = filter.getString("operator");
					if (operator.equals(CriteriaConstants.EQUALS_TO)) {
						operator = CriteriaConstants.IN;
					}
					WhereField.Operand leftOperand = new WhereField.Operand(new String[] {filter.getString("field")}, null, AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD, null, null);
					WhereField.Operand rightOperand = new WhereField.Operand(values.toArray(new String[]{}), null, AbstractStatement.OPERAND_TYPE_STATIC, null, null);
					query.addWhereField(id, null, false, leftOperand, operator, rightOperand, "AND");
					updateWhereClauseStructure(query, filter.getString(QbeJSONTemplateParser.ID), "AND");
				}
			}
		}
		logger.debug("OUT");
	}

	private void applyDynamicFilters(Query query) throws Exception {
		logger.debug("IN");
		JSONArray dynamicFilters = template.optJSONArray(QbeJSONTemplateParser.DYNAMIC_FILTERS);
		if (dynamicFilters != null && dynamicFilters.length() > 0) {
			for (int i = 0; i < dynamicFilters.length(); i++) {
				JSONObject filter = (JSONObject) dynamicFilters.get(i);
				String id = filter.getString(QbeJSONTemplateParser.ID);
				String field = formViewerState.getDynamicFilterField(id);
				if (field != null && !field.trim().equals("")) {
					List fields = query.getSimpleSelectFields(false);
					SimpleSelectField selectField = null;
					Iterator it = fields.iterator();
					while (it.hasNext()) {
						selectField = (SimpleSelectField) it.next();
						if (selectField.getUniqueName().equals(field)) {
							break;
						}
					}
					if (selectField != null) {
						if (selectField.getFunction() != null && selectField.getFunction() != AggregationFunctions.NONE_FUNCTION) {
							logger.debug("Applying having filter to field " + selectField.getUniqueName());
							addHavingFilter(query, selectField, filter);
						} else {
							logger.debug("Applying where filter to field " + selectField.getUniqueName());
							addWhereFilter(query, field, filter);
						}
					} else {
						logger.error("Field " + field + " not found on query selected fields");
					}
				}
			}
		}
		logger.debug("OUT");
	}
	
	private void addWhereFilter(Query query, String field, JSONObject filter) throws Exception {
		String id = filter.getString(QbeJSONTemplateParser.ID);
		WhereField.Operand leftOperand = new WhereField.Operand(new String[] {field}, null, AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD, null, null);
		String operator = filter.getString(QbeJSONTemplateParser.OPERATOR);
		WhereField.Operand rightOperand = null;
		if (operator.equalsIgnoreCase("BETWEEN")) {
			List<String> fromToValues = formViewerState.getDynamicFilterFromToValues(id);
			rightOperand = new WhereField.Operand(new String[] {fromToValues.get(0), fromToValues.get(1)}, null, AbstractStatement.OPERAND_TYPE_STATIC, null, null);
		} else {
			String value = formViewerState.getDynamicFilterValue(id);
			rightOperand = new WhereField.Operand(new String[] {value}, null, AbstractStatement.OPERAND_TYPE_STATIC, null, null);
		}
		query.addWhereField(id, null, false, leftOperand, filter.getString("operator"), rightOperand, "AND");
		updateWhereClauseStructure(query, id, "AND");
	}
	
	private void addHavingFilter(Query query, SimpleSelectField field, JSONObject filter) throws Exception {
		String id = filter.getString(QbeJSONTemplateParser.ID);
		HavingField.Operand leftOperand = new HavingField.Operand(new String[] {field.getUniqueName()}, null, AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD, null, null, field.getFunction());
		String operator = filter.getString(QbeJSONTemplateParser.OPERATOR);
		HavingField.Operand rightOperand = null;
		if (operator.equalsIgnoreCase("BETWEEN")) {
			List<String> fromToValues = formViewerState.getDynamicFilterFromToValues(id);
			rightOperand = new HavingField.Operand(new String[] {fromToValues.get(0), fromToValues.get(1)}, null, AbstractStatement.OPERAND_TYPE_STATIC, null, null, AggregationFunctions.NONE_FUNCTION);
		} else {
			String value = formViewerState.getDynamicFilterValue(id);
			rightOperand = new HavingField.Operand(new String[] {value}, null, AbstractStatement.OPERAND_TYPE_STATIC, null, null, AggregationFunctions.NONE_FUNCTION);
		}
		query.addHavingField(id, null, false, leftOperand, filter.getString("operator"), rightOperand, "AND");
	}

}
