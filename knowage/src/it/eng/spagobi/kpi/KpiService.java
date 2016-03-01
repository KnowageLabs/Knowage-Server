package it.eng.spagobi.kpi;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.kpi.bo.Alias;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
	private static Logger logger = Logger.getLogger(KpiService.class);

	private static final String MEASURE = "MEASURE";
	private static final String MEASURE_NAME = "measureName";
	private static final String MEASURE_ATTRIBUTES = "attributes";

	@GET
	@Path("/listPlaceholder")
	public Response listPlaceholder(@Context HttpServletRequest req) throws EMFUserError {
		List<Placeholder> placeholders = getKpiDAO(req).listPlaceholder();
		return Response.ok(JsonConverter.objectToJson(placeholders, placeholders.getClass())).build();
	}

	@GET
	@Path("/{id}/cloneRule")
	public Response cloneRule(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		getKpiDAO(req).cloneRule(id);
		return Response.ok().build();
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
		List<Alias> aliases = dao.listAliasNotInMeasure();
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
		String placeholders = null;
		try {
			JSONObject obj = RestUtilities.readBodyAsJSONObject(req);

			dataSourceId = obj.getInt("dataSourceId");
			query = obj.getString("query");
			maxItem = obj.optInt("maxItem", 1);
			placeholders = obj.optString("placeholders");

			JSONObject result = executeQuery(dataSourceId, query, maxItem, placeholders, getProfile(req));

			return Response.ok(result.toString()).build();

		} catch (IOException | JSONException | EMFInternalError e) {
			logger.error("dataSourceId[" + dataSourceId + "] query[" + query + "] maxItem[" + maxItem + "] placeholders[" + placeholders + "]");
			logger.error(req.getPathInfo(), e);
		}
		return Response.ok().build();
	}

	@POST
	@Path("/saveRule")
	public Response saveRule(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		try {
			String requestVal = RestUtilities.readBody(req);
			Rule rule = (Rule) JsonConverter.jsonToObject(requestVal, Rule.class);
			if (rule.getId() == null) {
				dao.insertRule(rule);
			} else {
				dao.updateRule(rule);
			}

		} catch (IOException e) {
			throw new SpagoBIServiceException(req.getPathInfo() + " Error while reading input object ", e);
		} catch (SpagoBIException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
		return Response.ok().build();
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

	@GET
	@Path("/{id}/loadThreshold")
	public Response loadThreshold(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		Threshold t = getKpiDAO(req).loadThreshold(id);
		return Response.ok(JsonConverter.objectToJson(t, t.getClass())).build();
	}

	@POST
	@Path("/saveKpi")
	public Response loadKpi(@Context HttpServletRequest req) throws EMFUserError, EMFInternalError {
		IKpiDAO dao = getKpiDAO(req);
		try {
			String requestVal = RestUtilities.readBody(req);
			Kpi kpi = (Kpi) JsonConverter.jsonToObject(requestVal, Kpi.class);

			checkMandatory(kpi);
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
			throw new EMFInternalError(EMFErrorSeverity.BLOCKING, e);
		} /*
		 * catch (SpagoBIException e) { throw new SpagoBIServiceException(req.getPathInfo(), e); }
		 */
		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}/deleteThreshold")
	public Response deleteThreshold(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		dao.removeThreshold(id);
		return Response.ok().build();
	}

	@POST
	@Path("/saveThreshold")
	public Response saveThreshold(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		try {
			String requestVal = RestUtilities.readBody(req);
			Threshold threshold = (Threshold) JsonConverter.jsonToObject(requestVal, Threshold.class);
			checkMandatory(threshold);
			if (threshold.getId() == null) {
				dao.insertThreshold(threshold);
			} else {
				dao.updateThreshold(threshold);
			}
			return Response.ok().build();
		} catch (IOException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	/* ***
	 * Private methods ***
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
		JSONArray measureArray = new JSONArray(cardinality);
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

	private void checkMandatory(Kpi kpi) {
		if (kpi.getName() == null) {
			throw new SpagoBIDOAException("Kpi Name is mandatory.");
		}
		if (kpi.getDefinition() == null) {
			throw new SpagoBIDOAException("Kpi Definition is mandatory.");
		}
	}

	private JSONObject executeQuery(Integer dataSourceId, String query, Integer maxItem, String placeholder, IEngUserProfile profile) throws JSONException,
			EMFUserError, EMFInternalError {

		Map<String, String> parameterMap = new HashMap<>();

		if (placeholder != null && !placeholder.isEmpty()) {
			JSONObject placeholderObj = new JSONObject(placeholder);

			Iterator<String> placeholderNames = placeholderObj.keys();
			while (placeholderNames.hasNext()) {
				String name = placeholderNames.next();
				String value = placeholderObj.getString(name);
				parameterMap.put(name, value);
			}
			// Replacing parameters from notation "@name" to "$P{name}"
			for (String paramName : parameterMap.keySet()) {
				query = query.replaceFirst("\\@\\b" + paramName + "\\b", "$P{" + paramName + "}");
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

	public static void main(String[] args) throws ScriptException {
		ScriptEngineManager sm = new ScriptEngineManager();
		ScriptEngine engine = sm.getEngineByExtension("js");
		String script = "(M0-M1+10) *M2/M0";
		if (script.replace("M", "").matches("[\\s\\+\\-\\*/\\d\\(\\)]+")) {
			script = script.replaceAll("M\\w", "1");
			System.out.println(engine.eval(script));
		} else {
			System.out.println("ko");
		}
	}
}
