package it.eng.spagobi.tools.hierarchiesmanagement.service.rest;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Dimension;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyUtils;
import it.eng.spagobi.utilities.assertion.Assert;
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

	// get dimensions available (defined into the configurations)
	@GET
	@Path("/getDimensions")
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

	// Get metadata of dimension
	@GET
	@Path("/dimensionMetadata")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDimensionFields(@QueryParam("dimension") String dimensionLabel) {

		logger.debug("START");

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();

		Dimension dimension = hierarchies.getDimension(dimensionLabel);

		List<Field> metadataFields = new ArrayList<Field>(dimension.getMetadataFields());

		JSONObject result = new JSONObject();

		try {

			JSONArray jsonDimension = HierarchyUtils.createJSONArrayFromFieldsList(metadataFields, false);
			result.put(HierarchyConstants.DIM_FIELDS, jsonDimension);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", t);
		}

		logger.debug("END");
		return result.toString();

	}

	@GET
	@Path("/dimensionData")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDimensionData(@QueryParam("dimension") String dimensionLabel, @QueryParam("validityDate") String validityDate,
			@QueryParam("filterDate") String filterDate, @QueryParam("filterHierarchy") String filterHierarchy,
			@QueryParam("filterHierType") String filterHierType) {

		logger.debug("START");

		JSONObject result = new JSONObject();

		try {

			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			Assert.assertNotNull(hierarchies, "Impossible to find a valid hierarchies object");

			Dimension dimension = hierarchies.getDimension(dimensionLabel);
			Assert.assertNotNull(dimension, "Impossible to find a valid dimension with label [" + dimensionLabel + "]");

			String hierTableName = hierarchies.getHierarchyTableName(dimensionLabel);
			String prefix = hierarchies.getPrefix(dimensionLabel);

			List<Field> metadataFields = new ArrayList<Field>(dimension.getMetadataFields());

			IDataSource dataSource = HierarchyUtils.getDataSource(dimensionLabel);

			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}

			IDataStore dataStore = HierarchyUtils.getDimensionDataStore(dataSource, dimensionLabel, metadataFields, validityDate, filterDate, filterHierarchy,
					filterHierType, hierTableName, prefix);

			// Create JSON for Dimension data from datastore
			JSONArray rootArray = HierarchyUtils.createRootData(dataStore);
			JSONArray columnsArray = HierarchyUtils.createJSONArrayFromFieldsList(metadataFields, false);
			JSONArray columnsSearchArray = HierarchyUtils.createColumnsSearch(metadataFields);

			if (rootArray == null || columnsArray == null || columnsSearchArray == null) {
				return null;
			}

			logger.debug("Root array is [" + rootArray.toString() + "]");
			result.put(HierarchyConstants.ROOT, rootArray);

			logger.debug("Columns array is [" + columnsArray.toString() + "]");
			result.put(HierarchyConstants.COLUMNS, columnsArray);

			logger.debug("Columns Search array is [" + columnsSearchArray.toString() + "]");
			result.put(HierarchyConstants.COLUMNS_SEARCH, columnsSearchArray);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving dimension data");
			throw new SpagoBIServiceException("An unexpected error occured while retriving dimension data", t);
		}

		logger.debug("JSON for dimension data is [" + result.toString() + "]");
		logger.debug("END");
		return result.toString();

	}
}
