/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.serializer.json;

import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.qbe.serializer.IDeserializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.engines.worksheet.bo.Field;
import it.eng.spagobi.engines.worksheet.bo.FieldOption;
import it.eng.spagobi.engines.worksheet.bo.FieldOptions;
import it.eng.spagobi.engines.worksheet.bo.Filter;
import it.eng.spagobi.engines.worksheet.bo.Measure;
import it.eng.spagobi.engines.worksheet.bo.Serie;
import it.eng.spagobi.engines.worksheet.bo.Sheet;
import it.eng.spagobi.engines.worksheet.bo.Sheet.FiltersPosition;
import it.eng.spagobi.engines.worksheet.bo.SheetContent;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.engines.worksheet.bo.WorksheetFieldsOptions;
import it.eng.spagobi.engines.worksheet.widgets.ChartDefinition;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition;
import it.eng.spagobi.engines.worksheet.widgets.TableDefinition;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorkSheetJSONDeserializer implements IDeserializer {

    public static transient Logger logger = Logger.getLogger(WorkSheetJSONDeserializer.class);
    
	public WorkSheetDefinition deserialize(Object o) throws SerializationException {
		WorkSheetDefinition workSheetDefinition = null;
		JSONObject workSheetDefinitionJSON = null;
		
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(o, "Object to be deserialized cannot be null");
			
			if(o instanceof String) {
				logger.debug("Deserializing string [" + (String)o + "]");
				try {
					workSheetDefinitionJSON = new JSONObject( (String)o );
				} catch(Throwable t) {
					logger.debug("Object to be deserialized must be string encoding a JSON object");
					throw new SerializationException("An error occurred while deserializing query: " + (String)o, t);
				}
			} else if(o instanceof JSONObject) {
				workSheetDefinitionJSON = (JSONObject)o;
			} else {
				Assert.assertUnreachable("Object to be deserialized must be of type string or of type JSONObject, not of type [" + o.getClass().getName() + "]");
			}
			
			workSheetDefinition  = new WorkSheetDefinition();
			
			try {
				deserializeSheets(workSheetDefinitionJSON, workSheetDefinition);
				deserializeGlobalFilters(workSheetDefinitionJSON, workSheetDefinition);
				deserializeOptions(workSheetDefinitionJSON, workSheetDefinition);
			} catch (Exception e) {
				throw new SerializationException("An error occurred while deserializing worksheet: " + workSheetDefinitionJSON.toString(), e);
			}

		} finally {
			logger.debug("OUT");
		}
		logger.debug("Worksheet deserialized");
		return workSheetDefinition;
	}
	
	private void deserializeGlobalFilters(JSONObject workSheetDefinitionJSON,
			WorkSheetDefinition workSheetDefinition) throws Exception {
		JSONArray gfJSON = workSheetDefinitionJSON.getJSONArray(WorkSheetSerializationUtils.GLOBAL_FILTERS);
		List<Attribute> globalFilters = new ArrayList<Attribute>();
		for (int i = 0; i < gfJSON.length(); i++) {
			globalFilters.add(deserializeAttribute(gfJSON.getJSONObject(i)));
		}
		workSheetDefinition.setGlobalFilters(globalFilters);
	}
	
	private void deserializeOptions(JSONObject workSheetDefinitionJSON,
			WorkSheetDefinition workSheetDefinition) throws Exception {
		JSONArray optionsJSON = workSheetDefinitionJSON.getJSONArray(WorkSheetSerializationUtils.FIELDS_OPTIONS);
		WorksheetFieldsOptions options = new WorksheetFieldsOptions();
		for (int i = 0 ; i < optionsJSON.length() ; i++) {
			
			JSONObject aField = optionsJSON.getJSONObject(i);
			String nature = aField.getString("nature");
			Field field = null;
			if (nature.equals("postLineCalculated") || nature.equals("segment_attribute") || nature.equals("attribute")) {
				Attribute attribute = (Attribute) SerializationManager.deserialize(aField, "application/json", Attribute.class);
				field = attribute;
			} else {
				Measure measure = (Measure) SerializationManager.deserialize(aField, "application/json", Measure.class);
				field = measure;
			}
			
			JSONObject optionsForFieldJSON = aField.getJSONObject(WorkSheetSerializationUtils.OPTIONS);
			Iterator optionsForFieldKeysIt = optionsForFieldJSON.keys();
			List<FieldOption> fieldOptionList = new ArrayList<FieldOption>();
			while (optionsForFieldKeysIt.hasNext()) {
				String name = (String) optionsForFieldKeysIt.next();
				Object value = optionsForFieldJSON.get(name);
				FieldOption o = WorksheetFieldsOptions.createOption(field, name, value);
				fieldOptionList.add(o);
			}
			
			FieldOptions fieldOptions = new FieldOptions();
			fieldOptions.setField(field);
			fieldOptions.setOptions(fieldOptionList);
			
			options.addFieldOptions(fieldOptions);
			
		}
		workSheetDefinition.setFieldsOptions(options);
	}

	private Attribute deserializeAttribute(JSONObject jsonObject) throws SerializationException {
		Attribute attribute = (Attribute) SerializationManager.deserialize(jsonObject, "application/json", Attribute.class);
		return attribute;
	}

	/**
	 * Deserialize the list of sheets
	 * @param crosstabDefinitionJSON
	 * @param crosstabDefinition
	 * @throws Exception
	 */
	private void deserializeSheets(JSONObject crosstabDefinitionJSON, WorkSheetDefinition crosstabDefinition) throws Exception {
		JSONArray sheetsJSON = crosstabDefinitionJSON.getJSONArray(WorkSheetSerializationUtils.SHEETS);
		List<Sheet> workSheets = new ArrayList<Sheet>();
		for(int i=0; i<sheetsJSON.length(); i++){
			workSheets.add(deserializeSheet(sheetsJSON.getJSONObject(i)));
		}
		crosstabDefinition.setSheets(workSheets);
	}
	
	/**
	 * Deserialize the Sheet
	 * @param sheetJSON
	 * @return
	 * @throws Exception
	 */
	private Sheet deserializeSheet(JSONObject sheetJSON) throws Exception {
		String name = sheetJSON.getString(WorkSheetSerializationUtils.NAME);
		JSONObject header = sheetJSON.optJSONObject(WorkSheetSerializationUtils.HEADER);
		String layout = sheetJSON.optString(WorkSheetSerializationUtils.LAYOUT);
		JSONObject footer = sheetJSON.optJSONObject(WorkSheetSerializationUtils.FOOTER);
		
		JSONObject filtersJSON = sheetJSON.optJSONObject(WorkSheetSerializationUtils.FILTERS);
		List<Filter> filters = deserializeSheetFilters(filtersJSON);
		FiltersPosition position = FiltersPosition.TOP;
		if (filtersJSON != null) {
			position = FiltersPosition.valueOf(filtersJSON.getString(WorkSheetSerializationUtils.POSITION).toUpperCase());
		}
		
		JSONArray filtersOnDomainValuesJSON = sheetJSON.optJSONArray(WorkSheetSerializationUtils.FILTERS_ON_DOMAIN_VALUES);
		List<Attribute> filtersOnDomainValues = deserializeSheetFiltersOnDomainValues(filtersOnDomainValuesJSON);
		
		logger.debug("Deserializing sheet " + name);
		SheetContent content = deserializeContent(sheetJSON);
		logger.debug("Sheet " + name + " deserialized successfully");
		
		return new Sheet(name, layout, header, filters, position, content, filtersOnDomainValues, footer);
	}
	
	private SheetContent deserializeContent(JSONObject sheetJSON) throws Exception {
		SheetContent toReturn = null;
		JSONObject content = sheetJSON.optJSONObject(WorkSheetSerializationUtils.CONTENT);
		if (content == null) {
			logger.warn("Sheet content not found for sheet [" + sheetJSON.getString(WorkSheetSerializationUtils.NAME) + "].");
			return null;
		}
		String designer = content.getString(WorkSheetSerializationUtils.DESIGNER);
		if (WorkSheetSerializationUtils.DESIGNER_PIVOT.equals(designer)) {
			toReturn = (CrosstabDefinition) SerializationManager.deserialize(content.getJSONObject(WorkSheetSerializationUtils.CROSSTABDEFINITION), "application/json", CrosstabDefinition.class);
			((CrosstabDefinition) toReturn).setStatic(false);
		} else if (WorkSheetSerializationUtils.DESIGNER_STATIC_PIVOT.equals(designer)) {
			toReturn = (CrosstabDefinition) SerializationManager.deserialize(content.getJSONObject(WorkSheetSerializationUtils.CROSSTABDEFINITION), "application/json", CrosstabDefinition.class);
			((CrosstabDefinition) toReturn).setStatic(true);
		} else if (WorkSheetSerializationUtils.DESIGNER_TABLE.equals(designer)) {
			toReturn = deserializeTable(content);
		} else {
			toReturn = deserializeChart(content);
		}
		return toReturn;
	}

	private SheetContent deserializeChart(JSONObject content)
			throws JSONException, SerializationException {
		SheetContent toReturn;
		ChartDefinition chart = new ChartDefinition();
		
		JSONObject categoryJSON = content.optJSONObject(WorkSheetSerializationUtils.CATEGORY);
		if (categoryJSON != null) {
			Attribute category = (Attribute) SerializationManager.deserialize(categoryJSON, "application/json", Attribute.class);
			chart.setCategory(category);
		}
		
		JSONObject groupingVariableJSON = content.optJSONObject(WorkSheetSerializationUtils.GROUPING_VARIABLE);
		if (groupingVariableJSON != null) {
			Attribute groupingVariable = (Attribute) SerializationManager.deserialize(groupingVariableJSON, "application/json", Attribute.class);
			chart.setGroupingVariable(groupingVariable);
		}
		
		List<Serie> series = new ArrayList<Serie>();
		JSONArray seriesJSON = content.getJSONArray(WorkSheetSerializationUtils.SERIES);
		SerieJSONDeserializer deserialier = new SerieJSONDeserializer();
		for (int i = 0; i < seriesJSON.length(); i++) {
			JSONObject aSerie = seriesJSON.getJSONObject(i);
			Serie serie = deserialier.deserialize(aSerie);
			series.add(serie);
		}
		chart.setSeries(series);
		
		content.remove(WorkSheetSerializationUtils.CATEGORY);
		content.remove(WorkSheetSerializationUtils.SERIES);
		chart.setConfig(content);
		
		toReturn = chart;
		return toReturn;
	}

	private SheetContent deserializeTable(JSONObject content)
			throws JSONException, SerializationException {
		SheetContent toReturn;
		TableDefinition table = new TableDefinition();
		JSONArray fields = content.getJSONArray(WorkSheetSerializationUtils.VISIBLE_SELECT_FIELDS);
		for (int i = 0; i < fields.length(); i++) {
			JSONObject aField = fields.getJSONObject(i);
			String nature = aField.getString("nature");
			if (nature.equals("postLineCalculated") || nature.equals("segment_attribute") || nature.equals("attribute")) {
				Attribute attribute = (Attribute) SerializationManager.deserialize(aField, "application/json", Attribute.class);
				table.addField(attribute);
			} else {
				Measure measure = (Measure) SerializationManager.deserialize(aField, "application/json", Measure.class);
				table.addField(measure);
			}
		}
		toReturn = table;
		return toReturn;
	}

	private List<Filter> deserializeSheetFilters(JSONObject filtersJSON) throws Exception {
		List<Filter> toReturn = new ArrayList<Filter>();
		FilterJSONDeserializer deserialier = new FilterJSONDeserializer();
		if (filtersJSON != null) {
			JSONArray filters = filtersJSON.optJSONArray(QuerySerializationConstants.FILTERS);
			if (filters != null && filters.length() > 0) {
				for (int i = 0; i < filters.length(); i++) {
					Filter attribute = deserialier.deserialize(filters.getJSONObject(i));
					toReturn.add(attribute);
				}
			}
		}
		return toReturn;
	}
	
	private List<Attribute> deserializeSheetFiltersOnDomainValues(JSONArray filtersJSON) throws Exception {
		List<Attribute> toReturn = new ArrayList<Attribute>();
		if (filtersJSON != null && filtersJSON.length() > 0) {
			for (int i = 0; i < filtersJSON.length(); i++) {
				Attribute attribute = deserializeAttribute(filtersJSON.getJSONObject(i));
				toReturn.add(attribute);
			}
		}
		return toReturn;
	}
		
}