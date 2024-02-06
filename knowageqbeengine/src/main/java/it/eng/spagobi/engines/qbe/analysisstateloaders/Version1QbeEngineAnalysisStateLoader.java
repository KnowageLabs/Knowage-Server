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
package it.eng.spagobi.engines.qbe.analysisstateloaders;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class Version1QbeEngineAnalysisStateLoader extends AbstractQbeEngineAnalysisStateLoader {

	/** Logger component. */
	private static final Logger LOGGER = Logger.getLogger(Version1QbeEngineAnalysisStateLoader.class);

	public static final String FROM_VERSION = "1";
	public static final String TO_VERSION = "2";

	public Version1QbeEngineAnalysisStateLoader() {
	}

	public Version1QbeEngineAnalysisStateLoader(IQbeEngineAnalysisStateLoader loader) {
		super(loader);
	}

	@Override
	public JSONObject convert(JSONObject data) {
		JSONObject resultJSON;
		JSONObject catalogueJSON;
		JSONArray queriesJSON;

		LOGGER.debug("IN");
		try {
			Assert.assertNotNull(data, "Data to convert cannot be null");

			LOGGER.debug("Converting from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION
					+ "] ...");
			LOGGER.debug("Data to convert [" + data.toString() + "]");

			catalogueJSON = data.getJSONObject("catalogue");
			// fix query encoding ...
			queriesJSON = catalogueJSON.getJSONArray("queries");
			LOGGER.debug("In the stored catalogue there are  [" + queriesJSON.length() + "] to convert");
			for (int i = 0; i < queriesJSON.length(); i++) {
				convertQuery(queriesJSON.getJSONObject(i));
			}

			resultJSON = new JSONObject();
			resultJSON.put("catalogue", catalogueJSON);

			LOGGER.debug("Converted data [" + resultJSON.toString() + "]");
			LOGGER.debug("Conversion from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION
					+ "] terminated succesfully");
		} catch (Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + data + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}

		return resultJSON;
	}

	private void convertQuery(JSONObject queryJSON) {
		JSONArray fieldsJSON;
		JSONArray filtersJSON;
		JSONArray subqueriesJSON;
		JSONObject fieldJSON;
		JSONObject filterJSON;
		String fieldUniqueName;
		String operandType;
		String queryId = null;

		LOGGER.debug("IN");

		try {
			Assert.assertNotNull(queryJSON, "Query to be converted cannot be null");
			queryId = queryJSON.getString("id");
			LOGGER.debug("Converting query [" + queryId + "] ...");
			LOGGER.debug("Query content to be converted [" + queryJSON.toString() + "]");

			// convert fields
			fieldsJSON = queryJSON.getJSONArray("fields");
			LOGGER.debug("Query [" + queryId + "] have [" + fieldsJSON.length() + "] fields to convert");
			for (int j = 0; j < fieldsJSON.length(); j++) {
				LOGGER.debug("Converting field [" + (j + 1) + "] ...");
				fieldJSON = fieldsJSON.getJSONObject(j);
				fieldUniqueName = fieldJSON.getString("id");
				fieldUniqueName = convertFieldUniqueName(fieldUniqueName);
				fieldJSON.put("id", fieldUniqueName);
			}

			// convert filters
			filtersJSON = queryJSON.getJSONArray("filters");
			LOGGER.debug("Query [" + queryId + "] have [" + filtersJSON.length() + "] filters to convert");
			for (int j = 0; j < filtersJSON.length(); j++) {
				LOGGER.debug("Converting filter [" + (j + 1) + "] ...");
				filterJSON = filtersJSON.getJSONObject(j);
				fieldUniqueName = filterJSON.getString("id");
				fieldUniqueName = convertFieldUniqueName(fieldUniqueName);
				filterJSON.put("id", fieldUniqueName);

				operandType = filterJSON.getString("otype");
				if (operandType.equals("Field Conten")) {
					LOGGER.debug("Converting filter [" + (j + 1) + "] operand ...");
					fieldUniqueName = filterJSON.getString("operand");
					fieldUniqueName = convertFieldUniqueName(fieldUniqueName);
					filterJSON.put("operand", fieldUniqueName);
				} else if (operandType.equals("Parent Field Content")) {
					LOGGER.debug("Converting filter [" + (j + 1) + "] operand ...");
					fieldUniqueName = filterJSON.getString("operand");
					String[] chunks = fieldUniqueName.split(" ");
					fieldUniqueName = chunks[1];
					fieldUniqueName = convertFieldUniqueName(fieldUniqueName);
					filterJSON.put("operand", chunks[0] + " " + fieldUniqueName);
				}

			}

			// convert subqueries
			subqueriesJSON = queryJSON.getJSONArray("subqueries");
			LOGGER.debug("Query [" + queryId + "] have [" + subqueriesJSON.length() + "] subqueries to convert");
			for (int j = 0; j < subqueriesJSON.length(); j++) {
				LOGGER.debug("Converting subquery [" + (j + 1) + "] of query [" + queryId + "] ...");
				convertQuery(subqueriesJSON.getJSONObject(j));
			}

			LOGGER.debug("Query [" + queryId + "] converted succesfully");
		} catch (Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible convert query [" + queryId + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	private String convertFieldUniqueName(String fieldUniqueName) {
		String result;
		String[] chunks;

		LOGGER.debug("Field unique name to convert [" + fieldUniqueName + "]");

		chunks = fieldUniqueName.split(":");

		result = "";
		for (int i = chunks.length - 1; i > 0; i--) {
			if (!StringUtils.isEmpty(chunks[i])) {
				/*
				 * if(chunks[i].indexOf("(") > 0 ) { chunks[i] = chunks[i].substring(0, chunks[i].indexOf("(")); }
				 */
				chunks[i] = chunks[i].substring(0, 1).toLowerCase() + chunks[i].substring(1);
			}
			result = StringUtils.isEmpty(result) ? chunks[i] : chunks[i] + ":" + result;
		}

		result = chunks[0] + ":" + result;

		LOGGER.debug("Converted field unique name [" + result + "]");

		return result;
	}

}
