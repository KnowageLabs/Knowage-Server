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

package it.eng.spagobi.api.v2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/1.0/user-configs")
public class FinalUserConfigResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(FinalUserConfigResource.class);

	/**
	 * In order to add new configurations to the list of the ones visible to the final user just add an item to the following list that contains the string label of
	 * the config.
	 */
	private static final List<String> USER_CONFIG_LABELS = Arrays.asList("SPAGOBI.SESSION_PARAMETERS_MANAGER.enabled",
			"SPAGOBI.DATE-FORMAT-SERVER.format", "SPAGOBI.TIMESTAMP-FORMAT.format",
			"KNOWAGE.DOWNLOAD.MANUAL_REFRESH","KNOWAGE.WEBSOCKET.DISABLE",
			"KNOWAGE.EMBEDDING_APPLICATION_VALUE", "KNOWAGE.RESOURCE.UPLOAD.MAX_SIZE", "home.button.url","KNOWAGE.HIDE_VERSION","oidc.session.polling.interval","oidc.session.polling.url");

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> getConfigs() {
		LOGGER.debug("Loading configurations");
		try {
			Map<String, String> userConfigs = new HashMap<>();
			IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());

			for (String label : USER_CONFIG_LABELS) {
				Optional<Config> cfg = configsDao.loadConfigParametersByLabelIfExist(label);
				if (cfg.isPresent()) {
					Config currCfg = cfg.get();
					if (currCfg.isActive()) {
						userConfigs.put(currCfg.getLabel(), currCfg.getValueCheck());
					}
				} else {
					LOGGER.warn("Configuration with label {} not found: it is required by /1.0/user-configs", label);
				}
			}

			return userConfigs;
		} catch (Exception e) {
			LOGGER.error("Error loading configurations", e);
			throw new SpagoBIRuntimeException("Error while getting the list of user configs", e);
		} finally {
			LOGGER.debug("End loading configurations");
		}
	}
}