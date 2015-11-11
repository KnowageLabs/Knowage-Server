/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core.catalogue;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.model.structure.HierarchicalDimensionField;
import it.eng.qbe.model.structure.Hierarchy;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.AbstractSelectField;
import it.eng.qbe.query.ExpressionNode;
import it.eng.qbe.query.IQueryField;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.InLineCalculatedSelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.QueryMeta;
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
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

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
		QueryGraph queryGraph = null; // the query graph (the graph that involves all the entities of the query)
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

			Assert.assertNotNull(getEngineInstance(),
					"It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			Assert.assertNotNull(jsonEncodedCatalogue,
					"Input parameter [" + CATALOGUE + "] cannot be null in oder to execute " + this.getActionName() + " service");

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

			// we create a new query adding filters defined by profile attributes
			IModelAccessModality accessModality = this.getEngineInstance().getDataSource().getModelAccessModality();

			handleTimeFilters(query);

			Query filteredQuery = accessModality.getFilteredStatement(query, this.getDataSource(), userProfile.getUserAttributes());

			// loading the ambiguous fields
			Set<ModelFieldPaths> ambiguousFields = new HashSet<ModelFieldPaths>();
			Map<IModelField, Set<IQueryField>> modelFieldsMap = filteredQuery.getQueryFields(getDataSource());
			Set<IModelField> modelFields = modelFieldsMap.keySet();
			Set<IModelEntity> modelEntities = filteredQuery.getQueryEntities(modelFields);

			Map<String, Object> pathFiltersMap = new HashMap<String, Object>();
			pathFiltersMap.put(CubeFilter.PROPERTY_MODEL_STRUCTURE, getDataSource().getModelStructure());
			pathFiltersMap.put(CubeFilter.PROPERTY_ENTITIES, modelEntities);

			if (oldQueryGraph == null && filteredQuery != null) {// normal execution: a query exists
				queryGraph = updateQueryGraphInQuery(filteredQuery, forceReturnGraph, modelEntities);
				if (queryGraph != null) {
					// String modelName = getDataSource().getConfiguration().getModelName();
					// Graph<IModelEntity, Relationship> graph = getDataSource().getModelStructure().getRootEntitiesGraph(modelName,
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

			// we update the query graph in the query designed by the user in order to save it for later executions
			query.setQueryGraph(filteredQuery.getQueryGraph());

			// serialize the ambiguous fields
			ObjectMapper mapper = new ObjectMapper();
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
				isDierctlyExecutable = serialized == null || serialized.equals("") || serialized.equals("[]");// no ambiguos fields found so the query is
																												// executable;
			}

			List<String> queryErrors = QueryValidator.validate(query, getDataSource());
			String serializedQueryErrors = mapper.writeValueAsString(queryErrors);

			// String queryString = buildQueryString(getDataSource(), query);

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

	private void handleTimeFilters(Query query) {
		// DD: RECUPERO LA DIMENSIONE TEMPORALE
		IModelEntity temporalDimension = getTemporalDimension(getDataSource());
		IModelEntity timeDimension = getTimeDimension(getDataSource());

		if (temporalDimension != null) {
			HierarchicalDimensionField hierarchicalDimensionByEntity = temporalDimension.getHierarchicalDimensionByEntity(temporalDimension.getType());
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
					if ("TIME".equals(lValues[0])) {
						String temporalLevelColumn = null;
						String temporalLevel = whereField.getLeftOperand().description;
						temporalLevelColumn = defaultHierarchy.getLevelByType(temporalLevel);

						String temporalDimensionId = "time_id";

						// DD: RECUPERO IL PERIODO CORRENTE
						// TemporalRecord currentPeriodRecord = getCurrentPeriod(temporalDimension, "time_id", "quarter", "the_year");
						TemporalRecord currentPeriod = getCurrentPeriod(temporalDimension, temporalDimensionId, temporalLevelColumn, null,
								defaultHierarchy.getAncestors(temporalLevelColumn));
								// DD: RECUPERO L'INDICE DEL PERIODO CORRENTE

						// CURRENT
						if ("Current".equals(rValues[0])) {
							lValues[0] = temporalDimension.getType() + ":" + temporalDimensionId;
							rValues[0] = currentPeriod.getId().toString();
						} else if (whereField.getOperator().equals("LAST")) {

							// DD: RECUPERO TUTTI I RECORD NIZIALI DEGLI INTERVALLI DI INTERESSE
							LinkedList<TemporalRecord> allPeriodsStartingDate = loadAllPeriodsStartingDate(temporalDimension, temporalDimensionId,
									temporalLevelColumn, defaultHierarchy.getAncestors(temporalLevelColumn));

							int currentPeriodIndex = getCurrentIndex(allPeriodsStartingDate, (Integer) currentPeriod.getId());

							whereFieldsIndexesToRemove.add(whereFieldIndex);

							timeFilterIndex++;

							Operand left = new Operand(new String[] { temporalDimension.getType() + ":" + temporalDimensionId },
									temporalDimension.getName() + ":" + temporalDimensionId, "Field Content", new String[] {}, null);
							Operand maxRight = new Operand(new String[] { currentPeriod.getId().toString() }, currentPeriod.getId().toString(),
									"Static Content", new String[] {}, null);

							String maxFilterId = "TimeFilterMax" + timeFilterIndex;
							WhereField maxWhereField = new WhereField(maxFilterId, maxFilterId, false, left, "EQUALS OR LESS THAN", maxRight, "AND");
							nodesToAdd.add(maxFilterId);

							int offset = Integer.parseInt(rValues[0]);
							int oldestPeriodIndex = currentPeriodIndex - offset > 0 ? currentPeriodIndex - offset : 0;
							TemporalRecord oldestPeriod = allPeriodsStartingDate.get(oldestPeriodIndex);
							Operand minRight = new Operand(new String[] { oldestPeriod.getId().toString() }, oldestPeriod.getId().toString(), "Static Content",
									new String[] {}, null);

							String minFilterId = "TimeFilterMax" + timeFilterIndex;
							WhereField minWhereField = new WhereField(minFilterId, minFilterId, false, left, "EQUALS OR GREATER THAN", minRight, "AND");
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
				query.addWhereField(whereFieldToAdd.getName(), whereFieldToAdd.getDescription(), whereFieldToAdd.isPromptable(),
						whereFieldToAdd.getLeftOperand(), whereFieldToAdd.getOperator(), whereFieldToAdd.getRightOperand(),
						whereFieldToAdd.getBooleanConnector());

			}

			query.updateWhereClauseStructure();

			handleInLineTemporalFilter(query, temporalDimension, defaultHierarchy);

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

	private void handleInLineTemporalFilter(Query query, IModelEntity temporalDimension, Hierarchy defaultHierarchy) {

		final String temporalDimensionId = "time_id";

		final String TEMPORAL_OPERAND_YTD = "YTD";
		final String TEMPORAL_OPERAND_QTD = "QTD";
		final String TEMPORAL_OPERAND_MTD = "MTD";
		final String TEMPORAL_OPERAND_WTD = "WTD";
		final String TEMPORAL_OPERAND_LAST_YEAR = "LAST_YEAR";
		final String TEMPORAL_OPERAND_LAST_MONTH = "LAST_MONTH";
		final String TEMPORAL_OPERAND_LAST_WEEK = "LAST_WEEK";
		final String TEMPORAL_OPERAND_PARALLEL_YEAR = "PARALLEL_YEAR";
		final String TEMPORAL_OPERAND_PARALLEL_MONTH = "PARALLEL_MONTH";
		final String TEMPORAL_OPERAND_PARALLEL_WEEK = "PARALLEL_WEEK";

		final String TEMPORAL_FIELD_YEAR = "YEAR";
		final String TEMPORAL_FIELD_QUARTER = "QUARTER";
		final String TEMPORAL_FIELD_MONTH = "MONTH";
		final String TEMPORAL_FIELD_WEEK = "WEEK";
		final String TEMPORAL_FIELD_DAY = "DAY";

		final String EQUALS_TO = " = ";
		final String AND = " and ";
		final String GREATER_EQUALS = " >= ";
		final String LESS_EQUALS = " <= ";

		List<ISelectField> selectFieldsToRemove = new LinkedList<ISelectField>();
		List<ISelectField> selectFieldsToAdd = new LinkedList<ISelectField>();

		List<ISelectField> selectFields = query.getSelectFields(true);
		for (ISelectField iSelectField : selectFields) {
			if (((AbstractSelectField) iSelectField).isSimpleField()) {
				SimpleSelectField sField = (SimpleSelectField) iSelectField;
				String temporalOperand = sField.getTemporalOperand();
				String temporalOperandParameter = sField.getTemporalOperandParameter();
				if (StringUtilities.isNotEmpty(temporalOperand)) {
					// SIAMO IN PRESENZA DI FILTRO TEMPORALE INLINE

					selectFieldsToRemove.add(iSelectField);

					String entity = temporalDimension.getType();
					String temporalCondition = " 1 = 1 ";
					String id = entity + ":" + temporalDimensionId;
					String year = entity + ":" + defaultHierarchy.getLevelByType(TEMPORAL_FIELD_YEAR);
					String quarter = entity + ":" + defaultHierarchy.getLevelByType(TEMPORAL_FIELD_QUARTER);
					String month = entity + ":" + defaultHierarchy.getLevelByType(TEMPORAL_FIELD_MONTH);
					String week = entity + ":" + defaultHierarchy.getLevelByType(TEMPORAL_FIELD_WEEK);

					Date actualTime = new Date();

					// type-safe
					if (!temporalOperandParameter.matches("\\d+"))
						temporalOperandParameter = "0";
					boolean isSIMPLEOperand = false;
					boolean isLASTOperand = false;
					boolean isParallelOperand = false;
					Integer slideBackHowMuch = Integer.parseInt(temporalOperandParameter);
					slideBackHowMuch *= -1;
					Calendar c = new GregorianCalendar();
					int slideWhat = -1;
					if (temporalOperand.equals(TEMPORAL_OPERAND_YTD)) {
						isSIMPLEOperand = true;
						slideWhat = Calendar.YEAR;
					} else if (temporalOperand.equals(TEMPORAL_OPERAND_QTD)) {
						isSIMPLEOperand = true;
						slideWhat = Calendar.MONTH;
						slideBackHowMuch *= 3;
					} else if (temporalOperand.equals(TEMPORAL_OPERAND_MTD)) {
						isSIMPLEOperand = true;
						slideWhat = Calendar.MONTH;
					} else if (temporalOperand.equals(TEMPORAL_OPERAND_WTD)) {
						isSIMPLEOperand = true;
						slideWhat = Calendar.WEEK_OF_YEAR;
					} else if (temporalOperand.equals(TEMPORAL_OPERAND_LAST_YEAR)) {
						isLASTOperand = true;
						slideWhat = Calendar.YEAR;
					} else if (temporalOperand.equals(TEMPORAL_OPERAND_LAST_MONTH)) {
						isLASTOperand = true;
						slideWhat = Calendar.MONTH;
					} else if (temporalOperand.equals(TEMPORAL_OPERAND_LAST_WEEK)) {
						isLASTOperand = true;
						slideWhat = Calendar.WEEK_OF_YEAR;
					} else if (temporalOperand.equals(TEMPORAL_OPERAND_PARALLEL_YEAR)) {
						isParallelOperand = true;
						slideWhat = Calendar.YEAR;
					} else if (temporalOperand.equals(TEMPORAL_OPERAND_PARALLEL_MONTH)) {
						isParallelOperand = true;
						slideWhat = Calendar.MONTH;
					} else if (temporalOperand.equals(TEMPORAL_OPERAND_PARALLEL_YEAR)) {
						isParallelOperand = true;
						slideWhat = Calendar.WEEK_OF_YEAR;
					}

					if (slideWhat > 0 && !isLASTOperand) {
						c.add(slideWhat, slideBackHowMuch);
						actualTime = c.getTime();
					}

					TemporalRecord actualPeriod = getCurrentPeriod(temporalDimension, defaultHierarchy, TEMPORAL_FIELD_DAY, temporalDimensionId, actualTime);
					if (actualPeriod != null /* || isLASTOperand || isParallelOperand */) {

						String beforeOrActualPeriod = " " + id + LESS_EQUALS + actualPeriod.getId();
						if (isSIMPLEOperand) {

							String yearValueBounded = getActualPeriodValueBounded(actualTime, temporalDimension, defaultHierarchy, temporalDimensionId,
									TEMPORAL_FIELD_YEAR, year);
							String withinThisYear = year + EQUALS_TO + yearValueBounded;

							if (temporalOperand.equals(TEMPORAL_OPERAND_YTD)) {
								temporalCondition = beforeOrActualPeriod + AND + withinThisYear + " ";
							} else if (temporalOperand.equals(TEMPORAL_OPERAND_QTD)) {
								String quarterValueBounded = getActualPeriodValueBounded(actualTime, temporalDimension, defaultHierarchy, temporalDimensionId,
										TEMPORAL_FIELD_QUARTER, quarter);
								String withinThisQuarter = quarter + EQUALS_TO + quarterValueBounded;

								temporalCondition = beforeOrActualPeriod + AND + withinThisYear + AND + withinThisQuarter + " ";
							} else if (temporalOperand.equals(TEMPORAL_OPERAND_MTD)) {
								String monthValueBounded = getActualPeriodValueBounded(actualTime, temporalDimension, defaultHierarchy, temporalDimensionId,
										TEMPORAL_FIELD_MONTH, month);
								String withinThisMonth = month + EQUALS_TO + monthValueBounded;

								temporalCondition = beforeOrActualPeriod + AND + withinThisYear + AND + withinThisMonth + " ";
							} else if (temporalOperand.equals(TEMPORAL_OPERAND_WTD)) {
								String weekValueBounded = getActualPeriodValueBounded(actualTime, temporalDimension, defaultHierarchy, temporalDimensionId,
										TEMPORAL_FIELD_WEEK, week);
								String withinThisMonth = week + EQUALS_TO + weekValueBounded;

								temporalCondition = beforeOrActualPeriod + AND + withinThisYear + AND + withinThisMonth + " ";
							}

						} else if (isLASTOperand) {

							String temporalField = TEMPORAL_FIELD_YEAR;

							if (temporalOperand.equals(TEMPORAL_OPERAND_LAST_YEAR)) {
								temporalField = TEMPORAL_FIELD_YEAR;
							} else if (temporalOperand.equals(TEMPORAL_OPERAND_LAST_MONTH)) {
								temporalField = TEMPORAL_FIELD_MONTH;
							} else if (temporalOperand.equals(TEMPORAL_OPERAND_LAST_WEEK)) {
								temporalField = TEMPORAL_FIELD_WEEK;
							}

							LinkedList<TemporalRecord> allPeriodsStartingDate = loadAllPeriodsStartingDate(temporalDimension, temporalDimensionId,
									defaultHierarchy.getLevelByType(temporalField),
									defaultHierarchy.getAncestors(defaultHierarchy.getLevelByType(temporalField)));

							TemporalRecord currentPeriod = getCurrentPeriod(temporalDimension, defaultHierarchy, temporalField, temporalDimensionId,
									actualTime);
							int currentPeriodIndex = getCurrentIndex(allPeriodsStartingDate, (Integer) currentPeriod.getId());
							int oldestPeriodIndex = currentPeriodIndex + slideBackHowMuch > 0 ? currentPeriodIndex + slideBackHowMuch : 0;
							TemporalRecord oldestPeriod = allPeriodsStartingDate.get(oldestPeriodIndex);

							String fromStartingDate = id + GREATER_EQUALS + oldestPeriod.getId();

							temporalCondition = fromStartingDate + AND + beforeOrActualPeriod;

						} else if (isParallelOperand) {
							String temporalField = TEMPORAL_FIELD_YEAR;

							if (temporalOperand.equals(TEMPORAL_OPERAND_PARALLEL_YEAR)) {
								temporalField = TEMPORAL_FIELD_YEAR;
							} else if (temporalOperand.equals(TEMPORAL_OPERAND_PARALLEL_MONTH)) {
								temporalField = TEMPORAL_FIELD_MONTH;
							} else if (temporalOperand.equals(TEMPORAL_OPERAND_PARALLEL_WEEK)) {
								temporalField = TEMPORAL_FIELD_WEEK;
							}

							LinkedList<TemporalRecord> allPeriodsStartingDate = loadAllPeriodsStartingDate(temporalDimension, temporalDimensionId,
									defaultHierarchy.getLevelByType(temporalField),
									defaultHierarchy.getAncestors(defaultHierarchy.getLevelByType(temporalField)));

							TemporalRecord currentPeriod = getCurrentPeriod(temporalDimension, defaultHierarchy, temporalField, temporalDimensionId,
									actualTime);
							int currentPeriodIndex = getCurrentIndex(allPeriodsStartingDate, (Integer) currentPeriod.getId());
							int oldestPeriodIndex = currentPeriodIndex + slideBackHowMuch > 0 ? currentPeriodIndex + slideBackHowMuch : 0;
							int youngestPeriodIndex = oldestPeriodIndex + 1 < allPeriodsStartingDate.size() ? oldestPeriodIndex + 1 : -1;
							TemporalRecord oldestPeriod = allPeriodsStartingDate.get(oldestPeriodIndex);
							TemporalRecord youngestPeriod = youngestPeriodIndex > 0 ? allPeriodsStartingDate.get(youngestPeriodIndex) : null;

							String fromStartingDate = id + GREATER_EQUALS + oldestPeriod.getId();
							String toStartingDate = youngestPeriod != null ? id + LESS_EQUALS + youngestPeriod.getId() : " 1 = 1 ";

							temporalCondition = fromStartingDate + AND + toStartingDate;

						}
					} else {
						temporalCondition = " 1 <> 1 ";
					}

					String expression = "( case when " + temporalCondition + " then " + sField.getName() + " else NULL end ) ";

					IModelField field = getDataSource().getModelStructure().getField(sField.getName());

					String nature = field.getProperties().get("type").toString().toUpperCase();
					String type = "STRING";
					if (nature.equals("MEASURE")) {
						type = "NUMBER";
					}
					String fieldAlias = sField.getAlias() + "_" + temporalOperand + "_" + temporalOperandParameter;

					InLineCalculatedSelectField icField = new InLineCalculatedSelectField(fieldAlias, expression, "", type, nature, sField.isIncluded(),
							sField.isVisible(), sField.isGroupByField(), sField.getOrderType(), sField.getFunction().getName());

					selectFieldsToAdd.add(icField);
				}
			}
		}

		for (ISelectField toRemove : selectFieldsToRemove) {
			//query.removeSelectField(toRemove);
		}

		for (ISelectField iSelectField : selectFieldsToAdd) {
			InLineCalculatedSelectField icField = (InLineCalculatedSelectField) iSelectField;
			query.addInLineCalculatedFiled(icField.getAlias(), icField.getExpression(), icField.getSlots(), icField.getType(), icField.getNature(),
					icField.isIncluded(), icField.isVisible(), icField.isGroupByField(), icField.getOrderType(), icField.getFunction().getName());
		}

	}

	private String getActualPeriodValueBounded(Date actualTime, IModelEntity temporalDimension, Hierarchy defaultHierarchy, String temporalDimensionId,
			String temporalFieldType, String fieldUniqueName) {

		String temporalLevel = temporalFieldType;// defaultHierarchy.getLevelByType(temporalFieldType);

		TemporalRecord value = getCurrentPeriod(temporalDimension, defaultHierarchy, temporalLevel, temporalDimensionId, actualTime);
		IModelField field = getDataSource().getModelStructure().getField(fieldUniqueName);
		return getValueBounded(value.getPeriod().toString(), field.getType());
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

	private TemporalRecord getCurrentPeriod(IModelEntity temporalDimension, Hierarchy defaultHierarchy, String temporalLevel, String temporalDimensionId,
			Date actualTime) {
		String temporalLevelColumn;
		temporalLevelColumn = defaultHierarchy.getLevelByType(temporalLevel);
		TemporalRecord currentPeriod = getCurrentPeriod(temporalDimension, temporalDimensionId, temporalLevelColumn, actualTime,
				defaultHierarchy.getAncestors(temporalLevelColumn));
		return currentPeriod;
	}

	private TemporalRecord getCurrentPeriod(IModelEntity temporalDimension, String idField, String periodField, Date actualTime, String... parentPeriodFields) {

		// nullsafe
		parentPeriodFields = parentPeriodFields != null ? parentPeriodFields : new String[0];

		actualTime = actualTime != null ? actualTime : new Date();

		Query currentPeriodQuery = new Query();
		currentPeriodQuery.addSelectFiled(temporalDimension.getType() + ":" + idField, null, "ID", true, true, false, "ASC", null);
		currentPeriodQuery.addSelectFiled(temporalDimension.getType() + ":" + periodField, null, "LEVEL", true, true, false, null, null);
		for (String parentPeriodField : parentPeriodFields) {
			currentPeriodQuery.addSelectFiled(temporalDimension.getType() + ":" + parentPeriodField, null, parentPeriodField, true, true, false, null, null);
		}

		String temporalDimensionDateField = "the_date";

		Operand left = new Operand(new String[] { temporalDimension.getType() + ":" + temporalDimensionDateField },
				temporalDimension.getName() + ":" + temporalDimensionDateField, "Field Content", new String[] {}, null);
		Operand right = new Operand(new String[] { new SimpleDateFormat("dd/MM/yyyy").format(actualTime) },
				new SimpleDateFormat("dd/MM/yyyy").format(actualTime), "Static Content", new String[] {}, null);
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
	}

	private LinkedList<TemporalRecord> loadAllPeriodsStartingDate(IModelEntity temporalDimension, String idField, String periodField,
			String... parentPeriodFields) {

		// nullsafe
		parentPeriodFields = parentPeriodFields != null ? parentPeriodFields : new String[] {};

		Query periodsStartingDates = new Query();
		periodsStartingDates.addSelectFiled(temporalDimension.getType() + ":" + idField, "MIN", "ID", true, true, false, "ASC", null);
		periodsStartingDates.addSelectFiled(temporalDimension.getType() + ":" + periodField, null, "LEVEL", true, true, true, null, null);
		for (String parentPeriodField : parentPeriodFields) {
			periodsStartingDates.addSelectFiled(temporalDimension.getType() + ":" + parentPeriodField, null, parentPeriodField, true, true, true, null, null);
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

	private IDataStore executeDatamartQuery(Query myquery) {
		this.getEngineInstance().getQueryCatalogue().addQuery(myquery);
		this.getEngineInstance().setActiveQuery(myquery);
		AbstractQbeDataSet qbeDataSet = (AbstractQbeDataSet) this.getEngineInstance().getActiveQueryAsDataSet();

		String queryString = qbeDataSet.getStatement().getQueryString();
		logger.debug("QUERY STRING: " + queryString);

		qbeDataSet.loadData();
		IDataStore dataStore = qbeDataSet.getDataStore();

		return dataStore;
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
				return false;// if at least one entity contained in the query is not covered by the old cover graph the old graph is not valid
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

	private QueryMeta deserializeMeta(JSONObject metaJSON) throws JSONException {
		QueryMeta meta = new QueryMeta();
		meta.setId(metaJSON.getString("id"));
		meta.setName(metaJSON.getString("name"));
		return null;
	}

	private Query deserializeQuery(JSONObject queryJSON) throws SerializationException, JSONException {
		// queryJSON.put("expression", queryJSON.get("filterExpression"));
		return SerializerFactory.getDeserializer("application/json").deserializeQuery(queryJSON.toString(), getEngineInstance().getDataSource());
	}

	public static List<ModelFieldPaths> deserializeList(String serialized, Collection<Relationship> relationShips, IModelStructure modelStructure, Query query)
			throws SerializationException {
		ObjectMapper mapper = new ObjectMapper();
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
	 * TODO metodo copiato da: AbstractStatementFilteringClause
	 *
	 * STANDARDIZZARE
	 *
	 */
	public String getValueBounded(String operandValueToBound, String operandType) {

		String boundedValue = operandValueToBound;
		if (operandType.equalsIgnoreCase("STRING") || operandType.equalsIgnoreCase("CHARACTER") || operandType.equalsIgnoreCase("java.lang.String")
				|| operandType.equalsIgnoreCase("java.lang.Character")) {

			// if the value is already surrounded by quotes, does not neither add quotes nor escape quotes
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
	 * TODO metodo copiato da: AbstractStatementClause
	 *
	 * STANDARDIZZARE
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
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = " TO_DATE(" + toReturn + ",'DD/MM/YYYY HH24:MI:SS') ";
				} else {
					toReturn = " TO_DATE('" + toReturn + "','DD/MM/YYYY HH24:MI:SS') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE9i10g)) {
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
					toReturn = toReturn;
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
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE)) {
				if (toReturn.startsWith("'") && toReturn.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + toReturn + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + toReturn + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE9i10g)) {
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
					toReturn = toReturn;
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
