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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import it.eng.qbe.datasource.jpa.IJpaDataSource;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.IStatement;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.iterator.JpaQueryIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it), Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class JPQLDataSet extends AbstractQbeDataSet {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(JPQLDataSet.class);

	public JPQLDataSet(JPQLStatement statement) {
		super(statement);
	}

	@Override
	public void loadData(int offset, int fetchSize, int maxResults) {
		try {
			loadDataPersistenceProvider(offset, fetchSize, maxResults);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to load data", t);
		}
	}

	private EntityManager getEntityMananger() {
		Assert.assertNotNull(statement, "Statement cannot be null");
		return ((IJpaDataSource) statement.getDataSource()).getEntityManager();
	}

	private IStatement getLoadingStatement(EntityManager entityManager) {
		logger.debug("Getting filtered statement...");
		IStatement filteredStatement = getFilteredStatement();
		logger.debug("Filtered statement retrieved");

		it.eng.qbe.query.Query query = getFilteredStatement().getQuery();
		Map params = this.getParamsMap();
		if (params != null && !params.isEmpty()) {
			this.updateParameters(query, params);
		}
		return filteredStatement;
	}

	private void loadDataPersistenceProvider(int offset, int fetchSize, int maxResults) {
		boolean overflow = false;
		int resultNumber = -1;

		EntityManager entityManager = getEntityMananger();

		IStatement filteredStatement = this.getStatement();
		String statementStr = filteredStatement.getQueryString();
		logger.debug("Compiling query statement [" + statementStr + "]");

		javax.persistence.Query jpqlQuery = entityManager.createQuery(statementStr);

		if (this.isCalculateResultNumberOnLoadEnabled()) {
			resultNumber = getResultNumber(statementStr, jpqlQuery, entityManager);
			logger.info("Number of fetched records: " + resultNumber + " for query " + filteredStatement.getQueryString());
			overflow = (maxResults > 0) && (resultNumber >= maxResults);
		}

		List result = null;

		if (overflow && abortOnOverflow) {
			// does not execute query
			result = new ArrayList();
		} else {
			offset = offset < 0 ? 0 : offset;
			if (maxResults > 0) {
				fetchSize = (fetchSize > 0) ? Math.min(fetchSize, maxResults) : maxResults;
			}
			logger.debug("Executing query " + filteredStatement.getQueryString() + " with offset = " + offset + " and fetch size = " + fetchSize);
			jpqlQuery.setFirstResult(offset);
			if (fetchSize > 0) {
				jpqlQuery.setMaxResults(fetchSize);
			}

			try {
				result = jpqlQuery.getResultList();
			} catch (Throwable t) {
				throw new RuntimeException("Impossible to execute statement [" + statementStr + "]", t);
			}

			logger.debug("Query " + filteredStatement.getQueryString() + " with offset = " + offset + " and fetch size = " + fetchSize + " executed");
		}

		dataStore = toDataStore(result, getDataStoreMeta(statement.getQuery()));

		if (this.isCalculateResultNumberOnLoadEnabled()) {
			dataStore.getMetaData().setProperty("resultNumber", resultNumber);
		}

		if (hasDataStoreTransformer()) {
			getDataStoreTransformer().transform(dataStore);
		}
	}

	private int getResultNumber(String statementStr, Query jpqlQuery, EntityManager entityManager) {
		int resultNumber = 0;

		try {
			logger.debug("Reading result number using an inline-view");
			resultNumber = getResultNumberUsingInlineView(statementStr, entityManager);
			logger.debug("Result number sucesfully read using an inline view (resultNumber=[" + resultNumber + "])");
		} catch (Throwable t1) {
			logger.warn("Error reading result number using inline view", t1);

			logger.debug("Reading result number executing the original query");
			try {
				resultNumber = (jpqlQuery).getResultList().size();
			} catch (Throwable t2) {
				logger.error(t2);
				throw new RuntimeException("Impossible to read result number", t2);
			}
			logger.debug("Result number sucesfully read using the original query(resultNumber=[" + resultNumber + "])");
		}

		return resultNumber;
	}

	/**
	 * Get the result number with an in line view
	 *
	 * @param jpqlQuery
	 * @param entityManager
	 * @return
	 * @throws Exception
	 */
	private int getResultNumberUsingInlineView(String jpqlQuery, EntityManager entityManager) throws Exception {
		int resultNumber = 0;
		logger.debug("IN: counting query result");

		JPQL2SQLStatementRewriter translator = new JPQL2SQLStatementRewriter(entityManager);
		String sqlQueryString = translator.rewrite(jpqlQuery);
		javax.persistence.Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM (" + sqlQueryString + ") temp");

		logger.debug("Count query prepared and parameters setted...");
		logger.debug("Executing query...");
		resultNumber = ((Number) countQuery.getResultList().get(0)).intValue();
		logger.debug("Query " + "SELECT COUNT(*) FROM (" + sqlQueryString + ")" + " executed");
		logger.debug("Result number is " + resultNumber);
		resultNumber = resultNumber < 0 ? 0 : resultNumber;
		logger.debug("OUT: returning " + resultNumber);

		return resultNumber;
	}

	@Override
	public void setDataSource(IDataSource dataSource) {
		// TODO Auto-generated method stub

	}

	private IStatement getFilteredStatement() {
		logger.debug("IN");
		// we create a new query adding filters defined by profile attributes
		IModelAccessModality accessModality = this.getStatement().getDataSource().getModelAccessModality();
		it.eng.qbe.query.Query query = accessModality.getFilteredStatement(this.getStatement().getQuery(), this.getStatement().getDataSource(),
				this.getUserProfileAttributes());
		IStatement filteredStatement = this.getStatement().getDataSource().createStatement(query);
		filteredStatement.setProfileAttributes(this.getStatement().getProfileAttributes());
		logger.debug("OUT");
		return filteredStatement;
	}

	@Override
	public String getSignature() {
		return this.getSQLQuery(true);
	}

	@Override
	public String getSQLQuery(boolean includeInjectedFilters) {
		logger.debug("IN: includeInjectedFilters = " + includeInjectedFilters);
		String toReturn = null;
		if (includeInjectedFilters) {
			IStatement filteredStatement = this.getFilteredStatement();
			toReturn = filteredStatement.getSqlQueryString();
		} else {
			toReturn = statement.getSqlQueryString();
		}
		logger.debug("OUT: returning [" + toReturn + "]");
		return toReturn;
	}

	@Override
	public DataIterator iterator() {
		logger.debug("IN");
		try {

			EntityManager entityManager = ((IJpaDataSource) statement.getDataSource()).getEntityManager();
			IStatement filteredStatement = getStatement();
			String statementStr = filteredStatement.getQueryString();
			logger.debug("Compiling query statement [" + statementStr + "]");

			javax.persistence.Query jpqlQuery = entityManager.createQuery(statementStr);
			jpqlQuery.setMaxResults(JpaQueryIterator.FETCH_SIZE);

			IMetaData metadata = getDataStoreMeta(statement.getQuery());

			DataIterator iterator = new JpaQueryIterator(jpqlQuery, metadata);
			return iterator;
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public boolean isIterable() {
		return true;
	}

}
