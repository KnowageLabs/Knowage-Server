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
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelDriverRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DriversValidationAPI;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.DependenciesPostProcessingLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.JSONStoreFeedTransformer;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheInterface;
import it.eng.spagobi.utilities.cache.ParameterCache;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class BusinessModelOpenUtils {
	public static String MODE_COMPLETE = "complete";
	public static String NODE_ID_SEPARATOR = "___SEPA__";
	public static String MODE_SIMPLE = "simple";
	public static String MASSIVE_EXPORT = "massiveExport";
	public static String DEFAULT_VALUES = "defaultValues";
	public static String DEFAULT_VALUES_METADATA = "defaultValuesMetadata";
	public static String DESCRIPTION_COLUMN_NAME_METADATA = "descriptionColumnNameMetadata";
	public static String VALUE_COLUMN_NAME_METADATA = "valueColumnNameMetadata";
	public static final String SERVICE_NAME = "GET_URL_FOR_EXECUTION_ACTION";

	public static transient Logger logger = Logger.getLogger(BusinessModelOpenUtils.class);

	public static List<BusinessModelDriverRuntime> getParameters(MetaModel businessModel, String executionRole, Locale locale, String modality,
			BusinessModelRuntime dum) {
		List<BusinessModelDriverRuntime> toReturn = getParameters(businessModel, executionRole, locale, modality, true, dum);
		return toReturn;
	}

	public static List<BusinessModelDriverRuntime> getParameters(MetaModel businessModel, String executionRole, Locale locale, String modality,
			boolean loadAdmissible, BusinessModelRuntime dum) {
		Monitor monitor = MonitorFactory.start("Knowage.DocumentExecutionUtils.getParameters");
		List<BusinessModelDriverRuntime> parametersForExecution = null;
		try {
			parametersForExecution = new ArrayList<BusinessModelDriverRuntime>();
			List<BIMetaModelParameter> parameters = businessModel.getDrivers();
			if (parameters != null && parameters.size() > 0) {
				Iterator<BIMetaModelParameter> it = parameters.iterator();
				while (it.hasNext()) {
					BIMetaModelParameter parameter = it.next();

					// check if coming from cross
					// boolean comingFromCross = false;
					// if (parsFromCross != null && parsFromCross.contains(parameter.getParameterUrlName())) {
					// comingFromCross = true;
					// }

					parametersForExecution.add(new BusinessModelDriverRuntime(parameter, executionRole, locale, businessModel, dum, parameters));
				}
			}
		} finally {
			monitor.stop();
		}
		return parametersForExecution;
	}

	// public static ArrayList<HashMap<String, Object>> getLovDefaultValues(
	public static HashMap<String, Object> getLovDefaultValues(String executionRole, MetaModel businessModel, BIMetaModelParameter driver, JSONObject requestVal,
			Integer treeLovNodeLevel, String treeLovNodeValue, HttpServletRequest req) {

		ArrayList<HashMap<String, Object>> defaultValues = new ArrayList<HashMap<String, Object>>();
		String lovResult = null;
		ILovDetail lovProvDet = null;
		List rows = null;
		HashMap<String, Object> result = new HashMap<String, Object>();

		List<MetaModelParuse> metaModelOpenDependencies = null;
		try {
			IEngUserProfile profile = UserProfileManager.getProfile();
			BusinessModelRuntime dum = new BusinessModelRuntime(profile, req.getLocale());

			JSONObject selectedParameterValuesJSON;
			Map selectedParameterValues = null;

			String mode = (requestVal != null && requestVal.opt("mode") != null) ? (String) requestVal.opt("mode") : null;
			String contest = (requestVal != null && requestVal.opt("contest") != null) ? (String) requestVal.opt("contest") : null;

			if (requestVal != null && requestVal.opt("PARAMETERS") != null) {
				selectedParameterValuesJSON = (JSONObject) requestVal.opt("PARAMETERS");

				if (selectedParameterValuesJSON != null) {
					dum.refreshParametersValues(selectedParameterValuesJSON, false, businessModel);
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

			lovProvDet = dum.getLovDetail(driver);

			metaModelOpenDependencies = getMetaModelDependencies(executionRole, driver);

			lovResult = getLovResult(profile, lovProvDet, metaModelOpenDependencies, businessModel, req, true);
			Assert.assertNotNull(lovResult, "Impossible to get parameter's values");

			LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
			rows = lovResultHandler.getRows();

			JSONArray valuesJSONArray = new JSONArray();
			if (lovProvDet instanceof DependenciesPostProcessingLov && selectedParameterValues != null && metaModelOpenDependencies != null
					&& metaModelOpenDependencies.size() > 0 && !contest.equals(MASSIVE_EXPORT)) {
				rows = ((DependenciesPostProcessingLov) lovProvDet).processDependencies(rows, selectedParameterValues, metaModelOpenDependencies);
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
					if (driver.getParameter() != null && driver.getParameter().getModalityValue() != null
							&& driver.getParameter().getModalityValue().getSelectionType() != null
							&& !driver.getParameter().getModalityValue().getSelectionType().equals("LOOKUP")) {

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

	public static List<MetaModelParuse> getMetaModelDependencies(String executionRole, BIMetaModelParameter metaModelParameter) {
		List<MetaModelParuse> metaModelOpenDependencies = new ArrayList<MetaModelParuse>();
		try {
			IParameterUseDAO parusedao = DAOFactory.getParameterUseDAO();
			ParameterUse parameterOpenModality = parusedao.loadByParameterIdandRole(metaModelParameter.getParID(), executionRole);
			IMetaModelParuseDAO metaModelParuseDAO = DAOFactory.getMetaModelParuseDao();
			metaModelOpenDependencies.addAll(metaModelParuseDAO.loadMetaModelParuse(metaModelParameter.getId(), parameterOpenModality.getUseID()));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get dependencies", e);
		}
		return metaModelOpenDependencies;
	}

	public static String getLovResult(IEngUserProfile profile, ILovDetail lovDefinition, List<MetaModelParuse> dependencies, MetaModel businessModel,
			HttpServletRequest req, boolean retrieveIfNotcached) throws Exception {

		String lovResult = null;

		if (lovDefinition instanceof QueryDetail) {
			// queries are cached
			String cacheKey = getCacheKey(profile, lovDefinition, dependencies, businessModel);

			CacheInterface cache = ParameterCache.getCache();

			if (cache.contains(cacheKey)) {
				// lov provider is present, so read the DATA in cache
				lovResult = (String) cache.get(cacheKey);
			} else if (retrieveIfNotcached) {
				lovResult = lovDefinition.getLovResult(profile, dependencies, businessModel.getDrivers(), req.getLocale());
				// insert the data in cache
				if (lovResult != null)
					cache.put(cacheKey, lovResult);
			}
		} else {
			// scrips, fixed list and java classes are not cached, and returned without considering retrieveIfNotcached input
			lovResult = lovDefinition.getLovResult(profile, dependencies, businessModel.getDrivers(), req.getLocale());
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
	 */
	private static String getCacheKey(IEngUserProfile profile, ILovDetail lovDefinition, List<MetaModelParuse> dependencies, MetaModel businessModel)
			throws Exception {
		String toReturn = null;
		String userID = (String) ((UserProfile) profile).getUserId();
		if (lovDefinition instanceof QueryDetail) {
			QueryDetail queryDetail = (QueryDetail) lovDefinition;
			QueryDetail clone = queryDetail.clone();
			// clone.setQueryDefinition(queryDetail.getWrappedStatement(dependencies, biObject.getDrivers()));
			// toReturn = userID + ";" + clone.toXML();

			Map<String, String> parameters = queryDetail.getParametersNameToValueMap(businessModel.getDrivers());
			String statement = queryDetail.getWrappedStatement(dependencies, businessModel.getDrivers());
			statement = StringUtilities.substituteProfileAttributesInString(statement, profile);
			if (parameters != null && !parameters.isEmpty()) {
				Map<String, String> types = queryDetail.getParametersNameToTypeMap(businessModel.getDrivers());
				statement = StringUtilities.substituteParametersInString(statement, parameters, types, false);
			}
			clone.setQueryDefinition(statement);
			toReturn = userID + ";" + clone.toXML();

		} else {
			toReturn = userID + ";" + lovDefinition.toXML();
		}
		return toReturn;
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

	public static List handleNormalExecutionError(UserProfile profile, MetaModel businessModel, HttpServletRequest req, String env, String role,
			String modality, JSONObject parametersJson, Locale locale) { // isFromCross,
		Monitor handleNormalExecutionErrorMonitor = MonitorFactory.start("Knowage.BusinessModelOpenUtils.handleNormalExecutionError");

		HashMap<String, String> logParam = new HashMap<String, String>();
		logParam.put("NAME", businessModel.getName());
		logParam.put("PARAMS", parametersJson.toString()); // this.getAttributeAsString(PARAMETERS)
		BusinessModelRuntime dum = new BusinessModelRuntime(profile, locale);
		DriversValidationAPI validation = new DriversValidationAPI(profile, locale);
		List errors = null;
		try {

			try {
				errors = validation.getParametersErrors(businessModel, role, dum);
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot evaluate errors on parameters validation", e);
			}

		} catch (Exception e) {
			logger.debug("Error in handleNormalExecutionError", e);

		} finally {
			handleNormalExecutionErrorMonitor.stop();
		}
		return errors;
	}

}
