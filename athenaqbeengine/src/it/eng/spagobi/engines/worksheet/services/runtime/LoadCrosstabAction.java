/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.services.runtime;

import it.eng.qbe.query.WhereField;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.QbeEngineStaticVariables;
import it.eng.spagobi.engines.qbe.crosstable.CrossTab;
import it.eng.spagobi.engines.worksheet.WorksheetEngineInstance;
import it.eng.spagobi.engines.worksheet.services.AbstractWorksheetEngineAction;
import it.eng.spagobi.engines.worksheet.utils.crosstab.CrosstabQueryCreator;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.FilteringBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class LoadCrosstabAction extends AbstractWorksheetEngineAction {	
	
	// INPUT PARAMETERS
	private static final String CROSSTAB_DEFINITION = QbeEngineStaticVariables.CROSSTAB_DEFINITION;
	//private static final String OPTIONAL_FILTERS = QbeEngineStaticVariables.OPTIONAL_FILTERS;
	public static final String SHEET = "sheetName";
	
	public static final String OUTPUT_TYPE = "OUTPUT_TYPE";
	public enum OutputType {JSON, HTML};

	private static final long serialVersionUID = -5780454016202425492L;

	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(LoadCrosstabAction.class);
    public static transient Logger auditlogger = Logger.getLogger("audit.query");
    
	
	public void service(SourceBean request, SourceBean response)  {				
				
		CrossTab crossTab;
		IDataStore valuesDataStore = null;
		CrosstabDefinition crosstabDefinition = null;
		
		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;
					
		logger.debug("IN");
		
		try {
		
			super.service(request, response);	
			
			totalTimeMonitor = MonitorFactory.start("WorksheetEngine.loadCrosstabAction.totalTime");
			
			// start reading input parameters
			JSONObject crosstabDefinitionJSON = getAttributeAsJSONObject( CROSSTAB_DEFINITION );			
			logger.debug("Parameter [" + crosstabDefinitionJSON + "] is equals to [" + crosstabDefinitionJSON.toString() + "]");
			Assert.assertNotNull(crosstabDefinitionJSON, "Parameter [" + CROSSTAB_DEFINITION + "] cannot be null in oder to execute " + this.getActionName() + " service");
			
			String sheetName = this.getAttributeAsString(SHEET);
			logger.debug("Parameter [" + SHEET + "] is equals to [" + sheetName + "]");
			
			JSONObject optionalFilters = getAttributeAsJSONObject(QbeEngineStaticVariables.FILTERS);
			logger.debug("Parameter [" + QbeEngineStaticVariables.FILTERS + "] is equals to [" + optionalFilters + "]");
			// end reading input parameters
			
			// retrieve engine instance
			WorksheetEngineInstance engineInstance = getEngineInstance();
			Assert.assertNotNull(engineInstance, "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");

			// persist dataset into temporary table	
			IDataSetTableDescriptor descriptor = this.persistDataSet();
			
			IDataSet dataset = engineInstance.getDataSet();
			// build SQL query against temporary table
			List<WhereField> whereFields = new ArrayList<WhereField>();
			if (!dataset.hasBehaviour(FilteringBehaviour.ID)) {
				/* 
				 * If the dataset had the FilteringBehaviour, data was already filtered on domain values by the FilteringBehaviour itself.
				 * If the dataset hadn't the FilteringBehaviour, we must pust filters on domain values on query to temporary table 
				 */
				Map<String, List<String>> globalFilters = getGlobalFiltersOnDomainValues();
				LogMF.debug(logger, "Global filters on domain values detected: {0}", globalFilters);
				List<WhereField> temp = transformIntoWhereClauses(globalFilters);
				whereFields.addAll(temp);
			}
			
			/* 
			 * We must consider sheet filters anyway because temporary table contains data for all sheets,
			 * but different sheets could have different filters defined on them
			 */
			Map<String, List<String>> sheetFilters = getSheetFiltersOnDomainValues(sheetName);
			LogMF.debug(logger, "Sheet filters on domain values detected: {0}", sheetFilters);
			List<WhereField> temp = transformIntoWhereClauses(sheetFilters);
			whereFields.addAll(temp);

			temp = getOptionalFilters(optionalFilters);
			whereFields.addAll(temp);

			// deserialize crosstab definition
			crosstabDefinition = (CrosstabDefinition) SerializationManager.deserialize(crosstabDefinitionJSON, "application/json", CrosstabDefinition.class);
						
			String worksheetQuery = null;
			IDataSource dsForTheTemporaryTable = descriptor.getDataSource();
			
			worksheetQuery = this.buildSqlStatement(crosstabDefinition, descriptor, whereFields, dsForTheTemporaryTable);
			// execute SQL query against temporary table
			logger.debug("Executing query on temporary table : " + worksheetQuery);
			valuesDataStore = this.executeWorksheetQuery(worksheetQuery, null, null);
			LogMF.debug(logger, "Query on temporary table executed successfully; datastore obtained: {0}", valuesDataStore);
			Assert.assertNotNull(valuesDataStore, "Datastore obatined is null!!");
			/* since the datastore, at this point, is a JDBC datastore, 
			* it does not contain information about measures/attributes, fields' name and alias...
			* therefore we adjust its metadata
			*/
			this.adjustMetadata((DataStore) valuesDataStore, dataset, descriptor);
			LogMF.debug(logger, "Adjusted metadata: {0}", valuesDataStore.getMetaData());
			logger.debug("Decoding dataset ...");
			this.applyOptions(valuesDataStore);
			dataset.decode(valuesDataStore);
			LogMF.debug(logger, "Dataset decoded: {0}", valuesDataStore);
			
			// serialize crosstab
			if(crosstabDefinition.isPivotTable()){
				//load the crosstab for a crosstab widget (with headers, sum, ...)
				if (crosstabDefinition.isStatic()) {
					crossTab = new CrossTab(valuesDataStore, crosstabDefinition, engineInstance, null);
				} else {
					crossTab = new CrossTab(valuesDataStore, crosstabDefinition, null, null);
				}
			}else{
				//load the crosstab data structure for all other widgets
				crossTab= new CrossTab(valuesDataStore, crosstabDefinition);
			}
			

			try {
				OutputType output = this.getOutputType();
				switch (output) {
					case JSON:
						JSONObject crossTabDefinition = crossTab.getJSONCrossTab();
						writeBackToClient(new JSONSuccess(crossTabDefinition));
						break;
					case HTML: 
						String htmlCode = crossTab.getHTMLCrossTab(this.getLocale());
						writeBackToClient(htmlCode);
						break;
				}
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}
			
		} catch(Throwable t) {
			errorHitsMonitor = MonitorFactory.start("WorksheetEngine.errorHits");
			errorHitsMonitor.stop();
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			if (totalTimeMonitor != null) totalTimeMonitor.stop();
			logger.debug("OUT");
		}	
	}


	private OutputType getOutputType() {
		OutputType toReturn = OutputType.JSON;  // default
		String outputTypeStr = null;
		if (this.requestContainsAttribute(OUTPUT_TYPE)) {
			try {
				outputTypeStr = this.getAttributeAsString(OUTPUT_TYPE);
				toReturn = OutputType.valueOf(outputTypeStr);
			} catch (Exception e) {
				throw new SpagoBIEngineServiceException(getActionName(), "Output type [" + outputTypeStr + "] not valid", e);
			}
		}
		return toReturn;
	}


	/**
	 * Build the sql statement to query the temporary table 
	 * @param crosstabDefinition definition of the crosstab
	 * @param descriptor the temporary table descriptor
	 * @param dataSource the datasource
	 * @param tableName the temporary table name
	 * @return the sql statement to query the temporary table 
	 */
	protected String buildSqlStatement(CrosstabDefinition crosstabDefinition,
			IDataSetTableDescriptor descriptor, List<WhereField> filters, IDataSource dataSource) {
		return CrosstabQueryCreator.getCrosstabQuery(crosstabDefinition, descriptor, filters, dataSource);
	}
	
}
