/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.services.runtime;

import it.eng.qbe.query.WhereField;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.QbeEngineStaticVariables;
import it.eng.spagobi.engines.worksheet.WorksheetEngineInstance;
import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.engines.worksheet.bo.Measure;
import it.eng.spagobi.engines.worksheet.serializer.json.WorkSheetSerializationUtils;
import it.eng.spagobi.engines.worksheet.services.AbstractWorksheetEngineAction;
import it.eng.spagobi.engines.worksheet.utils.crosstab.CrosstabQueryCreator;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.FilteringBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * 			Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */

public class ExecuteWorksheetQueryAction extends AbstractWorksheetEngineAction {
	
	private static final long serialVersionUID = -9134072368475124558L;
	
	// INPUT PARAMETERS
	public static final String LIMIT = "limit";
	public static final String START = "start";
	public static final String OPTIONAL_VISIBLE_COLUMNS = QbeEngineStaticVariables.OPTIONAL_VISIBLE_COLUMNS;
	//public static final String OPTIONAL_FILTERS = QbeEngineStaticVariables.OPTIONAL_FILTERS;
	public static final String SHEET = "sheetName";
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(LoadCrosstabAction.class);
	
	public void service(SourceBean request, SourceBean response)  {				
		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;
		
		JSONObject gridDataFeed = null;
		IDataStore dataStore = null;
		JSONArray jsonVisibleSelectFields = null;
		
		logger.debug("IN");
		
		try {
		
			super.service(request, response);	
			
			totalTimeMonitor = MonitorFactory.start("WorksheetEngine.executeWorksheetQueryAction.totalTime");
			
			jsonVisibleSelectFields = getAttributeAsJSONArray( OPTIONAL_VISIBLE_COLUMNS );
			logger.debug("jsonVisibleSelectFields input: " + jsonVisibleSelectFields);
			Assert.assertTrue(jsonVisibleSelectFields != null && jsonVisibleSelectFields.length() > 0, "jsonVisibleSelectFields input not valid");
			
			dataStore = executeQuery(jsonVisibleSelectFields);
			
			gridDataFeed = serializeDataStore(dataStore);
			
			try {
				writeBackToClient( new JSONSuccess(gridDataFeed) );
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
	

	public JSONObject serializeDataStore(IDataStore dataStore)  {
		Map<String, Object> properties = new HashMap<String, Object>();
		JSONArray fieldOptions = this.getAttributeAsJSONArray(WorkSheetSerializationUtils.WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS);
		properties.put(JSONDataWriter.PROPERTY_FIELD_OPTION, fieldOptions);
		JSONDataWriter dataSetWriter = new JSONDataWriter(properties);
		JSONObject gridDataFeed = (JSONObject)dataSetWriter.write(dataStore);
		return gridDataFeed;
	}
	
	protected IDataStore executeQuery(JSONArray jsonVisibleSelectFields) throws Exception {
		String sheetName = this.getAttributeAsString(SHEET);
		JSONObject optionalFilters = getAttributeAsJSONObject(QbeEngineStaticVariables.FILTERS);
		return executeQuery(jsonVisibleSelectFields, optionalFilters, sheetName);
	}
	
	
	protected IDataStore executeQuery(JSONArray jsonVisibleSelectFields, JSONObject optionalFilters, String sheetName) throws Exception {
		return executeQuery(jsonVisibleSelectFields, optionalFilters, sheetName, null);
	}
	
	protected IDataStore executeQuery(JSONArray jsonVisibleSelectFields, JSONObject optionalFilters, String sheetName, JSONArray fieldOptions) throws Exception {
		
		IDataStore dataStore = null;
		
		Integer limit;
		Integer start;
		
		start = getAttributeAsInteger( START );	
		logger.debug("Parameter [" + START + "] is equals to [" + start + "]");
		
		limit = getAttributeAsInteger( LIMIT );
		logger.debug("Parameter [" + LIMIT + "] is equals to [" + limit + "]");
		
		WorksheetEngineInstance engineInstance = getEngineInstance();
		Assert.assertNotNull(engineInstance, "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
		
		List<String> fieldNames = new ArrayList<String>();
		List<Attribute> onTableAttributes = new ArrayList<Attribute>();
		for (int i = 0; i < jsonVisibleSelectFields.length(); i++) {
			JSONObject aField = jsonVisibleSelectFields.getJSONObject(i);
			String nature = aField.getString("nature");
			if (nature.equals("postLineCalculated") || nature.equals("segment_attribute") || nature.equals("attribute")) {
				Attribute attribute = (Attribute) SerializationManager.deserialize(aField, "application/json", Attribute.class);
				fieldNames.add(attribute.getEntityId());
				onTableAttributes.add(attribute);
			} else {
				Measure measure = (Measure) SerializationManager.deserialize(aField, "application/json", Measure.class);
				fieldNames.add(measure.getEntityId());
			}
		}
		
		// persist dataset into temporary table	
		IDataSetTableDescriptor descriptor = this.persistDataSet();
		
		IDataSet dataset = engineInstance.getDataSet();
		// build SQL query against temporary table
		List<WhereField> whereFields = new ArrayList<WhereField>();
		if (!dataset.hasBehaviour(FilteringBehaviour.ID)) {
			Map<String, List<String>> globalFilters = getGlobalFiltersOnDomainValues();
			List<WhereField> temp = transformIntoWhereClauses(globalFilters);
			whereFields.addAll(temp);
		}

		Map<String, List<String>> sheetFilters = getSheetFiltersOnDomainValues(sheetName);
		List<WhereField> temp = transformIntoWhereClauses(sheetFilters);
		whereFields.addAll(temp);
		
		temp = getOptionalFilters(optionalFilters);
		whereFields.addAll(temp);
		
		String worksheetQuery = this.buildSqlStatement(fieldNames, descriptor, whereFields);
		// execute SQL query against temporary table
		logger.debug("Executing query on temporary table : " + worksheetQuery);
		dataStore = this.executeWorksheetQuery(worksheetQuery, start, limit);
		LogMF.debug(logger, "Query on temporary table executed successfully; datastore obtained: {0}", dataStore);
		Assert.assertNotNull(dataStore, "Datastore obatined is null!!");
		/* since the datastore, at this point, is a JDBC datastore, 
		* it does not contain information about measures/attributes, fields' name...
		* therefore we adjust its metadata
		*/
		this.adjustMetadata((DataStore) dataStore, dataset, descriptor,fieldOptions);
		LogMF.debug(logger, "Adjusted metadata: {0}", dataStore.getMetaData());
		logger.debug("Decoding dataset ...");
		this.applyOptions(dataStore);
		dataStore = dataset.decode(dataStore);
		LogMF.debug(logger, "Dataset decoded: {0}", dataStore);
		
		return dataStore;
	}

	private String buildSqlStatement(List<String> fieldNames,
			IDataSetTableDescriptor descriptor, List<WhereField> filters) {
		IDataSource dataSource = descriptor.getDataSource();
		return CrosstabQueryCreator.getTableQuery(fieldNames, false, descriptor, filters, dataSource);	
	}
	

}
