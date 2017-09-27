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

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
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

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.common.DataSetResourceAbstractResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.ConfigurationConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.associativity.IAssociativityManager;
import it.eng.spagobi.tools.dataset.associativity.strategy.AssociativeStrategyFactory;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.FilterCriteria;
import it.eng.spagobi.tools.dataset.cache.Operand;
import it.eng.spagobi.tools.dataset.cache.ProjectionCriteria;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroupJSONSerializer;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.tools.dataset.common.datawriter.IDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.ISbiDataSetDAO;
import it.eng.spagobi.tools.dataset.graph.AssociationAnalyzer;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.Selection;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicResult;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.dataset.persist.IPersistedManager;
import it.eng.spagobi.tools.dataset.persist.PersistedHDFSManager;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.assertion.UnreachableCodeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceParameterException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.sql.SqlUtils;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 *
 */
@Path("/2.0/datasets")

@ManageAuthorization
public class DataSetResource extends DataSetResourceAbstractResource {

	static protected Logger logger = Logger.getLogger(DataSetResource.class);

	static final private String DEFAULT_TABLE_NAME_DOT = DataStore.DEFAULT_TABLE_NAME + ".";

	private static final String DATE_TIME_FORMAT_MYSQL = CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT.replace("yyyy", "%Y").replace("MM", "%m")
			.replace("dd", "%d").replace("HH", "%H").replace("mm", "%i").replace("ss", "%s");
	private static final String DATE_TIME_FORMAT_SQL_STANDARD = CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT.replace("yyyy", "YYYY").replace("MM", "MM")
			.replace("dd", "DD").replace("HH", "HH24").replace("mm", "MI").replace("ss", "SS");
	private static final String DATE_TIME_FORMAT_SQLSERVER = "yyyyMMdd HH:mm:ss";

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSets(@QueryParam("typeDoc") String typeDoc, @QueryParam("callback") String callback) {
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
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getNotDerivedDataSets(@QueryParam("typeDoc") String typeDoc, @QueryParam("callback") String callback) {
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
			String error = "Error while looking for datasets";
			logger.error(error, e);
			throw new SpagoBIRuntimeException(error, e);
		}
		return dsDAO;
	}

	private IDataSourceDAO getDataSourceDAO() {
		IDataSourceDAO dataSourceDAO;
		try {
			dataSourceDAO = DAOFactory.getDataSourceDAO();
		} catch (EMFUserError e) {
			String error = "Error while looking for datasources";
			logger.error(error, e);
			throw new SpagoBIRuntimeException(error, e);
		}
		return dataSourceDAO;
	}

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
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response addDataSet(String body) {
		SbiDataSet sbiDataset = (SbiDataSet) JsonConverter.jsonToValidObject(body, SbiDataSet.class);

		sbiDataset.setId(new SbiDataSetId(null, 1, getUserProfile().getOrganization()));
		sbiDataset.setOwner((String) getUserProfile().getUserUniqueIdentifier());
		IDataSet dataset = DataSetFactory.toDataSet(sbiDataset);

		try {
			DAOFactory.getDataSetDAO().insertDataSet(dataset);

			if (dataset.isPersistedHDFS()) {
				IPersistedManager ptm = new PersistedHDFSManager(getUserProfile());
				ptm.persistDataSet(dataset);
			}

			if (dataset.isPersisted()) {
				IPersistedManager ptm = new PersistedTableManager(getUserProfile());
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
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
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
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDocumentSearchAndPaginate(@Context HttpServletRequest req, @QueryParam("Page") String pageStr,
			@QueryParam("ItemPerPage") String itemPerPageStr, @QueryParam("label") String search, @QueryParam("seeTechnical") Boolean seeTechnical,
			@QueryParam("ids") String ids) throws EMFUserError {

		ISbiDataSetDAO dao = DAOFactory.getSbiDataSetDAO();
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		// TODO check if profile is null
		dao.setUserProfile(profile);

		Integer page = getNumberOrNull(pageStr);
		Integer item_per_page = getNumberOrNull(itemPerPageStr);
		search = search != null ? search : "";

		Integer[] idArray = getIdsAsIntegers(ids);

		try {
			List<SbiDataSet> dataset = null;
			if (UserUtilities.isAdministrator(getUserProfile())) {
				dataset = dao.loadPaginatedSearchSbiDataSet(search, page, item_per_page, null, null, idArray);
			} else {
				dataset = dao.loadPaginatedSearchSbiDataSet(search, page, item_per_page, getUserProfile(), seeTechnical, idArray);
			}

			JSONObject jo = new JSONObject();
			JSONArray ja = new JSONArray();
			for (SbiDataSet ds : dataset) {
				ja.put(JSON.parse(JsonConverter.objectToJson(ds, SbiDataSet.class)));
			}
			jo.put("item", ja);
			jo.put("itemCount", dao.countSbiDataSet(search, idArray));

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
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getAssociativeSelections(@QueryParam("associationGroup") String associationGroupString, @QueryParam("selections") String selectionsString,
			@QueryParam("datasets") String datasetsString, @QueryParam("nearRealtime") String nearRealtimeDatasetsString) {
		logger.debug("IN");

		try {
			IDataSetDAO dataSetDAO = getDataSetDAO();
			dataSetDAO.setUserProfile(getUserProfile());

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
			Set<String> documents = new HashSet<>();
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
				for (int associationIndex = associationsWithoutParams.length() - 1; associationIndex >= 0; associationIndex--) {
					JSONObject association = associationsWithoutParams.getJSONObject(associationIndex);
					JSONArray fields = association.getJSONArray("fields");
					for (int fieldIndex = fields.length() - 1; fieldIndex >= 0; fieldIndex--) {
						JSONObject field = fields.getJSONObject(fieldIndex);
						String column = field.getString("column");
						String store = field.getString("store");
						String type = field.optString("type");
						if (("document".equalsIgnoreCase(type)) || (column.startsWith("$P{") && column.endsWith("}"))
								|| dataSetDAO.loadDataSetByLabel(store).isRealtime()) {
							fields.remove(fieldIndex);
						}
					}
				}
			}
			AssociationGroup associationGroupWithoutParams = serializer.deserialize(associationGroupObjectWithoutParams);
			fixAssociationGroup(associationGroupWithoutParams);

			// parse dataset parameters
			Map<String, Map<String, String>> datasetParameters = new HashMap<>();
			if (datasetsString != null && !datasetsString.isEmpty()) {
				JSONObject datasetsObject = new JSONObject(datasetsString);
				Iterator<String> datasetsIterator = datasetsObject.keys();
				while (datasetsIterator.hasNext()) {
					String datasetLabel = datasetsIterator.next();

					Map<String, String> parameters = new HashMap<>();
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

			// parse near realtime datasets
			Set<String> nearRealtimeDatasets = new HashSet<>();
			if (nearRealtimeDatasetsString != null && !nearRealtimeDatasetsString.isEmpty()) {
				JSONArray jsonArray = new JSONArray(nearRealtimeDatasetsString);
				for (int i = 0; i < jsonArray.length(); i++) {
					nearRealtimeDatasets.add(jsonArray.getString(i));
				}
			}

			AssociationAnalyzer analyzerWithoutParams = new AssociationAnalyzer(associationGroupWithoutParams.getAssociations());
			analyzerWithoutParams.process();
			Map<String, Map<String, String>> datasetToAssociationToColumnMap = analyzerWithoutParams.getDatasetToAssociationToColumnMap();

			AssociationAnalyzer analyzer = new AssociationAnalyzer(associationGroup.getAssociations());
			analyzer.process();
			Pseudograph<String, LabeledEdge<String>> graph = analyzer.getGraph();

			IDataSource cacheDataSource = SpagoBICacheConfiguration.getInstance().getCacheDataSource();

			// get datasets from selections
			List<Selection> filters = new ArrayList<>();
			Map<String, Map<String, Set<String>>> selectionsMap = new HashMap<>();

			Iterator<String> it = selectionsObject.keys();
			while (it.hasNext()) {
				String datasetDotColumn = it.next();
				Assert.assertTrue(datasetDotColumn.indexOf(".") >= 0, "Data not compliant with format <DATASET_LABEL>.<COLUMN> [" + datasetDotColumn + "]");
				String[] tmpDatasetAndColumn = datasetDotColumn.split("\\.");
				Assert.assertTrue(tmpDatasetAndColumn.length == 2, "Impossible to get both dataset label and column");

				String datasetLabel = tmpDatasetAndColumn[0];
				String column = SqlUtils.unQuote(tmpDatasetAndColumn[1]);

				Assert.assertNotNull(datasetLabel, "A dataset label in selections is null");
				Assert.assertTrue(!datasetLabel.isEmpty(), "A dataset label in selections is empty");
				Assert.assertNotNull(column, "A column for dataset " + datasetLabel + "  in selections is null");
				Assert.assertTrue(!column.isEmpty(), "A column for dataset " + datasetLabel + " in selections is empty");

				IDataSet dataset = getDataSetDAO().loadDataSetByLabel(datasetLabel);
				boolean isNearRealtime = nearRealtimeDatasets.contains(datasetLabel);
				IDataSource dataSource = getDataSource(dataset, isNearRealtime);
				boolean isDateColumn = isDateColumn(column, dataset);

				String values = null;
				String valuesForQuery = null;
				Object object = selectionsObject.getJSONArray(datasetDotColumn).get(0);
				if (object instanceof JSONArray) {
					if (isDateColumn) {
						JSONArray jsonArray = (JSONArray) object;
						List<String> valueList = new ArrayList<>();
						List<String> valueForQueryList = new ArrayList<>();
						for (int i = 0; i < jsonArray.length(); i++) {
							String value = convertDateString(jsonArray.getString(i), CockpitJSONDataWriter.DATE_TIME_FORMAT,
									CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT);
							valueForQueryList.add(getDateForQuery(value, dataSource));
							valueList.add("'" + value + "'");
						}
						values = StringUtils.join(valueList, ",");
						valuesForQuery = StringUtils.join(valueForQueryList, ",");
					} else {
						values = ("\"" + ((JSONArray) object).join("\",\"") + "\"").replace("\"\"", "'").replace("\"", "'");
						valuesForQuery = values;
					}
				} else {
					if (isDateColumn) {
						values = convertDateString(object.toString(), CockpitJSONDataWriter.DATE_TIME_FORMAT, CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT);
						valuesForQuery = getDateForQuery(values, dataSource);
						values = "'" + values + "'";
					} else {
						values = "'" + object.toString() + "'";
						valuesForQuery = values;
					}
				}

				filters.add(new Selection(datasetLabel, getFilter(dataset, isNearRealtime, column, valuesForQuery)));

				if (!selectionsMap.containsKey(datasetLabel)) {
					selectionsMap.put(datasetLabel, new HashMap<String, Set<String>>());
				}
				Map<String, Set<String>> selection = selectionsMap.get(datasetLabel);
				if (!selection.containsKey(column)) {
					selection.put(column, new HashSet<String>());
				}
				selection.get(column).add("(" + values + ")");
			}

			logger.debug("Filter list: " + filters);

			String strategy = SingletonConfig.getInstance().getConfigValue(ConfigurationConstants.SPAGOBI_DATASET_ASSOCIATIVE_LOGIC_STRATEGY);
			Config config = AssociativeLogicUtils.buildConfig(strategy, graph, datasetToAssociationToColumnMap, filters, nearRealtimeDatasets,
					datasetParameters, documents);

			IAssociativityManager manager = AssociativeStrategyFactory.createStrategyInstance(config, getUserProfile());
			AssociativeLogicResult result = manager.process();

			Map<String, Map<String, Set<String>>> selections = AssociationAnalyzer.getSelections(associationGroup, graph, result);

			for (String d : selectionsMap.keySet()) {
				if (!selections.containsKey(d)) {
					selections.put(d, new HashMap<String, Set<String>>());
				}
				selections.get(d).putAll(selectionsMap.get(d));
			}

			String stringFeed = JsonConverter.objectToJson(selections, Map.class);
			return stringFeed;
		} catch (Exception e) {
			String errorMessage = "An error occurred while getting associative selections";
			logger.error(errorMessage, e);
			throw new SpagoBIRestServiceException(errorMessage, buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	private String getFilter(IDataSet dataset, boolean isNearRealtime, String column, String values) {
		IDataSource dataSource = getDataSource(dataset, isNearRealtime);
		String tablePrefix = getTablePrefix(dataset, isNearRealtime);
		DatasetEvaluationStrategy strategy = getDatasetEvaluationStrategy(dataset, isNearRealtime);

		if (DatasetEvaluationStrategy.NEAR_REALTIME.equals(strategy) || SqlUtils.hasSqlServerDialect(dataSource)) {
			return getOrFilterString(column, values, dataSource, tablePrefix);
		} else {
			return getInFilterString(column, values, dataSource, tablePrefix);
		}
	}

	private String getOrFilterString(String column, String values, IDataSource dataSource, String tablePrefix) {
		String encapsulateColumnName = tablePrefix + AbstractJDBCDataset.encapsulateColumnName(column, dataSource);
		String[] singleValues = values.split(",");
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < singleValues.length; i++) {
			if (i > 0) {
				sb.append(" OR ");
			}
			sb.append(encapsulateColumnName);
			sb.append("=");
			sb.append(singleValues[i]);
		}

		return sb.toString();
	}

	private String getInFilterString(String column, String values, IDataSource dataSource, String tablePrefix) {
		StringBuilder sb = new StringBuilder();

		sb.append(tablePrefix);
		sb.append(AbstractJDBCDataset.encapsulateColumnName(column, dataSource));
		sb.append(" IN (");
		sb.append(values);
		sb.append(")");

		return sb.toString();
	}

	private String getTablePrefix(IDataSet dataset, boolean isNearRealtime) {
		DatasetEvaluationStrategy datasetEvaluationStrategy = getDatasetEvaluationStrategy(dataset, isNearRealtime);
		if (datasetEvaluationStrategy == DatasetEvaluationStrategy.NEAR_REALTIME) {
			return DEFAULT_TABLE_NAME_DOT;
		} else {
			return "";
		}
	}

	private enum DatasetEvaluationStrategy {
		PERSISTED, FLAT, JDBC, NEAR_REALTIME, CACHED
	}

	private DatasetEvaluationStrategy getDatasetEvaluationStrategy(IDataSet dataSet, boolean isNearRealtime) {
		DatasetEvaluationStrategy result;

		if (dataSet.isPersisted()) {
			result = DatasetEvaluationStrategy.PERSISTED;
		} else if (dataSet.isFlatDataset()) {
			result = DatasetEvaluationStrategy.FLAT;
		} else {
			boolean isJDBCDataSet = DatasetManagementAPI.isJDBCDataSet(dataSet);
			boolean isBigDataDialect = SqlUtils.isBigDataDialect(dataSet.getDataSource() != null ? dataSet.getDataSource().getHibDialectName() : "");
			if (isNearRealtime && isJDBCDataSet && !isBigDataDialect && !dataSet.hasDataStoreTransformer()) {
				result = DatasetEvaluationStrategy.JDBC;
			} else if (isNearRealtime) {
				result = DatasetEvaluationStrategy.NEAR_REALTIME;
			} else {
				result = DatasetEvaluationStrategy.CACHED;
			}
		}

		return result;
	}

	private void fixAssociationGroup(AssociationGroup associationGroup) {
		IDataSetDAO dataSetDAO = getDataSetDAO();

		Map<String, IMetaData> dataSetLabelToMedaData = new HashMap<>();
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
	protected List<FilterCriteria> getFilterCriteria(String datasetLabel, JSONObject selectionsObject, boolean isNearRealtime,
			Map<String, String> columnAliasToColumnName) throws JSONException {
		List<FilterCriteria> filterCriterias = new ArrayList<>();

		if (selectionsObject.has(datasetLabel)) {
			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(datasetLabel);
			boolean isAnEmptySelection = false;

			JSONObject datasetSelectionObject = selectionsObject.getJSONObject(datasetLabel);
			Iterator<String> it = datasetSelectionObject.keys();
			while (!isAnEmptySelection && it.hasNext()) {
				String columns = it.next();

				// check two case: if click selection contained wil be JSON array, and operator is =, if Json Object is from filter
				Object filterObject = datasetSelectionObject.get(columns);
				String filterOperator = null;
				JSONArray values = null;

				if (filterObject instanceof JSONArray) {
					logger.debug("coming from click");
					filterOperator = "=";
					values = (JSONArray) filterObject;
				} else if (filterObject instanceof JSONObject) {
					logger.debug("coming from filters ");
					JSONObject fiulterJsonObject = (JSONObject) filterObject;
					filterOperator = fiulterJsonObject.opt("filterOperator").toString();
					values = fiulterJsonObject.getJSONArray("filterVals");
				} else {
					throw new SpagoBIRuntimeException("Not recognised filter object " + filterObject, null);
				}

				if (values.length() > 0 || getDatasetManagementAPI().isZeroOperandsOperator(filterOperator)) {
					List<String> columnsList = new ArrayList<>();
					String columnNames = fixColumnAliasesAndNames(columns, columnAliasToColumnName);
					columnsList.addAll(Arrays.asList(columnNames.split("\\s*,\\s*"))); // trim spaces while splitting

					for (int i = 0; i < columnsList.size(); i++) {
						String column = columnsList.get(i);
						if (column.contains(":")) {
							columnsList.set(i, getDatasetManagementAPI().getQbeDataSetColumn(dataSet, column));
						}
					}

					IDataSource dataSource = getDataSource(dataSet, isNearRealtime);

					boolean isJDBCDataSet = DatasetManagementAPI.isJDBCDataSet(dataSet);
					String dialect = dataSource != null ? dataSource.getHibDialectName() : "";
					boolean isBigDataDialect = SqlUtils.isBigDataDialect(dialect);
					boolean isSqlServerDialect = dialect.contains("sqlserver");

					List<String> dateColumnNamesList = getDateColumnNamesListRaw(dataSet, dataSource); // with aliases aposthrophe
					// TODO

					DatasetEvaluationStrategy strategy = getDatasetEvaluationStrategy(dataSet, isNearRealtime);
					if (strategy == DatasetEvaluationStrategy.NEAR_REALTIME) {
						for (int i = 0; i < columnsList.size(); i++) {
							columnsList.set(i, DEFAULT_TABLE_NAME_DOT + AbstractJDBCDataset.encapsulateColumnName(columnsList.get(i), null));
						}
						String joinedColumns = StringUtils.join(columnsList, ",");
						Operand leftOperand = new Operand(joinedColumns);

						StringBuilder valuesSB = new StringBuilder();
						String openingBracket = columnsList.size() > 1 ? "(" : "";
						String closingBracket = columnsList.size() > 1 ? ")" : "";

						List<String> distinctValues = new ArrayList<>();
						for (int i = 0; i < values.length(); i++) {
							String value = values.getString(i);
							distinctValues.addAll(Arrays.asList(getDistinctValues(value)));
						}
						for (int i = 0; i < distinctValues.size(); i++) {
							String value = distinctValues.get(i);
							String column = columnsList.get(i % columnsList.size());
							if (i % columnsList.size() == 0) { // 1st item of tuple of values
								if (i >= columnsList.size()) { // starting from 2nd tuple of values
									valuesSB.append(" OR ");
									valuesSB.append(openingBracket);
								}
							} else {
								valuesSB.append(" AND "); // starting from 2nd item of tuple of values
							}
							if (i > 0) {
								valuesSB.append(column);
								valuesSB.append("=");
							}
							valuesSB.append(getValueForQuery(value, dateColumnNamesList.contains(column), dataSource));
							if (i % columnsList.size() == columnsList.size() - 1) { // last item of tuple of values
								valuesSB.append(closingBracket);
							}
						}
						Operand rightOperand = new Operand(valuesSB.toString());

						FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "=", rightOperand);
						filterCriterias.add(filterCriteria);

					}
					// else if (isSqlServerDialect) {
					// for (int i = 0; i < columnsList.size(); i++) {
					// columnsList.set(i, AbstractJDBCDataset.encapsulateColumnName(columnsList.get(i), dataSource));
					// }
					//
					// String openingBracket = columnsList.size() > 1 ? "(" : "";
					// String closingBracket = columnsList.size() > 1 ? ")" : "";
					//
					// Operand leftOperand = new Operand(openingBracket + columnsList.get(0));
					//
					// StringBuilder valuesSB = new StringBuilder();
					//
					// List<String> distinctValues = new ArrayList<>();
					// for (int i = 0; i < values.length(); i++) {
					// String value = values.getString(i);
					// distinctValues.addAll(Arrays.asList(getDistinctValues(value)));
					// }
					//
					// for (int i = 0; i < distinctValues.size(); i++) {
					// String value = distinctValues.get(i);
					// String column = columnsList.get(i % columnsList.size());
					// if (i % columnsList.size() == 0) { // 1st item of tuple of values
					// if (i >= columnsList.size()) { // starting from 2nd tuple of values
					// valuesSB.append(" OR ");
					// valuesSB.append(openingBracket);
					// }
					// } else {
					// valuesSB.append(" AND "); // starting from 2nd item of tuple of values
					// }
					// if (i > 0) {
					// valuesSB.append(column);
					// valuesSB.append("=");
					// }
					// valuesSB.append(getValueForQuery(value, dateColumnNamesList.contains(column), dataSource));
					// if (i % columnsList.size() == columnsList.size() - 1) { // last item of tuple of values
					// valuesSB.append(closingBracket);
					// }
					// }
					// Operand rightOperand = new Operand(valuesSB.toString());
					//
					// FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "=", rightOperand);
					// filterCriterias.add(filterCriteria);
					//
					// }
					else {
						Operand leftOperand = new Operand(StringUtils.join(columnsList, ","));

						List<String> valuesList = new ArrayList<>();
						for (int i = 0; i < values.length(); i++) {
							String[] valuesArray = getDistinctValues(values.getString(i));
							for (int j = 0; j < valuesArray.length; j++) {
								String column = columnsList.get(j % columnsList.size());
								valuesList.add(getValueForQuery(valuesArray[j], dateColumnNamesList.contains(column), dataSource));
							}
						}

						// case for all operators
						// =, < , >, <= , >= , like ,is null , is not null ,min ,max ,range
						FilterCriteria filterCriteria = null;

						List<String> oneOperandOperators = Arrays.asList("=", "!=", "<", ">", "<=", ">=", "like");
						List<String> twoOperandOperators = Arrays.asList("range");
						List<String> markupOperandOperators = Arrays.asList("max", "min");
						List<String> zeroOperandOperators = Arrays.asList("is null", "is not null");

						if (oneOperandOperators.contains(filterOperator)) {
							// if val not found do not put criteria, it could be already handled by associations
							String val = "''";
							if (valuesList.size() >= 1) {
								val = valuesList.get(0);
							}

							// if operator is like add %%
							if (filterOperator.equals("like") && !val.equals("''")) {
								if (val.startsWith("'") && val.endsWith("'")) {
									val = "'%" + val.substring(1, val.length() - 1) + "%'";
								} else {
									val = "%" + val + "%";
								}
							}

							Operand rightOperand = new Operand(val);
							filterCriteria = new FilterCriteria(leftOperand, filterOperator, rightOperand);
							// } else {
							// logger.warn("No value found for criteria on column " + columnNames + " with operator " + filterOperator);
							// }
						} else if (twoOperandOperators.contains(filterOperator)) {
							Operand rightOperand = null;
							String val1 = "''";
							String val2 = "''";
							if (valuesList.size() >= 2) {

								val1 = valuesList.get(0);
								val2 = valuesList.get(1);
							}
							Object valueToInsert = null;
							if (filterOperator.equalsIgnoreCase("range")) {
								filterOperator = "BETWEEN";
								valueToInsert = " " + val1 + " AND " + val2;
							} else {
								valueToInsert = new ArrayList<String>();
								((List<String>) valueToInsert).add(val1);
								((List<String>) valueToInsert).add(val2);
							}

							rightOperand = new Operand(valueToInsert);

							filterCriteria = new FilterCriteria(leftOperand, filterOperator, rightOperand);
							// } else {
							// logger.warn("No value found for criteria on column " + columnNames + " with operator " + filterOperator);
							// }
						} else if (markupOperandOperators.contains(filterOperator)) {
							Operand rightOperand = new Operand(new ArrayList<String>());
							filterCriteria = new FilterCriteria(leftOperand, filterOperator, rightOperand);
						} else if (zeroOperandOperators.contains(filterOperator)) {
							filterCriteria = new FilterCriteria(leftOperand, filterOperator, null);

						}

						// Operand rightOperand = new Operand(valuesList);
						// FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "IN", rightOperand);
						if (filterCriteria != null) {
							filterCriterias.add(filterCriteria);
						}
					}
				} else {
					isAnEmptySelection = true;
				}

			}

			if (isAnEmptySelection) {
				filterCriterias.clear();
				filterCriterias.add(new FilterCriteria(new Operand("0"), "=", new Operand("1")));
			}
		}

		return filterCriterias;
	}

	private List<String> getDateColumnNamesList(IDataSet dataSet, IDataSource dataSource) {
		List<String> dateColumnNamesList = new ArrayList<>();
		for (int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
			IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
			if (Date.class.isAssignableFrom(fieldMeta.getType())) {
				dateColumnNamesList.add(AbstractJDBCDataset.encapsulateColumnName(fieldMeta.getName(), dataSource));
			}
		}
		return dateColumnNamesList;
	}

	private List<String> getDateColumnNamesListRaw(IDataSet dataSet, IDataSource dataSource) {
		List<String> dateColumnNamesList = new ArrayList<>();
		for (int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
			IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
			if (Date.class.isAssignableFrom(fieldMeta.getType())) {
				dateColumnNamesList.add(fieldMeta.getName());
			}
		}
		return dateColumnNamesList;
	}

	private boolean isDateColumn(String columnName, IDataSet dataSet) {
		for (int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
			IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
			if (fieldMeta.getName().equals(columnName) && Date.class.isAssignableFrom(fieldMeta.getType())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected List<FilterCriteria> getLikeFilterCriteria(String datasetLabel, JSONObject likeSelectionsObject, boolean isNearRealtime,
			Map<String, String> columnAliasToColumnName, List<ProjectionCriteria> projectionCriteria, boolean getAttributes) throws JSONException {
		List<FilterCriteria> likeFilterCriteria = new ArrayList<>();

		if (likeSelectionsObject.has(datasetLabel)) {
			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(datasetLabel);
			boolean isAnEmptySelection = false;

			JSONObject datasetSelectionObject = likeSelectionsObject.getJSONObject(datasetLabel);
			Iterator<String> it = datasetSelectionObject.keys();
			while (!isAnEmptySelection && it.hasNext()) {
				String columns = it.next();
				String value = datasetSelectionObject.getString(columns);

				if (value != null && !value.isEmpty()) {
					List<String> columnsList = new ArrayList<>();
					String columnNames = fixColumnAliasesAndNames(columns, columnAliasToColumnName);
					columnsList.addAll(Arrays.asList(columnNames.split("\\s*,\\s*"))); // trim spaces while splitting

					List<String> attributesOrMeasures = getAttributesOrMeasures(columnsList, dataSet, projectionCriteria, isNearRealtime, getAttributes);
					if (!attributesOrMeasures.isEmpty()) {
						Operand leftOperand = null;
						StringBuilder rightOperandSB = new StringBuilder();
						for (String attributeOrMeasure : attributesOrMeasures) {
							if (leftOperand == null) {
								leftOperand = new Operand(attributeOrMeasure);

								rightOperandSB.append("'%");
								rightOperandSB.append(value);
								rightOperandSB.append("%'");
							} else {
								rightOperandSB.append(" OR ");
								rightOperandSB.append(attributeOrMeasure);
								rightOperandSB.append(" LIKE '%");
								rightOperandSB.append(value);
								rightOperandSB.append("%'");
							}
						}
						Operand rightOperand = new Operand(rightOperandSB.toString());
						FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "LIKE", rightOperand);
						likeFilterCriteria.add(filterCriteria);
					}
				} else {
					isAnEmptySelection = true;
				}
			}

			if (isAnEmptySelection) {
				likeFilterCriteria.clear();
				likeFilterCriteria.add(new FilterCriteria(new Operand("0"), "=", new Operand("1")));
			}
		}

		return likeFilterCriteria;
	}

	private List<String> getAttributesOrMeasures(List<String> columnNames, IDataSet dataSet, List<ProjectionCriteria> projectionCriteria,
			boolean isNearRealtime, boolean getAttributes) {
		List<String> attributesOrMeasures = new ArrayList<>();

		String defaultTableNameDot = isNearRealtime ? DEFAULT_TABLE_NAME_DOT : "";
		String datasetLabel = dataSet.getLabel();

		IDataSource dataSource = getDataSource(dataSet, isNearRealtime);

		for (String columnName : columnNames) {
			for (ProjectionCriteria projection : projectionCriteria) {
				if (projection.getDataset().equals(datasetLabel) && projection.getColumnName().equals(columnName)) {
					IAggregationFunction aggregationFunction = AggregationFunctions.get(projection.getAggregateFunction());
					boolean isAttribute = aggregationFunction == null || aggregationFunction.equals(AggregationFunctions.NONE_FUNCTION);
					if (isAttribute == getAttributes) {
						if (columnName.contains(":")) {
							columnName = getDatasetManagementAPI().getQbeDataSetColumn(dataSet, columnName);
						}
						String encapsulatedColumnName = AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);
						if (isAttribute) {
							attributesOrMeasures.add(defaultTableNameDot + encapsulatedColumnName);
						} else {
							attributesOrMeasures.add(defaultTableNameDot + aggregationFunction.apply(encapsulatedColumnName));
						}
					}
					break;
				}
			}
		}

		return attributesOrMeasures;
	}

	private IDataSource getDataSource(IDataSet dataSet, boolean isNearRealTime) {
		DatasetEvaluationStrategy strategy = getDatasetEvaluationStrategy(dataSet, isNearRealTime);
		IDataSource dataSource = null;

		if (strategy == DatasetEvaluationStrategy.PERSISTED) {
			dataSource = dataSet.getDataSourceForWriting();
		} else if (strategy == DatasetEvaluationStrategy.FLAT || strategy == DatasetEvaluationStrategy.JDBC) {
			try {
				dataSource = dataSet.getDataSource();
			} catch (UnreachableCodeException e) {
			}
		} else if (strategy == DatasetEvaluationStrategy.NEAR_REALTIME) {
			dataSource = null;
		} else {
			dataSource = SpagoBICacheConfiguration.getInstance().getCacheDataSource();
		}

		return dataSource;
	}

	private String[] getDistinctValues(String values) {
		ArrayList<String> arrayList = new ArrayList<>();
		// get values between "'"
		int start = values.indexOf("'");
		while (start > -1) {
			int end = values.indexOf("'", start + 1);
			arrayList.add(values.substring(start + 1, end));
			values = values.substring(end + 1);
			start = values.indexOf("'");
		}
		return arrayList.toArray(new String[0]);
	}

	private String getValueForQuery(String value, boolean isDate, IDataSource dataSource) {
		if (isDate) {
			return getDateForQuery(value, dataSource);
		} else {
			if (value.startsWith("'") && value.endsWith("'")) {
				return value;
			} else {
				return "'" + value + "'";
			}
		}
	}

	private String getDateForQuery(String dateStringToConvert, IDataSource dataSource) {
		String properDateString = dateStringToConvert;

		if (dataSource != null && dateStringToConvert != null) {
			String actualDialect = dataSource.getHibDialectClass();

			if (SqlUtils.DIALECT_MYSQL.equalsIgnoreCase(actualDialect)) {
				properDateString = "STR_TO_DATE('" + dateStringToConvert + "', '" + DATE_TIME_FORMAT_MYSQL + "')";
			} else if (SqlUtils.DIALECT_POSTGRES.equalsIgnoreCase(actualDialect) || SqlUtils.DIALECT_ORACLE.equalsIgnoreCase(actualDialect)
					|| SqlUtils.DIALECT_ORACLE9i10g.equalsIgnoreCase(actualDialect) || SqlUtils.DIALECT_HSQL.equalsIgnoreCase(actualDialect)
					|| SqlUtils.DIALECT_TERADATA.equalsIgnoreCase(actualDialect)) {
				properDateString = "TO_DATE('" + dateStringToConvert + "','" + DATE_TIME_FORMAT_SQL_STANDARD + "')";
			} else if (SqlUtils.DIALECT_SQLSERVER.equalsIgnoreCase(actualDialect)) {
				properDateString = "'" + convertDateString(dateStringToConvert, CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT, DATE_TIME_FORMAT_SQLSERVER) + "'";
			} else if (SqlUtils.DIALECT_INGRES.equalsIgnoreCase(actualDialect)) {
				properDateString = "DATE('" + dateStringToConvert + "')";
			}
		}

		return properDateString;
	}

	private String convertDateString(String dateString, String srcFormatString, String dstFormatString) {
		try {
			SimpleDateFormat srcFormat = new SimpleDateFormat(srcFormatString);
			Date date = srcFormat.parse(dateString);
			SimpleDateFormat dstFormat = new SimpleDateFormat(dstFormatString);
			return dstFormat.format(date);
		} catch (ParseException e) {
			String message = "Unable to parse date [" + dateString + "] with format [" + srcFormatString + "]";
			logger.error(message, e);
			throw new SpagoBIRuntimeException(message, e);
		}
	}

	private String fixColumnAliasesAndNames(String columns, Map<String, String> columnAliasToName) {
		if (columnAliasToName != null) {
			String[] columnsSplitted = columns.split("\\s*,\\s*");
			Set<String> aliases = columnAliasToName.keySet();

			for (int i = 0; i < columnsSplitted.length; i++) {
				String column = columnsSplitted[i].trim();
				if (aliases.contains(column)) {
					columnsSplitted[i] = columnAliasToName.get(column);
				}
			}
			return StringUtils.join(columnsSplitted, ",");
		} else {
			return columns;
		}
	}

	@POST
	@Path("/{label}/data")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataStorePost(@PathParam("label") String label, @QueryParam("parameters") String parameters, String selections,
			@QueryParam("likeSelections") String likeSelections, @DefaultValue("-1") @QueryParam("limit") int maxRowCount,
			@QueryParam("aggregations") String aggregations, @QueryParam("summaryRow") String summaryRow, @DefaultValue("-1") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("size") int fetchSize, @QueryParam("nearRealtime") boolean isNearRealtime) {
		logger.debug("IN");
		try {
			return getDataStore(label, parameters, selections, likeSelections, maxRowCount, aggregations, summaryRow, offset, fetchSize, isNearRealtime);
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/addDatasetInCache")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response addDatasetInCache(@Context HttpServletRequest req) {
		logger.debug("IN");
		try {
			JSONArray requestBodyJSONArray = RestUtilities.readBodyAsJSONArray(req);
			for (int i = 0; i < requestBodyJSONArray.length(); i++) {
				JSONObject info = requestBodyJSONArray.getJSONObject(i);
				getDataStore(info.getString("datasetLabel"), info.getString("parameters"), null, null, -1, info.getString("aggregation"), null, 0, 1,
						info.optBoolean("nearRealtime"));
			}
			return Response.ok().build();
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	protected IDataWriter getDataSetWriter() throws JSONException {
		CockpitJSONDataWriter dataWriter = new CockpitJSONDataWriter(getDataSetWriterProperties());
		dataWriter.setLocale(buildLocaleFromSession());
		return dataWriter;
	}
}