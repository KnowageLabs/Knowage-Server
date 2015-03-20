/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.commons.deserializer;


import it.eng.spagobi.tools.dataset.crosstab.Attribute;
import it.eng.spagobi.tools.dataset.crosstab.CrosstabDefinition;
import it.eng.spagobi.tools.dataset.crosstab.CrosstabDefinition.Column;
import it.eng.spagobi.tools.dataset.crosstab.CrosstabDefinition.Row;
import it.eng.spagobi.tools.dataset.crosstab.CrosstabSerializationConstants;
import it.eng.spagobi.tools.dataset.crosstab.Measure;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class CrosstabJSONDeserializer implements Deserializer {
	
	public static final String ID = "id";
	public static final String ALIAS = "alias";
	public static final String ICON_CLS = "iconCls";
	public static final String NATURE = "nature";
	public static final String VALUES = "values";
	public static final String FUNCTION = "funct";


    public static transient Logger logger = Logger.getLogger(CrosstabJSONDeserializer.class);
    
	public CrosstabDefinition deserialize(Object o, Class clazz) throws DeserializationException  {
		CrosstabDefinition crosstabDefinition = null;
		JSONObject crosstabDefinitionJSON = null;
		
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(o, "Input parameter [" + o + "] cannot be null");
			Assert.assertNotNull(o, "Input parameter [" + clazz + "] cannot be null");
			
			if(o instanceof String) {
				logger.debug("Deserializing string [" + (String)o + "]");
				try {
					crosstabDefinitionJSON = new JSONObject( (String)o );
				} catch(Throwable t) {
					logger.debug("Object to be deserialized must be string encoding a JSON object");
					throw new DeserializationException("An error occurred while deserializing query: " + (String)o, t);
				}
			} else if(o instanceof JSONObject) {
				crosstabDefinitionJSON = (JSONObject)o;
			} else {
				Assert.assertUnreachable("Object to be deserialized must be of type string or of type JSONObject, not of type [" + o.getClass().getName() + "]");
			}
			
			crosstabDefinition  = new CrosstabDefinition();
			
			try {
				deserializeRows(crosstabDefinitionJSON, crosstabDefinition);
				deserializeColumns(crosstabDefinitionJSON, crosstabDefinition);
				deserializeMeasures(crosstabDefinitionJSON, crosstabDefinition);
				
				// config (measures on rows/columns, totals/subototals on rows/columns) remains a JSONObject 
				JSONObject config = crosstabDefinitionJSON.optJSONObject(CrosstabSerializationConstants.CONFIG);
				crosstabDefinition.setConfig(config);
				
				String maxCellsString= config.optString("maxcellnumber");
				
				if (maxCellsString!=null && !maxCellsString.equals("")){
					try {
						crosstabDefinition.setCellLimit(new Integer(maxCellsString));
					} catch (Exception e) {
						logger.error("The cell limit of the crosstab definition is not a number : "+maxCellsString+". We consier it 0");
					}
					
				}
				
				JSONArray calculatedFields = crosstabDefinitionJSON.optJSONArray(CrosstabSerializationConstants.CALCULATED_FIELDS);
				crosstabDefinition.setCalculatedFields(calculatedFields);
				
				JSONObject additionalData = crosstabDefinitionJSON.optJSONObject(CrosstabSerializationConstants.ADDITIONAL_DATA);
				crosstabDefinition.setAdditionalData(additionalData);
				
			} catch (Exception e) {
				throw new DeserializationException("An error occurred while deserializing query: " + crosstabDefinitionJSON.toString(), e);
			}

		} finally {
			logger.debug("OUT");
		}
		
		return crosstabDefinition;
	}
	
	private void deserializeRows(JSONObject crosstabDefinitionJSON, CrosstabDefinition crosstabDefinition) throws Exception {
		List<Row> rows = new ArrayList<Row>();
		JSONArray rowsJSON = crosstabDefinitionJSON.optJSONArray(CrosstabSerializationConstants.ROWS);
		//Assert.assertTrue(rows != null && rows.length() > 0, "No rows specified!");
		if (rowsJSON != null) {
			for (int i = 0; i < rowsJSON.length(); i++) {
				JSONObject obj = (JSONObject) rowsJSON.get(i);
				//Attribute attribute = (Attribute) SerializationManager.deserialize(obj, "application/json", Attribute.class);
				Attribute attribute = deserializeAttribute(obj);
				rows.add(crosstabDefinition.new Row(attribute));
			}
		}
		crosstabDefinition.setRows(rows);
	}
	
	private void deserializeMeasures(JSONObject crosstabDefinitionJSON, CrosstabDefinition crosstabDefinition) throws Exception {
		List<Measure> measures = new ArrayList<Measure>();
		JSONArray measuresJSON = crosstabDefinitionJSON.optJSONArray(CrosstabSerializationConstants.MEASURES);
		//Assert.assertTrue(rows != null && rows.length() > 0, "No measures specified!");
		if (measuresJSON != null) {
			for (int i = 0; i < measuresJSON.length(); i++) {
				JSONObject obj = (JSONObject) measuresJSON.get(i);
				//Measure measure = (Measure) SerializationManager.deserialize(obj, "application/json", Measure.class);
				Measure measure = deserializeMeasure(obj);
				measures.add(measure);
			}
		}
		crosstabDefinition.setMeasures(measures);
	}
	
	private void deserializeColumns(JSONObject crosstabDefinitionJSON, CrosstabDefinition crosstabDefinition) throws Exception {
		List<Column> columns = new ArrayList<Column>();
		JSONArray columnsJSON = crosstabDefinitionJSON.optJSONArray(CrosstabSerializationConstants.COLUMNS);
		//Assert.assertTrue(rows != null && rows.length() > 0, "No columns specified!");
		if (columnsJSON != null) {
			for (int i = 0; i < columnsJSON.length(); i++) {
				JSONObject obj = (JSONObject) columnsJSON.get(i);
				//Attribute attribute = (Attribute) SerializationManager.deserialize(obj, "application/json", Attribute.class);
				Attribute attribute = deserializeAttribute(obj);
				columns.add(crosstabDefinition.new Column(attribute));
			}
		}
		crosstabDefinition.setColumns(columns);
	}
	
	private Attribute deserializeAttribute(JSONObject obj) throws JSONException {
		return new Attribute(obj.getString(ID),
				obj.getString(ALIAS),
				obj.getString(ICON_CLS),
				obj.getString(NATURE),
				obj.getString(VALUES));
	}
	
	private Measure deserializeMeasure(JSONObject obj) throws JSONException {
		return new Measure(obj.getString(ID),
				obj.getString(ALIAS),
				obj.getString(ICON_CLS),
				obj.getString(NATURE),
				obj.getString(FUNCTION));
	}
		
}