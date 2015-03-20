/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.analysisstateloaders.formbuilder;

import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public class Version0FormStateLoader extends AbstractFormStateLoader{

	public final static String FROM_VERSION = "0";
    public final static String TO_VERSION = "1";
    
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(Version0FormStateLoader.class);
	
    public Version0FormStateLoader() {
    	super();
    }
    
    public Version0FormStateLoader(IFormStateLoader loader) {
    	super(loader);
    }
    
	public JSONObject convert(JSONObject data) {
		logger.debug("IN");
		
		try {
			logger.debug( "Converting from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] ..." );
			
			JSONArray staticClosedFilters = data.optJSONArray("staticClosedFilters");
			if (staticClosedFilters != null && staticClosedFilters.length() > 0) {
				for (int i = 0; i < staticClosedFilters.length(); i++) {
					JSONObject staticClosedFilter = staticClosedFilters.getJSONObject(i);
					JSONArray options = staticClosedFilter.optJSONArray("options");
					if (options != null && options.length() > 0) {
						for (int j = 0; j < options.length(); j++) {
							JSONObject option = options.getJSONObject(j);
							JSONArray filters = option.optJSONArray("filters");
							if (filters != null && filters.length() > 0) {
								for (int k = 0; k < filters.length(); k++) {
									JSONObject filter = filters.getJSONObject(k);
									convertFilter(filter);
								}
							}
						}
					}
				}
			}

			logger.debug( "Conversion from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] terminated succesfully" );
			
		}catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + data + "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return data;
	}
	
	/**
	 * In previous version, when using IN, NOT IN, BETWEEN or NOT BETWEEN operators, the right operand values contained all values 
	 * joined by ","; now those values are converted into an array
	 * @param filterJSON
	 */
	private void convertFilter(JSONObject filterJSON) {
		logger.debug( "IN" );
		try {
			Assert.assertNotNull(filterJSON, "Filter to be converted cannot be null");
			JSONArray rightValuesJSON = new JSONArray();
			logger.debug("Converting filter : " + filterJSON.toString());
			String operator = filterJSON.getString("operator");
			String rightOperandType = filterJSON.optString("rightOperandType");
			String rightOperandValue = filterJSON.optString("rightOperandValue");
			if (rightOperandValue == null) {
				rightOperandValue = "";
			}
			if (rightOperandType == null || rightOperandType.trim().equals("")) {
				rightOperandType = "Static Value";
			}
			if (rightOperandType.equals("Static Value") && 
					(operator.equalsIgnoreCase("BETWEEN") || operator.equalsIgnoreCase("NOT BETWEEN") ||
							operator.equalsIgnoreCase("IN") || operator.equalsIgnoreCase("NOT IN")
							)) {
				rightValuesJSON = splitValuesUsingComma(rightOperandValue, operator);
			} else {
				if (rightOperandValue != null) rightValuesJSON.put(rightOperandValue);
			}
			logger.debug("New rigth operand : " + rightValuesJSON.toString());
			filterJSON.put("rightOperandValue", rightValuesJSON);
			logger.debug("Filter converted properly");
		} catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible convert filter " + (filterJSON == null ? "NULL" : filterJSON.toString()), t);
		} finally {
			logger.debug( "OUT" );
		}
	}
	
	private static JSONArray splitValuesUsingComma(String value, String operator) {
		logger.debug("IN: value = " + value + "; operator = " + operator);
		JSONArray toReturn = new JSONArray();
		if (value != null) {
			String[] values = null;
			// case BETWEEN or NOT BETWEEN using dates
			if ("BETWEEN".equalsIgnoreCase( operator ) || "NOT BETWEEN".equalsIgnoreCase( operator )) {
				
				if (value.contains("STR_TO_DATE") || value.contains("TO_TIMESTAMP")) {
					int firstComma = value.indexOf(',');
					int correctComma = value.indexOf(',', firstComma+1);
					String minValue = value.substring(0, correctComma-1);
					String maxValue = value.substring(correctComma+1, value.length());
					toReturn.put(minValue);
					toReturn.put(maxValue);
				} else {
					values = value.split(",");
					for (int i = 0; i < values.length ; i++) {
						toReturn.put(values[i]);
					}
				}
				
			} else if ((values = value.split(",")).length > 0) {
				for (int i = 0; i < values.length ; i++) {
					toReturn.put(values[i]);
				}
			}
		}
		logger.debug("OUT: new value = " + toReturn.toString());
		return toReturn;
	}

}
