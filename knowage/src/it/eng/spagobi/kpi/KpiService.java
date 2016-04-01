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

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Cardinality;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.KpiScheduler;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Scorecard;
import it.eng.spagobi.kpi.bo.ScorecardPerspective;
import it.eng.spagobi.kpi.bo.ScorecardTarget;
import it.eng.spagobi.kpi.bo.Target;
import it.eng.spagobi.kpi.bo.TargetValue;
import it.eng.spagobi.kpi.bo.Threshold;
import it.eng.spagobi.kpi.dao.IKpiDAO;
import it.eng.spagobi.kpi.dao.KpiDAOImpl.STATUS;
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
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

/**
 * @authors Salvatore Lupo (Salvatore.Lupo@eng.it)
 *
 */
@Path("/1.0/kpi")
@ManageAuthorization
public class KpiService {
	private static final String NEW_KPI_KPI_PLACEHOLDER_NOT_VALID_JSON = "newKpi.kpi.placeholder.notValidJson";
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

	private static final String JOB_GROUP = "KPI_SCHEDULER_GROUP";

	private static Logger logger = Logger.getLogger(KpiService.class);

	private static final String MEASURE = "MEASURE";
	private static final String MEASURE_NAME = "measureName";
	private static final String MEASURE_ATTRIBUTES = "attributes";

	@POST
	@Path("/buildCardinalityMatrix")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response buildCardinalityMatrix(@Context HttpServletRequest req) throws EMFUserError {
		try {
			String arrayOfMeasures = RestUtilities.readBody(req);
			List<String> measureList = (List) JsonConverter.jsonToObject(arrayOfMeasures, List.class);
			IKpiDAO dao = getKpiDAO(req);
			List<Cardinality> lst = dao.buildCardinality(measureList);
			return Response.ok(JsonConverter.objectToJson(lst, lst.getClass())).build();
		} catch (IOException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	@POST
	@Path("/listPlaceholderByMeasures")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listPlaceholderByMeasures(@Context HttpServletRequest req) throws EMFUserError {
		try {
			String arrayOfMeasures = RestUtilities.readBody(req);
			List measureList = (List) JsonConverter.jsonToObject(arrayOfMeasures, List.class);
			IKpiDAO dao = getKpiDAO(req);
			List<String> lst = dao.listPlaceholderByMeasures(measureList);
			return Response.ok(JsonConverter.objectToJson(lst, lst.getClass())).build();
		} catch (IOException e) {
			logger.error(req.getPathInfo(), e);
		}
		return Response.ok().build();
	}

	@POST
	@Path("/listPlaceholderByKpi")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listPlaceholderByKpi(@Context HttpServletRequest req) throws EMFUserError {
		try {
			String arrayOfMeasures = RestUtilities.readBody(req);
			List<String> kpiNames = (List) JsonConverter.jsonToObject(arrayOfMeasures, List.class);
			IKpiDAO dao = getKpiDAO(req);
			if (kpiNames != null && !kpiNames.isEmpty()) {
				Map<String, List<String>> lst = dao.listPlaceholderByKpiList(kpiNames);
				return Response.ok(JsonConverter.objectToJson(lst, lst.getClass())).build();
			}
		} catch (IOException e) {
			logger.error(req.getPathInfo(), e);
		}
		return Response.ok().build();
	}

	@GET
	@Path("/listPlaceholder")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listPlaceholder(@Context HttpServletRequest req) throws EMFUserError {
		List<Placeholder> placeholders = getKpiDAO(req).listPlaceholder();
		return Response.ok(JsonConverter.objectToJson(placeholders, placeholders.getClass())).build();
	}

	@GET
	@Path("/listMeasure")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listMeasure(@Context HttpServletRequest req, @QueryParam("orderProperty") String orderProperty, @QueryParam("orderType") String orderType)
			throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		// Listing only active records
		List<RuleOutput> measures = dao.listRuleOutputByType(MEASURE, STATUS.ACTIVE);
		return Response.ok(JsonConverter.objectToJson(measures, measures.getClass())).build();
	}

	@GET
	@Path("/{name}/existsMeasure")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public String existsMeasure(@PathParam("name") String name, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		return dao.existsMeasureNames(name).toString();
	}

	@GET
	@Path("/{id}/{version}/loadRule")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadRule(@PathParam("id") Integer id, @PathParam("version") Integer version, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		Rule r = dao.loadRule(id, version);
		return Response.ok(JsonConverter.objectToJson(r, r.getClass())).build();
	}

	@GET
	@Path("/{thresholdId}/loadThreshold")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadThreshold(@PathParam("thresholdId") Integer id, @QueryParam("kpiId") Integer kpiId, @Context HttpServletRequest req)
			throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		Threshold threshold = dao.loadThreshold(id);
		if (dao.isThresholdUsedByOtherKpi(kpiId, id)) {
			threshold.setUsedByKpi(true);
		}
		return Response.ok(JsonConverter.objectToJson(threshold, threshold.getClass())).build();
	}

	@GET
	@Path("/listAlias")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listAlias(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<Alias> aliases = dao.listAlias();
		return Response.ok(JsonConverter.objectToJson(aliases, aliases.getClass())).build();
	}

	@GET
	@Path("/listAvailableAlias")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listAvailableAlias(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<Alias> aliases = dao.listAliasNotInMeasure(null);
		return Response.ok(JsonConverter.objectToJson(aliases, aliases.getClass())).build();
	}

	@GET
	@Path("/listAvailableAlias/{ruleId}")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listAvailableAliasIncludingId(@PathParam("ruleId") Integer ruleId, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<Alias> aliases = dao.listAliasNotInMeasure(ruleId);
		return Response.ok(JsonConverter.objectToJson(aliases, aliases.getClass())).build();
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

		Integer dataSourceId = null;
		String query = null;
		Integer maxItem = null;
		List<Placeholder> placeholders = null;
		try {
			JSONObject obj = RestUtilities.readBodyAsJSONObject(req);
			Rule rule = (Rule) JsonConverter.jsonToObject(obj.getString("rule"), Rule.class);

			maxItem = obj.optInt("maxItem", 1);

			dataSourceId = rule.getDataSourceId();
			query = rule.getDefinition();
			placeholders = rule.getPlaceholders();

			JSONObject result = executeQuery(dataSourceId, query, maxItem, placeholders, getProfile(req));

			return Response.ok(result.toString()).build();

		} catch (IOException | JSONException | EMFInternalError e) {
			logger.error("dataSourceId[" + dataSourceId + "] query[" + query + "] maxItem[" + maxItem + "] placeholders[" + placeholders + "]");
			logger.error(req.getPathInfo(), e);
		}
		return Response.ok().build();
	}

	@POST
	@Path("/preSaveRule")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response preSave(@Context HttpServletRequest req) throws EMFUserError {
		try {
			String obj = RestUtilities.readBody(req);
			Rule rule = (Rule) JsonConverter.jsonToObject(obj, Rule.class);

			executeQuery(rule.getDataSourceId(), rule.getDefinition(), 1, rule.getPlaceholders(), getProfile(req));

			IKpiDAO kpiDao = getKpiDAO(req);
			Map<String, List<String>> aliasErrorMap = kpiDao.aliasValidation(rule);
			if (!aliasErrorMap.isEmpty()) {
				JSONArray errors = new JSONArray();
				for (Entry<String, List<String>> error : aliasErrorMap.entrySet()) {
					errors.put(
							new JSONObject().put("message", getMessage(error.getKey(), new JSONArray(error.getValue()).toString().replaceAll("[\\[\\]]", ""))));
				}
				return Response.ok(new JSONObject().put("errors", errors).toString()).build();
			}
			return Response.ok().build();
		} catch (Exception e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	@POST
	@Path("/saveRule")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response saveRule(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		try {
			String requestVal = RestUtilities.readBody(req);
			Rule rule = (Rule) JsonConverter.jsonToObject(requestVal, Rule.class);
			Integer id = rule.getId();
			Integer version = rule.getVersion();
			// Rule name must be unique
			Integer otherId = dao.getRuleIdByName(rule.getName());
			if (otherId != null && (id == null || !id.equals(otherId))) {
				String errorMsg = getMessage(NEW_KPI_RULE_NAME_NOT_AVAILABLE, rule.getName());
				return Response.ok(new JSONObject().put("errors", new JSONArray().put(new JSONObject().put("message", errorMsg)))).build();
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
			}
			return Response.ok(new JSONObject().put("id", id).put("version", version).toString()).build();
		} catch (IOException e) {
			throw new SpagoBIServiceException(req.getPathInfo() + " Error while reading input object ", e);
		} catch (SpagoBIException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} catch (JSONException e) {
			logger.error("Error while composing error message", e);
		}
		return Response.ok().build();
	}

	@GET
	@Path("/listKpi")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listKpi(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<Kpi> kpis = dao.listKpi(STATUS.ACTIVE);
		return Response.ok(JsonConverter.objectToJson(kpis, kpis.getClass())).build();
	}

	@GET
	@Path("/{id}/{version}/loadKpi")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadKpi(@PathParam("id") Integer id, @PathParam("version") Integer version, @Context HttpServletRequest req) throws EMFUserError {
		Kpi kpi = getKpiDAO(req).loadKpi(id, version);
		return Response.ok(JsonConverter.objectToJson(kpi, kpi.getClass())).build();
	}

	@GET
	@Path("/listThreshold")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listThreshold(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<Threshold> tt = dao.listThreshold();
		return Response.ok(JsonConverter.objectToJson(tt, tt.getClass())).build();
	}

	@POST
	@Path("/saveKpi")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response saveKpi(@Context HttpServletRequest req) throws EMFUserError, EMFInternalError {
		IKpiDAO dao = getKpiDAO(req);
		try {
			String requestVal = RestUtilities.readBody(req);
			Kpi kpi = (Kpi) JsonConverter.jsonToObject(requestVal, Kpi.class);

			List<String> errors = new ArrayList<>();
			checkMandatory(req, errors, kpi);

			if (kpi.getThreshold() == null) {
				errors.add(getMessage(NEW_KPI_THRESHOLD_MANDATORY));
			} else {
				checkMandatory(errors, kpi.getThreshold());
			}

			if (errors.isEmpty()) {
				// kpi name must be unique
				Integer sameNameKpiId = dao.getKpiIdByName(kpi.getName());
				if (sameNameKpiId != null && (kpi.getId() == null || !sameNameKpiId.equals(kpi.getId()))) {
					errors.add(getMessage(NEW_KPI_KPI_NAME_NOT_AVAILABLE, kpi.getName()));
				}
			}

			if (kpi.getCardinality() != null && !kpi.getCardinality().isEmpty()) {
				checkCardinality(errors, kpi.getCardinality());
			}
			if (kpi.getPlaceholder() != null && !kpi.getPlaceholder().isEmpty()) {
				checkPlaceholder(req, kpi);
			}

			if (!errors.isEmpty()) {
				JSONArray errorArray = new JSONArray();
				for (String error : errors) {
					errorArray.put(new JSONObject().put("message", error));
				}
				return Response.ok(new JSONObject().put("errors", errorArray).toString()).build();
			}

			if (kpi.getId() == null) {
				dao.insertKpi(kpi);
			} else {
				if (kpi.isEnableVersioning()) {
					dao.insertNewVersionKpi(kpi);
				} else {
					dao.updateKpi(kpi);
				}
			}

			return Response.ok().build();
		} catch (IOException | JSONException | SpagoBIException e) {
			logger.error(req.getPathInfo(), e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	@DELETE
	@Path("/{id}/{version}/deleteKpi")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response deleteKpi(@PathParam("id") Integer id, @PathParam("version") Integer version, @QueryParam("toBeVersioned") Boolean toBeVersioned,
			@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		dao.removeKpi(id, version, Boolean.TRUE.equals(toBeVersioned));
		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}/{version}/deleteRule")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response deleteRule(@PathParam("id") Integer id, @PathParam("version") Integer version, @Context HttpServletRequest req) throws EMFUserError {
		// Rule can only be removed logically
		IKpiDAO dao = getKpiDAO(req);
		dao.removeRule(id, version, true);
		return Response.ok().build();
	}

	@GET
	@Path("/listTarget")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listTarget(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<Target> targetList = dao.listTarget();
		return Response.ok(JsonConverter.objectToJson(targetList, targetList.getClass()).toString()).build();
	}

	@GET
	@Path("/{targetId}/listKpiWithTarget")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listKpiWithTarget(@PathParam("targetId") Integer targetId, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<TargetValue> kpiList = dao.listKpiWithTarget(targetId);
		return Response.ok(JsonConverter.objectToJson(kpiList, kpiList.getClass()).toString()).build();
	}

	@GET
	@Path("/{id}/loadTarget")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadTarget(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		Target t = dao.loadTarget(id);
		return Response.ok(JsonConverter.objectToJson(t, t.getClass())).build();
	}

	@POST
	@Path("/saveTarget")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response saveTarget(@Context HttpServletRequest req) throws EMFUserError {
		try {
			String requestVal = RestUtilities.readBody(req);
			Target target = (Target) JsonConverter.jsonToObject(requestVal, Target.class);
			check(target);
			IKpiDAO dao = getKpiDAO(req);
			Integer id = target.getId();
			if (id == null) {
				id = dao.insertTarget(target);
			} else {
				dao.updateTarget(target);
			}
			return Response.ok(new JSONObject().put("id", id).toString()).build();
		} catch (IOException | JSONException | SpagoBIException e) {
			logger.error(req.getPathInfo(), e);
		}
		try {
			return Response.ok(new JSONObject().put("errors", new JSONArray().put(new JSONObject().put("message", "Error"))).toString()).build();
		} catch (JSONException e) {
			logger.error(req.getPathInfo(), e);
		}
		return Response.ok().build();
	}

	private void check(Target target) throws SpagoBIException {
		if (target.getName() == null || target.getStartValidity() == null || target.getValues() == null || target.getValues().isEmpty()) {
			throw new SpagoBIException("Service [/saveTarget]: Some fields are mandatory");
		}
	}

	@DELETE
	@Path("/{id}/deleteTarget")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response deleteTarget(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		dao.removeTarget(id);
		return Response.ok().build();
	}

	@GET
	@Path("/listSchedulerKPI")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listSchedulerKPI(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<KpiScheduler> schedulerList = dao.listKpiScheduler();
		return Response.ok(JsonConverter.objectToJson(schedulerList, schedulerList.getClass()).toString()).build();
	}

	@GET
	@Path("/{id}/loadSchedulerKPI")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadSchedulerKPI(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		KpiScheduler t = dao.loadKpiScheduler(id);
		// loading trigger
		// TriggerInfo triggerInfo = SchedulerService.getTriggerInfo(JOB_GROUP + "_" + id, JOB_GROUP, JOB_GROUP + "_" + id, "");
		// Trigger trigger = new Trigger();
		// trigger.setStartTime(triggerInfo.getStartTime());
		return Response.ok(JsonConverter.objectToJson(t, t.getClass())).build();
	}

	@GET
	@Path("/listScorecard")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listScorecard(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<Scorecard> scorecards = dao.listScorecard();
		return Response.ok(JsonConverter.objectToJson(scorecards, scorecards.getClass())).build();
	}

	@GET
	@Path("/{id}/loadScorecard")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadScorecard(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		Scorecard scorecard = dao.loadScorecard(id);
		return Response.ok(JsonConverter.objectToJson(scorecard, scorecard.getClass())).build();
	}

	@POST
	@Path("/saveScorecard")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response saveScorecard(@Context HttpServletRequest req) throws EMFUserError {
		try {
			String requestVal = RestUtilities.readBody(req);
			Scorecard scorecard = (Scorecard) JsonConverter.jsonToObject(requestVal, Scorecard.class);
			scorecard.setCreationDate(new Date());
			check(scorecard);
			IKpiDAO dao = getKpiDAO(req);
			Integer id = scorecard.getId();
			if (id == null) {
				id = dao.insertScorecard(scorecard);
			} else {
				dao.updateScorecard(scorecard);
			}
			return Response.ok(new JSONObject().put("id", id).toString()).build();
		} catch (IOException | JSONException | SpagoBIException e) {
			logger.error(req.getPathInfo(), e);
		}
		try {
			return Response.ok(new JSONObject().put("errors", new JSONArray().put(new JSONObject().put("message", "Error"))).toString()).build();
		} catch (JSONException e) {
			logger.error(req.getPathInfo(), e);
		}
		return Response.ok().build();
	}

	private void check(Scorecard scorecard) throws SpagoBIException {
		if (scorecard.getName() == null) {
			throw new SpagoBIException("Service [/saveScorecard]: Some fields are mandatory");
		}
	}

	@DELETE
	@Path("/{id}/deleteScorecard")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response deleteScorecard(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		dao.removeScorecard(id);
		return Response.ok().build();
	}

	@GET
	@Path("/listScorecardPerspective")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listScorecardPerspective(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<ScorecardPerspective> scorecardsPerspective = dao.listScorecardPerspective();
		return Response.ok(JsonConverter.objectToJson(scorecardsPerspective, scorecardsPerspective.getClass())).build();
	}

	@GET
	@Path("/{id}/loadScorecardPerspective")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadScorecardPerspective(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		ScorecardPerspective scorecardPerspective = dao.loadScorecardPerspective(id);
		return Response.ok(JsonConverter.objectToJson(scorecardPerspective, scorecardPerspective.getClass())).build();
	}

	@POST
	@Path("/saveScorecardPerspective")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response saveScorecardPerspective(@Context HttpServletRequest req) throws EMFUserError {
		try {
			String requestVal = RestUtilities.readBody(req);
			ScorecardPerspective scorecardPerspective = (ScorecardPerspective) JsonConverter.jsonToObject(requestVal, ScorecardPerspective.class);
			check(scorecardPerspective);
			IKpiDAO dao = getKpiDAO(req);
			Integer id = scorecardPerspective.getId();
			if (id == null) {
				id = dao.insertScorecardPerspective(scorecardPerspective);
			} else {
				dao.updateScorecardPerspective(scorecardPerspective);
			}
			return Response.ok(new JSONObject().put("id", id).toString()).build();
		} catch (IOException | JSONException | SpagoBIException e) {
			logger.error(req.getPathInfo(), e);
		}
		try {
			return Response.ok(new JSONObject().put("errors", new JSONArray().put(new JSONObject().put("message", "Error"))).toString()).build();
		} catch (JSONException e) {
			logger.error(req.getPathInfo(), e);
		}
		return Response.ok().build();
	}

	private void check(ScorecardPerspective scorecardPerspective) throws SpagoBIException {
		if (scorecardPerspective.getName() == null) {
			throw new SpagoBIException("Service [/saveScorecardPerspective]: Some fields are mandatory");
		}
	}

	@DELETE
	@Path("/{id}/deleteScorecardPerspective")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response deleteScorecardPerspective(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		dao.removeScorecardPerspective(id);
		return Response.ok().build();
	}

	@GET
	@Path("/listScorecardTarget")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response listScorecardTarget(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<ScorecardTarget> scorecardsTarget = dao.listScorecardTarget();
		return Response.ok(JsonConverter.objectToJson(scorecardsTarget, scorecardsTarget.getClass())).build();
	}

	@GET
	@Path("/{id}/loadScorecardTarget")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response loadScorecardTarget(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		ScorecardTarget scorecardTarget = dao.loadScorecardTarget(id);
		return Response.ok(JsonConverter.objectToJson(scorecardTarget, scorecardTarget.getClass())).build();
	}

	@POST
	@Path("/saveScorecardTarget")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response saveScorecardTarget(@Context HttpServletRequest req) throws EMFUserError {
		try {
			String requestVal = RestUtilities.readBody(req);
			ScorecardTarget scorecardTarget = (ScorecardTarget) JsonConverter.jsonToObject(requestVal, ScorecardTarget.class);
			check(scorecardTarget);
			IKpiDAO dao = getKpiDAO(req);
			Integer id = scorecardTarget.getId();
			if (id == null) {
				id = dao.insertScorecardTarget(scorecardTarget);
			} else {
				dao.updateScorecardTarget(scorecardTarget);
			}
			return Response.ok(new JSONObject().put("id", id).toString()).build();
		} catch (IOException | JSONException | SpagoBIException e) {
			logger.error(req.getPathInfo(), e);
		}
		try {
			return Response.ok(new JSONObject().put("errors", new JSONArray().put(new JSONObject().put("message", "Error"))).toString()).build();
		} catch (JSONException e) {
			logger.error(req.getPathInfo(), e);
		}
		return Response.ok().build();
	}

	private void check(ScorecardTarget scorecardTarget) throws SpagoBIException {
		if (scorecardTarget.getName() == null) {
			throw new SpagoBIException("Service [/saveScorecardTarget]: Some fields are mandatory");
		}
	}

	@DELETE
	@Path("/{id}/deleteScorecardTarget")
	@UserConstraint(functionalities = { SpagoBIConstants.KPI_MANAGEMENT })
	public Response deleteScorecardTarget(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		dao.removeScorecardTarget(id);
		return Response.ok().build();
	}

	/*
	 * *** Private methods ***
	 */

	/**
	 * Check if placeholders with default value are a subset of placeholders linked to measures used in kpi definition (ie kpi formula)
	 *
	 * @param servlet
	 *            request
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

	private void checkCardinality(List<String> errors, String cardinality) throws JSONException, EMFUserError {
		JSONArray measureArray = new JSONObject(cardinality).getJSONArray("measureList");
		List<Measure> measureLst = new ArrayList<>();
		for (int i = 0; i < measureArray.length(); i++) {
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
				errors.add(getMessage(NEW_KPI_CARDINALITY_ERROR));
			}
		}
	}

	private void checkMandatory(List<String> errors, Threshold threshold) {
		if (threshold.getName() == null) {
			errors.add(getMessage(NEW_KPI_THRESHOLD_NAME_MANDATORY));
		}
		if (threshold.getType() == null && threshold.getTypeId() == null) {
			errors.add(getMessage(NEW_KPI_THRESHOLD_TYPE_MANDATORY));
		}
		if (threshold.getThresholdValues() == null || threshold.getThresholdValues().size() == 0) {
			errors.add(getMessage(NEW_KPI_THRESHOLD_VALUES_MANDATORY));
		}
	}

	private static IEngUserProfile getProfile(HttpServletRequest req) {
		return (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	}

	private static void setProfile(HttpServletRequest req, ISpagoBIDao dao) {
		dao.setUserProfile(getProfile(req));
	}

	private static IKpiDAO getKpiDAO(HttpServletRequest req) throws EMFUserError {
		// TODO rename getNewKpiDAO to getKpiDAO when old kpi will be removed
		IKpiDAO dao = DAOFactory.getNewKpiDAO();
		setProfile(req, dao);
		return dao;
	}

	private void checkMandatory(HttpServletRequest req, List<String> errors, Kpi kpi) throws JSONException, EMFUserError {
		if (kpi.getName() == null) {
			errors.add(getMessage(NEW_KPI_NAME_MANDATORY));
		}
		if (kpi.getDefinition() == null) {
			errors.add(getMessage(NEW_KPI_DEFINITION_MANDATORY));
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
					errors.add(getMessage(NEW_KPI_DEFINITION_SYNTAXERROR));
				}
			} else {
				errors.add(getMessage(NEW_KPI_DEFINITION_INVALIDCHARACTERS));
			}
			// validating kpi formula
			JSONArray measureArray = new JSONObject(kpi.getDefinition()).getJSONArray("measures");
			IKpiDAO dao = getKpiDAO(req);
			String[] measures = measureArray.join(",").split(",");
			dao.existsMeasureNames(measures);
		}
	}

	private JSONObject executeQuery(Integer dataSourceId, String query, Integer maxItem, List<Placeholder> placeholders, IEngUserProfile profile)
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
