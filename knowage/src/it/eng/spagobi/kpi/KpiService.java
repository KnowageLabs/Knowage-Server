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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Cardinality;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Threshold;
import it.eng.spagobi.kpi.dao.IKpiDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
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

/**
 * @authors Salvatore Lupo (Salvatore.Lupo@eng.it)
 *
 */
@Path("/1.0/kpi")
@ManageAuthorization
public class KpiService {
	private static Logger logger = Logger.getLogger(KpiService.class);

	private static final String MEASURE = "MEASURE";
	private static final String MEASURE_NAME = "measureName";
	private static final String MEASURE_ATTRIBUTES = "attributes";

	private static IMessageBuilder message = MessageBuilderFactory.getMessageBuilder();

	@POST
	@Path("/buildCardinalityMatrix")
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
	public Response listPlaceholderByMeasures(@Context HttpServletRequest req) throws EMFUserError {
		try {
			String arrayOfMeasures = RestUtilities.readBody(req);
			List measureList = (List) JsonConverter.jsonToObject(arrayOfMeasures, List.class);
			IKpiDAO dao = getKpiDAO(req);
			List<String> lst = dao.listPlaceholderByMeasures(measureList);
			return Response.ok(JsonConverter.objectToJson(lst, lst.getClass())).build();
		} catch (IOException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	@GET
	@Path("/listPlaceholder")
	public Response listPlaceholder(@Context HttpServletRequest req) throws EMFUserError {
		List<Placeholder> placeholders = getKpiDAO(req).listPlaceholder();
		return Response.ok(JsonConverter.objectToJson(placeholders, placeholders.getClass())).build();
	}

	@GET
	@Path("/listMeasure")
	public Response listMeasure(@Context HttpServletRequest req, @QueryParam("orderProperty") String orderProperty, @QueryParam("orderType") String orderType)
			throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<RuleOutput> measures = dao.listRuleOutputByType(MEASURE);
		return Response.ok(JsonConverter.objectToJson(measures, measures.getClass())).build();
	}

	@GET
	@Path("/{name}/existsMeasure")
	public String loadMeasureByName(@PathParam("name") String name, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		RuleOutput ruleOutput = dao.loadMeasureByName(name);
		return ruleOutput != null ? "true" : "false";
	}

	@GET
	@Path("/{id}/loadRule")
	public Response loadRule(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		Rule r = dao.loadRule(id);
		return Response.ok(JsonConverter.objectToJson(r, r.getClass())).build();
	}

	@GET
	@Path("/listAlias")
	public Response listAlias(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<Alias> aliases = dao.listAlias();
		return Response.ok(JsonConverter.objectToJson(aliases, aliases.getClass())).build();
	}

	@GET
	@Path("/listAvailableAlias")
	public Response listAvailableAlias(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<Alias> aliases = dao.listAliasNotInMeasure(null);
		return Response.ok(JsonConverter.objectToJson(aliases, aliases.getClass())).build();
	}

	@GET
	@Path("/listAvailableAlias/{ruleId}")
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
	public Response preSave(@Context HttpServletRequest req) throws EMFUserError {
		try {
			String obj = RestUtilities.readBody(req);
			Rule rule = (Rule) JsonConverter.jsonToObject(obj, Rule.class);

			executeQuery(rule.getDataSourceId(), rule.getDefinition(), 1, rule.getPlaceholders(), getProfile(req));

			IKpiDAO kpiDao = getKpiDAO(req);
			Map<String, List<String>> aliasErrorMap = kpiDao.aliasValidation(rule);
			if (!aliasErrorMap.isEmpty()) {
				JSONObject jsonError = new JSONObject();
				JSONArray errors = new JSONArray();
				JSONObject msg = new JSONObject();
				errors.put(msg);
				jsonError.put("errors", errors);
				for (Entry<String, List<String>> error : aliasErrorMap.entrySet()) {
					msg.put("message", MessageFormat.format(message.getMessage(error.getKey()), error.getValue()));
				}
				return Response.ok(jsonError.toString()).build();
			}
			return Response.ok().build();
		} catch (Exception e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	@POST
	@Path("/saveRule")
	public Response saveRule(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		try {
			String requestVal = RestUtilities.readBody(req);
			Rule rule = (Rule) JsonConverter.jsonToObject(requestVal, Rule.class);
			Integer id = rule.getId();
			if (id == null) {
				id = dao.insertRule(rule);
			} else {
				dao.updateRule(rule);
			}
			return Response.ok("{\"id\":" + id + "}").build();
		} catch (IOException e) {
			throw new SpagoBIServiceException(req.getPathInfo() + " Error while reading input object ", e);
		} catch (SpagoBIException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	@GET
	@Path("/listKpi")
	public Response listKpi(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<Kpi> kpis = dao.listKpi();
		return Response.ok(JsonConverter.objectToJson(kpis, kpis.getClass())).build();
	}

	@GET
	@Path("/{id}/loadKpi")
	public Response loadKpi(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		Kpi kpi = getKpiDAO(req).loadKpi(id);
		return Response.ok(JsonConverter.objectToJson(kpi, kpi.getClass())).build();
	}

	@GET
	@Path("/listThreshold")
	public Response listThreshold(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		List<Threshold> tt = dao.listThreshold();
		return Response.ok(JsonConverter.objectToJson(tt, tt.getClass())).build();
	}

	@POST
	@Path("/saveKpi")
	public Response saveKpi(@Context HttpServletRequest req) throws EMFUserError, EMFInternalError {
		IKpiDAO dao = getKpiDAO(req);
		try {
			String requestVal = RestUtilities.readBody(req);
			Kpi kpi = (Kpi) JsonConverter.jsonToObject(requestVal, Kpi.class);

			List<String> errors = new ArrayList<>();
			checkMandatory(errors, kpi);
			if (kpi.getCardinality() != null && !kpi.getCardinality().isEmpty()) {
				checkCardinality(kpi.getCardinality());
			}
			if (kpi.getPlaceholder() != null && !kpi.getPlaceholder().isEmpty()) {
				checkPlaceholder(kpi.getPlaceholder());
			}

			if (kpi.getId() == null) {
				dao.insertKpi(kpi);
			} else {
				dao.updateKpi(kpi);
			}

		} catch (IOException | JSONException e) {
			logger.error(req.getPathInfo(), e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}/deleteKpi")
	public Response deleteKpi(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		dao.removeKpi(id);
		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}/deleteRule")
	public Response deleteRule(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		dao.removeRule(id);
		return Response.ok().build();
	}

	/*
	 * *** Private methods ***
	 */

	/**
	 * @param placeholder
	 * @throws JSONException
	 */
	private void checkPlaceholder(String placeholder) throws JSONException {
		// currently we are checking input string for json format only
		new JSONArray(placeholder);
	}

	private class Measure {
		String name;
		List<String> selectedAttrs = new ArrayList<>();
	}

	private void checkCardinality(String cardinality) throws JSONException, EMFUserError {
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
				throw new EMFUserError(EMFErrorSeverity.ERROR, "kpi.cardinality.error");
			}
		}
	}

	private void checkMandatory(Threshold threshold) {
		if (threshold.getName() == null) {
			throw new SpagoBIDOAException("Threshold Name is mandatory.");
		}
		if (threshold.getType() == null && threshold.getTypeId() == null) {
			throw new SpagoBIDOAException("Threshold Type is mandatory.");
		}
		if (threshold.getThresholdValues() == null || threshold.getThresholdValues().size() == 0) {
			throw new SpagoBIDOAException("Error. There are no threshold values.");
		}
	}

	private static IEngUserProfile getProfile(HttpServletRequest req) {
		return (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	}

	private static void setProfile(HttpServletRequest req, ISpagoBIDao dao) {
		dao.setUserProfile(getProfile(req));
	}

	private static IKpiDAO getKpiDAO(HttpServletRequest req) throws EMFUserError {
		// TODO rename getNewKpiDAO to getKpiDAO
		IKpiDAO dao = DAOFactory.getNewKpiDAO();
		setProfile(req, dao);
		return dao;
	}

	private void checkMandatory(List<String> errors, Kpi kpi) {
		if (kpi.getName() == null) {
			errors.add("Kpi Name is mandatory");
		}
		if (kpi.getDefinition() == null) {
			errors.add("Kpi Definition is mandatory.");
		} else {

			ScriptEngineManager sm = new ScriptEngineManager();
			ScriptEngine engine = sm.getEngineByExtension("js");
			String script = kpi.getDefinition();
			script = script.replace("M", "");
			if (script.matches("[\\s\\+\\-\\*/\\d\\(\\)]+")) {
				try {
					engine.eval(script);
				} catch (Throwable e) {
					errors.add("Syntax error in definition");
				}
			} else {
				errors.add("Definition contains invalid characters");
			}
		}
	}

	private JSONObject executeQuery(Integer dataSourceId, String query, Integer maxItem, List<Placeholder> placeholders, IEngUserProfile profile)
			throws JSONException, EMFUserError, EMFInternalError {

		Map<String, String> parameterMap = new HashMap<>();

		if (placeholders != null && !placeholders.isEmpty()) {
			for (Placeholder placeholder : placeholders) {
				parameterMap.put(placeholder.getName(), placeholder.getValue());
			}
			// Replacing parameters from notation "@name" to "$P{name}"
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
			columns.put(col);
		}
		ret.put("rows", dataSetJSON.get(JSONDataWriter.ROOT));

		return ret;
	}
}
