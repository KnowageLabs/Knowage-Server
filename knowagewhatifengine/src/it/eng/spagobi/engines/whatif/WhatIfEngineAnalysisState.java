/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.whatif;

import it.eng.spagobi.engines.whatif.common.WhatIfConstants;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.serializer.SerializationManager;
import it.eng.spagobi.utilities.engines.EngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class WhatIfEngineAnalysisState extends EngineAnalysisState {

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(WhatIfEngineAnalysisState.class);

	public WhatIfEngineAnalysisState() {
		super();
	}

	/**
	 * Loads the subobject from a byte array
	 */
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

			// deserialize the model config
			ModelConfig configDeserialized = (ModelConfig) (SerializationManager.deserialize("application/json", config, ModelConfig.class));

			instance.getPivotModel().setMdx(query);
			instance.updateModelConfig(configDeserialized);

		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to deserialize catalogue", e);
		}

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

			analysisStateJSON.put(WhatIfConstants.MDX_QUERY, query);
			analysisStateJSON.put(WhatIfConstants.MODEL_CONFIG, config);
		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to serialize catalogue", e);
		}

		setProperty(WhatIfConstants.WHAT_IF_ANALYSIS_STATE, analysisStateJSON);
	}

}