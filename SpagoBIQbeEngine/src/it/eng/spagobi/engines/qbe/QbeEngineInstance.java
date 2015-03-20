/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.accessmodality.AbstractModelAccessModality;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.datasource.QbeDataSourceManager;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration;
import it.eng.spagobi.engines.qbe.template.QbeTemplate;
import it.eng.spagobi.engines.qbe.template.QbeTemplateParser;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.engines.AbstractEngineInstance;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class QbeEngineInstance extends AbstractEngineInstance {
	
	IDataSource dataSource;		
	QueryCatalogue queryCatalogue;
	String activeQueryId;
	QbeTemplate template;
	FormState formState;
	IDataSet dataSet;
	WorkSheetDefinition workSheetDefinition;

	// executable version of the query. cached here for performance reasons (i.e. avoid query re-compilation 
	// over result-set paging)
	IStatement statement;

	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(QbeEngineInstance.class);
	

	protected QbeEngineInstance(Object template, Map env) throws QbeEngineException {
		this( QbeTemplateParser.getInstance() != null ? QbeTemplateParser.getInstance().parse(template) : null, env );
	
	}
	
	protected QbeEngineInstance(QbeTemplate template, Map env) throws QbeEngineException {
		super( env );
		
		logger.debug("IN");
		
		this.template = template;
		
		queryCatalogue = new QueryCatalogue();
		queryCatalogue.addQuery(new Query());
		
		
		it.eng.spagobi.tools.datasource.bo.IDataSource dataSrc = (it.eng.spagobi.tools.datasource.bo.IDataSource)env.get( EngineConstants.ENV_DATASOURCE );
		logger.debug("Datasource is " + dataSrc);

		Map<String, Object> dataSourceProperties = new HashMap<String, Object>();
	
		if(template != null){
			dataSourceProperties.put("dblinkMap", template.getDbLinkMap());			
		}

		dataSourceProperties.put("metadataServiceProxy", env.get(EngineConstants.ENV_METAMODEL_PROXY));
		dataSourceProperties.put(EngineConstants.ENV_DATASETS, env.get(EngineConstants.ENV_DATASETS));
		
		dataSourceProperties.put("datasource", dataSrc);
		
		dataSource = QbeDataSourceManager.getInstance().getDataSource(
				template != null ? template.getDatamartNames() : null, 
				dataSourceProperties, 
				QbeEngineConfig.getInstance().isDataSourceCacheEnabled());
		
		
		if (template != null ){
			String maxRecuriosnLevel = (String)template.getProperty("maxRecursionLevel");
			if(maxRecuriosnLevel != null) {
				dataSource.getConfiguration().loadDataSourceProperties().put("maxRecursionLevel", maxRecuriosnLevel);
			}
		
		   if(template.getDatamartModelAccessModality() != null) {
			
			if(template.getDatamartModelAccessModality().getRecursiveFiltering() == null) {
				String recursiveFilteringAttr = dataSource.getModelStructure().getPropertyAsString(AbstractModelAccessModality.ATTR_RECURSIVE_FILTERING);
				if(!StringUtilities.isEmpty(recursiveFilteringAttr)) {
					if("disabled".equalsIgnoreCase(recursiveFilteringAttr)) {
						template.getDatamartModelAccessModality().setRecursiveFiltering( Boolean.FALSE );
					} else {
						template.getDatamartModelAccessModality().setRecursiveFiltering( Boolean.TRUE );
					}
				} else {
					template.getDatamartModelAccessModality().setRecursiveFiltering( Boolean.TRUE );
				}
			}
			
			dataSource.setDataMartModelAccessModality( template.getDatamartModelAccessModality() );
		}
		
		if( template.getProperty("query") != null ) {
			loadQueryDefinition((JSONObject) template.getProperty("query"));
		}
		
		if( template.getProperty("formJSONTemplate") != null ) {
			loadFormDefinition((JSONObject) template.getProperty("formJSONTemplate"));
		}
		
		if( template.getProperty("formValuesJSONTemplate") != null ) {
			loadFormValuesDefinition((JSONObject) template.getProperty("formValuesJSONTemplate"));
		}
//		
//		if( template.getProperty("worksheetJSONTemplate") != null ) {
//			loadWorksheetDefinition((JSONObject) template.getProperty("worksheetJSONTemplate"));
//		}
		
		}
		
		validate();
		
		
		logger.debug("OUT");
	}
	
//	private void loadWorksheetDefinition(JSONObject worksheetDefinition) {
//		try {
//			WorkSheetDefinition workSheetDefinition = new WorkSheetDefinition();
//			// TODO set the encoding
//			workSheetDefinition.load( worksheetDefinition.toString().getBytes() );
//			setWorkSheetDefinition(workSheetDefinition);
//		} catch(Throwable t) {
//			SpagoBIRuntimeException serviceException;
//			String msg = "Impossible load worksheet definition [" + worksheetDefinition + "].";
//			Throwable rootException = t;
//			while(rootException.getCause() != null) {
//				rootException = rootException.getCause();
//			}
//			String str = rootException.getMessage()!=null? rootException.getMessage(): rootException.getClass().getName();
//			msg += "\nThe root cause of the error is: " + str;
//			serviceException = new SpagoBIRuntimeException(msg, t);
//			
//			throw serviceException;
//		}
//	}

	private void loadFormDefinition(JSONObject formDefinition) {
		try {
			FormState formState = new FormState();
			// TODO set the encoding
			formState.load( formDefinition.toString().getBytes() );
			setFormState( formState );
		} catch(Throwable t) {
			SpagoBIRuntimeException serviceException;
			String msg = "Impossible load form state [" + formDefinition + "].";
			Throwable rootException = t;
			while(rootException.getCause() != null) {
				rootException = rootException.getCause();
			}
			String str = rootException.getMessage()!=null? rootException.getMessage(): rootException.getClass().getName();
			msg += "\nThe root cause of the error is: " + str;
			serviceException = new SpagoBIRuntimeException(msg, t);
			
			throw serviceException;
		}
		
	}
	
	private void loadFormValuesDefinition(JSONObject formValuesDefinition) {
		try {
			getFormState().setFormStateValues(formValuesDefinition);
		} catch(Throwable t) {
			SpagoBIRuntimeException serviceException;
			String msg = "Impossible load form state [" + formValuesDefinition + "].";
			Throwable rootException = t;
			while(rootException.getCause() != null) {
				rootException = rootException.getCause();
			}
			String str = rootException.getMessage()!=null? rootException.getMessage(): rootException.getClass().getName();
			msg += "\nThe root cause of the error is: " + str;
			serviceException = new SpagoBIRuntimeException(msg, t);
			
			throw serviceException;
		}
		
	}

	private void loadQueryDefinition(JSONObject queryDefinition) {
		try {
			QbeEngineAnalysisState analysisState = new QbeEngineAnalysisState( dataSource );
			// TODO set the encoding
			analysisState.load( queryDefinition.toString().getBytes() );
			setAnalysisState( analysisState );
		} catch(Throwable t) {
			SpagoBIRuntimeException serviceException;
			String msg = "Impossible load query [" + queryDefinition + "].";
			Throwable rootException = t;
			while(rootException.getCause() != null) {
				rootException = rootException.getCause();
			}
			String str = rootException.getMessage()!=null? rootException.getMessage(): rootException.getClass().getName();
			msg += "\nThe root cause of the error is: " + str;
			serviceException = new SpagoBIRuntimeException(msg, t);
			
			throw serviceException;
		}
	}

	public void setFormState(FormState formState) {
		this.formState = formState;
	}
	
	public FormState getFormState() {
		return this.formState;
	}

	public void validate() throws QbeEngineException {
		return;
	}
	
	public IEngineAnalysisState getAnalysisState() {
		QbeEngineAnalysisState analysisState = null;
		analysisState= new QbeEngineAnalysisState( dataSource );
		analysisState.setCatalogue( this.getQueryCatalogue() );
//		if (this.getWorkSheetDefinition() != null) {
//			analysisState.setWorkSheetDefinition( this.getWorkSheetDefinition() );
//		} else {
//			analysisState.setWorkSheetDefinition( WorkSheetDefinition.EMPTY_WORKSHEET );
//		}
		return analysisState;
	}
	
	public void setAnalysisState(IEngineAnalysisState analysisState) {	
		QbeEngineAnalysisState qbeEngineAnalysisState = null;
		
		qbeEngineAnalysisState = (QbeEngineAnalysisState)analysisState;
		setQueryCatalogue( qbeEngineAnalysisState.getCatalogue(  ) );
//		setWorkSheetDefinition( qbeEngineAnalysisState.getWorkSheetDefinition( ) );
	}
	

	public IDataSource getDataSource() {
		return dataSource;
	}
	
	public QbeTemplate getTemplate() {
		return template;
	}

	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}	
	
	
	public QueryCatalogue getQueryCatalogue() {
		return queryCatalogue;
	}

	public void setQueryCatalogue(QueryCatalogue queryCatalogue) {
		this.queryCatalogue = queryCatalogue;
	}
	
	private String getActiveQueryId() {
		return activeQueryId;
	}

	private void setActiveQueryId(String activeQueryId) {
		this.activeQueryId = activeQueryId;
	}
	
	public Query getActiveQuery() {
		return getQueryCatalogue().getQuery( getActiveQueryId() );
	}

	public void setActiveQuery(Query query) {
		setActiveQueryId(query.getId());
		this.statement = getDataSource().createStatement( query );
	}
	
	public void setActiveQuery(String queryId) {
		Query query;
		
		query = getQueryCatalogue().getQuery( queryId );
		if(query != null) {
			setActiveQueryId(query.getId());
			this.statement = getDataSource().createStatement( query );
		}
	}
	
	public void resetActiveQuery() {
		setActiveQueryId(null);
		setStatment(null);
	}
	
	public IStatement getStatment() {
		return statement;
	}

	public void setStatment(IStatement statment) {
		this.statement = statment;
	}


	public RegistryConfiguration getRegistryConfiguration() {
		QbeTemplate template = this.getTemplate();
		
		RegistryConfiguration registryConf = null; 
		if(template != null){
			registryConf = (RegistryConfiguration) template.getProperty("registryConfiguration");
		}
		return registryConf;
	}    
	
	/**
	 * Builds a IDataSet starting from the active query.
	 * @return the data set representation of the active query  
	 */
	public IDataSet getActiveQueryAsDataSet() {
		logger.debug("Getting the dataset from the query ");
		try {
			
			if (this.dataSet == null) {
				
				dataSet = QbeDatasetFactory.createDataSet(statement);
				boolean isMaxResultsLimitBlocking = QbeEngineConfig.getInstance().isMaxResultLimitBlocking();
				dataSet.setAbortOnOverflow(isMaxResultsLimitBlocking);

			} else {
				
				((AbstractQbeDataSet) dataSet).setStatement(statement);
				
			}
			
			Map userAttributes = new HashMap();
			UserProfile userProfile = (UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
			userAttributes.putAll(userProfile.getUserAttributes());
			userAttributes.put(SsoServiceInterface.USER_ID, userProfile.getUserId().toString());
			
			dataSet.addBinding("attributes", userAttributes);
			dataSet.addBinding("parameters", this.getEnv());
			dataSet.setUserProfileAttributes(userAttributes);
			
			dataSet.setParamsMap( this.getEnv() );
			
		} catch (Exception e) {
			logger.debug("Error getting the data set from the query");		
			throw new SpagoBIRuntimeException("Error getting the data set from the query", e);
		}
		logger.debug("Dataset correctly taken from the query ");
		return dataSet;
	}
	
	public it.eng.spagobi.tools.datasource.bo.IDataSource getDataSourceForWriting() {
		it.eng.spagobi.tools.datasource.bo.IDataSource datasource = (it.eng.spagobi.tools.datasource.bo.IDataSource) this
				.getEnv().get(EngineConstants.DATASOURCE_FOR_WRITING);
		if (datasource == null) {
			throw new SpagoBIEngineRuntimeException(
					"Datasource for writing not defined!");
		}
		return datasource;
	}

}
