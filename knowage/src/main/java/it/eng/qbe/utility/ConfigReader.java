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
package it.eng.qbe.utility;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;

/**
 * @author Gavardi Giulio(giulio.gavardi@eng.it)
 */

public class ConfigReader extends SimpleRestClient {

	private static final Logger LOGGER = Logger.getLogger(ConfigReader.class);
	private static final String CONFIG_LABEL = "KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS";

	UserProfile userProfile = null;

	public ConfigReader(UserProfile _userProfile) {
		userProfile = _userProfile;
	}

	public String readCustom() throws Exception {

		LOGGER.debug("IN");

		LOGGER.debug("call DAO");
		IConfigDAO configDAO = DAOFactory.getSbiConfigDAO();
		configDAO.setUserProfile(userProfile);
		Config config = configDAO.loadConfigParametersByLabel(CONFIG_LABEL);

		String toReturn = config != null ? config.getValueCheck() : null;

		LOGGER.debug("OUT");

		return toReturn;
	}

}
