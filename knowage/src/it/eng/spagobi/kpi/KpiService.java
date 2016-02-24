package it.eng.spagobi.kpi;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Kpi;
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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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

	private static final String MEASURE = "MEASURE";
	private static final String KPI_KPI_CATEGORY = "KPI_KPI_CATEGORY";
	private static final String KPI_MEASURE_CATEGORY = "KPI_MEASURE_CATEGORY";
	private static final String MEASURE_NAME = "measureName";
	private static final String MEASURE_ATTRIBUTES = "attributes";

	@GET
	@Path("/listMeasureCategory")
	public Response listMeasureCategory(@Context HttpServletRequest req) throws EMFUserError {
		IDomainDAO domain = DAOFactory.getDomainDAO();
		setProfile(req, domain);
		List categories = domain.loadListDomainsByTypeAndTenant(KPI_MEASURE_CATEGORY);
		return Response.ok(JsonConverter.objectToJson(categories, categories.getClass())).build();
	}

	@GET
	@Path("/listKpiCategory")
	public Response listKpiCategory(@Context HttpServletRequest req) throws EMFUserError {
		IDomainDAO domain = DAOFactory.getDomainDAO();
		setProfile(req, domain);
		List categories = domain.loadListDomainsByTypeAndTenant(KPI_KPI_CATEGORY);
		return Response.ok(JsonConverter.objectToJson(categories, categories.getClass())).build();
	}

	@GET
	@Path("/listMeasure")
	public Response listMeasure(@Context HttpServletRequest req) throws EMFUserError {
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
		try {
			JSONObject obj = RestUtilities.readBodyAsJSONObject(req);
			dataSourceId = obj.getInt("dataSourceId");
			query = obj.getString("query");
			maxItem = obj.getInt("maxItem");
		} catch (IOException | JSONException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}

		IDataSet dataSet = null;
		String queryScript = "";
		String queryScriptLanguage = "";

		JSONObject jsonDsConfig = new JSONObject();
		try {
			jsonDsConfig.put(DataSetConstants.QUERY, query);
			jsonDsConfig.put(DataSetConstants.QUERY_SCRIPT, "");
			jsonDsConfig.put(DataSetConstants.QUERY_SCRIPT_LANGUAGE, "");
			jsonDsConfig.put(DataSetConstants.DATA_SOURCE, dataSourceId);
		} catch (JSONException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}

		if (dataSourceId != null) {
			IDataSource dataSource;
			try {
				// dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dataSourceLabel);
				dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByID(dataSourceId);
				if (dataSource != null) {
					if (dataSource.getHibDialectClass().toLowerCase().contains("mongo")) {
						dataSet = new MongoDataSet();
					} else {
						dataSet = JDBCDatasetFactory.getJDBCDataSet(dataSource);
					}
					((ConfigurableDataSet) dataSet).setDataSource(dataSource);
					((ConfigurableDataSet) dataSet).setQuery(query);
					((ConfigurableDataSet) dataSet).setQueryScript(queryScript);
					((ConfigurableDataSet) dataSet).setQueryScriptLanguage(queryScriptLanguage);
				} else {
					throw new SpagoBIServiceException(req.getPathInfo(), "A datasource with id " + dataSourceId + " could not be found");
				}
			} catch (EMFUserError e) {
				e.printStackTrace();
				throw new SpagoBIServiceException(req.getPathInfo(), "Error while retrieving Datasource with id=" + dataSourceId, e);
			}
		}
		dataSet.setConfiguration(jsonDsConfig.toString());
		dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(getProfile(req)));

		IDataStore dataStore = null;
		try {
			dataStore = dataSet.test(0, maxItem, maxItem);
			if (dataStore == null) {
				throw new SpagoBIServiceException(req.getPathInfo(), "Impossible to read resultset");
			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing dataset ", t);
		}
		JSONDataWriter dataSetWriter = new JSONDataWriter();
		JSONObject dataSetJSON = (JSONObject) dataSetWriter.write(dataStore);
		JSONObject ret = new JSONObject();
		try {
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
		} catch (JSONException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
		return Response.ok(ret.toString()).build();
	}

	@POST
	@Path("/saveRule")
	public Response saveRule(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		try {
			String requestVal = RestUtilities.readBody(req);
			Rule rule = (Rule) JsonConverter.jsonToObject(requestVal, Rule.class);
			checkMandatory(rule);
			if (rule.isNewRecord()) {
				dao.insertRule(rule);
			} else {
				dao.updateRule(rule);
			}

		} catch (IOException e) {
			throw new SpagoBIServiceException(req.getPathInfo() + " Error while reading input object ", e);
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
	public Response loadKpi(@Context HttpServletRequest req) throws EMFUserError {
		IKpiDAO dao = getKpiDAO(req);
		try {
			String requestVal = RestUtilities.readBody(req);
			Kpi kpi = (Kpi) JsonConverter.jsonToObject(requestVal, Kpi.class);

			checkMandatory(kpi);
			checkCardinality(kpi.getCardinality());
			checkPlaceholder(kpi.getPlaceholder());

			if (kpi.isNewRecord()) {
				dao.insertKpi(kpi);
			} else {
				dao.updateKpi(kpi);
			}

			return Response.ok(JsonConverter.objectToJson(kpi, kpi.getClass())).build();
		} catch (IOException e) {
			e.printStackTrace();
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} catch (SpagoBIException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	/* Private methods */

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

	private void checkCardinality(String cardinality) throws JSONException, SpagoBIException {
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
			System.out.println("check " + prevMeasure.name + " with " + currMeasure.name);
			if (!currMeasure.selectedAttrs.containsAll(prevMeasure.selectedAttrs)) {
				throw new SpagoBIException("Check on measure [" + prevMeasure.name + "] failed");
			}
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
		if (kpi.getName() == null || kpi.getCategory() == null || kpi.getDefinition() == null || kpi.getCardinality() == null) {
			throw new SpagoBIDOAException("all fields are mandatory");
		}
	}

	private void checkMandatory(Rule rule) {
		if (rule.getName() == null || rule.getDefinition() == null) {
			throw new SpagoBIDOAException("all fields are mandatory");
		}

	}

}
