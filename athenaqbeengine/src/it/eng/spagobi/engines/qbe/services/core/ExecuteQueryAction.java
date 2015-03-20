/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;
import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.Query;
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
import it.eng.qbe.statement.graph.serializer.ModelFieldPathsJSONDeserializer;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;
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


	public void service(SourceBean request, SourceBean response)  {				
		//		(Locale)getEngineInstance().getEnv().get(EngineConstants.ENV_LOCALE);		

		//String queryId = null;
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

			Object startO = getAttribute( START );
			if( startO != null && !startO.toString().equals("")){
				start = getAttributeAsInteger( START );	
			}
			logger.debug("Parameter [" + START + "] is equals to [" + start + "]");

			Object limitO = getAttribute( LIMIT );
			if( limitO != null && !limitO.toString().equals("")){
				limit = getAttributeAsInteger( LIMIT );	
			}

			logger.debug("Parameter [" + LIMIT + "] is equals to [" + limit + "]");

			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");

			// retrieving query specified by id on request
			query = getQuery();
			Assert.assertNotNull(query, "Query object with id [" + getAttributeAsString( QUERY_ID ) + "] does not exist in the catalogue");
			if (getEngineInstance().getActiveQuery() == null 
					|| !getEngineInstance().getActiveQuery().getId().equals(query.getId())) {
				logger.debug("Query with id [" + query.getId() + "] is not the current active query. A new statment will be generated");
				getEngineInstance().setActiveQuery(query);
			}

			// promptable filters values may come with request (read-only user modality)
			updatePromptableFiltersValue(query, this);

			dataStore = executeQuery(start, limit);

			resultNumber = (Integer)dataStore.getMetaData().getProperty("resultNumber");

			logger.debug("Total records: " + resultNumber);			

			boolean overflow = maxSize != null && resultNumber >= maxSize;
			if (overflow) {
				logger.warn("Query results number [" + resultNumber + "] exceeds max result limit that is [" + maxSize + "]");
				//				auditlogger.info("[" + userProfile.getUserId() + "]:: max result limit [" + maxSize + "] exceeded with SQL: " + sqlQuery);
			}

			gridDataFeed = serializeDataStore(dataStore);

			try {
				writeBackToClient( new JSONSuccess(gridDataFeed) );
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		} catch(Throwable t) {
			errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
			errorHitsMonitor.stop();
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			if(totalTimeMonitor != null) totalTimeMonitor.stop();
			logger.debug("OUT");
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
			Set<IModelEntity> entities = query.getQueryEntities( getDataSource() );
			queryGraph = GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl()).getCoverGraph(graph, entities);
		}
		logger.debug("QueryGraph created");
		return queryGraph;
	}


	public static List<ModelFieldPaths> deserializeList(String serialized, Collection<Relationship> relationShips,  IModelStructure modelStructure, Query query) throws SerializationException{
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1,0,0,null));
		simpleModule.addDeserializer(ModelFieldPaths.class, new ModelFieldPathsJSONDeserializer(relationShips, modelStructure, query));
		mapper.registerModule(simpleModule);
		TypeReference<List<ModelFieldPaths>> type = new TypeReference<List<ModelFieldPaths>>() {};
		try {
			return mapper.readValue(serialized, type);
		} catch (Exception e) {
			throw new SerializationException("Error deserializing the list of ModelFieldPaths", e);
		}
	}

	protected IStatement getStatement(Query query){
		IStatement statement =  getDataSource().createStatement( query );
		return statement;
	}

	public JSONObject serializeDataStore(IDataStore dataStore) {
		JSONDataWriter dataSetWriter = new JSONDataWriter();
		JSONObject gridDataFeed = (JSONObject)dataSetWriter.write(dataStore);
		return gridDataFeed;
	}

	/**
	 * Get the query id from the request
	 * @return
	 */
	public Query getQuery() {
		String queryId = getAttributeAsString( QUERY_ID );
		logger.debug("Parameter [" + QUERY_ID + "] is equals to [" + queryId + "]");
		Query query = getEngineInstance().getQueryCatalogue().getQuery(queryId);
		return query;
	}
	
	public static void updatePromptableFiltersValue(Query query, AbstractQbeEngineAction action) throws JSONException{
		updatePromptableFiltersValue(query, action, false);
	}

	public static void updatePromptableFiltersValue(Query query, AbstractQbeEngineAction action, boolean useDefault) throws JSONException{
		logger.debug("IN");
		List whereFields = query.getWhereFields();
		Iterator whereFieldsIt = whereFields.iterator();
		String[] question = {"?"}; 


		JSONObject requestPromptableFilters = action.getAttributeAsJSONObject("promptableFilters");


		while (whereFieldsIt.hasNext()) {
			WhereField whereField = (WhereField) whereFieldsIt.next();
			if (whereField.isPromptable()) {
				// getting filter value on request
				if(!useDefault || requestPromptableFilters!=null){
					JSONArray promptValuesList =  requestPromptableFilters.optJSONArray(whereField.getName());
					if(promptValuesList!=null){
						String[] promptValues = toStringArray(promptValuesList);
						logger.debug("Read prompts " + promptValues + " for promptable filter " + whereField.getName() + ".");
						whereField.getRightOperand().lastValues = promptValues;
					}
				}else{
					whereField.getRightOperand().lastValues =question;
				}
			}
		}
		List havingFields = query.getHavingFields();
		Iterator havingFieldsIt = havingFields.iterator();
		while (havingFieldsIt.hasNext()) {
			HavingField havingField = (HavingField) havingFieldsIt.next();
			if (havingField.isPromptable()) {
				if(!useDefault || requestPromptableFilters!=null){
					// getting filter value on request
					// promptValuesList = action.getAttributeAsList(havingField.getEscapedName());
					JSONArray promptValuesList =  requestPromptableFilters.optJSONArray(havingField.getName());
					if(promptValuesList!=null){
						String[] promptValues = toStringArray(promptValuesList);
						logger.debug("Read prompt value " + promptValues + " for promptable filter " + havingField.getName() + ".");
						havingField.getRightOperand().lastValues = promptValues; // TODO how to manage multi-values prompts?
					}
				}
			}else{
				havingField.getRightOperand().lastValues =question;
			}
		}
		logger.debug("OUT");
	}

	private static String[] toStringArray(JSONArray o ) throws JSONException{
		String[] promptValues = new String[o.length()];
		for(int i=0; i<o.length(); i++){
			promptValues[i] = o.getString(i); 
		}
		return promptValues;
	}

	public IDataStore executeQuery(Integer start, Integer limit){
		IDataStore dataStore = null;
		IDataSet dataSet = this.getEngineInstance().getActiveQueryAsDataSet();
		AbstractQbeDataSet qbeDataSet = (AbstractQbeDataSet) dataSet;
		IStatement statement = qbeDataSet.getStatement();
		QueryGraph graph = statement.getQuery().getQueryGraph();
		boolean valid = GraphManager.getGraphValidatorInstance(QbeEngineConfig.getInstance().getGraphValidatorImpl()).isValid(graph, statement.getQuery().getQueryEntities(getDataSource()));
		logger.debug("QueryGraph valid = " + valid);
		if (!valid) {
			throw new SpagoBIEngineServiceException(getActionName(), "error.mesage.description.relationship.not.enough");
		}
		try {
			logger.debug("Executing query ...");
			Integer maxSize = QbeEngineConfig.getInstance().getResultLimit();			
			logger.debug("Configuration setting  [" + "QBE.QBE-SQL-RESULT-LIMIT.value" + "] is equals to [" + (maxSize != null? maxSize: "none") + "]");
			String jpaQueryStr = statement.getQueryString();
			logger.debug("Executable query (HQL/JPQL): [" +  jpaQueryStr+ "]");
			UserProfile userProfile = (UserProfile)getEnv().get(EngineConstants.ENV_USER_PROFILE);
			auditlogger.info("[" + userProfile.getUserId() + "]:: HQL/JPQL: " + jpaQueryStr);
			auditlogger.info("[" + userProfile.getUserId() + "]:: SQL: " + statement.getSqlQueryString());


			dataSet.loadData(start, limit, (maxSize == null? -1: maxSize.intValue()));
			dataStore = dataSet.getDataStore();
			Assert.assertNotNull(dataStore, "The dataStore returned by loadData method of the class [" + dataSet.getClass().getName()+ "] cannot be null");
		} catch (Exception e) {
			logger.debug("Query execution aborted because of an internal exceptian");
			SpagoBIEngineServiceException exception;
			String message;

			message = "An error occurred in " + getActionName() + " service while executing query: [" +  statement.getQueryString() + "]";				
			exception = new SpagoBIEngineServiceException(getActionName(), message, e);
			exception.addHint("Check if the query is properly formed: [" + statement.getQueryString() + "]");
			exception.addHint("Check connection configuration");
			exception.addHint("Check the qbe jar file");

			throw exception;
		}
		logger.debug("Query executed succesfully");
		return dataStore;
	}

}
