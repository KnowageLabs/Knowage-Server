/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.formviewer;

import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.statement.IStatement;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.query.FilterQueryTransformer;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;
import it.eng.spagobi.utilities.sql.SqlUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;


/**
 * The Class ExecuteQueryAction.
 */
public class ExecuteDetailQueryAction extends AbstractQbeEngineAction {	
	
	// INPUT PARAMETERS
	public static final String LIMIT = "limit";
	public static final String START = "start";
	public static final String SORT = "sort";
	public static final String DIR = "dir";
	public static final String FILTERS = "filters";
	public static final String FORM_STATE = "formState";
	
	public static final String LAST_DETAIL_QUERY = "LAST_DETAIL_QUERY";
	
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(ExecuteDetailQueryAction.class);
    public static transient Logger auditlogger = Logger.getLogger("audit.query");
    
	
	public void service(SourceBean request, SourceBean response)  {				
				
		
		String queryId;
		Integer limit;
		Integer start;
		JSONArray filters;
		Integer maxSize;
		boolean isMaxResultsLimitBlocking;
		IDataStore dataStore;
		JDBCDataSet dataSet;
		JSONDataWriter dataSetWriter;
		
		Query query;
		IStatement statement;
		
		Integer resultNumber;
		JSONObject gridDataFeed = new JSONObject();
		
		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor;
					
		logger.debug("IN");
		
		try {
		
			super.service(request, response);	
			
			totalTimeMonitor = MonitorFactory.start("QbeEngine.executeQueryAction.totalTime");
			
			start = getAttributeAsInteger( START );	
			logger.debug("Parameter [" + START + "] is equals to [" + start + "]");
			
			limit = getAttributeAsInteger( LIMIT );
			logger.debug("Parameter [" + LIMIT + "] is equals to [" + limit + "]");
			
			filters = getAttributeAsJSONArray( FILTERS );
			logger.debug("Parameter [" + FILTERS + "] is equals to [" + filters + "]");
			Assert.assertNotNull(filters, "Parameter [" + FILTERS + "] cannot be null");
						
			maxSize = QbeEngineConfig.getInstance().getResultLimit();			
			logger.debug("Configuration setting  [" + "QBE.QBE-SQL-RESULT-LIMIT.value" + "] is equals to [" + (maxSize != null? maxSize: "none") + "]");
			isMaxResultsLimitBlocking = QbeEngineConfig.getInstance().isMaxResultLimitBlocking();
			logger.debug("Configuration setting  [" + "QBE.QBE-SQL-RESULT-LIMIT.isBlocking" + "] is equals to [" + isMaxResultsLimitBlocking + "]");
			
			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			
			
			// STEP 1: modify the query according to the input that come from the form
			query = getEngineInstance().getQueryCatalogue().getFirstQuery();			
			// ... query transformation goes here	
			
			logger.debug("Making a deep copy of the original query...");
			String store = ((JSONObject)SerializerFactory.getSerializer("application/json").serialize(query, getEngineInstance().getDataSource(), getLocale())).toString();
			Query copy = SerializerFactory.getDeserializer("application/json").deserializeQuery(store, getEngineInstance().getDataSource());
			logger.debug("Deep copy of the original query produced");
			
			String jsonEncodedFormState = getAttributeAsString( FORM_STATE );
			logger.debug("Form state retrieved as a string: " + jsonEncodedFormState);
			JSONObject formState = new JSONObject(jsonEncodedFormState);
			logger.debug("Form state converted into a valid JSONObject: " + formState.toString(3));
			JSONObject template = (JSONObject) getEngineInstance().getFormState().getConf();
			logger.debug("Form viewer template retrieved.");
			
			FormViewerQueryTransformer formViewerQueryTransformer = new FormViewerQueryTransformer();
			formViewerQueryTransformer.setFormState(formState);
			formViewerQueryTransformer.setTemplate(template);
			logger.debug("Applying Form Viewer query transformation...");
			query = formViewerQueryTransformer.execTransformation(copy);
			logger.debug("Applying Form Viewer query transformation...");
			
			updatePromptableFiltersValue(query);
			getEngineInstance().setActiveQuery(query);
			
			
			
			// STEP 2: prepare statment and obtain the corresponding sql query
			statement = getEngineInstance().getStatment();	
			statement.setParameters( getEnv() );
			
			String jpaQueryStr = statement.getQueryString();
			String sqlQuery = statement.getSqlQueryString();
			logger.debug("Executable query (HQL/JPQL): [" +  jpaQueryStr+ "]");
			//logger.debug("Executable query (SQL): [" + sqlQuery + "]");
			UserProfile userProfile = (UserProfile)getEnv().get(EngineConstants.ENV_USER_PROFILE);
			//auditlogger.info("[" + userProfile.getUserId() + "]:: HQL: " + hqlQuery);
			//auditlogger.info("[" + userProfile.getUserId() + "]:: SQL: " + sqlQuery);
			
			// STEP 3: transform the sql query
			FilterQueryTransformer transformer = new FilterQueryTransformer();
			List selectFields = SqlUtils.getSelectFields(sqlQuery);
			
			List queryFields = query.getSimpleSelectFields(true);
			for(int i = 0; i < queryFields.size(); i++) {
				ISelectField queryField = (ISelectField)queryFields.get(i);
				String[] f = (String[])selectFields.get(i);	
				transformer.addColumn(f[1]!=null? f[1]:f[0], f[1]!=null? f[1]:f[0]);				
			}
			
			for(int i = 0; i < filters.length(); i++) {
				JSONObject filter = filters.getJSONObject(i);
				String columnName = filter.getString("columnName");
				//with Jackson lib numbers aren't managed as string, so is necessary a different cast
				//String value = filter.getString("value");				
				String value = null;	
				try{
					value = filter.getString("value");
				}catch(JSONException e){
					value = String.valueOf(filter.getDouble("value"));
				}
				
				int fieldIndex = query.getSelectFieldIndex(columnName);				
				String[] f = (String[])selectFields.get(fieldIndex);		
				transformer.addFilter(f[1]!=null? f[1]:f[0], value);
			}
			
			sqlQuery = (String)transformer.transformQuery(sqlQuery);
			
			// put the query into session
			this.setAttributeInSession(LAST_DETAIL_QUERY, sqlQuery);
			
			// STEP 4: execute the query
			
			try {
				logger.debug("Executing query: [" + sqlQuery + "]");
				auditlogger.info("[" + userProfile.getUserId() + "]:: SQL: " + sqlQuery);
				
				dataSet = new JDBCDataSet();
				IDataSource dataSource = (IDataSource)getDataSource().getConfiguration().loadDataSourceProperties().get("datasource"); 
				dataSet.setDataSource(dataSource);
				dataSet.setQuery(sqlQuery);
				dataSet.loadData(start, limit, -1);
				dataStore = dataSet.getDataStore();
				IMetaData dataStoreMetadata = dataStore.getMetaData();
				for(int i = 0; i < dataStoreMetadata.getFieldCount(); i++) {
					ISelectField queryField = (ISelectField)queryFields.get(i);
					dataStoreMetadata.changeFieldAlias(i, queryField.getAlias());
				}
			} catch (Exception e) {
				logger.debug("Query execution aborted because of an internal exceptian");
				SpagoBIEngineServiceException exception;
				String message;
				
				message = "An error occurred in " + getActionName() + " service while executing query: [" +  statement.getQueryString() + "]";				
				exception = new SpagoBIEngineServiceException(getActionName(), message, e);
				exception.addHint("Check if the query is properly formed: [" + statement.getQueryString() + "]");
				exception.addHint("Check connection configuration");
				exception.addHint("Check the qbe jar file");
				
				throw exception;
			}
			logger.debug("Query executed succesfully");
			
			
			//dataStore.getMetaData().setProperty("resultNumber", new Integer( (int)dataStore.getRecordsCount() ));
			
			resultNumber = (Integer)dataStore.getMetaData().getProperty("resultNumber");
			Assert.assertNotNull(resultNumber, "property [resultNumber] of the dataStore returned by loadData method of the class [" + dataSet.getClass().getName()+ "] cannot be null");
			logger.debug("Total records: " + resultNumber);			
			
			
			boolean overflow = maxSize != null && resultNumber >= maxSize;
			if (overflow) {
				logger.warn("Query results number [" + resultNumber + "] exceeds max result limit that is [" + maxSize + "]");
				auditlogger.info("[" + userProfile.getUserId() + "]:: max result limit [" + maxSize + "] exceeded with SQL: " + sqlQuery);
			}
						
			dataSetWriter = new JSONDataWriter();
			gridDataFeed = (JSONObject)dataSetWriter.write(dataStore);
			
			try {
				writeBackToClient( new JSONSuccess(gridDataFeed) );
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}
			
		} catch(Throwable t) {
			errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
			errorHitsMonitor.stop();
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			if(totalTimeMonitor != null) totalTimeMonitor.stop();
			logger.debug("OUT");
		}	
		
		
	}
	
	private void updatePromptableFiltersValue(Query query) {
		logger.debug("IN");
		List whereFields = query.getWhereFields();
		Iterator whereFieldsIt = whereFields.iterator();
		while (whereFieldsIt.hasNext()) {
			WhereField whereField = (WhereField) whereFieldsIt.next();
			if (whereField.isPromptable()) {
				// getting filter value on request
				String promptValue = this.getAttributeAsString(whereField.getName());
				logger.debug("Read prompt value [" + promptValue + "] for promptable filter [" + whereField.getName() + "].");
				if (promptValue != null) {
					whereField.getRightOperand().lastValues = new String[] {promptValue}; // TODO how to manage multi-values prompts?;
				}
			}
		}
		List havingFields = query.getHavingFields();
		Iterator havingFieldsIt = havingFields.iterator();
		while (havingFieldsIt.hasNext()) {
			HavingField havingField = (HavingField) havingFieldsIt.next();
			if (havingField.isPromptable()) {
				// getting filter value on request
				String promptValue = this.getAttributeAsString(havingField.getName());
				logger.debug("Read prompt value [" + promptValue + "] for promptable filter [" + havingField.getName() + "].");
				if (promptValue != null) {
					havingField.getRightOperand().lastValues = new String[] {promptValue}; // TODO how to manage multi-values prompts?;
				}
			}
		}
		logger.debug("OUT");
	}

	
}
