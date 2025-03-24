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
package it.eng.spagobi.analiticalmodel.execution.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

public class ExecuteAdHocUtility {

	// logger component
	private static Logger logger = Logger.getLogger(ExecuteAdHocUtility.class);

	public static Engine getQbeEngine() {
		return getEngineByDriver("it.eng.spagobi.engines.drivers.qbe.QbeDriver");
	}

	public static Engine getCockpitEngine() {
		return getEngineByDriver("it.eng.spagobi.engines.drivers.cockpit.CockpitDriver");
	}

	public static Engine getKPIEngine() {
		return getEngineByDriver("it.eng.spagobi.engines.drivers.kpi.KpiDriver");
	}

	public static Engine getDossierEngine() {
		return getEngineByDriver("it.eng.spagobi.engines.drivers.dossier.DossierDriver");
	}

	public static Engine getEngineByDriver(String driver) {
		Engine engine;

		engine = null;
		try {
			Assert.assertNotNull(DAOFactory.getEngineDAO(), "EngineDao cannot be null");
			engine = DAOFactory.getEngineDAO().loadEngineByDriver(driver);
			if (engine == null) {
				throw new SpagoBIRuntimeException(
						"There are no engines with driver equal to [" + driver + "] available");
			}
		} catch (Exception t) {
			throw new SpagoBIRuntimeException(
					"Impossible to load a valid engine whose drover is equal to [" + driver + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return engine;
	}

	public static Engine getEngineByDocumentType(String type) {
		Engine engine = null;
		try {
			Assert.assertNotNull(DAOFactory.getEngineDAO(), "EngineDao cannot be null");
			List<Engine> engines = DAOFactory.getEngineDAO().loadAllEnginesForBIObjectType(type);
			if (engines == null || engines.isEmpty()) {
				throw new SpagoBIRuntimeException(
						"There are no engines for documents of type [" + type + "] available");
			} else {
				engine = engines.get(0);
				LogMF.warn(logger, "There are more than one engine for document of type [" + type
						+ "]. We will use the one whose label is equal to [{0}]", engine.getLabel());
			}
		} catch (Exception t) {
			throw new SpagoBIRuntimeException("Impossible to load a valid engine for document of type [" + type + "]",
					t);
		} finally {
			logger.debug("OUT");
		}

		return engine;
	}

	public static String createNewExecutionId() {
		String executionId = null;

		logger.debug("IN");

		UUID uuidObj = UUID.randomUUID();
		executionId = uuidObj.toString();
		executionId = executionId.replace("-", "");

		logger.debug("OUT");

		return executionId;
	}

	// returns true if the dataset is geospazial
	public static boolean hasGeoHierarchy(String meta) throws JSONException, IOException {

		JSONObject metadataObject = JSONUtils.toJSONObject(meta);
		if (metadataObject == null) {
			return false;
		}

		JSONArray columnsMetadataArray = metadataObject.optJSONArray("columns");
		if (columnsMetadataArray != null) {
			for (int j = 0; j < columnsMetadataArray.length(); j++) {
				JSONObject columnJsonObject = columnsMetadataArray.getJSONObject(j);
				String propertyName = columnJsonObject.getString("pname");
				String propertyValue = columnJsonObject.getString("pvalue");

				if (propertyName.equalsIgnoreCase("hierarchy")) {
					if (propertyValue.equalsIgnoreCase("geo")) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private ExecuteAdHocUtility() {

	}
}
