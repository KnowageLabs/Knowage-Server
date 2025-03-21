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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.tag.SbiTag;
import it.eng.spagobi.tools.tag.dao.ISbiTagDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/2.0/tags")
public class TagResource extends AbstractSpagoBIResource {

	protected static Logger logger = Logger.getLogger(TagResource.class);

	private final String CHARSET_UTF8 = "; charset=UTF-8";

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF8)
	public Response getTags() {
		logger.debug("IN");
		List<SbiTag> tags = new ArrayList<>();
		ISbiTagDAO tagDao = null;
		try {
			tagDao = DAOFactory.getSbiTagDao();
			tagDao.setUserProfile(getUserProfile());
			tags = tagDao.loadTags();
			logger.debug("OUT");
			return Response.ok(tags).build();
		} catch (Exception e) {
			logger.error("Error has occurred while loading Tags", e);
			throw new SpagoBIRestServiceException("Cannot return Tags", buildLocaleFromSession(), e);
		}
	}

	@DELETE
	@Path("{tagId}")
	public Response deleteTag(@PathParam("tagId") Integer tagId) {
		logger.debug("IN");
		ISbiTagDAO tagDao = null;
		try {
			tagDao = DAOFactory.getSbiTagDao();
			tagDao.setUserProfile(getUserProfile());
			tagDao.deleteTag(tagId);
		} catch (Exception e) {
			logger.error("Error has occurred while deleting Tag [" + tagId + "]", e);
			throw new SpagoBIRestServiceException("Cannot delete Tag", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return Response.ok().build();
	}

}
