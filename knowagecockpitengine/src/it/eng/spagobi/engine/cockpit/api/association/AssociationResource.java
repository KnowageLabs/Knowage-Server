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
package it.eng.spagobi.engine.cockpit.api.association;

import it.eng.spagobi.engine.cockpit.api.AbstractCockpitEngineResource;
import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroupJSONSerializer;
import it.eng.spagobi.tools.dataset.common.association.AssociationJSONSerializer;
import it.eng.spagobi.tools.dataset.common.association.AssociationManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
@Path("/1.0/associations")
public class AssociationResource extends AbstractCockpitEngineResource {

	static private Logger logger = Logger.getLogger(AssociationResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getAssociations() {

		logger.debug("IN");
		try {
			AssociationManager associationManager = this.getEngineInstance().getAssociationManager();
			List<Association> associations = associationManager.getAssociations();
			return serializeAssociations(associations).toString();
		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String setAssociations(String jsonData) {

		logger.debug("IN");
		try {
			JSONObject dataJSON = new JSONObject(jsonData);
			JSONArray associationsJSON = dataJSON.getJSONArray("items");
			List<Association> associations = deserializeAssociations(associationsJSON);

			AssociationManager associationManager = this.getEngineInstance().getAssociationManager();
			associationManager.addAssociations(associations);
			List<AssociationGroup> associationGroups = associationManager.getAssociationGroups();

			return serializeAssociationGroups(associationGroups).toString();
		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
		} finally {
			logger.debug("OUT");
		}
	}

	// =======================================================================
	// SERIALIZATION METHODS
	// =======================================================================

	private JSONArray serializeAssociations(List<Association> associations) {
		try {
			AssociationJSONSerializer serializer = new AssociationJSONSerializer();
			return serializer.serialize(associations);
		} catch (Exception e) {
			throw new RuntimeException("An unexpected error occured while serializing results", e);
		}
	}

	private List<Association> deserializeAssociations(JSONArray associations) {
		try {
			AssociationJSONSerializer serializer = new AssociationJSONSerializer();
			return serializer.deserialize(associations);
		} catch (Exception e) {
			throw new RuntimeException("An unexpected error occured while serializing results", e);
		}
	}

	private JSONArray serializeAssociationGroups(List<AssociationGroup> associationGroups) {
		try {
			AssociationGroupJSONSerializer serializer = new AssociationGroupJSONSerializer();
			return serializer.serialize(associationGroups);
		} catch (Exception e) {
			throw new RuntimeException("An unexpected error occured while serializing results", e);
		}
	}

}
