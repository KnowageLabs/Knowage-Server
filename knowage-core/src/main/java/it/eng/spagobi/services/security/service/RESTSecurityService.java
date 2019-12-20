/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.services.security.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.widgets.dao.ISbiPythonWidgetDAO;
import it.eng.spagobi.tools.widgets.metadata.SbiPythonWidget;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/userprofile")
public class RESTSecurityService {

	static protected Logger logger = Logger.getLogger(RESTSecurityService.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public UserProfile getUserProfile() {
		logger.debug("IN");
		UserProfile userProfile = UserProfileManager.getProfile();
		logger.debug("OUT");
		return userProfile;
	}

	@POST
	@Path("/savescript")
	@Consumes(MediaType.APPLICATION_JSON)
	public void saveScriptOnDB(PythonWidget pythonWidget) {
		logger.debug("IN");
		try {
			ISbiPythonWidgetDAO sbiPythonWidgetDAO = DAOFactory.getSbiPythonWidgetDAO();
			sbiPythonWidgetDAO.saveWidget(pythonWidget.getDocumentId(), pythonWidget.getWidgetId(), pythonWidget.getUserId(), pythonWidget.getScript());
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/loadscript")
	@Consumes(MediaType.APPLICATION_JSON)
	public String loadScriptFromDB(PythonWidget pythonWidget) {
		SbiPythonWidget sbiPythonWidget = null;
		logger.debug("IN");
		try {
			ISbiPythonWidgetDAO sbiPythonWidgetDAO = DAOFactory.getSbiPythonWidgetDAO();
			sbiPythonWidget = sbiPythonWidgetDAO.loadWidget(pythonWidget.getDocumentId(), pythonWidget.getWidgetId());
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		} finally {
			logger.debug("OUT");
		}
		return sbiPythonWidget.getPythonScript();
	}
}
