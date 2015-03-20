/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.serializer.json;

import it.eng.qbe.serializer.ISerializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.engines.worksheet.bo.Field;
import it.eng.spagobi.engines.worksheet.bo.FieldOption;
import it.eng.spagobi.engines.worksheet.bo.FieldOptions;
import it.eng.spagobi.engines.worksheet.bo.Filter;
import it.eng.spagobi.engines.worksheet.bo.Serie;
import it.eng.spagobi.engines.worksheet.bo.Sheet;
import it.eng.spagobi.engines.worksheet.bo.WorksheetFieldsOptions;
import it.eng.spagobi.engines.worksheet.bo.Sheet.FiltersPosition;
import it.eng.spagobi.engines.worksheet.bo.SheetContent;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.engines.worksheet.widgets.ChartDefinition;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition;
import it.eng.spagobi.engines.worksheet.widgets.TableDefinition;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorkSheetJSONSerializer implements ISerializer {

	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(WorkSheetJSONSerializer.class);
    
    
	public Object serialize(Object o) throws SerializationException {
		
		logger.debug("IN");
		logger.debug("Serializing the worksheet");
		
		JSONObject toReturn = null;
		WorkSheetDefinition workSheetDefinition;
				
		Assert.assertNotNull(o, "Input parameter cannot be null");
		Assert.assertTrue(o instanceof WorkSheetDefinition, "Unable to serialize objects of type [" + o.getClass().getName() + "]");
		
		try {
			toReturn = new JSONObject();
			
			workSheetDefinition = (WorkSheetDefinition)o;
			
			JSONArray sheets = serializeSheets(workSheetDefinition.getSheets());
			toReturn.put(WorkSheetSerializationUtils.SHEETS, sheets);
			
			JSONArray globalFilters = serializeGlobalFilters(workSheetDefinition.getGlobalFilters());
			toReturn.put(WorkSheetSerializationUtils.GLOBAL_FILTERS, globalFilters);
			
			JSONArray fieldsOptions = serializeFieldsOptions(workSheetDefinition.getFieldsOptions());
			toReturn.put(WorkSheetSerializationUtils.FIELDS_OPTIONS, fieldsOptions);
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			logger.debug("OUT");
		}
		logger.debug("Worksheet serialized");
		return toReturn;
	}
	
	
	private JSONArray serializeGlobalFilters(List<Attribute> globalFilters) throws SerializationException, JSONException {
		JSONArray globalFiltersJSON = new JSONArray();
		Iterator<Attribute> it = globalFilters.iterator();
		while (it.hasNext()) {
			JSONObject js = (JSONObject) SerializationManager.serialize(it.next(), "application/json");
			globalFiltersJSON.put(js);
		}
		return globalFiltersJSON;
	}
	
	
	private JSONObject serializeSheetFilters(List<Filter> filters, FiltersPosition filtersPosition) throws SerializationException, JSONException {
		JSONArray globalFiltersJSON = serializeSheetFilters(filters);
		JSONObject toReturn = new JSONObject();
		toReturn.put(WorkSheetSerializationUtils.FILTERS, globalFiltersJSON);
		toReturn.put(WorkSheetSerializationUtils.POSITION, filtersPosition.name().toLowerCase());
		return toReturn;
	}
	
	private JSONArray serializeSheetFilters(List<Filter> filters) throws SerializationException, JSONException {
		JSONArray filtersJSON = new JSONArray();
		Attribute a;
		JSONObject js;
		Iterator<Filter> it = filters.iterator();
		FilterJSONSerializer serializer = new FilterJSONSerializer();
		
		while (it.hasNext()) {
			a = it.next();
			js = (JSONObject) serializer.serialize(a);
			filtersJSON.put(js);
		}
		
		return filtersJSON;
	}
	

	private JSONArray serializeSheets(List<Sheet> sheets) throws SerializationException {
		JSONArray jsonSheets = new JSONArray();
		for(int i=0; i<sheets.size(); i++){
			jsonSheets.put(serializeSheet(sheets.get(i)));
		}
		return jsonSheets;
	}
	
	private JSONObject serializeSheet(Sheet sheet) throws SerializationException {
		logger.debug("IN");
		logger.debug("Serializing the sheet " + sheet.getName());
		JSONObject jsonSheet = new JSONObject();
		
		setImageWidth(sheet.getFooter());
		setImageWidth( sheet.getHeader());
		
		try {
				
			jsonSheet.put(WorkSheetSerializationUtils.NAME, sheet.getName());
			jsonSheet.put(WorkSheetSerializationUtils.LAYOUT, sheet.getLayout());
			jsonSheet.put(WorkSheetSerializationUtils.HEADER, sheet.getHeader());
			jsonSheet.put(WorkSheetSerializationUtils.FILTERS, serializeSheetFilters(sheet.getFilters(), sheet.getFiltersPosition()));
			jsonSheet.put(WorkSheetSerializationUtils.CONTENT, serializeContent(sheet.getContent()));
			jsonSheet.put(WorkSheetSerializationUtils.FILTERS_ON_DOMAIN_VALUES, serializeGlobalFilters(sheet.getFiltersOnDomainValues()));
			jsonSheet.put(WorkSheetSerializationUtils.FOOTER, sheet.getFooter());


			
		} catch (Exception e) {
			logger.error("Error serializing the sheet "+sheet.getName(),e);
			throw new SerializationException("Error serializing the sheet "+sheet.getName(),e);
		} finally{
			logger.debug("OUT");
		}
		logger.debug("Serialized the sheet "+sheet.getName());
		return jsonSheet;
		
	}

	private JSONObject serializeContent(SheetContent content) throws SerializationException, JSONException {
		if (content == null) {
			return new JSONObject();
		}
		if (content instanceof CrosstabDefinition) {
			JSONObject toReturn = new JSONObject();
			toReturn.put(WorkSheetSerializationUtils.CROSSTABDEFINITION, 
					(JSONObject) SerializationManager.serialize(content, "application/json"));
			boolean isStatic = ((CrosstabDefinition) content).isStatic();
			toReturn.put(WorkSheetSerializationUtils.DESIGNER, 
					isStatic ? 
							WorkSheetSerializationUtils.DESIGNER_STATIC_PIVOT :
							WorkSheetSerializationUtils.DESIGNER_PIVOT);
			return toReturn;
		}
		if (content instanceof ChartDefinition) {
			return serializeChart((ChartDefinition) content);
		}
		if (content instanceof TableDefinition) {
			return serializeTable((TableDefinition) content);
		}
		else
			throw new SpagoBIEngineRuntimeException("Unknown sheet content type: " + content.getClass().getName());
	}

	private JSONObject serializeTable(TableDefinition table) throws SerializationException, JSONException {
		JSONObject toReturn = new JSONObject();
		JSONArray fieldsJSON = new JSONArray();
		List<Field> fields = table.getFields();
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			fieldsJSON.put(SerializationManager.serialize(field, "application/json"));
		}
		toReturn.put(WorkSheetSerializationUtils.VISIBLE_SELECT_FIELDS, fieldsJSON);
		toReturn.put(WorkSheetSerializationUtils.DESIGNER, WorkSheetSerializationUtils.DESIGNER_TABLE);
		return toReturn;
	}

	private JSONObject serializeChart(ChartDefinition chart) throws SerializationException, JSONException {
		String config = chart.getConfig().toString();
		JSONObject toReturn = new JSONObject(config);
		toReturn.put(WorkSheetSerializationUtils.CATEGORY, SerializationManager.serialize(chart.getCategory(), "application/json"));

		if (chart.getGroupingVariable() != null) {
			toReturn.put(WorkSheetSerializationUtils.GROUPING_VARIABLE, SerializationManager.serialize(chart.getGroupingVariable(), "application/json"));
		}
		
		JSONArray seriesJSON = new JSONArray();
		List<Serie> series = chart.getSeries();
		SerieJSONSerializer serialier = new SerieJSONSerializer();
		for (int i = 0; i < series.size(); i++) {
			seriesJSON.put(serialier.serialize(series.get(i)));
		}
		toReturn.put(WorkSheetSerializationUtils.SERIES, seriesJSON);
		
		return toReturn;
	}
	
    /**
     * Set the with of the image in the template
     * @param title The JSONObject rapresentation of the header/footer
     * @throws Exception
     */
	private static void setImageWidth(JSONObject title) {
		logger.debug("IN");
		
		if(title!=null){
			String s = title.optString("img");
			if(s!=null && !s.equals("") && !s.equals("null")){
				try {
					logger.debug("Image file = "+s);
					File toReturn = null;
					File imagesDir = QbeEngineConfig.getInstance().getWorksheetImagesDir();
					toReturn = new File(imagesDir, s);

					BufferedImage img = ImageIO.read(toReturn);
				    int width= img.getWidth();
					
					title.put("width", width);	
				} catch (Exception e) {
					logger.error("Error loading the image "+s+":  "+e);
				}

			}
		}
		logger.debug("OUT");
	}
	
	private JSONArray serializeFieldsOptions(WorksheetFieldsOptions fieldsOptions) throws SerializationException, JSONException {
		JSONArray fieldsOptionsJSON = new JSONArray();
		List<FieldOptions> optionsList = fieldsOptions.getFieldsOptions();
		Iterator<FieldOptions> it = optionsList.iterator();
		while (it.hasNext()) {
			FieldOptions fieldOptions = it.next();
			JSONObject fieldJSON = (JSONObject) SerializationManager.serialize(fieldOptions.getField(), "application/json");
			List<FieldOption> options = fieldOptions.getOptions();
			Iterator<FieldOption> optionsIt = options.iterator();
			JSONObject optionsJSON = new JSONObject();
			while (optionsIt.hasNext()) {
				FieldOption option = optionsIt.next();
				String name = option.getName();
				Object value = option.getValue();
				optionsJSON.put(name, value);
			}
			fieldJSON.put(WorkSheetSerializationUtils.OPTIONS, optionsJSON);
			fieldsOptionsJSON.put(fieldJSON);
		}
		return fieldsOptionsJSON;
	}

}
