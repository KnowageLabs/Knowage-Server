package it.eng.spagobi.tools.hierarchiesmanagement.service.rest;

import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Hierarchy;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyUtils;
import it.eng.spagobi.utilities.assertion.Assert;
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
import org.json.JSONException;
import org.json.JSONObject;

@Path("/hierarchies")
public class HierarchyService {

	static private Logger logger = Logger.getLogger(HierarchyService.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchyFields(@QueryParam("dimension") String dimensionName) {

		logger.debug("START");

		JSONArray result = new JSONArray();

		try {

			result = createHierarchyJSONArray(dimensionName, false);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", t);
		}

		logger.debug("END");
		return result.toString();
	}

	@GET
	@Path("/node")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchyNodeFields(@QueryParam("dimension") String dimensionName, @QueryParam("excludeLeaf") boolean excludeLeaf) {

		logger.debug("START");

		JSONArray result = new JSONArray();

		try {

			result = createHierarchyJSONArray(dimensionName, excludeLeaf);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", t);
		}

		logger.debug("END");
		return result.toString();
	}

	/**
	 * This method manages the creation of the JSON for hierarchies fields
	 *
	 * @param dimensionName
	 *            the name of the dimension
	 * @param excludeLeaf
	 *            exclusion for fields in leaf section
	 * @return the JSON with fields in hierarchy section
	 * @throws JSONException
	 */
	private JSONArray createHierarchyJSONArray(String dimensionName, boolean excludeLeaf) throws JSONException {

		logger.debug("START");

		JSONArray result = new JSONArray();

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();
		Assert.assertNotNull(hierarchies, "Impossible to find valid hierarchies config");

		Hierarchy hierarchy = hierarchies.getHierarchy(dimensionName);
		Assert.assertNotNull(hierarchy, "Impossible to find a hierarchy for the dimension called [" + dimensionName + "]");

		List<Field> nodeMetadataFields = new ArrayList<Field>(hierarchy.getMetadataNodeFields());

		JSONObject nodeFieldsJSONObject = HierarchyUtils.createJSONObjectFromFieldsList(nodeMetadataFields, HierarchyConstants.NODE_FIELDS, true);
		result.put(nodeFieldsJSONObject);

		if (!excludeLeaf) { // add leaf fields
			List<Field> leafMetadataFields = new ArrayList<Field>(hierarchy.getMetadataLeafFields());

			JSONObject leafFieldsJSONObject = HierarchyUtils.createJSONObjectFromFieldsList(leafMetadataFields, HierarchyConstants.LEAF_FIELDS, true);
			result.put(leafFieldsJSONObject);
		}

		logger.debug("END");
		return result;

	}

}
