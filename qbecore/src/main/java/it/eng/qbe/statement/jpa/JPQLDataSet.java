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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.type.Type;

import it.eng.qbe.datasource.jpa.IJpaDataSource;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.IStatement;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.iterator.JpaQueryIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

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
		Session session = entityManager.unwrap(Session.class);
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

				while (it.hasNext()) {
					String filterName = it.next();
					filter = session.enableFilter(filterName);
					Map<String, Type> parametersTypes = filter.getFilterDefinition().getParameterTypes();
					for (Entry<String, Type> entry : parametersTypes.entrySet()) {
						String parameterName = entry.getKey();
						Type parameterType = entry.getValue();
						String driverName = parameterName.toString();
						Object value = null;
						Object valueAfterConversion = null;
						Class<?> wantedClass = parameterType.getReturnedClass();
						try {
							List<?> valueList = (List<?>) drivers.get(driverName);
							if (valueList.size() == 1) {
								Map<?, ?> valueDescriptionMap = (Map<?, ?>) valueList.get(0);
								if (!valueDescriptionMap.isEmpty()) {
									value = valueDescriptionMap.get("value");
									valueAfterConversion = mapValueToRequiredType(wantedClass, value);
									filter.setParameter(driverName, valueAfterConversion);
								}
							}
							if (valueList.size() > 1) {
								List<Object> multivalueList = new ArrayList<Object>();
								for (int i = 0; i < valueList.size(); i++) {
									Map<?, ?> valueDescriptionMap = (Map<?, ?>) valueList.get(i);
									value = valueDescriptionMap.get("value");
									valueAfterConversion = mapValueToRequiredType(wantedClass, value);
									multivalueList.add(valueAfterConversion);
								}
								filter.setParameterList(driverName, multivalueList);
							}
							value = null;
							valueAfterConversion = null;
						} catch (Exception e) {
							String msg = String.format("Error during conversion for driver %s from value %s of class %s to %s of class %s", driverName, value,
									value != null ? value.getClass().getName() : "N.D.", valueAfterConversion, wantedClass.getName());
							logger.error(msg, e);
							throw new SpagoBIRuntimeException(msg, e);
						}
					}
				}
			}
		}
	}

	/**
	 * Map value from driver to the required value from Hibernate's filter.
	 *
	 * @param wantedClass
	 *            Type wanted by Hibernate
	 * @param value
	 *            Actual value
	 * @return The mapped value
	 *
	 * @throws NoSuchMethodException
	 *             When the wanted class has no constructor with only one string as parameter
	 * @throws SecurityException
	 *             When constructor with only one string as parameter is private
	 * @throws InstantiationException
	 *             When you can't instantiate the required class
	 * @throws IllegalAccessException
	 *             When you can't access the required constructor
	 * @throws IllegalArgumentException
	 *             Shouldn't happen
	 * @throws InvocationTargetException
	 *             Shouldn't happen
	 * @throws ParseException
	 *             When the date string is invalid
	 */
	private Object mapValueToRequiredType(Class<?> wantedClass, Object value) throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException {
		Object ret = null;
		if (Date.class.equals(wantedClass)) {
			String configValue = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format");
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(configValue);
			ret = simpleDateFormat.parse(value.toString());
		} else {
			Constructor<?> constructor = wantedClass.getConstructor(String.class);
			ret = constructor.newInstance(value.toString());
		}
		return ret;
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

	public IStatement getFilteredStatement() {
		logger.debug("IN");
		// we create a new query adding filters defined by profile attributes
		IModelAccessModality accessModality = this.getStatement().getDataSource().getModelAccessModality();
		it.eng.qbe.query.Query query = accessModality.getFilteredStatement(this.getStatement().getQuery(), this.getStatement().getDataSource(),
				this.getUserProfileAttributes());
		Map params = this.getStatement().getParameters();
		IStatement filteredStatement = this.getStatement().getDataSource().createStatement(query);
		filteredStatement.setParameters(params);
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

			Session session = entityManager.unwrap(Session.class);
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

		EntityManager entityManager = getEntityMananger();
		Session session = entityManager.unwrap(Session.class);
		enableFilters(session);
	}

}
