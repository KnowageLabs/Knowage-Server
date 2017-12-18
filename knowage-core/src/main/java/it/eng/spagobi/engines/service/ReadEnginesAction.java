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
package it.eng.spagobi.engines.service;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReadEnginesAction extends AbstractSpagoBIAction {

	private static final long serialVersionUID = 1L;

	// logger component
	public static Logger logger = Logger.getLogger(ReadEnginesAction.class);

	public static final String STRING_TYPE = "string";
	public static final String NUMBER_TYPE = "number";
	public static final String RAW_TYPE = "raw";
	public static final String GENERIC_TYPE = "generic";

	public static final String ENGINE_LIST = "ENGINE_LIST";
	public static final String ENGINE_DATASOURCES = "ENGINE_DATASOURCES";
	// public static final String ENGINE_TEST = "ENGINE_TEST";

	public static final String START = "start";
	public static final String LIMIT = "limit";
	public static final Integer START_DEFAULT = 0;
	public static final Integer LIMIT_DEFAULT = 20;

	public static final String MESSAGE_DET = "MESSAGE_DET";

	private IEngUserProfile profile;

	@Override
	public void doService() {
		logger.debug("IN");

		IEngineDAO engineDao;
		profile = getUserProfile();
		try {
			engineDao = DAOFactory.getEngineDAO();
			// engineDao.setUserProfile(profile);

			// engines must not be filtered by tenant, so unset tenant filter
			// TenantManager.unset();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while instatiating the dao", t);
		}

		String serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Service type " + serviceType);

		if (serviceType != null && serviceType.equalsIgnoreCase(ENGINE_LIST)) {
			listEnginesByTenant(engineDao);
		} else if (serviceType != null && serviceType.equalsIgnoreCase(ENGINE_DATASOURCES)) {
			getDataSources();
		}

		else {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to process action of type [" + serviceType + "]");
		}

		logger.debug("OUT");
	}

	private void listEngines(IEngineDAO engineDao) {
		List<Engine> engines;

		Integer start = getAttributeAsInteger(START);
		Integer limit = getAttributeAsInteger(LIMIT);
		if (start == null) {
			start = START_DEFAULT;
		}
		if (limit == null) {
			limit = LIMIT_DEFAULT;
		}

		Integer enginesNum = 0;
		try {
			// engines = engineDao.loadAllEngines();
			enginesNum = engineDao.countEngines();

			engines = engineDao.loadPagedEnginesList(start, limit);

		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to load engines from database", t);
		}

		JSONObject responseJSON;
		try {
			JSONArray enginesJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(engines, getLocale());
			responseJSON = createJSONResponse(enginesJSON, enginesNum);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize engines", t);
		}

		try {
			writeBackToClient(new JSONSuccess(responseJSON));
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back response to client", t);
		}
	}

	private void listEnginesByTenant(IEngineDAO engineDao) {
		List<Engine> engines;

		Integer start = getAttributeAsInteger(START);
		Integer limit = getAttributeAsInteger(LIMIT);
		if (start == null || start < 0) {
			start = START_DEFAULT;
		}
		if (limit == null || limit < 0) {
			limit = LIMIT_DEFAULT;
		}

		Integer enginesNum = 0;
		try {
			List<Engine> nonPagedEngines = engineDao.loadAllEnginesByTenant();
			enginesNum = nonPagedEngines.size();
			Integer end;

			if (start > enginesNum) {
				engines = new ArrayList<Engine>(0);
			} else {
				if (start + limit > enginesNum) {
					end = enginesNum;
				} else {
					end = limit;
				}
				engines = nonPagedEngines.subList(start, end);
			}

		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to load engines from database", t);
		}

		JSONObject responseJSON;
		try {
			JSONArray enginesJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(engines, getLocale());
			responseJSON = createJSONResponse(enginesJSON, enginesNum);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize engines", t);
		}

		try {
			writeBackToClient(new JSONSuccess(responseJSON));
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back response to client", t);
		}
	}

	private JSONObject createJSONResponse(JSONArray rows, Integer totalResNumber) throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("total", totalResNumber);
		results.put("title", "BIEngines");
		results.put("rows", rows);
		return results;
	}

	private void getDataSources() {
		IDataSourceDAO dataSourceDao = null;
		profile = getUserProfile();
		List<DataSource> dataSources;

		try {
			dataSourceDao = DAOFactory.getDataSourceDAO();
			dataSourceDao.setUserProfile(profile);
			dataSources = dataSourceDao.loadAllDataSources();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while instatiating the dao", t);

		}

		JSONObject responseJSON;
		try {
			JSONArray dataSourcesJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(dataSources, getLocale());
			responseJSON = createJSONResponse(dataSourcesJSON, dataSources.size());
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize engines", t);
		}

		try {
			writeBackToClient(new JSONSuccess(responseJSON));
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back response to client", t);
		}

	}

}
