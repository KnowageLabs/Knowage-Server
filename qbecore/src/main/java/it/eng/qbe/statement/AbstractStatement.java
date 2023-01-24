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
package it.eng.qbe.statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.eng.qbe.datasource.AbstractDataSource;
import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.CalculatedSelectField;
import it.eng.qbe.query.IQueryField;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.InLineCalculatedSelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.query.filters.SqlFilterModelAccessModality;
import it.eng.qbe.query.serializer.json.QueryJSONSerializer;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.qbe.statement.graph.GraphManager;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.spagobi.tools.dataset.bo.DataSetVariable;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.objects.Couple;

/**
 * @author Andrea Gioia
 */
public abstract class AbstractStatement implements IStatement {

	public static final String PROPERTY_IS_SEGMENT_ATTRIBUTE = "isSegmentAttribute";
	public static final String PROPERTY_IS_MANDATORY_MEASURE = "isMandatoryMeasure";

	IDataSource dataSource;

	Query query;

	Map parameters;

	Map profileAttributes;

	String queryString;

	/** The max results. */
	int maxResults;

	/**
	 * If it is true (i.e. the maxResults limit is exceeded) then query execution should be stopped
	 */
	boolean isBlocking;

	/** The fetch size. */
	int fetchSize;

	/** The offset. */
	int offset;

	/**
	 * Instantiates a new basic statement.
	 *
	 * @param dataMartModel the data mart model
	 */
	protected AbstractStatement(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Create a new statement from query bound to the specific datamart-model
	 *
	 * @param dataMartModel the data mart model
	 * @param query         the query
	 */
	protected AbstractStatement(IDataSource dataSource, Query query) {
		this.dataSource = dataSource;
		this.query = query;
		updateQueryQraph();
	}

	private void updateQueryQraph() {

		QueryGraph queryGraph = null;
		HashSet<IModelEntity> modifiableEntities;

		modifiableEntities = getEntites();

		queryGraph = updateQueryGraphWithSqlFilter(modifiableEntities);

		this.query.setQueryGraph(queryGraph);

	}

	/**
	 * @param modifiableEntities
	 * @return
	 */
	private QueryGraph updateQueryGraphWithSqlFilter(HashSet<IModelEntity> modifiableEntities) {
		QueryGraph queryGraph;
		SqlFilterModelAccessModality sqlFilterModality = new SqlFilterModelAccessModality();
		modifiableEntities.addAll(sqlFilterModality.getSqlFilterEntities(dataSource, modifiableEntities));
		queryGraph = sqlFilterModality.setGraphWithSqlQueryEntities(modifiableEntities, this);
		return queryGraph;
	}

	/**
	 * @return
	 */
	private HashSet<IModelEntity> getEntites() {
		HashSet<IModelEntity> modifiableEntities;
		if (GraphManager.getGraphEntities(dataSource, query).size() > 0) {
			modifiableEntities = new HashSet<IModelEntity>(GraphManager.getGraphEntities(dataSource, query));
		} else {
			modifiableEntities = new HashSet<IModelEntity>(query.getQueryEntities(dataSource));
		}
		return modifiableEntities;
	}

	@Override
	public IDataSource getDataSource() {
		return dataSource;
	}

	@Override
	public Query getQuery() {
		return query;
	}

	@Override
	public void setQuery(Query query) {
		this.query = query;
		this.queryString = null;
	}

	@Override
	public Map getParameters() {
		return parameters;
	}

	@Override
	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

	@Override
	public Map getProfileAttributes() {
		return profileAttributes;
	}

	@Override
	public void setProfileAttributes(Map profileAttributes) {
		this.profileAttributes = profileAttributes;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.qbe.model.IStatement#getOffset()
	 */
	@Override
	public int getOffset() {
		return offset;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.qbe.model.IStatement#setOffset(int)
	 */
	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.qbe.model.IStatement#getFetchSize()
	 */
	@Override
	public int getFetchSize() {
		return fetchSize;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.qbe.model.IStatement#setFetchSize(int)
	 */
	@Override
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.qbe.model.IStatement#getMaxResults()
	 */
	@Override
	public int getMaxResults() {
		return maxResults;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.qbe.model.IStatement#setMaxResults(int)
	 */
	@Override
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public boolean isMaxResultsLimitBlocking() {
		return isBlocking;
	}

	public void setIsMaxResultsLimitBlocking(boolean isBlocking) {
		this.isBlocking = isBlocking;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	protected void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public abstract String getValueBounded(String operandValueToBound, String operandType);

	@Override
	public String getNextAlias(Map entityAliasesMaps) {
		int aliasesCount = 0;
		Iterator it = entityAliasesMaps.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Map entityAliases = (Map) entityAliasesMaps.get(key);
			aliasesCount += entityAliases.keySet().size();
		}

		return "t_" + aliasesCount;
	}

	private String getEntityAlias(IModelField datamartField, Map entityAliases, Map entityAliasesMaps) {
		IModelEntity rootEntity;

		Couple queryNameAndRoot = datamartField.getQueryName();

		if (queryNameAndRoot.getSecond() != null) {
			rootEntity = (IModelEntity) queryNameAndRoot.getSecond();
		} else {
			rootEntity = datamartField.getParent().getRoot();
		}

		String rootEntityAlias = (String) entityAliases.get(rootEntity.getUniqueName());
		if (rootEntityAlias == null) {
			rootEntityAlias = getNextAlias(entityAliasesMaps);
			entityAliases.put(rootEntity.getUniqueName(), rootEntityAlias);
		}

		return rootEntityAlias;
	}

	@Override
	public String getFieldAliasNoRoles(IModelField datamartField, Map entityAliases, Map entityAliasesMaps) {

		Couple queryNameAndRoot = datamartField.getQueryName();

		String queryName = (String) queryNameAndRoot.getFirst();

		String rootEntityAlias = getEntityAlias(datamartField, entityAliases, entityAliasesMaps);

		return buildFieldQueryNameWithEntityAlias(rootEntityAlias, queryName);// queryName.substring(0,1).toLowerCase()+queryName.substring(1);
	}

	/**
	 * Creates a list of clauses. for each role of the entity create this clause part: role+.+field name. This function is used to create the joins for the
	 * entities
	 *
	 * @param datamartField
	 * @param entityAliases
	 * @param entityAliasesMaps
	 * @param roleName
	 * @return
	 */
	@Override
	public String getFieldAliasWithRoles(IModelField datamartField, Map entityAliases, Map entityAliasesMaps, String roleName) {

		IModelEntity rootEntity;

		Couple queryNameAndRoot = datamartField.getQueryName();

		String queryName = (String) queryNameAndRoot.getFirst();

		if (queryNameAndRoot.getSecond() != null) {
			rootEntity = (IModelEntity) queryNameAndRoot.getSecond();
		} else {
			rootEntity = datamartField.getParent().getRoot();
		}

		Set<String> entityRoleAlias = getQuery().getEntityRoleAlias(rootEntity, getDataSource());

		String rootEntityAlias = getEntityAlias(datamartField, entityAliases, entityAliasesMaps);

		// if there is no role for this field
		if (entityRoleAlias != null) {
			rootEntityAlias = buildEntityAliasWithRoles(rootEntity, roleName, rootEntityAlias);
		}

		return buildFieldQueryNameWithEntityAlias(rootEntityAlias, queryName);// .substring(0,1).toLowerCase()+queryName.substring(1);
	}

	/**
	 * Creates a list of clauses. for each role of the entity create this clause part: role+.+field name. This function is used to create the joins for the
	 * entities
	 *
	 * @param datamartField
	 * @param entityAliases
	 * @param entityAliasesMaps
	 * @param roleName
	 * @return
	 */
	@Override
	public List<String> getFieldAliasWithRolesList(IModelField datamartField, Map entityAliases, Map entityAliasesMaps) {

		List<String> toReturn = new ArrayList<String>();

		IModelEntity rootEntity;

		Couple queryNameAndRoot = datamartField.getQueryName();

		String queryName = (String) queryNameAndRoot.getFirst();

		if (queryNameAndRoot.getSecond() != null) {
			rootEntity = (IModelEntity) queryNameAndRoot.getSecond();
		} else {
			rootEntity = datamartField.getParent().getRoot();
		}

		Map<String, List<String>> roleAliasMap = getQuery().getMapEntityRoleField(getDataSource()).get(rootEntity);
		Set<String> roleAlias = null;
		if (roleAliasMap != null) {
			roleAlias = roleAliasMap.keySet();
		}

		String rootEntityAlias = getEntityAlias(datamartField, entityAliases, entityAliasesMaps);

		if (roleAlias != null && roleAlias.size() > 1) {
			Iterator<String> iter = roleAlias.iterator();
			while (iter.hasNext()) {
				String firstRole = iter.next();
				String rootEntityAliasWithRole = buildEntityAliasWithRoles(rootEntity, firstRole, rootEntityAlias);
				toReturn.add(buildFieldQueryNameWithEntityAlias(rootEntityAliasWithRole, queryName));// .substring(0,1).toLowerCase()+queryName.substring(1));
			}
		} else {
			toReturn.add(buildFieldQueryNameWithEntityAlias(rootEntityAlias, queryName));// queryName.substring(0,1).toLowerCase()+queryName.substring(1));
		}

		return toReturn;
	}

	/**
	 * Check if a select field with the alias exist and if so take it as field to get the query name. Used in FILTERS
	 *
	 * @param datamartField
	 * @param entityAliases
	 * @param entityAliasesMaps
	 * @param alias
	 * @return
	 */
	@Override
	public String getFieldAliasWithRolesFromAlias(IModelField datamartField, Map entityAliases, Map entityAliasesMaps, String alias) {
		Query query = this.getQuery();
		List<ISelectField> fields = query.getSelectFields(true);
		if (alias != null) {// if the field contains an alias, check if it
							// contains a role
			for (int i = 0; i < fields.size(); i++) {
				ISelectField field = fields.get(i);
				if (field.getAlias().equals(alias) && field.getName().equals(datamartField.getUniqueName())) {
					return getFieldAliasWithRoles(datamartField, entityAliases, entityAliasesMaps, field);
				}
			}
		}

		return getFieldAliasNoRoles(datamartField, entityAliases, entityAliasesMaps);
	}

	/**
	 * Used in select, group, order
	 */
	@Override
	public String getFieldAliasWithRoles(IModelField datamartField, Map entityAliases, Map entityAliasesMaps, IQueryField queryField) {

		IModelEntity rootEntity;

		Couple queryNameAndRoot = datamartField.getQueryName();
		String queryName = (String) queryNameAndRoot.getFirst();

		if (queryNameAndRoot.getSecond() != null) {
			rootEntity = (IModelEntity) queryNameAndRoot.getSecond();
		} else {
			rootEntity = datamartField.getParent().getRoot();
		}

		// List<List<Relationship>> roleAlias = (List<List<Relationship>>)
		// rootEntity.getProperty(GraphUtilities.roleRelationsProperty);
		Map<String, List<String>> mapRoleField = getQuery().getMapEntityRoleField(getDataSource()).get(rootEntity);

		String rootEntityAlias = (String) entityAliases.get(rootEntity.getUniqueName());
		if (rootEntityAlias == null) {
			rootEntityAlias = getNextAlias(entityAliasesMaps);
			entityAliases.put(rootEntity.getUniqueName(), rootEntityAlias);
		}

		if (mapRoleField != null && mapRoleField.keySet().size() > 0) {
			Iterator<String> iter = mapRoleField.keySet().iterator();
			while (iter.hasNext()) {
				String role = iter.next();
				List<String> fieldsWithRole = mapRoleField.get(role);
				if (fieldsWithRole != null && fieldsWithRole.contains(queryField.getAlias())) {
					rootEntityAlias = buildEntityAliasWithRoles(rootEntity, role, rootEntityAlias);
					break;
				}
			}
		}

		return buildFieldQueryNameWithEntityAlias(rootEntityAlias, queryName);
	}

	/**
	 * Used to build the from clause
	 */
	@Override
	public String buildFromEntityAliasWithRoles(IModelEntity me, String rel, String entityAlias) {
		String fromClauseElement = me.getName() + " " + entityAlias;
		// for(int i=0; i<rel.size(); i++){
		fromClauseElement = fromClauseElement + ("_" + rel).replace(" ", "");
		// }
		return fromClauseElement;
	}

	private String buildEntityAliasWithRoles(IModelEntity me, String role, String entityAlias) {
		String fromClauseElement = (entityAlias + "_" + role).replace(" ", "");
		;
		return fromClauseElement;
	}

	protected String buildFieldQueryNameWithEntityAlias(String rootEntityAlias, String queryName) {
		return rootEntityAlias + "." + queryName;
	}

	public IMetaData getDataStoreMeta() {
		IMetaData dataStoreMeta;
		ISelectField queryField;
		FieldMetadata dataStoreFieldMeta;

		Map<String, String> aliasSelectedFields = QueryJSONSerializer.getFieldsNature(this.getQuery(), this.getDataSource());
		dataStoreMeta = new MetaData();

		Iterator fieldsIterator = query.getSelectFields(true).iterator();
		while (fieldsIterator.hasNext()) {
			queryField = (ISelectField) fieldsIterator.next();

			dataStoreFieldMeta = new FieldMetadata();
			dataStoreFieldMeta.setAlias(queryField.getAlias());
			if (queryField.isSimpleField()) {
				SimpleSelectField dataMartSelectField = (SimpleSelectField) queryField;
				dataStoreFieldMeta.setName(((SimpleSelectField) queryField).getAlias());
				dataStoreFieldMeta.setProperty("calculated", new Boolean(false));
				dataStoreFieldMeta.setProperty("uniqueName", dataMartSelectField.getUniqueName());

				if (dataMartSelectField.getFunction().getName().equals("NONE") && dataMartSelectField.getJavaClass() != null) {
					dataStoreFieldMeta.setType(dataMartSelectField.getJavaClass());
				} else {
					dataStoreFieldMeta.setType(Object.class);
				}

				String format = dataMartSelectField.getPattern();
				if (format != null && !format.trim().equals("")) {
					dataStoreFieldMeta.setProperty("format", format);
				}

				IModelField datamartField = ((AbstractDataSource) dataSource).getModelStructure().getField(dataMartSelectField.getUniqueName());
				String iconCls = datamartField.getPropertyAsString("type");
				String nature = dataMartSelectField.getNature();
				dataStoreFieldMeta.setProperty("aggregationFunction", dataMartSelectField.getFunction().getName());

				if (nature.equals(QuerySerializationConstants.FIELD_NATURE_MANDATORY_MEASURE)) {
					dataStoreFieldMeta.setFieldType(FieldType.MEASURE);
					dataStoreFieldMeta.getProperties().put(PROPERTY_IS_MANDATORY_MEASURE, Boolean.TRUE);
				} else if (nature.equals(QuerySerializationConstants.FIELD_NATURE_MEASURE)) {
					dataStoreFieldMeta.setFieldType(FieldType.MEASURE);
				} else if (nature.equals(QuerySerializationConstants.FIELD_NATURE_SEGMENT_ATTRIBUTE)) {
					dataStoreFieldMeta.setFieldType(FieldType.ATTRIBUTE);
					dataStoreFieldMeta.getProperties().put(PROPERTY_IS_SEGMENT_ATTRIBUTE, Boolean.TRUE);
				} else if (nature.equals(QuerySerializationConstants.FIELD_NATURE_ATTRIBUTE)) {
					dataStoreFieldMeta.setFieldType(FieldType.ATTRIBUTE);
				} else {
					dataStoreFieldMeta.setFieldType(FieldType.ATTRIBUTE);
				}

			} else if (queryField.isCalculatedField()) {
				CalculatedSelectField calculatedQueryField = (CalculatedSelectField) queryField;
				dataStoreFieldMeta.setName(calculatedQueryField.getAlias());
				dataStoreFieldMeta.setProperty("calculated", new Boolean(true));
				dataStoreFieldMeta.setProperty("calculatedExpert", new Boolean(true));
				// FIXME also calculated field must have uniquename for
				// uniformity
				dataStoreFieldMeta.setProperty("uniqueName", calculatedQueryField.getAlias());
				DataSetVariable variable = new DataSetVariable(calculatedQueryField.getAlias(), calculatedQueryField.getType(),
						calculatedQueryField.getExpression());
				dataStoreFieldMeta.setProperty("variable", variable);
				dataStoreFieldMeta.setType(variable.getTypeClass());

			} else if (queryField.isInLineCalculatedField()) {
				InLineCalculatedSelectField calculatedQueryField = (InLineCalculatedSelectField) queryField;
				dataStoreFieldMeta.setName(calculatedQueryField.getAlias());
				dataStoreFieldMeta.setProperty("calculated", new Boolean(false));
				// FIXME also calculated field must have uniquename for
				// uniformity
				dataStoreFieldMeta.setProperty("uniqueName", calculatedQueryField.getAlias());
				DataSetVariable variable = new DataSetVariable(calculatedQueryField.getAlias(), calculatedQueryField.getType(),
						calculatedQueryField.getExpression());
				dataStoreFieldMeta.setProperty("variable", variable);
				dataStoreFieldMeta.setType(variable.getTypeClass());

				String nature = queryField.getNature();
				if (nature == null) {
					nature = QueryJSONSerializer.getInLinecalculatedFieldNature(calculatedQueryField.getExpression(), aliasSelectedFields);
				}
				dataStoreFieldMeta.setProperty("nature", nature);
				if (nature.equalsIgnoreCase(QuerySerializationConstants.FIELD_NATURE_MANDATORY_MEASURE)
						|| nature.equalsIgnoreCase(QuerySerializationConstants.FIELD_NATURE_MEASURE)) {
					dataStoreFieldMeta.setFieldType(FieldType.MEASURE);
				} else {
					dataStoreFieldMeta.setFieldType(FieldType.ATTRIBUTE);
				}
			}
			dataStoreFieldMeta.setProperty("visible", new Boolean(queryField.isVisible()));

			dataStoreMeta.addFiedMeta(dataStoreFieldMeta);
		}

		return dataStoreMeta;
	}

}
