/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.functionscatalog.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class CatalogFunctionRuntimeConfigDTO {

	private static transient Logger logger = Logger.getLogger(CatalogFunctionRuntimeConfigDTO.class);

	private Map<String, InputVariable> inputVariables;
	private Map<String, OutputColumnRuntime> outputColumns;
	private Map<String, String> inputColumns;
	private String environment;

	private CatalogFunctionRuntimeConfigDTO() {

	}

	public CatalogFunctionRuntimeConfigDTO(Map<String, InputVariable> inputVariables, Map<String, OutputColumnRuntime> outputColumns,
			Map<String, String> inputColumns) {
		super();
		this.inputVariables = inputVariables;
		this.outputColumns = outputColumns;
		this.inputColumns = inputColumns;
	}

	public Map<String, InputVariable> getInputVariables() {
		return inputVariables;
	}

	public void setInputVariables(Map<String, InputVariable> inputVariables) {
		this.inputVariables = inputVariables;
	}

	public Map<String, OutputColumnRuntime> getOutputColumns() {
		return outputColumns;
	}

	public void setOutputColumns(Map<String, OutputColumnRuntime> outputColumns) {
		this.outputColumns = outputColumns;
	}

	public Map<String, String> getInputColumns() {
		return inputColumns;
	}

	public void setInputColumns(Map<String, String> inputColumns) {
		this.inputColumns = inputColumns;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public static CatalogFunctionRuntimeConfigDTO fromJSON(JSONObject jsonConfig) {
		CatalogFunctionRuntimeConfigDTO toReturn = new CatalogFunctionRuntimeConfigDTO();
		toReturn.setInputColumns(jsonConfig);
		toReturn.setOutputColumns(jsonConfig);
		toReturn.setInputVariables(jsonConfig);
		toReturn.setEnvironment(jsonConfig);
		return toReturn;
	}

	private void setInputColumns(JSONObject jsonConfig) {
		inputColumns = new HashMap<String, String>();
		JSONArray inCols = jsonConfig.optJSONArray("inputColumns");
		for (int i = 0; i < inCols.length(); i++) {
			try {
				String columnName = inCols.getJSONObject(i).getString("name");
				String dsColumn = inCols.getJSONObject(i).getString("dsColumn");
				inputColumns.put(columnName, dsColumn);
			} catch (Exception e) {
				logger.error("Error initializing input columns", e);
				inputColumns = new HashMap<String, String>();
			}
		}
	}

	private void setOutputColumns(JSONObject jsonConfig) {
		outputColumns = new HashMap<String, OutputColumnRuntime>();
		JSONArray outCols = jsonConfig.optJSONArray("outputColumns");
		for (int i = 0; i < outCols.length(); i++) {
			try {
				String columnName = outCols.getJSONObject(i).getString("name");
				String fieldType = outCols.getJSONObject(i).getString("fieldType");
				String type = outCols.getJSONObject(i).getString("type");
				String alias = outCols.getJSONObject(i).optString("alias");
				OutputColumnRuntime outputColumn = new OutputColumnRuntime(columnName, fieldType, type, alias);
				outputColumns.put(columnName, outputColumn);
			} catch (Exception e) {
				logger.error("Error initializing output columns", e);
				outputColumns = new HashMap<String, OutputColumnRuntime>();
			}
		}
	}

	private void setInputVariables(JSONObject jsonConfig) {
		inputVariables = new HashMap<String, InputVariable>();
		JSONArray inVars = jsonConfig.optJSONArray("inputVariables");
		for (int i = 0; i < inVars.length(); i++) {
			try {
				String varName = inVars.getJSONObject(i).getString("name");
				String type = inVars.getJSONObject(i).getString("type");
				String value = inVars.getJSONObject(i).getString("value");
				InputVariable inputVariable = new InputVariable(varName, type, value);
				inputVariables.put(varName, inputVariable);
			} catch (Exception e) {
				logger.error("Error initializing input variables", e);
				inputVariables = new HashMap<String, InputVariable>();
			}
		}
	}

	private void setEnvironment(JSONObject jsonConfig) {
		environment = jsonConfig.optString("environment");
	}

}
