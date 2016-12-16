/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2;

import static it.eng.spagobi.tools.glossary.util.Util.getNumberOrNull;
import gnu.trove.set.hash.TLongHashSet;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.AssociativeLogicManager;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.FilterCriteria;
import it.eng.spagobi.tools.dataset.cache.Operand;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroupJSONSerializer;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.similarity.Similarity;
import it.eng.spagobi.tools.dataset.common.similarity.SimilarityEvaluator;
import it.eng.spagobi.tools.dataset.common.similarity.SimilarityStrategyFactory;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.ISbiDataSetDAO;
import it.eng.spagobi.tools.dataset.graph.AssociationAnalyzer;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.dataset.persist.IPersistedManager;
import it.eng.spagobi.tools.dataset.persist.PersistedHDFSManager;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceParameterException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.sql.SqlUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

		IDataSetDAO dsDAO = getDataSetDAO();

		List<IDataSet> toBeReturned = dsDAO.loadNotDerivedDataSets(getUserProfile());

		try {
			logger.debug("OUT");
			if (callback == null || callback.isEmpty())

				return ((JSONArray) SerializerFactory.getSerializer("application/json").serialize(toBeReturned, buildLocaleFromSession())).toString();

			else {
				String jsonString = ((JSONArray) SerializerFactory.getSerializer("application/json").serialize(toBeReturned, buildLocaleFromSession()))
						.toString();

				return callback + "(" + jsonString + ")";
			}
		} catch (SerializationException e) {
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
	}

	private IDataSetDAO getDataSetDAO() {
		IDataSetDAO dsDAO;
		try {
			dsDAO = DAOFactory.getDataSetDAO();
		} catch (EMFUserError e) {
			logger.error("Error while looking for datasets", e);
			throw new SpagoBIRuntimeException("Error while looking for datasets", e);
		}
		return dsDAO;
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

			if (dataset.isPersisted()) {
				IPersistedManager ptm = null;
				if (dataset.isPersistedHDFS()) {
					ptm = new PersistedHDFSManager();
				} else {
					ptm = new PersistedTableManager(getUserProfile());
				}
				ptm.persistDataSet(dataset);
			}
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
	@Path("/loadAssociativeSelections")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAssociativeSelections(@QueryParam("associationGroup") String associationGroupString, @QueryParam("selections") String selectionsString,
			@QueryParam("datasets") String datasetsString, @QueryParam("realtime") String realtimeDatasetsString) {
		logger.debug("IN");

		try {
			// parse selections
			if (selectionsString == null || selectionsString.isEmpty()) {
				throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [selections] cannot be null or empty");
			}
			JSONObject selectionsObject = new JSONObject(selectionsString);

			// parse association group
			if (associationGroupString == null) {
				throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [associationGroup] cannot be null");
			}

			AssociationGroupJSONSerializer serializer = new AssociationGroupJSONSerializer();

			JSONObject associationGroupObject = new JSONObject(associationGroupString);
			AssociationGroup associationGroup = serializer.deserialize(associationGroupObject);
			fixAssociationGroup(associationGroup);

			// parse documents
			Set<String> documents = new HashSet<String>();
			JSONArray associations = associationGroupObject.optJSONArray("associations");
			if (associations != null) {
				for (int associationIndex = 0; associationIndex < associations.length(); associationIndex++) {
					JSONObject association = associations.getJSONObject(associationIndex);
					JSONArray fields = association.optJSONArray("fields");
					if (fields != null) {
						for (int fieldIndex = fields.length() - 1; fieldIndex >= 0; fieldIndex--) {
							JSONObject field = fields.getJSONObject(fieldIndex);
							String type = field.optString("type");
							if ("document".equalsIgnoreCase(type)) {
								String store = field.optString("store");
								documents.add(store);
							}
						}
					}
				}
			}

			JSONObject associationGroupObjectWithoutParams = new JSONObject(associationGroupString);
			JSONArray associationsWithoutParams = associationGroupObjectWithoutParams.optJSONArray("associations");
			if (associationsWithoutParams != null) {
				for (int associationIndex = 0; associationIndex < associationsWithoutParams.length(); associationIndex++) {
					JSONObject association = associationsWithoutParams.getJSONObject(associationIndex);
					JSONArray fields = association.optJSONArray("fields");
					if (fields != null) {
						for (int fieldIndex = fields.length() - 1; fieldIndex >= 0; fieldIndex--) {
							JSONObject field = fields.getJSONObject(fieldIndex);
							String column = field.getString("column");
							String type = field.optString("type");
							if ("document".equalsIgnoreCase(type) || column.startsWith("$P{") && column.endsWith("}")) {
								fields.remove(fieldIndex);
							}
						}
					}
				}
			}
			AssociationGroup associationGroupWithoutParams = serializer.deserialize(associationGroupObjectWithoutParams);
			fixAssociationGroup(associationGroupWithoutParams);

			// parse dataset parameters
			Map<String, Map<String, String>> datasetParameters = new HashMap<String, Map<String, String>>();
			if (datasetsString != null && !datasetsString.isEmpty()) {
				JSONObject datasetsObject = new JSONObject(datasetsString);
				Iterator<String> datasetsIterator = datasetsObject.keys();
				while (datasetsIterator.hasNext()) {
					String datasetLabel = datasetsIterator.next();

					Map<String, String> parameters = new HashMap<String, String>();
					datasetParameters.put(datasetLabel, parameters);

					JSONObject datasetObject = datasetsObject.getJSONObject(datasetLabel);
					Iterator<String> datasetIterator = datasetObject.keys();
					while (datasetIterator.hasNext()) {
						String param = datasetIterator.next();
						String value = datasetObject.getString(param);
						parameters.put(param, value);
					}
				}
			}

			// parse realtime datasets
			Set<String> realtimeDatasets = new HashSet<String>();
			if (realtimeDatasetsString != null && !realtimeDatasetsString.isEmpty()) {
				JSONArray jsonArray = new JSONArray(realtimeDatasetsString);
				for (int i = 0; i < jsonArray.length(); i++) {
					realtimeDatasets.add(jsonArray.getString(i));
				}
			}

			AssociationAnalyzer analyzerWithoutParams = new AssociationAnalyzer(associationGroupWithoutParams.getAssociations());
			analyzerWithoutParams.process();
			Map<String, Map<String, String>> datasetToAssociationToColumnMap = analyzerWithoutParams.getDatasetToAssociationToColumnMap();

			AssociationAnalyzer analyzer = new AssociationAnalyzer(associationGroup.getAssociations());
			analyzer.process();
			Pseudograph<String, LabeledEdge<String>> graph = analyzer.getGraph();

			String selectedDataset = null;
			String selectedColumn = null;
			String value = null;
			IDataSource cacheDataSource = SpagoBICacheConfiguration.getInstance().getCacheDataSource();

			// get datasets from selections
			Map<String, String> filtersMap = new HashMap<String, String>();
			Map<String, Map<String, Set<String>>> selectionsMap = new HashMap<String, Map<String, Set<String>>>();
			Iterator<String> it = selectionsObject.keys();
			while (it.hasNext()) {
				String datasetDotColumn = it.next();
				if (datasetDotColumn.indexOf(".") >= 0) {
					String[] tmpDatasetAndColumn = datasetDotColumn.split("\\.");
					if (tmpDatasetAndColumn.length == 2) {
						String datasetLabel = tmpDatasetAndColumn[0];
						String column = tmpDatasetAndColumn[1];
						column = SqlUtils.unQuote(column);

						if (datasetLabel != null && !datasetLabel.isEmpty() && column != null && !column.isEmpty()) {
							value = selectionsObject.getJSONArray(datasetDotColumn).get(0).toString();
							IDataSet dataset = getDataSetDAO().loadDataSetByLabel(datasetLabel);
							String filter;
							if (realtimeDatasets.contains(datasetLabel)
									&& (!DatasetManagementAPI.isJDBCDataSet(dataset) || SqlUtils.isBigDataDialect(dataset.getDataSource().getHibDialectName()))) {
								filter = DataStore.DEFAULT_TABLE_NAME + "." + AbstractJDBCDataset.encapsulateColumnName(column, null) + "='" + value + "'";
							} else {
								filter = AbstractJDBCDataset.encapsulateColumnName(column, cacheDataSource) + "=('" + value + "')";
							}
							filtersMap.put(datasetLabel, filter);

							if (!selectionsMap.containsKey(datasetLabel)) {
								selectionsMap.put(datasetLabel, new HashMap<String, Set<String>>());
							}
							Map<String, Set<String>> selection = selectionsMap.get(datasetLabel);
							if (!selection.containsKey(column)) {
								selection.put(column, new HashSet<String>());
							}
							selection.get(column).add("('" + value + "')");
						}
					}
				}
			}

			AssociativeLogicManager manager = new AssociativeLogicManager(graph, datasetToAssociationToColumnMap, filtersMap, realtimeDatasets,
					datasetParameters, documents);
			manager.setUserProfile(getUserProfile());
			Map<EdgeGroup, Set<String>> egdegroupToValuesMap = manager.process();

			Map<String, Map<String, Set<String>>> selections = AssociationAnalyzer.getSelections(associationGroup, graph, egdegroupToValuesMap);

			for (String d : selectionsMap.keySet()) {
				if (!selections.containsKey(d)) {
					selections.put(d, new HashMap<String, Set<String>>());
				}
				selections.get(d).putAll(selectionsMap.get(d));
			}

			String stringFeed = JsonConverter.objectToJson(selections, Map.class);
			return stringFeed;
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = "An error occurred while getting associative selections";
			logger.error(errorMessage, e);
			throw new SpagoBIRestServiceException(errorMessage, buildLocaleFromSession(), e); // FIXME
		} finally {
			logger.debug("OUT");
		}
	}

	private void fixAssociationGroup(AssociationGroup associationGroup) {
		IDataSetDAO dataSetDAO;
		try {
			dataSetDAO = DAOFactory.getDataSetDAO();
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Unable to load DataSet DAO", e);
		}

		Map<String, IMetaData> dataSetLabelToMedaData = new HashMap<String, IMetaData>();
		for (Association association : associationGroup.getAssociations()) {
			if (association.getDescription().contains(".")) {
				for (Field field : association.getFields()) {
					String fieldName = field.getFieldName();
					if (fieldName.contains(":")) {
						String dataSetLabel = field.getDataSetLabel();

						IMetaData metadata = null;
						if (dataSetLabelToMedaData.containsKey(dataSetLabel)) {
							metadata = dataSetLabelToMedaData.get(dataSetLabel);
						} else {
							metadata = dataSetDAO.loadDataSetByLabel(dataSetLabel).getMetadata();
							dataSetLabelToMedaData.put(dataSetLabel, metadata);
						}

						for (int i = 0; i < metadata.getFieldCount(); i++) {
							IFieldMetaData fieldMeta = metadata.getFieldMeta(i);
							String alias = fieldMeta.getAlias();
							if (fieldMeta.getName().equals(fieldName)) {
								association.setDescription(association.getDescription().replace(dataSetLabel + "." + fieldName, dataSetLabel + "." + alias));
								field.setFieldName(alias);
								break;
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected List<FilterCriteria> getFilterCriteria(String datasetLabel, JSONObject selectionsObject, boolean isRealtime, Map<String, String> columnAliasToName)
			throws JSONException {
		List<FilterCriteria> filterCriterias = new ArrayList<FilterCriteria>();

		IDataSetDAO dsDAO = getDataSetDAO();

		IDataSet dataSet = dsDAO.loadDataSetByLabel(datasetLabel);

		if (selectionsObject.has(datasetLabel)) {
			boolean isAnEmptySelection = false;
			JSONObject datasetSelectionObject = selectionsObject.getJSONObject(datasetLabel);
			Iterator<String> it = datasetSelectionObject.keys();
			while (!isAnEmptySelection && it.hasNext()) {
				String columns = it.next();
				JSONArray values = datasetSelectionObject.getJSONArray(columns);
				String columnNames = replaceColumnAliasesWithNames(columns, columnAliasToName);
				if (values.length() > 0) {
					if (isRealtime && !dataSet.isFlatDataset() && !(dataSet.isPersisted() && !dataSet.isPersistedHDFS())) {
						String defaultTableNameDot = DataStore.DEFAULT_TABLE_NAME + ".";
						String[] columnsArray = columnNames.split(",");
						Operand leftOperand = new Operand(defaultTableNameDot + AbstractJDBCDataset.encapsulateColumnName(columnsArray[0], null));
						for (int i = 0; i < values.length(); i++) {
							String currentValues = values.getString(i);
							if (currentValues.startsWith("(") && currentValues.endsWith(")")) {
								currentValues = currentValues.substring(1, currentValues.length() - 1);
							}
							String[] valuesArray = currentValues.split(",");
							StringBuilder sb = new StringBuilder();
							if (valuesArray.length == 1) {
								String delim = valuesArray[0].startsWith("'") ? "" : "'";
								sb.append(delim);
								sb.append(valuesArray[0]);
								sb.append(delim);
							} else {
								sb.append(valuesArray[0]);
								for (int j = 1; j < valuesArray.length; j++) {
									sb.append(" AND ");
									sb.append(defaultTableNameDot);
									sb.append(columnsArray[j]);
									sb.append("=");
									sb.append(valuesArray[j]);
								}
							}
							Operand rightOperand = new Operand(sb.toString());
							FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "=", rightOperand);
							filterCriterias.add(filterCriteria);
						}
					} else {
						List<String> valuesList = new ArrayList<String>();
						for (int i = 0; i < values.length(); i++) {
							valuesList.add(values.getString(i));
						}
						Operand leftOperand = new Operand(columnNames);
						Operand rightOperand = new Operand(valuesList);
						FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "IN", rightOperand);
						filterCriterias.add(filterCriteria);
					}
				} else {
					isAnEmptySelection = true;
				}
			}
			if (isAnEmptySelection) {
				Operand leftOperand = new Operand("0");
				Operand rightOperand = new Operand("1");
				FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "=", rightOperand);
				filterCriterias.clear();
				filterCriterias.add(filterCriteria);
			}
		}

		return filterCriterias;
	}

	private String replaceColumnAliasesWithNames(String columns, Map<String, String> columnAliasToName) {
		if (columnAliasToName != null) {
			for (String alias : columnAliasToName.keySet()) {
				columns = columns.replace(alias, columnAliasToName.get(alias));
			}
		}
		return columns;
	}

	@POST
	@Path("/{label}/data")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDataStorePost(@PathParam("label") String label, @QueryParam("parameters") String parameters,
			@QueryParam("aggregations") String aggregations, @QueryParam("summaryRow") String summaryRow, String selections, @QueryParam("offset") int offset,
			@QueryParam("size") int fetchSize, @QueryParam("realtime") boolean isRealtime) {
		logger.debug("IN");
		try {
			return getDataStore(label, parameters, selections, aggregations, summaryRow, offset, fetchSize, isRealtime);
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/addDatasetInCache")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addDatasetInCache(@Context HttpServletRequest req) {
		logger.debug("IN");
		try {
			JSONArray requestBodyJSONArray = RestUtilities.readBodyAsJSONArray(req);
			for (int i = 0; i < requestBodyJSONArray.length(); i++) {
				JSONObject info = requestBodyJSONArray.getJSONObject(i);
				getDataStore(info.getString("datasetLabel"), info.getString("parameters"), null, info.getString("aggregation"), null, 0, 1,
						info.optBoolean("realtime"));

			}
			return Response.ok().build();
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/associations/autodetect")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Similarity> autodetect(@QueryParam("top") int top, @QueryParam("threshold") double threshold, @QueryParam("aggregate") boolean aggregate,
			@QueryParam("strategy") String strategy, @Context HttpServletRequest req) {
		logger.debug("IN");
		Set<Similarity> toReturn = new HashSet<>(0);
		try {
			JSONObject requestBodyJSONObject = RestUtilities.readBodyAsJSONObject(req);
			if (requestBodyJSONObject != null && requestBodyJSONObject.length() > 0) {

				top = top > 0 ? top : Integer.MAX_VALUE;
				threshold = (threshold >= 0 && threshold <= 1) ? threshold : Double.MIN_VALUE;

				List<String> dataSets = new ArrayList<>(requestBodyJSONObject.length());
				Map<String, Map<String, TLongHashSet>> dataSetDomainValues = new HashMap<>(requestBodyJSONObject.length());

				IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
				dataSetDAO.setUserProfile(getUserProfile());
				DatasetManagementAPI datasetManagementAPI = getDatasetManagementAPI();

				Iterator<String> labels = requestBodyJSONObject.keys();
				while (labels.hasNext()) {
					String label = labels.next();
					logger.debug("Getting dataSet with label [" + label + "]");
					IDataSet dataSet = dataSetDAO.loadDataSetByLabel(label);
					if (dataSet != null) {
						JSONObject parameters = requestBodyJSONObject.getJSONObject(label);
						Map<String, TLongHashSet> domainValues = datasetManagementAPI.readDomainValues(dataSet, DataSetUtilities.getParametersMap(parameters));
						if (domainValues != null) {
							dataSets.add(label);
							dataSetDomainValues.put(label, domainValues);
						}
					} else {
						throw new SpagoBIRuntimeException("Impossibile to load dataSet with label [" + label + "]");
					}
				}

				SimilarityEvaluator similarityEvaluator = new SimilarityEvaluator(SimilarityStrategyFactory.createStrategyInstance(strategy), top, threshold);
				toReturn = similarityEvaluator.evaluate(dataSets, dataSetDomainValues, aggregate);

			}
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

}