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
package it.eng.spagobi.engines.qbe.services.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONObjectDeserializator;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.ExpressionNode;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.qbe.statement.graph.GraphManager;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.tools.dataset.bo.DataSetVariable;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.service.JSONSuccess;

public class GetValuesForQbeFilterLookup extends AbstractQbeEngineAction {

	public static final String SERVICE_NAME = "GET_VALUES_FOR_QBE_FILTER_LOOKUP_ACTION";

	// request parameters
	public static String ENTITY_ID = "ENTITY_ID";
	public static String INLINE_CALCULATED_FIELD_DESCRIPTOR = "fieldDescriptor";
	public static String FILTERS = "FILTERS";

	public static String MODE = "MODE";
	public static String MODE_SIMPLE = "simple";
	public static String MODE_COMPLETE = "complete";
	public static String START = "start";
	public static String LIMIT = "limit";

	public static final String DRIVERS = "DRIVERS";

	// logger component
	private static Logger logger = Logger.getLogger(GetValuesForQbeFilterLookup.class);

	@Override
	public void service(SourceBean request, SourceBean response) {
		String entityId = null;

		Integer limit = null;
		Integer start = null;
		Integer maxSize = null;
		IDataStore dataStore = null;
		IDataSet dataSet = null;
		JSONDataWriter serializer;
		JSONObject filtersJSON = null;
		JSONObject inlineCalculatedDescriptorJSON;
		JSONObject simpleCalculatedDescriptorJSON;
		Query query = null;
		IStatement statement = null;

		Integer resultNumber = null;
		JSONObject gridDataFeed = new JSONObject();

		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		logger.debug("IN");

		try {

			super.service(request, response);

			totalTimeMonitor = MonitorFactory.start("QbeEngine.GetValuesForQbeFilterLookup.totalTime");

			simpleCalculatedDescriptorJSON = null;
			if (this.requestContainsAttribute(ENTITY_ID)) {
				entityId = getAttributeAsString(ENTITY_ID);
				simpleCalculatedDescriptorJSON = new JSONObject();
				simpleCalculatedDescriptorJSON.put("entity", entityId);
			}

			inlineCalculatedDescriptorJSON = null;
			if (this.requestContainsAttribute(INLINE_CALCULATED_FIELD_DESCRIPTOR)) {
				try {
					inlineCalculatedDescriptorJSON = getAttributeAsJSONObject(INLINE_CALCULATED_FIELD_DESCRIPTOR);
				} catch (Throwable t) {
					throw new RuntimeException("Value [" + getAttributeAsString(INLINE_CALCULATED_FIELD_DESCRIPTOR) + "] of request parameter ["
							+ INLINE_CALCULATED_FIELD_DESCRIPTOR + "] is not a well formed JSON string", t);
				}
			}

			Assert.assertTrue(simpleCalculatedDescriptorJSON != null || inlineCalculatedDescriptorJSON != null,
					"One between request parameters [" + ENTITY_ID + "] and [" + INLINE_CALCULATED_FIELD_DESCRIPTOR + "] must be not null");

			if (this.requestContainsAttribute(FILTERS)) {
				try {
					filtersJSON = getAttributeAsJSONObject(FILTERS);
				} catch (Throwable t) {
					throw new RuntimeException(
							"Value [" + getAttributeAsString(FILTERS) + "] of request parameter [" + FILTERS + "] is not a well formed JSON string", t);
				}
			}

			if (inlineCalculatedDescriptorJSON != null) {
				query = buildQuery(inlineCalculatedDescriptorJSON, ISelectField.IN_LINE_CALCULATED_FIELD, filtersJSON);
			} else {
				// note: if the field is not of type IN_LINE_CALCULATED_FIELD it muts be og type SIMPLE_FIELD because it
				// is impossible to get domain values of a CALCULATED_FIELD
				query = buildQuery(simpleCalculatedDescriptorJSON, ISelectField.SIMPLE_FIELD, filtersJSON);
			}

			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);

			// we create a new query adding filters defined by profile attributes
			IModelAccessModality accessModality = this.getEngineInstance().getDataSource().getModelAccessModality();
			Query filteredQuery = accessModality.getFilteredStatement(query, this.getDataSource(), userProfile.getUserAttributes());

			// calculate the default cover graph
			logger.debug("Calculating the default graph");
			IModelStructure modelStructure = getDataSource().getModelStructure();
			RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(getDataSource().getConfiguration().getModelName(), false);
			Graph<IModelEntity, Relationship> graph = rootEntitiesGraph.getRootEntitiesGraph();
			logger.debug("UndirectedGraph retrieved");

			Set<IModelEntity> entities = filteredQuery.getQueryEntities(getDataSource());
			if (entities.size() > 0) {
				QueryGraph queryGraph = GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl()).getCoverGraph(graph,
						entities);
				filteredQuery.setQueryGraph(queryGraph);
			}

			statement = getDataSource().createStatement(query);

			statement.setParameters(getEnv());

			String jpaQueryStr = statement.getQueryString();
			// String sqlQuery = statement.getSqlQueryString();
			logger.debug("Executable query (HQL/JPQL): [" + jpaQueryStr + "]");
			// logger.debug("Executable query (SQL): [" + sqlQuery + "]");

			start = getAttributeAsInteger(START);
			limit = getAttributeAsInteger(LIMIT);

			logger.debug("Parameter [" + ENTITY_ID + "] is equals to [" + entityId + "]");
			logger.debug("Parameter [" + START + "] is equals to [" + start + "]");
			logger.debug("Parameter [" + LIMIT + "] is equals to [" + limit + "]");

			// Assert.assertNotNull(entityId, "Parameter [" + ENTITY_ID + "] cannot be null" );

			try {
				logger.debug("Executing query ...");
				dataSet = getActiveQueryAsDataSet(filteredQuery);

				Map<String, String> envs = getEnv();
				String stringDrivers = envs.get(DRIVERS);
				Map<String, Object> drivers = null;
				try {
					drivers = JSONObjectDeserializator.getHashMapFromString(stringDrivers);
				} catch (Exception e) {
					logger.debug("Drivers cannot be transformed from string to map");
					throw new SpagoBIRuntimeException("Drivers cannot be transformed from string to map", e);
				}
				dataSet.setDrivers(drivers);

				dataSet.loadData((start == null) ? -1 : start.intValue(), (limit == null) ? -1 : limit.intValue(), (maxSize == null) ? -1 : maxSize.intValue());

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

			resultNumber = (Integer) dataStore.getMetaData().getProperty("resultNumber");
			Assert.assertNotNull(resultNumber,
					"property [resultNumber] of the dataStore returned by loadData method of the class [" + dataSet.getClass().getName() + "] cannot be null");
			logger.debug("Total records: " + resultNumber);

			// serializer = new DataStoreJSONSerializer();
			// gridDataFeed = (JSONObject)serializer.serialize(dataStore);

			// serializer2 = new LookupStoreJSONSerializer();
			// gridDataFeed = (JSONObject)serializer2.serialize(dataStore);

			Map<String, Object> props = new HashMap<String, Object>();
			props.put(JSONDataWriter.PROPERTY_PUT_IDS, Boolean.FALSE);
			serializer = new JSONDataWriter(props);
			gridDataFeed = (JSONObject) serializer.write(dataStore);

			// the first column contains the actual domain values, we must put this information into the response
			JSONObject metadataJSON = gridDataFeed.getJSONObject("metaData");
			JSONArray fieldsMetaDataJSON = metadataJSON.getJSONArray("fields");
			JSONObject firstColumn = fieldsMetaDataJSON.getJSONObject(1); // remember that JSONDataWriter puts a recNo column as first column
			// those information are useful to understand the column that contain the actual value
			String name = firstColumn.getString("name");
			metadataJSON.put("valueField", name);
			metadataJSON.put("displayField", name);
			metadataJSON.put("descriptionField", name);

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

	private Query buildQuery(JSONObject fieldDescriptor, String type, JSONObject filtersJSON) throws JSONException {

		Query query = new Query();
		query.setId(UUID.randomUUID().toString()); // this is required for serialization/deserialization and cloning
		String value = null;

		if (type.equals(ISelectField.IN_LINE_CALCULATED_FIELD)) {

			String entity = null;
			if (fieldDescriptor.has("entity")) {
				entity = fieldDescriptor.getString("entity");
			}

			String slots = null;
			if (fieldDescriptor.has("slots")) {
				slots = fieldDescriptor.getString("slots");
			}

			String nature = null;
			if (fieldDescriptor.has("nature")) {
				nature = fieldDescriptor.getString("nature");
			}

			String cftype = DataSetVariable.STRING;
			if (fieldDescriptor.has("type")) {
				cftype = fieldDescriptor.getString("type");
			}

			boolean editable = true;
			if (fieldDescriptor.has("editable") && fieldDescriptor.get("editable").toString().equalsIgnoreCase("false")) {
				editable = false;
			}

			query.addInLineCalculatedFiled("Valori", fieldDescriptor.getString("expression"), slots, cftype, nature, true, true, false, "asc", "NONE", entity,
					editable);
			value = fieldDescriptor.getString("expression");
		} else {
			query.addSelectFiled(fieldDescriptor.getString("entity"), "NONE", "Valori", true, true, false, "asc", null);
			value = fieldDescriptor.getString("entity");
		}
		query.setDistinctClauseEnabled(true);
		if (filtersJSON != null) {

			ExpressionNode whereClauseStructure = new ExpressionNode("NODE_CONST", "$F{Filter1}");
			query.setWhereClauseStructure(whereClauseStructure);
			String valuefilter = (String) filtersJSON.get(SpagoBIConstants.VALUE_FILTER);
			String typeFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_FILTER);
			String typeValueFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_VALUE_FILTER);

			WhereField.Operand leftOperand = new WhereField.Operand(new String[] { value }, "", AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD, null, null, "");
			valuefilter = typeValueFilter.equalsIgnoreCase("NUMBER") ? valuefilter : "" + valuefilter + "";
			WhereField.Operand rightOperand = new WhereField.Operand(new String[] { valuefilter }, "", AbstractStatement.OPERAND_TYPE_STATIC, null, null, "");
			query.addWhereField("Filter1", "", false, leftOperand, typeFilter, rightOperand, "AND", null);

		}
		logger.debug("OUT");
		return query;
	}

}
