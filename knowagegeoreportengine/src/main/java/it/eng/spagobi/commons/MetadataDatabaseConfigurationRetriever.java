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
package it.eng.spagobi.commons;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This object is instantiated and used by SingletonConfig to read config parameters. SpagoBi project have its own implementation. The engines may have
 * different implementations.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class MetadataDatabaseConfigurationRetriever implements IConfigurationRetriever {

	private static Logger logger = Logger.getLogger(MetadataDatabaseConfigurationRetriever.class);

	public MetadataDatabaseConfigurationRetriever() {
	}

	@Override
	public String get(String key) {
		String toReturn = null;
		try {
			IConfigDAO dao = DAOFactory.getSbiConfigDAO();
			Config config = dao.loadConfigParametersByLabel(key);
			if (config != null) {
				toReturn = config.getValueCheck();
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while getting configuration with label [" + key + "]", e);
		}

		if (toReturn == null) {
			logger.info("The property '" + key + "' doens't have any value assigned, check SBI_CONFIG table");
			return null;
		}
		logger.debug("GET :" + key + "=" + toReturn);
		return toReturn;
	}


	@Override
	public List<IConfiguration> getByCategory(String category) {
		try {
			IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(UserProfileManager.getProfile());
			List<Config> returnedVals = configsDao.loadConfigParametersByCategory(category);
			return new ArrayList<IConfiguration>(returnedVals);
		} catch (Exception e) {
			logger.error("Error while getting the list of configs", e);
			throw new SpagoBIRuntimeException("Error while getting the list of configs", e);
		}
	}

}
