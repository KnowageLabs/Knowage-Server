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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.deserializer.DeserializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

public class ManageEnginesAction extends AbstractSpagoBIAction {

	private static final long serialVersionUID = 1L;

	// logger component
	public static Logger logger = Logger.getLogger(ManageEnginesAction.class);

	public static final String STRING_TYPE = "string";
	public static final String NUMBER_TYPE = "number";
	public static final String RAW_TYPE = "raw";
	public static final String GENERIC_TYPE = "generic";
	public static final String ENGINE_INSERT = "ENGINE_INSERT";
	public static final String ENGINE_DELETE = "ENGINE_DELETE";
	public static final String ENGINE_TEST = "ENGINE_TEST";

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
			TenantManager.unset();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while instatiating the dao", t);
		}

		String serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Service type " + serviceType);

		if (serviceType != null && serviceType.equalsIgnoreCase(ENGINE_INSERT)) {
			insertEngine(engineDao);
		} else if (serviceType != null && serviceType.equalsIgnoreCase(ENGINE_DELETE)) {
			deleteEngine(engineDao);
		} else if (serviceType != null && serviceType.equalsIgnoreCase(ENGINE_TEST)) {
			testEngine();
		} else {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to process action of type [" + serviceType + "]");
		}

		logger.debug("OUT");
	}

	private JSONObject createJSONResponse(JSONArray rows, Integer totalResNumber) throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("total", totalResNumber);
		results.put("title", "BIEngines");
		results.put("rows", rows);
		return results;
	}

	private void insertEngine(IEngineDAO engineDao) {
		Engine engine;
		try {
			JSONObject encodedValues = this.getAttributeAsJSONObject("engineValues");

			engine = (Engine) DeserializerFactory.getDeserializer("application/json").deserialize(encodedValues, Engine.class);
			if (engine.getId() != null) {
				engineDao.modifyEngine(engine);
				logger.debug("Engine modified inserted");
			} else {
				engineDao.insertEngine(engine);
				logger.debug("New Engine inserted");
			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to save or modify engine", t);
		}

		try {
			logger.debug("Engine Operation succeded");
			JSONObject attributesResponseSuccessJSON = new JSONObject();
			attributesResponseSuccessJSON.put("success", true);
			attributesResponseSuccessJSON.put("responseText", "Operation succeded");
			writeBackToClient(new JSONSuccess(attributesResponseSuccessJSON));
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back response to client", t);
		}

	}

	private void deleteEngine(IEngineDAO engineDao) {
		Engine engine;

		try {
			Integer engineId = getAttributeAsInteger("id");
			engine = engineDao.loadEngineByID(engineId);

			if (engine != null) {
				engineDao.eraseEngine(engine);
			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to delete engine", t);
		}

		try {
			logger.debug("Engine Operation succeded");
			JSONObject attributesResponseSuccessJSON = new JSONObject();
			attributesResponseSuccessJSON.put("success", true);
			attributesResponseSuccessJSON.put("responseText", "Operation succeded");
			writeBackToClient(new JSONSuccess(attributesResponseSuccessJSON));
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back response to client", t);
		}

	}

	private void testEngine() {
		String message;
		try {
			JSONObject encodedValues = this.getAttributeAsJSONObject("engineValues");

			String driverName = encodedValues.getString("driver");
			String className = encodedValues.getString("engine_class");

			if (driverName != null && !driverName.equals("")) {
				Class.forName(driverName);
			} else if (className != null && !className.equals("")) {
				Class.forName(className);
			} else {
				message = "Class Name Error";
			}
			message = "Operation succeded";

		} catch (ClassNotFoundException e) {
			message = "Class Name Error";
			logger.error("testEngine",e);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to save or modify engine", t);
		}

		try {
			if (message.equals("Operation succeded")) {
				logger.debug("Test succeded");
				JSONObject attributesResponseSuccessJSON = new JSONObject();
				attributesResponseSuccessJSON.put("success", true);
				attributesResponseSuccessJSON.put("responseText", message);
				writeBackToClient(new JSONSuccess(attributesResponseSuccessJSON));
			} else {
				logger.debug("Test succeded");
				JSONObject attributesResponseSuccessJSON = new JSONObject();
				attributesResponseSuccessJSON.put("success", false);
				attributesResponseSuccessJSON.put("responseText", message);
				writeBackToClient(new JSONSuccess(attributesResponseSuccessJSON));
			}

		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back response to client", t);
		}

	}

}
