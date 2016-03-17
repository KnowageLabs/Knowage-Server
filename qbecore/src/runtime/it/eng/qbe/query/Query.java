/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.qbe.query;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.qbe.statement.StatementCompositionException;
import it.eng.qbe.statement.StatementTockenizer;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.spagobi.tools.dataset.common.query.IQuery;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class Query implements IQuery {
	String id;
	String name;
	String description;

	boolean distinctClauseEnabled;

	List<ISelectField> selectFields;
	List<WhereField> whereClause;
	List<HavingField> havingClause;

	ExpressionNode whereClauseStructure;
	boolean nestedExpression;

	Map whereFieldMap;
	Map havingFieldMap;

	Query parentQuery;
	Map subqueries;

	QueryGraph graph;
	String relationsRoles;

	Map<IModelEntity, Map<String, List<String>>> mapEntityRoleField;

	private Map<String, Map<String, String>> inlineFilteredSelectFields;
	private Set<String> aliasesToBeRemovedAfterExecution;
	private Set<String> temporalFieldTypesInSelect;
	private Map<String, String> hierarchyFullColumnMap;
	private int relativeYearIndex;
	private LinkedList<String> allYearsOnDWH;
	private Map<String, List<String>> distinctPeriods;
	private Map<String, String> currentPeriodValuyesByType;

	public Query() {
		selectFields = new LinkedList(); /* modified by: (danilo.ristovski@mht.net) */
		whereClause = new ArrayList();
		havingClause = new ArrayList();
		whereFieldMap = new HashMap();
		havingFieldMap = new HashMap();
		subqueries = new HashMap();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRelationsRoles() {
		return relationsRoles;
	}

	public void setRelationsRoles(String relationsRoles) {
		this.relationsRoles = relationsRoles;
	}

	public Map<IModelEntity, Map<String, List<String>>> getMapEntityRoleField(IDataSource datasource) {
		if (mapEntityRoleField == null) {
			mapEntityRoleField = new HashMap<IModelEntity, Map<String, List<String>>>();
			try {
				initFieldsRolesMapInEntity(datasource);
			} catch (Exception e) {
				throw new SpagoBIEngineRuntimeException("Error parsing the roles of the query");
			}
		}
		return mapEntityRoleField;
	}

	public Map<IModelEntity, List<String>> getEntityFieldFromRoleMap(IDataSource datasource) {
		Map<IModelEntity, Map<String, List<String>>> mapEntityRoleField = getMapEntityRoleField(datasource);
		Map<IModelEntity, List<String>> entityFieldFromRoleMap = new HashMap<IModelEntity, List<String>>();

		Iterator<IModelEntity> mapEntityRoleFieldIterator = mapEntityRoleField.keySet().iterator();

		while (mapEntityRoleFieldIterator.hasNext()) {
			IModelEntity iModelEntity = mapEntityRoleFieldIterator.next();
			List<String> fieldsAliasList = new ArrayList<String>();
			Collection<List<String>> rolesFieldsMap = mapEntityRoleField.get(iModelEntity).values();
			if (rolesFieldsMap != null) {
				Iterator<List<String>> rolesFieldsMapIterator = rolesFieldsMap.iterator();
				while (rolesFieldsMapIterator.hasNext()) {
					List<java.lang.String> list = rolesFieldsMapIterator.next();
					fieldsAliasList.addAll(list);
				}
			}
			entityFieldFromRoleMap.put(iModelEntity, fieldsAliasList);
		}

		return entityFieldFromRoleMap;
	}

	public static Set<IModelEntity> getQueryEntities(Set<IModelField> mf) {
		Set<IModelEntity> me = new HashSet<IModelEntity>();
		Iterator<IModelField> mfi = mf.iterator();
		while (mfi.hasNext()) {
			IModelField iModelField = mfi.next();
			me.add(iModelField.getParent());

		}
		return me;
	}

	public void initFieldsRolesMapInEntity(IDataSource datasource) throws JSONException {
		Map<IModelField, Set<IQueryField>> modelFieldsMap = getQueryFields(datasource);
		Set<IModelField> modelFields = modelFieldsMap.keySet();
		Set<IModelEntity> modelEntities = getQueryEntities(modelFields);
		initFieldsRolesMapInEntity(modelEntities, datasource);
	}

	/**
	 * For each entity creates a property taht contains the map role-->fields associated to that role
	 *
	 * @param serializedEntityRoles
	 * @param modelEntities
	 * @throws JSONException
	 */
	public void initFieldsRolesMapInEntity(Set<IModelEntity> modelEntities, IDataSource datasource) throws JSONException {
		JSONObject serializedEntityRoles = null;
		if (relationsRoles != null && !relationsRoles.equals("") && !relationsRoles.equals("[]")) {
			serializedEntityRoles = new JSONObject(relationsRoles);
		}
		if (serializedEntityRoles != null && modelEntities != null && serializedEntityRoles.getJSONArray("entities") != null) {
			JSONArray serializedEntityRolesArray = serializedEntityRoles.getJSONArray("entities");
			for (int k = 0; k < serializedEntityRolesArray.length(); k++) {
				JSONArray serializedFieldsRoles = serializedEntityRolesArray.getJSONArray(k);
				for (int i = 0; i < serializedFieldsRoles.length(); i++) {
					JSONObject serializedRole = serializedFieldsRoles.getJSONObject(i);

					// JSONObject entity = serializedRole.getJSONObject("entity");
					String role = serializedRole.getString("role");
					JSONArray fields = serializedRole.getJSONArray("fields");

					if (fields.length() > 0) {
						IModelField datamartField = datasource.getModelStructure().getField(fields.getJSONObject(0).getString("id"));
						IModelEntity me = datamartField.getParent();

						Map<String, List<String>> mapRoleField = getMapEntityRoleField(datasource).get(me);
						if (mapRoleField == null) {
							mapRoleField = new HashMap<String, List<String>>();
						}

						List<String> fieldsForRole = new ArrayList<String>();

						for (int j = 0; j < fields.length(); j++) {
							JSONObject field = fields.getJSONObject(j);
							String fieldId = field.getString("queryFieldAlias");
							fieldsForRole.add(fieldId);
						}

						mapRoleField.put(role, fieldsForRole);
						getMapEntityRoleField(datasource).put(me, mapRoleField);
					}
				}
			}
		}
	}

	public Set<String> getEntityRoleAlias(IModelEntity entity, IDataSource datasource) {
		Map<String, List<String>> roleAliasMap = getMapEntityRoleField(datasource).get(entity);
		Set<String> roleAlias = null;
		if (roleAliasMap != null) {
			roleAlias = roleAliasMap.keySet();
		}
		return roleAlias;
	}

	public void setMapEntityRoleField(Map<IModelEntity, Map<String, List<String>>> mapEntityRoleField) {
		this.mapEntityRoleField = mapEntityRoleField;
	}

	public boolean isEmpty() {
		int selectedFieldsCount;
		List fields, calculatedFields, inlineCalculatedFields;

		fields = getSimpleSelectFields(true);
		Assert.assertNotNull(fields, "getDataMartSelectFields method cannot return a null value");
		calculatedFields = getCalculatedSelectFields(true);
		Assert.assertNotNull(fields, "getCalculatedSelectFields method cannot return a null value");
		inlineCalculatedFields = getInLineCalculatedSelectFields(true);
		Assert.assertNotNull(fields, "getInLineCalculatedSelectFields method cannot return a null value");

		selectedFieldsCount = fields.size() + calculatedFields.size() + inlineCalculatedFields.size();

		return (selectedFieldsCount == 0);
	}

	@Override
	public void addSelectFiled(String fieldUniqueName, String function, String fieldAlias, boolean include, boolean visible, boolean groupByField,
			String orderType, String pattern) {
		selectFields.add(new SimpleSelectField(fieldUniqueName, function, fieldAlias, include, visible, groupByField, orderType, pattern, null, null));
	}

	/**
	 * Extend the method by the 'orderColumn' parameter, that is now dynamic (not fixed) and it is just temporarily enabled only for the first category in the
	 * chart. Ordering column is the attribute (column) that user can pick from the set of all available attributes that are provided by the used dataset.
	 *
	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	@Override
	public void addSelectFiled(String fieldUniqueName, String function, String fieldAlias, boolean include, boolean visible, boolean groupByField,
			String orderType, String pattern, String orderColumn) {
		selectFields.add(new SimpleSelectField(fieldUniqueName, function, fieldAlias, include, visible, groupByField, orderType, pattern, null, null,
				orderColumn));
	}

	/**
	 * Extend the method by the 'orderColumn' parameter, that is now dynamic (not fixed) and it is just temporarily enabled only for the first category in the
	 * chart. Ordering column is the attribute (column) that user can pick from the set of all available attributes that are provided by the used dataset.
	 *
	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	public void addSelectFiled(String fieldUniqueName, String function, String fieldAlias, boolean include, boolean visible, boolean groupByField,
			String orderType, String pattern, String temporalOperand, String temporalOperandParameter) {
		selectFields.add(new SimpleSelectField(fieldUniqueName, function, fieldAlias, include, visible, groupByField, orderType, pattern, temporalOperand,
				temporalOperandParameter));
	}

	public void addSelectFiled(SimpleSelectField timeIdField) {
		this.selectFields.add(timeIdField);
	}

	public void addCalculatedFiled(String fieldAlias, String expression, String type, boolean included, boolean visible) {
		selectFields.add(new CalculatedSelectField(fieldAlias, expression, type, included, visible));
	}

	public void addInLineCalculatedFiled(String fieldAlias, String expression, String slots, String type, String nature, boolean included, boolean visible,
			boolean groupByField, String orderType, String funct) {
		selectFields.add(new InLineCalculatedSelectField(fieldAlias, expression, slots, type, nature, included, visible, groupByField, orderType, funct));
	}

	public WhereField addWhereField(String name, String description, boolean promptable, it.eng.qbe.query.WhereField.Operand leftOperand, String operator,
			it.eng.qbe.query.WhereField.Operand rightOperand, String booleanConnector) {
		return addWhereField(name, description, promptable, leftOperand, operator, rightOperand, booleanConnector, null);
	}

	public WhereField addWhereField(String name, String description, boolean promptable, it.eng.qbe.query.WhereField.Operand leftOperand, String operator,
			it.eng.qbe.query.WhereField.Operand rightOperand, String booleanConnector, JSONObject temporalOperand) {

		WhereField whereField = new WhereField(name, description, promptable, leftOperand, operator, rightOperand, booleanConnector, temporalOperand);

		whereClause.add(whereField);
		whereFieldMap.put("$F{" + name + "}", whereField);
		return whereField;
	}

	@Override
	public void addWhereField(String name, String description, boolean promptable, String[] leftOperatorValues, String leftOperatorDescription,
			String leftOperatorType, String[] leftOperatorDefaulttValues, String[] leftOperatorLastValues, String leftOperatorAlias, String operator,
			String[] rightOperatorValues, String rightOperatorDescription, String rightOperatorType, String[] rightOperatorDefaulttValues,
			String[] rightOperatorLastValues, String rightOperatorAlias, String booleanConnector) {
		it.eng.qbe.query.WhereField.Operand leftOperand = new it.eng.qbe.query.WhereField.Operand(leftOperatorValues, leftOperatorDescription,
				leftOperatorType, leftOperatorDefaulttValues, leftOperatorLastValues, leftOperatorAlias);
		it.eng.qbe.query.WhereField.Operand rightOperand = new it.eng.qbe.query.WhereField.Operand(rightOperatorValues, rightOperatorDescription,
				rightOperatorType, rightOperatorDefaulttValues, rightOperatorLastValues, rightOperatorAlias);
		WhereField whereField = new WhereField(name, description, promptable, leftOperand, operator, rightOperand, booleanConnector);

		whereClause.add(whereField);
		whereFieldMap.put("$F{" + name + "}", whereField);
	}

	public HavingField addHavingField(String name, String description, boolean promptable, it.eng.qbe.query.HavingField.Operand leftOperand, String operator,
			it.eng.qbe.query.HavingField.Operand rightOperand, String booleanConnector) {

		HavingField havingField = new HavingField(name, description, promptable, leftOperand, operator, rightOperand, booleanConnector);

		havingClause.add(havingField);
		havingFieldMap.put("$F{" + name + "}", havingField);
		return havingField;
	}

	public WhereField getWhereFieldByName(String fname) {
		return (WhereField) whereFieldMap.get(fname.trim());
	}

	public HavingField getHavingFieldByName(String fname) {
		return (HavingField) havingFieldMap.get(fname.trim());
	}

	/**
	 * @param onlyIncluded
	 *            true to return all the select fields. false to include only the select fields actually included in the select clause of the generated statemet
	 *            (i.e it is possible for a select field to be used only in 'order by' or in 'group by' clause of the statement)
	 *
	 * @return a List of all selected fields (ISelectField). All the field types are included (i.e. simple fields, calculated fields and inline calculated
	 *         fields). Never returns null. If there are no selected fields in the query it returns an empty list.
	 */
	public List<ISelectField> getSelectFields(boolean onlyIncluded) {
		List<ISelectField> fields;
		if (onlyIncluded == false) {
			fields = new ArrayList<ISelectField>(selectFields);
		} else {
			fields = new ArrayList<ISelectField>();
			for (ISelectField field : selectFields) {
				if (field.isIncluded()) {
					fields.add(field);
				}
			}
		}
		return fields;
	}

	public List getSelectSimpleFieldsByUniqueName(String uniqueName) {
		List<SimpleSelectField> matchingSimpleSelectFields;

		matchingSimpleSelectFields = new ArrayList<SimpleSelectField>();
		List<SimpleSelectField> simpleSelectFields = getSimpleSelectFields(false);
		for (SimpleSelectField simpleSelectField : simpleSelectFields) {
			if (simpleSelectField.getUniqueName().equalsIgnoreCase(uniqueName)) {
				matchingSimpleSelectFields.add(simpleSelectField);
			}
		}

		return matchingSimpleSelectFields;
	}

	public List<SimpleSelectField> getSelectSimpleFieldsByAlias(String alias) {
		List<SimpleSelectField> matchingSimpleSelectFields;

		matchingSimpleSelectFields = new ArrayList<SimpleSelectField>();
		List<SimpleSelectField> simpleSelectFields = getSimpleSelectFields(false);
		for (SimpleSelectField simpleSelectField : simpleSelectFields) {
			if (simpleSelectField.getAlias().equalsIgnoreCase(alias)) {
				matchingSimpleSelectFields.add(simpleSelectField);
			}
		}

		return matchingSimpleSelectFields;
	}

	public void removeSelectField(int fieldIndex) {
		Assert.assertTrue(fieldIndex >= 0 && fieldIndex < selectFields.size(), "Index [" + fieldIndex + "] out of bound for select fields list (0 - "
				+ selectFields.size() + ")");
		selectFields.remove(fieldIndex);
	}

	public void removeSelectField(ISelectField toRemove) {
		for (ISelectField iSelectField : this.selectFields) {
			if (iSelectField.getAlias().equals(toRemove.getAlias())) {
				selectFields.remove(iSelectField);
				break;
			}
		}
	}

	public void removeWhereField(int fieldIndex) {
		Assert.assertTrue(fieldIndex >= 0 && fieldIndex < whereClause.size(), "Index [" + fieldIndex + "] out of bound for select fields list (0 - "
				+ whereClause.size() + ")");
		whereClause.remove(fieldIndex);
	}

	public void removeHavingField(int fieldIndex) {
		Assert.assertTrue(fieldIndex >= 0 && fieldIndex < havingClause.size(), "Index [" + fieldIndex + "] out of bound for select fields list (0 - "
				+ havingClause.size() + ")");
		havingClause.remove(fieldIndex);
	}

	public ISelectField getSelectFieldByIndex(int fieldIndex) {
		Assert.assertTrue(fieldIndex >= 0 && fieldIndex < selectFields.size(), "Index [" + fieldIndex + "] out of bound for select fields list (0 - "
				+ selectFields.size() + ")");
		return selectFields.get(fieldIndex);
	}

	public int getSelectFieldIndex(String uniqueName) {
		int index;

		index = -1;

		for (int i = 0; i < selectFields.size(); i++) {
			ISelectField f = selectFields.get(i);
			if (f.isSimpleField()) {
				SimpleSelectField field = (SimpleSelectField) f;
				if (field.getUniqueName().equalsIgnoreCase(uniqueName)) {
					index = i;
					break;
				}
			}
		}

		return index;
	}

	/**
	 * Returns a list of of simple select fields (no inlineCalculatedSelectField & calculatedSelectField)
	 *
	 * @param onlyIncluded
	 *            if true the returned list will include only the simple select fields actually included in the select statement. All the simple select fields
	 *            will be returned otherwise.
	 *
	 * @return a list of SimpleSelectField. It never returns null. If there are not fields in select clause it will return an empty list.
	 */
	public List<SimpleSelectField> getSimpleSelectFields(boolean onlyIncluded) {
		List<SimpleSelectField> simpleSelectFields;

		simpleSelectFields = new ArrayList<SimpleSelectField>();
		for (ISelectField selectField : selectFields) {
			if (selectField.isSimpleField()) {
				if (onlyIncluded == false || (onlyIncluded == true && selectField.isIncluded())) {
					simpleSelectFields.add((SimpleSelectField) selectField);
				}
			}
		}

		return simpleSelectFields;
	}

	public List getCalculatedSelectFields(boolean onlyIncluded) {
		List calculatedSelectFields;
		Iterator it;
		ISelectField field;

		calculatedSelectFields = new ArrayList();
		it = getSelectFields(false).iterator();
		while (it.hasNext()) {
			field = (ISelectField) it.next();
			if (field.isCalculatedField()) {
				if (onlyIncluded == false || (onlyIncluded == true && field.isIncluded())) {
					calculatedSelectFields.add(field);
				}
			}
		}

		return calculatedSelectFields;
	}

	/**
	 * Returns the list of inline calculated fields included in select clause (no simpleSelectField & calculatedSelectField)
	 *
	 * @param onlyIncluded
	 *            if true the returned list will include only the inline calculated fields actually included in the select statement. All the inline calculated
	 *            fields will be returned otherwise.
	 *
	 * @return a list of InLineCalculatedSelectField. It never returns null. If there are not inline calculated fields in select clause it will return an empty
	 *         list.
	 */
	public List getInLineCalculatedSelectFields(boolean onlyIncluded) {
		List<InLineCalculatedSelectField> inLineCalculatedSelectFields;
		List<ISelectField> selectFields;

		selectFields = getSelectFields(false);
		inLineCalculatedSelectFields = new ArrayList<InLineCalculatedSelectField>();

		for (ISelectField field : selectFields) {
			if (field.isInLineCalculatedField()) {
				if (onlyIncluded == false || (onlyIncluded == true && field.isIncluded())) {
					inLineCalculatedSelectFields.add((InLineCalculatedSelectField) field);
				}
			}
		}

		return inLineCalculatedSelectFields;
	}

	public List<WhereField> getWhereFields() {
		return whereClause;
	}

	public List<HavingField> getHavingFields() {
		return havingClause;
	}

	@Override
	public boolean isDistinctClauseEnabled() {
		return distinctClauseEnabled;
	}

	@Override
	public void setDistinctClauseEnabled(boolean distinctClauseEnabled) {
		this.distinctClauseEnabled = distinctClauseEnabled;
	}

	/**
	 * Get all the fields in order by clause (i.e. SimpleSelectField + InLineCalculatedSelectedField). Note: CalculatedField cannot be used in order by clause.
	 * If some CalculateField has been erroneously added to order by clause it will be ignored by this method.
	 *
	 * @return The list of ISelectField included in order by clause (except CalculatedSelectField). It never returns null. If there are not fields in order by
	 *         clause it will return an empty list.
	 */
	public List<ISelectField> getOrderByFields() {
		List<ISelectField> orderByFields = new ArrayList<ISelectField>();
		List<ISelectField> selectFields = new ArrayList<ISelectField>();

		List<SimpleSelectField> simpleSelectField = getSimpleSelectFields(false);
		selectFields.addAll(simpleSelectField);

		List<SimpleSelectField> inlineCalculatedSelectField = this.getInLineCalculatedSelectFields(false);
		selectFields.addAll(inlineCalculatedSelectField);

		for (ISelectField selectField : selectFields) {
			if (selectField.isOrderByField()) {
				orderByFields.add(selectField);
			}
		}

		return orderByFields;
	}

	public List<ISelectField> getGroupByFields() {
		List<ISelectField> groupByFields = new ArrayList();
		Iterator it = this.getSimpleSelectFields(false).iterator();
		while (it.hasNext()) {
			SimpleSelectField selectField = (SimpleSelectField) it.next();
			if (selectField.isGroupByField()) {
				groupByFields.add(selectField);
			}
		}

		Iterator<InLineCalculatedSelectField> it2 = this.getInLineCalculatedSelectFields(false).iterator();
		while (it2.hasNext()) {
			InLineCalculatedSelectField selectField = it2.next();
			if (selectField.isGroupByField()) {
				groupByFields.add(selectField);
			}
		}

		return groupByFields;
	}

	public ExpressionNode getWhereClauseStructure() {
		return whereClauseStructure;
	}

	public void setWhereClauseStructure(ExpressionNode whereClauseStructure) {
		this.whereClauseStructure = whereClauseStructure;
	}

	/*
	 * true iff it is an expression built using the client side expression wizard
	 */
	public boolean isNestedExpression() {
		return nestedExpression;
	}

	public void setNestedExpression(boolean nestedExpression) {
		this.nestedExpression = nestedExpression;
	}

	public Query getParentQuery() {
		return parentQuery;
	}

	public void setParentQuery(Query parentQuery) {
		this.parentQuery = parentQuery;
	}

	public boolean hasParentQuery() {
		return getParentQuery() != null;
	}

	public void addSubquery(Query subquery) {
		subqueries.put(subquery.getId(), subquery);
		subquery.setParentQuery(this);
	}

	public Query getSubquery(String id) {
		return (Query) subqueries.get(id);
	}

	public Set getSubqueryIds() {
		return new HashSet(subqueries.keySet());
	}

	public Query removeSubquery(String id) {
		Query subquery = (Query) subqueries.remove(id);
		if (subquery != null)
			subquery.setParentQuery(null);
		return subquery;
	}

	public void clearSelectedFields() {
		if (selectFields != null) {
			selectFields.clear();
		}
	}

	public void clearWhereFields() {
		if (whereClause != null) {
			whereClause.clear();
		}
		if (whereFieldMap != null) {
			whereFieldMap.clear();
		}
		whereClauseStructure = null;
	}

	public void clearHavingFields() {
		if (havingClause != null) {
			havingClause.clear();
		}
		if (havingFieldMap != null) {
			havingFieldMap.clear();
		}
	}

	public void setQueryGraph(QueryGraph graph) {
		this.graph = graph;
	}

	public QueryGraph getQueryGraph() {
		return graph;
	}

	// public Set<IModelField> getQueryModelFields(IDataSource dataSource){
	// Map<IModelField, Set<IQueryField>> mf = getQueryFields(dataSource);
	// return (mf.keySet());
	// }

	public Map<IModelField, Set<IQueryField>> getQueryFields(IDataSource dataSource) {
		Map<IModelField, Set<IQueryField>> modelFieldsInvolved = new HashMap<IModelField, Set<IQueryField>>();
		getSelectIModelFields(modelFieldsInvolved, dataSource);
		getWhereIModelFields(modelFieldsInvolved, dataSource);
		return modelFieldsInvolved;
	}

	public void getSelectIModelFields(Map<IModelField, Set<IQueryField>> modelFieldsInvolved, IDataSource dataSource) {

		List<ISelectField> selectFields;

		selectFields = this.getSelectFields(true);

		for (ISelectField selectAbstractField : selectFields) {
			if (selectAbstractField.isSimpleField()) {
				IModelField datamartField = dataSource.getModelStructure().getField(((SimpleSelectField) selectAbstractField).getUniqueName());
				addFieldIntoMap(selectAbstractField, datamartField, modelFieldsInvolved);

			} else if (selectAbstractField.isInLineCalculatedField()) {
				replaceFieldsIncalculatedFields((InLineCalculatedSelectField) selectAbstractField, modelFieldsInvolved, dataSource);
			}
		}
	}

	public boolean isAliasDefinedInSelectFields() {
		List<String> checkedFields = new ArrayList<String>();
		List<ISelectField> selectFields = this.getSelectFields(true);

		for (ISelectField selectAbstractField : selectFields) {
			if (selectAbstractField.isSimpleField()) {
				String fieldToString = selectAbstractField.getName() + selectAbstractField.getNature() + selectAbstractField.getType()
						+ selectAbstractField.getAlias();
				int index = checkedFields.indexOf(fieldToString);
				if (index >= 0) {// if in the select fields there is the same field with the same alias there could be some misunderstanding
					return false;
				} else {
					checkedFields.add(fieldToString);
				}
			}
		}
		return true;
	}

	private void replaceFieldsIncalculatedFields(InLineCalculatedSelectField cf, Map<IModelField, Set<IQueryField>> modelFieldsInvolved, IDataSource dataSource) {
		IModelField modelField;

		try {
			StatementTockenizer tokenizer = new StatementTockenizer(cf.getExpression());
			while (tokenizer.hasMoreTokens()) {

				String token = tokenizer.nextTokenInStatement();

				modelField = null;
				String decodedToken = token;
				decodedToken = decodedToken.replaceAll("\\[", "(");
				decodedToken = decodedToken.replaceAll("\\]", ")");
				modelField = dataSource.getModelStructure().getField(decodedToken);

				if (modelField != null) {
					addFieldIntoMap(cf, modelField, modelFieldsInvolved);
				}
			}

		} catch (Throwable t) {
			throw new RuntimeException("An unpredicted error occurred while parsing expression [" + cf.getExpression() + "]", t);
		}

	}

	protected void getWhereIModelFields(Map<IModelField, Set<IQueryField>> modelFieldsInvolved, IDataSource dataSource) {

		try {
			addUserProvidedConditions(modelFieldsInvolved, dataSource);
		} catch (Throwable t) {
			throw new StatementCompositionException("Impossible to build where clause", t);
		}
	}

	private void addUserProvidedConditions(Map<IModelField, Set<IQueryField>> modelFieldsInvolved, IDataSource dataSource) {
		ExpressionNode filterExp = getWhereClauseStructure();

		if (filterExp != null) {
			addUserProvidedConditions(filterExp, modelFieldsInvolved, dataSource);
		}

	}

	private void addUserProvidedConditions(ExpressionNode filterExp, Map<IModelField, Set<IQueryField>> modelFieldsInvolved, IDataSource dataSource) {
		String type = filterExp.getType();
		if ("NODE_OP".equalsIgnoreCase(type)) {
			for (int i = 0; i < filterExp.getChildNodes().size(); i++) {
				ExpressionNode child = (ExpressionNode) filterExp.getChildNodes().get(i);
				addUserProvidedConditions(child, modelFieldsInvolved, dataSource);
			}
		} else {
			WhereField whereField = getWhereFieldByName(filterExp.getValue());
			addOperandCondition(whereField.getLeftOperand(), whereField, dataSource, modelFieldsInvolved);
			addOperandCondition(whereField.getRightOperand(), whereField, dataSource, modelFieldsInvolved);
		}
	}

	private void addOperandCondition(Operand operand, WhereField whereField, IDataSource dataSource, Map<IModelField, Set<IQueryField>> modelFieldsInvolved) {
		if (AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD.equalsIgnoreCase(operand.type)
				|| AbstractStatement.OPERAND_TYPE_PARENT_FIELD.equalsIgnoreCase(operand.type)) {

			IModelField datamartField = dataSource.getModelStructure().getField(operand.values[0]);
			addFieldIntoMap(whereField, datamartField, modelFieldsInvolved);
		}
	}

	private static void addFieldIntoMap(IQueryField queryField, IModelField datamartField, Map<IModelField, Set<IQueryField>> modelFieldsInvolved) {
		Set<IQueryField> queryfields = modelFieldsInvolved.get(datamartField);
		if (queryfields == null) {
			queryfields = new HashSet<IQueryField>();
			modelFieldsInvolved.put(datamartField, queryfields);
		}
		queryfields.add(queryField);

	}

	public Set<IModelEntity> getQueryEntities(IDataSource dataSource) {
		Map<IModelField, Set<IQueryField>> modelFieldsMap = getQueryFields(dataSource);
		Set<IModelField> mf = modelFieldsMap.keySet();
		Set<IModelEntity> me = new HashSet<IModelEntity>();
		Iterator<IModelField> mfi = mf.iterator();
		while (mfi.hasNext()) {
			IModelField iModelField = mfi.next();
			me.add(iModelField.getParent());

		}
		return me;
	}

	@Override
	public String toSql(String schema, String table) {

		final String whereStart = "WHERE ( ";
		final String whereEnd = ") ";
		final String whereEmpty = whereStart + whereEnd;

		String fromClause = "FROM " + schema + "." + table + " ";
		String selectClause = "SELECT ";
		String whereClause = whereStart;
		String groupByClause = "GROUP BY ";
		String groupByClauseEmpty = groupByClause;

		/**
		 * String 'orderByClause' will keep the part of the query that is required when ordering of series/categories is specified for the chart.
		 * 
		 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		String orderByClause = "ORDER BY ";
		String orderByClauseEmpty = orderByClause;

		List<ISelectField> selectFields = getSelectFields(false);
		List<WhereField> whereFields = getWhereFields();
		List<ISelectField> groupByFields = getGroupByFields();

		if (isDistinctClauseEnabled()) {
			selectClause += "DISTINCT ";
		}

		for (ISelectField select : selectFields) {
			if (select instanceof SimpleSelectField) {
				SimpleSelectField simpleField = (SimpleSelectField) select;
				String columnName = extractColumnNameFromFieldName(simpleField.getName());

				/**
				 * Set the SELECT clause (part) of the final SQL query by firstly taking the ordering column, if the one is provided and if it is not the same
				 * (of the same name) as the category for which it is set as a ordering column (attribute) (since this one (the category inside the
				 * 'simpleField') will be appended to the SELECT clause at its very end in order to form appropriate query for this purpose).
				 * 
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				String orderColumn = simpleField.getOrderColumn();

				if (orderColumn != null && orderColumn != "" && !orderColumn.equals(columnName)) {
					selectClause += orderColumn + " AS " + orderColumn + ", ";
				}

				selectClause += simpleField.getFunction().apply(columnName) + " AS " + simpleField.getAlias() + " ";

				if (selectFields.indexOf(select) != (selectFields.size() - 1)) {
					selectClause += ", ";
				}
			} else {
				throw new SpagoBIEngineRuntimeException(
						"The method toSql only supports SimpleSelectField. Do not use this method to obtain a full SQL statement.");
			}
		}

		for (WhereField where : whereFields) {
			String columnName = extractColumnNameFromFieldName(where.getLeftOperand().values[0]);
			String operator = where.getOperator();
			if (!operator.equals("EQUALS TO")) {
				throw new SpagoBIEngineRuntimeException(
						"The method toSql only supports = as filter operator. Do not use this method to obtain a full SQL statement.");
			} else {
				operator = "=";
			}
			try {
				double d = Double.parseDouble(where.getRightOperand().values[0]);
				whereClause += columnName + " " + operator + " " + where.getRightOperand().values[0] + " ";
			} catch (NumberFormatException nfe) {
				// if not a number, adding '' to the value
				whereClause += columnName + " " + operator + " '" + where.getRightOperand().values[0] + "' ";
			}
			if (whereFields.indexOf(where) != (whereFields.size() - 1)) {
				whereClause += where.getBooleanConnector() + " ";
			}
		}
		whereClause += whereEnd;

		for (ISelectField groupBy : groupByFields) {
			if (groupBy instanceof SimpleSelectField) {
				SimpleSelectField simpleField = (SimpleSelectField) groupBy;
				String columnName = extractColumnNameFromFieldName(simpleField.getName());

				/**
				 * Set the GROUP BY clause (part) of the final SQL query by firstly taking the ordering column, if the one is provided and if it is not the same
				 * (of the same name) as the category for which it is set as a ordering column (attribute) (since this one (the category inside the
				 * 'simpleField') will be appended to the GROUP BY clause at its very end in order to form appropriate query for this purpose).
				 * 
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				String orderColumn = simpleField.getOrderColumn();

				if (orderColumn != null && orderColumn != "" && !orderColumn.equals(columnName)) {
					groupByClause += orderColumn + ", ";
				}

				groupByClause += simpleField.getFunction().apply(columnName) + " ";

				if (groupByFields.indexOf(groupBy) != (groupByFields.size() - 1)) {
					groupByClause += ", ";
				}
			} else {
				throw new SpagoBIEngineRuntimeException(
						"The method toSql only supports SimpleSelectField. Do not use this method to obtain a full SQL statement.");
			}
		}

		/**
		 * allSeriesForOrdering - the string that will contain all series through which the table should be firstly ordered.
		 *
		 * allCategoriesForOrdering - the string that will be used for the ORDER BY clause of the query that we are going to construct. The ordering by
		 * potential column and the category for it comes after the ordering by the series item(s).
		 *
		 * keepCategoryForOrdering - the part of the ORDER BY clause that is associated to the first category of the document.
		 *
		 * columnAndCategoryAreTheSame - indicator if the column that is set as an ordering one for the first category is of the same value (name) as the
		 * category. In that case we will skip duplicating of columns of the table that the query should specify in the SELECT, GROUP BY and ORDER BY clause.
		 *
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		ArrayList<String> allCategoriesForOrdering = new ArrayList<String>();
		ArrayList<String> allSeriesForOrdering = new ArrayList<String>();
		String keepCategoryForOrdering = "";
		boolean columnAndCategoryAreTheSame = false;

		/**
		 * This part is added since we need to take care of ordering of the serie and/or categories of the one is provided (specified) for them.
		 * 
		 * @comment Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		for (ISelectField orderBy : this.getSelectFields(false)) {
			SimpleSelectField simpleField = (SimpleSelectField) orderBy;
			String columnName = extractColumnNameFromFieldName(simpleField.getName());

			/**
			 * Set the ORDER BY clause (part) of the final SQL query by firstly taking the ordering column, if the one is provided and if it is not the same (of
			 * the same name) as the category for which it is set as a ordering column (attribute) (since this one (the category inside the 'simpleField') will
			 * be appended to the ORDER BY clause at its very end in order to form appropriate query for this purpose).
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */

			/**
			 * Get the ordering column (column by which the results will be ordered) and its ordering type (ascending or descending) in order to provide an
			 * appropriate ORDER BY caluse.
			 */
			String orderColumn = simpleField.getOrderColumn();
			String orderType = simpleField.getOrderType();

			/**
			 * If the ordering column is specified for the category that is inspected at this moment. This part is associated to the categories (attributes,
			 * columns).
			 */
			if (orderColumn != null) {
				/**
				 * If the order column is not an empty string value (is defined).
				 */
				if (!orderColumn.equals("")) {
					/**
					 * If the ordering column is the same as the category for which is set.
					 */
					if (orderColumn.equals(columnName)) {
						columnAndCategoryAreTheSame = true;

						if (orderType.equals(""))
							allCategoriesForOrdering.add(columnName + " ASC");
						else
							allCategoriesForOrdering.add(columnName + " " + orderType);
					} else {
						if (orderType.equals(""))
							allCategoriesForOrdering.add(orderColumn + " ASC");
						else
							allCategoriesForOrdering.add(orderColumn + " " + orderType);
					}
				}

				/**
				 * If the order column is an empty string, that means that we do not have anything to which we will append the ordering by the current category
				 * (there is not ordering column). For that reason, apply the ordering for the current category.
				 */
				keepCategoryForOrdering = columnName + " ASC";

				/*------ Previous implementation -------*/
				// if (orderColumn!= "" && !orderColumn.equals(columnName))
				// {
				// if (orderType != "" && orderType != null)
				// {
				// allCategoriesForOrdering.add(simpleField.getFunction().apply(orderColumn) + " " + orderType);
				// }
				// else
				// {
				// /**
				// * If the user specifies only the column through which if wants to order the table
				// * (it does not specify the ordering type of it - ASC or DESC), use the default one,
				// * i.e. the ascending (ASC) ordering type for that column.
				// */
				// allCategoriesForOrdering.add(simpleField.getFunction().apply(orderColumn) + " ASC");
				// }
				//
				// allCategoriesForOrdering.add(columnName + " ASC");
				// }
				// else
				// {
				// /**
				// * Set the ordering type of the first category of the chart as ascending
				// * since this is the default one when drilling down.
				// */
				// String orderTemp = "";
				//
				// if (orderColumn.equals(""))
				// orderTemp = simpleField.getFunction().apply(columnName) + " ASC";
				// else if (!orderType.equals(""))
				// orderTemp = simpleField.getFunction().apply(columnName) + " " + orderType;
				//
				// allCategoriesForOrdering.add(orderTemp);
				// }

			}
			/**
			 * If the ordering column is not specified for the item that is inspected at this moment (the series item). This part is associated to the series
			 * and their ordering type.
			 */
			else if (orderType != "" && orderType != null) {
				allSeriesForOrdering.add(simpleField.getFunction().apply(columnName) + " " + orderBy.getOrderType());
			}
		}

		/**
		 * If 'keepCategoryForOrdering' is an empty string we are dealing with drilling down, so in that case we do not need nor do not have a record about the
		 * category that is set as the first category for the chart and this one should not be put at the end of the query.
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if (!columnAndCategoryAreTheSame && !keepCategoryForOrdering.equals(""))
			allCategoriesForOrdering.add(keepCategoryForOrdering);

		/**
		 * The ORDER BY clause should firstly contain the ordering by series and afterwards the ordering by category and its potentially associated column
		 * (attribute).
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */

		if (allSeriesForOrdering.size() > 0) {
			for (int i = 0; i < allSeriesForOrdering.size(); i++) {
				if (i < allSeriesForOrdering.size() - 1)
					orderByClause = orderByClause + allSeriesForOrdering.get(i) + ", ";
				else
					orderByClause = orderByClause + allSeriesForOrdering.get(i);
			}
		}

		if (!orderByClause.equals(orderByClauseEmpty) && allCategoriesForOrdering.size() > 0)
			orderByClause = orderByClause + ", ";

		if (allCategoriesForOrdering.size() > 0) {
			for (int i = 0; i < allCategoriesForOrdering.size(); i++) {
				if (i < allCategoriesForOrdering.size() - 1)
					orderByClause = orderByClause + allCategoriesForOrdering.get(i) + ", ";
				else
					orderByClause = orderByClause + allCategoriesForOrdering.get(i);
			}
		}

		String queryFinal = "";

		queryFinal = selectClause + fromClause + (whereClause.equals(whereEmpty) ? " " : whereClause)
				+ (groupByClause.equals(groupByClauseEmpty) ? " " : groupByClause) + (orderByClause.equals(orderByClauseEmpty) ? " " : orderByClause);

		return queryFinal;
	}

	private String extractColumnNameFromFieldName(String fieldName) {
		return fieldName.contains(":") ? fieldName.split(":")[1] : fieldName;
	}

	public void updateWhereClauseStructure() {
		setWhereClauseStructure(null);
		for (WhereField whereField : getWhereFields()) {
			addNodeToWhereClauseStructure(this, whereField.getName(), whereField.getBooleanConnector());
		}
	}

	private void addNodeToWhereClauseStructure(Query query, String filterId, String booleanConnector) {
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

	public void setInlineFilteredSelectFields(Map<String, Map<String, String>> inlineFilteredSelectFields) {
		this.inlineFilteredSelectFields = inlineFilteredSelectFields;
	}

	public Map<String, Map<String, String>> getInlineFilteredSelectFields() {
		return inlineFilteredSelectFields;
	}

	public void setAliasesToBeRemovedAfterExecution(Set<String> aliasesToBeRemovedAfterExecution) {
		this.aliasesToBeRemovedAfterExecution = aliasesToBeRemovedAfterExecution;
	}

	public Set<String> getAliasesToBeRemovedAfterExecution() {
		return aliasesToBeRemovedAfterExecution;
	}

	public void setTemporalFieldTypesInSelect(Set<String> temporalFieldTypesInSelect) {
		this.temporalFieldTypesInSelect = temporalFieldTypesInSelect;
	}

	public Set<String> getTemporalFieldTypesInSelect() {
		return temporalFieldTypesInSelect;
	}

	public Set<String> getTemporalFieldTypesInQuery() {
		Set<String> temporalFieldTypesInQuery = new HashSet<>();
		temporalFieldTypesInQuery.addAll(temporalFieldTypesInSelect);
		temporalFieldTypesInQuery.addAll(aliasesToBeRemovedAfterExecution);
		return temporalFieldTypesInQuery;
	}

	public void setHierarchyFullColumnMap(Map<String, String> hierarchyFullColumnMap) {
		this.hierarchyFullColumnMap = hierarchyFullColumnMap;
	}

	public Map<String, String> getHierarchyFullColumnMap() {
		return hierarchyFullColumnMap;
	}

	public void setRelativeYearIndex(int relativeYearIndex) {
		this.relativeYearIndex = relativeYearIndex;
	}

	public int getRelativeYearIndex() {
		return relativeYearIndex;
	}

	public void setAllYearsOnDWH(LinkedList<String> allYearsOnDWH) {
		this.allYearsOnDWH = allYearsOnDWH;
	}

	public LinkedList<String> getAllYearsOnDWH() {
		return allYearsOnDWH;
	}

	public void setDistinctPeriods(Map<String, List<String>> distinctPeriods) {
		this.distinctPeriods = distinctPeriods;
	}

	public Map<String, List<String>> getDistinctPeriods() {
		return distinctPeriods;
	}

	public void setCurrentPeriodValuyesByType(Map<String, String> currentPeriodValuyesByType) {
		this.currentPeriodValuyesByType = currentPeriodValuyesByType;
	}

	public Map<String, String> getCurrentPeriodValuyesByType() {
		return currentPeriodValuyesByType;
	}

}
