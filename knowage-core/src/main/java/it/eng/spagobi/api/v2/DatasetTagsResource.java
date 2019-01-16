/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.tag.SbiTag;
import it.eng.spagobi.tools.tag.dao.ISbiTagDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/")
public class DatasetTagsResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DatasetTagsResource.class);

	private final String CHARSET_UTF8 = "; charset=UTF-8";

	@GET
	@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF8)
	public Response getDatasetTags(@PathParam("dsId") Integer dsId, @QueryParam("versNum") Integer versNum) {
		logger.debug("IN");
		List<SbiTag> datasetTags = new ArrayList<>();
		ISbiTagDAO tagDao = null;
		try {
			tagDao = DAOFactory.getSbiTagDao();
			tagDao.setUserProfile(getUserProfile());
			String organization = getUserProfile().getOrganization();
			SbiDataSetId datasetId = new SbiDataSetId(dsId, versNum, organization);
			datasetTags = tagDao.loadTagsByDatasetId(datasetId);
			return Response.ok(datasetTags).build();
		} catch (Exception e) {
			logger.error("Error has occurred while loading Tags for Dataset [" + dsId + "], version number: [" + versNum + "]", e);
			throw new SpagoBIRestServiceException("Cannot return Dataset Tags", buildLocaleFromSession(), e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON + CHARSET_UTF8)
	@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF8)
	public Response associateTagsToDataset(@PathParam("dsId") Integer dsId, @Context HttpServletRequest req) {
		logger.debug("IN");
		ISbiTagDAO tagDao = null;
		Integer versNum = 0;
		List<SbiTag> toReturn = new ArrayList<>();
		try {
			JSONObject body = RestUtilities.readBodyAsJSONObject(req);
			tagDao = DAOFactory.getSbiTagDao();
			tagDao.setUserProfile(getUserProfile());
			String organization = getUserProfile().getOrganization();
			versNum = body.getInt("versNum");

			SbiDataSetId datasetId = new SbiDataSetId(dsId, versNum, organization);
			JSONArray tagsToAdd = body.getJSONArray("tagsToAdd");
			toReturn = tagDao.associateTagsToDatasetVersion(datasetId, tagsToAdd);
		} catch (Exception e) {
			logger.error("Error has occurred while associating Tags for Dataset [" + dsId + "], version number: [" + versNum + "]", e);
			throw new SpagoBIRestServiceException("Cannot associate Tags to Dataset", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return Response.ok(toReturn).build();
	}

}
