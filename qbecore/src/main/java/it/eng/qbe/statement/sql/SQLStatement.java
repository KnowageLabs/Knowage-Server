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

package it.eng.qbe.statement.sql;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */

public class SQLStatement extends AbstractStatement {

	public static transient Logger logger = Logger.getLogger(SQLStatement.class);

	private String initialDatasetQuery;

	protected SQLStatement(IDataSource dataSource) {
		super(dataSource);
	}

	public SQLStatement(IDataSource dataSource, Query query) {
		super(dataSource, query);
	}

	@Override
	public void prepare() {
		String queryStr;

		// one map of entity aliases for each queries (master query + subqueries)
		// each map is indexed by the query id
		Map<String, Map<String, String>> entityAliasesMaps = new HashMap<String, Map<String, String>>();

		queryStr = compose(getQuery(), entityAliasesMaps);

		if (getParameters() != null) {
			try {
				queryStr = StringUtils.replaceParameters(queryStr.trim(), "P", getParameters());
			} catch (IOException e) {
				throw new SpagoBIRuntimeException("Impossible to set parameters in query", e);
			}

		}

		setQueryString(queryStr);

	}

	/*
	 * internally used to generate the parametric statement string. Shared by the prepare method and the buildWhereClause method in order to recursively
	 * generate subquery statement string to be embedded in the parent query.
	 */
	private String compose(Query query, Map<String, Map<String, String>> entityAliasesMaps) {
		String queryStr = null;
		String selectClause = null;
		String whereClause = null;
		String groupByClause = null;
		String orderByClause = null;
		String fromClause = null;
		String havingClause = null;

		Assert.assertNotNull(query, "Input parameter 'query' cannot be null");
		Assert.assertTrue(!query.isEmpty(), "Input query cannot be empty (i.e. with no selected fields)");

		// let's start with the query at hand
		entityAliasesMaps.put(query.getId(), new HashMap<String, String>());

		selectClause = SQLStatementSelectClause.build(this, query, entityAliasesMaps, true);
		whereClause = SQLStatementWhereClause.build(this, query, entityAliasesMaps);
		groupByClause = SQLStatementGroupByClause.build(this, query, entityAliasesMaps);
		orderByClause = SQLStatementOrderByClause.build(this, query, entityAliasesMaps);
		havingClause = SQLStatementHavingClause.build(this, query, entityAliasesMaps);

		whereClause = SQLStatementWhereClause.injectAutoJoins(this, whereClause, query, entityAliasesMaps);

		fromClause = SQLStatementFromClause.build(this, query, entityAliasesMaps, initialDatasetQuery);

		queryStr = selectClause + " " + fromClause + " " + whereClause + " " + groupByClause + " " + havingClause + " " + orderByClause;

		Set subqueryIds;
		try {
			subqueryIds = StringUtils.getParameters(queryStr, "Q");
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Impossible to set parameters in query", e);
		}

		Iterator it = subqueryIds.iterator();
		while (it.hasNext()) {
			String id = (String) it.next();
			Query subquery = query.getSubquery(id);

			String subqueryStr = compose(subquery, entityAliasesMaps);
			queryStr = queryStr.replaceAll("Q\\{" + subquery.getId() + "\\}", subqueryStr);
		}

		return queryStr;
	}

	@Override
	public Set getSelectedEntities() {
		Set selectedEntities;
		Map<String, Map<String, String>> entityAliasesMaps;
		Iterator entityUniqueNamesIterator;
		String entityUniqueName;
		IModelEntity entity;

		Assert.assertNotNull(getQuery(), "Input parameter 'query' cannot be null");
		Assert.assertTrue(!getQuery().isEmpty(), "Input query cannot be empty (i.e. with no selected fields)");

		selectedEntities = new HashSet();

		// one map of entity aliases for each queries (master query + subqueries)
		// each map is indexed by the query id
		entityAliasesMaps = new HashMap<String, Map<String, String>>();

		// let's start with the query at hand
		entityAliasesMaps.put(getQuery().getId(), new HashMap<String, String>());

		SQLStatementGroupByClause.build(this, getQuery(), entityAliasesMaps);

		Map entityAliases = entityAliasesMaps.get(getQuery().getId());
		entityUniqueNamesIterator = entityAliases.keySet().iterator();
		while (entityUniqueNamesIterator.hasNext()) {
			entityUniqueName = (String) entityUniqueNamesIterator.next();
			entity = getDataSource().getModelStructure().getEntity(entityUniqueName);
			selectedEntities.add(entity);
		}
		return selectedEntities;
	}

	@Override
	public String getQueryString() {
		if (super.getQueryString() == null) {
			this.prepare();
		}

		return super.getQueryString();
	}

	@Override
	public String getQuerySQLString(String initialDatasetQuery) {
		if (initialDatasetQuery != null) {
			this.setInitialDatasetQuery(initialDatasetQuery);
			this.prepare();
		}
		return super.getQueryString();

	}

	@Override
	public String getSqlQueryString() {

		// ISQLDataSource ds = ((ISQLDataSource)getDataSource());
		return getQueryString();
		// EntityManager em = ds.getEntityManager();
		//
		// JPQL2SQLStatementRewriter translator = new JPQL2SQLStatementRewriter(em);
		// return translator.rewrite( getQueryString());

	}

	@Override
	public String getValueBounded(String operandValueToBound, String operandType) {
		// JPQLStatementWhereClause clause = new JPQLStatementWhereClause(this);
		// return clause.getValueBounded(operandValueToBound, operandType);
		return null;
	}

	@Override
	protected String buildFieldQueryNameWithEntityAlias(String rootEntityAlias, String queryName) {
		DataSetDataSource datasetDatasource = (DataSetDataSource) this.getDataSource();
		it.eng.spagobi.tools.datasource.bo.IDataSource datasourceForReading = datasetDatasource.getDataSourceForReading();
		return rootEntityAlias + "." + AbstractJDBCDataset.encapsulateColumnName(queryName, datasourceForReading);
	}

	public String getInitialDatasetQuery() {
		return initialDatasetQuery;
	}

	public void setInitialDatasetQuery(String initialDatasetQuery) {
		this.initialDatasetQuery = initialDatasetQuery;
	}

}
