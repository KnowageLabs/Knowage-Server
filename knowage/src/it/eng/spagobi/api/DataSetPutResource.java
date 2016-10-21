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

/**
 * Authors: Nikola Simović (nikola.simovic@mht.net)
 */

package it.eng.spagobi.api;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDataSetException;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.service.ManageDataSetsForREST;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Nikola Simovic (nikola.simovic@mht.net)
 */
@Path("/1.0/datasets")
public class DataSetPutResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DataSetPutResource.class);

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String persistDataSets(@Context HttpServletRequest req) throws IOException, JSONException {
		IDataSetDAO dsDao;
		try {
			dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(getUserProfile());
		} catch (SpagoBIDataSetException dse) {
			throw new SpagoBIRestServiceException(getLocale(), dse);
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
		JSONObject json = RestUtilities.readBodyAsJSONObject(req);
		ManageDataSetsForREST mdsfr = new ManageDataSetsForREST();
		String toReturnString = mdsfr.jsonReciever(json.toString(), dsDao, null, getUserProfile(), req);
		// SONObject toReturn = new JSONObject(toReturnString);
		return toReturnString;
	}

	@POST
	@Path("/preview")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String previewDataSet(@Context HttpServletRequest req) throws IOException, JSONException {

		JSONObject json = RestUtilities.readBodyAsJSONObject(req);
		ManageDataSetsForREST mdsfr = new ManageDataSetsForREST();
		String toReturnString = mdsfr.jsonPreviewReciever(json.toString(), getUserProfile());
		// SONObject toReturn = new JSONObject(toReturnString);
		return toReturnString;
	}

}
