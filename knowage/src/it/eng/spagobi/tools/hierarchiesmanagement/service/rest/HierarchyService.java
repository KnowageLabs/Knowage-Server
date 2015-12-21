package it.eng.spagobi.tools.hierarchiesmanagement.service.rest;

import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Hierarchy;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
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

@Path("/hierarchies")
public class HierarchyService {

	static private Logger logger = Logger.getLogger(HierarchyService.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchyNode(@QueryParam("dimension") String dimensionName) {

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();

		Hierarchy hierarchy = hierarchies.getHierarchy(dimensionName);

		List<Field> nodeMetadataFields = new ArrayList<Field>(hierarchy.getMetadataNodeFields());
		List<Field> leafMetadataFields = new ArrayList<Field>(hierarchy.getMetadataLeafFields());

		JSONObject result = new JSONObject();

		JSONArray nodeFieldsJSONArray = new JSONArray();
		JSONArray leafFieldsJSONArray = new JSONArray();

		try {

			for (Field tempField : nodeMetadataFields) {

				JSONObject dimensionJSON = HierarchyUtils.createJSONObjectFromField(tempField, true);
				nodeFieldsJSONArray.put(dimensionJSON);
			}

			result.put(HierarchyConstants.NODE_FIELDS, nodeFieldsJSONArray);

			for (Field tempField : leafMetadataFields) {

				JSONObject dimensionJSON = HierarchyUtils.createJSONObjectFromField(tempField, true);
				nodeFieldsJSONArray.put(dimensionJSON);
			}

			return nodeFieldsJSONArray.toString();

		} catch (Throwable t) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", t);
		}

	}

	@GET
	@Path("/leaf")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchyLeaf(@QueryParam("dimension") String dimensionName) {

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();

		Hierarchy hierarchy = hierarchies.getHierarchy(dimensionName);

		List<Field> leafMetadataFields = new ArrayList<Field>(hierarchy.getMetadataLeafFields());

		JSONArray leafFieldsJSONArray = new JSONArray();

		try {

			for (Field tempField : leafMetadataFields) {

				JSONObject dimensionJSON = HierarchyUtils.createJSONObjectFromField(tempField, true);
				leafFieldsJSONArray.put(dimensionJSON);
			}

			return leafFieldsJSONArray.toString();

		} catch (Throwable t) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", t);
		}

	}

}
