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
package it.eng.spagobi.analiticalmodel.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentDriverRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DriversValidationAPI;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.DependenciesPostProcessingLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.JSONStoreFeedTransformer;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.catalogue.metadata.IDrivableBIResource;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheInterface;
import it.eng.spagobi.utilities.cache.ParameterCache;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class DocumentExecutionUtils {
	public static final String SELECTION_TYPE_TREE = "TREE";
	public static final String SELECTION_TYPE_LOOKUP = "LOOKUP";
	public static final String PARAMETERS = "PARAMETERS";
	public static final String SERVICE_NAME = "GET_URL_FOR_EXECUTION_ACTION";
	public static String NODE_ID_SEPARATOR = "___SEPA__";
	public static String MODE_SIMPLE = "simple";
	public static String MODE_COMPLETE = "complete";
	public static String START = "start";
	public static String LIMIT = "limit";
	public static String MASSIVE_EXPORT = "massiveExport";
	public static String DEFAULT_VALUES = "defaultValues";
	public static String DEFAULT_VALUES_METADATA = "defaultValuesMetadata";
	public static String DESCRIPTION_COLUMN_NAME_METADATA = "descriptionColumnNameMetadata";
	public static String VALUE_COLUMN_NAME_METADATA = "valueColumnNameMetadata";

	public static transient Logger logger = Logger.getLogger(DocumentExecutionUtils.class);

	public static ILovDetail getLovDetail(BIObjectParameter parameter) {
		Parameter par = parameter.getParameter();
		ModalitiesValue lov = par.getModalityValue();
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = null;
		try {
			lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get lov detail associated to input BIObjectParameter", e);
		}
		return lovProvDet;
	}

	public static List<DocumentDriverRuntime> getParameters(BIObject document, String executionRole, Locale locale, String modality, DocumentRuntime dum) {
		List<DocumentDriverRuntime> toReturn = getParameters(document, executionRole, locale, modality, null, true, dum);
		return toReturn;
	}

	public static List<DocumentDriverRuntime> getParameters(BIObject document, String executionRole, Locale locale, String modality, List<String> parsFromCross,
			boolean loadAdmissible, DocumentRuntime dum) {
		Monitor monitor = MonitorFactory.start("Knowage.DocumentExecutionUtils.getParameters");
		List<DocumentDriverRuntime> parametersForExecution = null;
		try {
			parametersForExecution = new ArrayList<DocumentDriverRuntime>();
			List<BIObjectParameter> parameters = document.getDrivers();
			if (parameters != null && parameters.size() > 0) {
				Iterator<BIObjectParameter> it = parameters.iterator();
				while (it.hasNext()) {
					BIObjectParameter parameter = it.next();

					// check if coming from cross
					boolean comingFromCross = false;
					if (parsFromCross != null && parsFromCross.contains(parameter.getParameterUrlName())) {
						comingFromCross = true;
					}

					parametersForExecution
							.add(new DocumentDriverRuntime(parameter, executionRole, locale, document, comingFromCross, loadAdmissible, dum, parameters));
				}
			}
		} finally {
			monitor.stop();
		}
		return parametersForExecution;
	}

	public static JSONObject handleNormalExecution(UserProfile profile, BIObject obj, HttpServletRequest req, String env, String role, String modality,
			String parametersJson, Locale locale) { // isFromCross,

		JSONObject response = new JSONObject();
		HashMap<String, String> logParam = new HashMap<String, String>();
		logParam.put("NAME", obj.getName());
		logParam.put("ENGINE", obj.getEngine().getName());
		logParam.put("PARAMS", parametersJson); // this.getAttributeAsString(PARAMETERS)
		DocumentRuntime dum = new DocumentRuntime(profile, locale);
		DriversValidationAPI validation = new DriversValidationAPI(profile, locale);
		try {
			List errors = null;
			JSONObject executionInstanceJSON = null;
			try {
				executionInstanceJSON = new JSONObject(parametersJson);
			} catch (JSONException e2) {
				logger.debug("Error in handleNormalExecution", e2);
			}
			dum.refreshParametersValues(executionInstanceJSON, false, obj);
			try {
				errors = validation.getParametersErrors(obj, role, dum);
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot evaluate errors on parameters validation", e);
			}

			// ERRORS
			// if (errors != null && errors.size() > 0) {
			// there are errors on parameters validation, send errors' descriptions to the client
			JSONArray errorsArray = new JSONArray();
			Iterator errorsIt = errors.iterator();
			while (errorsIt.hasNext()) {
				EMFUserError error = (EMFUserError) errorsIt.next();
				errorsArray.put(error.getDescription());
			}
			try {
				response.put("errors", errorsArray);
			} catch (JSONException e) {
				try {
					AuditLogUtilities.updateAudit(req, profile, "DOCUMENT.GET_URL", logParam, "ERR");
				} catch (Exception e1) {
					logger.debug("Error in handleNormalExecution", e1);
				}
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize errors to the client", e);
			}
			// }else {
			// URL
			// there are no errors, we can proceed, so calculate the execution url and send it back to the client
			String url = dum.getExecutionUrl(obj, modality, role);
			// url += "&isFromCross=" + (isFromCross == true ? "true" : "false");
			// adds information about the environment
			if (env == null) {
				env = "DOCBROWSER";
			}
			url += "&SBI_ENVIRONMENT=" + env;
			try {
				response.put("url", url);
			} catch (JSONException e) {
				try {
					AuditLogUtilities.updateAudit(req, profile, "DOCUMENT.GET_URL", logParam, "KO");
				} catch (Exception e1) {
					logger.debug("Error in handleNormalExecution", e1);
				}
				try {
					AuditLogUtilities.updateAudit(req, profile, "DOCUMENT.GET_URL", logParam, "ERR");
				} catch (Exception e1) {
					logger.debug("Error in handleNormalExecution", e1);
				}
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize the url [" + url + "] to the client", e);
			}

			AuditLogUtilities.updateAudit(req, profile, "DOCUMENT.GET_URL", logParam, "OK");
		} catch (Exception e) {
			logger.debug("Error in handleNormalExecution", e);
		}
		return response;
	}

	public static String handleNormalExecutionUrl(UserProfile profile, BIObject obj, HttpServletRequest req, String env, String role, String modality,
			JSONObject parametersJson, Locale locale) { // isFromCross,
		Monitor handleNormalExecutionUrlMonitor = MonitorFactory.start("Knowage.DocumentExecutionResource.handleNormalExecutionUrl");

		HashMap<String, String> logParam = new HashMap<String, String>();
		logParam.put("NAME", obj.getName());
		logParam.put("ENGINE", obj.getEngine().getName());
		logParam.put("PARAMS", parametersJson.toString()); // this.getAttributeAsString(PARAMETERS)
		DocumentRuntime documentUrlManager = new DocumentRuntime(profile, locale);
		String url = "";
		try {

			documentUrlManager.refreshParametersValues(parametersJson, false, obj);

			// URL
			// there are no errors, we can proceed, so calculate the execution url and send it back to the client
			url = documentUrlManager.getExecutionUrl(obj, modality, role);
			// url += "&isFromCross=" + (isFromCross == true ? "true" : "false");
			// adds information about the environment
			if (env == null) {
				env = "DOCBROWSER";
			}
			url += "&SBI_ENVIRONMENT=" + env;

			AuditLogUtilities.updateAudit(req, profile, "DOCUMENT.GET_URL", logParam, "OK");
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, e.getMessage());
		} finally {
			handleNormalExecutionUrlMonitor.stop();
		}
		return url;
	}

	public static <T extends IDrivableBIResource<? extends AbstractDriver>> List handleNormalExecutionError(UserProfile profile, T obj, HttpServletRequest req, String env, String role, String modality,
			JSONObject parametersJson, Locale locale) { // isFromCross,
		Monitor handleNormalExecutionErrorMonitor = MonitorFactory.start("Knowage.DocumentExecutionResource.handleNormalExecutionError");

		DocumentRuntime dum = new DocumentRuntime(profile, locale);
		DriversValidationAPI validation = new DriversValidationAPI(profile, locale);
		List errors = null;
		try {
			errors = validation.getParametersErrors(obj, role, dum);
		} catch (Exception e) {
			logger.debug("Error in handleNormalExecutionError", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Cannot evaluate errors on parameters validation", e);

		} finally {
			handleNormalExecutionErrorMonitor.stop();
		}
		return errors;
	}

	// public static ArrayList<HashMap<String, Object>> getLovDefaultValues(String executionRole, BIObject biObject, BIObjectParameter objParameter,
	public static HashMap<String, Object> getLovDefaultValues(String executionRole, BIObject biObject, BIObjectParameter objParameter, HttpServletRequest req) {

		return getLovDefaultValues(executionRole, biObject, objParameter, null, 0, null, req);
	}

	/**
	 * @deprecated Replaced by {@link #getLovDefaultValues(String, BIObject, BIObjectParameter, JSONObject, Integer, String, Locale)}
	 */
	@Deprecated
	public static HashMap<String, Object> getLovDefaultValues(String executionRole, BIObject biObject, BIObjectParameter objParameter, JSONObject requestVal,
			Integer treeLovNodeLevel, String treeLovNodeValue, HttpServletRequest req) {

		ArrayList<HashMap<String, Object>> defaultValues = new ArrayList<HashMap<String, Object>>();
		String lovResult = null;
		ILovDetail lovProvDet = null;
		List rows = null;
		HashMap<String, Object> result = new HashMap<String, Object>();

		List<ObjParuse> biParameterExecDependencies = null;
		try {
			IEngUserProfile profile = UserProfileManager.getProfile();
			DocumentRuntime dum = new DocumentRuntime(profile, req.getLocale());

			JSONObject selectedParameterValuesJSON;
			Map selectedParameterValues = null;

			String mode = (requestVal != null && requestVal.opt("mode") != null) ? (String) requestVal.opt("mode") : null;
			String contest = (requestVal != null && requestVal.opt("contest") != null) ? (String) requestVal.opt("contest") : null;

			if (requestVal != null && requestVal.opt("PARAMETERS") != null) {
				selectedParameterValuesJSON = (JSONObject) requestVal.opt("PARAMETERS");

				if (selectedParameterValuesJSON != null) {
					dum.refreshParametersValues(selectedParameterValuesJSON, false, biObject);
				}

				if (selectedParameterValuesJSON != null) {
					try {
						selectedParameterValues = new HashMap();
						Iterator it = selectedParameterValuesJSON.keys();
						while (it.hasNext()) {
							String key = (String) it.next();
							Object v = selectedParameterValuesJSON.get(key);
							if (v == JSONObject.NULL) {
								selectedParameterValues.put(key, null);
							} else if (v instanceof JSONArray) {
								JSONArray a = (JSONArray) v;
								String[] nv = new String[a.length()];
								for (int i = 0; i < a.length(); i++) {
									if (a.get(i) != null) {
										nv[i] = a.get(i).toString();
									} else {
										nv[i] = null;
									}
								}
								selectedParameterValues.put(key, nv);
							} else if (v instanceof String) {
								selectedParameterValues.put(key, v);
							} else if (v instanceof Integer) {
								selectedParameterValues.put(key, "" + v);
							} else if (v instanceof Double) {
								selectedParameterValues.put(key, "" + v);
							} else {
								Assert.assertUnreachable("Attribute [" + key + "] value [" + v
										+ "] of PARAMETERS is not of type JSONArray nor String. It is of type [" + v.getClass().getName() + "]");
							}
						}
					} catch (JSONException e) {
						throw new SpagoBIServiceException("parameter JSONObject is malformed", e);
					}
				}
			}

			lovProvDet = dum.getLovDetail(objParameter);

			biParameterExecDependencies = getBiObjectDependencies(executionRole, objParameter);

			lovResult = getLovResult(profile, lovProvDet, biParameterExecDependencies, biObject, req, true);
			Assert.assertNotNull(lovResult, "Impossible to get parameter's values");

			LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
			rows = lovResultHandler.getRows();

			JSONArray valuesJSONArray = new JSONArray();
			if (lovProvDet instanceof DependenciesPostProcessingLov && selectedParameterValues != null && biParameterExecDependencies != null
					&& biParameterExecDependencies.size() > 0 && !contest.equals(MASSIVE_EXPORT)) {
				rows = ((DependenciesPostProcessingLov) lovProvDet).processDependencies(rows, selectedParameterValues, biParameterExecDependencies);
			}

			if (lovProvDet.getLovType() != null && lovProvDet.getLovType().contains("tree")) {
				valuesJSONArray = getChildrenForTreeLov(lovProvDet, rows, mode, treeLovNodeLevel, treeLovNodeValue);
			} else {
				JSONObject valuesJSON = buildJSONForLOV(lovProvDet, rows, MODE_SIMPLE);
				valuesJSONArray = valuesJSON.getJSONArray("root");
			}

			List defaultValuesMetadata = lovProvDet.getVisibleColumnNames();
			result.put(DEFAULT_VALUES_METADATA, defaultValuesMetadata);
			result.put(DESCRIPTION_COLUMN_NAME_METADATA, lovProvDet.getDescriptionColumnName());
			result.put(VALUE_COLUMN_NAME_METADATA, lovProvDet.getValueColumnName());

			for (int i = 0; i < valuesJSONArray.length(); i++) {
				JSONObject item = valuesJSONArray.getJSONObject(i);
				if (item.length() > 0) {
					HashMap<String, Object> itemAsMap = new HashMap<String, Object>();

					for (int j = 0; j < defaultValuesMetadata.size(); j++) {
						String key = ((String) defaultValuesMetadata.get(j)).toUpperCase();

						if (item.has(key)) {
							itemAsMap.put(key, item.get(key));
						}
					}

					itemAsMap.put("value", item.get("value"));
					itemAsMap.put("label", item.has("label") ? item.get("label") : item.get("value"));
					if (item.has("id")) {
						itemAsMap.put("id", item.get("id"));
					}
					if (item.has("leaf")) {
						itemAsMap.put("leaf", item.get("leaf"));
					}
					itemAsMap.put("description", item.get("description"));
					itemAsMap.put("isEnabled", true);

					// CHECH VALID DEFAULT PARAM
					ArrayList<HashMap<String, Object>> defaultErrorValues = new ArrayList<HashMap<String, Object>>();
					boolean defaultParameterAlreadyExist = false;
					if (objParameter.getParameter() != null && objParameter.getParameter().getModalityValue() != null
							&& objParameter.getParameter().getModalityValue().getSelectionType() != null
							&& !objParameter.getParameter().getModalityValue().getSelectionType().equals("LOOKUP")) {

						for (HashMap<String, Object> defVal : defaultValues) {
							if (defVal.get("value").equals(item.get("value")) && !item.isNull("label")) {
								if (defVal.get("label").equals(item.get("label")) && defVal.get("description").equals(item.get("description"))) {
									defaultParameterAlreadyExist = true;
									break;
								} else {
									HashMap<String, Object> itemErrorMap = new HashMap<String, Object>();
									itemErrorMap.put("error", true);
									itemErrorMap.put("value", defVal.get("value"));
									itemErrorMap.put("labelAlreadyExist", defVal.get("label"));
									itemErrorMap.put("labelSameValue", item.get("label"));
									defaultErrorValues.add(itemErrorMap);
									// return defaultErrorValues;
									result.put(DEFAULT_VALUES, defaultErrorValues);
									return result;
								}
							}
						}
					}

					if (!defaultParameterAlreadyExist) {
						defaultValues.add(itemAsMap);
						result.put(DEFAULT_VALUES, defaultValues);
						// } else {
						// result.put(DEFAULT_VALUES, defaultErrorValues);
						// return result;
					}
				}
			}
			// return defaultValues;
			return result;

		} catch (Exception e) {
			throw new SpagoBIServiceException("Impossible to get parameter's values", e);
		}

	}

	/**
	 *
	 * @param executionRole
	 * @param biObject
	 * @param objParameter
	 * @param requestVal Something like:
	 *   <pre>
	 *   {
	 *     "label": "KNOWAGE-6401",
	 *     "role": "admin",
	 *     "parameterId": "KNOWAGE-6401-1-3",
	 *     "mode": "complete",
	 *     "treeLovNode": "Baking Goods___SEPA__1",
	 *     "PARAMETERS": {
	 *       "KNOWAGE-6401-FAM": "Food",
	 *       "KNOWAGE-6401-FAM_field_visible_description": "Food",
	 *       "KNOWAGE-6401-1-1": "Nuts",
	 *       "KNOWAGE-6401-1-1_field_visible_description": "Descrizione di Nuts",
	 *       "KNOWAGE-6401-1-2": [
	 *         "Nuts"
	 *       ],
	 *       "KNOWAGE-6401-1-2_field_visible_description": "Descrizione di Nuts",
	 *       "KNOWAGE-6401-1-3": [],
	 *       "KNOWAGE-6401-1-3_field_visible_description": ""
	 *     }
	 *   }
	 *   </pre>
	 * @param treeLovNodeLevel
	 * @param treeLovNodeValue
	 * @param locale
	 * @return
	 * @deprecated Where possible, prefer {@link #getLovDefaultValues(String, List, BIObjectParameter, JSONObject, Integer, String, Locale)}
	 */
	@Deprecated
	public static Map<String, Object> getLovDefaultValues(
			String executionRole,
			BIObject biObject,
			BIObjectParameter objParameter,
			JSONObject requestVal,
			Integer treeLovNodeLevel,
			String treeLovNodeValue,
			Locale locale) {
		List<BIObjectParameter> drivers = biObject.getDrivers();
		return getLovDefaultValues(executionRole, drivers, objParameter, requestVal, treeLovNodeLevel, treeLovNodeValue, locale);
	}

	public static Map<String, Object> getLovDefaultValues(String executionRole, List<? extends AbstractDriver> drivers, AbstractDriver objParameter,
			JSONObject requestVal, Integer treeLovNodeLevel, String treeLovNodeValue, Locale locale) {
		ArrayList<HashMap<String, Object>> defaultValues = new ArrayList<HashMap<String, Object>>();
		String lovResult = null;
		ILovDetail lovProvDet = null;
		List rows = null;
		HashMap<String, Object> result = new HashMap<String, Object>();

		List<ObjParuse> biParameterExecDependencies = null;
		try {
			IEngUserProfile profile = UserProfileManager.getProfile();
			DocumentRuntime dum = new DocumentRuntime(profile, locale);

			JSONArray selectedParameterValuesJSON;
			Map selectedParameterValues = null;

			String mode = (requestVal != null && requestVal.opt("mode") != null) ? (String) requestVal.opt("mode") : null;
			String contest = (requestVal != null && requestVal.opt("contest") != null) ? (String) requestVal.opt("contest") : null;

			if (requestVal != null && requestVal.opt("parameters") != null) {
				selectedParameterValuesJSON = (JSONArray) requestVal.opt("parameters");

				if (selectedParameterValuesJSON != null) {
					dum.refreshParametersValues(selectedParameterValuesJSON, false, drivers);
				}

				if (selectedParameterValuesJSON != null) {
					selectedParameterValues = createParameterValuesMap(selectedParameterValuesJSON);
				}
			}

			lovProvDet = dum.getLovDetail(objParameter);

			biParameterExecDependencies = getBiObjectDependencies(executionRole, objParameter);

			lovResult = getLovResult(profile, lovProvDet, biParameterExecDependencies, drivers, locale, true);
			Assert.assertNotNull(lovResult, "Impossible to get parameter's values");

			LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
			rows = lovResultHandler.getRows();

			JSONArray valuesJSONArray = new JSONArray();
			if (lovProvDet instanceof DependenciesPostProcessingLov && selectedParameterValues != null && biParameterExecDependencies != null
					&& biParameterExecDependencies.size() > 0 && !contest.equals(MASSIVE_EXPORT)) {
				rows = ((DependenciesPostProcessingLov) lovProvDet).processDependencies(rows, selectedParameterValues, biParameterExecDependencies);
			}

			if (lovProvDet.getLovType() != null && lovProvDet.getLovType().contains("tree")) {
				valuesJSONArray = getChildrenForTreeLov(lovProvDet, rows, mode, treeLovNodeLevel, treeLovNodeValue);
			} else {
				JSONObject valuesJSON = buildJSONForLOV(lovProvDet, rows, MODE_SIMPLE);
				valuesJSONArray = valuesJSON.getJSONArray("root");
			}

			List defaultValuesMetadata = lovProvDet.getVisibleColumnNames();
			result.put(DEFAULT_VALUES_METADATA, defaultValuesMetadata);
			result.put(DESCRIPTION_COLUMN_NAME_METADATA, lovProvDet.getDescriptionColumnName());
			result.put(VALUE_COLUMN_NAME_METADATA, lovProvDet.getValueColumnName());

			for (int i = 0; i < valuesJSONArray.length(); i++) {
				JSONObject item = valuesJSONArray.getJSONObject(i);
				if (item.length() > 0) {
					HashMap<String, Object> itemAsMap = new HashMap<String, Object>();

					for (int j = 0; j < defaultValuesMetadata.size(); j++) {
						String key = ((String) defaultValuesMetadata.get(j)).toUpperCase();

						if (item.has(key)) {
							itemAsMap.put(key, item.get(key));
						}
					}

					itemAsMap.put("data", item.get("value"));
					itemAsMap.put("label", item.get("description"));
					if (item.has("id")) {
						itemAsMap.put("id", item.get("id"));
					}
					if (item.has("leaf")) {
						itemAsMap.put("leaf", item.get("leaf"));
					} else {
						itemAsMap.put("leaf", false);
					}
					itemAsMap.put("isEnabled", true);

					// CHECH VALID DEFAULT PARAM
					ArrayList<HashMap<String, Object>> defaultErrorValues = new ArrayList<HashMap<String, Object>>();
					boolean defaultParameterAlreadyExist = false;
					if (objParameter.getParameter() != null && objParameter.getParameter().getModalityValue() != null
							&& objParameter.getParameter().getModalityValue().getSelectionType() != null
							&& !objParameter.getParameter().getModalityValue().getSelectionType().equals("LOOKUP")) {

						for (HashMap<String, Object> defVal : defaultValues) {
							if (defVal.get("data").equals(item.get("value")) && !item.isNull("label")) {
								if (defVal.get("label").equals(item.get("description"))) {
									defaultParameterAlreadyExist = true;
									break;
								} else {
									HashMap<String, Object> itemErrorMap = new HashMap<String, Object>();
									itemErrorMap.put("error", true);
									itemErrorMap.put("value", defVal.get("value"));
									itemErrorMap.put("labelAlreadyExist", defVal.get("label"));
									itemErrorMap.put("labelSameValue", item.get("label"));
									defaultErrorValues.add(itemErrorMap);
									result.put(DEFAULT_VALUES, defaultErrorValues);
									return result;
								}
							}
						}
					}

					if (!defaultParameterAlreadyExist) {
						defaultValues.add(itemAsMap);
						result.put(DEFAULT_VALUES, defaultValues);
					}
				}
			}
			return result;

		} catch (Exception e) {
			throw new SpagoBIServiceException("Impossible to get parameter's values", e);
		}
	}


	// Same method as GetParameterValuesForExecutionAction.getChildrenForTreeLov()
	private static JSONArray getChildrenForTreeLov(ILovDetail lovProvDet, List rows, String mode, Integer treeLovNodeLevel, String treeLovNodeValue) {

		String valueColumn;
		String descriptionColumn;
		boolean addNode;
		String treeLovNodeName = "";
		String treeLovParentNodeName = "";
		String treeLovNodeNameBen = "";

		try {

			if (treeLovNodeValue != null && treeLovNodeValue.equalsIgnoreCase("lovroot")) {// root node
				treeLovNodeName = lovProvDet.getTreeLevelsColumns().get(0).getFirst();
				treeLovNodeNameBen = lovProvDet.getTreeLevelsColumns().get(0).getSecond();
				treeLovParentNodeName = "lovroot";
				treeLovNodeLevel = -1;

				// treeLovNodeLevel-1 because the fake root node is the level 0
			} else if (lovProvDet.getTreeLevelsColumns().size() > treeLovNodeLevel + 1) {
				treeLovNodeName = lovProvDet.getTreeLevelsColumns().get(treeLovNodeLevel + 1).getFirst();
				treeLovParentNodeName = lovProvDet.getTreeLevelsColumns().get(treeLovNodeLevel).getFirst();
				treeLovNodeNameBen = lovProvDet.getTreeLevelsColumns().get(treeLovNodeLevel + 1).getSecond();
			}

			Set<JSONObject> valuesDataJSON = new LinkedHashSet<JSONObject>();

			valueColumn = lovProvDet.getValueColumnName();
			descriptionColumn = lovProvDet.getDescriptionColumnName();

			for (int q = 0; q < rows.size(); q++) {
				SourceBean row = (SourceBean) rows.get(q);
				JSONObject valueJSON = null;
				addNode = false;
				List columns = row.getContainedAttributes();
				valueJSON = new JSONObject();
				boolean notNullNode = false; // if the row does not contain the value atribute we don't add the node
				for (int i = 0; i < columns.size(); i++) {
					SourceBeanAttribute attribute = (SourceBeanAttribute) columns.get(i);
					if ((treeLovParentNodeName == "lovroot") || (attribute.getKey().equalsIgnoreCase(treeLovParentNodeName)
							&& (attribute.getValue().toString()).equalsIgnoreCase(treeLovNodeValue))) {
						addNode = true;
					}

					// its a leaf so we take the value and description defined in the lov definition
					if (lovProvDet.getTreeLevelsColumns().size() == treeLovNodeLevel + 2) {
						if (attribute.getKey().equalsIgnoreCase(descriptionColumn)) {// its the column of the description
							valueJSON.put("description", attribute.getValue());
							notNullNode = true;
						}
						if (attribute.getKey().equalsIgnoreCase(valueColumn)) {// its the column of the value
							valueJSON.put("value", attribute.getValue());
							valueJSON.put("id", attribute.getValue() + NODE_ID_SEPARATOR + (treeLovNodeLevel + 1));
							notNullNode = true;
						}
						valueJSON.put("leaf", true);
					}

					else if (attribute.getKey().equalsIgnoreCase(treeLovNodeName)) {
						valueJSON = new JSONObject();
						valueJSON.put("value", attribute.getValue());
						valueJSON.put("id", attribute.getValue() + NODE_ID_SEPARATOR + (treeLovNodeLevel + 1));
						// SETTING DESCRIPTION NODE
						for (int s = 0; s < columns.size(); s++) {
							SourceBeanAttribute attributes = (SourceBeanAttribute) columns.get(s);
							if (attributes.getKey().equalsIgnoreCase(treeLovNodeNameBen)) {
								valueJSON.put("description", attributes.getValue());
							}
						}
						notNullNode = true;
					}

					// else if (attribute.getKey().equalsIgnoreCase(treeLovNodeName)) {
					// valueJSON = new JSONObject();
					// valueJSON.put("value", attribute.getValue());
					// valueJSON.put("id", attribute.getValue() + NODE_ID_SEPARATOR + (treeLovNodeLevel + 1));
					// valueJSON.put("description", attribute.getValue());
					// notNullNode = true;
					// }

				}

				if (addNode && notNullNode) {
					valuesDataJSON.add(valueJSON);
				}
			}

			JSONArray valuesDataJSONArray = new JSONArray();

			for (Iterator iterator = valuesDataJSON.iterator(); iterator.hasNext();) {
				JSONObject jsonObject = (JSONObject) iterator.next();
				valuesDataJSONArray.put(jsonObject);
			}

			return valuesDataJSONArray;
		} catch (Exception e) {
			throw new SpagoBIServiceException("Impossible to serialize response", e);
		}
	}

	public static List<ObjParuse> getBiObjectDependencies(String executionRole, AbstractDriver biobjParameter) {
		List<ObjParuse> biParameterExecDependencies = new ArrayList<ObjParuse>();
		try {
			IParameterUseDAO parusedao = DAOFactory.getParameterUseDAO();
			ParameterUse biParameterExecModality = parusedao.loadByParameterIdandRole(biobjParameter.getParID(), executionRole);
			IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
			biParameterExecDependencies.addAll(objParuseDAO.loadObjParuse(biobjParameter.getId(), biParameterExecModality.getUseID()));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get dependencies", e);
		}
		return biParameterExecDependencies;
	}

	/**
	 * @deprecated Replaced by {@link #getLovResult(IEngUserProfile, ILovDetail, List, BIObject, Locale, boolean)}
	 */
	@Deprecated
	public static String getLovResult(IEngUserProfile profile, ILovDetail lovDefinition, List<ObjParuse> dependencies, BIObject biObject,
			HttpServletRequest req, boolean retrieveIfNotcached) throws Exception {

		String lovResult = null;

		if (lovDefinition instanceof QueryDetail) {
			// queries are cached
			String cacheKey = getCacheKey(profile, lovDefinition, dependencies, biObject);

			CacheInterface cache = ParameterCache.getCache();

			if (cache.contains(cacheKey)) {
				// lov provider is present, so read the DATA in cache
				lovResult = (String) cache.get(cacheKey);
			} else if (retrieveIfNotcached) {
				lovResult = lovDefinition.getLovResult(profile, dependencies, biObject.getDrivers(), req.getLocale());
				// insert the data in cache
				if (lovResult != null)
					cache.put(cacheKey, lovResult);
			}
		} else {
			// scrips, fixed list and java classes are not cached, and returned without considering retrieveIfNotcached input
			lovResult = lovDefinition.getLovResult(profile, dependencies, biObject.getDrivers(), req.getLocale());
		}

		return lovResult;
	}

	/**
	 * @deprecated Where possible, prefer #getLovResult
	 */
	@Deprecated
	public static String getLovResult(IEngUserProfile profile, ILovDetail lovDefinition, List<ObjParuse> dependencies, BIObject biObject,
			Locale locale, boolean retrieveIfNotcached) throws Exception {
		List<BIObjectParameter> drivers = biObject.getDrivers();
		return getLovResult(profile, lovDefinition, dependencies, drivers, locale, retrieveIfNotcached);
	}

	private static String getLovResult(IEngUserProfile profile, ILovDetail lovDefinition, List<ObjParuse> dependencies, List<? extends AbstractDriver> drivers,
			Locale locale, boolean retrieveIfNotcached) throws Exception {
		String lovResult = null;

		if (lovDefinition instanceof QueryDetail) {
			// queries are cached
			String cacheKey = getCacheKey(profile, lovDefinition, dependencies, drivers);

			CacheInterface cache = ParameterCache.getCache();

			if (cache.contains(cacheKey)) {
				// lov provider is present, so read the DATA in cache
				lovResult = (String) cache.get(cacheKey);
			} else if (retrieveIfNotcached) {
				lovResult = lovDefinition.getLovResult(profile, dependencies, drivers, locale);
				// insert the data in cache
				if (lovResult != null)
					cache.put(cacheKey, lovResult);
			}
		} else {
			// scrips, fixed list and java classes are not cached, and returned without considering retrieveIfNotcached input
			lovResult = lovDefinition.getLovResult(profile, dependencies, drivers, locale);
		}

		return lovResult;
	}

	/**
	 * This method finds out the cache to be used for lov's result cache. This key is composed mainly by the user identifier and the lov definition. Note that,
	 * in case when the lov is a query and there is correlation, the executed statement if different from the original query (since correlation expression is
	 * injected inside SQL query using in-line view construct), therefore we should consider the modified query.
	 *
	 * @param profile
	 *            The user profile
	 * @param lovDefinition
	 *            The lov original definition
	 * @param dependencies
	 *            The dependencies to be considered (if any)
	 * @param biObject
	 *            The document object
	 * @return The key to be used in cache
	 * @throws Exception
	 * @deprecated Where possible, prefer {@link #getCacheKey(IEngUserProfile, ILovDetail, List, List)}
	 */
	@Deprecated
	private static String getCacheKey(IEngUserProfile profile, ILovDetail lovDefinition, List<ObjParuse> dependencies, BIObject biObject) throws Exception {
		List<BIObjectParameter> drivers = biObject.getDrivers();
		return getCacheKey(profile, lovDefinition, dependencies, drivers);
	}

	public static String getCacheKey(IEngUserProfile profile, ILovDetail lovDefinition, List<ObjParuse> dependencies, List<? extends AbstractDriver> drivers)
			throws Exception {
		String toReturn = null;
		String userID = (String) ((UserProfile) profile).getUserId();
		if (lovDefinition instanceof QueryDetail) {
			QueryDetail queryDetail = (QueryDetail) lovDefinition;
			QueryDetail clone = queryDetail.clone();
			// clone.setQueryDefinition(queryDetail.getWrappedStatement(dependencies, biObject.getDrivers()));
			// toReturn = userID + ";" + clone.toXML();

			Map<String, String> parameters = queryDetail.getParametersNameToValueMap(drivers);
			String statement = queryDetail.getWrappedStatement(dependencies, drivers);
			statement = StringUtilities.substituteProfileAttributesInString(statement, profile);
			if (parameters != null && !parameters.isEmpty()) {
				Map<String, String> types = queryDetail.getParametersNameToTypeMap(drivers);
				statement = StringUtilities.substituteParametersInString(statement, parameters, types, false);
			}
			clone.setQueryDefinition(statement);
			toReturn = userID + ";" + clone.toXML();

		} else {
			toReturn = userID + ";" + lovDefinition.toXML();
		}
		return toReturn;
	}

	public static JSONObject buildJSONForLOV(ILovDetail lovProvDet, List rows, String mode) {
		String valueColumn;
		String descriptionColumn;
		JSONObject valuesJSON;
		// Integer start;
		// Integer limit;
		String displayColumn;

		// START building JSON object to be returned
		try {
			JSONArray valuesDataJSON = new JSONArray();

			valueColumn = lovProvDet.getValueColumnName();
			displayColumn = lovProvDet.getDescriptionColumnName();
			descriptionColumn = displayColumn;

			// start = getAttributeAsInteger(START);
			// limit = getAttributeAsInteger(LIMIT);

			int lb = 0;
			int ub = rows.size();

			for (int q = lb; q < ub; q++) {
				SourceBean row = (SourceBean) rows.get(q);
				JSONObject valueJSON = new JSONObject();

				if (MODE_COMPLETE.equalsIgnoreCase(mode) || MODE_SIMPLE.equalsIgnoreCase(mode)) {
					List columns = row.getContainedAttributes();
					for (int i = 0; i < columns.size(); i++) {
						SourceBeanAttribute attribute = (SourceBeanAttribute) columns.get(i);
						valueJSON.put(attribute.getKey().toUpperCase(), attribute.getValue());
					}
					// } else {
					// String value = (String) row.getAttribute(valueColumn);
					// String description = (String) row.getAttribute(descriptionColumn);
					// valueJSON.put("value", value);
					// valueJSON.put("label", description);
					// valueJSON.put("description", description);
				}

				String value = (String) row.getAttribute(valueColumn);
				String description = (String) row.getAttribute(descriptionColumn);
				valueJSON.put("value", value);
				valueJSON.put("label", description);
				valueJSON.put("description", description);

				valuesDataJSON.put(valueJSON);
			}

			String[] visiblecolumns;

			if (MODE_COMPLETE.equalsIgnoreCase(mode) || MODE_SIMPLE.equalsIgnoreCase(mode)) {
				visiblecolumns = (String[]) lovProvDet.getVisibleColumnNames().toArray(new String[0]);
				for (int j = 0; j < visiblecolumns.length; j++) {
					visiblecolumns[j] = visiblecolumns[j].toUpperCase();
				}
			} else {

				valueColumn = "value";
				displayColumn = "label";
				descriptionColumn = "description";

				visiblecolumns = new String[] { "value", "label", "description" };
			}

			valuesJSON = (JSONObject) JSONStoreFeedTransformer.getInstance().transform(valuesDataJSON, valueColumn.toUpperCase(), displayColumn.toUpperCase(),
					descriptionColumn.toUpperCase(), visiblecolumns, new Integer(rows.size()));
			return valuesJSON;
		} catch (Exception e) {
			throw new SpagoBIServiceException("Impossible to serialize response", e);
		}
	}

	/**
	 * @param paramsArray Something like:
	 *   <pre>
	 *     [
	 *       {
	 *         "name": "KNOWAGE-6401-1-1",
	 *         "value": "Ice Cream",
	 *         "description": "Descrizione di Ice Cream"
	 *       },
	 *       {
	 *         "name": "KNOWAGE-6401-1-3",
	 *         "value": [],
	 *         "description": ""
	 *       },
	 *       {
	 *         "name": "KNOWAGE-6401-1-2",
	 *         "value": [ "Spices" ],
	 *         "description": "Descrizione di Spices"
	 *       },
	 *       {
	 *         "name": "KNOWAGE-6401-1-4",
	 *         "value": "",
	 *         "description": ""
	 *       }
	 *     ]
	 *   </pre>
	 * @return
	 */
	public static Map<String, Object> createParameterValuesMap(JSONArray paramsArray) {
		Map<String, Object> ret = null;

		int length = paramsArray.length();
		if (length > 0) {
			try {
				ret = new HashMap<>();

				for (int j=0; j<length; j++) {
					JSONObject jsonObject = (JSONObject) paramsArray.get(j);

					String key = (String) jsonObject.get("label");
					Object v = jsonObject.get("value");

					if (v == JSONObject.NULL) {
						ret.put(key, null);
					} else if (v instanceof JSONArray) {
						JSONArray a = (JSONArray) v;
						String[] nv = new String[a.length()];
						for (int i = 0; i < a.length(); i++) {
							if (a.get(i) != null) {
								nv[i] = a.get(i).toString();
							} else {
								nv[i] = null;
							}
						}
						ret.put(key, nv);
					} else if (v instanceof String) {
						ret.put(key, v);
					} else if (v instanceof Integer) {
						ret.put(key, "" + v);
					} else if (v instanceof Double) {
						ret.put(key, "" + v);
					} else {
						Assert.assertUnreachable("Attribute [" + key + "] value [" + v
								+ "] of PARAMETERS is not of type JSONArray nor String. It is of type [" + v.getClass().getName() + "]");
					}
				}
			} catch (JSONException e) {
				throw new SpagoBIServiceException("parameter JSONObject is malformed", e);
			}
		}

		return ret;
	}

}
