/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engines.qbe.analysisstateloaders.formbuilder;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public class Version0FormStateLoader extends AbstractFormStateLoader{

	/** Logger component. */
	private static final Logger LOGGER = Logger.getLogger(Version0FormStateLoader.class);

	public static final String FROM_VERSION = "0";
	public static final String TO_VERSION = "1";

	public Version0FormStateLoader() {
		super();
	}

	public Version0FormStateLoader(IFormStateLoader loader) {
		super(loader);
	}

	@Override
	public JSONObject convert(JSONObject data) {
		LOGGER.debug("IN");

		try {
			LOGGER.debug( "Converting from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] ..." );

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

			LOGGER.debug( "Conversion from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] terminated succesfully" );

		}catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + data + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}

		return data;
	}

	/**
	 * In previous version, when using IN, NOT IN, BETWEEN or NOT BETWEEN operators, the right operand values contained all values
	 * joined by ","; now those values are converted into an array
	 * @param filterJSON
	 */
	private void convertFilter(JSONObject filterJSON) {
		LOGGER.debug( "IN" );
		try {
			Assert.assertNotNull(filterJSON, "Filter to be converted cannot be null");
			JSONArray rightValuesJSON = new JSONArray();
			LOGGER.debug("Converting filter : " + filterJSON.toString());
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
			LOGGER.debug("New rigth operand : " + rightValuesJSON.toString());
			filterJSON.put("rightOperandValue", rightValuesJSON);
			LOGGER.debug("Filter converted properly");
		} catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible convert filter " + (filterJSON == null ? "NULL" : filterJSON.toString()), t);
		} finally {
			LOGGER.debug( "OUT" );
		}
	}

	private static JSONArray splitValuesUsingComma(String value, String operator) {
		LOGGER.debug("IN: value = " + value + "; operator = " + operator);
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
		LOGGER.debug("OUT: new value = " + toReturn.toString());
		return toReturn;
	}

}
