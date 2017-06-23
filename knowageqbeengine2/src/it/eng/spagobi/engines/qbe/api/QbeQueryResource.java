package it.eng.spagobi.engines.qbe.api;

import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.TimeAggregationHandler;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.graph.GraphManager;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.hibernate.HQLDataSet;
import it.eng.qbe.statement.jpa.JPQLDataSet;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
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

	@POST
	@Path("/executeQuerys")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response executes() {
		return Response.ok().build();
	}

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
	@Path("/executeQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response execute(@QueryParam("start") String startS, @QueryParam("limit") String limitS, @QueryParam("id") String id,
			@QueryParam("promptableFilters") String promptableFilters, @javax.ws.rs.core.Context HttpServletRequest req) {
		Integer limit = null;
		Integer start = null;
		Integer maxSize = null;
		Query query = null;
		IDataStore dataStore = null;
		try {
			JSONObject queryJson = RestUtilities.readBodyAsJSONObject(req);

			query = deserializeQuery(queryJson);
		} catch (Exception e) {
			String errorString = "Cannot get query from request";
			logger.error(errorString, e);
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", e);

		}
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

			dataStore = executeQuery(start, limit);
			if (thereAreInlineTemporalFilters) {
				dataStore = new TimeAggregationHandler(null).handleTimeAggregations(query, dataStore);
			}
			resultNumber = (Integer) dataStore.getMetaData().getProperty("resultNumber");
			boolean overflow = maxSize != null && resultNumber >= maxSize;
			if (overflow) {
				logger.warn("Query results number [" + resultNumber + "] exceeds max result limit that is [" + maxSize + "]");
				// auditlogger.info("[" + userProfile.getUserId() +
				// "]:: max result limit [" + maxSize + "] exceeded with SQL: "
				// + sqlQuery);
			}
			gridDataFeed = serializeDataStore(dataStore);
			return Response.ok(gridDataFeed).build();
		} catch (Throwable t) {
			errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
			errorHitsMonitor.stop();
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
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
		Query query = null; // getEngineInstance().getQueryCatalogue().getQuery(id);
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

	public IDataStore executeQuery(Integer start, Integer limit) {
		IDataStore dataStore = null;
		IDataSet dataSet = this.getEngineInstance().getActiveQueryAsDataSet();
		AbstractQbeDataSet qbeDataSet = (AbstractQbeDataSet) dataSet;
		IStatement statement = qbeDataSet.getStatement();
		QueryGraph graph = statement.getQuery().getQueryGraph();
		boolean valid = GraphManager.getGraphValidatorInstance(QbeEngineConfig.getInstance().getGraphValidatorImpl()).isValid(graph,
				statement.getQuery().getQueryEntities(getEngineInstance().getDataSource()));
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

	private String getActionName() {
		return "errror";
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
