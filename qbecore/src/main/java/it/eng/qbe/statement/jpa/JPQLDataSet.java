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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.hibernate.Filter;
import org.hibernate.Session;

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
		Session session = (Session) entityManager.getDelegate();
		enableFilters(session);

		IStatement filteredStatement = this.getStatement();
		String statementStr = filteredStatement.getQueryString();
		logger.debug("Compiling query statement [" + statementStr + "]");
		javax.persistence.Query jpqlQuery = entityManager.createQuery(statementStr);

		if (this.isCalculateResultNumberOnLoadEnabled()) {
			resultNumber = getResultNumber(jpqlQuery);
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

	/**
	 * @param session
	 * @param runtimeDrivers
	 */
	private void enableFilters(Session session) {
		Map<String, Object> drivers = this.getDrivers();
		if (drivers != null) {
			if (drivers.isEmpty() == false) {
				Filter filter = null;
				Set<String> filterNames = session.getSessionFactory().getDefinedFilterNames();
				Iterator<String> it = filterNames.iterator();
				String driverName = null;

				while (it.hasNext()) {
					String filterName = it.next();
					filter = session.enableFilter(filterName);
					Map<String, String> driverUrlNames = filter.getFilterDefinition().getParameterTypes();
					for (String key : driverUrlNames.keySet()) {
						driverName = key.toString();
						Map mapOfValues = (Map) drivers.get(driverName);
						if (mapOfValues.get("value") instanceof List) {
							filter.setParameterList(driverName, (Collection) mapOfValues.get("value"));
						} else if (mapOfValues.get("value") instanceof Map) {
							Map defaultValue = (Map) mapOfValues.get("value");
							filter.setParameter(driverName, defaultValue.get("value"));
						} else {
							filter.setParameter(driverName, mapOfValues.get("value"));
						}
					}
				}
			}
		}
	}

	private int getResultNumber(Query jpqlQuery) {
		int resultNumber = 0;
		try {
			resultNumber = jpqlQuery.getResultList().size();
		} catch (Exception e) {
			throw new RuntimeException("Impossible to get result number", e);
		}
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

			Session session = (Session) entityManager.getDelegate();
			enableFilters(session);

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

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#getDrivers()
	 */
	@Override
	public Map<String, Object> getDrivers() {
		return super.getDrivers();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#setDrivers()
	 */
	@Override
	public void setDrivers(Map<String, Object> drivers) {
		super.setDrivers(drivers);
	}

}
