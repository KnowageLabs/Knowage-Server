package it.eng.spagobi.tools.hierarchiesmanagement.service.rest;

import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Dimension;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/dimensions")
public class DimensionService {

	static private Logger logger = Logger.getLogger(DimensionService.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDimensions(@QueryParam("dimension") String dimensionName) {

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();

		Dimension dimension = hierarchies.getDimension(dimensionName);
		// Hierarchy h = hierarchies.getHierarchy(dimension);

		List<Field> metadataFields = new ArrayList<Field>(dimension.getMetadataFields());

		JSONArray dimesionFieldsJSONArray = new JSONArray();

		try {

			for (Field tempField : metadataFields) {

				JSONObject dimensionJSON = HierarchyUtils.createJSONObjectFromField(tempField, false);
				dimesionFieldsJSONArray.put(dimensionJSON);
			}

			return dimesionFieldsJSONArray.toString();

		} catch (Throwable t) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", t);
		}

	}

}
