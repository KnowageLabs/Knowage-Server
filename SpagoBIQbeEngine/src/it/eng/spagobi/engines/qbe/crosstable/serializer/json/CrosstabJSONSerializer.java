/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.crosstable.serializer.json;

import it.eng.qbe.serializer.ISerializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.worksheet.bo.Measure;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition.Column;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition.Row;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class CrosstabJSONSerializer implements ISerializer {

	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(CrosstabJSONSerializer.class);
    
    
	public Object serialize(Object o) throws SerializationException {
		JSONObject toReturn = null;
		CrosstabDefinition crosstabDefinition;
				
		Assert.assertNotNull(o, "Input parameter cannot be null");
		Assert.assertTrue(o instanceof CrosstabDefinition, "Unable to serialize objects of type [" + o.getClass().getName() + "]");
		
		try {
			toReturn = new JSONObject();
			
			crosstabDefinition = (CrosstabDefinition)o;
			
			// config (measures on rows/columns, totals/subototals on rows/columns)
			JSONObject config = crosstabDefinition.getConfig();
			config.put("maxcellnumber", crosstabDefinition.getCellLimit());

			toReturn.put(CrosstabSerializationConstants.CONFIG, config);
			
			// calculated fields definition
			JSONArray calculatedFields = crosstabDefinition.getCalculatedFields();
			toReturn.put(CrosstabSerializationConstants.CALCULATED_FIELDS, calculatedFields);
			
			// rows 
			JSONArray rows = this.serializeRows(crosstabDefinition);
			toReturn.put(CrosstabSerializationConstants.ROWS, rows);
			
			// columns
			JSONArray columns = this.serializeColumns(crosstabDefinition);
			toReturn.put(CrosstabSerializationConstants.COLUMNS, columns);
			
			// measures
			JSONArray measures = this.serializeMeasures(crosstabDefinition);
			toReturn.put(CrosstabSerializationConstants.MEASURES, measures);
			
			//additional data
			JSONObject additionalData = crosstabDefinition.getAdditionalData();
			toReturn.put(CrosstabSerializationConstants.ADDITIONAL_DATA, additionalData);
			
			
		} catch (SerializationException se) {
			throw se;
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return toReturn;
	}
	
	private JSONArray serializeRows(CrosstabDefinition crosstabDefinition) throws SerializationException {
		List<Row> rows = crosstabDefinition.getRows();
		JSONArray toReturn = new JSONArray();
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			JSONObject obj = (JSONObject) SerializationManager.serialize(row, "application/json");
			toReturn.put(obj);
		}
		return toReturn;
	}
	
	private JSONArray serializeColumns(CrosstabDefinition crosstabDefinition) throws SerializationException {
		List<Column> columns = crosstabDefinition.getColumns();
		JSONArray toReturn = new JSONArray();
		for (int i = 0; i < columns.size(); i++) {
			Column column = columns.get(i);
			JSONObject obj = (JSONObject) SerializationManager.serialize(column, "application/json");
			toReturn.put(obj);
		}
		return toReturn;
	}
	
	private JSONArray serializeMeasures(CrosstabDefinition crosstabDefinition) throws SerializationException {
		List<Measure> measures = crosstabDefinition.getMeasures();
		JSONArray toReturn = new JSONArray();
		for (int i = 0; i < measures.size(); i++) {
			Measure measure = measures.get(i);
			JSONObject obj = (JSONObject) SerializationManager.serialize(measure, "application/json");
			toReturn.put(obj);
		}
		return toReturn;
	}

}
