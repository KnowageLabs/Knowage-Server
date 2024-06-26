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
package it.eng.spagobi.tools.hierarchiesmanagement.service.rest;

import java.util.ArrayList;
import java.util.HashMap;
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

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Dimension;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Filter;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/dimensions")
public class DimensionService {

	private static Logger logger = Logger.getLogger(DimensionService.class);

	// get dimensions available (defined into the configurations)
	@GET
	@Path("/getDimensions")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.HIERARCHIES_MANAGEMENT })
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
				String prefix = sbRow.getAttribute(HierarchyConstants.PREFIX) != null ? sbRow.getAttribute(HierarchyConstants.PREFIX).toString() : null;
				dimension.put("DIMENSION_PREFIX", prefix);
				String datasource = sbRow.getAttribute(HierarchyConstants.DATASOURCE) != null ? sbRow.getAttribute(HierarchyConstants.DATASOURCE).toString()
						: null;
				dimension.put("DIMENSION_DS", datasource);
				dimesionsJSONArray.put(dimension);
			}

			return dimesionsJSONArray.toString();

		} catch (Throwable t) {
			String message = "An unexpected error occured while retriving dimensions names";
			logger.error(message);
			throw new SpagoBIServiceException(message, t);
		}

	}

	// Get metadata of dimension
	@GET
	@Path("/dimensionMetadata")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.HIERARCHIES_MANAGEMENT })
	public String getDimensionFields(@QueryParam("dimension") String dimensionLabel) {

		logger.debug("START");

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();

		Dimension dimension = hierarchies.getDimension(dimensionLabel);

		List<Field> metadataFields = new ArrayList<Field>(dimension.getMetadataFields());

		JSONObject result = new JSONObject();

		try {
			JSONArray jsonDimension = HierarchyUtils.createJSONArrayFromFieldsList(metadataFields, false);
			result.put(HierarchyConstants.DIM_FIELDS, jsonDimension);

			// adds columns matching configurations
			HashMap configsMap = hierarchies.getConfig(dimensionLabel);
			JSONObject matchDim = new JSONObject();
			if (configsMap.get(HierarchyConstants.DIMENSION_ID) != null && configsMap.get(HierarchyConstants.TREE_LEAF_ID) != null) {
				matchDim.put((String) configsMap.get(HierarchyConstants.DIMENSION_ID), (String) configsMap.get(HierarchyConstants.TREE_LEAF_ID));
			}
			if (configsMap.get(HierarchyConstants.DIMENSION_CD) != null && configsMap.get(HierarchyConstants.TREE_LEAF_CD) != null) {
				matchDim.put((String) configsMap.get(HierarchyConstants.DIMENSION_CD), (String) configsMap.get(HierarchyConstants.TREE_LEAF_CD));
			}
			if (configsMap.get(HierarchyConstants.DIMENSION_NM) != null && configsMap.get(HierarchyConstants.TREE_LEAF_NM) != null) {
				matchDim.put((String) configsMap.get(HierarchyConstants.DIMENSION_NM), (String) configsMap.get(HierarchyConstants.TREE_LEAF_NM));
			}
			result.put(HierarchyConstants.MATCH_LEAF_FIELDS, matchDim);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", t);
		}

		logger.debug("END");
		return result.toString();

	}

	@GET
	@Path("/dimensionFilterMetadata")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.HIERARCHIES_MANAGEMENT })
	public String getDimensionFilters(@QueryParam("dimension") String dimensionLabel) {
		// Get metadata filters of dimension
		logger.debug("START");

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();

		Dimension dimension = hierarchies.getDimension(dimensionLabel);

		List<Filter> metadataFilters = new ArrayList<Filter>(dimension.getMetadataFilters());

		JSONObject result = new JSONObject();

		try {
			JSONArray jsonFilterDimension = HierarchyUtils.createJSONArrayFromFiltersList(metadataFilters);
			result.put(HierarchyConstants.DIM_FILTERS, jsonFilterDimension);
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
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.HIERARCHIES_MANAGEMENT })
	public String getDimensionData(@QueryParam("dimension") String dimensionLabel, @QueryParam("validityDate") String validityDate,
			@QueryParam("filterDate") String filterDate, @QueryParam("filterHierarchy") String filterHierarchy,
			@QueryParam("filterHierType") String filterHierType, @QueryParam("optionalFilters") String optionalFilters) {

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

			String dimensionName = dimension.getName();

			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}

			boolean exludeHierLeaf = (filterHierarchy != null) ? true : false;
			IDataStore dataStore = HierarchyUtils.getDimensionDataStore(dataSource, dimensionName, metadataFields, validityDate, optionalFilters, filterDate,
					filterHierarchy, filterHierType, hierTableName, prefix, exludeHierLeaf);

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
