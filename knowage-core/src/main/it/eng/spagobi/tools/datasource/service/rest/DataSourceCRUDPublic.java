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

package it.eng.spagobi.tools.datasource.service.rest;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
@Path("/datasourcespublic")
public class DataSourceCRUDPublic {

	static private Logger logger = Logger.getLogger(DataSourceCRUDPublic.class);


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllDataSourcesLabel(@Context HttpServletRequest req) {
		IDataSourceDAO dataSourceDao = null;
		List<DataSource> dataSources;
		IEngUserProfile profile = (IEngUserProfile) req.getSession()
				.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		JSONArray datasorcesJSON = new JSONArray();
		try {
			dataSourceDao = DAOFactory.getDataSourceDAO();
			dataSourceDao.setUserProfile(profile);
			dataSources = dataSourceDao.loadAllDataSources();
			if (dataSources != null) {
				for(int i=0; i<dataSources.size(); i++){
					JSONObject jo = new JSONObject();
					jo.put("label", dataSources.get(i).getLabel());
					datasorcesJSON.put(jo);
				}
			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException(
					"An unexpected error occured while instatiating the dao", t);
		}
		return datasorcesJSON.toString();

	}

}
