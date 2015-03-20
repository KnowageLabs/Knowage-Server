/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.analysisstateloaders.worksheet;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class Version0WorksheetStateLoader extends AbstractWorksheetStateLoader {

	public final static String FROM_VERSION = "0";
    public final static String TO_VERSION = "1";
    
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(Version0WorksheetStateLoader.class);
	
    public Version0WorksheetStateLoader() {
    	super();
    }
    
    public Version0WorksheetStateLoader(IWorksheetStateLoader loader) {
    	super(loader);
    }
    
	@Override
	public JSONObject convert(JSONObject data) {
		logger.debug("IN");
		
		try {
			logger.debug( "Converting from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] ..." );
			
			convertSheets(data);
			putGlobalFilters(data);

			logger.debug( "Conversion from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] terminated succesfully" );
			
		} catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + data + "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return data;
	}

	private void putGlobalFilters(JSONObject data) throws Exception {
		JSONArray filters = new JSONArray();
		data.put("globalFilters", filters);
	}

	private void convertSheets(JSONObject data) throws Exception {
		JSONArray sheets = data.optJSONArray("sheets");
		if (sheets != null && sheets.length() > 0) {
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject aSheet = sheets.getJSONObject(i);
				aSheet.put("filtersOnDomainValues", new JSONArray());
				convertContent(aSheet);
				convertFilters(aSheet);
			}
		}
	}

	private void convertContent(JSONObject aSheet) throws Exception {
		JSONObject content = aSheet.getJSONObject("content");
		String designer = content.getString("designer");
		if (designer.equals("Pivot Table")) {
			JSONObject crosstabDefinition = content.getJSONObject("crosstabDefinition");
			JSONArray rows = crosstabDefinition.getJSONArray("rows");
			convertAttributes(rows);
			JSONArray columns = crosstabDefinition.getJSONArray("columns");
			convertAttributes(columns);
		}
		if (designer.equals("Bar Chart") || designer.equals("Line Chart") || designer.equals("Pie Chart")) {
			JSONObject category = content.getJSONObject("category");
			convertAttribute(category);
		}
		if (designer.equals("Table")) {
			JSONArray fields = content.getJSONArray("visibleselectfields");
			convertAttributes(fields);
		}
	}
	
	private void convertFilters(JSONObject aSheet) throws Exception {
		JSONObject filtersObj = aSheet.getJSONObject("filters");
		JSONArray filters = filtersObj.getJSONArray("filters");
		convertAttributes(filters);
	}

	private void convertAttributes(JSONArray attributes) throws Exception {
		if (attributes != null && attributes.length() > 0) {
			for (int i = 0; i < attributes.length(); i++) {
				JSONObject anAttribute = attributes.getJSONObject(i);
				convertAttribute(anAttribute);
			}
		}
	}
	
	private void convertAttribute(JSONObject attribute) throws Exception {
		JSONArray values = new JSONArray();
		attribute.put("values", values.toString());
	}

}
