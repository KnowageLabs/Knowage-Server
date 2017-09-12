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

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.CriteriaConstants;
import it.eng.qbe.query.ExpressionNode;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @authors Davide Zerbetto (davide.zerbetto@eng.it) Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetFilterValuesAction extends AbstractQbeEngineAction {

	public static final String SERVICE_NAME = "GET_FILTER_VALUES_ACTION";

	// request parameters
	public static String QUERY_TYPE = "QUERY_TYPE";

	public static String LOOKUP_QUERY = "LOOKUP_QUERY";

	public static String ENTITY_ID = "ENTITY_ID";
	public static String ORDER_ENTITY = "ORDER_ENTITY";
	public static String ORDER_TYPE = "ORDER_TYPE";
	public static String QUERY_ROOT_ENTITY = "QUERY_ROOT_ENTITY";
	public static String DEPENDENCES = "DEPENDENCES";

	// logger component
	private static Logger logger = Logger.getLogger(GetFilterValuesAction.class);

	@Override
	public void service(SourceBean request, SourceBean response) {

		// custom | standard
		String queryType;
		// standard query
		String entityId;
		String orderEntity;
		String orderType;
		boolean queryRootEntity;
		// custom query
		String lookupQuery;
		String dependences;

		IDataStore dataStore = null;
		IDataSet dataSet = null;
		JSONDataWriter dataSetWriter;
		Query query = null;
		IStatement statement = null;

		Integer resultNumber = null;
		JSONObject gridDataFeed = new JSONObject();

		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		logger.debug("IN");

		try {

			super.service(request, response);

			totalTimeMonitor = MonitorFactory.start("QbeEngine.getFilterValuesAction.totalTime");

			queryType = getAttributeAsString(QUERY_TYPE);
			logger.debug("Parameter [" + QUERY_TYPE + "] is equals to [" + queryType + "]");
			if (StringUtilities.isEmpty(queryType)) {
				queryType = "standard";
				logger.debug("Parameter [" + QUERY_TYPE + "] set up to default value [" + queryType + "]");
			}

			lookupQuery = getAttributeAsString(LOOKUP_QUERY);
			logger.debug("Parameter [" + LOOKUP_QUERY + "] is equals to [" + lookupQuery + "]");

			entityId = getAttributeAsString(ENTITY_ID);
			logger.debug("Parameter [" + ENTITY_ID + "] is equals to [" + entityId + "]");

			orderEntity = getAttributeAsString(ORDER_ENTITY);
			logger.debug("Parameter [" + ORDER_ENTITY + "] is equals to [" + orderEntity + "]");

			orderType = getAttributeAsString(ORDER_TYPE);
			logger.debug("Parameter [" + ORDER_TYPE + "] is equals to [" + orderType + "]");

			queryRootEntity = getAttributeAsBoolean(QUERY_ROOT_ENTITY);
			logger.debug("Parameter [" + QUERY_ROOT_ENTITY + "] is equals to [" + queryRootEntity + "]");

			dependences = getAttributeAsString(DEPENDENCES);
			logger.debug("Parameter [" + DEPENDENCES + "] is equals to [" + dependences + "]");

			// if order entity is different select entity cannot apply distinct
			// filter
			boolean setDistinctClause = StringUtilities.isEmpty(orderEntity) || orderEntity.equalsIgnoreCase(entityId) ? true : false;

			if (queryType.equalsIgnoreCase("standard")) {
				query = buildQuery(entityId, orderEntity, orderType, queryRootEntity, setDistinctClause, dependences);
			} else {
				QbeEngineInstance engineInstance = this.getEngineInstance();
				QueryCatalogue queryCatalogue = engineInstance.getQueryCatalogue();
				query = queryCatalogue.getQuery(lookupQuery);
				if (query == null) {
					throw new SpagoBIEngineServiceException(getActionName(), "Impossible to retrive custom query [" + lookupQuery + "] from catalogue");
				}

			}

			statement = getDataSource().createStatement(query);

			statement.setParameters(getEnv());

			String jpaQueryStr = statement.getQueryString();
			// String sqlQuery = statement.getSqlQueryString();
			logger.debug("Executable query (HQL/JPQL): [" + jpaQueryStr + "]");
			// logger.debug("Executable query (SQL): [" + sqlQuery + "]");

			try {
				logger.debug("Executing query ...");
				dataSet = QbeDatasetFactory.createDataSet(statement);
				dataSet.setAbortOnOverflow(true);

				Map userAttributes = new HashMap();
				UserProfile profile = (UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
				Iterator it = profile.getUserAttributeNames().iterator();
				while (it.hasNext()) {
					String attributeName = (String) it.next();
					Object attributeValue = profile.getUserAttribute(attributeName);
					userAttributes.put(attributeName, attributeValue);
				}
				dataSet.addBinding("attributes", userAttributes);
				dataSet.addBinding("parameters", this.getEnv());
				dataSet.loadData();
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
			Assert.assertNotNull(resultNumber, "property [resultNumber] of the dataStore returned by loadData method of the class ["
					+ dataSet.getClass().getName() + "] cannot be null");
			logger.debug("Total records: " + resultNumber);

			dataSetWriter = new JSONDataWriter();
			gridDataFeed = (JSONObject) dataSetWriter.write(dataStore);

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

	private Query buildQuery(String entityId, String orderEntity, String orderType, boolean queryRootEntity, boolean setDistinctClause, String dependences)
			throws JSONException {

		String entityPattern = null;
		String orderEntityPattern = null;
		String[] lstDependences = null;

		logger.debug("IN");

		Assert.assertNotNull(entityId, "Parameter [" + ENTITY_ID + "] cannot be null");

		// default values for request parameters
		if (StringUtilities.isEmpty(orderType)) {
			orderType = "NONE";
		}

		if (queryRootEntity) {
			logger.debug("Must use query root entity. Looking for select and order fields...");
			IDataSource model = getDataSource();
			IModelStructure structure = model.getModelStructure();
			IModelField selectField = structure.getField(entityId);
			IModelField orderField = null;
			if (orderEntity != null && !orderEntity.equals("")) {
				orderField = structure.getField(orderEntity);
			}
			IModelEntity parentEntity = selectField.getParent();
			logger.debug("Parent entity is " + parentEntity.getUniqueName());
			IModelEntity rootEntity = structure.getRootEntity(parentEntity);
			logger.debug("Relevant root entity is " + rootEntity.getUniqueName());
			List fields = rootEntity.getAllFields();
			Iterator it = fields.iterator();
			while (it.hasNext()) {
				IModelField aField = (IModelField) it.next();
				if (aField.getName().equals(selectField.getName())) {
					entityId = aField.getUniqueName();
					entityPattern = aField.getPropertyAsString("format");
					break;
				}
			}
			logger.debug("Select field in root entity is " + entityId);
			if (orderField != null) {
				it = fields.iterator();
				while (it.hasNext()) {
					IModelField aField = (IModelField) it.next();
					if (aField.getName().equals(orderField.getName())) {
						orderEntity = aField.getUniqueName();
						orderEntityPattern = aField.getPropertyAsString("format");
						break;
					}
				}
				logger.debug("Order field in root entity is " + orderEntity);
			}
		}
		Query query = new Query();
		query.addSelectFiled(entityId, "NONE", "Valori", true, true, false, (orderEntity != null && !orderEntity.trim().equals("")) ? null : orderType,
				entityPattern);
		query.setDistinctClauseEnabled(setDistinctClause);
		if (orderEntity != null && !orderEntity.equals("")) {
			query.addSelectFiled(orderEntity, "NONE", "Ordinamento", false, false, false, orderType, orderEntityPattern);
		}

		if (null != dependences && !"".equals(dependences)) {
			lstDependences = dependences.split(",");
			for (int i = 0; i < lstDependences.length; i++) {
				String nameFiledWhere = lstDependences[i].substring(0, lstDependences[i].indexOf("="));
				String valueFieldWhere = lstDependences[i].substring(lstDependences[i].indexOf("=") + 1);
				String[] fields = new String[] { nameFiledWhere };
				String[] values = new String[] { valueFieldWhere };
				WhereField.Operand left = new WhereField.Operand(fields, "name", AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD, null, null);
				WhereField.Operand right = new WhereField.Operand(values, "value", AbstractStatement.OPERAND_TYPE_STATIC, null, null);
				query.addWhereField(nameFiledWhere, valueFieldWhere, false, left, CriteriaConstants.EQUALS_TO, right, "AND");
				ExpressionNode newFilterNode = new ExpressionNode("NODE_CONST", "$F{" + nameFiledWhere + "}");
				query.setWhereClauseStructure(newFilterNode);
				// query.addSelectFiled(nameFiledWhere, "NONE", "dependes" + i,
				// true, true, false, null, null);
			}
		}
		logger.debug("OUT");
		return query;
	}

}
