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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/1.0/user-configs")
public class FinalUserConfigResource extends AbstractSpagoBIResource {

	/*
	 * In order to add new configurations to the list of the ones visible to the final user just add an item to the following list that contains the string
	 * label of the config. ex: Arrays.asList("a", "b", "c");
	 */
	private static final List<String> userConfigLabels = Arrays.asList("SPAGOBI.SESSION_PARAMETERS_MANAGER.enabled", "SPAGOBI.DATE-FORMAT-SERVER.format",
			"SPAGOBI.TIMESTAMP-FORMAT.format");

	private static Logger logger = Logger.getLogger(ConfigResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> getConfigs() {
		logger.debug("IN");
		try {
			Map<String, String> userConfigs = new HashMap<String, String>();
			IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());

			for (String label : userConfigLabels) {
				Config cfg = configsDao.loadConfigParametersByLabel(label);
				if (cfg.isActive())
					userConfigs.put(cfg.getLabel(), cfg.getValueCheck());
			}

			return userConfigs;
		} catch (Exception e) {
			logger.error("Error while getting the list of user configs", e);
			throw new SpagoBIRuntimeException("Error while getting the list of user configs", e);
		} finally {
			logger.debug("OUT");
		}
	}
}