package it.eng.spagobi.engines.qbe.api;

import it.eng.qbe.dataset.FederatedDataSet;
import it.eng.qbe.dataset.QbeDataSet;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.IQueryField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.TimeAggregationHandler;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.qbe.statement.graph.GraphManager;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;
import it.eng.qbe.statement.hibernate.HQLDataSet;
import it.eng.qbe.statement.jpa.JPQLDataSet;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Path("/qbequery")
@ManageAuthorization
public class QbeQueryResource extends AbstractQbeEngineResource {
	
	public static transient Logger logger = Logger.getLogger(QbeQueryResource.class);
	public static transient Logger auditlogger = Logger.getLogger("audit.query");
	protected boolean handleTimeFilter = true;

	/*
	 * @POST
	 *
	 * @Path("/setQueryCatalog")
	 *
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * setQueryCatalog(@QueryParam("currentQueryId") String currentQueryId,
	 *
	 * @javax.ws.rs.core.Context HttpServletRequest req) {
	 *
	 * Monitor totalTimeMonitor = null; Monitor errorHitsMonitor = null;
	 *
	 * JSONObject jsonEncodedReq = null; JSONArray catalogue; JSONObject
	 * queryJSON; Query query; JSONArray queries; QueryGraph oldQueryGraph =
	 * null; String roleSelectionFromTheSavedQuery = null; boolean
	 * isDierctlyExecutable = false; QueryGraph queryGraph = null; // the query
	 * graph (the graph that // involves all the entities of the // query)
	 * String ambiguousWarinig = null; boolean forceReturnGraph = false;
	 *
	 * logger.debug("IN");
	 *
	 * try {
	 *
	 * totalTimeMonitor =
	 * MonitorFactory.start("QbeEngine.setCatalogueAction.totalTime");
	 *
	 * // get current query and all the linked objects
	 *
	 * if (query == null) { // the qbe is new query =
	 * this.getEngineInstance().getQueryCatalogue().getFirstQuery();
	 * oldQueryGraph = query.getQueryGraph(); roleSelectionFromTheSavedQuery =
	 * query.getRelationsRoles();
	 * logger.debug("The query is already defined in the catalogue"); if
	 * (roleSelectionFromTheSavedQuery != null) {
	 * logger.debug("The previous roleSelection is " +
	 * roleSelectionFromTheSavedQuery); } if (oldQueryGraph != null) {
	 * logger.debug("The previous oldQueryGraph is " + oldQueryGraph); } } if
	 * (req != null) { jsonEncodedReq = RestUtilities.readBodyAsJSONObject(req);
	 * } catalogue = jsonEncodedReq.getJSONArray("catalogue"); if (catalogue ==
	 * null) { catalogue = jsonEncodedReq.getJSONArray("qbeJSONQuery");
	 * JSONObject jo = new JSONObject(catalogue); jo =
	 * jo.getJSONObject("catalogue"); queries = jo.getJSONArray("queries"); }
	 * else { queries = new JSONArray(catalogue.toString()); } String
	 * ambiguousFieldsPaths = jsonEncodedReq.getString("ambiguousFieldsPaths");
	 * String ambiguousRoles = jsonEncodedReq.getString("ambiguousRoles");
	 *
	 * logger.debug("catalogue" + " = [" + catalogue + "]");
	 *
	 * try {
	 *
	 * for (int i = 0; i < queries.length(); i++) { queryJSON =
	 * queries.getJSONObject(i); query = deserializeQuery(queryJSON);
	 * getEngineInstance().getQueryCatalogue().addQuery(query);
	 * getEngineInstance().resetActiveQuery();
	 *
	 * } } catch (SerializationException e) { String message =
	 * "Impossible to deserialize query"; throw new
	 * SpagoBIEngineServiceException("DESERIALIZATING QUERY", message, e);
	 *
	 * }
	 *
	 * UserProfile userProfile = (UserProfile)
	 * getEnv().get(EngineConstants.ENV_USER_PROFILE);
	 *
	 * // we create a new query adding filters defined by profile // attributes
	 * IModelAccessModality accessModality =
	 * this.getEngineInstance().getDataSource().getModelAccessModality();
	 *
	 * Query filteredQuery = accessModality.getFilteredStatement(query,
	 * this.getEngineInstance().getDataSource(),
	 * userProfile.getUserAttributes());
	 *
	 * // loading the ambiguous fields Set<ModelFieldPaths> ambiguousFields =
	 * new HashSet<ModelFieldPaths>(); Map<IModelField, Set<IQueryField>>
	 * modelFieldsMap =
	 * filteredQuery.getQueryFields(getEngineInstance().getDataSource());
	 * Set<IModelField> modelFields = modelFieldsMap.keySet(); Set<IModelEntity>
	 * modelEntities = Query.getQueryEntities(modelFields);
	 *
	 * Map<String, Object> pathFiltersMap = new HashMap<String, Object>();
	 * pathFiltersMap.put(CubeFilter.PROPERTY_MODEL_STRUCTURE,
	 * getEngineInstance().getDataSource().getModelStructure());
	 * pathFiltersMap.put(CubeFilter.PROPERTY_ENTITIES, modelEntities);
	 *
	 * if (oldQueryGraph == null && filteredQuery != null) {// normal //
	 * execution: // a query // exists queryGraph =
	 * updateQueryGraphInQuery(filteredQuery, forceReturnGraph, modelEntities,
	 * ambiguousFieldsPaths); if (queryGraph != null) { // String modelName = //
	 * getDataSource().getConfiguration().getModelName(); // Graph<IModelEntity,
	 * Relationship> graph = //
	 * getDataSource().getModelStructure().getRootEntitiesGraph(modelName, //
	 * false).getRootEntitiesGraph(); ambiguousFields =
	 * getAmbiguousFields(filteredQuery, modelEntities, modelFieldsMap); //
	 * filter paths GraphManager.filterPaths(ambiguousFields, pathFiltersMap,
	 * (QbeEngineConfig.getInstance().getPathsFiltersImpl()));
	 *
	 * boolean removeSubPaths =
	 * QbeEngineConfig.getInstance().isRemoveSubpaths(); if (removeSubPaths) {
	 * String orderDirection = QbeEngineConfig.getInstance().getPathsOrder();
	 * GraphUtilities.cleanSubPaths(ambiguousFields, orderDirection); }
	 *
	 * GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().
	 * getDefaultCoverImpl()).applyDefault(ambiguousFields, queryGraph,
	 * modelEntities); isDierctlyExecutable =
	 * GraphManager.isDirectlyExecutable(modelEntities, queryGraph); } else { //
	 * no ambigous fields found isDierctlyExecutable = true; } } else {// saved
	 * query ambiguousFields = getAmbiguousFields(filteredQuery, modelEntities,
	 * modelFieldsMap); // filter paths
	 * GraphManager.filterPaths(ambiguousFields, pathFiltersMap,
	 * (QbeEngineConfig.getInstance().getPathsFiltersImpl()));
	 * applySavedGraphPaths(oldQueryGraph, ambiguousFields); queryGraph =
	 * oldQueryGraph; }
	 *
	 * if (queryGraph != null) { boolean valid =
	 * GraphManager.getGraphValidatorInstance
	 * (QbeEngineConfig.getInstance().getGraphValidatorImpl())
	 * .isValid(queryGraph, modelEntities); logger.debug("QueryGraph valid = " +
	 * valid); if (!valid) { throw new
	 * SpagoBIEngineServiceException("RELATIONS",
	 * "error.mesage.description.relationship.not.enough"); } }
	 *
	 * // we update the query graph in the query designed by the user in //
	 * order to save it for later executions
	 * query.setQueryGraph(filteredQuery.getQueryGraph());
	 *
	 * // serialize the ambiguous fields ObjectMapper mapper = new
	 * ObjectMapper();
	 *
	 * @SuppressWarnings("deprecation") SimpleModule simpleModule = new
	 * SimpleModule("SimpleModule", new Version(1, 0, 0, null));
	 * simpleModule.addSerializer(Relationship.class, new
	 * RelationJSONSerializer(getEngineInstance().getDataSource(),
	 * getLocale())); simpleModule.addSerializer(ModelObjectI18n.class, new
	 * ModelObjectInternationalizedSerializer
	 * (getEngineInstance().getDataSource(), getLocale()));
	 * mapper.registerModule(simpleModule);
	 *
	 * if (ambiguousFields.size() > 0 || ambiguousFieldsPaths == null) {
	 * ambiguousFieldsPaths = mapper.writeValueAsString(ambiguousFields); }
	 *
	 * // update the roles in the query if exists ambiguous paths String
	 * serializedRoles = ""; if (ambiguousFieldsPaths.length() > 5) {
	 * serializedRoles = ambiguousRoles; if (serializedRoles == null ||
	 * serializedRoles.length() < 5) { serializedRoles =
	 * roleSelectionFromTheSavedQuery; } LogMF.debug(logger, ambiguousRoles +
	 * "is {0}", ambiguousFieldsPaths);
	 * query.setRelationsRoles(serializedRoles);
	 * applySelectedRoles(serializedRoles, modelEntities, query); }
	 *
	 * // validate the response and create the list of warnings and errors if
	 * (!query.isAliasDefinedInSelectFields()) { ambiguousWarinig =
	 * "sbi.qbe.relationshipswizard.roles.validation.no.fields.alias"; }
	 *
	 * if (!isDierctlyExecutable) { isDierctlyExecutable = ambiguousFieldsPaths
	 * == null || ambiguousFieldsPaths.equals("") ||
	 * ambiguousFieldsPaths.equals("[]"); // no ambiguos fields found so the
	 * query is executable; }
	 *
	 * List<String> queryErrors = QueryValidator.validate(query,
	 * getEngineInstance().getDataSource()); String serializedQueryErrors =
	 * mapper.writeValueAsString(queryErrors); JSONObject toReturn = new
	 * JSONObject(); // String queryString = buildQueryString(getDataSource(),
	 * query); if (handleTimeFilter) {
	 *
	 * toReturn.put("ambiguousFieldsPaths", ambiguousFieldsPaths);
	 * toReturn.put("serializedRoles", serializedRoles);
	 * toReturn.put("executeDirectly", isDierctlyExecutable);
	 * toReturn.put("ambiguousWarinig", ambiguousWarinig);
	 * toReturn.put("catalogueErrors", serializedQueryErrors);
	 *
	 * // toReturn.put(QUERY_STRING, queryString);
	 *
	 * } else { this.getEngineInstance().setActiveQuery(query); }
	 *
	 * return Response.ok(toReturn.toString()).build(); } catch (Throwable t) {
	 * errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
	 * errorHitsMonitor.stop(); throw
	 * SpagoBIEngineServiceExceptionHandler.getInstance
	 * ().getWrappedException("setQueryCatalog", getEngineInstance(), t); }
	 * finally { if (totalTimeMonitor != null) totalTimeMonitor.stop();
	 * logger.debug("OUT"); }
	 *
	 * }
	 */

	/*
	 * public static void cleanFieldsRolesMapInEntity(Query query) { if (query
	 * != null) { query.setMapEntityRoleField(null); } }
	 * 
	 * public void applySelectedRoles(String serializedRoles, Set<IModelEntity>
	 * modelEntities, Query query) { cleanFieldsRolesMapInEntity(query); try {
	 * if (serializedRoles != null && !serializedRoles.trim().equals("{}") &&
	 * !serializedRoles.trim().equals("[]") &&
	 * !serializedRoles.trim().equals("")) {
	 * query.initFieldsRolesMapInEntity(getEngineInstance().getDataSource()); }
	 * 
	 * } catch (Exception e2) {
	 * logger.error("Error deserializing the list of roles of the entities",
	 * e2); throw new SpagoBIEngineRuntimeException(
	 * "Error deserializing the list of roles of the entities", e2); } }
	 * 
	 * public void applySavedGraphPaths(QueryGraph queryGraph,
	 * Set<ModelFieldPaths> ambiguousFields) {
	 * 
	 * PathInspector pi = new PathInspector(queryGraph, queryGraph.vertexSet());
	 * Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> paths =
	 * pi.getAllEntitiesPathsMap();
	 * (GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig
	 * .getInstance().getDefaultCoverImpl())).applyDefault(paths,
	 * ambiguousFields);
	 * 
	 * }
	 * 
	 * 
	 * public Set<ModelFieldPaths> getAmbiguousFields(Query query,
	 * Set<IModelEntity> modelEntities, Map<IModelField, Set<IQueryField>>
	 * modelFieldsMap) { logger.debug("IN");
	 * 
	 * try {
	 * 
	 * String modelName =
	 * getEngineInstance().getDataSource().getConfiguration().getModelName();
	 * 
	 * Set<IModelField> modelFields = modelFieldsMap.keySet();
	 * 
	 * Assert.assertNotNull(modelFields, "No field specified in teh query");
	 * Set<ModelFieldPaths> ambiguousModelField = new
	 * HashSet<ModelFieldPaths>(); if (modelFields != null) {
	 * 
	 * Graph<IModelEntity, Relationship> graph =
	 * getEngineInstance().getDataSource
	 * ().getModelStructure().getRootEntitiesGraph(modelName, false)
	 * .getRootEntitiesGraph();
	 * 
	 * PathInspector pathInspector = new PathInspector(graph, modelEntities);
	 * Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>>
	 * ambiguousMap = pathInspector.getAmbiguousEntitiesAllPathsMap();
	 * 
	 * Iterator<IModelField> modelFieldsIter = modelFields.iterator();
	 * 
	 * while (modelFieldsIter.hasNext()) { IModelField iModelField =
	 * modelFieldsIter.next(); IModelEntity me = iModelField.getParent();
	 * Set<GraphPath<IModelEntity, Relationship>> paths = ambiguousMap.get(me);
	 * if (paths != null) { Set<IQueryField> queryFields =
	 * modelFieldsMap.get(iModelField); if (queryFields != null) {
	 * Iterator<IQueryField> queryFieldsIter = queryFields.iterator(); while
	 * (queryFieldsIter.hasNext()) { ambiguousModelField.add(new
	 * ModelFieldPaths(queryFieldsIter.next(), iModelField, paths)); } } } } }
	 * 
	 * return ambiguousModelField;
	 * 
	 * } catch (Throwable t) { throw new
	 * SpagoBIRuntimeException("Error while getting ambiguous fields", t); }
	 * finally { logger.debug("OUT"); } }
	 * 
	 * public QueryGraph updateQueryGrapsshInQuery(Query query, boolean
	 * forceReturnGraph, Set<IModelEntity> modelEntities, String
	 * ambiguousFieldsPaths) { boolean isTheOldQueryGraphValid = false;
	 * logger.debug("IN"); QueryGraph queryGraph = null; try {
	 * 
	 * queryGraph = getQueryGraphFromRequest(query, modelEntities,
	 * ambiguousFieldsPaths);
	 * 
	 * if (queryGraph != null) { // check if the graph selected by the user is
	 * still valid isTheOldQueryGraphValid = isTheOldQueryGraphValid(queryGraph,
	 * query); }
	 * 
	 * if (queryGraph == null || !isTheOldQueryGraphValid) { // calculate the
	 * default cover graph logger.debug("Calculating the default graph");
	 * IModelStructure modelStructure =
	 * getEngineInstance().getDataSource().getModelStructure();
	 * RootEntitiesGraph rootEntitiesGraph =
	 * modelStructure.getRootEntitiesGraph(
	 * getEngineInstance().getDataSource().getConfiguration().getModelName(),
	 * false); Graph<IModelEntity, Relationship> graph =
	 * rootEntitiesGraph.getRootEntitiesGraph();
	 * logger.debug("UndirectedGraph retrieved");
	 * 
	 * Set<IModelEntity> entities =
	 * query.getQueryEntities(getEngineInstance().getDataSource()); if
	 * (entities.size() > 0) { queryGraph =
	 * GraphManager.getDefaultCoverGraphInstance
	 * (QbeEngineConfig.getInstance().getDefaultCoverImpl
	 * ()).getCoverGraph(graph, entities); } } else {
	 * query.setQueryGraph(queryGraph); return null; }
	 * 
	 * query.setQueryGraph(queryGraph); return queryGraph;
	 * 
	 * } catch (Throwable t) { throw new
	 * SpagoBIRuntimeException("Error while loading the not ambigous graph", t);
	 * } finally { logger.debug("OUT"); }
	 * 
	 * }
	 * 
	 * public boolean isTheOldQueryGraphValid(QueryGraph oldQueryGraph, Query
	 * newQuery) {
	 * 
	 * if (oldQueryGraph == null) { return false; }
	 * 
	 * Set<IModelEntity> oldVertexes = oldQueryGraph.vertexSet(); if
	 * (oldVertexes == null) { return false; } Set<IModelEntity>
	 * newQueryEntities =
	 * newQuery.getQueryEntities(getEngineInstance().getDataSource()); if
	 * (newQueryEntities == null) { return true; }
	 * 
	 * Iterator<IModelEntity> newQueryEntitiesIter =
	 * newQueryEntities.iterator(); while (newQueryEntitiesIter.hasNext()) {
	 * IModelEntity iModelEntity = newQueryEntitiesIter.next(); if
	 * (!oldVertexes.contains(iModelEntity)) { return false;// if at least one
	 * entity contained in the query is // not covered by the old cover graph
	 * the old // graph is not valid } } return true; }
	 * 
	 * 
	 * 
	 * private QueryGraph getQueryGraphFromRequest(Query query,
	 * Set<IModelEntity> modelEntities, String ambiguousFieldsPaths) {
	 * List<Relationship> toReturn = new ArrayList<Relationship>();
	 * IModelStructure modelStructure =
	 * getEngineInstance().getDataSource().getModelStructure();
	 * logger.debug("IModelStructure retrieved"); RootEntitiesGraph
	 * rootEntitiesGraph =
	 * modelStructure.getRootEntitiesGraph(getEngineInstance(
	 * ).getDataSource().getConfiguration().getModelName(), false);
	 * logger.debug("RootEntitiesGraph retrieved");
	 * 
	 * Set<Relationship> relationships = rootEntitiesGraph.getRelationships();
	 * logger.debug("Set<Relationship> retrieved");
	 * 
	 * LogMF.debug(logger, ambiguousFieldsPaths + "is {0}",
	 * ambiguousFieldsPaths);
	 * 
	 * List<ModelFieldPaths> list = null; if
	 * (StringUtilities.isNotEmpty(ambiguousFieldsPaths)) { try { list =
	 * deserializeList(ambiguousFieldsPaths, relationships, modelStructure,
	 * query); } catch (FieldNotAttendInTheQuery e1) { logger.debug(
	 * "The query has been updated and in the previous ambiguos paths selection there is some field don't exist in teh query"
	 * ); return null; } catch (SerializationException e) { throw new
	 * SpagoBIEngineRuntimeException
	 * ("Error while deserializing list of relationships", e); }
	 * logger.debug("Paths deserialized"); } QueryGraph queryGraph = null;
	 * 
	 * if (list != null && !list.isEmpty()) { Iterator<ModelFieldPaths> it =
	 * list.iterator(); while (it.hasNext()) { ModelFieldPaths modelFieldPaths =
	 * it.next(); if (modelFieldPaths != null) { Set<PathChoice> set =
	 * modelFieldPaths.getChoices(); Iterator<PathChoice> pathChoiceIterator =
	 * set.iterator(); while (pathChoiceIterator.hasNext()) { PathChoice choice
	 * = pathChoiceIterator.next(); toReturn.addAll(choice.getRelations()); } }
	 * } QueryGraphBuilder builder = new QueryGraphBuilder(); queryGraph =
	 * builder.buildGraphFromEdges(toReturn); }
	 * logger.debug("QueryGraph created"); return queryGraph; }
	 * 
	 * public static List<ModelFieldPaths> deserializeList(String serialized,
	 * Collection<Relationship> relationShips, IModelStructure modelStructure,
	 * Query query) throws SerializationException { ObjectMapper mapper = new
	 * ObjectMapper();
	 * 
	 * @SuppressWarnings("deprecation") SimpleModule simpleModule = new
	 * SimpleModule("SimpleModule", new Version(1, 0, 0, null));
	 * simpleModule.addDeserializer(ModelFieldPaths.class, new
	 * ModelFieldPathsJSONDeserializer(relationShips, modelStructure, query));
	 * mapper.registerModule(simpleModule); TypeReference<List<ModelFieldPaths>>
	 * type = new TypeReference<List<ModelFieldPaths>>() { }; try { return
	 * mapper.readValue(serialized, type); } catch (Exception e) { throw new
	 * SerializationException("Error deserializing the list of ModelFieldPaths",
	 * e); } }
	 */
	@POST
	@Path("/executeQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response executeQuery(@javax.ws.rs.core.Context HttpServletRequest req, @QueryParam("start") String startS, @QueryParam("limit") String limitS,
			@QueryParam("currentQueryId") String id) {

		Integer limit = null;
		Integer start = null;
		Integer maxSize = null;
		IDataStore dataStore = null;
		Query query = null;

		Integer resultNumber = null;
		JSONObject gridDataFeed = new JSONObject();
		JSONObject jsonEncodedReq = null;
		JSONArray catalogue;
		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;
		JSONArray queries = null;
		JSONObject queryJSON = null;
		logger.debug("IN");

		try {
			totalTimeMonitor = MonitorFactory.start("QbeEngine.executeQueryAction.totalTime");
			jsonEncodedReq = RestUtilities.readBodyAsJSONObject(req);

			catalogue = jsonEncodedReq.getJSONArray("catalogue");
			if (catalogue == null) {
				catalogue = jsonEncodedReq.getJSONArray("qbeJSONQuery");
				JSONObject jo = new JSONObject(catalogue);
				jo = jo.getJSONObject("catalogue");
				queries = jo.getJSONArray("queries");
			} else {
				queries = new JSONArray(catalogue.toString());
			}

			logger.debug("catalogue" + " = [" + catalogue + "]");

			try {

				for (int i = 0; i < queries.length(); i++) {
					queryJSON = queries.getJSONObject(i);
					if (queryJSON.get("id").equals(id)) {
						query = deserializeQuery(queryJSON);
					}
				}
			} catch (SerializationException e) {
				String message = "Impossible to deserialize query";
				throw new SpagoBIEngineServiceException("DESERIALIZATING QUERY", message, e);

			}

			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);

			logger.debug("Parameter [" + "limit" + "] is equals to [" + limit + "]");

			IModelAccessModality accessModality = getEngineInstance().getDataSource().getModelAccessModality();

			Query filteredQuery = accessModality.getFilteredStatement(query, this.getEngineInstance().getDataSource(), userProfile.getUserAttributes());
			Map<IModelField, Set<IQueryField>> modelFieldsMap = filteredQuery.getQueryFields(getEngineInstance().getDataSource());
			Set<IModelField> modelFields = modelFieldsMap.keySet();
			Set<IModelEntity> modelEntities = Query.getQueryEntities(modelFields);

			updateQueryGraphInQuery(filteredQuery, true, modelEntities);

			Map<String, Map<String, String>> inlineFilteredSelectFields = query.getInlineFilteredSelectFields();

			boolean thereAreInlineTemporalFilters = inlineFilteredSelectFields != null && inlineFilteredSelectFields.size() > 0;
			if (thereAreInlineTemporalFilters) {
				limit = 0;
			}
			if (startS != null && !startS.equals("")) {
				start = Integer.parseInt(startS);
			}

			if (limitS != null && !limitS.equals("")) {
				limit = Integer.parseInt(limitS);
			}
			dataStore = executeQuery(start, limit, filteredQuery);
			if (thereAreInlineTemporalFilters) {
				dataStore = new TimeAggregationHandler(null).handleTimeAggregations(query, dataStore);
			}
			resultNumber = (Integer) dataStore.getMetaData().getProperty("resultNumber");

			logger.debug("Total records: " + resultNumber);
			boolean overflow = maxSize != null && resultNumber >= maxSize;
			if (overflow) {
				logger.warn("Query results number [" + resultNumber + "] exceeds max result limit that is [" + maxSize + "]");
				// auditlogger.info("[" + userProfile.getUserId() +
				// "]:: max result limit [" + maxSize + "] exceeded with SQL: "
				// + sqlQuery);
			}
			gridDataFeed = serializeDataStore(dataStore);
			return Response.ok(gridDataFeed.toString()).build();
		} catch (Throwable t) {
			errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
			errorHitsMonitor.stop();
			throw new SpagoBIServiceException("execute query", "An unexpected error occured while executing service", t);
		} finally {
			if (totalTimeMonitor != null)
				totalTimeMonitor.stop();
			logger.debug("OUT");
		}

	}

	private void updateQueryGraphInQuery(Query filteredQuery, boolean b, Set<IModelEntity> modelEntities) {
		boolean isTheOldQueryGraphValid = false;
		logger.debug("IN");
		QueryGraph queryGraph = null;
		try {

			// calculate the default cover graph
			logger.debug("Calculating the default graph");
			IModelStructure modelStructure = getEngineInstance().getDataSource().getModelStructure();
			RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(getEngineInstance().getDataSource().getConfiguration().getModelName(),
					false);
			Graph<IModelEntity, Relationship> graph = rootEntitiesGraph.getRootEntitiesGraph();
			logger.debug("UndirectedGraph retrieved");
			Set<IModelEntity> entities = filteredQuery.getQueryEntities(getEngineInstance().getDataSource());
			if (entities.size() > 0) {
				queryGraph = GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl()).getCoverGraph(graph, entities);
			}

			filteredQuery.setQueryGraph(queryGraph);

		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Error while loading the not ambigous graph", t);
		} finally {
			logger.debug("OUT");
		}

	}

	public JSONObject serializeDataStore(IDataStore dataStore) {
		JSONDataWriter dataSetWriter = new JSONDataWriter();
		JSONObject gridDataFeed = (JSONObject) dataSetWriter.write(dataStore);
		return gridDataFeed;
	}

	public static void updatePromptableFiltersValue(Query query, String promptableFilters) throws JSONException {
		updatePromptableFiltersValue(query, false, promptableFilters);
	}

	private Query deserializeQuery(JSONObject queryJSON) throws SerializationException, JSONException {
		// queryJSON.put("expression", queryJSON.get("filterExpression"));
		return SerializerFactory.getDeserializer("application/json").deserializeQuery(queryJSON.toString(), getEngineInstance().getDataSource());
	}

	public static void updatePromptableFiltersValue(Query query, boolean useDefault, String promptableFilters) throws JSONException {
		logger.debug("IN");
		List whereFields = query.getWhereFields();
		Iterator whereFieldsIt = whereFields.iterator();
		String[] question = { "?" };

		JSONObject requestPromptableFilters = new JSONObject(promptableFilters);

		while (whereFieldsIt.hasNext()) {
			WhereField whereField = (WhereField) whereFieldsIt.next();
			if (whereField.isPromptable()) {
				// getting filter value on request
				if (!useDefault || requestPromptableFilters != null) {
					JSONArray promptValuesList = requestPromptableFilters.optJSONArray(whereField.getName());
					if (promptValuesList != null) {
						String[] promptValues = toStringArray(promptValuesList);
						logger.debug("Read prompts " + promptValues + " for promptable filter " + whereField.getName() + ".");
						whereField.getRightOperand().lastValues = promptValues;
					}
				} else {
					whereField.getRightOperand().lastValues = question;
				}
			}
		}
		List havingFields = query.getHavingFields();
		Iterator havingFieldsIt = havingFields.iterator();
		while (havingFieldsIt.hasNext()) {
			HavingField havingField = (HavingField) havingFieldsIt.next();
			if (havingField.isPromptable()) {
				if (!useDefault || requestPromptableFilters != null) {
					// getting filter value on request
					// promptValuesList =
					// action.getAttributeAsList(havingField.getEscapedName());
					JSONArray promptValuesList = requestPromptableFilters.optJSONArray(havingField.getName());
					if (promptValuesList != null) {
						String[] promptValues = toStringArray(promptValuesList);
						logger.debug("Read prompt value " + promptValues + " for promptable filter " + havingField.getName() + ".");
						havingField.getRightOperand().lastValues = promptValues; // TODO
																					// how
																					// to
																					// manage
																					// multi-values
																					// prompts?
					}
				}
			} else {
				havingField.getRightOperand().lastValues = question;
			}
		}
		logger.debug("OUT");
	}

	private static String[] toStringArray(JSONArray o) throws JSONException {
		String[] promptValues = new String[o.length()];
		for (int i = 0; i < o.length(); i++) {
			promptValues[i] = o.getString(i);
		}
		return promptValues;
	}

	public IDataStore executeQuery(Integer start, Integer limit, Query q) {
		IDataStore dataStore = null;
		IDataSet dataSet = getActiveQueryAsDataSet(q);
		AbstractQbeDataSet qbeDataSet = (AbstractQbeDataSet) dataSet;
		IStatement statement = qbeDataSet.getStatement();
		QueryGraph graph = statement.getQuery().getQueryGraph();
		boolean valid = GraphManager.getGraphValidatorInstance(QbeEngineConfig.getInstance().getGraphValidatorImpl()).isValid(graph,
				statement.getQuery().getQueryEntities(getEngineInstance().getDataSource()));
		logger.debug("QueryGraph valid = " + valid);
		if (!valid) {
			throw new SpagoBIEngineServiceException("RELATIONS", "error.mesage.description.relationship.not.enough");
		}
		try {
			logger.debug("Executing query ...");
			Integer maxSize = QbeEngineConfig.getInstance().getResultLimit();
			logger.debug("Configuration setting  [" + "QBE.QBE-SQL-RESULT-LIMIT.value" + "] is equals to [" + (maxSize != null ? maxSize : "none") + "]");
			String jpaQueryStr = statement.getQueryString();

			logger.debug("Executable query (HQL/JPQL): [" + jpaQueryStr + "]");

			logQueryInAudit(qbeDataSet);

			dataSet.loadData(start, limit, (maxSize == null ? -1 : maxSize.intValue()));
			dataStore = dataSet.getDataStore();
			Assert.assertNotNull(dataStore, "The dataStore returned by loadData method of the class [" + dataSet.getClass().getName() + "] cannot be null");
		} catch (Exception e) {
			logger.debug("Query execution aborted because of an internal exceptian");
			SpagoBIEngineServiceException exception;
			String message;

			message = "An error occurred in  service while executing query: [" + statement.getQueryString() + "]";
			exception = new SpagoBIEngineServiceException("Execute query", message, e);
			exception.addHint("Check if the query is properly formed: [" + statement.getQueryString() + "]");
			exception.addHint("Check connection configuration");
			exception.addHint("Check the qbe jar file");

			throw exception;
		}
		logger.debug("Query executed succesfully");
		return dataStore;
	}

	private IDataSet getActiveQueryAsDataSet(Query q) {
		IStatement statement = getEngineInstance().getDataSource().createStatement(q);
		IDataSet dataSet;
		try {

			dataSet = QbeDatasetFactory.createDataSet(statement);
			boolean isMaxResultsLimitBlocking = QbeEngineConfig.getInstance().isMaxResultLimitBlocking();
			dataSet.setAbortOnOverflow(isMaxResultsLimitBlocking);

			Map userAttributes = new HashMap();
			UserProfile userProfile = (UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
			userAttributes.putAll(userProfile.getUserAttributes());
			userAttributes.put(SsoServiceInterface.USER_ID, userProfile.getUserId().toString());

			dataSet.addBinding("attributes", userAttributes);
			dataSet.addBinding("parameters", this.getEnv());
			dataSet.setUserProfileAttributes(userAttributes);

			dataSet.setParamsMap(this.getEnv());

		} catch (Exception e) {
			logger.debug("Error getting the data set from the query");
			throw new SpagoBIRuntimeException("Error getting the data set from the query", e);
		}
		logger.debug("Dataset correctly taken from the query ");
		return dataSet;

	}

	private void logQueryInAudit(AbstractQbeDataSet dataset) {
		UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);

		if (dataset instanceof JPQLDataSet) {
			auditlogger.info("[" + userProfile.getUserId() + "]:: JPQL: " + dataset.getStatement().getQueryString());
			auditlogger.info("[" + userProfile.getUserId() + "]:: SQL: " + ((JPQLDataSet) dataset).getSQLQuery(true));
		} else if (dataset instanceof HQLDataSet) {
			auditlogger.info("[" + userProfile.getUserId() + "]:: HQL: " + dataset.getStatement().getQueryString());
			auditlogger.info("[" + userProfile.getUserId() + "]:: SQL: " + ((HQLDataSet) dataset).getSQLQuery(true));
		} else {
			auditlogger.info("[" + userProfile.getUserId() + "]:: SQL: " + dataset.getStatement().getSqlQueryString());
		}

	}

	@POST
	@Path("/saveDataSet")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveDataSet(@QueryParam("currentQueryId") String currentQueryId, @javax.ws.rs.core.Context HttpServletRequest req,
			@javax.ws.rs.core.Context HttpServletRequest req1, @QueryParam("label") String label, @QueryParam("name") String name,
			@QueryParam("description") String description, @QueryParam("isPersisted") String isPersisted, @QueryParam("isScheduled") String isScheduled,
			@QueryParam("persistTable") String persistTable, @QueryParam("startDateField") String startDateField,
			@QueryParam("endDateField") String endDateField, @QueryParam("scopeId") String scopeId, @QueryParam("scopeCd") String scopeCd,
			@QueryParam("categoryId") String categoryId, @QueryParam("categoryCd") String categoryCd, @QueryParam("qbeDataSource") String qbeDataSource,
			@QueryParam("sourceDatasetLabel") String sourceDatasetLabel, @QueryParam("isFlatDataset") String isFlatDataset) {
		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;
		Query query = null;
		JSONArray catalogue = null;
		JSONArray queries = null;
		JSONObject queryJSON = null;
		try {
			handleTimeFilter = false;

			JSONObject jsonEncodedRequest = RestUtilities.readBodyAsJSONObject(req);
			catalogue = jsonEncodedRequest.getJSONArray("catalogue");
			if (catalogue == null) {
				catalogue = jsonEncodedRequest.getJSONArray("qbeJSONQuery");
				JSONObject jo = new JSONObject(catalogue);
				jo = jo.getJSONObject("catalogue");
				queries = jo.getJSONArray("queries");
			} else {
				queries = new JSONArray(catalogue.toString());
			}

			logger.debug("catalogue" + " = [" + catalogue + "]");

			try {

				for (int i = 0; i < queries.length(); i++) {
					queryJSON = queries.getJSONObject(i);
					if (queryJSON.get("id").equals(currentQueryId)) {
						query = deserializeQuery(queryJSON);
					}
				}
			} catch (SerializationException e) {
				String message = "Impossible to deserialize query";
				throw new SpagoBIEngineServiceException("DESERIALIZATING QUERY", message, e);

			}
			String schedulingCronLine = jsonEncodedRequest.getString("schedulingCronLine");
			String meta = jsonEncodedRequest.getString("meta");
			String qbeJSONQuery = jsonEncodedRequest.getString("qbeJSONQuery");
			String pars = getDataSetParametersAsString(jsonEncodedRequest);
			validateLabel(label);
			IDataSet dataset = getActiveQueryAsDataSet(query);
			int datasetId = -1;

			datasetId = saveQbeDataset(dataset, label, name, description, scopeId, scopeCd, categoryId, categoryCd, isPersisted, isScheduled, persistTable,
					startDateField, endDateField, schedulingCronLine, meta, qbeJSONQuery, pars);

			JSONObject obj = new JSONObject();
			obj.put("success", "true");
			obj.put("id", String.valueOf(datasetId));
			return Response.ok(obj.toString()).build();

		} catch (Throwable t) {
			errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
			errorHitsMonitor.stop();
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("error while saving dataset", getEngineInstance(), t);
		} finally {
			if (totalTimeMonitor != null)
				totalTimeMonitor.stop();
			logger.debug("OUT");
		}
	}

	private void validateLabel(String label) {
		DataSetServiceProxy proxy = (DataSetServiceProxy) getEnv().get(EngineConstants.ENV_DATASET_PROXY);
		IDataSet dataset = proxy.getDataSetByLabel(label);
		if (dataset != null) {
			throw new SpagoBIRuntimeException("Label already in use");
		}
	}

	private int saveQbeDataset(IDataSet dataset, String label, String name, String description, String scopeId, String scopeCd, String categoryId,
			String categoryCd, String isPersisted, String isScheduled, String persistTable, String startDateField, String endDateField,
			String schedulingCronLine, String meta, String qbeJSONQuery, String pars) {

		QbeDataSet newDataset = createNewQbeDataset(dataset, label, name, description, scopeId, scopeCd, categoryId, categoryCd, isPersisted, isScheduled,
				persistTable, startDateField, endDateField, schedulingCronLine, meta, qbeJSONQuery, pars);

		IDataSet datasetSaved = saveNewDataset(newDataset);

		int datasetId = datasetSaved.getId();
		return datasetId;
	}

	private QbeDataSet createNewQbeDataset(IDataSet dataset, String label, String name, String description, String scopeIdParam, String scopeCdParam,
			String categoryIdParam, String categoryCdParam, String isPersistedParam, String isScheduledParam, String persistTable, String startDateField,
			String endDateField, String schedulingCronLine, String meta, String qbeJSONQuery, String pars) {
		AbstractQbeDataSet qbeDataset = (AbstractQbeDataSet) dataset;

		QbeDataSet newDataset;

		UserProfile profile = (UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);

		// if its a federated dataset we've to add the dependent datasets
		if (getEnv().get(EngineConstants.ENV_FEDERATION) != null) {

			FederationDefinition federation = (FederationDefinition) getEnv().get(EngineConstants.ENV_FEDERATION);
			// Object relations = (getEnv().get(EngineConstants.ENV_RELATIONS));
			// if (relations != null) {
			// federation.setRelationships(relations.toString());
			// } else {
			// logger.debug("No relation defined " + relations);
			// }
			//
			// federation.setLabel((getEnv().get(EngineConstants.ENV_FEDERATED_ID).toString()));
			// federation.setFederation_id(new Integer((String)
			// (getEnv().get(EngineConstants.ENV_FEDERATED_ID))));

			newDataset = new FederatedDataSet(federation, (String) profile.getUserId());
			// ((FederatedDataSet)
			// newDataset).setDependentDataSets(federation.getSourceDatasets());
			newDataset.setDataSourceForWriting((IDataSource) getEnv().get(EngineConstants.ENV_DATASOURCE));
			newDataset.setDataSourceForReading((IDataSource) getEnv().get(EngineConstants.ENV_DATASOURCE));
		} else {
			newDataset = new QbeDataSet();
		}

		newDataset.setLabel(label);
		newDataset.setName(name);
		newDataset.setDescription(description);
		if (pars != null) {
			newDataset.setParameters(pars);
		}
		String scopeCd = null;
		Integer scopeId = null;
		String categoryCd = null;
		Integer categoryId = null;

		if (scopeIdParam != null) {
			scopeCd = scopeCdParam;
			scopeId = Integer.parseInt(scopeIdParam);
		} else {
			scopeCd = SpagoBIConstants.DS_SCOPE_USER;
		}

		if (categoryIdParam != null) {
			categoryCd = categoryCdParam;
			categoryId = Integer.parseInt(categoryIdParam);
		} else {
			categoryCd = dataset.getCategoryCd();
			categoryId = dataset.getCategoryId();
		}
		if (categoryId == null
				&& (scopeCd.equalsIgnoreCase(SpagoBIConstants.DS_SCOPE_TECHNICAL) || scopeCd.equalsIgnoreCase(SpagoBIConstants.DS_SCOPE_ENTERPRISE))) {
			throw new SpagoBIRuntimeException("Dataset Enterprise or Technical must have a category");

		}
		newDataset.setScopeCd(scopeCd);
		newDataset.setScopeId(scopeId);
		newDataset.setCategoryCd(categoryCd);
		newDataset.setCategoryId(categoryId);

		String owner = profile.getUserId().toString();
		// saves owner of the dataset
		newDataset.setOwner(owner);

		String metadata = getMetadataAsString(dataset);
		logger.debug("Dataset's metadata: [" + metadata + "]");
		newDataset.setDsMetadata(metadata);

		newDataset.setDataSource(qbeDataset.getDataSource());

		String datamart = qbeDataset.getStatement().getDataSource().getConfiguration().getModelName();
		String datasource = qbeDataset.getDataSource().getLabel();
		JSONObject jsonConfig = new JSONObject();
		try {
			jsonConfig.put(QbeDataSet.QBE_DATA_SOURCE, datasource);
			jsonConfig.put(QbeDataSet.QBE_DATAMARTS, datamart);
			jsonConfig.put(QbeDataSet.QBE_JSON_QUERY, qbeJSONQuery);
			jsonConfig.put(FederatedDataSet.QBE_DATASET_CACHE_MAP, getEnv().get(EngineConstants.ENV_DATASET_CACHE_MAP));
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Error while creating dataset's JSON config", e);
		}

		newDataset.setConfiguration(jsonConfig.toString());

		// get Persist and scheduling informations
		boolean isPersisted = Boolean.parseBoolean(isPersistedParam);
		newDataset.setPersisted(isPersisted);
		boolean isScheduled = Boolean.parseBoolean(isScheduledParam);
		newDataset.setScheduled(isScheduled);
		if (persistTable != null) {
			newDataset.setPersistTableName(persistTable);
		}
		if (startDateField != null) {
			newDataset.setStartDateField(startDateField);
		}
		if (endDateField != null) {
			newDataset.setEndDateField(endDateField);
		}
		if (schedulingCronLine != null) {
			newDataset.setSchedulingCronLine(schedulingCronLine);
		}

		try {

			JSONArray metadataArray = JSONUtils.toJSONArray(meta);

			IMetaData metaData = dataset.getMetadata();
			for (int i = 0; i < metaData.getFieldCount(); i++) {
				IFieldMetaData ifmd = metaData.getFieldMeta(i);
				for (int j = 0; j < metadataArray.length(); j++) {

					String fieldAlias = ifmd.getAlias() != null ? ifmd.getAlias() : "";
					// remove dataset source
					String fieldName = ifmd.getName().substring(ifmd.getName().indexOf(':') + 1);

					if (fieldAlias.equals((metadataArray.getJSONObject(j)).getString("name"))
							|| fieldName.equals((metadataArray.getJSONObject(j)).getString("name"))) {
						if ("MEASURE".equals((metadataArray.getJSONObject(j)).getString("fieldType"))) {
							ifmd.setFieldType(IFieldMetaData.FieldType.MEASURE);
						} else {
							ifmd.setFieldType(IFieldMetaData.FieldType.ATTRIBUTE);
						}
						break;
					}
				}
			}

			DatasetMetadataParser dsp = new DatasetMetadataParser();
			String dsMetadata = dsp.metadataToXML(metaData);

			newDataset.setDsMetadata(dsMetadata);

		} catch (Exception e) {
			logger.error("Error in calculating metadata");
			throw new SpagoBIRuntimeException("Error in calculating metadata", e);
		}

		return newDataset;
	}

	private String getMetadataAsString(IDataSet dataset) {
		IMetaData metadata = getDataSetMetadata(dataset);
		DatasetMetadataParser parser = new DatasetMetadataParser();
		String toReturn = parser.metadataToXML(metadata);
		return toReturn;
	}

	private IMetaData getDataSetMetadata(IDataSet dataset) {
		IMetaData metaData = null;
		Integer start = new Integer(0);
		Integer limit = new Integer(10);
		Integer maxSize = QbeEngineConfig.getInstance().getResultLimit();
		try {
			dataset.loadData(start, limit, maxSize);
			IDataStore dataStore = dataset.getDataStore();
			metaData = dataStore.getMetaData();
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while executing dataset", e);
		}
		return metaData;
	}

	private IDataSet saveNewDataset(IDataSet newDataset) {
		DataSetServiceProxy proxy = (DataSetServiceProxy) getEnv().get(EngineConstants.ENV_DATASET_PROXY);
		logger.debug("Saving new dataset ...");
		IDataSet saved = proxy.saveDataSet(newDataset);
		logger.debug("Dataset saved without errors");
		return saved;
	}

	private String getDataSetParametersAsString(JSONObject json) {
		String parametersString = null;

		try {
			JSONArray parsListJSON = json.optJSONArray(DataSetConstants.PARS);
			if (parsListJSON == null) {
				return null;
			}

			SourceBean sb = new SourceBean("PARAMETERSLIST");
			SourceBean sb1 = new SourceBean("ROWS");

			for (int i = 0; i < parsListJSON.length(); i++) {
				JSONObject obj = (JSONObject) parsListJSON.get(i);
				String name = obj.optString("name");
				String type = obj.optString("type");
				String multiValue = obj.optString("multiValue");
				String defaultValue = obj.optString("defaultValue");

				SourceBean b = new SourceBean("ROW");
				b.setAttribute("NAME", name);
				b.setAttribute("TYPE", type);
				b.setAttribute("MULTIVALUE", multiValue);
				b.setAttribute(DataSetParametersList.DEFAULT_VALUE_XML, defaultValue);
				sb1.setAttribute(b);
			}
			sb.setAttribute(sb1);
			parametersString = sb.toXML(false);
		} catch (Throwable t) {
			if (t instanceof SpagoBIServiceException) {
				throw (SpagoBIServiceException) t;
			}
			throw new SpagoBIServiceException("ManageDatasets", "An unexpected error occured while deserializing dataset parameters", t);

		}
		return parametersString;
	}
}
