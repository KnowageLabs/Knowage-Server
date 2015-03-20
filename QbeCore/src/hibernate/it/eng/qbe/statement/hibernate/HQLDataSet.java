/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.hibernate;

import it.eng.qbe.datasource.hibernate.IHibernateDataSource;
import it.eng.qbe.datasource.transaction.hibernate.HibernateTransaction;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.JDBCSharedConnectionDataProxy;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class HQLDataSet extends AbstractQbeDataSet {


	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(HQLDataSet.class);

	public HQLDataSet(HQLStatement statement) {
		super(statement);

	}

	public void loadData(int offset, int fetchSize, int maxResults)  {
		Session session = null;
		org.hibernate.Query hibernateQuery;
		int resultNumber = -1;
		boolean overflow = false;

		try{		
			session = ((IHibernateDataSource)statement.getDataSource()).getHibernateSessionFactory().openSession();

			Query query = this.statement.getQuery();
			Map params = this.getParamsMap();
			if (params != null && !params.isEmpty()) {
				this.updateParameters(query, params);
			}

			// execute query
			hibernateQuery = session.createQuery( statement.getQueryString() );

			if (this.isCalculateResultNumberOnLoadEnabled()) {
				resultNumber = getResultNumber(hibernateQuery, session);
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
				hibernateQuery.setFirstResult(offset);
				if(fetchSize > 0) hibernateQuery.setMaxResults(fetchSize);			
				result = hibernateQuery.list();
				logger.debug("Query " + statement.getQueryString() + " with offset = " + offset + " and fetch size = " + fetchSize + " executed");
			}	

			dataStore = toDataStore(result);
			if (this.isCalculateResultNumberOnLoadEnabled()) {
				dataStore.getMetaData().setProperty("resultNumber", resultNumber);
			}

			if(hasDataStoreTransformer()) {
				getDataStoreTransformer().transform(dataStore);
			}
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}		
	}

	private int getResultNumber(org.hibernate.Query hibernateQuery, Session session) {
		int resultNumber = 0;
		try {
			resultNumber = getResultNumberUsingInlineView(hibernateQuery, session);
		} catch (Exception e) {
			logger.warn("Error getting result number using inline view!!", e);
			resultNumber = getResultNumberUsingScrollableResults(hibernateQuery, session);
		}
		return resultNumber;
	}

	private int getResultNumberUsingInlineView(org.hibernate.Query hibernateQuery, Session session) throws Exception {
		int resultNumber = 0;
		logger.debug("IN");
		String sqlQuery = "SELECT COUNT(*) FROM (" + statement.getSqlQueryString() + ") temptable";
		logger.debug("Executing query " + sqlQuery + " ...");
		JDBCDataSet dataSet = new JDBCDataSet();
		JDBCSharedConnectionDataProxy proxy = new JDBCSharedConnectionDataProxy(HibernateTransaction.getConnection(session));
		dataSet.setDataProxy(proxy);
		dataSet.setQuery(sqlQuery);
		dataSet.loadData(0, 1, -1);
		logger.debug("Query " + sqlQuery + " executed");
		IDataStore dataStore = dataSet.getDataStore();
		logger.debug("Data store retrieved");
		resultNumber = ((Number)dataStore.getRecordAt(0).getFieldAt(0).getValue()).intValue();
		logger.debug("Result number is " + resultNumber);
		resultNumber = resultNumber < 0? 0: resultNumber;
		logger.debug("OUT: returning " + resultNumber);
		return resultNumber;
	}

	private int getResultNumberUsingScrollableResults(org.hibernate.Query hibernateQuery, Session session) {
		int resultNumber = 0;
		logger.debug("Scrolling query " + statement.getQueryString() + " ...");
		ScrollableResults scrollableResults = hibernateQuery.scroll();
		scrollableResults.last();
		logger.debug("Scrolled query " + statement.getQueryString());
		resultNumber = scrollableResults.getRowNumber() + 1; // Hibernate ScrollableResults row number starts with 0
		logger.debug("Number of fetched records: " + resultNumber + " for query " + statement.getQueryString());
		resultNumber = resultNumber < 0? 0: resultNumber;
		return resultNumber;
	}

	public IDataStore fetchNext() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFetchSize(int l) {
		// TODO Auto-generated method stub

	}

//	public void updateParameters(Query query, Map parameters) {
//		logger.debug("IN");
//		List whereFields = query.getWhereFields();
//		Iterator whereFieldsIt = whereFields.iterator();
//		while (whereFieldsIt.hasNext()) {
//			WhereField whereField = (WhereField) whereFieldsIt.next();
//			if (whereField.isPromptable()) {
//				String key = getParameterKey(whereField.getRightOperand().values[0]);
//				if (key != null) {
//					String parameterValues = (String) parameters.get(key);
//					if (parameterValues != null) {
//						String[] promptValues = new String[] {parameterValues}; // TODO how to manage multi-values prompts?
//						logger.debug("Read prompts " + promptValues + " for promptable filter " + whereField.getName() + ".");
//						whereField.getRightOperand().lastValues = promptValues;
//					}
//				}
//			}
//		}
//		List havingFields = query.getHavingFields();
//		Iterator havingFieldsIt = havingFields.iterator();
//		while (havingFieldsIt.hasNext()) {
//			HavingField havingField = (HavingField) havingFieldsIt.next();
//			if (havingField.isPromptable()) {
//				String key = getParameterKey(havingField.getRightOperand().values[0]);
//				if (key != null) {
//					String parameterValues = (String) parameters.get(key);
//					if (parameterValues != null) {
//						String[] promptValues = new String[] {parameterValues}; // TODO how to manage multi-values prompts?
//						logger.debug("Read prompt value " + promptValues + " for promptable filter " + havingField.getName() + ".");
//						havingField.getRightOperand().lastValues = promptValues; 
//					}
//				}
//			}
//		}
//		logger.debug("OUT");
//	}
//
//
//	private String getParameterKey(String fieldValue) {
//		int beginIndex = fieldValue.indexOf("P{");
//		int endIndex = fieldValue.indexOf("}");
//		if (beginIndex > 0 && endIndex > 0 && endIndex > beginIndex) {
//			return fieldValue.substring(beginIndex + 2, endIndex);
//		} else {
//			return null;
//		}
//
//	}

	public void setDataSource(IDataSource dataSource) {
		// TODO Auto-generated method stub
		
	}

	
	
}
