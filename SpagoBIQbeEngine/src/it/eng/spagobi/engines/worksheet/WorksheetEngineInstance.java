/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.qbe.QbeEngineException;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.engines.worksheet.template.WorksheetTemplate;
import it.eng.spagobi.engines.worksheet.template.WorksheetTemplateParser;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.engines.AbstractEngineInstance;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorksheetEngineInstance extends AbstractEngineInstance {

	IDataSource dataSource;
	IDataSource dataSourceForWriting;
	IDataSet dataSet;
	WorksheetTemplate template;
	/** The temporary table name to be considered for this analysis */
	private String temporaryTableName;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(QbeEngineInstance.class);


	protected WorksheetEngineInstance(Object template, Map env) throws WorksheetEngineException {
		this( WorksheetTemplateParser.getInstance().parse(template, env), env );
	}

	protected WorksheetEngineInstance(WorksheetTemplate template, Map env) throws WorksheetEngineException {
		super( env );
		logger.debug("IN");
		this.template = template;
		this.setTemporaryTableName();
		logger.debug("OUT");
		
	}

	public void validate() throws QbeEngineException {
		return;
	}

	public WorksheetTemplate getTemplate() {
		return template;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.utilities.engines.IEngineInstance#getAnalysisState()
	 */
	//@Override
	public IEngineAnalysisState getAnalysisState() {
		return this.getTemplate().getWorkSheetDefinition();
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.utilities.engines.IEngineInstance#setAnalysisState(it.eng.spagobi.utilities.engines.IEngineAnalysisState)
	 */
	//@Override
	public void setAnalysisState(IEngineAnalysisState analysisState) {
		this.getTemplate().setWorkSheetDefinition((WorkSheetDefinition)analysisState);
	}

	public IDataSet getDataSet() {
		if(dataSet!=null){
			return dataSet; 
		}
		if(template!=null){
			return template.getDataSet(); 
		}
		return null;
	}

	public void setDataSet(IDataSet dataSet) {
		this.dataSet = dataSet; 
	}

	public IDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public QbeEngineInstance getQbeEngineInstance() {
		return template.getQbeEngineInstance();
	}
	
	public void setQbeEngineInstance(QbeEngineInstance qbeEngineInstance){
		template.setQbeEngineInstance(qbeEngineInstance);
	}
	
	private void setTemporaryTableName() {
		logger.debug("IN");
		String temporaryTableNameRoot = (String) this.getEnv().get(SpagoBIConstants.TEMPORARY_TABLE_ROOT_NAME);
		logger.debug("Temporary table name root specified on the environment : [" + temporaryTableNameRoot + "]");
		// if temporaryTableNameRadix is not specified on the environment, create a new name using the user profile
		if (temporaryTableNameRoot == null) {
			logger.debug("Temporary table name root not specified on the environment, creating a new one using user identifier ...");
			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
			temporaryTableNameRoot = userProfile.getUserId().toString();
		}
		logger.debug("Temporary table root name : [" + temporaryTableNameRoot + "]");
		String temporaryTableNameComplete = TemporaryTableManager.getTableName(temporaryTableNameRoot);
		logger.debug("Temporary table name : [" + temporaryTableNameComplete + "]. Putting it into the environment");
		this.getEnv().put(SpagoBIConstants.TEMPORARY_TABLE_NAME, temporaryTableNameComplete);
		logger.debug("OUT : temporaryTableName = [" + temporaryTableNameComplete + "]");
		this.temporaryTableName = temporaryTableNameComplete;
	}
	
	public String getTemporaryTableName() {
		return temporaryTableName;
	}

	public IDataSource getDataSourceForWriting() {
		IDataSource datasource = (IDataSource) this.getEnv().get(EngineConstants.DATASOURCE_FOR_WRITING);
		if (datasource == null) {
			throw new SpagoBIEngineRuntimeException("Datasource for writing not defined!");
		}
		return datasource;
//		if(dataSource.getHibDialectClass().toLowerCase().contains("hive")){
//			if(dataSourceForWriting==null || dataSet.isPersisted()){
//				return dataSet.getDataSourceForReading();
//			}
//			return dataSourceForWriting;
//		}
//		return dataSource;
	}
	
	
}
