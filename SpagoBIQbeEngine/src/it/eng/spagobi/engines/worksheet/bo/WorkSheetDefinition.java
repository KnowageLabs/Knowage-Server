/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.bo;

import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.qbe.analysisstateloaders.worksheet.IWorksheetStateLoader;
import it.eng.spagobi.engines.qbe.analysisstateloaders.worksheet.WorksheetStateLoaderFactory;
import it.eng.spagobi.engines.worksheet.exceptions.WrongConfigurationForFiltersOnDomainValuesException;
import it.eng.spagobi.utilities.engines.EngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.json.AbstractJSONDecorator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * 			Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class WorkSheetDefinition extends EngineAnalysisState {
	
	private static transient Logger logger = Logger.getLogger(WorkSheetDefinition.class);
	
	public static final String CURRENT_VERSION = "2";
	
	public static final WorkSheetDefinition EMPTY_WORKSHEET;
	
	static {
		EMPTY_WORKSHEET = new WorkSheetDefinition();
	}
	
	private List<Sheet> sheets;
	
	private List<Attribute> globalFilters;
	private WorksheetFieldsOptions fieldsOptions;
	
	public WorkSheetDefinition(){
		sheets = new ArrayList<Sheet>();
		globalFilters = new ArrayList<Attribute>();
		fieldsOptions = new WorksheetFieldsOptions();
	}
	
	public WorkSheetDefinition(List<Sheet> sheets){
		this.sheets = sheets;
	}

	public List<Sheet> getSheets() {
		return sheets;
	}

	public void setSheets(List<Sheet> sheets) {
		this.sheets = sheets;
	}
	
	public List<Attribute> getGlobalFilters() {
		return globalFilters;
	}

	public void setGlobalFilters(List<Attribute> globalFilters) {
		this.globalFilters = globalFilters;
	}

	public void setFieldsOptions(WorksheetFieldsOptions fieldsOptions) {
		this.fieldsOptions = fieldsOptions;
	}
	
	public WorksheetFieldsOptions getFieldsOptions() {
		return this.fieldsOptions;
	}
	
	public Map<String, List<String>> getGlobalFiltersAsMap() {
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
		if (globalFilters != null && globalFilters.size() > 0) {
			Iterator<Attribute> it = globalFilters.iterator();
			while (it.hasNext()) {
				Attribute attribute = it.next();
				toReturn.put(attribute.getEntityId(), attribute.getValuesAsList());
			}
		}
		return toReturn;
	}
	
	public JSONObject getConf(AbstractJSONDecorator decorator){
		try {
			JSONObject toReturn = (JSONObject) SerializationManager.serialize(this, "application/json");
			if (decorator != null) {
				decorator.decorate(toReturn);
			}
			return toReturn;
		} catch (Exception e) {
			 throw new SpagoBIEngineRuntimeException("Error while serializing worksheet definition", e);
		}

	}
	
	public Sheet getSheet(String name) {
		Sheet toReturn = null;
		Iterator<Sheet> it = this.sheets.iterator();
		while (it.hasNext()) {
			Sheet sheet = it.next();
			if (sheet.getName().equals(name)) {
				toReturn = sheet;
				break;
			}
		}
		return toReturn;
	}

	public void load(byte[] rowData) throws SpagoBIEngineException {
		String str = null;
		JSONObject worksheetStateJSON = null;
		JSONObject rowDataJSON = null;
		String encodingFormatVersion;
		
		logger.debug("IN");

		try {
			str = new String( rowData );
			logger.debug("loading form state from row data [" + str + "] ...");
			
			rowDataJSON = new JSONObject(str);
			try {
				//encodingFormatVersion = rowDataJSON.getString("version");
				encodingFormatVersion = String.valueOf(rowDataJSON.getInt("version"));
			} catch (JSONException e) {
				logger.debug("no version found, default is 0");
				encodingFormatVersion = "0";
			}
			
			logger.debug("Row data encoding version  [" + encodingFormatVersion + "]");
			
			if (encodingFormatVersion.equalsIgnoreCase(CURRENT_VERSION)) {				
				worksheetStateJSON = rowDataJSON;
			} else {
				logger.warn("Row data encoding version [" + encodingFormatVersion + "] does not match with the current version used by the engine [" + CURRENT_VERSION + "] ");
				logger.debug("Converting from encoding version [" + encodingFormatVersion + "] to encoding version [" + CURRENT_VERSION + "]....");
				IWorksheetStateLoader worksheetViewerStateLoader;
				worksheetViewerStateLoader = WorksheetStateLoaderFactory.getInstance().getLoader(encodingFormatVersion);
				if (worksheetViewerStateLoader == null) {
					throw new SpagoBIEngineException("Unable to load data stored in format [" + encodingFormatVersion + "] ");
				}
				worksheetStateJSON = (JSONObject) worksheetViewerStateLoader.load(str);
				logger.debug("Encoding conversion has been executed succesfully");
			}
			
			WorkSheetDefinition workSheetDefinition = (WorkSheetDefinition) SerializationManager.deserialize(worksheetStateJSON, "application/json", WorkSheetDefinition.class);
			this.setSheets(workSheetDefinition.getSheets());
			this.setGlobalFilters(workSheetDefinition.getGlobalFilters());
			this.setFieldsOptions(workSheetDefinition.getFieldsOptions());
			
			logger.debug("analysis state loaded succsfully from row data");
			
		} catch (Exception e) {
			throw new SpagoBIEngineException("Impossible to load form state from raw data", e);
		} finally {
			logger.debug("OUT");
		}
		
	}

	public byte[] store() throws SpagoBIEngineException {
		JSONObject worksheetJSON = null;
		String rowData = null;	
				
		try {
			worksheetJSON = (JSONObject) SerializationManager.serialize(this, "application/json");
			worksheetJSON.put("version", CURRENT_VERSION);
			rowData = worksheetJSON.toString();
		} catch (Throwable e) {
			throw new SpagoBIEngineException("Impossible to store form state", e);
		}
		
		return rowData.getBytes();
	}

	public Map<String, List<String>> getFiltersOnDomainValues() throws WrongConfigurationForFiltersOnDomainValuesException {
		List<Attribute> globalFilters = this.getGlobalFilters(); // the global filters
		List<Attribute> sheetFilters = this.getFiltersOnDomainValuesOnSheets(); // the union of the filters defined in all the sheets
		Map<String, List<String>> toReturn = mergeDomainValuesFilters(globalFilters, sheetFilters);
		return toReturn;
	}

	private List<Attribute> getFiltersOnDomainValuesOnSheets() {
		List<Attribute> toReturn = new ArrayList<Attribute>();
		Iterator<Sheet> it = this.sheets.iterator();
		while (it.hasNext()) {
			Sheet aSheet = it.next();
			List<Attribute> sheetFilters = aSheet.getFiltersOnDomainValues();
			addDomainValuesFilters(toReturn, sheetFilters);
		}
		return toReturn;
	}

	public static void addDomainValuesFilters(List<Attribute> toReturn,
			List<Attribute> sheetFilters) {
		Iterator<Attribute> it = sheetFilters.iterator();
		while (it.hasNext()) {
			Attribute aFilter = it.next();
			if (toReturn.contains(aFilter)) {
				int index = toReturn.indexOf(aFilter);
				Attribute previousFilter = toReturn.get(index);
				List<String> previousValues = previousFilter.getValuesAsList();
				List<String> newValues = aFilter.getValuesAsList();
				List<String> sum = ListUtils.sum(previousValues, newValues);
				previousFilter.setValues(sum);
			} else {
				Attribute clone = aFilter.clone();
				toReturn.add(clone);
			}
		}
	}
	

	public static Map<String, List<String>> mergeDomainValuesFilters(
			List<Attribute> globalFilters, List<Attribute> sheetFilters) throws WrongConfigurationForFiltersOnDomainValuesException {
		Iterator<Attribute> globalFiltersIt = globalFilters.iterator();
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
		while (globalFiltersIt.hasNext()) {
			Attribute aGlobalFilter = globalFiltersIt.next();
			if (sheetFilters.contains(aGlobalFilter)) { // the filter is defined globally and also on sheets
				// wins the more restrictive filter
				int index = sheetFilters.indexOf(aGlobalFilter);
				Attribute sheetsFilter = sheetFilters.get(index);
				List<String> aGlobalFilterValues = aGlobalFilter.getValuesAsList();
				List<String> sheetsFilterValues = sheetsFilter.getValuesAsList();
				if (aGlobalFilterValues.containsAll(sheetsFilterValues)) {
					// the sheets filters are less or equal to the global filters (this should always happen)
					toReturn.put(aGlobalFilter.getEntityId(), sheetsFilterValues);
				} else {
					logger.error("The global filter on field " + aGlobalFilter.getAlias() + " is overridden by the sheet.");
					throw new WrongConfigurationForFiltersOnDomainValuesException("The global filter on field " + aGlobalFilter.getAlias() + " is overridden by the sheet. Please expand the global filter's values.");
				}
			} else {
				toReturn.put(aGlobalFilter.getEntityId(), aGlobalFilter.getValuesAsList());
			}
		}
		Iterator<Attribute> sheetFiltersIt = sheetFilters.iterator();
		while (sheetFiltersIt.hasNext()) {
			Attribute aSheetsFilter = sheetFiltersIt.next();
			if (toReturn.containsKey(aSheetsFilter.getEntityId())) {
				// conflict already solved
				continue;
			}
			toReturn.put(aSheetsFilter.getEntityId(), aSheetsFilter.getValuesAsList());
		}
		return toReturn;
	}

	public List<Field> getAllFields() {
		List<Field> toReturn = new ArrayList<Field>();
		List<Attribute> globalFilters = this.getGlobalFilters();
		toReturn.addAll(globalFilters);
		List<Sheet> sheets = this.getSheets();
		Iterator<Sheet> it = sheets.iterator();
		while (it.hasNext()) {
			Sheet sheet = it.next();
			List<Field> sheetFields = sheet.getAllFields();
			Iterator<Field> sheetFieldsIt = sheetFields.iterator();
			while (sheetFieldsIt.hasNext()) {
				Field field = sheetFieldsIt.next();
				if (!toReturn.contains(field)) {
					toReturn.add(field);
				}
			}
		}
		return toReturn;
	}
	
}
