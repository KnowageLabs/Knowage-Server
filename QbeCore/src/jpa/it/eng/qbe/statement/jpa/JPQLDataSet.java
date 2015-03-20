/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.jpa;

import it.eng.qbe.datasource.jpa.IJpaDataSource;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;



/**
 * @author Andrea Gioia (andrea.gioia@eng.it), Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class JPQLDataSet extends AbstractQbeDataSet {

	
	private List resultList;
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(JPQLDataSet.class);
    
	
	public JPQLDataSet(JPQLStatement statement) {
		super(statement);
	}
	
	
	public void loadData(int offset, int fetchSize, int maxResults) {
		EntityManager entityManager;
		
		try {
			entityManager = ((IJpaDataSource)statement.getDataSource()).getEntityManager();
			loadDataPersistenceProvider(offset, fetchSize, maxResults, entityManager);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to load data", t);
		}
	
	}
	
	private void loadDataPersistenceProvider(int offset, int fetchSize, int maxResults, EntityManager entityManager) {

		javax.persistence.Query jpqlQuery;
		boolean overflow = false;
		int resultNumber = -1;
		it.eng.qbe.query.Query query = this.statement.getQuery();
		Map params = this.getParamsMap();
		if (params != null && !params.isEmpty()) {
			this.updateParameters(query, params);
		}
		String statementStr = statement.getQueryString();
		
		try {
			jpqlQuery = entityManager.createQuery( statementStr );
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to compile query statement [" + statementStr + "]", t);
		}
			
		if (this.isCalculateResultNumberOnLoadEnabled()) {
			resultNumber = getResultNumber(statementStr, jpqlQuery, entityManager);
			logger.info("Number of fetched records: " + resultNumber + " for query " + statement.getQueryString());
			overflow = (maxResults > 0) && (resultNumber >= maxResults);
		}


		List result = null;

		if (overflow && abortOnOverflow) {
			// does not execute query
			result = new ArrayList();
		} else {
			offset = offset < 0 ? 0 : offset;
			if(maxResults > 0) {
				fetchSize = (fetchSize > 0)? Math.min(fetchSize, maxResults): maxResults;
			}
			logger.debug("Executing query " + statement.getQueryString() + " with offset = " + offset + " and fetch size = " + fetchSize);
			jpqlQuery.setFirstResult(offset);
			if(fetchSize > 0) {
				jpqlQuery.setMaxResults(fetchSize);		
			}
			
			try {
				result = jpqlQuery.getResultList();
			} catch (Throwable t) {
				throw new RuntimeException("Impossible to execute statement [" + statementStr + "]", t);
			}

			logger.debug("Query " + statement.getQueryString() + " with offset = " + offset + " and fetch size = " + fetchSize + " executed");
		}	

		dataStore = toDataStore(result);
		
		if (this.isCalculateResultNumberOnLoadEnabled()) {
			dataStore.getMetaData().setProperty("resultNumber", resultNumber);
		}
				
		if(hasDataStoreTransformer()) {
			getDataStoreTransformer().transform(dataStore);
		}
	}
	
	private void loadDataWithMyPagination(int offset, int fetchSize, int maxResults,EntityManager entityManager){
		javax.persistence.Query jpqlQuery;
		boolean overflow = false;
		int maxPageResult=offset+fetchSize;

		jpqlQuery = entityManager.createQuery( statement.getQueryString() );	
		this.resultList = jpqlQuery.getResultList();
		int resultNumber = this.resultList.size();
		logger.info("Number of fetched records: " + resultNumber + " for query " + statement.getQueryString());
		overflow = (maxResults > 0) && (resultNumber >= maxResults);

		
		if (overflow && abortOnOverflow) {
			// does not execute query
			this.resultList = new ArrayList();
		} else {
			offset = offset < 0 ? 0 : offset;
			if(maxResults > 0) {
				maxPageResult = (maxPageResult > 0)? Math.min(maxPageResult, maxResults): maxResults;				
			}
			maxPageResult = Math.min(maxPageResult, resultNumber);
			logger.debug("Executing query " + statement.getQueryString() + " with offset = " + offset + " and fetch size = " + fetchSize);
			dataStore = toDataStore(this.resultList.subList(offset, maxPageResult));
			logger.debug("Query " + statement.getQueryString() + " with offset = " + offset + " and fetch size = " + fetchSize + " executed");
		}	
		dataStore.getMetaData().setProperty("resultNumber", resultNumber);	
		
		if(hasDataStoreTransformer()) {
			getDataStoreTransformer().transform(dataStore);
		}
	}
	
	
	private int getResultNumber(String statementStr, Query jpqlQuery, EntityManager entityManager) {
		int resultNumber = 0;
		
		try {
			logger.debug("Reading result number using an inline-view");
			resultNumber = getResultNumberUsingInlineView(statementStr,entityManager);
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
		resultNumber = ((Number)countQuery.getResultList().get(0)).intValue();
		logger.debug("Query " + "SELECT COUNT(*) FROM (" + sqlQueryString + ")" + " executed");
		logger.debug("Result number is " + resultNumber);
		resultNumber = resultNumber < 0? 0: resultNumber;
		logger.debug("OUT: returning " + resultNumber);

		return resultNumber;
	}
	

	public void setDataSource(IDataSource dataSource) {
		// TODO Auto-generated method stub
		
	}

	
}
