package it.eng.spagobi.engines.qbe.api;

import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.IQueryField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.QueryValidator;
import it.eng.qbe.query.TimeAggregationHandler;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.qbe.statement.graph.GraphManager;
import it.eng.qbe.statement.graph.GraphUtilities;
import it.eng.qbe.statement.graph.ModelFieldPaths;
import it.eng.qbe.statement.graph.PathInspector;
import it.eng.qbe.statement.graph.QueryGraphBuilder;
import it.eng.qbe.statement.graph.bean.ModelObjectI18n;
import it.eng.qbe.statement.graph.bean.PathChoice;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;
import it.eng.qbe.statement.graph.filter.CubeFilter;
import it.eng.qbe.statement.graph.serializer.FieldNotAttendInTheQuery;
import it.eng.qbe.statement.graph.serializer.ModelFieldPathsJSONDeserializer;
import it.eng.qbe.statement.graph.serializer.ModelObjectInternationalizedSerializer;
import it.eng.qbe.statement.graph.serializer.RelationJSONSerializer;
import it.eng.qbe.statement.hibernate.HQLDataSet;
import it.eng.qbe.statement.jpa.JPQLDataSet;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Path("/qbequery")
@ManageAuthorization
public class QbeQueryResource extends AbstractQbeEngineResource {

	public static transient Logger logger = Logger.getLogger(QbeQueryResource.class);
	public static transient Logger auditlogger = Logger.getLogger("audit.query");
	protected boolean handleTimeFilter = true;

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String proba() throws Exception {

		String message;
		JSONObject json = new JSONObject();
		Query query = deserializeQuery(json);
		json.put("test1", "value1");
		JSONObject jsonObj = new JSONObject();

		jsonObj.put("id", 0);
		jsonObj.put("name", "testName");
		json.put("test2", jsonObj);

		message = json.toString();

		return message;
	}

	@POST
	@Path("/setQueryCatalog")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setQueryCatalog(@QueryParam("currentQueryId") String currentQueryId, @QueryParam("ambiguousFieldsPaths") String ambiguousFieldsPaths,
			@QueryParam("ambiguousRoles") String ambiguousRoles, @javax.ws.rs.core.Context HttpServletRequest req) {

		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		JSONArray jsonEncodedCatalogue = null;
		JSONArray catalogue;
		JSONObject queryJSON;
		Query query;
		QueryGraph oldQueryGraph = null;
		String roleSelectionFromTheSavedQuery = null;
		boolean isDierctlyExecutable = false;
		QueryGraph queryGraph = null; // the query graph (the graph that
										// involves all the entities of the
										// query)
		String ambiguousWarinig = null;
		boolean forceReturnGraph = false;

		logger.debug("IN");

		try {

			totalTimeMonitor = MonitorFactory.start("QbeEngine.setCatalogueAction.totalTime");

			// get current query and all the linked objects
			query = getCurrentQuery(currentQueryId);
			if (query == null) {
				// the qbe is new
				query = this.getEngineInstance().getQueryCatalogue().getFirstQuery();
				oldQueryGraph = query.getQueryGraph();
				roleSelectionFromTheSavedQuery = query.getRelationsRoles();
				logger.debug("The query is already defined in the catalogue");
				if (roleSelectionFromTheSavedQuery != null) {
					logger.debug("The previous roleSelection is " + roleSelectionFromTheSavedQuery);
				}
				if (oldQueryGraph != null) {
					logger.debug("The previous oldQueryGraph is " + oldQueryGraph);
				}
			}

			// get the cataologue from the request
			jsonEncodedCatalogue = RestUtilities.readBodyAsJSONArray(req);

			logger.debug("catalogue" + " = [" + jsonEncodedCatalogue + "]");

			try {

				for (int i = 0; i < jsonEncodedCatalogue.length(); i++) {
					queryJSON = jsonEncodedCatalogue.getJSONObject(i);
					query = deserializeQuery(queryJSON);
					getEngineInstance().getQueryCatalogue().addQuery(query);
					getEngineInstance().resetActiveQuery();
				}

			} catch (SerializationException e) {
				String message = "Impossible to deserialize query";
				throw new SpagoBIEngineServiceException("DESERIALIZATING QUERY", message, e);
			}

			query = getCurrentQuery(currentQueryId);
			if (query == null) {
				query = this.getEngineInstance().getQueryCatalogue().getFirstQuery();
			} else {
				oldQueryGraph = null;
			}

			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);

			// we create a new query adding filters defined by profile
			// attributes
			IModelAccessModality accessModality = this.getEngineInstance().getDataSource().getModelAccessModality();

			if (handleTimeFilter) {
				new TimeAggregationHandler(getEngineInstance().getDataSource()).handleTimeFilters(query);
			}

			Query filteredQuery = accessModality.getFilteredStatement(query, this.getEngineInstance().getDataSource(), userProfile.getUserAttributes());

			// loading the ambiguous fields
			Set<ModelFieldPaths> ambiguousFields = new HashSet<ModelFieldPaths>();
			Map<IModelField, Set<IQueryField>> modelFieldsMap = filteredQuery.getQueryFields(getEngineInstance().getDataSource());
			Set<IModelField> modelFields = modelFieldsMap.keySet();
			Set<IModelEntity> modelEntities = Query.getQueryEntities(modelFields);

			Map<String, Object> pathFiltersMap = new HashMap<String, Object>();
			pathFiltersMap.put(CubeFilter.PROPERTY_MODEL_STRUCTURE, getEngineInstance().getDataSource().getModelStructure());
			pathFiltersMap.put(CubeFilter.PROPERTY_ENTITIES, modelEntities);

			if (oldQueryGraph == null && filteredQuery != null) {// normal
																	// execution:
																	// a query
																	// exists
				queryGraph = updateQueryGraphInQuery(filteredQuery, forceReturnGraph, modelEntities, ambiguousFieldsPaths);
				if (queryGraph != null) {
					// String modelName =
					// getDataSource().getConfiguration().getModelName();
					// Graph<IModelEntity, Relationship> graph =
					// getDataSource().getModelStructure().getRootEntitiesGraph(modelName,
					// false).getRootEntitiesGraph();
					ambiguousFields = getAmbiguousFields(filteredQuery, modelEntities, modelFieldsMap);
					// filter paths
					GraphManager.filterPaths(ambiguousFields, pathFiltersMap, (QbeEngineConfig.getInstance().getPathsFiltersImpl()));

					boolean removeSubPaths = QbeEngineConfig.getInstance().isRemoveSubpaths();
					if (removeSubPaths) {
						String orderDirection = QbeEngineConfig.getInstance().getPathsOrder();
						GraphUtilities.cleanSubPaths(ambiguousFields, orderDirection);
					}

					GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl()).applyDefault(ambiguousFields, queryGraph,
							modelEntities);
					isDierctlyExecutable = GraphManager.isDirectlyExecutable(modelEntities, queryGraph);
				} else {
					// no ambigous fields found
					isDierctlyExecutable = true;
				}
			} else {// saved query
				ambiguousFields = getAmbiguousFields(filteredQuery, modelEntities, modelFieldsMap);
				// filter paths
				GraphManager.filterPaths(ambiguousFields, pathFiltersMap, (QbeEngineConfig.getInstance().getPathsFiltersImpl()));
				applySavedGraphPaths(oldQueryGraph, ambiguousFields);
				queryGraph = oldQueryGraph;
			}

			if (queryGraph != null) {
				boolean valid = GraphManager.getGraphValidatorInstance(QbeEngineConfig.getInstance().getGraphValidatorImpl())
						.isValid(queryGraph, modelEntities);
				logger.debug("QueryGraph valid = " + valid);
				if (!valid) {
					throw new SpagoBIEngineServiceException("RELATIONS", "error.mesage.description.relationship.not.enough");
				}
			}

			// we update the query graph in the query designed by the user in
			// order to save it for later executions
			query.setQueryGraph(filteredQuery.getQueryGraph());

			// serialize the ambiguous fields
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("deprecation")
			SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
			simpleModule.addSerializer(Relationship.class, new RelationJSONSerializer(getEngineInstance().getDataSource(), getLocale()));
			simpleModule.addSerializer(ModelObjectI18n.class, new ModelObjectInternationalizedSerializer(getEngineInstance().getDataSource(), getLocale()));
			mapper.registerModule(simpleModule);

			if (ambiguousFields.size() > 0 || ambiguousFieldsPaths == null) {
				ambiguousFieldsPaths = mapper.writeValueAsString(ambiguousFields);
			}

			// update the roles in the query if exists ambiguous paths
			String serializedRoles = "";
			if (ambiguousFieldsPaths.length() > 5) {
				serializedRoles = ambiguousRoles;
				if (serializedRoles == null || serializedRoles.length() < 5) {
					serializedRoles = roleSelectionFromTheSavedQuery;
				}
				LogMF.debug(logger, ambiguousRoles + "is {0}", ambiguousFieldsPaths);
				query.setRelationsRoles(serializedRoles);
				applySelectedRoles(serializedRoles, modelEntities, query);
			}

			// validate the response and create the list of warnings and errors
			if (!query.isAliasDefinedInSelectFields()) {
				ambiguousWarinig = "sbi.qbe.relationshipswizard.roles.validation.no.fields.alias";
			}

			if (!isDierctlyExecutable) {
				isDierctlyExecutable = ambiguousFieldsPaths == null || ambiguousFieldsPaths.equals("") || ambiguousFieldsPaths.equals("[]");
				// no ambiguos fields found so the query is executable;
			}

			List<String> queryErrors = QueryValidator.validate(query, getEngineInstance().getDataSource());
			String serializedQueryErrors = mapper.writeValueAsString(queryErrors);
			JSONObject toReturn = new JSONObject();
			// String queryString = buildQueryString(getDataSource(), query);
			if (handleTimeFilter) {

				toReturn.put("ambiguousFieldsPaths", ambiguousFieldsPaths);
				toReturn.put("serializedRoles", serializedRoles);
				toReturn.put("executeDirectly", isDierctlyExecutable);
				toReturn.put("ambiguousWarinig", ambiguousWarinig);
				toReturn.put("catalogueErrors", serializedQueryErrors);

				// toReturn.put(QUERY_STRING, queryString);

			} else {
				this.getEngineInstance().setActiveQuery(query);
			}

			return Response.ok(toReturn.toString()).build();
		} catch (Throwable t) {
			errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
			errorHitsMonitor.stop();
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("setQueryCatalog", getEngineInstance(), t);
		} finally {
			if (totalTimeMonitor != null)
				totalTimeMonitor.stop();
			logger.debug("OUT");
		}

	}

	public static void cleanFieldsRolesMapInEntity(Query query) {
		if (query != null) {
			query.setMapEntityRoleField(null);
		}
	}

	public void applySelectedRoles(String serializedRoles, Set<IModelEntity> modelEntities, Query query) {
		cleanFieldsRolesMapInEntity(query);
		try {
			if (serializedRoles != null && !serializedRoles.trim().equals("{}") && !serializedRoles.trim().equals("[]") && !serializedRoles.trim().equals("")) {
				query.initFieldsRolesMapInEntity(getEngineInstance().getDataSource());
			}

		} catch (Exception e2) {
			logger.error("Error deserializing the list of roles of the entities", e2);
			throw new SpagoBIEngineRuntimeException("Error deserializing the list of roles of the entities", e2);
		}
	}

	public void applySavedGraphPaths(QueryGraph queryGraph, Set<ModelFieldPaths> ambiguousFields) {

		PathInspector pi = new PathInspector(queryGraph, queryGraph.vertexSet());
		Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> paths = pi.getAllEntitiesPathsMap();
		(GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl())).applyDefault(paths, ambiguousFields);

	}

	public Set<ModelFieldPaths> getAmbiguousFields(Query query, Set<IModelEntity> modelEntities, Map<IModelField, Set<IQueryField>> modelFieldsMap) {
		logger.debug("IN");

		try {

			String modelName = getEngineInstance().getDataSource().getConfiguration().getModelName();

			Set<IModelField> modelFields = modelFieldsMap.keySet();

			Assert.assertNotNull(modelFields, "No field specified in teh query");
			Set<ModelFieldPaths> ambiguousModelField = new HashSet<ModelFieldPaths>();
			if (modelFields != null) {

				Graph<IModelEntity, Relationship> graph = getEngineInstance().getDataSource().getModelStructure().getRootEntitiesGraph(modelName, false)
						.getRootEntitiesGraph();

				PathInspector pathInspector = new PathInspector(graph, modelEntities);
				Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> ambiguousMap = pathInspector.getAmbiguousEntitiesAllPathsMap();

				Iterator<IModelField> modelFieldsIter = modelFields.iterator();

				while (modelFieldsIter.hasNext()) {
					IModelField iModelField = modelFieldsIter.next();
					IModelEntity me = iModelField.getParent();
					Set<GraphPath<IModelEntity, Relationship>> paths = ambiguousMap.get(me);
					if (paths != null) {
						Set<IQueryField> queryFields = modelFieldsMap.get(iModelField);
						if (queryFields != null) {
							Iterator<IQueryField> queryFieldsIter = queryFields.iterator();
							while (queryFieldsIter.hasNext()) {
								ambiguousModelField.add(new ModelFieldPaths(queryFieldsIter.next(), iModelField, paths));
							}
						}
					}
				}
			}

			return ambiguousModelField;

		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Error while getting ambiguous fields", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private Query getCurrentQuery(String currentQueryId) {
		Query query = null;
		if (StringUtilities.isNotEmpty(currentQueryId)) {
			query = this.getEngineInstance().getQueryCatalogue().getQuery(currentQueryId);
		}
		return query;
	}

	public QueryGraph updateQueryGraphInQuery(Query query, boolean forceReturnGraph, Set<IModelEntity> modelEntities, String ambiguousFieldsPaths) {
		boolean isTheOldQueryGraphValid = false;
		logger.debug("IN");
		QueryGraph queryGraph = null;
		try {

			queryGraph = this.getQueryGraphFromRequest(query, modelEntities, ambiguousFieldsPaths);

			if (queryGraph != null) {
				// check if the graph selected by the user is still valid
				isTheOldQueryGraphValid = isTheOldQueryGraphValid(queryGraph, query);
			}

			if (queryGraph == null || !isTheOldQueryGraphValid) {
				// calculate the default cover graph
				logger.debug("Calculating the default graph");
				IModelStructure modelStructure = getEngineInstance().getDataSource().getModelStructure();
				RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(
						getEngineInstance().getDataSource().getConfiguration().getModelName(), false);
				Graph<IModelEntity, Relationship> graph = rootEntitiesGraph.getRootEntitiesGraph();
				logger.debug("UndirectedGraph retrieved");

				Set<IModelEntity> entities = query.getQueryEntities(getEngineInstance().getDataSource());
				if (entities.size() > 0) {
					queryGraph = GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl()).getCoverGraph(graph, entities);
				}
			} else {
				query.setQueryGraph(queryGraph);
				return null;
			}

			query.setQueryGraph(queryGraph);
			return queryGraph;

		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Error while loading the not ambigous graph", t);
		} finally {
			logger.debug("OUT");
		}

	}

	public boolean isTheOldQueryGraphValid(QueryGraph oldQueryGraph, Query newQuery) {

		if (oldQueryGraph == null) {
			return false;
		}

		Set<IModelEntity> oldVertexes = oldQueryGraph.vertexSet();
		if (oldVertexes == null) {
			return false;
		}
		Set<IModelEntity> newQueryEntities = newQuery.getQueryEntities(getEngineInstance().getDataSource());
		if (newQueryEntities == null) {
			return true;
		}

		Iterator<IModelEntity> newQueryEntitiesIter = newQueryEntities.iterator();
		while (newQueryEntitiesIter.hasNext()) {
			IModelEntity iModelEntity = newQueryEntitiesIter.next();
			if (!oldVertexes.contains(iModelEntity)) {
				return false;// if at least one entity contained in the query is
								// not covered by the old cover graph the old
								// graph is not valid
			}
		}
		return true;
	}

	private QueryGraph getQueryGraphFromRequest(Query query, Set<IModelEntity> modelEntities, String ambiguousFieldsPaths) {
		List<Relationship> toReturn = new ArrayList<Relationship>();
		IModelStructure modelStructure = getEngineInstance().getDataSource().getModelStructure();
		logger.debug("IModelStructure retrieved");
		RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(getEngineInstance().getDataSource().getConfiguration().getModelName(), false);
		logger.debug("RootEntitiesGraph retrieved");

		Set<Relationship> relationships = rootEntitiesGraph.getRelationships();
		logger.debug("Set<Relationship> retrieved");

		LogMF.debug(logger, ambiguousFieldsPaths + "is {0}", ambiguousFieldsPaths);

		List<ModelFieldPaths> list = null;
		if (StringUtilities.isNotEmpty(ambiguousFieldsPaths)) {
			try {
				list = deserializeList(ambiguousFieldsPaths, relationships, modelStructure, query);
			} catch (FieldNotAttendInTheQuery e1) {
				logger.debug("The query has been updated and in the previous ambiguos paths selection there is some field don't exist in teh query");
				return null;
			} catch (SerializationException e) {
				throw new SpagoBIEngineRuntimeException("Error while deserializing list of relationships", e);
			}
			logger.debug("Paths deserialized");
		}
		QueryGraph queryGraph = null;

		if (list != null && !list.isEmpty()) {
			Iterator<ModelFieldPaths> it = list.iterator();
			while (it.hasNext()) {
				ModelFieldPaths modelFieldPaths = it.next();
				if (modelFieldPaths != null) {
					Set<PathChoice> set = modelFieldPaths.getChoices();
					Iterator<PathChoice> pathChoiceIterator = set.iterator();
					while (pathChoiceIterator.hasNext()) {
						PathChoice choice = pathChoiceIterator.next();
						toReturn.addAll(choice.getRelations());
					}
				}
			}
			QueryGraphBuilder builder = new QueryGraphBuilder();
			queryGraph = builder.buildGraphFromEdges(toReturn);
		}
		logger.debug("QueryGraph created");
		return queryGraph;
	}

	public static List<ModelFieldPaths> deserializeList(String serialized, Collection<Relationship> relationShips, IModelStructure modelStructure, Query query)
			throws SerializationException {
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("deprecation")
		SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
		simpleModule.addDeserializer(ModelFieldPaths.class, new ModelFieldPathsJSONDeserializer(relationShips, modelStructure, query));
		mapper.registerModule(simpleModule);
		TypeReference<List<ModelFieldPaths>> type = new TypeReference<List<ModelFieldPaths>>() {
		};
		try {
			return mapper.readValue(serialized, type);
		} catch (Exception e) {
			throw new SerializationException("Error deserializing the list of ModelFieldPaths", e);
		}
	}

	@POST
	@Path("/executeQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response executeQuery(@QueryParam("start") String startS, @QueryParam("limit") String limitS, @QueryParam("id") String id,
			@QueryParam("promptableFilters") String promptableFilters) {

		Integer limit = null;
		Integer start = null;
		Integer maxSize = null;
		IDataStore dataStore = null;

		Query query = null;

		Integer resultNumber = null;
		JSONObject gridDataFeed = new JSONObject();

		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		logger.debug("IN");

		try {
			totalTimeMonitor = MonitorFactory.start("QbeEngine.executeQueryAction.totalTime");
			if (startS != null && !startS.equals("")) {
				start = Integer.parseInt(startS);
			}

			if (limitS != null && !limitS.equals("")) {
				limit = Integer.parseInt(limitS);
			}

			query = getQuery(id);
			if (getEngineInstance().getActiveQuery() == null || !getEngineInstance().getActiveQuery().getId().equals(query.getId())) {
				logger.debug("Query with id [" + query.getId() + "] is not the current active query. A new statment will be generated");
				getEngineInstance().setActiveQuery(query);
			}
			updatePromptableFiltersValue(query, promptableFilters);

			Map<String, Map<String, String>> inlineFilteredSelectFields = query.getInlineFilteredSelectFields();

			boolean thereAreInlineTemporalFilters = inlineFilteredSelectFields != null && inlineFilteredSelectFields.size() > 0;
			if (thereAreInlineTemporalFilters) {
				limit = 0;
			}

			logger.debug("Parameter [" + "limit" + "] is equals to [" + limit + "]");

			dataStore = executeQuery(start, limit, query);
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

	public JSONObject serializeDataStore(IDataStore dataStore) {
		JSONDataWriter dataSetWriter = new JSONDataWriter();
		JSONObject gridDataFeed = (JSONObject) dataSetWriter.write(dataStore);
		return gridDataFeed;
	}

	public Query getQuery(String id) {
		Query query = getEngineInstance().getQueryCatalogue().getQuery(id);
		return query;
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

}
