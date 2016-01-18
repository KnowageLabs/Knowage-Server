/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.graph.GraphManager;
import it.eng.qbe.statement.graph.ModelFieldPaths;
import it.eng.qbe.statement.graph.QueryGraphBuilder;
import it.eng.qbe.statement.graph.bean.PathChoice;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;
import it.eng.qbe.statement.graph.serializer.ModelFieldPathsJSONDeserializer;
import it.eng.qbe.statement.hibernate.HQLDataSet;
import it.eng.qbe.statement.jpa.JPQLDataSet;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.services.core.catalogue.SetCatalogueAction;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jgrapht.Graph;
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
 * The Class ExecuteQueryAction.
 */
public class ExecuteQueryAction extends AbstractQbeEngineAction {

	private static final long serialVersionUID = -8812774864345259197L;

	// INPUT PARAMETERS
	public static final String LIMIT = "limit";
	public static final String START = "start";
	public static final String QUERY_ID = "id";
	public static final String AMBIGUOUS_FIELDS_PATHS = "ambiguousFieldsPaths";

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(ExecuteQueryAction.class);
	public static transient Logger auditlogger = Logger.getLogger("audit.query");

	@Override
	public void service(SourceBean request, SourceBean response) {
		// (Locale)getEngineInstance().getEnv().get(EngineConstants.ENV_LOCALE);

		// String queryId = null;
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

			super.service(request, response);

			totalTimeMonitor = MonitorFactory.start("QbeEngine.executeQueryAction.totalTime");

			Object startO = getAttribute(START);
			if (startO != null && !startO.toString().equals("")) {
				start = getAttributeAsInteger(START);
			}
			logger.debug("Parameter [" + START + "] is equals to [" + start + "]");

			Object limitO = getAttribute(LIMIT);
			if (limitO != null && !limitO.toString().equals("")) {
				limit = getAttributeAsInteger(LIMIT);
			}

			logger.debug("Parameter [" + LIMIT + "] is equals to [" + limit + "]");

			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName()
					+ " service before having properly created an instance of EngineInstance class");

			// retrieving query specified by id on request
			query = getQuery();
			Assert.assertNotNull(query, "Query object with id [" + getAttributeAsString(QUERY_ID) + "] does not exist in the catalogue");
			if (getEngineInstance().getActiveQuery() == null || !getEngineInstance().getActiveQuery().getId().equals(query.getId())) {
				logger.debug("Query with id [" + query.getId() + "] is not the current active query. A new statment will be generated");
				getEngineInstance().setActiveQuery(query);
			}

			// promptable filters values may come with request (read-only user modality)
			updatePromptableFiltersValue(query, this);

			dataStore = executeQuery(start, limit);
			dataStore = handleTimeAggregations(dataStore, start, limit);

			resultNumber = (Integer) dataStore.getMetaData().getProperty("resultNumber");

			logger.debug("Total records: " + resultNumber);

			
			boolean overflow = maxSize != null && resultNumber >= maxSize;
			if (overflow) {
				logger.warn("Query results number [" + resultNumber + "] exceeds max result limit that is [" + maxSize + "]");
				// auditlogger.info("[" + userProfile.getUserId() + "]:: max result limit [" + maxSize + "] exceeded with SQL: " + sqlQuery);
			}

			gridDataFeed = serializeDataStore(dataStore);

			try {
				writeBackToClient(new JSONSuccess(gridDataFeed));
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



	private IDataStore handleTimeAggregations(IDataStore fullDatastore, Integer start, Integer limit) {
		
		Query query = this.getQuery();
		Map<String, Map<String, String>> inlineFilteredSelectFields = query.getInlineFilteredSelectFields();
		
		if(inlineFilteredSelectFields != null && inlineFilteredSelectFields.size() > 0) {
			IDataStore finalDatastore = null;
			
			/*
			 * DATA FOR AGGREGATION
			 * */
			Set<String> aliasesToBeRemovedAfterExecution = query.getAliasesToBeRemovedAfterExecution();
			Map<String, String> hierarchyFullColumnMap = query.getHierarchyFullColumnMap();
			LinkedList<String> allYearsOnDWH = query.getAllYearsOnDWH();
			int relativeYearIndex = query.getRelativeYearIndex();
			Set<String> temporalFieldTypesInQuery = query.getTemporalFieldTypesInQuery();
			Map<String, List<String>> distinctPeriods = query.getDistinctPeriods();
			
			// per comodit� riorganizzo i periodi per type
			Map<String, List<String>> distinctPeriodsByType = new HashMap<>();
			for (String type : hierarchyFullColumnMap.keySet()) {
				distinctPeriodsByType.put(type, distinctPeriods.get( hierarchyFullColumnMap.get(type)));	
			}
			
			/*
			 * END DATA FOR AGGREGATION
			 * */
			
			// elimino le groupby aggiuntive per ottenere tutte le righe della query finale
			List<ISelectField> selectFields = query.getSelectFields(false);
			for (ISelectField sfield : selectFields) {
				if (sfield.isSimpleField()) {
					SimpleSelectField ssField = (SimpleSelectField) sfield;
					if(aliasesToBeRemovedAfterExecution != null && aliasesToBeRemovedAfterExecution.contains(ssField.getUniqueName())) {
						ssField.setGroupByField(false);
						ssField.setFunction(AggregationFunctions.COUNT_FUNCTION);
					}
				}
			}
			
			// eseguo la query per avere il numero di righe finale
			finalDatastore = executeQuery(start, limit);
			
			// aggrego!
			for (Iterator finalIterator = finalDatastore.iterator(); finalIterator.hasNext();) {
				Record finalRecord = (Record) finalIterator.next();

				Map<String, String> rowPeriodValuesByType = new HashMap<>();
				for (String type : temporalFieldTypesInQuery) {
					for (int fieldIndex = 0; fieldIndex < finalDatastore.getMetaData().getFieldCount(); fieldIndex++) {
						String fieldName = finalDatastore.getMetaData().getFieldName(fieldIndex);
						if(fieldName != null && query.getTemporalFieldTypesInQuery().contains(fieldName)){
							rowPeriodValuesByType.put(fieldName, finalRecord.getFieldAt(fieldIndex).getValue().toString()); 
						}
					}
				}
				
				
				// recupero l'identificativo della riga, rappresentato 
				// come coppie alias/valore
				Map<String, String> currentRecordId = getRecordAggregatedId(finalRecord, finalDatastore, query);
				
				
				
				
				// Creo una mappa per tipo in cui tutti gli elementi sono numerati es i mesi da 0 a 11, i quarter da 0 a 3
				Map<String, Integer> rowPeriodsNumbered = new HashMap<>();
				for (String type : rowPeriodValuesByType.keySet()) {
					String currentPeriodValue = rowPeriodValuesByType.get(type);
					List<String> distinctPeriodsForThisType = distinctPeriods.get(type);
					int currentValueIndexForThisType = -1;
					for(int i = 0; i< distinctPeriodsForThisType.size(); i++) {
						String period = distinctPeriodsForThisType.get(i);
						if(period.equals(currentPeriodValue)) {
							currentValueIndexForThisType = i;
							break;
						}
					}
					rowPeriodsNumbered.put(type, currentValueIndexForThisType);	
				}
				
				
				String rowLog = "| ";
				
				// per ogni colonna di ogni riga, se c'� un operatore inline, ne calcolo il valore
				for (int fieldIndex = 0; fieldIndex < finalDatastore.getMetaData().getFieldCount(); fieldIndex++) {
					Map<String, String> firstRecordId = new HashMap<>();
					firstRecordId.putAll(currentRecordId);
					Map<String, String> lastRecordId = new HashMap<>();
					lastRecordId.putAll(currentRecordId);
					
					String fieldAlias = finalDatastore.getMetaData().getFieldAlias(fieldIndex);
					// se la colonna � da calcolare...
					if(fieldAlias != null && inlineFilteredSelectFields.containsKey(fieldAlias)){
						
						Map<String, String> inlineParameters = inlineFilteredSelectFields.get(fieldAlias);
						String temporalOperand = inlineParameters.get("temporalOperand");
						String temporalOperandParameter_str = inlineParameters.get("temporalOperandParameter");
						int temporalOperandParameter = Integer.parseInt(temporalOperandParameter_str);
						
						String periodType = null;
						boolean lastPeriod = false;
						switch (temporalOperand) {
						
						// PERIOD_TO_DATE
						// per i PERIOD_TO_DATE devo recuperare l'id temporale della riga  da cui partire, 
						// quella a cui fermarmi corrisponde con la riga corrente traslata nel periodo di riferimento
						// YTD_1 per la riga corrispondente a Giugno 2016 visualizzer� il dato aggregato da inizio 2015 a tutto Giugno 2015
						case SetCatalogueAction.TEMPORAL_OPERAND_YTD:
						{
							// PORTO AL PRIMO RECORD DEL ANNO
							for (String fieldType : temporalFieldTypesInQuery) {
								if(!hierarchyFullColumnMap.get("YEAR").equals(fieldType)) {
									firstRecordId.put(fieldType, distinctPeriods.get(fieldType).get(0));
								}
							}
							int parallelYearIndex = relativeYearIndex - temporalOperandParameter;
							if(parallelYearIndex >= 0 && allYearsOnDWH.size() > parallelYearIndex -1 ) {
								String parallelYear =  allYearsOnDWH.get(parallelYearIndex);
								firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), parallelYear);
								lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), parallelYear);
							}
							else {
								firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), null);
								lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), null);
							}
							break;
						}	
						case SetCatalogueAction.TEMPORAL_OPERAND_QTD:
							if (periodType == null) {
								periodType = "QUARTER";
							}
						case SetCatalogueAction.TEMPORAL_OPERAND_MTD:
							if (periodType == null) {
								periodType = "MONTH";
							}
						case SetCatalogueAction.TEMPORAL_OPERAND_WTD:
							if (periodType == null) {
								periodType = "WEEK";
							}
						case SetCatalogueAction.TEMPORAL_OPERAND_LAST_QUARTER:
							if (periodType == null) {
								periodType = "QUARTER";
								lastPeriod = true;
							}

						case SetCatalogueAction.TEMPORAL_OPERAND_LAST_MONTH:
							if (periodType == null) {
								periodType = "MONTH";
								lastPeriod = true;
							}

						case SetCatalogueAction.TEMPORAL_OPERAND_LAST_WEEK:
							if (periodType == null) {
								periodType = "WEEK";
								lastPeriod = true;
							}
						{
							// PORTO AL PRIMO RECORD DEL PERIODO (nell'anno)
							for (String fieldType : temporalFieldTypesInQuery) {
								if(!hierarchyFullColumnMap.get("YEAR").equals(fieldType) &&
								   !hierarchyFullColumnMap.get(periodType).equals(fieldType)) {
									firstRecordId.put(fieldType, distinctPeriods.get(fieldType).get(0));
								}
							}
							
							Integer rowPeriodNumber = rowPeriodsNumbered.get(hierarchyFullColumnMap.get(periodType));
							Integer otherPeriodNumber = rowPeriodNumber - temporalOperandParameter;
							if(otherPeriodNumber < rowPeriodNumber) {
								otherPeriodNumber = otherPeriodNumber + 1;
							}
							else {
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
							
							int yearOtherIndex = (int) (relativeYearIndex + yearOffset);
							if(yearOtherIndex < 0) {
								yearOtherIndex = 0;
							}
							if(yearOtherIndex >= allYearsOnDWH.size()) {
								yearOtherIndex = allYearsOnDWH.size() -1;
								periodOtherIndex = periods.size() -1;
							}
							// L'ANNO LO DEVO METTERE SOLO SE PRESENTE TRA I CAMPI DELLA SELECT ???
							firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.get(yearOtherIndex));
							firstRecordId.put(hierarchyFullColumnMap.get(periodType), periods.get(periodOtherIndex));
							
							if(lastPeriod) {
								// se operatore last, aggrego fino al periodo della riga corrente
								lastRecordId.put(hierarchyFullColumnMap.get(periodType), rowPeriodValuesByType.get(hierarchyFullColumnMap.get(periodType)));
								lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.get(relativeYearIndex));
							}
							else {
								// se operatore period to date, aggrego fino allo stesso 'tempo' nel periodo di riferimento
								lastRecordId.put(hierarchyFullColumnMap.get(periodType), periods.get(periodOtherIndex));
								lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.get(yearOtherIndex));
							}
							break;
						}
							
							
						// LAST_PERIOD
						// per i LAST_PERIOD devo recuperare l'id temporale della riga da cui partire, 
						// quella a cui fermarmi corrisponde con la riga corrente
						// LM_3 per la riga Giugno 2016 visualizzer� il dato aggregato da Aprile a Giugno 2015
						// LM_4 per la riga Gennaio 2016 visualizzer� il dato aggregato da Ottobre 2015 a Gennaio 2016
						case SetCatalogueAction.TEMPORAL_OPERAND_LAST_YEAR:
						{
							// setta gennaio/Q1/W1
							for (String fieldType : temporalFieldTypesInQuery) {
								if(!hierarchyFullColumnMap.get("YEAR").equals(fieldType)) {
									firstRecordId.put(fieldType, distinctPeriods.get(fieldType).get(0));
								}
							}
							
							int parallelYearIndex = relativeYearIndex - temporalOperandParameter;
							if(parallelYearIndex >= 0 && allYearsOnDWH.size() > parallelYearIndex ) {
								String parallelYear =  allYearsOnDWH.get(parallelYearIndex);
								firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), parallelYear);
								lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.get(relativeYearIndex));
							}
							else {
								firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), null);
							}
							
							break;
						}

						// PARALLEL_PERIOD
						case SetCatalogueAction.TEMPORAL_OPERAND_PARALLEL_YEAR:
						{
							// i parallel years si calcolano sempre in funzione di quello che trovo nella where
							
							String year = null;
							
							int parallelYearIndex = relativeYearIndex - temporalOperandParameter;
							if(parallelYearIndex >= 0 && allYearsOnDWH.size() > parallelYearIndex -1 ) {
								year =  allYearsOnDWH.get(parallelYearIndex);
							}
							firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), year);
							lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), year);
							break;
						}
						default:
							break;
						}
						
						double finalValue = 0D;
						boolean firstRecordFound = false;
						/** INQUESTO CICLO DEVO UTILIZZARE I CAMPI FIRST E LAST */
						for (Iterator fullIterator = fullDatastore.iterator(); fullIterator.hasNext();) {
							Record record = (Record) fullIterator.next();
							Map<String, String> recordId = getRecordFullId(record, finalDatastore, query);
							if(recordId.equals(firstRecordId)) {
								firstRecordFound = true;
							}
							
							if(firstRecordFound) {
								finalValue += Double.parseDouble(record.getFieldAt(fieldIndex).getValue().toString());
							}
							
							if(recordId.equals(lastRecordId)) {
								finalRecord.getFieldAt(fieldIndex).setValue(finalValue);
								break;
							}
						}
						
						rowLog += " | " + firstRecordId + " >>> " + lastRecordId;
					}
					else {
						rowLog += " | NON AGGREGATO ";
					}
				}
				
				logger.debug(rowLog);
				
			}
			
			return finalDatastore;
			
		}
		else {
			return fullDatastore;
		}
		
		
	}
	
	private Map<String, String> getRecordAggregatedId(Record finalRecord, IDataStore finalDatastore, Query query) {
		Set<String> idAliases = query.getTemporalFieldTypesInSelect();
		return getRecordId(finalRecord, finalDatastore, query, idAliases);
	}
	 
	private Map<String, String> getRecordFullId(Record finalRecord, IDataStore finalDatastore, Query query) {
		Set<String> idAliases = query.getTemporalFieldTypesInQuery();
		return getRecordId(finalRecord, finalDatastore, query, idAliases);
	}
	
	private Map<String, String> getRecordId(Record finalRecord, IDataStore finalDatastore, Query query, Set<String> idAliases) {
		Map<String, String> recordId = new HashMap<>();
		for (int fieldIndex = 0; fieldIndex < finalDatastore.getMetaData().getFieldCount(); fieldIndex++) {
			String fieldName = finalDatastore.getMetaData().getFieldName(fieldIndex);
			if(fieldName != null && idAliases.contains(fieldName)){
				recordId.put(fieldName, (finalRecord.getFieldAt(fieldIndex).getValue() != null ? finalRecord.getFieldAt(fieldIndex).getValue().toString(): "") );
			}
		}
		return recordId;
	}



	private void handleTTTTTTimeAggregations(IDataStore dataStore) {
		final String PY = "_PY_";
		final String LY = "_LY_";
		final String LM = "_LM_";
		final String LW = "_LW_";
		
		Map<String, LinkedList<Integer>> groupedFieldsIndexMap = new LinkedHashMap<String, LinkedList<Integer>>();
		
		Map<Object, Record> finalResultMap = new LinkedHashMap<Object, Record>();
		Integer yearIndex = null;
		for (int fieldIndex = 0; fieldIndex < dataStore.getMetaData().getFieldCount(); fieldIndex++) {
			String fieldAlias = dataStore.getMetaData().getFieldAlias(fieldIndex);

			if(fieldAlias != null){
				
				extractGroupedFieldsIndexes(groupedFieldsIndexMap, fieldIndex, fieldAlias, PY);
				extractGroupedFieldsIndexes(groupedFieldsIndexMap, fieldIndex, fieldAlias, LY);
				extractGroupedFieldsIndexes(groupedFieldsIndexMap, fieldIndex, fieldAlias, LM);
				extractGroupedFieldsIndexes(groupedFieldsIndexMap, fieldIndex, fieldAlias, LW);

				if(yearIndex == null && fieldAlias.equals("YEAR")) {
					yearIndex = fieldIndex;
				}
			}
		}

		IRecord r = new Record();
		
		IDataStore ds = new DataStore();
		ds.appendRecord(r);
		
		LinkedList<Integer> pyFields = groupedFieldsIndexMap.get(PY);	
		LinkedList<Integer> lyFields = groupedFieldsIndexMap.get(LY);
		
		// for each record 
		for (Iterator iterator = dataStore.iterator(); iterator.hasNext();) {
			Record record = (Record) iterator.next();
			List<IField> fields = record.getFields();

			//for each field
			for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
				IField currentField = fields.get(fieldIndex);
				if(pyFields != null && pyFields.contains(fieldIndex)) {
					Object currentKey = fields.get(yearIndex).getValue();
					
					Record finalRecord = finalResultMap.get(currentKey);
					if(finalRecord == null) {
						finalResultMap.put(currentKey, record);
					}
					else {
						IField finalField = finalRecord.getFieldAt(fieldIndex);
						if(finalField != null) {
							double finalValue = Double.parseDouble(finalField.getValue() != null ? finalField.getValue().toString() : "0");
							double currentValue = Double.parseDouble(currentField.getValue() != null ? currentField.getValue().toString() : "0");
							// MAX MIN AVG COUNT ? ? ? 
							finalField.setValue(finalValue+currentValue);
						}
					}
				}
			}
		}

		// UPDATING RESULTSET
		// for each record 
		for (Iterator iterator = dataStore.iterator(); iterator.hasNext();) {
			Record record = (Record) iterator.next();
			List<IField> fields = record.getFields();
			//for each field
			for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
				IField currentField = fields.get(fieldIndex);
				if(pyFields.contains(fieldIndex)) {
					Object currentKey = fields.get(yearIndex).getValue();
					Record finalRecord = finalResultMap.get(currentKey);
					currentField.setValue(finalRecord.getFieldAt(fieldIndex).getValue());
				}
			}
		}
	}

	private void extractGroupedFieldsIndexes(Map<String, LinkedList<Integer>> groupedFieldsIndexMap, int fieldIndex,
			String fieldAlias, String groupingType) {
		if(fieldAlias.contains(groupingType)) {
			if(groupedFieldsIndexMap.get(groupingType) == null) {
				groupedFieldsIndexMap.put(groupingType, new LinkedList<Integer>());
			}
			LinkedList<Integer> l = groupedFieldsIndexMap.get(groupingType);
			l.add(fieldIndex);
		}
	}

	private QueryGraph getQueryGraph(Query query) {
		List<Relationship> toReturn = new ArrayList<Relationship>();
		IModelStructure modelStructure = getDataSource().getModelStructure();
		logger.debug("IModelStructure retrieved");
		RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(getDataSource().getConfiguration().getModelName(), false);
		logger.debug("RootEntitiesGraph retrieved");
		Graph<IModelEntity, Relationship> graph = rootEntitiesGraph.getRootEntitiesGraph();
		logger.debug("UndirectedGraph retrieved");
		Set<Relationship> relationships = rootEntitiesGraph.getRelationships();
		logger.debug("Set<Relationship> retrieved");
		String serialized = this.getAttributeAsString(AMBIGUOUS_FIELDS_PATHS);
		LogMF.debug(logger, AMBIGUOUS_FIELDS_PATHS + "is {0}", serialized);
		List<ModelFieldPaths> list;
		try {
			list = deserializeList(serialized, relationships, modelStructure, query);
		} catch (SerializationException e) {
			throw new SpagoBIEngineRuntimeException("Error while deserializing list of relationships", e);
		}
		logger.debug("Paths deserialized");
		QueryGraph queryGraph = null;
		if (list != null && !list.isEmpty()) {
			Iterator<ModelFieldPaths> it = list.iterator();
			while (it.hasNext()) {
				ModelFieldPaths modelFieldPaths = it.next();
				Set<PathChoice> set = modelFieldPaths.getChoices();
				Iterator<PathChoice> pathChoiceIterator = set.iterator();
				while (pathChoiceIterator.hasNext()) {
					PathChoice choice = pathChoiceIterator.next();
					toReturn.addAll(choice.getRelations());
				}
			}
			QueryGraphBuilder builder = new QueryGraphBuilder();
			queryGraph = builder.buildGraphFromEdges(toReturn);
		} else {
			Set<IModelEntity> entities = query.getQueryEntities(getDataSource());
			queryGraph = GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl()).getCoverGraph(graph, entities);
		}
		logger.debug("QueryGraph created");
		return queryGraph;
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

	protected IStatement getStatement(Query query) {
		IStatement statement = getDataSource().createStatement(query);
		return statement;
	}

	public JSONObject serializeDataStore(IDataStore dataStore) {
		JSONDataWriter dataSetWriter = new JSONDataWriter();
		JSONObject gridDataFeed = (JSONObject) dataSetWriter.write(dataStore);
		return gridDataFeed;
	}

	/**
	 * Get the query id from the request
	 *
	 * @return
	 */
	@Override
	public Query getQuery() {
		String queryId = getAttributeAsString(QUERY_ID);
		logger.debug("Parameter [" + QUERY_ID + "] is equals to [" + queryId + "]");
		Query query = getEngineInstance().getQueryCatalogue().getQuery(queryId);
		return query;
	}

	public static void updatePromptableFiltersValue(Query query, AbstractQbeEngineAction action) throws JSONException {
		updatePromptableFiltersValue(query, action, false);
	}

	public static void updatePromptableFiltersValue(Query query, AbstractQbeEngineAction action, boolean useDefault) throws JSONException {
		logger.debug("IN");
		List whereFields = query.getWhereFields();
		Iterator whereFieldsIt = whereFields.iterator();
		String[] question = { "?" };

		JSONObject requestPromptableFilters = action.getAttributeAsJSONObject("promptableFilters");

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
					// promptValuesList = action.getAttributeAsList(havingField.getEscapedName());
					JSONArray promptValuesList = requestPromptableFilters.optJSONArray(havingField.getName());
					if (promptValuesList != null) {
						String[] promptValues = toStringArray(promptValuesList);
						logger.debug("Read prompt value " + promptValues + " for promptable filter " + havingField.getName() + ".");
						havingField.getRightOperand().lastValues = promptValues; // TODO how to manage multi-values prompts?
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

	public IDataStore executeQuery(Integer start, Integer limit) {
		IDataStore dataStore = null;
		IDataSet dataSet = this.getEngineInstance().getActiveQueryAsDataSet();
		AbstractQbeDataSet qbeDataSet = (AbstractQbeDataSet) dataSet;
		IStatement statement = qbeDataSet.getStatement();
		QueryGraph graph = statement.getQuery().getQueryGraph();
		boolean valid = GraphManager.getGraphValidatorInstance(QbeEngineConfig.getInstance().getGraphValidatorImpl()).isValid(graph,
				statement.getQuery().getQueryEntities(getDataSource()));
		logger.debug("QueryGraph valid = " + valid);
		if (!valid) {
			throw new SpagoBIEngineServiceException(getActionName(), "error.mesage.description.relationship.not.enough");
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

			message = "An error occurred in " + getActionName() + " service while executing query: [" + statement.getQueryString() + "]";
			exception = new SpagoBIEngineServiceException(getActionName(), message, e);
			exception.addHint("Check if the query is properly formed: [" + statement.getQueryString() + "]");
			exception.addHint("Check connection configuration");
			exception.addHint("Check the qbe jar file");

			throw exception;
		}
		logger.debug("Query executed succesfully");
		return dataStore;
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
