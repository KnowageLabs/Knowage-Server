package it.eng.spagobi.api.v2;

import static it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils.createParameterValuesMap;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_OPTIONS_KEY;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_QUANTITY_JSON;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_TYPE_JSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DriversRuntimeLoaderFactory;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.api.common.MetaUtils;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.lov.bo.DependenciesPostProcessingLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.exceptions.MissingLOVDependencyException;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.JSONStoreFeedTransformer;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/2.0/documentExeParameters")
@ManageAuthorization
public class DocumentExecutionParametersResource extends AbstractSpagoBIResource {

	public static final String SERVICE_NAME = "GET DOCUMENT PARAMETERS ";

	// request parameters
	public static String PARAMETER_ID = "paramId";
	public static String SELECTED_PARAMETER_VALUES = "parameters";
	public static String FILTERS = "FILTERS";

	/**
	 * @deprecated Replaced by {@link MetaUtils#NODE_ID_SEPARATOR}
	 */
	@Deprecated
	public static String NODE_ID_SEPARATOR = "___SEPA__";

	public static String NODE = "node";
	public static String MODE_SIMPLE = "simple";
	public static String MODE_COMPLETE = "complete";
	public static String MODE_EXTRA = "extra";
	public static String START = "start";
	public static String LIMIT = "limit";
	// in massive export case
	public static String OBJ_PARAMETER_IDS = "OBJ_PARAMETER_IDS";
	public static String CONTEST = "CONTEST"; // used to check if mssive export
	// case; cannot use MODALITY
	// because already in use
	public static String MASSIVE_EXPORT = "massiveExport";
	private static final String ROLE = "role";
	private static final String OBJECT_LABEL = "label";

	private static final String DESCRIPTION_FIELD = "description";

	private static final String VALUE_FIELD = "value";

	private static final String LABEL_FIELD = "label";

	private static IMessageBuilder message = MessageBuilderFactory.getMessageBuilder();

	private static final String[] VISIBLE_COLUMNS = new String[] { VALUE_FIELD, LABEL_FIELD, DESCRIPTION_FIELD };

	static protected Logger logger = Logger.getLogger(DocumentExecutionParametersResource.class);

	@POST
	@Path("/admissibleValues")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getParameters2(@Context HttpServletRequest req) throws Exception {
		return getParameters(req);
	}

	/**
	 * @throws Exception
	 * @deprecated Replaced by {@link #getParameters2(HttpServletRequest)}
	 */
	@POST
	@Path("/getParameters")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@Deprecated
	public String getParameters(@Context HttpServletRequest req) throws Exception {

		String result = "";

		String biparameterId;
		JSONArray selectedParameterValuesJSON;
		JSONObject filtersJSON = null;
		Map selectedParameterValues;
		JSONObject valuesJSON;
		// String contest;

		// ExecutionInstance executionInstance;

		List rows;

		ILovDetail lovProvDet;

		List objParameterIds;
		int treeLovNodeLevel = 0;
		String treeLovNodeValue = null;
		String role;
		String label;
		Integer start = null;
		Integer limit = null;

		// PARAMETER

		try {
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
			role = (String) requestVal.opt(ROLE);
			label = (String) requestVal.opt(OBJECT_LABEL);
			biparameterId = (String) requestVal.opt(PARAMETER_ID);
			selectedParameterValuesJSON = (JSONArray) requestVal.opt(SELECTED_PARAMETER_VALUES);
			if (requestVal.opt(FILTERS) != null) {
				filtersJSON = (JSONObject) requestVal.opt(FILTERS);
			}
			// contest = (String) requestVal.opt(CONTEST);
			if (requestVal.opt(NODE) != null) {
				treeLovNodeValue = (String) requestVal.opt(NODE);
				if (treeLovNodeValue.contains("lovroot")) {
					treeLovNodeValue = "lovroot";
					treeLovNodeLevel = 0;
				} else {
					String[] splittedNode = treeLovNodeValue.split(NODE_ID_SEPARATOR);
					treeLovNodeValue = splittedNode[0];
					treeLovNodeLevel = new Integer(splittedNode[1]);
				}
			}
			if (requestVal.opt(START) != null) {
				start = (Integer) requestVal.opt(START);
			}
			if (requestVal.opt(LIMIT) != null) {
				limit = (Integer) requestVal.opt(LIMIT);
			}

			try {
				MetaUtils metaUtils = MetaUtils.getInstance();
				BIObject obj = DriversRuntimeLoaderFactory.getDriversRuntimeLoader().loadBIObjectForExecutionByLabelAndRole(label, role);
				ArrayList<HashMap<String, Object>> qbeDrivers = metaUtils.getQbeDrivers(getUserProfile(), request.getLocale(), obj);
				if (qbeDrivers == null || qbeDrivers.isEmpty()) {
					BIObjectParameter biObjectParameter;
					List<ObjParuse> biParameterExecDependencies;
					DocumentRuntime dum = new DocumentRuntime(this.getUserProfile(), req.getLocale());
					if (selectedParameterValuesJSON != null) {
						dum.refreshParametersValues(selectedParameterValuesJSON, false, obj);
					}

					selectedParameterValues = createParameterValuesMap(selectedParameterValuesJSON);

					// START get the relevant biobject parameter
					biObjectParameter = null;
					List parameters = obj.getDrivers();
					for (int i = 0; i < parameters.size(); i++) {
						BIObjectParameter p = (BIObjectParameter) parameters.get(i);
						if (biparameterId.equalsIgnoreCase(p.getParameterUrlName())) {
							biObjectParameter = p;
							break;
						}
					}
					Assert.assertNotNull(biObjectParameter, "Impossible to find parameter [" + biparameterId + "]");
					// END get the relevant biobject parameter

					// Date Range managing
					// try {
					// Parameter parameter = biObjectParameter.getParameter();
					// if (DateRangeDAOUtilities.isDateRange(parameter)) {
					// valuesJSON = manageDataRange(biObjectParameter, role, req);
					// result = buildJsonResult("OK", "", valuesJSON, null, biparameterId).toString();
					// return result;
					// }
					// } catch (Exception e) {
					// throw new SpagoBIServiceException(SERVICE_NAME, "Error on loading date range combobox values", e);
					// }

					lovProvDet = dum.getLovDetail(biObjectParameter);
					// START get the lov result
					String lovResult = null;
					try {
						// get the result of the lov
						IEngUserProfile profile = getUserProfile();

						// get from cache, if available
						LovResultCacheManager executionCacheManager = new LovResultCacheManager();
						lovResult = executionCacheManager.getLovResultDum(profile, lovProvDet, dum.getDependencies(biObjectParameter, role), obj, true,
								req.getLocale());

						// get all the rows of the result
						LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
						rows = lovResultHandler.getRows();

					} catch (MissingLOVDependencyException mldaE) {
						String localizedMessage = getLocalizedMessage("sbi.api.documentExecParameters.dependencyNotFill", req);
						String msg = localizedMessage + ": " + mldaE.getDependsFrom();
						throw new SpagoBIServiceException(SERVICE_NAME, msg);
					} catch (Exception e) {
						throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's values", e);
					}

					Assert.assertNotNull(lovResult, "Impossible to get parameter's values");
					// END get the lov result

					// START filtering the list by filtering toolbar
					try {
						if (filtersJSON != null) {
							String valuefilter = (String) filtersJSON.get(SpagoBIConstants.VALUE_FILTER);
							String columnfilter = (String) filtersJSON.get(SpagoBIConstants.COLUMN_FILTER);
							String typeFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_FILTER);
							String typeValueFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_VALUE_FILTER);
							rows = DelegatedBasicListService.filterList(rows, valuefilter, typeValueFilter, columnfilter, typeFilter);
						}
					} catch (JSONException e) {
						throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to read filter's configuration", e);
					}
					// END filtering the list by filtering toolbar

					// START filtering for correlation (only for
					// DependenciesPostProcessingLov, i.e. scripts, java classes and
					// fixed lists)
					biParameterExecDependencies = dum.getDependencies(biObjectParameter, role);
					if (lovProvDet instanceof DependenciesPostProcessingLov && selectedParameterValues != null && biParameterExecDependencies != null
							&& biParameterExecDependencies.size() > 0) { // && contest != null && !contest.equals(MASSIVE_EXPORT)
						rows = ((DependenciesPostProcessingLov) lovProvDet).processDependencies(rows, selectedParameterValues, biParameterExecDependencies);
					}
					// END filtering for correlation

					if (lovProvDet.getLovType() != null && lovProvDet.getLovType().contains("tree")) {
						JSONArray valuesJSONArray = metaUtils.getChildrenForTreeLov(lovProvDet, rows, treeLovNodeLevel, treeLovNodeValue);
						result = metaUtils.buildJsonResult("OK", "", null, valuesJSONArray, biparameterId).toString();
					} else {
						valuesJSON = metaUtils.buildJSONForLOV(lovProvDet, rows, start, limit);
						result = metaUtils.buildJsonResult("OK", "", valuesJSON, null, biparameterId).toString();
					}
				} else {
					BusinessModelRuntime bum = new BusinessModelRuntime(UserProfileManager.getProfile(), req.getLocale());
					if (selectedParameterValuesJSON != null) {
						bum.refreshParametersMetamodelValues(selectedParameterValuesJSON, false, obj);
					}

					selectedParameterValues = createParameterValuesMap(selectedParameterValuesJSON);

					// START get the relevant biobject parameter
					BIMetaModelParameter biMetaModelParameter = null;
					List parameters = obj.getMetamodelDrivers();
					for (int i = 0; i < parameters.size(); i++) {
						BIMetaModelParameter p = (BIMetaModelParameter) parameters.get(i);
						if (biparameterId.equalsIgnoreCase(p.getParameterUrlName())) {
							biMetaModelParameter = p;

							break;
						}
					}
					Assert.assertNotNull(biMetaModelParameter, "Impossible to find parameter [" + biparameterId + "]");
					// END get the relevant biobject parameter

					lovProvDet = bum.getLovDetail(biMetaModelParameter);
					// START get the lov result
					String lovResult = null;
					List<MetaModelParuse> biParameterExecDependencies = bum.getDependencies(biMetaModelParameter, role);
					try {
						// get the result of the lov
						IEngUserProfile profile = getUserProfile();

						// get from cache, if available
						LovResultCacheManager executionCacheManager = new LovResultCacheManager();
						lovResult = executionCacheManager.getLovResultBum(profile, lovProvDet, biParameterExecDependencies, obj, true, req.getLocale());

						// get all the rows of the result
						LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
						rows = lovResultHandler.getRows();

					} catch (MissingLOVDependencyException mldaE) {
						String localizedMessage = getLocalizedMessage("sbi.api.documentExecParameters.dependencyNotFill", req);
						String msg = localizedMessage + ": " + mldaE.getDependsFrom();
						throw new SpagoBIServiceException(SERVICE_NAME, msg);
					} catch (Exception e) {
						throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's values", e);
					}

					Assert.assertNotNull(lovResult, "Impossible to get parameter's values");
					// END get the lov result

					// START filtering the list by filtering toolbar
					try {
						if (filtersJSON != null) {
							String valuefilter = (String) filtersJSON.get(SpagoBIConstants.VALUE_FILTER);
							String columnfilter = (String) filtersJSON.get(SpagoBIConstants.COLUMN_FILTER);
							String typeFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_FILTER);
							String typeValueFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_VALUE_FILTER);
							rows = DelegatedBasicListService.filterList(rows, valuefilter, typeValueFilter, columnfilter, typeFilter);
						}
					} catch (JSONException e) {
						throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to read filter's configuration", e);
					}
					// END filtering the list by filtering toolbar

					// START filtering for correlation (only for
					// DependenciesPostProcessingLov, i.e. scripts, java classes and
					// fixed lists)
					if (lovProvDet instanceof DependenciesPostProcessingLov && selectedParameterValues != null && biParameterExecDependencies != null
							&& biParameterExecDependencies.size() > 0) { // && contest != null && !contest.equals(MASSIVE_EXPORT)
						rows = ((DependenciesPostProcessingLov) lovProvDet).processDependencies(rows, selectedParameterValues, biParameterExecDependencies);
					}
					// END filtering for correlation




					if (lovProvDet.getLovType() != null && lovProvDet.getLovType().contains("tree")) {
						JSONArray valuesJSONArray = metaUtils.getChildrenForTreeLov(lovProvDet, rows, treeLovNodeLevel, treeLovNodeValue);
						result = metaUtils.buildJsonResult("OK", "", null, valuesJSONArray, biparameterId).toString();
					} else {
						valuesJSON = metaUtils.buildJSONForLOV(lovProvDet, rows, start, limit);
						result = metaUtils.buildJsonResult("OK", "", valuesJSON, null, biparameterId).toString();
					}

				}
			} catch (EMFUserError e1) {
				// result = buildJsonResult("KO", e1.getMessage(), null,null).toString();
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get document Execution Parameter EMFUserError", e1);
			}

		} catch (IOException e2) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get document Execution Parameter IOException", e2);
		} catch (JSONException e2) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get document Execution Parameter JSONException", e2);
		}

		// return Response.ok(resultAsMap).build();
		return result;
	}

	/*
	 * DATE RANGE
	 */

	private JSONObject manageDataRange(BIObjectParameter biObjectParameter, String executionRole, HttpServletRequest req)
			throws EMFUserError, SerializationException, JSONException, IOException {
		Integer parID = biObjectParameter.getParID();
		Assert.assertNotNull(parID, "parID");
		ParameterUse param = DAOFactory.getParameterUseDAO().loadByParameterIdandRole(parID, executionRole);
		String options = param.getOptions();
		Assert.assertNotNull(options, "options");

		JSONArray dateRangeValuesDataJSON = getDateRangeValuesDataJSON(options, req);
		int dataRangeOptionsSize = getDataRangeOptionsSize(options);
		JSONObject valuesJSON = (JSONObject) JSONStoreFeedTransformer.getInstance().transform(dateRangeValuesDataJSON, VALUE_FIELD.toUpperCase(),
				LABEL_FIELD.toUpperCase(), DESCRIPTION_FIELD.toUpperCase(), VISIBLE_COLUMNS, dataRangeOptionsSize);

		return valuesJSON;

	}

	private static int getDataRangeOptionsSize(String options) throws JSONException {
		JSONObject json = new JSONObject(options);
		JSONArray res = json.getJSONArray(DATE_RANGE_OPTIONS_KEY);
		return res.length();
	}

	private JSONArray getDateRangeValuesDataJSON(String optionsJson, HttpServletRequest req) throws JSONException {
		JSONObject json = new JSONObject(optionsJson);
		JSONArray options = json.getJSONArray(DATE_RANGE_OPTIONS_KEY);
		JSONArray res = new JSONArray();
		for (int i = 0; i < options.length(); i++) {
			JSONObject opt = new JSONObject();
			JSONObject optJson = (JSONObject) options.get(i);
			String type = (String) optJson.get(DATE_RANGE_TYPE_JSON);
			String typeDesc = getLocalizedMessage("SBIDev.paramUse." + type, req);
			String quantity = (String) optJson.get(DATE_RANGE_QUANTITY_JSON);
			String value = type + "_" + quantity;
			String label = quantity + " " + typeDesc;
			opt.put(VALUE_FIELD, value);
			opt.put(LABEL_FIELD, label);
			opt.put(DESCRIPTION_FIELD, label);
			res.put(opt);
		}
		return res;
	}

	private String getLocalizedMessage(String code, HttpServletRequest req) {
		return MessageBuilderFactory.getMessageBuilder().getMessage(code, req);
	}

}
