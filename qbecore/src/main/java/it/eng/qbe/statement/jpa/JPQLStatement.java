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
package it.eng.qbe.statement.jpa;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.jpa.IJpaDataSource;
import it.eng.qbe.datasource.jpa.JPADataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.KnowageStringUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JPQLStatement extends AbstractStatement {

	protected IJpaDataSource dataSource;

	public static transient Logger logger = Logger.getLogger(JPQLStatement.class);

	protected JPQLStatement(IDataSource dataSource) {
		super(dataSource);
	}

	public JPQLStatement(IDataSource dataSource, Query query) {
		super(dataSource, query);
	}

	@Override
	public void prepare() {
		String queryStr;

		// one map of entity aliases for each queries (master query +
		// subqueries)
		// each map is indexed by the query id
		Map<String, Map<String, String>> entityAliasesMaps = new HashMap<>();

		queryStr = compose(getQuery(), entityAliasesMaps, false);

		if (getParameters() != null) {
			try {
				queryStr = KnowageStringUtils.replaceParameters(queryStr.trim(), "$P", getParameters());
			} catch (IOException e) {
				throw new SpagoBIRuntimeException("Impossible to set parameters in query", e);
			}
		}

		if (getProfileAttributes() != null) {
			try {
				queryStr = KnowageStringUtils.replaceParameters(queryStr.trim(), "$", getProfileAttributes());

			} catch (IOException e) {
				throw new SpagoBIRuntimeException("Impossible to set profile attributes in query", e);
			}
		}

		setQueryString(queryStr);

	}

	/*
	 * internally used to generate the parametric statement string. Shared by the prepare method and the buildWhereClause method in order to recursively
	 * generate subquery statement string to be embedded in the parent query.
	 */
	private String compose(Query query, Map<String, Map<String, String>> entityAliasesMaps, boolean isSubquery) {
		String queryStr = null;
		String selectClause = null;
		String whereClause = null;
		String groupByClause = null;
		String orderByClause = null;
		String fromClause = null;
		String havingClause = null;
		// String viewRelation = null;

		Assert.assertNotNull(query, "Input parameter 'query' cannot be null");
		Assert.assertTrue(!query.isEmpty(), "Input query cannot be empty (i.e. with no selected fields)");

		// let's start with the query at hand
		entityAliasesMaps.put(query.getId(), new LinkedHashMap<>());

		// JPQLBusinessViewUtility viewsUtility = new
		// JPQLBusinessViewUtility(this);

		selectClause = JPQLStatementSelectClause.build(this, query, entityAliasesMaps);
		whereClause = JPQLStatementWhereClause.build(this, query, entityAliasesMaps);
		groupByClause = JPQLStatementGroupByClause.build(this, query, entityAliasesMaps);
		orderByClause = JPQLStatementOrderByClause.build(this, query, entityAliasesMaps, isSubquery);
		havingClause = JPQLStatementHavingClause.build(this, query, entityAliasesMaps);
		// viewRelation = viewsUtility.buildViewsRelations(entityAliasesMaps,
		// query, whereClause);

		whereClause = JPQLStatementWhereClause.injectAutoJoins(this, whereClause, query, entityAliasesMaps);

		fromClause = JPQLStatementFromClause.build(this, query, entityAliasesMaps);

		whereClause = JPQLStatementWhereClause.fix(this, whereClause, query, entityAliasesMaps);

		queryStr = selectClause + " " + fromClause + " " + whereClause + " "
		// + viewRelation + " "
				+ groupByClause + " " + havingClause + " " + orderByClause;

		Set subqueryIds;
		try {
			subqueryIds = KnowageStringUtils.getParameters(queryStr, "Q");
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Impossible to set parameters in query", e);
		}

		Iterator it = subqueryIds.iterator();
		while (it.hasNext()) {
			String id = (String) it.next();
			Query subquery = query.getSubquery(id);

			String subqueryStr = compose(subquery, entityAliasesMaps, true);
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

		// one map of entity aliases for each queries (master query +
		// subqueries)
		// each map is indexed by the query id
		entityAliasesMaps = new HashMap<>();

		// let's start with the query at hand
		entityAliasesMaps.put(getQuery().getId(), new HashMap<>());

		JPQLStatementSelectClause.build(this, getQuery(), entityAliasesMaps);
		JPQLStatementWhereClause.build(this, getQuery(), entityAliasesMaps);
		JPQLStatementGroupByClause.build(this, getQuery(), entityAliasesMaps);
		JPQLStatementOrderByClause.build(this, getQuery(), entityAliasesMaps, false);
		JPQLStatementFromClause.build(this, getQuery(), entityAliasesMaps);

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
	public String getSqlQueryString() {

		JPADataSource ds = ((JPADataSource) getDataSource());
		EntityManager em = ds.getEntityManager();

		JPQL2SQLStatementRewriter translator = new JPQL2SQLStatementRewriter(em);
		String translatedQuery = translator.rewrite(getQueryString());

		String finalSQLQuery = adjustActualAliases(translatedQuery);

		return finalSQLQuery;
	}

	@Override
	public String getValueBounded(String operandValueToBound, String operandType) {
		JPQLStatementWhereClause clause = new JPQLStatementWhereClause(this);
		return clause.getValueBounded(operandValueToBound, operandType);
	}

	@Override
	public String getQuerySQLString(String wrappedDatasetQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	private String adjustActualAliases(String sqlQueryString) {
		logger.debug("IN: input query is " + sqlQueryString);
		it.eng.spagobi.tools.datasource.bo.IDataSource dataSource = ((JPADataSource) this.getDataSource()).getToolsDataSource();

		String aliasDelimiter;
		try {
			aliasDelimiter = DataBaseFactory.getDataBase(dataSource).getAliasDelimiter();
		} catch (DataBaseException e) {
			throw new SpagoBIRuntimeException("An error occurred while getting datasource alias delimiter", e);
		}

		IMetaData metadata = this.getDataStoreMeta();

		for (int i = 0; i < metadata.getFieldCount(); i++) {
			IFieldMetaData fieldMeta = metadata.getFieldMeta(i);
			String alias = fieldMeta.getAlias();
			int col = sqlQueryString.indexOf(" as col_");
			int com = sqlQueryString.indexOf("_,") > -1 ? sqlQueryString.indexOf("_,") : sqlQueryString.indexOf("_ ");
			sqlQueryString = sqlQueryString.replace(sqlQueryString.substring(col, com + 1), " as " + aliasDelimiter + alias + aliasDelimiter);
		}

		logger.debug("OUT: output query is " + sqlQueryString);
		return sqlQueryString;
	}

}
