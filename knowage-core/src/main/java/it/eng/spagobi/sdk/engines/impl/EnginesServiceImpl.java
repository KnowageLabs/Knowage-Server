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
package it.eng.spagobi.sdk.engines.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.sdk.AbstractSDKService;
import it.eng.spagobi.sdk.engines.EnginesService;
import it.eng.spagobi.sdk.engines.bo.SDKEngine;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.utilities.SDKObjectsConverter;

public class EnginesServiceImpl extends AbstractSDKService implements EnginesService {

	private static final Logger LOGGER = Logger.getLogger(EnginesServiceImpl.class);

	@Override
	public SDKEngine getEngine(Integer engineId) throws NotAllowedOperationException {
		SDKEngine toReturn = null;
		LOGGER.debug("IN: engineId in input = " + engineId);

		this.setTenant();

		try {
			super.checkUserPermissionForFunctionality(CommunityFunctionalityConstants.READ_ENGINES_MANAGEMENT, "User cannot see engines congifuration.");
			if (engineId == null) {
				LOGGER.warn("Engine identifier in input is null!");
				return null;
			}
			Engine engine = DAOFactory.getEngineDAO().loadEngineByID(engineId);
			if (engine == null) {
				LOGGER.warn("Engine with identifier [" + engineId + "] not existing.");
				return null;
			}
			toReturn = new SDKObjectsConverter().fromEngineToSDKEngine(engine);
		} catch (NotAllowedOperationException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("Error while retrieving SDKEngine", e);
			LOGGER.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			LOGGER.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public SDKEngine[] getEngines() throws NotAllowedOperationException {
		SDKEngine[] toReturn = null;
		LOGGER.debug("IN");

		this.setTenant();

		try {
			super.checkUserPermissionForFunctionality(CommunityFunctionalityConstants.READ_ENGINES_MANAGEMENT, "User cannot see engines congifuration.");
			List<Engine> enginesList = DAOFactory.getEngineDAO().loadAllEngines();
			List<SDKEngine> sdkEnginesList = new ArrayList<>();
			if (enginesList != null && !enginesList.isEmpty()) {
				for (Iterator<Engine> it = enginesList.iterator(); it.hasNext();) {
					Engine engine = it.next();
					SDKEngine sdkEngine = new SDKObjectsConverter().fromEngineToSDKEngine(engine);
					sdkEnginesList.add(sdkEngine);
				}
			}
			toReturn = new SDKEngine[sdkEnginesList.size()];
			toReturn = sdkEnginesList.toArray(toReturn);
		} catch (NotAllowedOperationException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("Error while retrieving SDKEngine list", e);
			LOGGER.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			LOGGER.debug("OUT");
		}
		return toReturn;
	}

}
