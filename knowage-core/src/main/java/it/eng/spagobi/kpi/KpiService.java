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
package it.eng.spagobi.kpi;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobExecutionException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Cardinality;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.KpiExecution;
import it.eng.spagobi.kpi.bo.KpiScheduler;
import it.eng.spagobi.kpi.bo.KpiValue;
import it.eng.spagobi.kpi.bo.KpiValueExecLog;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.SchedulerFilter;
import it.eng.spagobi.kpi.bo.Scorecard;
import it.eng.spagobi.kpi.bo.Target;
import it.eng.spagobi.kpi.bo.Threshold;
import it.eng.spagobi.kpi.dao.IKpiDAO;
import it.eng.spagobi.kpi.dao.KpiDAOImpl.STATUS;
import it.eng.spagobi.kpi.job.ProcessKpiJob;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.bo.ConfigurableDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDatasetFactory;
import it.eng.spagobi.tools.dataset.bo.MongoDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.JSError;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @authors Salvatore Lupo (Salvatore.Lupo@eng.it)
 *
 */
@Path("/1.0/kpi")
@ManageAuthorization
public class KpiService {
	private static final String NEW_KPI_KPI_PLACEHOLDER_NOT_VALID_JSON = "newKpi.kpi.placeholder.notValidJson";
	private static final String NEW_KPI_RULE_NAME_EMPTY = "newKpi.ruleNameEmpty";
	private static final String NEW_KPI_RULE_NAME_NOT_AVAILABLE = "newKpi.ruleNameNotAvailable";
	private static final String NEW_KPI_THRESHOLD_MANDATORY = "newKpi.threshold.mandatory";
	private static final String NEW_KPI_THRESHOLD_VALUES_MANDATORY = "newKpi.threshold.values.mandatory";
	private static final String NEW_KPI_THRESHOLD_TYPE_MANDATORY = "newKpi.threshold.type.mandatory";
	private static final String NEW_KPI_THRESHOLD_NAME_MANDATORY = "newKpi.threshold.name.mandatory";
	private static final String NEW_KPI_DEFINITION_INVALIDCHARACTERS = "newKpi.definition.invalidcharacters";
	private static final String NEW_KPI_DEFINITION_SYNTAXERROR = "newKpi.definition.syntaxerror";
	private static final String NEW_KPI_DEFINITION_MANDATORY = "newKpi.definition.mandatory";
	private static final String NEW_KPI_NAME_MANDATORY = "newKpi.name.mandatory";
	private static final String NEW_KPI_CARDINALITY_ERROR = "newKpi.cardinality.error";
	private static final String NEW_KPI_KPI_NAME_NOT_AVAILABLE = "newKpi.kpiNameNotAvailable";
	private static final String KPI_SCHEDULER_GROUP = "KPI_SCHEDULER_GROUP";

	private static Logger logger = Logger.getLogger(KpiService.class);

	private static final String MEASURE = "MEASURE";
	private static final String MEASURE_NAME = "measureName";
	private static final String MEASURE_ATTRIBUTES = "attributes";

	@POST
	@Path("/buildCardinalityMatrix")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response buildCardinalityMatrix(@Context HttpServletRequest req) throws EMFUserError {
		logger.debug("buildCardinalityMatrix IN");
		Response out;
		try {
			JSONObject arrayOfMeasures = RestUtilities.readBodyAsJSONObject(req);
			Iterator it = arrayOfMeasures.keys();
			List<String> measureList = new ArrayList<>();
			while (it.hasNext()) {
				String val = it.next().toString();
				measureList.add(arrayOfMeasures.getString(val));
			}
			// List<String> measureList = (List) JsonConverter.jsonToObject(arrayOfMeasures, List.class);
			IKpiDAO dao = getKpiDAO(req);
			List<Cardinality> lst = dao.buildCardinality(measureList);
			out = Response.ok(JsonConverter.objectToJson(lst, lst.getClass())).build();
			logger.debug("buildCardinalityMatrix OUT");
			return out;
		} catch (IOException e) {
			logger.error("buildCardinalityMatrix error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} catch (JSONException e) {
			logger.error("buildCardinalityMatrix error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	@POST
	@Path("/listPlaceholderByMeasures")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listPlaceholderByMeasures(@Context HttpServletRequest req) throws EMFUserError {
		Response out;
		try {
			logger.debug("listPlaceholderByMeasures IN");
			String arrayOfMeasures = RestUtilities.readBodyAsJSONObject(req).toString();
			List measureList = (List) JsonConverter.jsonToObject(arrayOfMeasures, List.class);
			IKpiDAO dao = getKpiDAO(req);
			List<String> lst = dao.listPlaceholderByMeasures(measureList);
			out = Response.ok(JsonConverter.objectToJson(lst, lst.getClass())).build();
			logger.debug("listPlaceholderByMeasures OUT");
			return out;
		} catch (IOException e) {
			logger.error("ListPlaceHolderByMeasures  ", e);
			logger.error(req.getPathInfo(), e);
		} catch (JSONException e) {
			logger.error("ListPlaceHolderByMeasures  ", e);
			logger.error(req.getPathInfo(), e);
		}
		out = Response.ok().build();
		logger.debug("listPlaceholderByMeasures OUT");
		return out;
	}

	@POST
	@Path("/listPlaceholderByKpi")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listPlaceholderByKpi(@Context HttpServletRequest req) throws EMFUserError {
		logger.debug("listPlaceholderByKpi IN");
		Response out;
		try {
			JSONArray arrayOfKpi = RestUtilities.readBodyAsJSONArray(req);
			List<Kpi> kpiLst = new ArrayList<>();
			for (int i = 0; i < arrayOfKpi.length(); i++) {
				JSONObject kpiKey = arrayOfKpi.getJSONObject(i);
				kpiLst.add(new Kpi(kpiKey.getInt("id"), kpiKey.getInt("version")));
			}
			Map<String, String> result = new HashMap<>();

			IKpiDAO dao = getKpiDAO(req);
			if (kpiLst != null && !kpiLst.isEmpty()) {
				Map<Kpi, List<String>> lst = dao.listPlaceholderByKpiList(kpiLst);

				for (Entry<Kpi, List<String>> keyValue : lst.entrySet()) {
					JSONArray jsonPlaceholdersWithValue = new JSONArray();
					Kpi kpi = keyValue.getKey();
					List<String> placeholders = keyValue.getValue();
					JSONObject placeholderValues = new JSONObject();
					if (kpi.getPlaceholder() != null && !kpi.getPlaceholder().isEmpty()) {
						placeholderValues = new JSONObject(kpi.getPlaceholder());
					}
					for (String placeholder : placeholders) {
						String value = placeholderValues.optString(placeholder);
						jsonPlaceholdersWithValue.put(new JSONObject().put(placeholder, value));
					}
					result.put(kpi.getName(), jsonPlaceholdersWithValue.toString());
				}
			}
			out = Response.ok(JsonConverter.objectToJson(result, result.getClass())).build();
			logger.debug("listPlaceholderByKpi OUT");
			return out;
		} catch (IOException | JSONException e) {
			logger.error("listPlaceholderByKpi  ");
			logger.error(req.getPathInfo(), e);
		}
		out = Response.ok().build();
		logger.debug("listPlaceholderByKpi OUT");
		return out;
	}

	@GET
	@Path("/listPlaceholder")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listPlaceholder(@Context HttpServletRequest req) throws EMFUserError {
		Response out;
		logger.debug("listPlaceholder IN");
		List<Placeholder> placeholders = getKpiDAO(req).listPlaceholder();
		out = Response.ok(JsonConverter.objectToJson(placeholders, placeholders.getClass())).build();
		logger.debug("listPlaceholder OUT");
		return out;
	}

	@GET
	@Path("/listMeasure")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listMeasure(@Context HttpServletRequest req, @QueryParam("orderProperty") String orderProperty, @QueryParam("orderType") String orderType)
			throws EMFUserError {
		Response out;
		logger.debug("listMeasure IN");
		IKpiDAO dao = getKpiDAO(req);
		// Listing only active records
		List<RuleOutput> measures = dao.listRuleOutputByType(MEASURE, STATUS.ACTIVE);
		out = Response.ok(JsonConverter.objectToJson(measures, measures.getClass())).build();
		logger.debug("listMeasure OUT");
		return out;
	}

	@GET
	@Path("/{name}/existsMeasure")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public String existsMeasure(@PathParam("name") String name, @Context HttpServletRequest req) throws EMFUserError {
		logger.debug("existsMeasure IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		String ret = dao.existsMeasureNames(name).toString();
		logger.debug("existsMeasure OUT");
		return ret;
	}

	@GET
	@Path("/{id}/{number}/logExecutionList")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response logExecutionList(@PathParam("id") Integer id, @PathParam("number") Integer number, @Context HttpServletRequest req) throws EMFUserError {
		logger.debug("logExecutionList IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		ArrayList<KpiValueExecLog> result = dao.loadKpiValueExecLog(id, number);
		out = Response.ok(JsonConverter.objectToJson(result, result.getClass())).build();
		logger.debug("logExecutionList OUT");
		return out;
	}

	@GET
	@Path("/{id}/logExecutionListOutputContent")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response logExecutionListOutputContent(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		logger.debug("logExecutionListOutputContent IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		KpiValueExecLog result = dao.loadlogExecutionListOutputContent(id);
		out = Response.ok(JsonConverter.objectToJson(result, result.getClass())).build();
		logger.debug("logExecutionListOutputContent OUT");
		return out;
	}

	@GET
	@Path("/{id}/{version}/loadRule")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadRule(@PathParam("id") Integer id, @PathParam("version") Integer version, @Context HttpServletRequest req) throws EMFUserError {
		logger.debug("loadRule IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		Rule r = dao.loadRule(id, version);
		out = Response.ok(JsonConverter.objectToJson(r, r.getClass())).build();
		logger.debug("loadRule OUT");
		return out;
	}

	@GET
	@Path("/{thresholdId}/loadThreshold")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadThreshold(@PathParam("thresholdId") Integer id, @QueryParam("kpiId") Integer kpiId, @Context HttpServletRequest req)
			throws EMFUserError {
		logger.debug("loadThreshold IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		Threshold threshold = dao.loadThreshold(id);
		if (dao.isThresholdUsedByOtherKpi(kpiId, id)) {
			threshold.setUsedByKpi(true);
		}
		out = Response.ok(JsonConverter.objectToJson(threshold, threshold.getClass())).build();
		logger.debug("loadThreshold OUT");
		return out;
	}

	@GET
	@Path("/listAlias")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listAlias(@Context HttpServletRequest req) throws EMFUserError {
		Response out;
		logger.debug("listAlias IN");
		IKpiDAO dao = getKpiDAO(req);
		List<Alias> aliases = dao.listAlias();
		out = Response.ok(JsonConverter.objectToJson(aliases, aliases.getClass())).build();
		logger.debug("listAlias OUT");
		return out;
	}

	@GET
	@Path("/listAvailableAlias")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listAvailableAlias(@QueryParam("ruleId") Integer ruleId, @QueryParam("ruleVersion") Integer ruleVersion, @Context HttpServletRequest req)
			throws EMFUserError, JSONException {
		logger.debug("IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		logger.debug("Getting available aliases");
		List<Alias> aliases = dao.listAliasNotInMeasure(ruleId, ruleVersion);
		logger.debug("Getting unavailable aliases");
		List<Alias> unavaliases = dao.listAliasInMeasure(ruleId, ruleVersion);

		JSONObject resp = new JSONObject();
		resp.put("available", new JSONArray(JsonConverter.objectToJson(aliases, aliases.getClass())));
		resp.put("notAvailable", new JSONArray(JsonConverter.objectToJson(unavaliases, unavaliases.getClass())));
		out = Response.ok(resp.toString()).build();
		logger.debug("OUT");
		return out;
	}

	/**
	 * Executes a given query over a given datasource (dataSourceId) limited by maxItem param. It uses existing backend to retrieve data and metadata, but the
	 * resulting json is lightened in order to give back something like this: {"columns": [{"name": "column_1", "label": "order_id"},...], "rows": [{"column_1":
	 * "1"},...]}
	 *
	 * @param req
	 * @return
	 * @throws EMFUserError
	 */
	@POST
	@Path("/queryPreview")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response queryPreview(@Context HttpServletRequest req) throws EMFUserError {
		logger.debug("queryPreview IN");
		Response out;
		Integer dataSourceId = null;
		String query = null;
		Integer maxItem = null;
		Set<Placeholder> placeholders = null;
		try {
			JSONObject obj = RestUtilities.readBodyAsJSONObject(req);
			Rule rule = (Rule) JsonConverter.jsonToObject(obj.getString("rule"), Rule.class);

			maxItem = obj.optInt("maxItem", 1);

			dataSourceId = rule.getDataSourceId();
			query = rule.getDefinition();
			placeholders = rule.getPlaceholders();

			JSONObject result = executeQuery(dataSourceId, query, maxItem, placeholders, getProfile(req));

			out = Response.ok(result.toString()).build();
			logger.debug("queryPreview OUT");
			return out;

		} catch (IOException | JSONException | EMFInternalError e) {
			logger.error("queryPreview  ");
			logger.error("dataSourceId[" + dataSourceId + "] query[" + query + "] maxItem[" + maxItem + "] placeholders[" + placeholders + "]");
			logger.error(req.getPathInfo(), e);
		}
		out = Response.ok().build();
		logger.debug("queryPreview OUT");
		return out;
	}

	@GET
	@Path("/executeKpiScheduler/{schedulerId}")
	public Response executeKpiScheduler(@PathParam("schedulerId") String schedulerId) throws JobExecutionException {
		logger.debug("executeKpiScheduler IN");
		Response out;
		try {
			KpiValueExecLog result = ProcessKpiJob.computeKpis(Integer.parseInt(schedulerId), new Date(), true);
			out = Response.ok(new ObjectMapper().writeValueAsString(result)).build();
			logger.debug("executeKpiScheduler OUT");
			return out;
		} catch (Exception e) {
			logger.error("executeKpiScheduler  error");
			logger.error("executeKpiScheduler error ", e);
			throw new JobExecutionException(e);
		}
	}

	@GET
	@Path("/findKpiValuesTest")
	public Response findKpiValuesTest() throws EMFUserError, JsonGenerationException, JsonMappingException, IOException {
		logger.debug("findKpiValuesTest IN");
		Response out;
		IKpiDAO kpiDao = DAOFactory.getKpiDAO();
		Map<String, String> attributesValues = new HashMap<>();
		attributesValues.put("STORE_CITY", "Los Angeles");
		attributesValues.put("STORE_TYPE", "Supermarket");
		attributesValues.put("OPENED_MONTH", "5");
		// attributesValues.put("SA2", "5");
		List<KpiValue> kpiValues = kpiDao.findKpiValues(11, 0, null, null, attributesValues);
		String result = new ObjectMapper().writeValueAsString(kpiValues);
		out = Response.ok(result).build();
		logger.debug("findKpiValuesTest OUT");
		return out;
	}

	@POST
	@Path("/preSaveRule")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response preSave(@Context HttpServletRequest req) throws EMFUserError {
		logger.debug("preSaveRule IN");
		Response out;
		try {
			String obj = RestUtilities.readBodyAsJSONObject(req).toString();
			Rule rule = (Rule) JsonConverter.jsonToObject(obj, Rule.class);
			Map<Kpi, List<String>> kpimap = null;

			// Checking if query executes
			executeQuery(rule.getDataSourceId(), rule.getDefinition(), 1, rule.getPlaceholders(), getProfile(req));

			// Checking if aliases used by rule are all usable
			IKpiDAO kpiDao = getKpiDAO(req);
			Map<String, List<String>> aliasErrorMap = kpiDao.aliasValidation(rule);
			JSError jsError = new JSError();
			if (!aliasErrorMap.isEmpty()) {
				for (Entry<String, List<String>> error : aliasErrorMap.entrySet()) {
					jsError.addErrorKey(error.getKey(), new JSONArray(error.getValue()).toString().replaceAll("[\\[\\]]", ""));
				}
			}
			if (rule.getId() != null) {
				Assert.assertNotNull(rule.getVersion(), "Impossible to continue without the rule version");
				kpimap = kpiDao.listKpisLinkedToRule(rule.getId(), rule.getVersion(), true);
				checkConflictsWithKpi(jsError, rule, kpimap, kpiDao);
			}
			if (jsError.hasErrors() || jsError.hasWarnings()) {
				out = Response.ok(jsError.toString()).build();
				logger.debug("preSaveRule OUT");
				return out;
			}
			// check if temporal attribute are correct
			String query = rule.getDefinition();

			Set<RuleOutput> ruleOutput = rule.getRuleOutputs();
			out = Response.ok().build();

			for (RuleOutput ruleOut : ruleOutput) {

				if (ruleOut.getType().getValueCd().equals("TEMPORAL_ATTRIBUTE")) {
					// 1 - Hierarchy is mandatory
					if (ruleOut.getHierarchy() == null) {
						jsError.addErrorKey("Set type of Temporal attribute", "Set type of Temporal attribute");

						out = Response.ok(jsError.toString()).build();
						logger.debug("Set type of Temporal attribute");
					} else {

						// 2 - Number of records should not exceed the size of the selected hierarchy
						boolean toManyValues = checkValuesNumberForTemporalAttributes(req, rule, query, ruleOut);
						if (toManyValues) {
							jsError.addErrorKey("Error: too many values for temporal attribute", "Error: too many values for temporal attribute");
							out = Response.ok(jsError.toString()).build();
							logger.debug("Error: too many values for temporal attribute");
							break;
						}

						// 3 - All the records should be coherent with the selected temporal level
						boolean isValid = checkValuesFormatForTemporalAttributes(req, rule, query, ruleOut);
						if (!isValid) {
							String errMsg = "Error on temporal attributes " + ruleOut.getAlias() + " contains not allowed data for type: "
									+ ruleOut.getHierarchy().getValueCd();
							jsError.addErrorKey(errMsg, errMsg);
							out = Response.ok(jsError.toString()).build();
							logger.debug(errMsg);
							break;
						}
					}
				}
			}

			logger.debug("preSaveRule OUT");
			return out;
		} catch (Exception e) {
			logger.error("preSaveRule ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	private boolean checkValuesFormatForTemporalAttributes(HttpServletRequest req, Rule rule, String query, RuleOutput ruleOut)
			throws EMFUserError, EMFInternalError, JSONException {
		boolean isValid = true;
		String distinctQuery = "SELECT DISTINCT " + ruleOut.getAlias() + " FROM ( " + query + ") a_l_i_a_s";
		JSONObject distinctResult = executeQuery(rule.getDataSourceId(), distinctQuery, 0, rule.getPlaceholders(), getProfile(req));
		// check Temporal Attribute...
		String columnName = "";
		JSONArray distinctResultColumns = distinctResult.getJSONArray("columns");
		for (int i = 0; i < distinctResultColumns.length(); i++) {
			JSONObject distinctValuesColumn = distinctResultColumns.getJSONObject(i);
			if (distinctValuesColumn.getString("label").equals(ruleOut.getAlias())) {
				columnName = distinctValuesColumn.getString("name");
				break;
			}
		}
		if ("".equals(columnName)) {
			isValid = false;
		} else {
			JSONArray distinctResultRows = distinctResult.getJSONArray("rows");
			for (int j = 0; j < distinctResultRows.length(); j++) {
				Integer testingValue = null;
				try {
					String value = distinctResultRows.getJSONObject(j).getString(columnName);
					testingValue = new BigDecimal(value).toBigInteger().intValue();
				} catch (NumberFormatException e) {
					isValid = false;
					break;
				}
				if (ruleOut.getHierarchy().getValueCd().equals("QUARTER")) {
					if (testingValue < 1 || testingValue > 4) {
						isValid = false;
						break;
					}
				} else if (ruleOut.getHierarchy().getValueCd().equals("YEAR")) {
					if (testingValue < 0 || testingValue > 3000) {
						isValid = false;
						break;
					}

				} else if (ruleOut.getHierarchy().getValueCd().equals("MONTH")) {
					if (testingValue < 1 || testingValue > 12) {
						isValid = false;
						break;
					}
				} else if (ruleOut.getHierarchy().getValueCd().equals("WEEK_OF_YEAR")) {
					if (testingValue < 1 || testingValue > 52) {
						isValid = false;
						break;
					}
				} else if (ruleOut.getHierarchy().getValueCd().equals("DAY")) {
					if (testingValue < 1 || testingValue > 31) {
						isValid = false;
						break;
					}
				}

			}
		}
		return isValid;
	}

	private boolean checkValuesNumberForTemporalAttributes(HttpServletRequest req, Rule rule, String query, RuleOutput ruleOut)
			throws JSONException, EMFUserError, EMFInternalError {
		String countQuery = "SELECT count(distinct " + ruleOut.getAlias() + ") as totRows  FROM ( " + query + ") a_l_i_a_s";
		JSONObject countResult = executeQuery(rule.getDataSourceId(), countQuery, 0, rule.getPlaceholders(), getProfile(req));
		Integer maxSize = 0;

		switch (ruleOut.getHierarchy().getValueCd()) {
		case "QUARTER":
			maxSize = 4;
			break;
		case "MONTH":
			maxSize = 12;
			break;
		case "WEEK_OF_YEAR":
			maxSize = 52;
			break;
		case "DAY":
			maxSize = 31;
			break;
		default:
			break;
		}
		Integer countResultValue = countResult.getJSONArray("rows").getJSONObject(0).getInt("column_1");
		boolean toManyValues = maxSize > 0 && countResultValue > maxSize;
		return toManyValues;
	}

	@POST
	@Path("/saveRule")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response saveRule(@Context HttpServletRequest req) throws EMFUserError {
		logger.debug("saveRule IN");
		Response out;
		try {
			IKpiDAO dao = getKpiDAO(req);
			String requestVal = RestUtilities.readBodyAsJSONObject(req).toString();
			Rule rule = (Rule) JsonConverter.jsonToObject(requestVal, Rule.class);
			Integer id = rule.getId();
			Integer version = rule.getVersion();
			Map<Kpi, List<String>> kpimap = null;
			JSError jsError = new JSError();

			if (rule.getName() == null || rule.getName().isEmpty()) {
				jsError.addErrorKey(NEW_KPI_RULE_NAME_EMPTY, rule.getName());
			}
			// Rule name must be unique
			Integer otherId = dao.getRuleIdByName(rule.getName());
			if (otherId != null && (id == null || !id.equals(otherId))) {
				jsError.addErrorKey(NEW_KPI_RULE_NAME_NOT_AVAILABLE, rule.getName());
			}
			if (id != null) {
				Assert.assertNotNull(rule.getVersion(), "Impossible to continue without the rule version");
				kpimap = dao.listKpisLinkedToRule(rule.getId(), rule.getVersion(), true);
				checkConflictsWithKpi(jsError, rule, kpimap, dao);
			}
			if (jsError.hasErrors()) {
				out = Response.ok(jsError.toString()).build();
				logger.debug("saveRule OUT");
				return out;
			}
			if (id == null) {
				// Save a new Rule
				Rule newRule = dao.insertRule(rule);
				id = newRule.getId();
				version = newRule.getVersion();
			} else {
				// Rule can only be modified logically
				Rule newRule = dao.insertNewVersionRule(rule);
				id = newRule.getId();
				version = newRule.getVersion();
				for (Kpi kpi : kpimap.keySet()) {
					Kpi fullKpi = dao.loadKpi(kpi.getId(), kpi.getVersion());
					dao.updateKpi(fullKpi);
				}
			}
			return Response.ok(new JSONObject().put("id", id).put("version", version).toString()).build();
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		} finally {
			logger.debug("saveRule OUT");
		}
	}

	@GET
	@Path("/listKpi")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT, SpagoBIConstants.CREATE_DOCUMENT })
	public Response listKpi(@Context HttpServletRequest req) throws EMFUserError {
		logger.debug("listKpi IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		List<Kpi> kpis = dao.listKpi(STATUS.ACTIVE, profile);
		out = Response.ok(JsonConverter.objectToJson(kpis, kpis.getClass())).build();
		logger.debug("listKpi OUT");
		return out;
	}

	@GET
	@Path("/listKpiWithResult")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listKpiWithResult(@Context HttpServletRequest req) throws EMFUserError {
		logger.debug("listKpiWithResult IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		List<KpiExecution> kpis = dao.listKpiWithResult();
		out = Response.ok(JsonConverter.objectToJson(kpis, kpis.getClass())).build();
		logger.debug("listKpiWithResult OUT");
		return out;
	}

	@GET
	@Path("/{id}/{version}/loadKpi")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadKpi(@PathParam("id") Integer id, @PathParam("version") Integer version, @Context HttpServletRequest req) throws EMFUserError {
		logger.debug("ID VERSION loadKpi  IN");
		Response out;
		Kpi kpi = getKpiDAO(req).loadKpi(id, version);
		out = Response.ok(JsonConverter.objectToJson(kpi, kpi.getClass())).build();
		logger.debug("ID VERSION loadKpi OUT");
		return out;
	}

	@GET
	@Path("/listThreshold")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listThreshold(@Context HttpServletRequest req) throws EMFUserError {
		logger.debug("listThreshold IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		List<Threshold> tt = dao.listThreshold();
		out = Response.ok(JsonConverter.objectToJson(tt, tt.getClass())).build();
		logger.debug("listThreshold OUT");
		return out;
	}

	@POST
	@Path("/saveKpi")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response saveKpi(@Context HttpServletRequest req) throws EMFUserError, EMFInternalError {
		logger.debug("saveKpi IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		try {
			String requestVal = RestUtilities.readBodyAsJSONObject(req).toString();
			Kpi kpi = (Kpi) JsonConverter.jsonToObject(requestVal, Kpi.class);

			JSError jsError = new JSError();

			checkMandatory(req, jsError, kpi);

			if (kpi.getThreshold() == null) {
				jsError.addErrorKey(NEW_KPI_THRESHOLD_MANDATORY);
			} else {
				checkMandatory(jsError, kpi.getThreshold());
			}

			if (!jsError.hasErrors()) {
				// kpi name must be unique
				Integer sameNameKpiId = dao.getKpiIdByName(kpi.getName());
				if (sameNameKpiId != null && (kpi.getId() == null || !sameNameKpiId.equals(kpi.getId()))) {
					jsError.addErrorKey(NEW_KPI_KPI_NAME_NOT_AVAILABLE, kpi.getName());
				}
			}

			if (kpi.getCardinality() != null && !kpi.getCardinality().isEmpty()) {
				checkCardinality(jsError, kpi.getCardinality(), kpi.getDefinition());
			}
			if (kpi.getPlaceholder() != null && !kpi.getPlaceholder().isEmpty()) {
				checkPlaceholder(req, kpi);
			}

			if (jsError.hasErrors()) {
				return Response.ok(jsError.toString()).build();
			}

			if (kpi.getId() == null) {
				dao.insertKpi(kpi);
			} else {
				dao.updateKpi(kpi);
			}
			out = Response.ok().build();
			logger.debug("saveKpi OUT");
			return out;
		} catch (IOException | JSONException | SpagoBIException e) {
			logger.error("saveKpi  ");
			logger.error(req.getPathInfo(), e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	@DELETE
	@Path("/{id}/{version}/deleteKpi")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response deleteKpi(@PathParam("id") Integer id, @PathParam("version") Integer version, @Context HttpServletRequest req) throws EMFUserError {
		logger.debug("deleteKpi IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		JSError jsError = checkKpiRel(dao, id, version);
		if (!jsError.hasErrors()) {
			dao.removeKpi(id, version);
			out = Response.ok().build();
			logger.debug("deleteKpi OUT");
			return out;
		} else {
			out = Response.ok(jsError.toString()).build();
			logger.error("deleteKpi ");
			return out;
		}
	}

	@DELETE
	@Path("/{id}/{version}/deleteRule")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response deleteRule(@PathParam("id") Integer id, @PathParam("version") Integer version, @Context HttpServletRequest req) throws EMFUserError {
		// Rule can only be removed logically
		logger.debug("deleteRule IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		Map<Kpi, List<String>> kpimap = dao.listKpisLinkedToRule(id, version, true);
		if (!kpimap.isEmpty()) {
			StringBuilder kpiNames = new StringBuilder();
			Iterator<Kpi> iKpiset = kpimap.keySet().iterator();
			while (iKpiset.hasNext()) {
				Kpi kpi = iKpiset.next();
				kpiNames.append(kpi.getName());
				if (iKpiset.hasNext()) {
					kpiNames.append(" ,");
				}
			}
			out = Response.ok(new JSError().addErrorKey("newKpi.rule.usedByKpi.delete.error", kpiNames.toString()).toString()).build();
			logger.debug("deleteRule OUT");
			return out;
		}
		// Rule can only be removed logically
		dao.removeRule(id, version, true);
		out = Response.ok().build();
		logger.debug("deleteRule OUT");
		return out;
	}

	@DELETE
	@Path("/{id}/deleteKpiScheduler")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response deleteKpiScheduler(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		logger.debug("deleteKpiScheduler IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		dao.removeKpiScheduler(id);
		out = Response.ok().build();
		logger.debug("deleteKpiScheduler OUT");
		return out;
	}

	@GET
	@Path("/listSchedulerKPI")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listSchedulerKPI(@Context HttpServletRequest req) throws EMFUserError {
		logger.debug("listSchedulerKPI IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		List<KpiScheduler> schedulerList = dao.listKpiScheduler();
		out = Response.ok(JsonConverter.objectToJson(schedulerList, schedulerList.getClass()).toString()).build();
		logger.debug("listSchedulerKPI OUT");
		return out;
	}

	@POST
	@Path("/saveSchedulerKPI")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response saveSchedulerKPI(@Context HttpServletRequest req) throws EMFUserError {
		logger.debug("saveSchedulerKPI IN");
		Response out;
		try {
			String requestVal = RestUtilities.readBodyAsJSONObject(req).toString();
			KpiScheduler scheduler = (KpiScheduler) JsonConverter.jsonToObject(requestVal, KpiScheduler.class);
			checkMandatory(scheduler);
			checkValidity(scheduler);
			IKpiDAO dao = getKpiDAO(req);
			Integer id = scheduler.getId();
			if (id == null) {
				id = dao.insertScheduler(scheduler);
			} else {
				dao.updateScheduler(scheduler);
			}
			out = Response.ok(new JSONObject().put("id", id).toString()).build();
			logger.debug("saveSchedulerKPI OUT");
			return out;
		} catch (SpagoBIException e) {
			logger.error("saveSchedulerKpi", e);
			out = Response.ok(new JSError().addErrorKey("newKpi.kpi.jobOrTriggerError").toString()).build();
			logger.debug("saveSchedulerKPI OUT");
			return out;
		} catch (Throwable e) {
			logger.error("saveSchedulerKpi");
			logger.error(req.getPathInfo(), e);
			out = Response.ok(new JSError().addErrorKey("sbi.rememberme.errorWhileSaving")).build();
			logger.debug("saveSchedulerKPI OUT");
			return out;
		}
	}

	@POST
	@Path("/editKpiValue")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT, SpagoBIConstants.MANAGE_KPI_VALUE })
	public void editKpiValue(@Context HttpServletRequest req) throws EMFUserError {
		logger.debug("editKpiValue IN");
		Response out;
		JSONArray array = new JSONArray();
		UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
			JSONObject kpiValue = requestVal.getJSONObject("valueSeries");
			String comment = kpiValue.getString("manualNote");

			String[] checkComment = comment.split("\n");

			SimpleDateFormat dateFormat = new SimpleDateFormat();
			dateFormat.applyPattern(GeneralUtilities.getServerTimeStampFormat());
			Date creationDate = new Date();
			String creationDateStr = dateFormat.format(creationDate);

			if (!checkComment[checkComment.length - 1].startsWith(profile.getUserId().toString())) {
				comment = comment.concat("\n" + profile.getUserId() + " " + creationDateStr);
			} else {
				comment.replaceAll(checkComment[checkComment.length - 1], profile.getUserId() + " " + creationDateStr);
			}
			if (requestVal.get("manualValue") == JSONObject.NULL) {
				DAOFactory.getKpiDAO().editKpiValue(kpiValue.getInt("id"), -999, comment);

			} else {
				DAOFactory.getKpiDAO().editKpiValue(kpiValue.getInt("id"), requestVal.getDouble("manualValue"), comment);

			}

		} catch (Throwable e)

		{
			logger.error("ediKpiValue ");
			logger.error(req.getPathInfo(), e);

		}
		logger.debug("editKpiValue OUT");
	}

	@GET
	@Path("/{id}/loadSchedulerKPI")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadSchedulerKPI(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		logger.debug("loadSchedulerKPI IN");
		Response out;
		IKpiDAO dao = getKpiDAO(req);
		KpiScheduler t = dao.loadKpiScheduler(id);
		out = Response.ok(JsonConverter.objectToJson(t, t.getClass())).build();
		logger.debug("loadSchedulerKPI OUT");
		return out;
	}

	/*
	 * *** Private methods ***
	 */

	private void checkConflictsWithKpi(JSError jsError, Rule rule, Map<Kpi, List<String>> kpimap, IKpiDAO kpiDao) throws EMFUserError {
		if (rule.getId() != null && rule.getVersion() != null) {

			// Checking if any removed measure is linked to a kpi (if so we cannot save this rule)
			Collection<String> measureAndKpi = new HashSet<>();
			Set<String> usedMeasureList = new HashSet<>();
			for (List<String> m : kpimap.values()) {
				usedMeasureList.addAll(m);
			}
			Set<String> newRuleOutputList = new HashSet<>();
			for (RuleOutput ro : rule.getRuleOutputs()) {
				newRuleOutputList.add(ro.getAlias());
			}
			usedMeasureList.removeAll(newRuleOutputList);
			for (String name : usedMeasureList) {
				for (Entry<Kpi, List<String>> kpi : kpimap.entrySet()) {
					if (kpi.getValue().contains(name)) {
						measureAndKpi.add("\"" + name + "\" used by \"" + kpi.getKey().getName() + "\"");
						break;
					}
				}
			}
			if (!measureAndKpi.isEmpty()) {
				jsError.addErrorKey("newKpi.rule.usedByKpi.save.error", StringUtils.join(measureAndKpi, ", "));
			}

			// Checking if any removed attribute is linked to a kpi (if so we cannot save this rule)
			if (!kpimap.isEmpty()) {
				Set<String> usedAttributes = new HashSet<>();
				Map<String, List<String>> kpis = new HashMap<>();
				for (Kpi kpi : kpimap.keySet()) {
					try {
						JSONArray measures = new JSONObject(kpi.getCardinality()).getJSONArray("measureList");
						for (int i = 0; i < measures.length(); i++) {
							Iterator<String> attributesIterator = measures.getJSONObject(i).getJSONObject("attributes").keys();
							while (attributesIterator.hasNext()) {
								String attribute = attributesIterator.next();
								usedAttributes.add(attribute);
								if (kpis.get(kpi.getName()) == null) {
									kpis.put(kpi.getName(), new ArrayList<String>());
								}
								kpis.get(kpi.getName()).add(attribute);
							}
						}
					} catch (JSONException e) {
						logger.error("Error while trying to read measureList/attributes from kpi [id=" + kpi.getId() + "|version=" + kpi.getVersion() + "]", e);
					}
				}
				if (!usedAttributes.isEmpty()) {
					Collection<String> attributesError = new HashSet<>();
					usedAttributes.removeAll(newRuleOutputList);
					for (String name : usedAttributes) {
						for (Entry<String, List<String>> kpi : kpis.entrySet()) {
							if (kpi.getValue().contains(name)) {
								attributesError.add("\"" + name + "\" used by \"" + kpi.getKey() + "\"");
								break;
							}
						}
					}
					if (!attributesError.isEmpty()) {
						jsError.addErrorKey("newKpi.rule.attributeUsedByKpi.save.error", StringUtils.join(attributesError, ", "));
					}
				}
			}

			// Checking if there are placeholders that are not set in a scheduler
			// This check will give only a warning
			if (rule.getPlaceholders() != null && !rule.getPlaceholders().isEmpty()) {
				List<String> placeholderNames = new ArrayList<>();
				for (Placeholder placeholder : rule.getPlaceholders()) {
					placeholderNames.add(placeholder.getName());
				}
				List<String> kpiNames = new ArrayList<>();
				boolean anyScheduler = false;
				for (Kpi kpi : kpimap.keySet()) {
					List<KpiScheduler> schedulerList = kpiDao.listSchedulerAndFiltersByKpi(kpi.getId(), kpi.getVersion(), true);
					for (KpiScheduler kpiScheduler : schedulerList) {
						for (SchedulerFilter filter : kpiScheduler.getFilters()) {
							placeholderNames.remove(filter.getPlaceholderName());
						}
					}
					kpiNames.add(kpi.getName());
					if (schedulerList.size() > 0) {
						anyScheduler = true;
					}
				}
				if (!placeholderNames.isEmpty() && anyScheduler) {
					jsError.addWarningKey("newKpi.rule.placeholdersMustBeSet.save.error", StringUtils.join(placeholderNames, ", "),
							StringUtils.join(kpiNames, ", "));
				}
			}
		}
	}

	private JSError checkKpiRel(IKpiDAO dao, Integer id, Integer version) {
		JSError jsError = new JSError();
		// Check if any relation exists with Scorecard
		List<Scorecard> scorecards = dao.listScorecardByKpi(id, version);
		if (scorecards != null && !scorecards.isEmpty()) {
			String scNames = "";
			for (int i = 0; i < scorecards.size(); i++) {
				if (i != 0) {
					scNames += ", ";
				}
				scNames += scorecards.get(i).getName();
			}
			jsError.addErrorKey("newKpi.kpi.kpiIsUsedByScorecards", scNames);
		}
		// Check if any relation exists with Target
		List<Target> targets = dao.listTargetByKpi(id, version);
		if (targets != null && !targets.isEmpty()) {
			String names = "";
			for (int i = 0; i < targets.size(); i++) {
				if (i != 0) {
					names += ", ";
				}
				names += targets.get(i).getName();
			}
			jsError.addErrorKey("newKpi.kpi.kpiIsUsedByTargets", names);
		}
		// Check if any relation exists with Scheduler
		List<KpiScheduler> schedulers = dao.listSchedulerByKpi(id, version);
		if (schedulers != null && !schedulers.isEmpty()) {
			String names = "";
			for (int i = 0; i < schedulers.size(); i++) {
				if (i != 0) {
					names += ", ";
				}
				names += schedulers.get(i).getName();
			}
			jsError.addErrorKey("newKpi.kpi.kpiIsUsedBySchedulers", names);
		}
		return jsError;
	}

	private void checkValidity(KpiScheduler scheduler) throws SpagoBIException {
		String fieldValue = null;
		String fieldName = null;
		if (!scheduler.getFrequency().getEndTime().isEmpty() && !scheduler.getFrequency().getStartTime().matches("\\d{2}:\\d{2}")) {
			fieldName = "StartTime";
			fieldValue = scheduler.getFrequency().getStartTime();
		}
		if (scheduler.getFrequency().getEndTime() != null && !scheduler.getFrequency().getEndTime().isEmpty()
				&& !scheduler.getFrequency().getEndTime().matches("\\d{2}:\\d{2}")) {
			fieldName = "EndTime";
			fieldValue = scheduler.getFrequency().getEndTime();
		}
		if (fieldName != null) {
			throw new SpagoBIException("Field error: " + fieldName + "[" + fieldValue + "]");
		}
	}

	private void checkMandatory(KpiScheduler scheduler) throws SpagoBIException {
		String fieldName = null;
		if (scheduler.getName() == null) {
			fieldName = "Name";
		} else if (scheduler.getFrequency() == null) {
			fieldName = "Frequency";
		} else if (scheduler.getDelta() == null) {
			fieldName = "Delta";
		} else if (scheduler.getFrequency().getStartDate() == null) {
			fieldName = "StartDate";
		} else if (scheduler.getFrequency().getCron() == null) {
			fieldName = "Crono";
		} else if (scheduler.getDelta() == null) {
			fieldName = "Delta";
		} else if (scheduler.getKpis() == null || scheduler.getKpis().isEmpty()) {
			fieldName = "Kpi list";
		} else if (scheduler.getFrequency().getStartTime() == null) {
			fieldName = "StartTime";
		}
		if (scheduler.getFilters() != null && !scheduler.getFilters().isEmpty()) {
			for (SchedulerFilter filter : scheduler.getFilters()) {
				if (filter.getPlaceholderName() == null || filter.getValue() == null) {
					fieldName = "PlaceholderName [" + filter.getPlaceholderName() + "] value [" + filter.getValue() + "]";
				}
			}
		}
		if (fieldName != null) {
			throw new SpagoBIException(fieldName + " is mandatory ");
		}
	}

	private void check(Scorecard scorecard) throws SpagoBIException {
		if (scorecard.getName() == null) {
			throw new SpagoBIException("Service [/saveScorecard]: Some fields are mandatory");
		}
	}

	/**
	 * Check if placeholders with default value are a subset of placeholders linked to measures used in kpi definition (ie kpi formula)
	 *
	 * @param servlet     request
	 * @param placeholder
	 * @throws EMFUserError
	 * @throws SpagoBIException
	 * @throws JSONException
	 */
	private void checkPlaceholder(HttpServletRequest req, Kpi kpi) throws EMFUserError, SpagoBIException {
		Iterator<String> placeholders = null;
		try {
			placeholders = new JSONObject(kpi.getPlaceholder()).keys();
		} catch (JSONException e) {
			throw new SpagoBIServiceException(getMessage(NEW_KPI_KPI_PLACEHOLDER_NOT_VALID_JSON), e);
		}
		if (placeholders.hasNext()) {
			try {
				JSONArray measuresArray = new JSONObject(kpi.getDefinition()).getJSONArray("measures");
				IKpiDAO dao = getKpiDAO(req);
				List<String> measureList = new ArrayList<>();
				for (int i = 0; i < measuresArray.length(); i++) {
					if (!measureList.contains(measuresArray.getString(i))) {
						measureList.add(measuresArray.getString(i));
					}
				}
				if (!measureList.isEmpty()) {
					List<String> lst = dao.listPlaceholderByMeasures(measureList);
					while (placeholders.hasNext()) {
						String placeholder = placeholders.next();
						if (!lst.contains(placeholder)) {
							throw new SpagoBIException("Placeholder \"" + placeholder + "\" not valid");
						}
					}
				}
			} catch (JSONException e) {
				throw new SpagoBIServiceException("KPI - checkPlaceholder - error", e);
			}
		}
	}

	private class Measure {
		String name;
		List<String> selectedAttrs = new ArrayList<>();
	}

	private void checkCardinality(JSError errors, String cardinality, String definition) throws JSONException, EMFUserError {
		JSONArray measureArray = new JSONObject(cardinality).getJSONArray("measureList");
		JSONArray measureOfFormulaArray = new JSONObject(definition).getJSONArray("measures");
		if (measureArray.length() != measureOfFormulaArray.length()) {
			errors.addErrorKey(NEW_KPI_CARDINALITY_ERROR);
		}
		if (!errors.hasErrors()) {
			List<Measure> measureLst = new ArrayList<>();
			for (int i = 0; i < measureArray.length(); i++) {
				String measureName = measureArray.getJSONObject(i).getString(MEASURE_NAME);
				if (!measureOfFormulaArray.get(i).equals(measureName)) {
					errors.addErrorKey(NEW_KPI_CARDINALITY_ERROR);
					break;
				}
				JSONObject misuraJson = measureArray.getJSONObject(i);
				JSONObject attrs = misuraJson.optJSONObject(MEASURE_ATTRIBUTES);
				Measure measure = new Measure();
				measure.name = misuraJson.getString(MEASURE_NAME);
				measureLst.add(measure);
				if (attrs != null) {
					Iterator<String> attrNames = attrs.keys();
					while (attrNames.hasNext()) {
						String attr = attrNames.next();
						if (attrs.optBoolean(attr)) {
							measure.selectedAttrs.add(attr);
						}
					}
				}
			}
			Collections.sort(measureLst, new Comparator<Measure>() {
				@Override
				public int compare(Measure m1, Measure m2) {
					return m1.selectedAttrs.size() - m2.selectedAttrs.size();
				}
			});
			for (int i = 1; i < measureLst.size(); i++) {
				Measure prevMeasure = measureLst.get(i - 1);
				Measure currMeasure = measureLst.get(i);
				if (!currMeasure.selectedAttrs.containsAll(prevMeasure.selectedAttrs)) {
					errors.addErrorKey(NEW_KPI_CARDINALITY_ERROR);
				}
			}
		}
	}

	private void checkMandatory(JSError errors, Threshold threshold) {
		if (threshold.getName() == null) {
			errors.addErrorKey(NEW_KPI_THRESHOLD_NAME_MANDATORY);
		}
		if (threshold.getType() == null && threshold.getTypeId() == null) {
			errors.addErrorKey(NEW_KPI_THRESHOLD_TYPE_MANDATORY);
		}
		if (threshold.getThresholdValues() == null || threshold.getThresholdValues().size() == 0) {
			errors.addErrorKey(NEW_KPI_THRESHOLD_VALUES_MANDATORY);
		}
	}

	private static IEngUserProfile getProfile(HttpServletRequest req) {
		return (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	}

	private static void setProfile(HttpServletRequest req, ISpagoBIDao dao) {
		dao.setUserProfile(getProfile(req));
	}

	private static IKpiDAO getKpiDAO(HttpServletRequest req) throws EMFUserError {
		logger.debug("IN");
		IKpiDAO dao = DAOFactory.getKpiDAO();
		setProfile(req, dao);
		logger.debug("OUT");
		return dao;
	}

	private void checkMandatory(HttpServletRequest req, JSError jsError, Kpi kpi) throws JSONException, EMFUserError {
		if (kpi.getName() == null) {
			jsError.addErrorKey(NEW_KPI_NAME_MANDATORY);
		}
		if (kpi.getDefinition() == null) {
			jsError.addErrorKey(NEW_KPI_DEFINITION_MANDATORY);
		} else {
			// validating kpi formula
			ScriptEngineManager sm = new ScriptEngineManager();
			ScriptEngine engine = sm.getEngineByExtension("js");
			String script = new JSONObject(kpi.getDefinition()).getString("formula");
			script = script.replace("M", "");
			if (script.matches("[\\s\\+\\-\\*/\\d\\(\\)]+")) {
				try {
					engine.eval(script);
				} catch (Throwable e) {
					jsError.addErrorKey(NEW_KPI_DEFINITION_SYNTAXERROR);
				}
			} else {
				jsError.addErrorKey(NEW_KPI_DEFINITION_INVALIDCHARACTERS);
			}
			// validating kpi formula
			JSONArray measureArray = new JSONObject(kpi.getDefinition()).getJSONArray("measures");
			IKpiDAO dao = getKpiDAO(req);
			String[] measures = measureArray.join(",").split(",");
			dao.existsMeasureNames(measures);
		}
	}

	private JSONObject executeQuery(Integer dataSourceId, String query, Integer maxItem, Set<Placeholder> placeholders, IEngUserProfile profile)
			throws JSONException, EMFUserError, EMFInternalError {

		Map<String, String> parameterMap = new HashMap<>();

		if (placeholders != null && !placeholders.isEmpty()) {
			for (Placeholder placeholder : placeholders) {
				String value = placeholder.getValue();
				if (value != null) {
					// To execute query through IDataSet, parameters must be quoted manually
					value = "'" + value.trim().replace("'", "") + "'";
				}
				parameterMap.put(placeholder.getName(), placeholder.getValue());
			}
			// Replacing parameters from "@name" to "$P{name}" notation as expected by IDataSet
			for (String paramName : parameterMap.keySet()) {
				query = query.replaceAll("\\@\\b" + paramName + "\\b", "\\$P{" + paramName + "}");
			}
		}

		IDataSet dataSet = null;
		String queryScript = "";
		String queryScriptLanguage = "";

		JSONObject jsonDsConfig = new JSONObject();
		jsonDsConfig.put(DataSetConstants.QUERY, query);
		jsonDsConfig.put(DataSetConstants.QUERY_SCRIPT, "");
		jsonDsConfig.put(DataSetConstants.QUERY_SCRIPT_LANGUAGE, "");
		jsonDsConfig.put(DataSetConstants.DATA_SOURCE, dataSourceId);

		IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByID(dataSourceId);
		if (dataSource != null) {
			if (dataSource.getHibDialectClass().toLowerCase().contains("mongo")) {
				dataSet = new MongoDataSet();
			} else {
				dataSet = JDBCDatasetFactory.getJDBCDataSet(dataSource);
			}
			dataSet.setParamsMap(parameterMap);
			((ConfigurableDataSet) dataSet).setDataSource(dataSource);
			((ConfigurableDataSet) dataSet).setQuery(query);
			((ConfigurableDataSet) dataSet).setQueryScript(queryScript);
			((ConfigurableDataSet) dataSet).setQueryScriptLanguage(queryScriptLanguage);
		} else {
			throw new EMFInternalError(EMFErrorSeverity.BLOCKING, "A datasource with id " + dataSourceId + " could not be found");
		}

		dataSet.setConfiguration(jsonDsConfig.toString());
		dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));

		IDataStore dataStore = dataSet.test(0, maxItem, maxItem);

		JSONDataWriter dataSetWriter = new JSONDataWriter();
		JSONObject dataSetJSON = (JSONObject) dataSetWriter.write(dataStore);
		JSONObject ret = new JSONObject();

		JSONArray fields = dataSetJSON.getJSONObject(JSONDataWriter.METADATA).getJSONArray("fields");
		JSONArray columns = new JSONArray();
		ret.put("columns", columns);
		// skipping i=0 that is "recNo" value
		for (int i = 1; i < fields.length(); i++) {
			JSONObject column = fields.getJSONObject(i);
			JSONObject col = new JSONObject();
			col.put("name", column.getString("name"));
			col.put("label", column.getString("header"));
			col.put("type", column.getString("type"));
			columns.put(col);
		}
		ret.put("rows", dataSetJSON.get(JSONDataWriter.ROOT));

		return ret;
	}

	private static String getMessage(String key, String... args) {
		String msg = MessageBuilderFactory.getMessageBuilder().getMessage(key);
		if (args.length > 0) {
			msg = MessageFormat.format(msg, args);
		}
		return msg;
	}

}
