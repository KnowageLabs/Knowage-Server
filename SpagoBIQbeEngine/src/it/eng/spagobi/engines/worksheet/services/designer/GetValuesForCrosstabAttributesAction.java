/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.services.designer;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.QbeEngineStaticVariables;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.worksheet.WorksheetEngineInstance;
import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.engines.worksheet.bo.Sheet;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.engines.worksheet.exceptions.WrongConfigurationForFiltersOnDomainValuesException;
import it.eng.spagobi.engines.worksheet.services.AbstractWorksheetEngineAction;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.FilteringBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.DataStoreFilter;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStoreFilter;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class GetValuesForCrosstabAttributesAction extends AbstractWorksheetEngineAction {	
	
	private static final long serialVersionUID = 4830778339451786311L;

	// INPUT PARAMETERS
	public static final String ALIAS = "ALIAS";
	public static final String ENTITY_ID = "ENTITY_ID";
	public static final String LIMIT = "limit";
	public static final String START = "start";
	public static final String FILTERS = "FILTERS";
	public static final String SHEET = "sheetName";
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(GetValuesForCrosstabAttributesAction.class);
    
	
	public void service(SourceBean request, SourceBean response)  {				
				
		IDataStore dataStore = null;
		JSONObject gridDataFeed = null;
		
		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;
					
		logger.debug("IN");
		
		try {
		
			super.service(request, response);
			
			totalTimeMonitor = MonitorFactory.start("WorksheetEngine.getValuesForCrosstabAttributesAction.totalTime");
			
			String fieldName = getAttributeAsString( ENTITY_ID );
			Assert.assertNotNull(fieldName, "Parameter [" + ENTITY_ID + "] cannot be null in oder to execute " + this.getActionName() + " service");
			logger.debug("Parameter [" + ENTITY_ID + "] is equals to [" + fieldName + "]");
			String alias = getAttributeAsString( ALIAS );
			logger.debug("Parameter [" + ALIAS + "] is equals to [" + alias + "]");
			Integer start = getAttributeAsInteger( START );	
			logger.debug("Parameter [" + START + "] is equals to [" + start + "]");
			Integer limit = getAttributeAsInteger( LIMIT );
			logger.debug("Parameter [" + LIMIT + "] is equals to [" + limit + "]");
			String sheetName = this.getAttributeAsString(SHEET);
			logger.debug("Parameter [" + SHEET + "] is equals to [" + sheetName + "]");
			
			// update worksheet definition 
			JSONObject worksheetDefinitionJSON = getAttributeAsJSONObject(QbeEngineStaticVariables.WORKSHEET_DEFINITION_LOWER );
			if (worksheetDefinitionJSON != null) {
				logger.debug("Updating worksheet definition:");
				logger.debug(worksheetDefinitionJSON);
				updateWorksheetDefinition(worksheetDefinitionJSON);
			}
			
			IDataStoreFilter filter = getDataStoreFilterIfAny();
			
			WorksheetEngineInstance engineInstance = this.getEngineInstance();
			Assert.assertNotNull(engineInstance, "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of WorksheetEngineInstance class");
	
			IDataSet dataset = engineInstance.getDataSet();
			Assert.assertNotNull(dataset, "The engine instance is missing the dataset!!");
			persistDataSet();
			
			// set all filters, because getDomainValues() method may depend on them
			if (dataset.hasBehaviour(FilteringBehaviour.ID)) {
				logger.debug("Dataset has FilteringBehaviour.");
				FilteringBehaviour filteringBehaviour = (FilteringBehaviour) dataset.getBehaviour(FilteringBehaviour.ID);
				WorkSheetDefinition workSheetDefinition = (WorkSheetDefinition) engineInstance.getAnalysisState();
				List<Attribute> globalFilters = workSheetDefinition.getGlobalFilters();
				List<Attribute> sheetFilters = new ArrayList<Attribute>();
				if (sheetName != null) {
					Sheet aSheet = workSheetDefinition.getSheet(sheetName);
					sheetFilters = aSheet.getFiltersOnDomainValues();
				}
				Map<String, List<String>> filters = null;
				try {
					filters = WorkSheetDefinition.mergeDomainValuesFilters(globalFilters, sheetFilters);
				} catch (WrongConfigurationForFiltersOnDomainValuesException e) {
					throw new SpagoBIEngineServiceException(this.getActionName(), e.getMessage(), e);
				}
				logger.debug("Setting filters on domain values : " + filters);
				filteringBehaviour.setFilters(filters);
			}

			dataStore = dataset.getDomainValues(fieldName, start, limit, filter);
			if (alias != null) {
				IMetaData metadata = dataStore.getMetaData();
				metadata.changeFieldAlias(0, alias);
			}
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(JSONDataWriter.PROPERTY_PUT_IDS, Boolean.FALSE);
			/* 
			 * The JSONDataWriter uses "column_" + index as field name and therefore the actual field name is lost.
			 * The DomainValuesJSONDataWriter uses the field name as column name instead.
			 * In this case we use the field name because it is easier to filter the domain values, 
			 * since we don't save the domain values' store anywhere.
			 * That's way don't use JSONDataWriter here.
			 * TODO change JSONDataWriter to use the field name as column name and change any class (java and javascript) 
			 * based on "column_" + index convention
			 */
			JSONDataWriter writer = new DomainValuesJSONDataWriter(props);  
			gridDataFeed = (JSONObject) writer.write(dataStore);
			
			// the first column contains the actual domain values, we must put this information into the response
			JSONObject metadataJSON = gridDataFeed.getJSONObject("metaData");
			JSONArray fieldsMetaDataJSON = metadataJSON.getJSONArray("fields");
			JSONObject firstColumn = fieldsMetaDataJSON.getJSONObject(1); // remember that JSONDataWriter puts a recNo column as first column
			String valueColumnName = firstColumn.getString("name");
			String descriptionColumnName = null;
			IMetaData metadata = dataStore.getMetaData();
			int fieldsCount = metadata.getFieldCount();
			JSONObject secondColumn = null;
			if (fieldsCount > 1) {
				// there are 2 or more columns: the first column contains the values, while the second column contains the descriptions
				secondColumn = fieldsMetaDataJSON.getJSONObject(2);
				descriptionColumnName = secondColumn.getString("name");
			} else {
				descriptionColumnName = valueColumnName;
			}
			
			// those information are useful to understand the column that contains the actual value and the column that contains the descriptions
			metadataJSON.put("valueField", valueColumnName); 
			metadataJSON.put("displayField", valueColumnName);
			metadataJSON.put("descriptionField", descriptionColumnName);
			
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

	private IDataStoreFilter getDataStoreFilterIfAny() throws JSONException {
		IDataStoreFilter filter = null;
		if (this.requestContainsAttribute( FILTERS ) ) {
			JSONObject filtersJSON = getAttributeAsJSONObject( FILTERS );
			String field = (String) filtersJSON.get(SpagoBIConstants.COLUMN_FILTER);
			String value = (String) filtersJSON.get(SpagoBIConstants.VALUE_FILTER);
			String operator = (String) filtersJSON.get(SpagoBIConstants.TYPE_FILTER);
			String type = (String) filtersJSON.get(SpagoBIConstants.TYPE_VALUE_FILTER);
			logger.debug("Filter on data store: field = [" + field + "], value = [" + value + "], operator = [" + operator + "], type = [" + type + "]");
			filter = new DataStoreFilter(field, value, operator, type);
		} else {
			logger.debug("No filter on data store found");
		}
		return filter;
	}
	
	private class DomainValuesJSONDataWriter extends JSONDataWriter {
		
		public DomainValuesJSONDataWriter(Map<String, Object> properties) {
			super(properties);
		}
		
		@Override
		protected String getFieldName(IFieldMetaData fieldMetaData, int i) {
			return fieldMetaData.getName();
		}
		
	}
	
}
