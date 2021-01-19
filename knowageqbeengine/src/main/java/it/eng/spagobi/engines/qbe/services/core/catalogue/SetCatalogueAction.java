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
package it.eng.spagobi.engines.qbe.services.core.catalogue;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.IQueryField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.QueryValidator;
import it.eng.qbe.query.TimeAggregationHandler;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.qbe.serializer.SerializationException;
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
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Commit all the modifications made to the catalogue on the client side
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class SetCatalogueAction extends AbstractQbeEngineAction {

	private static final long serialVersionUID = 4576121408010108119L;
	public static final String SERVICE_NAME = "SET_CATALOGUE_ACTION";

	@Override
	public String getActionName() {
		return SERVICE_NAME;
	}

	// INPUT PARAMETERS
	public static final String CATALOGUE = "catalogue";
	public static final String CURRENT_QUERY_ID = "currentQueryId";
	public static final String AMBIGUOUS_FIELDS_PATHS = "ambiguousFieldsPaths";
	public static final String AMBIGUOUS_ROLES = "ambiguousRoles";
	public static final String EXECUTE_DIRECTLY = "executeDirectly";
	public static final String AMBIGUOUS_WARING = "ambiguousWarinig";
	public static final String CATALOGUE_ERRORS = "catalogueErrors";
	public static final String QUERY_STRING = "queryString";
	public static final String PARS = "pars";

	public static final String MESSAGE = "message";
	public static final String MESSAGE_SAVE = "save";

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(SetCatalogueAction.class);

	protected boolean handleTimeFilter = true;

	@Override
	public void service(SourceBean request, SourceBean response) {

		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		String jsonEncodedCatalogue = null;
		JSONArray queries;
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

			super.service(request, response);

			// get current query and all the linked objects
			query = this.getCurrentQuery();
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
			JSONArray parameters = getAttributeAsJSONArray(PARS);
			addParameters(parameters);
			// get the cataologue from the request
			jsonEncodedCatalogue = getAttributeAsString(CATALOGUE);
			if (jsonEncodedCatalogue == null) {
				jsonEncodedCatalogue = getAttributeAsString("qbeJSONQuery");
				JSONObject jo = new JSONObject(jsonEncodedCatalogue);
				jo = jo.getJSONObject("catalogue");
				queries = jo.getJSONArray("queries");
			} else {
				queries = new JSONArray(jsonEncodedCatalogue);
			}

			logger.debug(CATALOGUE + " = [" + jsonEncodedCatalogue + "]");

			try {

				for (int i = 0; i < queries.length(); i++) {
					queryJSON = queries.getJSONObject(i);
					query = deserializeQuery(queryJSON);
					getEngineInstance().getQueryCatalogue().addQuery(query);
					getEngineInstance().resetActiveQuery();
				}

			} catch (SerializationException e) {
				String message = "Impossible to syncronize the query with the server. Query passed by the client is malformed";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

			query = this.getCurrentQuery();
			if (query == null) {
				query = this.getEngineInstance().getQueryCatalogue().getFirstQuery();
			} else {
				oldQueryGraph = null;
			}

			if (query.isEmpty()) {
				writeEmptyQueryMessageToClient();
				return;
			}

			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);

			// we create a new query adding filters defined by profile
			// attributes
			IModelAccessModality accessModality = this.getEngineInstance().getDataSource().getModelAccessModality();

			if (handleTimeFilter) {
				new TimeAggregationHandler(getDataSource()).handleTimeFilters(query);
			}

			Query filteredQuery = accessModality.getFilteredStatement(query, this.getDataSource(), userProfile.getUserAttributes());

			// loading the ambiguous fields
			Set<ModelFieldPaths> ambiguousFields = new HashSet<ModelFieldPaths>();
			Map<IModelField, Set<IQueryField>> modelFieldsMap = filteredQuery.getQueryFields(getDataSource());
			Set<IModelField> modelFields = modelFieldsMap.keySet();
			Set<IModelEntity> modelEntities = Query.getQueryEntities(modelFields);

			Map<String, Object> pathFiltersMap = new HashMap<String, Object>();
			pathFiltersMap.put(CubeFilter.PROPERTY_MODEL_STRUCTURE, getDataSource().getModelStructure());
			pathFiltersMap.put(CubeFilter.PROPERTY_ENTITIES, modelEntities);

			if (oldQueryGraph == null && filteredQuery != null) {// normal
																	// execution:
																	// a query
																	// exists
				queryGraph = updateQueryGraphInQuery(filteredQuery, forceReturnGraph, modelEntities);
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
				boolean valid = GraphManager.getGraphValidatorInstance(QbeEngineConfig.getInstance().getGraphValidatorImpl()).isValid(queryGraph,
						modelEntities);
				logger.debug("QueryGraph valid = " + valid);
				if (!valid) {
					throw new SpagoBIEngineServiceException(getActionName(), "error.mesage.description.relationship.not.enough");
				}
			}

			// we update the query graph in the query designed by the user in
			// order to save it for later executions
			query.setQueryGraph(filteredQuery.getQueryGraph());

			// serialize the ambiguous fields
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("deprecation")
			SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
			simpleModule.addSerializer(Relationship.class, new RelationJSONSerializer(getDataSource(), getLocale()));
			simpleModule.addSerializer(ModelObjectI18n.class, new ModelObjectInternationalizedSerializer(getDataSource(), getLocale()));
			mapper.registerModule(simpleModule);

			String serialized = this.getAttributeAsString(AMBIGUOUS_FIELDS_PATHS);
			if (ambiguousFields.size() > 0 || serialized == null) {
				serialized = mapper.writeValueAsString(ambiguousFields);
			}

			// update the roles in the query if exists ambiguous paths
			String serializedRoles = "";
			if (serialized.length() > 5) {
				serializedRoles = this.getAttributeAsString(AMBIGUOUS_ROLES);
				if (serializedRoles == null || serializedRoles.length() < 5) {
					serializedRoles = roleSelectionFromTheSavedQuery;
				}
				LogMF.debug(logger, AMBIGUOUS_ROLES + "is {0}", serialized);
				query.setRelationsRoles(serializedRoles);
				applySelectedRoles(serializedRoles, modelEntities, query);
			}

			// validate the response and create the list of warnings and errors
			if (!query.isAliasDefinedInSelectFields()) {
				ambiguousWarinig = "sbi.qbe.relationshipswizard.roles.validation.no.fields.alias";
			}

			if (!isDierctlyExecutable) {
				isDierctlyExecutable = serialized == null || serialized.equals("") || serialized.equals("[]");
				// no ambiguos fields found so the query is executable;
			}

			List<String> queryErrors = QueryValidator.validate(query, getDataSource());
			String serializedQueryErrors = mapper.writeValueAsString(queryErrors);

			// String queryString = buildQueryString(getDataSource(), query);
			if (handleTimeFilter) {
				JSONObject toReturn = new JSONObject();
				toReturn.put(AMBIGUOUS_FIELDS_PATHS, serialized);
				toReturn.put(AMBIGUOUS_ROLES, serializedRoles);
				toReturn.put(EXECUTE_DIRECTLY, isDierctlyExecutable);
				toReturn.put(AMBIGUOUS_WARING, ambiguousWarinig);
				toReturn.put(CATALOGUE_ERRORS, serializedQueryErrors);
				// toReturn.put(QUERY_STRING, queryString);

				try {
					writeBackToClient(toReturn.toString());
				} catch (IOException e) {
					String message = "Impossible to write back the responce to the client";
					throw new SpagoBIEngineServiceException(getActionName(), message, e);
				}
			} else {
				this.getEngineInstance().setActiveQuery(query);
			}

		} catch (Throwable t) {
			errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
			errorHitsMonitor.stop();
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			if (totalTimeMonitor != null)
				totalTimeMonitor.stop();
			logger.debug("OUT");
		}
	}

	private void writeEmptyQueryMessageToClient() throws JSONException {
		JSONObject toReturn = new JSONObject();
		toReturn.put(AMBIGUOUS_FIELDS_PATHS, "[]");
		toReturn.put(AMBIGUOUS_ROLES, "");
		toReturn.put(EXECUTE_DIRECTLY, true);
		toReturn.put(AMBIGUOUS_WARING, "");
		toReturn.put(CATALOGUE_ERRORS, "[]");
		try {
			writeBackToClient(toReturn.toString());
		} catch (IOException e) {
			String message = "Impossible to write back the responce to the client";
			throw new SpagoBIEngineServiceException(getActionName(), message, e);
		}
	}

	/**
	 * Get the graph from the request: - if exist: - checks it is valid for the query - if its valid update the graph in the query and return null - if its not
	 * valid calculate the default graph and update the graph in the query - if not exists calculate the default graph and update the graph in the query
	 *
	 * @param query
	 * @return
	 */
	public QueryGraph updateQueryGraphInQuery(Query query, boolean forceReturnGraph, Set<IModelEntity> modelEntities) {
		boolean isTheOldQueryGraphValid = false;
		logger.debug("IN");
		QueryGraph queryGraph = null;
		try {

			queryGraph = this.getQueryGraphFromRequest(query, modelEntities);

			if (queryGraph != null) {
				// check if the graph selected by the user is still valid
				isTheOldQueryGraphValid = isTheOldQueryGraphValid(queryGraph, query);
			}

			if (queryGraph == null || !isTheOldQueryGraphValid) {
				// calculate the default cover graph
				logger.debug("Calculating the default graph");
				IModelStructure modelStructure = getDataSource().getModelStructure();
				RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(getDataSource().getConfiguration().getModelName(), false);
				Graph<IModelEntity, Relationship> graph = rootEntitiesGraph.getRootEntitiesGraph();
				logger.debug("UndirectedGraph retrieved");

				Set<IModelEntity> entities = query.getQueryEntities(getDataSource());
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

	public void applySavedGraphPaths(QueryGraph queryGraph, Set<ModelFieldPaths> ambiguousFields) {

		PathInspector pi = new PathInspector(queryGraph, queryGraph.vertexSet());
		Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> paths = pi.getAllEntitiesPathsMap();
		(GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl())).applyDefault(paths, ambiguousFields);

	}

	public void applySelectedRoles(String serializedRoles, Set<IModelEntity> modelEntities, Query query) {
		cleanFieldsRolesMapInEntity(query);
		try {
			if (serializedRoles != null && !serializedRoles.trim().equals("{}") && !serializedRoles.trim().equals("[]") && !serializedRoles.trim().equals("")) {
				query.initFieldsRolesMapInEntity(getDataSource());
			}

		} catch (Exception e2) {
			logger.error("Error deserializing the list of roles of the entities", e2);
			throw new SpagoBIEngineRuntimeException("Error deserializing the list of roles of the entities", e2);
		}
	}

	public Set<ModelFieldPaths> getAmbiguousFields(Query query, Set<IModelEntity> modelEntities, Map<IModelField, Set<IQueryField>> modelFieldsMap) {
		logger.debug("IN");

		try {

			String modelName = getDataSource().getConfiguration().getModelName();

			Set<IModelField> modelFields = modelFieldsMap.keySet();

			Assert.assertNotNull(modelFields, "No field specified in teh query");
			Set<ModelFieldPaths> ambiguousModelField = new HashSet<ModelFieldPaths>();
			if (modelFields != null) {

				Graph<IModelEntity, Relationship> graph = getDataSource().getModelStructure().getRootEntitiesGraph(modelName, false).getRootEntitiesGraph();

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

	private QueryGraph getQueryGraphFromRequest(Query query, Set<IModelEntity> modelEntities) {
		List<Relationship> toReturn = new ArrayList<Relationship>();
		IModelStructure modelStructure = getDataSource().getModelStructure();
		logger.debug("IModelStructure retrieved");
		RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(getDataSource().getConfiguration().getModelName(), false);
		logger.debug("RootEntitiesGraph retrieved");

		Set<Relationship> relationships = rootEntitiesGraph.getRelationships();
		logger.debug("Set<Relationship> retrieved");
		String serialized = this.getAttributeAsString(AMBIGUOUS_FIELDS_PATHS);
		LogMF.debug(logger, AMBIGUOUS_FIELDS_PATHS + "is {0}", serialized);

		List<ModelFieldPaths> list = null;
		if (StringUtilities.isNotEmpty(serialized)) {
			try {
				list = deserializeList(serialized, relationships, modelStructure, query);
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

	public static void cleanFieldsRolesMapInEntity(Query query) {
		if (query != null) {
			query.setMapEntityRoleField(null);
		}
	}

	/**
	 * checks if the query graph covers all the entities in the query
	 *
	 * @param oldQueryGraph
	 * @param newQuery
	 * @return
	 */
	public boolean isTheOldQueryGraphValid(QueryGraph oldQueryGraph, Query newQuery) {

		if (oldQueryGraph == null) {
			return false;
		}

		Set<IModelEntity> oldVertexes = oldQueryGraph.vertexSet();
		if (oldVertexes == null) {
			return false;
		}
		Set<IModelEntity> newQueryEntities = newQuery.getQueryEntities(getDataSource());
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

	private Query getCurrentQuery() {
		String queryId = this.getAttributeAsString(CURRENT_QUERY_ID);
		Query query = null;
		if (StringUtilities.isNotEmpty(queryId)) {
			query = this.getEngineInstance().getQueryCatalogue().getQuery(queryId);
		}
		return query;
	}

	private Query deserializeQuery(JSONObject queryJSON) throws SerializationException, JSONException {
		// queryJSON.put("expression", queryJSON.get("filterExpression"));
		return SerializerFactory.getDeserializer("application/json").deserializeQuery(queryJSON.toString(), getEngineInstance().getDataSource());
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

	/*
	 * TODO method copied from AbstractStatementFilteringClause TO BE GENERALIZED
	 *
	 */
	public String getValueBounded(String operandValueToBound, String operandType) {

		String boundedValue = operandValueToBound;
		if (operandType.equalsIgnoreCase("STRING") || operandType.equalsIgnoreCase("CHARACTER") || operandType.equalsIgnoreCase("java.lang.String")
				|| operandType.equalsIgnoreCase("java.lang.Character")) {

			// if the value is already surrounded by quotes, does not neither
			// add quotes nor escape quotes
			if (StringUtils.isBounded(operandValueToBound, "'")) {
				boundedValue = operandValueToBound;
			} else {
				operandValueToBound = StringUtils.escapeQuotes(operandValueToBound);
				return StringUtils.bound(operandValueToBound, "'");
			}
		} else if (operandType.equalsIgnoreCase("DATE") || operandType.equalsIgnoreCase("java.sql.date") || operandType.equalsIgnoreCase("java.util.date")) {
			boundedValue = parseDate(operandValueToBound);
		} else if (operandType.equalsIgnoreCase("TIMESTAMP") || operandType.equalsIgnoreCase("java.sql.TIMESTAMP")) {
			boundedValue = parseTimestamp(operandValueToBound);
		}

		return boundedValue;
	}

	/*
	 * TODO method copied from AbstractStatementFilteringClause TO BE GENERALIZED
	 *
	 */
	protected String parseDate(String date) {
		if (date == null || date.equals("")) {
			return "";
		}

		String toReturn = date;

		it.eng.spagobi.tools.datasource.bo.IDataSource connection = (it.eng.spagobi.tools.datasource.bo.IDataSource) this.getEngineInstance().getDataSource()
				.getConfiguration().loadDataSourceProperties().get("datasource");

		String dialect = connection.getHibDialectClass();

		if (dialect != null) {

			if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_MYSQL)
					|| dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_MYSQL_INNODB)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = " STR_TO_DATE(" + toReturn + ",'%d/%m/%Y %H:%i:%s') ";
				} else {
					toReturn = " STR_TO_DATE('" + toReturn + "','%d/%m/%Y %H:%i:%s') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_HSQL)) {
				try {
					DateFormat daf;
					if (StringUtils.isBounded(toReturn, "'")) {
						daf = new SimpleDateFormat("'dd/MM/yyyy HH:mm:SS'");
					} else {
						daf = new SimpleDateFormat("dd/MM/yyyy HH:mm:SS");
					}

					Date myDate = daf.parse(toReturn);
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					toReturn = "'" + df.format(myDate) + "'";

				} catch (Exception e) {
					toReturn = "'" + toReturn + "'";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_INGRES)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = " STR_TO_DATE(" + toReturn + ",'%d/%m/%Y') ";
				} else {
					toReturn = " STR_TO_DATE('" + toReturn + "','%d/%m/%Y') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE)
					|| dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE9i10g)
					|| dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE_SPATIAL)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = " TO_DATE(" + toReturn + ",'DD/MM/YYYY HH24:MI:SS') ";
				} else {
					toReturn = " TO_DATE('" + toReturn + "','DD/MM/YYYY HH24:MI:SS') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_POSTGRES)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = " TO_DATE(" + toReturn + ",'DD/MM/YYYY HH24:MI:SS') ";
				} else {
					toReturn = " TO_DATE('" + toReturn + "','DD/MM/YYYY HH24:MI:SS') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_SQLSERVER)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = "" + toReturn + "";
				} else {
					toReturn = "'" + toReturn + "'";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_TERADATA)) {
				/*
				 * Unfortunately we cannot use neither CAST(" + dateStr + " AS DATE FORMAT 'dd/mm/yyyy') nor CAST((" + dateStr + " (Date,Format 'dd/mm/yyyy'))
				 * As Date) because Hibernate does not recognize (and validate) those SQL functions. Therefore we must use a predefined date format
				 * (yyyy-MM-dd).
				 */
				try {
					DateFormat dateFormat;
					if (StringUtils.isBounded(toReturn, "'")) {
						dateFormat = new SimpleDateFormat("'dd/MM/yyyy'");
					} else {
						dateFormat = new SimpleDateFormat("dd/MM/yyyy");
					}
					Date myDate = dateFormat.parse(toReturn);
					dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					toReturn = "'" + dateFormat.format(myDate) + "'";
				} catch (Exception e) {
					logger.error("Error parsing the date " + toReturn, e);
					throw new SpagoBIRuntimeException("Error parsing the date " + toReturn + ".");
				}
			}
		}

		return toReturn;
	}

	/*
	 * TODO metodo copiato da: AbstractStatementClause
	 *
	 * STANDARDIZZARE
	 *
	 */
	protected String parseTimestamp(String date) {
		if (date == null || date.equals("")) {
			return "";
		}

		String toReturn = date;

		it.eng.spagobi.tools.datasource.bo.IDataSource connection = (it.eng.spagobi.tools.datasource.bo.IDataSource) this.getEngineInstance().getDataSource()
				.getConfiguration().loadDataSourceProperties().get("datasource");

		String dialect = connection.getHibDialectClass();

		if (dialect != null) {

			if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_MYSQL)
					|| dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_MYSQL_INNODB)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = " STR_TO_DATE(" + toReturn + ",'%d/%m/%Y %H:%i:%s') ";
				} else {
					toReturn = " STR_TO_DATE('" + toReturn + "','%d/%m/%Y %H:%i:%s') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_HSQL)) {
				try {
					DateFormat daf;
					if (StringUtils.isBounded(toReturn, "'")) {
						daf = new SimpleDateFormat("'dd/MM/yyyy HH:mm:SS'");
					} else {
						daf = new SimpleDateFormat("dd/MM/yyyy HH:mm:SS");
					}

					Date myDate = daf.parse(toReturn);
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					toReturn = "'" + df.format(myDate) + "'";

				} catch (Exception e) {
					toReturn = "'" + toReturn + "'";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_INGRES)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = " STR_TO_DATE(" + toReturn + ",'%d/%m/%Y') ";
				} else {
					toReturn = " STR_TO_DATE('" + toReturn + "','%d/%m/%Y') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE)
					|| dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE9i10g)
					|| dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE_SPATIAL)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + toReturn + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + toReturn + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_POSTGRES)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + toReturn + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + toReturn + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_SQLSERVER)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = "" + toReturn + "";
				} else {
					toReturn = "'" + toReturn + "'";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_TERADATA)) {
				/*
				 * Unfortunately we cannot use neither CAST(" + dateStr + " AS DATE FORMAT 'dd/mm/yyyy') nor CAST((" + dateStr + " (Date,Format 'dd/mm/yyyy'))
				 * As Date) because Hibernate does not recognize (and validate) those SQL functions. Therefore we must use a predefined date format
				 * (yyyy-MM-dd).
				 */
				try {
					DateFormat dateFormat;
					if (StringUtils.isBounded(toReturn, "'")) {
						dateFormat = new SimpleDateFormat("'dd/MM/yyyy'");
					} else {
						dateFormat = new SimpleDateFormat("dd/MM/yyyy");
					}
					Date myDate = dateFormat.parse(toReturn);
					dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					toReturn = "'" + dateFormat.format(myDate) + "'";
				} catch (Exception e) {
					logger.error("Error parsing the date " + toReturn, e);
					throw new SpagoBIRuntimeException("Error parsing the date " + toReturn + ".");
				}
			}
		}

		return toReturn;
	}

}
