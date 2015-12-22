/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.api.v2;

import static it.eng.spagobi.tools.glossary.util.Util.getNumberOrNull;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.AssociativeLogicManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.FilterCriteria;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.Operand;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroupJSONSerializer;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.ISbiDataSetDAO;
import it.eng.spagobi.tools.dataset.graph.AssociationAnalyzer;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceParameterException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jgrapht.graph.Pseudograph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.util.JSON;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 *
 */
@Path("/2.0/datasets")
public class DataSetResource extends it.eng.spagobi.api.DataSetResource {

	static protected Logger logger = Logger.getLogger(DataSetResource.class);

	@Override
	public String getDataSets(String typeDoc, String callback) {
		logger.debug("IN");

		ISbiDataSetDAO dsDAO;
		try {
			dsDAO = DAOFactory.getSbiDataSetDAO();
		} catch (EMFUserError e) {
			logger.error("Error while looking for datasets", e);
			throw new SpagoBIRuntimeException("Error while looking for datasets", e);
		}

		List<SbiDataSet> dataSets = dsDAO.loadSbiDataSets();
		List<SbiDataSet> toBeReturned = new ArrayList<SbiDataSet>();

		for (SbiDataSet dataset : dataSets) {
			IDataSet iDataSet = DataSetFactory.toDataSet(dataset);
			if (DataSetUtilities.isExecutableByUser(iDataSet, getUserProfile()))
				toBeReturned.add(dataset);
		}

		logger.debug("OUT");
		if (callback == null || callback.isEmpty())
			return JsonConverter.objectToJson(toBeReturned, toBeReturned.getClass());
		else {
			String jsonString = JsonConverter.objectToJson(toBeReturned, toBeReturned.getClass());

			return callback + "(" + jsonString + ")";
		}
	}

	@GET
	@Path("/listNotDerivedDataset")
	public String getNotDerivedDataSets(String typeDoc, String callback) {
		logger.debug("IN");

		ISbiDataSetDAO dsDAO;
		try {
			dsDAO = DAOFactory.getSbiDataSetDAO();
		} catch (EMFUserError e) {
			logger.error("Error while looking for datasets", e);
			throw new SpagoBIRuntimeException("Error while looking for datasets", e);
		}

		List<SbiDataSet> dataSets = dsDAO.loadNotDerivedSbiDataSets();
		List<SbiDataSet> toBeReturned = new ArrayList<SbiDataSet>();

		for (SbiDataSet dataset : dataSets) {
			if (DataSetUtilities.isExecutableByUser(dataset.isPublicDS(), dataset.getOwner(), getUserProfile()))
				toBeReturned.add(dataset);
		}

		logger.debug("OUT");
		if (callback == null || callback.isEmpty())
			return JsonConverter.objectToJson(toBeReturned, toBeReturned.getClass());
		else {
			String jsonString = JsonConverter.objectToJson(toBeReturned, toBeReturned.getClass());

			return callback + "(" + jsonString + ")";
		}
	}

	@Override
	public String getDataSet(String label) {

		ISbiDataSetDAO dsDAO;
		try {
			dsDAO = DAOFactory.getSbiDataSetDAO();
		} catch (EMFUserError e) {
			logger.error("Error while looking for datasets", e);
			throw new SpagoBIRuntimeException("Error while looking for datasets", e);
		}

		SbiDataSet dataset = dsDAO.loadSbiDataSetByLabel(label);

		if (dataset != null)
			return JsonConverter.objectToJson(dataset, SbiDataSet.class);
		else
			throw new SpagoBIRuntimeException("Dataset with label [" + label + "] doesn't exist");
	}

	@POST
	@Path("/")
	public Response addDataSet(String body) {
		SbiDataSet sbiDataset = (SbiDataSet) JsonConverter.jsonToValidObject(body, SbiDataSet.class);

		sbiDataset.setId(new SbiDataSetId(null, 1, getUserProfile().getOrganization()));
		sbiDataset.setOwner((String) getUserProfile().getUserUniqueIdentifier());
		IDataSet dataset = DataSetFactory.toDataSet(sbiDataset);

		try {
			DAOFactory.getDataSetDAO().insertDataSet(dataset);
		} catch (Exception e) {
			logger.error("Error while creating the dataset: " + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while creating the dataset: " + e.getMessage(), e);
		}

		try {
			return Response.created(new URI("1.0/datasets/" + dataset.getLabel().replace(" ", "%20"))).build();
		} catch (URISyntaxException e) {
			logger.error("Error while creating the resource url, maybe an error in the label", e);
			throw new SpagoBIRuntimeException("Error while creating the resource url, maybe an error in the label", e);
		}
	}

	@PUT
	@Path("/{label}")
	public Response modifyDataSet(@PathParam("label") String label, String body) {
		IDataSet dataset = null;

		try {
			dataset = getDatasetManagementAPI().getDataSet(label);
		} catch (Exception e) {
			logger.error("Error while creating the dataset: " + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while creating the dataset: " + e.getMessage(), e);
		}

		SbiDataSet sbiDataset = (SbiDataSet) JsonConverter.jsonToValidObject(body, SbiDataSet.class);

		int version = 1;
		if (dataset instanceof VersionedDataSet) {
			version = ((VersionedDataSet) dataset).getVersionNum();
		}

		sbiDataset.setId(new SbiDataSetId(dataset.getId(), version + 1, dataset.getOrganization()));
		sbiDataset.setOwner(dataset.getOwner());
		sbiDataset.setLabel(label);

		IDataSet newDataset = DataSetFactory.toDataSet(sbiDataset);

		try {
			DAOFactory.getDataSetDAO().modifyDataSet(newDataset);
		} catch (Exception e) {
			logger.error("Error while creating the dataset: " + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while creating the dataset: " + e.getMessage(), e);
		}

		return Response.ok().build();
	}

	@GET
	@Path("/listDataset")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDocumentSearchAndPaginate(@Context HttpServletRequest req, @QueryParam("Page") String pageStr,
			@QueryParam("ItemPerPage") String itemPerPageStr, @QueryParam("label") String search) throws EMFUserError {

		ISbiDataSetDAO dao = DAOFactory.getSbiDataSetDAO();
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		// TODO check if profile is null
		dao.setUserProfile(profile);

		Integer page = getNumberOrNull(pageStr);
		Integer item_per_page = getNumberOrNull(itemPerPageStr);
		search = search != null ? search : "";

		try {
			List<SbiDataSet> dataset = dao.loadPaginatedSearchSbiDataSet(search, page, item_per_page);

			JSONObject jo = new JSONObject();
			JSONArray ja = new JSONArray();
			for (SbiDataSet ds : dataset) {
				ja.put(JSON.parse(JsonConverter.objectToJson(ds, SbiDataSet.class)));
			}
			jo.put("item", ja);
			jo.put("itemCount", dao.countSbiDataSet(search));

			return jo.toString();
		} catch (Exception e) {
			logger.error("Error while getting the list of documents", e);
			throw new SpagoBIRuntimeException("Error while getting the list of documents", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/restrictions")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRestrictions(@QueryParam("associationGroup") String associationGroup, @QueryParam("sourceDataset") String sourceDataset,
			@QueryParam("sourceColumn") String sourceColumn, @QueryParam("sourceValue") String sourceValue) {
		logger.debug("IN");

		if (sourceDataset == null || sourceDataset.isEmpty()) {
			throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [sourceDataset] cannot be null or empty");
		}

		if (sourceColumn == null || sourceColumn.isEmpty()) {
			throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [sourceColumn] cannot be null or empty");
		}

		if (sourceValue == null || sourceValue.isEmpty()) {
			throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [sourceValue] cannot be null or empty");
		}

		AssociationGroup associationGroupObject = null;
		if (associationGroup == null) {
			throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [associationGroup] cannot be null");
		}
		try {
			AssociationGroupJSONSerializer serializer = new AssociationGroupJSONSerializer();
			associationGroupObject = serializer.deserialize(new JSONObject(associationGroup));
		} catch (JSONException e) {
			throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [associationGroup] is not a valid JSON object", e);
		}

		AssociationAnalyzer analyzer = new AssociationAnalyzer(associationGroupObject.getAssociations());
		Map<String, Map<String, String>> datasetToAssociationToColumnMap = analyzer.getDatasetToAssociationToColumnMap();
		Pseudograph<String, LabeledEdge<String>> graph = analyzer.getGraph();

		// add datasets to the map
		IDataSetDAO dataSetDAO;
		try {
			dataSetDAO = DAOFactory.getDataSetDAO();
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "Unable to get DataSetDAO", e);
		}
		Map<String, IDataSet> datasets = new HashMap<String, IDataSet>();
		for (String datasetLabel : graph.vertexSet()) {
			datasets.put(datasetLabel, dataSetDAO.loadDataSetByLabel(datasetLabel));
		}
		if (!datasets.containsKey(sourceDataset)) {
			datasets.put(sourceDataset, dataSetDAO.loadDataSetByLabel(sourceDataset));
		}
		if (datasets.containsValue(null)) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "A dataset doesn't exist");
		}

		AssociativeLogicManager manager = new AssociativeLogicManager(graph, datasets, datasetToAssociationToColumnMap, sourceDataset, sourceColumn);
		Map<EdgeGroup, Set<String>> egdegroupToValuesMap;
		try {
			egdegroupToValuesMap = manager.process();
		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "Unable to process associations", e);
		}

		Map<String, Map<String, Set<String>>> selections = AssociationAnalyzer.getSelections(associationGroupObject, graph, egdegroupToValuesMap);

		String stringFeed = JsonConverter.objectToJson(selections, Map.class);
		logger.debug("OUT");
		return stringFeed;
	}

	@Override
	protected List<FilterCriteria> getFilterCriteria(String dataset, JSONObject selectionsObject) throws JSONException {
		List<FilterCriteria> filterCriterias = new ArrayList<FilterCriteria>();

		JSONObject datasetSelectionObject = selectionsObject.getJSONObject(dataset);
		Iterator<String> it = datasetSelectionObject.keys();
		while (it.hasNext()) {
			String columns = it.next();

			JSONArray values = datasetSelectionObject.getJSONArray(columns);
			if (values.length() == 0)
				continue;
			List<String> valuesList = new ArrayList<String>();
			for (int i = 0; i < values.length(); i++) {
				valuesList.add(values.getString(i));
			}

			Operand leftOperand = new Operand(columns);
			Operand rightOperand = new Operand(valuesList);
			FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "IN", rightOperand);
			filterCriterias.add(filterCriteria);
		}

		return filterCriterias;
	}
}
