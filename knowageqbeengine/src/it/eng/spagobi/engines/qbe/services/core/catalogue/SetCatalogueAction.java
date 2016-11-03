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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.model.structure.HierarchicalDimensionField;
import it.eng.qbe.model.structure.Hierarchy;
import it.eng.qbe.model.structure.HierarchyLevel;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.ExpressionNode;
import it.eng.qbe.query.IQueryField;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.QueryValidator;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.WhereField.Operand;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.statement.AbstractQbeDataSet;
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
import it.eng.qbe.utility.TemporalRecord;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
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

			// get the cataologue from the request
			jsonEncodedCatalogue = getAttributeAsString(CATALOGUE);
			logger.debug(CATALOGUE + " = [" + jsonEncodedCatalogue + "]");

			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName()
					+ " service before having properly created an instance of EngineInstance class");
			Assert.assertNotNull(jsonEncodedCatalogue, "Input parameter [" + CATALOGUE
					+ "] cannot be null in oder to execute " + this.getActionName() + " service");

			try {
				queries = new JSONArray(jsonEncodedCatalogue);
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

			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);

			// we create a new query adding filters defined by profile
			// attributes
			IModelAccessModality accessModality = this.getEngineInstance().getDataSource().getModelAccessModality();

			if(handleTimeFilter) {
				handleTimeFilters(query);
			}

			Query filteredQuery = accessModality.getFilteredStatement(query, this.getDataSource(),
					userProfile.getUserAttributes());

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
					GraphManager.filterPaths(ambiguousFields, pathFiltersMap,
							(QbeEngineConfig.getInstance().getPathsFiltersImpl()));

					boolean removeSubPaths = QbeEngineConfig.getInstance().isRemoveSubpaths();
					if (removeSubPaths) {
						String orderDirection = QbeEngineConfig.getInstance().getPathsOrder();
						GraphUtilities.cleanSubPaths(ambiguousFields, orderDirection);
					}

					GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl())
							.applyDefault(ambiguousFields, queryGraph, modelEntities);
					isDierctlyExecutable = GraphManager.isDirectlyExecutable(modelEntities, queryGraph);
				} else {
					// no ambigous fields found
					isDierctlyExecutable = true;
				}
			} else {// saved query
				ambiguousFields = getAmbiguousFields(filteredQuery, modelEntities, modelFieldsMap);
				// filter paths
				GraphManager.filterPaths(ambiguousFields, pathFiltersMap,
						(QbeEngineConfig.getInstance().getPathsFiltersImpl()));
				applySavedGraphPaths(oldQueryGraph, ambiguousFields);
				queryGraph = oldQueryGraph;
			}

			if (queryGraph != null) {
				boolean valid = GraphManager
						.getGraphValidatorInstance(QbeEngineConfig.getInstance().getGraphValidatorImpl())
						.isValid(queryGraph, modelEntities);
				logger.debug("QueryGraph valid = " + valid);
				if (!valid) {
					throw new SpagoBIEngineServiceException(getActionName(),
							"error.mesage.description.relationship.not.enough");
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
			simpleModule.addSerializer(ModelObjectI18n.class,
					new ModelObjectInternationalizedSerializer(getDataSource(), getLocale()));
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
			if(handleTimeFilter) {
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
			}
			else {
				this.getEngineInstance().setActiveQuery(query);
			}

		} catch (Throwable t) {
			errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
			errorHitsMonitor.stop();
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(),
					getEngineInstance(), t);
		} finally {
			if (totalTimeMonitor != null)
				totalTimeMonitor.stop();
			logger.debug("OUT");
		}
	}

	private void handleTimeFilters(Query query) {
		// DD: retrieve time dimension
		IModelEntity temporalDimension = getTemporalDimension(getDataSource());
		IModelEntity timeDimension = getTimeDimension(getDataSource());

		if (temporalDimension != null) {
			HierarchicalDimensionField hierarchicalDimensionByEntity = temporalDimension.getHierarchicalDimensionByEntity(temporalDimension.getType());
			if (hierarchicalDimensionByEntity == null) {
				throw new SpagoBIRuntimeException("Temporal dimension hierarchy is [null]");
			}
			Hierarchy defaultHierarchy = hierarchicalDimensionByEntity.getDefaultHierarchy();

			List<Integer> whereFieldsIndexesToRemove = new LinkedList<Integer>();
			List<WhereField> whereFieldsToAdd = new LinkedList<WhereField>();
			List<WhereField> whereFields = query.getWhereFields();
			List<String> nodesToAdd = new LinkedList<String>();

			int timeFilterIndex = 0;
			int whereFieldIndex = 0;
			for (WhereField whereField : whereFields) {
				String[] lValues = whereField.getLeftOperand().values;
				String[] rValues = whereField.getRightOperand().values;

				if (lValues != null && lValues.length > 0 && rValues != null && rValues.length > 0) {
					if (QuerySerializationConstants.TEMPORAL.equals(lValues[0])) {

						whereField.setDescription(QuerySerializationConstants.TEMPORAL);

						String temporalLevelColumn = null;
						String temporalLevel = whereField.getLeftOperand().description;
						temporalLevelColumn = defaultHierarchy.getLevelByType(temporalLevel);

						String temporalDimensionId = getTimeId(temporalDimension);

						// DD: retrieve current period
						TemporalRecord currentPeriod = getCurrentPeriod(temporalDimension, temporalDimensionId,
								temporalLevelColumn, null, defaultHierarchy.getAncestors(temporalLevelColumn));
						// DD: retrieve current period index

						if ("Current".equals(rValues[0])) {
							lValues[0] = temporalDimension.getType() + ":" + temporalLevelColumn;
							rValues[0] = currentPeriod.getPeriod().toString();
						} else if (whereField.getOperator().equals("LAST")) {

							// DD: retrieving all periods start time records
							LinkedList<TemporalRecord> allPeriodsStartingDate = loadAllPeriodsStartingDate(
									temporalDimension, temporalDimensionId, temporalLevelColumn,
									defaultHierarchy.getAncestors(temporalLevelColumn));

							int currentPeriodIndex = getCurrentIndex(allPeriodsStartingDate,
									(Integer) currentPeriod.getId());

							whereFieldsIndexesToRemove.add(whereFieldIndex);

							timeFilterIndex++;

							Operand left = new Operand(
									new String[] { temporalDimension.getType() + ":" + temporalDimensionId },
									temporalDimension.getName() + ":" + temporalDimensionId, "Field Content",
									new String[] {""}, new String[] {""}, "");
							Operand maxRight = new Operand(new String[] { currentPeriod.getId().toString() },
									currentPeriod.getId().toString(), "Static Content", new String[] {""}, new String[] {""}, "");

							String maxFilterId = "TimeFilterMax" + timeFilterIndex;
							WhereField maxWhereField = new WhereField(maxFilterId, maxFilterId, false, left,
									"EQUALS OR LESS THAN", maxRight, "AND");
							nodesToAdd.add(maxFilterId);

							int offset = Integer.parseInt(rValues[0]);
							int oldestPeriodIndex = currentPeriodIndex - offset > 0 ? currentPeriodIndex - offset : 0;
							TemporalRecord oldestPeriod = allPeriodsStartingDate.get(oldestPeriodIndex);
							Operand minRight = new Operand(new String[] { oldestPeriod.getId().toString() },
									oldestPeriod.getId().toString(), "Static Content", new String[] {""}, new String[] {""}, "");

							String minFilterId = "TimeFilterMax" + timeFilterIndex;
							WhereField minWhereField = new WhereField(minFilterId, minFilterId, false, left,
									"EQUALS OR GREATER THAN", minRight, "AND");
							nodesToAdd.add(minFilterId);

							whereFieldsToAdd.add(maxWhereField);
							whereFieldsToAdd.add(minWhereField);
						}
					}
				}
				whereFieldIndex++;
			}

			for (Integer index : whereFieldsIndexesToRemove) {
				query.removeWhereField(index);
			}

			for (int index = 0; index < whereFieldsToAdd.size(); index++) {
				WhereField whereFieldToAdd = whereFieldsToAdd.get(index);
				query.addWhereField(whereFieldToAdd.getName(), whereFieldToAdd.getDescription(),
						whereFieldToAdd.isPromptable(), whereFieldToAdd.getLeftOperand(), whereFieldToAdd.getOperator(),
						whereFieldToAdd.getRightOperand(), whereFieldToAdd.getBooleanConnector());

			}

			query.updateWhereClauseStructure();

			handleInLineTemporalFilter(query, temporalDimension, defaultHierarchy);

		}

		if (timeDimension != null) {

			HierarchicalDimensionField hierarchicalDimensionByEntity = timeDimension
					.getHierarchicalDimensionByEntity(timeDimension.getType());
			Hierarchy defaultHierarchy = hierarchicalDimensionByEntity.getDefaultHierarchy();

			int timeFilterIndex = 0;
			int whereFieldIndex = 0;
			List<WhereField> whereFields = query.getWhereFields();
			List<WhereField> whereFieldsToAdd = new LinkedList<WhereField>();
			List<Integer> whereFieldsIndexesToRemove = new LinkedList<Integer>();
			List<String> nodesToAdd = new LinkedList<String>();

			for (WhereField whereField : whereFields) {
				String[] lValues = whereField.getLeftOperand().values;
				String[] rValues = whereField.getRightOperand().values;

				if (lValues != null && lValues.length > 0 && rValues != null && rValues.length > 0) {
					if ("TIME".equals(lValues[0])) {
						String timeLevelColumn = null;
						String timeLevel = whereField.getLeftOperand().description;
						timeLevelColumn = defaultHierarchy.getLevelByType(timeLevel);

						String timeDimensionId = "ID";

						TemporalRecord currentTime = getCurrentTime(timeDimension, timeDimensionId, timeLevelColumn,
								null, defaultHierarchy.getAncestors(timeLevelColumn));

						if ("Current".equals(rValues[0])) {
							lValues[0] = timeDimension.getType() + ":" + timeLevelColumn;
							rValues[0] = currentTime.getPeriod().toString();

						} else if (whereField.getOperator().equals("LAST")) {

							LinkedList<TemporalRecord> allPeriodsStartingDate = loadAllPeriodsStartingDate(
									timeDimension, timeDimensionId, timeLevelColumn,
									defaultHierarchy.getAncestors(timeLevelColumn));

							int currentPeriodIndex = getCurrentIndex(allPeriodsStartingDate,
									(Integer) currentTime.getId());
							whereFieldsIndexesToRemove.add(whereFieldIndex);
							Operand left = new Operand(new String[] { timeDimension.getType() + ":" + timeDimensionId },
									timeDimension.getName() + ":" + timeDimensionId, "Field Content", new String[] {""}, new String[] {""}, "");
							Operand maxRight = new Operand(new String[] { currentTime.getId().toString() },
									currentTime.getId().toString(), "Static Content", new String[] {""}, new String[] {""}, "");

							String maxFilterId = "TimeFilterMax" + timeFilterIndex;
							WhereField maxWhereField = new WhereField(maxFilterId, maxFilterId, false, left,
									"EQUALS OR LESS THAN", maxRight, "AND");
							nodesToAdd.add(maxFilterId);

							int offset = Integer.parseInt(rValues[0]);
							int oldestPeriodIndex = currentPeriodIndex - offset > 0 ? currentPeriodIndex - offset : 0;
							TemporalRecord oldestPeriod = allPeriodsStartingDate.get(oldestPeriodIndex);
							Operand minRight = new Operand(new String[] { oldestPeriod.getId().toString() },
									oldestPeriod.getId().toString(), "Static Content", new String[] {""}, new String[] {""}, "");

							String minFilterId = "TimeFilterMax" + timeFilterIndex;
							WhereField minWhereField = new WhereField(minFilterId, minFilterId, false, left,
									"EQUALS OR GREATER THAN", minRight, "AND");
							nodesToAdd.add(minFilterId);

							whereFieldsToAdd.add(maxWhereField);
							whereFieldsToAdd.add(minWhereField);
						}
					}
				}
			}

			query.updateWhereClauseStructure();

		}
	}

	private int getCurrentIndex(LinkedList<TemporalRecord> allPeriodsStartingDate, int currentPeriodId) {
		int index = 0;
		for (TemporalRecord temporalRecord : allPeriodsStartingDate) {
			int curr = (Integer) temporalRecord.getId();
			if (currentPeriodId <= curr) {
				break;
			}
			index++;
		}
		return index - 1;
	}

	public final static String TEMPORAL_OPERAND_YTD = "YTD";
	public final static String TEMPORAL_OPERAND_QTD = "QTD";
	public final static String TEMPORAL_OPERAND_MTD = "MTD";
	public final static String TEMPORAL_OPERAND_WTD = "WTD";
	public final static String TEMPORAL_OPERAND_LAST_YEAR = "LAST_YEAR";
	public final static String TEMPORAL_OPERAND_LAST_QUARTER = "LAST_QUARTER";
	public final static String TEMPORAL_OPERAND_LAST_MONTH = "LAST_MONTH";
	public final static String TEMPORAL_OPERAND_LAST_WEEK = "LAST_WEEK";
	public final static String TEMPORAL_OPERAND_PARALLEL_YEAR = "PARALLEL_YEAR";

	private void handleInLineTemporalFilter(Query query, IModelEntity temporalDimension, Hierarchy hierarchy) {

		List<ISelectField> selectFields = query.getSelectFields(false);
		List<WhereField> whereFields = query.getWhereFields();
		
		// verifing if there's an inline filter
		if (hasInlineFilters(selectFields)) {

			// Retrieving all hierarchy levels
			List<HierarchyLevel> levels = hierarchy.getLevels();

			// Retrieving all hierarchy columns (used to check if those are all included in the group by clause)
			Map<String, String> hierarchyFullColumnMap = new LinkedHashMap<>();
			Map<String, String> hierarchyColumnMap = new LinkedHashMap<>();
			for (HierarchyLevel level : levels) {
				hierarchyColumnMap.put(level.getType(), level.getColumn());
				hierarchyFullColumnMap.put(level.getType(), extractColumnName(temporalDimension, level.getColumn()));
			}
			
			// retrieving all the temporal fields coming from temporal inline filters
			Set<String> inlineFilterFieldTypes = extractInlineFilterFieldTypes(selectFields, hierarchyFullColumnMap);
			
			// retrieving time_id
			String temporalDimensionId = getTimeId(temporalDimension);

			// adding  time_id to query
			addTimeIdToQuery(query, temporalDimension, temporalDimensionId);

			// relative year
			String relativeYear = (new GregorianCalendar().get(Calendar.YEAR)) + "";
			Set<String> yearsInWhere = extractYearsFromWhereFields(whereFields, hierarchyFullColumnMap.get("YEAR"));
			if (yearsInWhere.size() > 0) {
				relativeYear = yearsInWhere.iterator().next();
			}

			// retrieving all year on DWH
			LinkedList<TemporalRecord> allYearsOnDWH = loadAllPeriodsStartingDate(temporalDimension,
					temporalDimensionId, hierarchyColumnMap.get("YEAR"));
			LinkedList<String> allYearsOnDWHString = new LinkedList<>();
			for (TemporalRecord temporalRecord : allYearsOnDWH) {
				allYearsOnDWHString.add(temporalRecord.getPeriod().toString());
			}

			// retrieving relative year index on allYearsOnDWH list
			int relativeYearIndex = -1;
			for (int i = 0; i < allYearsOnDWHString.size(); i++) {
				if (allYearsOnDWHString.get(i).equals(relativeYear)) {
					relativeYearIndex = i;
					break;
				}
			}

			Set<String> aliasesToBeRemovedAfterExecution = addMissingGroupByToTheQuery(query, selectFields,
					inlineFilterFieldTypes, hierarchyFullColumnMap);

			addSumFunctionToAllMeasureInSelect(selectFields);

			Map<String, String> currentPeriodValuyesByType = addMissingCurrentPeriodWhereClauses(query,
					temporalDimension, selectFields, whereFields, inlineFilterFieldTypes, temporalDimensionId,
					hierarchyFullColumnMap, hierarchyColumnMap, relativeYear);

			// retrieving all temporal fields used in query
			Set<String> temporalFieldTypesInSelect = getTemporalFieldsInSelect(query.getSelectFields(false), hierarchyFullColumnMap);

			// defining wich fields will be calculated after query execution
			Map<String, Map<String, String>> inlineFilteredSelectFields = updateInlineFilteredSelectFieldsAliases(
					selectFields);

			// adding yera as select field if not present
			if (!temporalFieldTypesInSelect.contains(hierarchyFullColumnMap.get("YEAR"))) {
				addYearToQuery(query, temporalDimension, hierarchyFullColumnMap);
				aliasesToBeRemovedAfterExecution.add(hierarchyFullColumnMap.get("YEAR"));
			}

			// preparing data for query post-processing
			query.setInlineFilteredSelectFields(inlineFilteredSelectFields);
			query.setAliasesToBeRemovedAfterExecution(aliasesToBeRemovedAfterExecution);
			query.setTemporalFieldTypesInSelect(temporalFieldTypesInSelect);
			query.setHierarchyFullColumnMap(hierarchyFullColumnMap);
			query.setRelativeYearIndex(relativeYearIndex);
			query.setAllYearsOnDWH(allYearsOnDWHString);

			Map<String, List<String>> distinctPeriods = new LinkedHashMap<>();
			for (String temporalFieldColumn : temporalFieldTypesInSelect) {
				distinctPeriods.put(temporalFieldColumn,
						loadDistinctPeriods(temporalDimension, temporalDimensionId, temporalFieldColumn));
			}
			for (String temporalFieldColumn : aliasesToBeRemovedAfterExecution) {
				distinctPeriods.put(temporalFieldColumn,
						loadDistinctPeriods(temporalDimension, temporalDimensionId, temporalFieldColumn));
			}
			query.setDistinctPeriods(distinctPeriods);
			query.setCurrentPeriodValuyesByType(currentPeriodValuyesByType);

			// relativeYear will be added later on where clause only if needed
			removeRelativeYearFromWhereFields(whereFields, yearsInWhere, hierarchyFullColumnMap.get("YEAR"), relativeYear);
			
			// when period to date will result in a single record, temporal where clauses are used to calculate the relative period, not for filtering 
			if(containsPeriodToDate(selectFields) && !isMultiLineResult(selectFields, hierarchyFullColumnMap)) {
				removeCurrentPeriodFromWhereFields(whereFields, hierarchyFullColumnMap);
			}
			
			addYearsFilterForPerformances(query, selectFields, whereFields, hierarchyFullColumnMap, relativeYear,
					yearsInWhere, allYearsOnDWHString, relativeYearIndex, distinctPeriods, currentPeriodValuyesByType);
		}
	}

	private boolean removeCurrentPeriodFromWhereFields(List<WhereField> whereFields,
			Map<String, String> hierarchyFullColumnMap) {

		List<WhereField> whereFieldsToBeRemoved = new ArrayList<>();
		
		LOOP_4:
		for (String levelType : hierarchyFullColumnMap.keySet()) {
			if(!"YEAR".equals(levelType)) {
				String levelColumn = hierarchyFullColumnMap.get(levelType);
				
				for (WhereField wField : whereFields) {
					if (wField.getLeftOperand().values != null && wField.getLeftOperand().values.length > 0
							&& levelColumn.equals(wField.getLeftOperand().values[0]) && ("EQUALS TO".equals(wField.getOperator()) || ("IN".equals(wField.getOperator())))
							&& wField.getRightOperand().values != null && wField.getRightOperand().values.length > 0) {
						
						whereFieldsToBeRemoved.add(wField);
						break;
					}
				}
			}
		}
		
		if(whereFieldsToBeRemoved.size() > 0) {
			whereFields.removeAll(whereFieldsToBeRemoved);
		}
		return whereFieldsToBeRemoved.size() > 0;
	}

	private void removeRelativeYearFromWhereFields(List<WhereField> whereFields, Set<String> yearsInWhere, String yearColumn, String relativeYear) {
		for (WhereField wField : whereFields) {
			if (wField.getLeftOperand().values != null && wField.getLeftOperand().values.length > 0
					&& yearColumn.equals(wField.getLeftOperand().values[0]) && ("EQUALS TO".equals(wField.getOperator()) || ("IN".equals(wField.getOperator())))
					&& wField.getRightOperand().values != null && wField.getRightOperand().values.length > 0
					&& relativeYear.equals(wField.getRightOperand().values[0] + "")) {
				
				whereFields.remove(wField);
				break;
			}
		}
		
		yearsInWhere.remove(relativeYear);
	}

	private String getTimeId(IModelEntity temporalDimension) {

		List<IModelField> fields = temporalDimension.getAllFields();
		for (IModelField f : fields) {
			if (f.getName().equalsIgnoreCase("time_id")) {
				return f.getName();
			}
		}

		throw new SpagoBIRuntimeException("Impossible to find time_id on Temporal Dimension");
	}

	private String getDateField(IModelEntity temporalDimension) {

		List<IModelField> fields = temporalDimension.getAllFields();
		for (IModelField f : fields) {
			if (f.getName().equalsIgnoreCase("the_date") || f.getName().equalsIgnoreCase("time_date")) {
				return f.getName();
			}
		}

		throw new SpagoBIRuntimeException("Impossible to find a date field on Temporal Dimension");
	}

	private void addYearsFilterForPerformances(Query query, List<ISelectField> selectFields,
			List<WhereField> whereFields, Map<String, String> hierarchyFullColumnMap, String relativeYear,
			Set<String> yearsInWhere, LinkedList<String> allYearsOnDWHString, int relativeYearIndex,
			Map<String, List<String>> distinctPeriods, Map<String, String> currentPeriodValuyesByType) {
		
		if(!existsNotInlineTemporlFilteredColumn(selectFields)) {
		
			Set<String> yearsToBeAddedToWhereClause = extractYearsToBeAddedToWhereClause(selectFields, relativeYear,
					yearsInWhere, allYearsOnDWHString, relativeYearIndex, hierarchyFullColumnMap, distinctPeriods,
					currentPeriodValuyesByType);
			if (yearsToBeAddedToWhereClause.size() > 0) {
				boolean yersAdded = false;
				if (whereFields.size() > 0) {
					for (WhereField wField : whereFields) {
						if (wField.getLeftOperand().values != null && wField.getLeftOperand().values.length > 0
								&& hierarchyFullColumnMap.get("YEAR").equals(wField.getLeftOperand().values[0])
								&& ("EQUALS TO".equals(wField.getOperator()) || ("IN".equals(wField.getOperator()))) &&  wField.getRightOperand().values != null
								&&  wField.getRightOperand().values.length > 0) {
	
							for (String value :  wField.getRightOperand().values) {
								yearsToBeAddedToWhereClause.add(value);
							}
							
							Operand right = new Operand(
									yearsToBeAddedToWhereClause.toArray(new String[yearsToBeAddedToWhereClause.size()]),
									"YEAR", "Static Content", new String[] {""}, new String[] {""}, "");
	
							wField.setRightOperand(right);
							wField.setOperator("IN");
							yersAdded = true;
							break;
						}
					}
				}
				
				if(!yersAdded) {
					Operand left = new Operand(new String[] { hierarchyFullColumnMap.get("YEAR") },
							hierarchyFullColumnMap.get("YEAR"), "Field Content", new String[] {""}, new String[] {""}, "");
	
					Operand right = new Operand(
							yearsToBeAddedToWhereClause.toArray(new String[yearsToBeAddedToWhereClause.size()]), "YEAR",
							"Static Content", new String[] {""}, new String[] {""}, "");
					query.addWhereField("ParallelYear", "ParallelYear", false, left, "IN", right, "AND");
				}
	
			}
		}
		query.updateWhereClauseStructure();
	}

	private boolean existsNotInlineTemporlFilteredColumn(List<ISelectField> selectFields) {
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();
		
				if(StringUtilities.isEmpty(temporalOperand) && 
						ssField.getFunction() != null && !"NONE".equals(ssField.getFunction().getName()) &&
						!ssField.isGroupByField()) {
					return true;
				}
			}
		}
		return false;
	}

	private Map<String, Map<String, String>> updateInlineFilteredSelectFieldsAliases(List<ISelectField> selectFields) {
		Map<String, Map<String, String>> inlineFilteredSelectFields = new HashMap<>();
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();
				if (temporalOperand != null && !"".equals(temporalOperand)) {
					String temporalOperandParameter = ssField.getTemporalOperandParameter();
					if (temporalOperandParameter == null)
						temporalOperandParameter = "0";
					String newAlias = ssField.getAlias() + "_" + temporalOperand + "_" + temporalOperandParameter;
					ssField.setAlias(newAlias);

					Map<String, String> parameters = new HashMap<>();
					parameters.put("temporalOperand", temporalOperand);
					parameters.put("temporalOperandParameter", temporalOperandParameter);

					inlineFilteredSelectFields.put(newAlias, parameters);

				}
			}
		}
		return inlineFilteredSelectFields;
	}

	private Map<String, String> addMissingCurrentPeriodWhereClauses(Query query, IModelEntity temporalDimension,
			List<ISelectField> selectFields, List<WhereField> whereFields, Set<String> inlineFilterFieldTypes,
			String temporalDimensionId, Map<String, String> hierarchyFullColumnMap,
			Map<String, String> hierarchyColumnMap, String relativeYear) {

		Map<String, String> currentPeriodValuesByType = new HashMap<>();

		Set<String> temporalFieldTypesInSelect = new HashSet<>();
		Set<String> temporalFieldTypesInWhere = new HashSet<>();

		boolean hasPeriodToDate = false;
		
		Map<String, int[]> lastOperatorRanges = new HashMap<>();
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();
				int temporalOperandParameter = -1*Integer.parseInt(ssField.getTemporalOperandParameter() != null ? ssField.getTemporalOperandParameter() : "0");
				if (temporalOperand != null) {
					switch (temporalOperand) {
					
					case TEMPORAL_OPERAND_YTD:
						hasPeriodToDate=true;
						break;
					case TEMPORAL_OPERAND_QTD:
						hasPeriodToDate=true;
					case TEMPORAL_OPERAND_LAST_QUARTER:
						int[] opRangeQuarter = {0,0};
						if(lastOperatorRanges.containsKey("QUARTER")) {
							opRangeQuarter = lastOperatorRanges.get("QUARTER");
						}
						if(temporalOperandParameter <= 0 && temporalOperandParameter < opRangeQuarter[0]) {
							opRangeQuarter[0] = temporalOperandParameter;
						}
						if(temporalOperandParameter >= 0 && temporalOperandParameter > opRangeQuarter[1]) {
							opRangeQuarter[1] = temporalOperandParameter;
						}
						lastOperatorRanges.put("QUARTER", opRangeQuarter);
						break;
					case TEMPORAL_OPERAND_MTD:
						hasPeriodToDate=true;
					case TEMPORAL_OPERAND_LAST_MONTH:
						int[] opRangeMonth = {0,0};
						if(lastOperatorRanges.containsKey("MONTH")) {
							opRangeMonth = lastOperatorRanges.get("MONTH");
						}
						if(temporalOperandParameter <= 0 && temporalOperandParameter < opRangeMonth[0]) {
							opRangeMonth[0] = temporalOperandParameter;
						}
						if(temporalOperandParameter >= 0 && temporalOperandParameter > opRangeMonth[1]) {
							opRangeMonth[1] = temporalOperandParameter;
						}
						lastOperatorRanges.put("MONTH", opRangeMonth);
						break;
					default:
					}
				}
			}
		}
		
		LOOP_1: for (String levelType : inlineFilterFieldTypes) {
			String levelColumn = hierarchyFullColumnMap.get(levelType);
			if (!temporalFieldTypesInSelect.contains(levelType)) {

				// seraching on selct fields
				for (ISelectField sfield : selectFields) {
					if (sfield.isSimpleField()) {
						SimpleSelectField ssField = (SimpleSelectField) sfield;
						if (levelColumn.equals(ssField.getUniqueName())) {
							temporalFieldTypesInSelect.add(levelType);

							TemporalRecord currentPeriod = getCurrentPeriod(temporalDimension, temporalDimensionId,
									hierarchyColumnMap.get(levelType), new Date());
							String currentPeriodValue = "K_UNDEFINED";
							if ((currentPeriod != null)) {
								currentPeriodValue = currentPeriod.getPeriod() + "";
							}
							currentPeriodValuesByType.put(levelColumn, currentPeriodValue);

							continue LOOP_1;
						}
					}
				}
			}

			if (!temporalFieldTypesInWhere.contains(levelType)) {
				// serching on filters
				for (WhereField wField : whereFields) {
					if (wField.getLeftOperand().values != null && wField.getLeftOperand().values.length > 0
							&& levelColumn.equals(wField.getLeftOperand().values[0])
							&& "EQUALS TO".equals(wField.getOperator()) || "IN".equals(wField.getOperator())) {
						temporalFieldTypesInWhere.add(levelType);

						currentPeriodValuesByType.put(levelColumn, wField.getRightOperand().values[0]);

						continue LOOP_1;
					}
				}
			}
		}

		Set<String> temporalFieldTypesInSelectOrWhere = new HashSet<>();
		temporalFieldTypesInSelectOrWhere.addAll(temporalFieldTypesInSelect);
		temporalFieldTypesInSelectOrWhere.addAll(temporalFieldTypesInWhere);

		for (String levelType : inlineFilterFieldTypes) {
			if (!temporalFieldTypesInSelectOrWhere.contains(levelType) ) {
				String levelColumn = hierarchyFullColumnMap.get(levelType);
				Operand left = new Operand(new String[] { levelColumn }, levelColumn, "Field Content", new String[] {""}, new String[] {""}, "");

				TemporalRecord currentPeriod = getCurrentPeriod(temporalDimension, temporalDimensionId,
						hierarchyColumnMap.get(levelType), new Date());
				String currentPeriodValue = "K_UNDEFINED";
				if ((currentPeriod != null)) {
					currentPeriodValue = currentPeriod.getPeriod() + "";
				}
				
				String[] currentPeriodValues = new String[] {  currentPeriodValue };
				
				int[] opRange = lastOperatorRanges.get(levelType);
				if(opRange != null) {
				
					LinkedList<LinkedHashMap<String,String>> allMonthOrQuarterPeriods = new LinkedList<>();
					LinkedList<TemporalRecord> allPeriodsInDwh = loadAllPeriodsStartingDate(temporalDimension, temporalDimensionId, hierarchyColumnMap.get(levelType));
					LinkedList<TemporalRecord> allYearsOnDWH = loadAllPeriodsStartingDate(temporalDimension, temporalDimensionId, hierarchyColumnMap.get("YEAR"));
					for (TemporalRecord  yearRecord : allYearsOnDWH) {
						for (TemporalRecord monthOrQuarterRecord : allPeriodsInDwh) {
							LinkedHashMap<String, String> record = new LinkedHashMap<>();
							record.put("YEAR", yearRecord.getPeriod().toString());
							record.put(levelType, monthOrQuarterRecord.getPeriod().toString());
							allMonthOrQuarterPeriods.add(record);
						}
					}
					LinkedHashMap<String, String> currentRecord = new LinkedHashMap<>();
					currentRecord.put("YEAR", relativeYear);
					currentRecord.put(levelType, currentPeriodValue);
					
					int currentPeriodIndex = allMonthOrQuarterPeriods.indexOf(currentRecord);
					
	
					int start = currentPeriodIndex + opRange[0];
					if (start < 0)
						start = 0;
					if (start > allMonthOrQuarterPeriods.size() - 1)
						start = allMonthOrQuarterPeriods.size() - 1;
					int end = currentPeriodIndex + opRange[1];
					if (end < 0)
						end = 0;
					if (end > allMonthOrQuarterPeriods.size()-1)end =  allMonthOrQuarterPeriods.size()-1;
					
					Set<String> currentPeriodValuesSet = new LinkedHashSet<>();
					for (int i = start; i <= end ; i++ ) {
						currentPeriodValuesSet.add(allMonthOrQuarterPeriods.get(i).get(levelType));
					}
					
					currentPeriodValues = currentPeriodValuesSet.toArray(new String[0]);
				}
				
				// Period to date aggregates only with 'daily granularity'
				 if(levelType.equals("YEAR") || !hasPeriodToDate) {
				
					Operand right = new Operand(currentPeriodValues, levelType, "Static Content", new String[] {""}, new String[] {""}, "");
					query.addWhereField("current_" + levelType, "current_" + levelType, false, left, "IN", right,
							"AND");
					query.updateWhereClauseStructure();
				 }
					
				currentPeriodValuesByType.put(levelColumn, currentPeriodValue);
			}
		}
		query.updateWhereClauseStructure();

		return currentPeriodValuesByType;
	}

	private Set<String> getTemporalFieldsInSelect(List<ISelectField> selectFields,
			Map<String, String> hierarchyFullColumnMap) {
		Set<String> temporalFieldsInSelect = new HashSet<>();

		LOOP_3:

		for (String levelType : hierarchyFullColumnMap.keySet()) {
			String levelColumn = hierarchyFullColumnMap.get(levelType);
			// lo cerco nelle select
			for (ISelectField sfield : selectFields) {
				if (sfield.isSimpleField()) {
					SimpleSelectField ssField = (SimpleSelectField) sfield;
					if (levelColumn.equals(ssField.getUniqueName())) {
						temporalFieldsInSelect.add(levelColumn);
						continue LOOP_3;
					}
				}
			}
		}

		return temporalFieldsInSelect;
	}

	private void addTimeIdToQuery(Query query, IModelEntity temporalDimension, String temporalDimensionId) {
		String fieldUniqueName = extractColumnName(temporalDimension, temporalDimensionId);
		String function = "MIN";
		boolean include = true;
		boolean visible = false;
		boolean groupByField = false;
		String orderType = "ASC";
		String pattern = null;
		String temporalOperand = null;
		String temporalOperandParameter = null;
		query.addSelectFiled(fieldUniqueName, function, temporalDimensionId, include, visible, groupByField, orderType,
				pattern, temporalOperand, temporalOperandParameter);
	}

	private void addYearToQuery(Query query, IModelEntity temporalDimension,
			Map<String, String> hierarchyFullColumnMap) {
		String fieldUniqueName = hierarchyFullColumnMap.get("YEAR");
		String function = null;
		boolean include = true;
		boolean visible = false;
		boolean groupByField = true;
		String orderType = "ASC";
		String pattern = null;
		String temporalOperand = null;
		String temporalOperandParameter = null;
		query.addSelectFiled(fieldUniqueName, function, "YEAR", include, visible, groupByField, orderType, pattern,
				temporalOperand, temporalOperandParameter);
	}

	private void addSumFunctionToAllMeasureInSelect(List<ISelectField> selectFields) {
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;

				if (ssField.getFunction() == null && "MEASURE".equals(ssField.getNature())
						|| ssField.getTemporalOperand() != null && ssField.getTemporalOperand().length() > 0) {
					ssField.setFunction(AggregationFunctions.get(AggregationFunctions.SUM));
				}

			}
		}
	}

	private Set<String> addMissingGroupByToTheQuery(Query query, List<ISelectField> selectFields,
			Set<String> inlineFilterFieldTypes, Map<String, String> hierarchyFullColumnMap) {
		Set<String> aliasesToBeRemovedAfterExecution = new HashSet<>();

		Set<String> temporalFieldAlreadyInSelect = new HashSet<>();
		for (String levelType : hierarchyFullColumnMap.keySet()) {
			String levelColumn = hierarchyFullColumnMap.get(levelType);
			for (ISelectField sfield : selectFields) {
				if (sfield.isSimpleField()) {
					SimpleSelectField ssField = (SimpleSelectField) sfield;
					if (levelColumn.equals(ssField.getUniqueName())) {
						temporalFieldAlreadyInSelect.add(levelColumn);
						// se nei campi select � presente un campo della
						// gerarchia, tale campo parteciper� al raggruppamento
						ssField.setGroupByField(true);
					}
				}
			}
		}

		for (String inlineFilterType : inlineFilterFieldTypes) {
			if (!temporalFieldAlreadyInSelect.contains(hierarchyFullColumnMap.get(inlineFilterType))) {
				String fieldUniqueName = hierarchyFullColumnMap.get(inlineFilterType);
				boolean include = true;
				boolean visible = false;
				boolean groupByField = true;
				String orderType = null;
				String pattern = null;
				String temporalOperand = null;
				String temporalOperandParameter = null;
				query.addSelectFiled(fieldUniqueName, null, inlineFilterType, include, visible, groupByField, orderType,
						pattern, temporalOperand, temporalOperandParameter);

				aliasesToBeRemovedAfterExecution.add(fieldUniqueName);
			}
		}
		return aliasesToBeRemovedAfterExecution;
	}

	private Set<String> extractYearsToBeAddedToWhereClause(List<ISelectField> selectFields, String relativeYear,
			Set<String> yearsInWhere, LinkedList<String> allYearsOnDWHString, int relativeYearIndex,
			Map<String, String> hierarchyFullColumnMap, Map<String, List<String>> distinctPeriods,
			Map<String, String> currentPeriodValuesByType) {
		Set<String> yearsToBeAddedToWhereClause = new HashSet<>();

		Map<String, List<String>> distinctPeriodsByType = new HashMap<>();
		for (String type : hierarchyFullColumnMap.keySet()) {
			distinctPeriodsByType.put(type, distinctPeriods.get(hierarchyFullColumnMap.get(type)));
		}
		Map<String, Integer> currentPeriodsNumbered = new HashMap<>();
		for (String type : currentPeriodValuesByType.keySet()) {
			String currentPeriodValue = currentPeriodValuesByType.get(type);
			List<String> distinctPeriodsForThisType = distinctPeriods.get(type);
			int currentValueIndexForThisType = -1;
			for (int i = 0; i < distinctPeriodsForThisType.size(); i++) {
				String period = distinctPeriodsForThisType.get(i);
				if (period.equals(currentPeriodValue)) {
					currentValueIndexForThisType = i;
					break;
				}
			}
			currentPeriodsNumbered.put(type, currentValueIndexForThisType + 1);
		}

		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();
				String temporalOperandParameter = ssField.getTemporalOperandParameter();

				int n = (temporalOperandParameter == null || !temporalOperandParameter.matches("-?[0-9]*")) ? 0
						: Integer.parseInt(temporalOperandParameter);

				if (temporalOperand != null && !"".equals(temporalOperand)) {
					Integer yearOtherIndex = null;
					String periodType = null;
					boolean lastPeriod = false;
					switch (temporalOperand) {

					case TEMPORAL_OPERAND_QTD:
						if (periodType == null) {
							periodType = "QUARTER";
						}
					case TEMPORAL_OPERAND_MTD:
						if (periodType == null) {
							periodType = "MONTH";
						}
					case TEMPORAL_OPERAND_WTD:
						if (periodType == null) {
							periodType = "WEEK";
						}
					case TEMPORAL_OPERAND_LAST_QUARTER:
						if (periodType == null) {
							periodType = "QUARTER";
							lastPeriod = true;
						}

					case TEMPORAL_OPERAND_LAST_MONTH:
						if (periodType == null) {
							periodType = "MONTH";
							lastPeriod = true;
						}

					case TEMPORAL_OPERAND_LAST_WEEK:
						if (periodType == null) {
							periodType = "WEEK";
							lastPeriod = true;
						}

						Integer currentPeriodNumber = currentPeriodsNumbered
								.get(hierarchyFullColumnMap.get(periodType));
						Integer otherPeriodNumber = currentPeriodNumber - n;
						if (otherPeriodNumber < currentPeriodNumber) {
							otherPeriodNumber = otherPeriodNumber + 1;
						} else {
							otherPeriodNumber = otherPeriodNumber - 1;
						}

						List<String> periods = distinctPeriodsByType.get(periodType);
						int periodsCount = periods.size();
						int periodOtherIndex = (otherPeriodNumber % periodsCount);

						int yearOffset = 0;
						while (periodOtherIndex < 0) {
							periodOtherIndex += periodsCount;
							yearOffset--;
						}
						while (periodOtherIndex >= periodsCount) {
							periodOtherIndex = periodOtherIndex % periodsCount;
							yearOffset++;
						}

						yearOtherIndex = (int) (relativeYearIndex + yearOffset);
						if (yearOtherIndex < 0) {
							yearOtherIndex = 0;
						}
						if (yearOtherIndex >= allYearsOnDWHString.size()) {
							yearOtherIndex = allYearsOnDWHString.size() - 1;
							periodOtherIndex = periods.size() - 1;
						}

					case TEMPORAL_OPERAND_LAST_YEAR:
						if (yearOtherIndex == null) {
							yearOtherIndex = relativeYearIndex - n;
						}

						if (lastPeriod) {
							if (yearOtherIndex < relativeYearIndex) {
								yearsToBeAddedToWhereClause
										.addAll(allYearsOnDWHString.subList(yearOtherIndex, relativeYearIndex + 1));
							} else {
								yearsToBeAddedToWhereClause
										.addAll(allYearsOnDWHString.subList(relativeYearIndex, yearOtherIndex + 1));
							}
						} else {
							if (yearOtherIndex >= 0 && allYearsOnDWHString.size() > yearOtherIndex) {
								yearsToBeAddedToWhereClause.add(allYearsOnDWHString.get(yearOtherIndex));
							}
						}
						break;

					case TEMPORAL_OPERAND_YTD:
					case TEMPORAL_OPERAND_PARALLEL_YEAR:
						int parallelYearIndex = relativeYearIndex - n;
						if (parallelYearIndex >= 0 && allYearsOnDWHString.size() > parallelYearIndex) {
							yearsToBeAddedToWhereClause.add(allYearsOnDWHString.get(parallelYearIndex));
						}
						break;

					default:
						break;
					}
				}
			}
		}
		return yearsToBeAddedToWhereClause;
	}

	private Set<String> extractInlineFilterFieldTypes(List<ISelectField> selectFields, Map<String, String> hierarchyFullColumnMap) {
		
		boolean multipleLineResult = isMultiLineResult(selectFields, hierarchyFullColumnMap);
		
		return harvestInlineFilterType(selectFields, !multipleLineResult);
	}

	private boolean isMultiLineResult(List<ISelectField> selectFields, Map<String, String> hierarchyFullColumnMap) {
		boolean multipleLineResult = false;
		for (ISelectField sfield : selectFields) {
			if(sfield instanceof SimpleSelectField) {
				String sfieldUniqueName = ((SimpleSelectField)sfield).getUniqueName();
				if(hierarchyFullColumnMap.values().contains(sfieldUniqueName)) {
					multipleLineResult = true;
					break;
				}
			}
		}
		return multipleLineResult;
	}

	private boolean hasInlineFilters(List<ISelectField> selectFields) {
		return harvestInlineFilterType(selectFields, false).size() > 0;
	}
	
	private Set<String> harvestInlineFilterType(List<ISelectField> selectFields, boolean singleLineResult) {
		Set<String> inlineFilterFieldTypes = new HashSet<>();
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();

				if (temporalOperand != null) {
					switch (temporalOperand) {

					// YEAR
					case TEMPORAL_OPERAND_YTD:
						if(singleLineResult) {
							inlineFilterFieldTypes.add("MONTH");
							inlineFilterFieldTypes.add("DAY");
						}
					case TEMPORAL_OPERAND_LAST_YEAR:
					case TEMPORAL_OPERAND_PARALLEL_YEAR:
						inlineFilterFieldTypes.add("YEAR");
						break;

					// QUARTER
					case TEMPORAL_OPERAND_QTD:
						if(singleLineResult) {
							inlineFilterFieldTypes.add("MONTH");
							inlineFilterFieldTypes.add("DAY");
						}
					case TEMPORAL_OPERAND_LAST_QUARTER:
						inlineFilterFieldTypes.add("QUARTER");
						break;

					// MONTH
					case TEMPORAL_OPERAND_MTD:
						if(singleLineResult) {
							inlineFilterFieldTypes.add("DAY");
						}
					case TEMPORAL_OPERAND_LAST_MONTH:
						inlineFilterFieldTypes.add("MONTH");
						break;

					// WEEK
					case TEMPORAL_OPERAND_WTD:
						if(singleLineResult) {
							inlineFilterFieldTypes.add("DAY");
						}
					case TEMPORAL_OPERAND_LAST_WEEK:
						inlineFilterFieldTypes.add("WEEK");
						break;

					default:
						break;
					}
				}

			}
		}
		return inlineFilterFieldTypes;
	}
	
	private boolean containsPeriodToDate(List<ISelectField> selectFields) {
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();
				if (temporalOperand != null) {
					switch (temporalOperand) {
					case TEMPORAL_OPERAND_YTD:
					case TEMPORAL_OPERAND_QTD:
					case TEMPORAL_OPERAND_MTD:
					case TEMPORAL_OPERAND_WTD:
						return true;

					default:
						break;
					}
				}
			}
		}
		return false;
	}

	private Set<String> extractYearsFromWhereFields(List<WhereField> whereFields, String yearColumn) {
		Set<String> yearsInWhere = new HashSet<>();

		for (WhereField wField : whereFields) {
			if (wField.getLeftOperand().values != null && wField.getLeftOperand().values.length > 0
					&& yearColumn.equals(wField.getLeftOperand().values[0]) && "EQUALS TO".equals(wField.getOperator())
					&& wField.getRightOperand().values != null && wField.getRightOperand().values.length > 0) {
				yearsInWhere.add(wField.getRightOperand().values[0] + "");
			}
		}

		return yearsInWhere;
	}

	private String extractColumnName(IModelEntity temporalDimension, String column) {
		return temporalDimension.getType() + ":" + column;
	}

	private IModelEntity getTemporalDimension(IDataSource dataSource) {
		IModelEntity temporalDimension = null;
		Iterator<String> it = dataSource.getModelStructure().getModelNames().iterator();
		while (it.hasNext()) {
			String modelName = it.next();
			List<IModelEntity> rootEntities = getDataSource().getModelStructure().getRootEntities(modelName);
			for (IModelEntity bc : rootEntities) {
				if ("temporal_dimension".equals(bc.getProperty("type"))) {
					temporalDimension = bc;
					break;
				}
			}
		}
		return temporalDimension;
	}

	private IModelEntity getTimeDimension(IDataSource dataSource) {
		IModelEntity timeDimension = null;
		Iterator<String> it = dataSource.getModelStructure().getModelNames().iterator();
		while (it.hasNext()) {
			String modelName = it.next();
			List<IModelEntity> rootEntities = getDataSource().getModelStructure().getRootEntities(modelName);
			for (IModelEntity bc : rootEntities) {
				if ("time_dimension".equals(bc.getProperty("type"))) {
					timeDimension = bc;
					break;
				}
			}
		}
		return timeDimension;
	}

	private TemporalRecord getCurrentPeriod(IModelEntity temporalDimension, String idField, String periodField,
			Date actualTime, String... parentPeriodFields) {
		try {
			// nullsafe
			parentPeriodFields = parentPeriodFields != null ? parentPeriodFields : new String[0];

			actualTime = actualTime != null ? actualTime : new Date();

			Query currentPeriodQuery = new Query();
			currentPeriodQuery.addSelectFiled(temporalDimension.getType() + ":" + idField, null, "ID", true, true,
					false, "ASC", null);
			currentPeriodQuery.addSelectFiled(temporalDimension.getType() + ":" + periodField, null, "LEVEL", true,
					true, false, null, null);
			for (String parentPeriodField : parentPeriodFields) {
				currentPeriodQuery.addSelectFiled(temporalDimension.getType() + ":" + parentPeriodField, null,
						parentPeriodField, true, true, false, null, null);
			}

			String temporalDimensionDateField = getDateField(temporalDimension);

			Operand left = new Operand(new String[] { temporalDimension.getType() + ":" + temporalDimensionDateField },
					temporalDimension.getName() + ":" + temporalDimensionDateField, "Field Content", new String[] {""}, new String[] {""}, "");
			Operand right = new Operand(new String[] { new SimpleDateFormat("dd/MM/yyyy").format(actualTime) },
					new SimpleDateFormat("dd/MM/yyyy").format(actualTime), "Static Content", new String[] {""}, new String[] {""}, "");
			currentPeriodQuery.addWhereField("Filter1", "Filter1", false, left, "EQUALS TO", right, "AND");
			ExpressionNode newFilterNode = new ExpressionNode("NODE_CONST", "$F{" + "Filter1" + "}");
			currentPeriodQuery.setWhereClauseStructure(newFilterNode);

			IDataStore currentPeriodDataStore = executeDatamartQuery(currentPeriodQuery);
			@SuppressWarnings("unchecked")
			Iterator<IRecord> currentPeriodIterator = currentPeriodDataStore.iterator();

			TemporalRecord currentPeriodRecord = null;
			while (currentPeriodIterator.hasNext()) {
				IRecord r = currentPeriodIterator.next();
				currentPeriodRecord = new TemporalRecord(r, parentPeriodFields.length);
				break;
			}
			return currentPeriodRecord;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(
					"Impossible to retrieve current '" + periodField + "' from the temporal dimesion");
		}
	}

	private TemporalRecord getCurrentTime(IModelEntity timeDimension, String idField, String periodField,
			Date actualTime, String... parentPeriodFields) {

		// nullsafe
		parentPeriodFields = parentPeriodFields != null ? parentPeriodFields : new String[0];

		actualTime = actualTime != null ? actualTime : new Date();

		Query currentTimeQuery = new Query();
		currentTimeQuery.addSelectFiled(timeDimension.getType() + ":" + idField, null, "ID", true, true, false, "ASC",
				null);
		currentTimeQuery.addSelectFiled(timeDimension.getType() + ":" + periodField, null, "LEVEL", true, true, false,
				null, null);
		for (String parentPeriodField : parentPeriodFields) {
			currentTimeQuery.addSelectFiled(timeDimension.getType() + ":" + parentPeriodField, null, parentPeriodField,
					true, true, false, null, null);
		}

		String timeDimensionIdField = "ID";

		Operand left = new Operand(new String[] { timeDimension.getType() + ":" + timeDimensionIdField },
				timeDimension.getName() + ":" + timeDimensionIdField, "Field Content", new String[] {""}, new String[] {""}, "");

		Operand right = new Operand(new String[] { new SimpleDateFormat("HHmm").format(actualTime) },
				new SimpleDateFormat("HHmm").format(actualTime), "Static Content", new String[] {""}, new String[] {""}, "");

		currentTimeQuery.addWhereField("Filter1", "Filter1", false, left, "EQUALS TO", right, "AND");
		ExpressionNode newFilterNode = new ExpressionNode("NODE_CONST", "$F{" + "Filter1" + "}");
		currentTimeQuery.setWhereClauseStructure(newFilterNode);

		IDataStore currentTimeDataStore = executeDatamartQuery(currentTimeQuery);
		@SuppressWarnings("unchecked")
		Iterator<IRecord> currentTimeIterator = currentTimeDataStore.iterator();

		TemporalRecord currentTimeRecord = null;
		while (currentTimeIterator.hasNext()) {
			IRecord r = currentTimeIterator.next();
			currentTimeRecord = new TemporalRecord(r, parentPeriodFields.length);
			break;
		}
		return currentTimeRecord;
	}

	private LinkedList<TemporalRecord> loadAllPeriodsStartingDate(IModelEntity temporalDimension, String idField,
			String periodField, String... parentPeriodFields) {

		// nullsafe
		parentPeriodFields = parentPeriodFields != null ? parentPeriodFields : new String[] {};

		Query periodsStartingDates = new Query();
		periodsStartingDates.addSelectFiled(extractColumnName(temporalDimension, idField), "MIN", "ID", true, true,
				false, "ASC", null);
		periodsStartingDates.addSelectFiled(extractColumnName(temporalDimension, periodField), null, "LEVEL", true,
				true, true, null, null);
		for (String parentPeriodField : parentPeriodFields) {
			periodsStartingDates.addSelectFiled(extractColumnName(temporalDimension, parentPeriodField), null,
					parentPeriodField, true, true, true, null, null);
		}
		IDataStore periodsStartingDatesDataStore = executeDatamartQuery(periodsStartingDates);
		@SuppressWarnings("unchecked")
		Iterator<IRecord> periodsStartingDatesIterator = periodsStartingDatesDataStore.iterator();

		LinkedList<TemporalRecord> periodStartingDates = new LinkedList<TemporalRecord>();
		while (periodsStartingDatesIterator.hasNext()) {
			IRecord r = periodsStartingDatesIterator.next();
			TemporalRecord tr = new TemporalRecord(r, parentPeriodFields.length);
			periodStartingDates.add(tr);
		}
		return periodStartingDates;
	}

	private LinkedList<String> loadDistinctPeriods(IModelEntity temporalDimension, String idField,
			String temporalFieldColumn) {

		Query distinctPeriodsQuery = new Query();
		distinctPeriodsQuery.addSelectFiled(extractColumnName(temporalDimension, idField), "MIN", "ID", true, true,
				false, "ASC", null);
		distinctPeriodsQuery.addSelectFiled(temporalFieldColumn, null, "LEVEL", true, true, true, null, null);
		distinctPeriodsQuery.setDistinctClauseEnabled(true);

		IDataStore distinctPeriodsDatesDataStore = executeDatamartQuery(distinctPeriodsQuery);
		@SuppressWarnings("unchecked")
		Iterator<IRecord> distinctPeriod = distinctPeriodsDatesDataStore.iterator();

		LinkedList<String> distinctPeriods = new LinkedList<String>();
		while (distinctPeriod.hasNext()) {
			IRecord r = distinctPeriod.next();
			TemporalRecord tr = new TemporalRecord(r, 0);
			distinctPeriods.add(tr.getPeriod().toString());
		}
		return distinctPeriods;
	}

	private IDataStore executeDatamartQuery(Query myquery) {
		this.getEngineInstance().getQueryCatalogue().addQuery(myquery);
		this.getEngineInstance().setActiveQuery(myquery);
		AbstractQbeDataSet qbeDataSet = (AbstractQbeDataSet) this.getEngineInstance().getActiveQueryAsDataSet();

		String queryString = qbeDataSet.getStatement().getQueryString();
		logger.debug("QUERY STRING: " + queryString);

		qbeDataSet.loadData();
		IDataStore dataStore = qbeDataSet.getDataStore();

		this.getEngineInstance().getQueryCatalogue().removeQuery(myquery.getId());

		return dataStore;
	}

	/**
	 * Get the graph from the request: - if exist: - checks it is valid for the
	 * query - if its valid update the graph in the query and return null - if
	 * its not valid calculate the default graph and update the graph in the
	 * query - if not exists calculate the default graph and update the graph in
	 * the query
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
				RootEntitiesGraph rootEntitiesGraph = modelStructure
						.getRootEntitiesGraph(getDataSource().getConfiguration().getModelName(), false);
				Graph<IModelEntity, Relationship> graph = rootEntitiesGraph.getRootEntitiesGraph();
				logger.debug("UndirectedGraph retrieved");

				Set<IModelEntity> entities = query.getQueryEntities(getDataSource());
				if (entities.size() > 0) {
					queryGraph = GraphManager
							.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl())
							.getCoverGraph(graph, entities);
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
		(GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl()))
				.applyDefault(paths, ambiguousFields);

	}

	public void applySelectedRoles(String serializedRoles, Set<IModelEntity> modelEntities, Query query) {
		cleanFieldsRolesMapInEntity(query);
		try {
			if (serializedRoles != null && !serializedRoles.trim().equals("{}") && !serializedRoles.trim().equals("[]")
					&& !serializedRoles.trim().equals("")) {
				query.initFieldsRolesMapInEntity(getDataSource());
			}

		} catch (Exception e2) {
			logger.error("Error deserializing the list of roles of the entities", e2);
			throw new SpagoBIEngineRuntimeException("Error deserializing the list of roles of the entities", e2);
		}
	}

	public Set<ModelFieldPaths> getAmbiguousFields(Query query, Set<IModelEntity> modelEntities,
			Map<IModelField, Set<IQueryField>> modelFieldsMap) {
		logger.debug("IN");

		try {

			String modelName = getDataSource().getConfiguration().getModelName();

			Set<IModelField> modelFields = modelFieldsMap.keySet();

			Assert.assertNotNull(modelFields, "No field specified in teh query");
			Set<ModelFieldPaths> ambiguousModelField = new HashSet<ModelFieldPaths>();
			if (modelFields != null) {

				Graph<IModelEntity, Relationship> graph = getDataSource().getModelStructure()
						.getRootEntitiesGraph(modelName, false).getRootEntitiesGraph();

				PathInspector pathInspector = new PathInspector(graph, modelEntities);
				Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> ambiguousMap = pathInspector
						.getAmbiguousEntitiesAllPathsMap();

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
								ambiguousModelField
										.add(new ModelFieldPaths(queryFieldsIter.next(), iModelField, paths));
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
		RootEntitiesGraph rootEntitiesGraph = modelStructure
				.getRootEntitiesGraph(getDataSource().getConfiguration().getModelName(), false);
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
				logger.debug(
						"The query has been updated and in the previous ambiguos paths selection there is some field don't exist in teh query");
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
		return SerializerFactory.getDeserializer("application/json").deserializeQuery(queryJSON.toString(),
				getEngineInstance().getDataSource());
	}

	public static List<ModelFieldPaths> deserializeList(String serialized, Collection<Relationship> relationShips,
			IModelStructure modelStructure, Query query) throws SerializationException {
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("deprecation")
		SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
		simpleModule.addDeserializer(ModelFieldPaths.class,
				new ModelFieldPathsJSONDeserializer(relationShips, modelStructure, query));
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
		if (operandType.equalsIgnoreCase("STRING") || operandType.equalsIgnoreCase("CHARACTER")
				|| operandType.equalsIgnoreCase("java.lang.String")
				|| operandType.equalsIgnoreCase("java.lang.Character")) {

			// if the value is already surrounded by quotes, does not neither
			// add quotes nor escape quotes
			if (StringUtils.isBounded(operandValueToBound, "'")) {
				boundedValue = operandValueToBound;
			} else {
				operandValueToBound = StringUtils.escapeQuotes(operandValueToBound);
				return StringUtils.bound(operandValueToBound, "'");
			}
		} else if (operandType.equalsIgnoreCase("DATE") || operandType.equalsIgnoreCase("java.sql.date")
				|| operandType.equalsIgnoreCase("java.util.date")) {
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

		it.eng.spagobi.tools.datasource.bo.IDataSource connection = (it.eng.spagobi.tools.datasource.bo.IDataSource) this
				.getEngineInstance().getDataSource().getConfiguration().loadDataSourceProperties().get("datasource");

		String dialect = connection.getHibDialectClass();

		if (dialect != null) {

			if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_MYSQL)) {
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
					toReturn = ""+toReturn+"";
				} else {
					toReturn = "'" + toReturn + "'";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_TERADATA)) {
				/*
				 * Unfortunately we cannot use neither CAST(" + dateStr + " AS
				 * DATE FORMAT 'dd/mm/yyyy') nor CAST((" + dateStr + "
				 * (Date,Format 'dd/mm/yyyy')) As Date) because Hibernate does
				 * not recognize (and validate) those SQL functions. Therefore
				 * we must use a predefined date format (yyyy-MM-dd).
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

		it.eng.spagobi.tools.datasource.bo.IDataSource connection = (it.eng.spagobi.tools.datasource.bo.IDataSource) this
				.getEngineInstance().getDataSource().getConfiguration().loadDataSourceProperties().get("datasource");

		String dialect = connection.getHibDialectClass();

		if (dialect != null) {

			if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_MYSQL)) {
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
					toReturn = ""+toReturn+"";
				} else {
					toReturn = "'" + toReturn + "'";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_TERADATA)) {
				/*
				 * Unfortunately we cannot use neither CAST(" + dateStr + " AS
				 * DATE FORMAT 'dd/mm/yyyy') nor CAST((" + dateStr + "
				 * (Date,Format 'dd/mm/yyyy')) As Date) because Hibernate does
				 * not recognize (and validate) those SQL functions. Therefore
				 * we must use a predefined date format (yyyy-MM-dd).
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
