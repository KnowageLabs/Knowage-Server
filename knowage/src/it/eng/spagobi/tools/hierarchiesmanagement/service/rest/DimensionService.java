package it.eng.spagobi.tools.hierarchiesmanagement.service.rest;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Dimension;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/dimensions")
public class DimensionService {

	static private Logger logger = Logger.getLogger(DimensionService.class);

	@GET
	// @Path("/dimensions")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDimensions(@Context HttpServletRequest req) {

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();
		SourceBean sb = hierarchies.getTemplate();
		JSONArray dimesionsJSONArray = new JSONArray();

		try {
			SourceBean dimensions = (SourceBean) sb.getAttribute(HierarchyConstants.DIMENSIONS);

			List lst = dimensions.getAttributeAsList(HierarchyConstants.DIMENSION);
			for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
				JSONObject dimension = new JSONObject();
				SourceBean sbRow = (SourceBean) iterator.next();
				// String name = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
				String label = sbRow.getAttribute(HierarchyConstants.LABEL) != null ? sbRow.getAttribute(HierarchyConstants.LABEL).toString() : null;
				dimension.put("DIMENSION_NM", label);
				String datasource = sbRow.getAttribute(HierarchyConstants.DATASOURCE) != null ? sbRow.getAttribute(HierarchyConstants.DATASOURCE).toString()
						: null;
				dimension.put("DIMENSION_DS", datasource);
				dimesionsJSONArray.put(dimension);
			}

			return dimesionsJSONArray.toString();

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving dimensions names");
			throw new SpagoBIServiceException("An unexpected error occured while retriving dimensions names", t);
		}

	}

	@GET
	@Path("/dimensionMetadata")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDimensionFields(@QueryParam("dimension") String dimensionName) {

		logger.debug("START");

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();

		Dimension dimension = hierarchies.getDimension(dimensionName);

		List<Field> metadataFields = new ArrayList<Field>(dimension.getMetadataFields());

		JSONObject jsonDimension = new JSONObject();

		try {

			jsonDimension = HierarchyUtils.createJSONObjectFromFieldsList(metadataFields, HierarchyConstants.DIM_FIELDS, false);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", t);
		}

		logger.debug("END");
		return jsonDimension.toString();

	}

}
