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
package it.eng.spagobi.engines.whatif;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.whatif.calculatedmember.CalculatedMember;
import it.eng.spagobi.engines.whatif.common.WhatIfConstants;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.serializer.SerializationManager;
import it.eng.spagobi.utilities.engines.EngineAnalysisState;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

public class WhatIfEngineAnalysisState extends EngineAnalysisState {

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(WhatIfEngineAnalysisState.class);

	public WhatIfEngineAnalysisState() {
		super();
	}

	/**
	 * Loads the subobject from a byte array
	 */
	@Override
	public void load(byte[] rowData) throws SpagoBIEngineException {
		String str = null;
		JSONObject rowDataJSON = null;
		JSONObject analysisStateJSON = null;
		String encodingFormatVersion;

		logger.debug("IN");

		try {
			str = new String(rowData);
			logger.debug("loading analysis state from row data [" + str + "] ...");

			rowDataJSON = new JSONObject(str);
			try {
				encodingFormatVersion = String.valueOf(rowDataJSON.getInt("version")); // Jackson management
			} catch (JSONException e) {
				encodingFormatVersion = "0";
			}

			if (encodingFormatVersion.equalsIgnoreCase(WhatIfConstants.CURRENT_WHAT_IF_ANALYSIS_STATE_VERSION)) {
				analysisStateJSON = rowDataJSON;
			} else {
				logger.error("Version management not already available");
			}

			logger.debug("Row data encoding version  [" + encodingFormatVersion + "]");

			setProperty(WhatIfConstants.WHAT_IF_ANALYSIS_STATE, analysisStateJSON);
			logger.debug("analysis state loaded succsfully from row data");
		} catch (JSONException e) {
			throw new SpagoBIEngineException("Impossible to load analysis state from raw data", e);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Store subobject into a byte array
	 */
	@Override
	public byte[] store() throws SpagoBIEngineException {
		JSONObject rowDataJSON = null;
		String rowData = null;

		rowDataJSON = (JSONObject) getProperty(WhatIfConstants.WHAT_IF_ANALYSIS_STATE);

		try {
			rowData = rowDataJSON.toString();
		} catch (Throwable e) {
			throw new SpagoBIEngineException("Impossible to store analysis state from catalogue object", e);
		}

		return rowData.getBytes();
	}

	/**
	 * Set the state of the subobject into the instance
	 *
	 * @param instance
	 */
	public void getAnalysisState(WhatIfEngineInstance instance) {

		try {

			JSONObject analysisStateJSON = (JSONObject) getProperty(WhatIfConstants.WHAT_IF_ANALYSIS_STATE);

			// set the property query into the analysis state
			String query = analysisStateJSON.getString(WhatIfConstants.MDX_QUERY);
			// set the model config into the analysis state
			String config = analysisStateJSON.getString(WhatIfConstants.MODEL_CONFIG);
			// set the calculated fields into the analysis state
			String calculatedFields = analysisStateJSON.optString(WhatIfConstants.CALCULATED_FIELDS);

			// deserialize the model config
			ModelConfig configDeserialized = (ModelConfig) (SerializationManager.deserialize("application/json", config, ModelConfig.class));

			instance.getPivotModel().setMdx(query);
			instance.updateModelConfig(configDeserialized);
			setCalculatedMember(calculatedFields, instance);

		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to deserialize catalogue", e);
		}

	}

	public Map<String, Object> getDriversValues() {
		Map<String, Object> toReturn = new HashMap<String, Object>();
		try {
			JSONObject analysisStateJSON = (JSONObject) getProperty(WhatIfConstants.WHAT_IF_ANALYSIS_STATE);
			JSONObject driversAsJSON = analysisStateJSON.optJSONObject(WhatIfConstants.DRIVERS);
			if (driversAsJSON != null) {
				Iterator<String> keys = driversAsJSON.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					Object value = driversAsJSON.get(key);
					if (value instanceof JSONArray) {
						Object[] array = fromJSONArray2ObjectsArray((JSONArray) value);
						toReturn.put(key, array);
					} else {
						toReturn.put(key, value);
					}
				}
			}
			LogMF.debug(logger, "Retrieved drivers from analisys state: [{0}]", toReturn);
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Impossible to get drivers from analisys state", e);
		}
		return toReturn;
	}

	protected Object[] fromJSONArray2ObjectsArray(JSONArray jsonArray) throws JSONException {
		Object[] array = new Object[] { jsonArray.length() };
		for (int i = 0; i < jsonArray.length(); i++) {
			array[i] = jsonArray.get(i);
		}
		return array;
	}

	/**
	 * Set the state of the subobject starting from the instance
	 *
	 * @param instance
	 */
	public void setAnalysisState(WhatIfEngineInstance instance) {

		JSONObject analysisStateJSON = new JSONObject();

		try {
			String query = instance.getPivotModel().getCurrentMdx();
			String config = (String) SerializationManager.serialize("application/json", instance.getModelConfig());

			if (instance.getPivotModel() instanceof SpagoBIPivotModel) {
				String cc = (String) SerializationManager.serialize("application/json", ((SpagoBIPivotModel) instance.getPivotModel()).getCalculatedFields());
				analysisStateJSON.put(WhatIfConstants.CALCULATED_FIELDS, cc);
			}

			analysisStateJSON.put(WhatIfConstants.MDX_QUERY, query);
			analysisStateJSON.put(WhatIfConstants.MODEL_CONFIG, config);

			Map drivers = getDrivers(instance);
			analysisStateJSON.put(WhatIfConstants.DRIVERS, drivers);

		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to serialize catalogue", e);
		}

		setProperty(WhatIfConstants.WHAT_IF_ANALYSIS_STATE, analysisStateJSON);
	}

	private Map getDrivers(WhatIfEngineInstance instance) {
		// cloning env since we need to remove unnecessary entries
		Map env = new HashMap(instance.getEnv());

		String[] keysToRemove = { EngineConstants.ENV_DOCUMENT_ID, EngineConstants.ENV_DATASOURCE, EngineConstants.ENV_USER_PROFILE,
				EngineConstants.ENV_CONTENT_SERVICE_PROXY, EngineConstants.ENV_AUDIT_SERVICE_PROXY, EngineConstants.ENV_DATASET_PROXY,
				EngineConstants.ENV_DATASOURCE_PROXY, EngineConstants.ENV_ARTIFACT_PROXY, EngineConstants.ENV_LOCALE, SpagoBIConstants.SBI_ARTIFACT_VERSION_ID,
				SpagoBIConstants.SBI_ARTIFACT_ID, SpagoBIConstants.SBI_ARTIFACT_STATUS, SpagoBIConstants.SBI_ARTIFACT_LOCKER, "template", "document",
				"spagobicontext", "BACK_END_SPAGOBI_CONTEXT", "userId", "auditId", "SBI_LANGUAGE", "SBI_SCIRPT", "SBI_ENVIRONMENT",
				"DEFAULT_DATASOURCE_FOR_WRITING_LABEL", "DOCUMENT_DESCRIPTION", "knowage_sys_country", "SBI_EXECUTION_ID", "knowage_sys_language",
				"DOCUMENT_LABEL", "DOCUMENT_NAME", "DOCUMENT_IS_PUBLIC", "SBI_COUNTRY", "SBI_EXECUTION_ROLE", "SPAGOBI_AUDIT_ID", "DOCUMENT_FUNCTIONALITIES",
				"EDIT_MODE", "DOCUMENT_OUTPUT_PARAMETERS", "IS_TECHNICAL_USER", "DOCUMENT_VERSION", "DOCUMENT_AUTHOR", "DOCUMENT_COMMUNITIES",
				"DOCUMENT_IS_VISIBLE", "documentMode", "user_id", "timereloadurl" };

		for (int i = 0; i < keysToRemove.length; i++) {
			env.remove(keysToRemove[i]);
		}

		return env;
	}

	private void setCalculatedMember(String cc, WhatIfEngineInstance instance) throws JSONException {
		List<CalculatedMember> toreturn = new ArrayList<CalculatedMember>();

		if (instance.getPivotModel() instanceof SpagoBIPivotModel) {
			SpagoBIPivotModel model = (SpagoBIPivotModel) instance.getPivotModel();

			if (cc != null && cc.length() > 0) {
				JSONArray ja = new JSONArray(cc);
				for (int i = 0; i < ja.length(); i++) {
					JSONObject aSerMember = ja.getJSONObject(i);
					CalculatedMember cm = new CalculatedMember(model.getCube(), aSerMember.getString("calculateFieldName"),
							aSerMember.getString("calculateFieldFormula"), aSerMember.getString("parentMemberUniqueName"),
							aSerMember.getInt("parentMemberAxisOrdinal"));
					toreturn.add(cm);
				}
			}

			model.setCalculatedFields(toreturn);
		}

	}

}