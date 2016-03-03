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

package it.eng.spagobi.engines.datamining.common;

import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.serializer.SerializationException;
import it.eng.spagobi.engines.datamining.serializer.SerializationManager;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.rest.AbstractEngineRestService;
import it.eng.spagobi.utilities.engines.rest.ExecutionSession;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 
 * @author Monica Franceschini
 * 
 */
public class AbstractDataMiningEngineService extends AbstractEngineRestService {

	private static final String ENGINE_NAME = "SpagoBIDataMiningEngine";

	private static final String OUTPUTFORMAT = "OUTPUTFORMAT";
	public static final String OUTPUTFORMAT_JSONHTML = "application/json";

	private String successString;

	public static transient Logger logger = Logger.getLogger(AbstractDataMiningEngineService.class);

	@Context
	protected HttpServletRequest servletRequest;

	@Override
	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	/**
	 * Gets the data mining engine instance.
	 * 
	 * @return the console engine instance
	 */
	public DataMiningEngineInstance getDataMiningEngineInstance() {
		ExecutionSession es = getExecutionSession();
		return (DataMiningEngineInstance) es.getAttributeFromSession(EngineConstants.ENGINE_INSTANCE);

	}

	public String getOutputFormat() {
		String outputFormat = servletRequest.getParameter(OUTPUTFORMAT);

		if (outputFormat == null || outputFormat.equals("")) {
			logger.debug("the output format is null.. use the default one" + OUTPUTFORMAT_JSONHTML);
			outputFormat = OUTPUTFORMAT_JSONHTML;
		}

		return outputFormat;
	}

	public String serializeDatasetsList(List<DataMiningDataset> datasets) {
		logger.debug("IN");

		String serializedList = null;

		try {
			serializedList = serialize(datasets);
		} catch (SerializationException e) {
			logger.error("Error serializing the dataset list", e);
			throw new SpagoBIEngineRuntimeException("Error serializing the dataset list", e);
		}

		logger.debug("OUT: dataset list correctly serialized");
		return serializedList;

	}

	public String serializeList(List listToserialize) {
		logger.debug("IN");

		String serializedList = null;

		try {
			serializedList = serialize(listToserialize);
		} catch (SerializationException e) {
			logger.error("Error serializing the list", e);
			throw new SpagoBIEngineRuntimeException("Error serializing the list", e);
		}

		logger.debug("OUT: list correctly serialized");
		return serializedList;

	}

	public String serialize(Object obj) throws SerializationException {
		String outputFormat = getOutputFormat();
		return (String) SerializationManager.serialize(outputFormat, obj);
	}

	public Object deserialize(String obj, Class clazz) throws SerializationException {
		String outputFormat = getOutputFormat();
		return SerializationManager.deserialize(outputFormat, obj, clazz);
	}

	public Object deserialize(String obj, TypeReference object) throws SerializationException {
		String outputFormat = getOutputFormat();
		return SerializationManager.deserialize(outputFormat, obj, object);
	}

	@Override
	public String getEngineName() {
		return ENGINE_NAME;
	}

	/**
	 * Builds a simple success json {result: ok}
	 * 
	 * @return
	 */
	public String getJsonSuccess() {
		if (successString == null) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", "ok");
			} catch (JSONException e) {
				logger.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			successString = obj.toString();
		}
		return successString;
	}

	/**
	 * Builds a simple success json {success: true}
	 * 
	 * @return
	 */
	public String getJsonSuccessTrue() {
		if (successString == null) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("success", true);
			} catch (JSONException e) {
				logger.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			successString = obj.toString();
		}
		return successString;
	}

	public String getJsonOk() {
		if (successString == null) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", "ok");
			} catch (JSONException e) {
				logger.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			successString = obj.toString();
		}
		return successString;
	}

	public String getJsonKo() {
		if (successString == null) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", "ko");
			} catch (JSONException e) {
				logger.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			successString = obj.toString();
		}
		return successString;
	}
}
