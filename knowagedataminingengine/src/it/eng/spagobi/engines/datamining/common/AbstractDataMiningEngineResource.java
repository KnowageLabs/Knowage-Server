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

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.serializer.SerializationException;
import it.eng.spagobi.engines.datamining.serializer.SerializationManager;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.rest.AbstractEngineRestService;
import it.eng.spagobi.utilities.engines.rest.ExecutionSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 *
 * @author Monica Franceschini
 *
 */
public class AbstractDataMiningEngineResource extends AbstractEngineRestService {

	private static final String ENGINE_NAME = "SpagoBIDataMiningEngine";

	private static final String OUTPUTFORMAT = "OUTPUTFORMAT";
	public static final String OUTPUTFORMAT_JSONHTML = "application/json";

	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	public static transient Logger logger = Logger.getLogger(AbstractDataMiningEngineResource.class);

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

		// TODO: check AbstractCockpitEngineResource: there for concurrent issue the engine instance is retrieved with IOManager...
		// Check if this is also the case
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

	@Override
	public Locale getLocale() {
		logger.debug("IN");
		Locale toReturn = null;
		try {
			String language = getServletRequest().getParameter(LANGUAGE);
			String country = getServletRequest().getParameter(COUNTRY);
			if (StringUtils.isNotEmpty(language) && StringUtils.isNotEmpty(country)) {
				toReturn = new Locale(language, country);
			} else {
				logger.warn("Language and country not specified in request. Considering default locale that is " + DEFAULT_LOCALE.toString());
				toReturn = DEFAULT_LOCALE;
			}
		} catch (Exception e) {
			logger.error("An error occurred while retrieving locale from request, using default locale that is " + DEFAULT_LOCALE.toString(), e);
			toReturn = DEFAULT_LOCALE;
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public Map getEnv() {
		Map env = new HashMap();

		env.put(EngineConstants.ENV_USER_PROFILE, getUserProfile());
		env.put(EngineConstants.ENV_CONTENT_SERVICE_PROXY, getContentServiceProxy());
		env.put(EngineConstants.ENV_AUDIT_SERVICE_PROXY, getAuditServiceProxy());
		env.put(EngineConstants.ENV_DATASET_PROXY, getDataSetServiceProxy());
		env.put(EngineConstants.ENV_DATASOURCE_PROXY, getDataSourceServiceProxy());
		env.put(EngineConstants.ENV_ARTIFACT_PROXY, getArtifactServiceProxy());
		env.put(EngineConstants.ENV_LOCALE, this.getLocale());
		env.put(SpagoBIConstants.SBI_ARTIFACT_VERSION_ID, getServletRequest().getParameter(SpagoBIConstants.SBI_ARTIFACT_VERSION_ID));
		env.put(SpagoBIConstants.SBI_ARTIFACT_ID, getServletRequest().getParameter(SpagoBIConstants.SBI_ARTIFACT_ID));
		env.put(SpagoBIConstants.SBI_ARTIFACT_STATUS, getServletRequest().getParameter(SpagoBIConstants.SBI_ARTIFACT_STATUS));
		env.put(SpagoBIConstants.SBI_ARTIFACT_LOCKER, getServletRequest().getParameter(SpagoBIConstants.SBI_ARTIFACT_LOCKER));

		copyRequestParametersIntoEnv(env, getServletRequest());

		return env;
	}

	private void copyRequestParametersIntoEnv(Map env, HttpServletRequest servletRequest) {
		Set parameterStopList = null;

		logger.debug("IN");

		parameterStopList = new HashSet();
		parameterStopList.add("template");
		parameterStopList.add("ACTION_NAME");
		parameterStopList.add("NEW_SESSION");
		parameterStopList.add("document");
		parameterStopList.add("spagobicontext");
		parameterStopList.add("BACK_END_SPAGOBI_CONTEXT");
		parameterStopList.add("userId");
		parameterStopList.add("auditId");

		HashMap requestParameters = ParametersDecoder.getDecodedRequestParameters(servletRequest);

		Iterator it = requestParameters.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = requestParameters.get(key);
			logger.debug("Parameter [" + key + "] has been read from request");
			if (value == null) {
				logger.debug("Parameter [" + key + "] is null");
				logger.debug("Parameter [" + key + "] copyed into environment parameters list: FALSE");
				continue;
			} else {
				logger.debug("Parameter [" + key + "] is of type  " + value.getClass().getName());
				logger.debug("Parameter [" + key + "] is equal to " + value.toString());
				if (parameterStopList.contains(key)) {
					logger.debug("Parameter [" + key + "] copyed into environment parameters list: FALSE");
					continue;
				}
				env.put(key, value);
				logger.debug("Parameter [" + key + "] copyed into environment parameters list: TRUE");
			}
		}
		logger.debug("OUT");
	}
}
