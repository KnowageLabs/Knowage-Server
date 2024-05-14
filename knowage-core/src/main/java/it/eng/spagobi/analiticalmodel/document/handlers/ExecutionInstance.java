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
package it.eng.spagobi.analiticalmodel.document.handlers;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import it.eng.LightNavigationConstants;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.util.JavaScript;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesRetriever;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.ParameterValuesDecoder;
import it.eng.spagobi.commons.validation.SpagoBIValidationImpl;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.kpi.KpiDriver;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.objects.Couple;

/**
 * This class represents a document execution instance. This contains the following attributes: 1. execution flow id: it is the id of an execution flow
 * (execution in cross navigation mode share the same flow id) 2. execution id: single execution id, it is unique for a single execution 3. the BIObject being
 * executed 4. the execution role 4. the execution modality
 *
 * @author zerbetto
 *
 */
public class ExecutionInstance implements Serializable {

	private static final Logger LOGGER = LogManager.getLogger(ExecutionInstance.class);
	// private static final String TREE_INNER_LOV_TYPE = "treeinner";

	private String flowId = null;
	private String executionId = null;
	private BIObject object = null;
	private SubObject subObject = null;
	private Snapshot snapshot = null;
	private String executionRole = null;
	private String executionModality = null;
	private IEngUserProfile userProfile = null;
	private boolean displayToolbar = true;
	private boolean displaySliders = true;
	private Calendar calendar = null;
	private Locale locale = null;

	/**
	 * Instantiates a new execution instance.
	 *
	 * @param flowId        the flow id
	 * @param executionId   the execution id
	 * @param obj           the obj
	 * @param executionRole the execution role
	 * @throws Exception
	 */
	public ExecutionInstance(IEngUserProfile userProfile, String flowId, String executionId, Integer biobjectId,
			String executionRole, String executionModality, Locale locale) throws Exception {

		LOGGER.debug(
				"Creating ExecutionInstance for user {}, flow id {}, execution id {}, biObjectId {}, execution role {}, execution modality {} and locale {}",
				userProfile, flowId, executionId, biobjectId, executionRole, executionModality, locale);

		if (userProfile == null || flowId == null || executionId == null || biobjectId == null) {
			throw new Exception("Invalid arguments.");
		}
		this.userProfile = userProfile;
		this.flowId = flowId;
		this.executionId = executionId;
		this.object = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByIdAndRole(biobjectId, executionRole);
		this.calendar = new GregorianCalendar();
		this.executionRole = executionRole;
		this.executionModality = (executionModality == null) ? SpagoBIConstants.NORMAL_EXECUTION_MODALITY
				: executionModality;
		this.locale = locale;
		initBIParameters();
	}

	public ExecutionInstance(IEngUserProfile userProfile, String flowId, String executionId, Integer biobjectId,
			String executionRole, String executionModality, boolean displayToolbar, Locale locale) throws Exception {
		this(userProfile, flowId, executionId, biobjectId, executionRole, executionModality, locale);
		this.displayToolbar = displayToolbar;
	}

	public ExecutionInstance(IEngUserProfile userProfile, String flowId, String executionId, Integer biobjectId,
			String executionRole, String executionModality, boolean displayToolbar, boolean displaySliders,
			Locale locale) throws Exception {
		this(userProfile, flowId, executionId, biobjectId, executionRole, executionModality, displayToolbar, locale);
		this.displaySliders = displaySliders;
	}

	public ExecutionInstance(IEngUserProfile userProfile, String flowId, String executionId, Integer biobjectId,
			Integer biobjectVersion, String executionRole, String executionModality, boolean displayToolbar,
			boolean displaySliders, Locale locale) throws Exception {
		this(userProfile, flowId, executionId, biobjectId, executionRole, executionModality, displayToolbar,
				displaySliders, locale);
		this.object.setDocVersion(biobjectVersion);
	}

	/**
	 * Used by Kpi Engine for detail documents
	 *
	 * @param userProfile
	 * @param flowId
	 * @param executionId
	 * @param biobjectLabel
	 * @param executionRole
	 * @param executionModality
	 * @throws Exception
	 */
	public ExecutionInstance(IEngUserProfile userProfile, String flowId, String executionId, String biobjectLabel,
			String executionRole, String executionModality, Locale locale) throws Exception {

		LOGGER.debug(
				"Creating ExecutionInstance for user {}, flow id {}, execution id {}, biObject label {}, execution role {}, execution modality {} and locale {}",
				userProfile, flowId, executionId, biobjectLabel, executionRole, executionModality, locale);

		if (userProfile == null || flowId == null || executionId == null || biobjectLabel == null) {
			throw new Exception("Invalid arguments.");
		}
		this.userProfile = userProfile;
		this.flowId = flowId;
		this.executionId = executionId;
		this.object = DriversRuntimeLoaderFactory.getDriversRuntimeLoader()
				.loadBIObjectForExecutionByLabelAndRole(biobjectLabel, executionRole);
		this.calendar = new GregorianCalendar();
		this.executionRole = executionRole;
		this.executionModality = (executionModality == null) ? SpagoBIConstants.NORMAL_EXECUTION_MODALITY
				: executionModality;
		this.locale = locale;
		initBIParameters();
	}

	public static ExecutionInstance getExecutionInstanceByLabel(ExecutionInstance instance, String biobjectLabel,
			Locale locale) throws Exception {
		IEngUserProfile userProfile = instance.userProfile;
		String flowId = instance.flowId;
		String executionId = instance.executionId;
		String executionRole = instance.executionRole;
		String executionModality = instance.executionModality;

		return new ExecutionInstance(userProfile, flowId, executionId, biobjectLabel, executionRole, executionModality,
				locale);
	}

	public void changeExecutionRole(String newRole) throws Exception {
		LOGGER.debug("IN");
		List correctExecutionRoles = loadCorrectRolesForExecution();
		if (!correctExecutionRoles.contains(newRole)) {
			throw new Exception("The role [" + newRole + "] is not a valid role for executing document ["
					+ object.getLabel() + "].");
		}
		// reload the biobject
		this.object = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByIdAndRole(object.getId(), newRole);
		// generates a new execution id
		UUID uuidObj = UUID.randomUUID();
		String currExecutionId = uuidObj.toString();
		this.executionId = currExecutionId.replace("-", "");
		this.calendar = new GregorianCalendar();
		initBIParameters();
		LOGGER.debug("OUT");
	}

	private void initBIParameters() {
		LOGGER.debug("IN");
		Monitor monitor = MonitorFactory.start("spagobi.ExecutionInstance.initBIParameters");
		List<BIObjectParameter> tmpBIObjectParameters = object.getDrivers();
		Iterator<BIObjectParameter> it = tmpBIObjectParameters.iterator();
		BIObjectParameter aBIObjectParameter = null;
		while (it.hasNext()) {
			aBIObjectParameter = it.next();
			LOGGER.debug("Parameter Label: {}", aBIObjectParameter.getLabel());
			// check if the script return an unique value and preload it
			Parameter par = aBIObjectParameter.getParameter();
			if (par != null) {
				ModalitiesValue paruse = par.getModalityValue();
				if (!paruse.getITypeCd().equals("MAN_IN") && paruse.getSelectionType().equals("COMBOBOX")) { // load values only if not a lookup
					try {
						String lovProv = paruse.getLovProvider();
						ILovDetail lovProvDet = LovDetailFactory.getLovFromXML(lovProv);

						Set<String> parameterNames = lovProvDet.getParameterNames();
						if (parameterNames == null || parameterNames.isEmpty()) {
							LovResultCacheManager executionCacheManager = new LovResultCacheManager();
							String lovResult = executionCacheManager.getLovResult(this.userProfile, lovProvDet,
									this.getDependencies(aBIObjectParameter), this, true);

							LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
							// if the lov is single value and the parameter value is not set, the parameter value
							// is the lov result
							if (lovResultHandler.isSingleValue() && aBIObjectParameter.getParameterValues() == null) {
								if (!aBIObjectParameter.getParameter().getType().equals("DATE")) {
									aBIObjectParameter.setParameterValues(
											lovResultHandler.getValues(lovProvDet.getValueColumnName()));
									aBIObjectParameter.setHasValidValues(true);
									aBIObjectParameter.setTransientParmeters(true);
								}

							}
						}
					} catch (Exception e) {
						LOGGER.error(e);
						continue;
					}
				}
			}
		}
		monitor.stop();
		LOGGER.debug("OUT");
	}

	private List<String> loadCorrectRolesForExecution() throws EMFInternalError, EMFUserError {
		LOGGER.debug("Loading correct roles for execution");
		List<String> correctRoles = ObjectsAccessVerifier.getCorrectRolesForExecution(object.getId(), userProfile);
		LOGGER.debug("Correct roles for execution are {}", correctRoles);
		return correctRoles;
	}

	public boolean isDirectExecution() {
		LOGGER.debug("Checking if it is direct execution");
		if (object == null) {
			LOGGER.error("No object is set into this ExecutionInstance!!");
			return false;
		}
		List<BIObjectParameter> biParameters = object.getDrivers();
		if (biParameters == null) {
			LOGGER.error("BIParameters list cannot be null!!!");
			return false;
		}
		if (biParameters.isEmpty()) {
			LOGGER.debug("BIParameters list is empty.");
			return true;
		}
		int countHidePar = 0;
		Iterator<BIObjectParameter> iterPars = biParameters.iterator();
		BIObjectParameter biParameter = null;
		while (iterPars.hasNext()) {
			biParameter = iterPars.next();
			Parameter par = biParameter.getParameter();
			if (biParameter.isTransientParmeters()) {
				countHidePar++;
				continue;
			}
			if (biParameter.hasValidValues()) {
				countHidePar++;
				continue;
			}
			if (par == null) {
				LOGGER.error("The biparameter with label {} and url name {} has no parameter associated. ",
						biParameter.getLabel(), biParameter.getParameterUrlName());
				continue;
			}
			/*
			 * The following lines were commented because there should be not need to check if the parameter is single-value, since if it is single-value then it is
			 * transient (see initBIParameters method)
			 *
			 * if (biParameter.getLovResult() == null) continue; LovResultHandler lovResultHandler; try { lovResultHandler = new
			 * LovResultHandler(biParameter.getLovResult()); if(lovResultHandler.isSingleValue()) countHidePar ++; } catch (SourceBeanException e) { continue; }
			 */
		}
		return countHidePar == biParameters.size();
	}

	public void applyViewpoint(String userProvidedParametersStr, boolean transientMode) {
		LOGGER.debug("Applying viewpoint {} with transient mode equals to {}", userProvidedParametersStr,
				transientMode);
		if (userProvidedParametersStr != null) {
			List<BIObjectParameter> biparameters = object.getDrivers();
			if (biparameters == null) {
				LOGGER.error("BIParameters list cannot be null!!!");
				return;
			}
			userProvidedParametersStr = JavaScript.unescape(userProvidedParametersStr);
			String[] userProvidedParameters = userProvidedParametersStr.split("&");
			for (int i = 0; i < userProvidedParameters.length; i++) {
				String[] chunks = userProvidedParameters[i].split("=");
				if (chunks == null || chunks.length > 2) {
					LOGGER.warn(
							"User provided parameter {} cannot be splitted in [parameter url name=parameter value] by '=' characters.",
							userProvidedParameters[i]);
					continue;
				}
				String parUrlName = chunks[0];
				if (parUrlName == null || parUrlName.trim().equals(""))
					continue;
				BIObjectParameter biparameter = null;
				Iterator<BIObjectParameter> it = biparameters.iterator();
				while (it.hasNext()) {
					BIObjectParameter temp = it.next();
					if (temp.getParameterUrlName().equals(parUrlName)) {
						biparameter = temp;
						break;
					}
				}
				if (biparameter == null) {
					LOGGER.warn("No BIObjectParameter with url name {} was found.", parUrlName);
					continue;
				}
				// if the user specified the parameter value it is considered, elsewhere an empty String is considered
				String parValue = "";
				if (chunks.length == 2) {
					parValue = chunks[1];
				}
				if (parValue != null && parValue.equalsIgnoreCase("NULL")) {
					biparameter.setParameterValues(null);
				} else {
					List parameterValues = new ArrayList();
					String[] values = parValue.split(";");
					for (int m = 0; m < values.length; m++) {
						parameterValues.add(values[m]);
					}
					biparameter.setParameterValues(parameterValues);
				}
				biparameter.setTransientParmeters(transientMode);
			}
		}
		LOGGER.debug("End applying viewpoint");
	}

	public void setParameterValues(String userProvidedParametersStr, boolean transientMode) {
		LOGGER.debug("Setting parameter value {} for transient mode equals to {}", userProvidedParametersStr,
				transientMode);
		if (userProvidedParametersStr != null) {
			ParameterValuesDecoder decoder = new ParameterValuesDecoder();
			List<BIObjectParameter> biparameters = object.getDrivers();
			if (biparameters == null) {
				LOGGER.error("BIParameters list cannot be null!!!");
				return;
			}
			userProvidedParametersStr = JavaScript.unescape(userProvidedParametersStr);
			String[] userProvidedParameters = userProvidedParametersStr.split("&");
			for (int i = 0; i < userProvidedParameters.length; i++) {
				String[] chunks = userProvidedParameters[i].split("=");
				if (chunks == null || chunks.length > 2) {
					LOGGER.warn(
							"User provided parameter {} cannot be splitted in [parameter url name=parameter value] by '=' characters.",
							userProvidedParameters[i]);
					continue;
				}
				String parUrlName = chunks[0];
				if (parUrlName == null || parUrlName.trim().equals(""))
					continue;
				BIObjectParameter biparameter = null;
				Iterator<BIObjectParameter> it = biparameters.iterator();
				while (it.hasNext()) {
					BIObjectParameter temp = it.next();
					if (temp.getParameterUrlName().equals(parUrlName)) {
						biparameter = temp;
						break;
					}
				}
				if (biparameter == null) {
					LOGGER.warn("No BIObjectParameter with url name {} was found.", parUrlName);
					continue;
				}
				// if the user specified the parameter value it is considered, elsewhere an empty String is considered
				String parValue = "";
				if (chunks.length == 2) {
					parValue = chunks[1];
				}
				if (parValue != null && parValue.equalsIgnoreCase("NULL")) {
					biparameter.setParameterValues(null);
				} else {
					List parameterValues = decoder.decode(parValue);
					// List parameterValues = new ArrayList();
					// String[] values = parValue.split(";");
					// for (int m = 0; m < values.length; m++) {
					// parameterValues.add(values[m]);
					// }
					biparameter.setParameterValues(parameterValues);
				}
				biparameter.setTransientParmeters(transientMode);
			}
		}
		LOGGER.debug("End setting parameter values");
	}

	public void refreshParametersValues(SourceBean request, boolean transientMode) {
		LOGGER.debug("Refreshing parameters value from {} with transient mode equals to {}", request, transientMode);
		String pendingDelete = (String) request.getAttribute("PENDING_DELETE");
		List<BIObjectParameter> biparams = object.getDrivers();
		Iterator<BIObjectParameter> iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			BIObjectParameter biparam = iterParams.next();
			if (pendingDelete != null && !pendingDelete.trim().equals("")) {
				/*
				 * The following line was commented because there should be not need to check if the parameter is single-value, since if it is single-value then it is transient
				 * (see initBIParameters method)
				 */
				// if (isSingleValue(biparam) || biparam.isTransientParmeters())
				if (biparam.isTransientParmeters())
					continue;
				biparam.setParameterValues(null);
				biparam.setParameterValuesDescription(null);
			} else {
				refreshParameter(biparam, request, transientMode);
			}
		}
		LOGGER.debug("End refreshing parameters values");
	}

	public void refreshParametersValues(JSONObject jsonObject, boolean transientMode) {
		LOGGER.debug("Refreshing parameters value from {} with transient mode equals to {}", jsonObject, transientMode);
		Assert.assertNotNull(jsonObject, "JSONObject in input is null!!");
		List<BIObjectParameter> biparams = object.getDrivers();
		Iterator<BIObjectParameter> iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			BIObjectParameter biparam = iterParams.next();
			refreshParameter(biparam, jsonObject, transientMode);
		}
		LOGGER.debug("End refreshing parameters values");
	}

	private void refreshParameter(BIObjectParameter biparam, JSONObject jsonObject, boolean transientMode) {
		LOGGER.debug("Refreshing parameter value {} from {} with transient mode equals to {}", biparam, jsonObject,
				transientMode);
		Assert.assertNotNull(biparam, "Parameter in input is null!!");
		Assert.assertNotNull(jsonObject, "JSONObject in input is null!!");
		String nameUrl = biparam.getParameterUrlName();
		List values = new ArrayList();
		try {
			Object o = jsonObject.opt(nameUrl);
			if (o != null) {
				if (o instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) o;
					for (int c = 0; c < jsonArray.length(); c++) {
						Object anObject = jsonArray.get(c);
						if (anObject != null) {
							values.add(anObject.toString());
						}
					}
				} else {
					// trim value at beginning and end of the string
					String valToInsert = o.toString();
					valToInsert = valToInsert.trim();
					if (!valToInsert.isEmpty()) {
						values.add(valToInsert);
					}
				}
			}
		} catch (JSONException e) {
			LOGGER.error("Cannot get {} values from JSON object", nameUrl, e);
			throw new SpagoBIServiceException("Cannot retrieve values for biparameter " + biparam.getLabel(), e);
		}

		if (!values.isEmpty()) {
			LOGGER.debug("Updating values of biparameter {} to {}", biparam.getLabel(), values);
			biparam.setParameterValues(values);
		} else {
			LOGGER.debug("Erasing values of biparameter {}", biparam.getLabel());
			biparam.setParameterValues(null);
		}

		biparam.setTransientParmeters(transientMode);
		LOGGER.debug("End refreshing parameter value");
	}

	public void refreshParametersValues(Map parametersMap, boolean transientMode) {
		LOGGER.debug("Refreshing parameter value from {} with transient mode equals to {}", parametersMap,
				transientMode);
		Monitor monitor = MonitorFactory.start("spagobi.ExecutionInstance.refreshParametersValues");
		List<BIObjectParameter> biparams = object.getDrivers();
		Iterator<BIObjectParameter> iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			BIObjectParameter biparam = iterParams.next();
			refreshParameter(biparam, parametersMap, transientMode);
		}
		monitor.stop();
		LOGGER.debug("End refreshing parameter value");
	}

	private void refreshParameter(BIObjectParameter biparam, SourceBean request, boolean transientMode) {
		LOGGER.debug("Refreshing parameter value {} from {} with transient mode equals to {}", biparam, request,
				transientMode);
		String nameUrl = biparam.getParameterUrlName();
		List paramAttrsList = request.getAttributeAsList(nameUrl);
		ArrayList paramvalues = new ArrayList();
		if (paramAttrsList.isEmpty())
			return;
		Iterator iterParAttr = paramAttrsList.iterator();
		while (iterParAttr.hasNext()) {
			String values = (String) iterParAttr.next();
			String[] value = values.split(";");
			for (int i = 0; i < value.length; i++) {
				if (!value[i].trim().equalsIgnoreCase(""))
					paramvalues.add(value[i]);
			}
		}
		if (paramvalues.isEmpty())
			biparam.setParameterValues(null);
		else
			biparam.setParameterValues(paramvalues);
		biparam.setTransientParmeters(transientMode);
		LOGGER.debug("End refreshing parameter value");
	}

	private void refreshParameter(BIObjectParameter biparam, Map parametersMap, boolean transientMode) {
		LOGGER.debug("Refreshing parameter value {} from {} with transient mode equals to {}", biparam, parametersMap,
				transientMode);
		String nameUrl = biparam.getParameterUrlName();
		Object parameterValueObj = parametersMap.get(nameUrl);
		Map<String, List<Object>> paramsValueV2 = new HashMap<>();
		if (parametersMap.containsKey("params")) {
			String parametersV2FromUrlAsString = (String) parametersMap.get("params");
			String parametersV2FromUrl = Optional.ofNullable(parametersV2FromUrlAsString)
					.map(e -> new String(java.util.Base64.getDecoder().decode(e))).orElse("[]");
			LOGGER.debug("The JSON of the params in the query is {}", parametersV2FromUrl);
			try {
				JSONArray parametersV2FromUrlAsJSONArray = new JSONArray(parametersV2FromUrl);
				for (int i = 0; i < parametersV2FromUrlAsJSONArray.length(); i++) {
					JSONObject curr = (JSONObject) parametersV2FromUrlAsJSONArray.get(i);
					String currUrlName = curr.getString("urlName");
					JSONArray jsonArray = curr.getJSONArray("value");

					paramsValueV2.putIfAbsent(currUrlName, new ArrayList<>());

					int numOfValues = jsonArray.length();
					LOGGER.debug("Found {} values", numOfValues);
					for (int j = 0; i < numOfValues; j++) {
						JSONObject currValue = (JSONObject) jsonArray.get(j);
						LOGGER.debug("The current value is {}", currValue);
						Object currValueValue = currValue.get("value");

						paramsValueV2.get(currUrlName).add(currValueValue);
					}
				}
			} catch (JSONException e) {
				LOGGER.error("Non-Fatal error. The new params structure in query param is not a JSONArray: {}",
						parametersV2FromUrl, e);
			}
		}
		List values = null;
		if (parameterValueObj != null) {
			if (parameterValueObj instanceof List) {
				values = (List) parameterValueObj;
			} else if (parameterValueObj instanceof Object[]) {
				Object[] array = (Object[]) parameterValueObj;
				values = new ArrayList();
				for (int i = 0; i < array.length; i++) {
					Object o = array[i];
					if (o != null) {
						values.add(o.toString());
					}
				}
			} else if (parameterValueObj instanceof String) {
				values = new ArrayList();
				values.add(parameterValueObj);
			} else {
				values = new ArrayList();
				values.add(parameterValueObj.toString());
			}
		} else if (paramsValueV2.containsKey(nameUrl)) {
			List<Object> paramsV2Values = paramsValueV2.get(nameUrl);
			LOGGER.debug("Setting parameter with label {} with values {} from new params query param",
					biparam.getLabel(), paramsV2Values);
			values = new ArrayList();
			values.addAll(paramsV2Values);
		} else {
			LOGGER.debug("No attribute found on input map for biparameter with name {}", biparam.getLabel());
		}

		if (values != null && !values.isEmpty()) {
			LOGGER.debug("Updating values of biparameter {} to {}", biparam.getLabel(), values);
			biparam.setParameterValues(values);
		} else {
			LOGGER.debug("Erasing values of biparameter {}", biparam.getLabel());
			biparam.setParameterValues(null);
		}

		biparam.setTransientParmeters(transientMode);
		LOGGER.debug("End refreshing parameter");
	}

	public List getParametersErrors() throws Exception {
		return getParametersErrors(false);
	}

	public List getParametersErrors(boolean onEditMode) throws Exception {
		LOGGER.debug("Getting parameters errors with edit mode equals to {}", onEditMode);
		List toReturn = new ArrayList();
		List<BIObjectParameter> biparams = object.getDrivers();
		if (biparams.isEmpty())
			return toReturn;
		Iterator<BIObjectParameter> iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			BIObjectParameter biparam = iterParams.next();
			LOGGER.debug("Evaluating errors for biparameter {}...", biparam.getLabel());
			List errorsOnChecks = getValidationErrorsOnChecks(biparam);

			List values = biparam.getParameterValues();
			if ((!onEditMode && biparam.isRequired())
					&& (values == null || values.isEmpty() || normalizeList(values).isEmpty())) {
				EMFValidationError error = SpagoBIValidationImpl.validateField(biparam.getParameterUrlName(),
						biparam.getLabel(), null, "MANDATORY", null, null, null);
				errorsOnChecks.add(error);
			}

			if (errorsOnChecks != null && !errorsOnChecks.isEmpty()) {
				LOGGER.warn("Found {} errors on checks for biparameter {}", errorsOnChecks.size(), biparam.getLabel());
			}
			toReturn.addAll(errorsOnChecks);

			if (values != null && !values.isEmpty()
					&& !(values.size() == 1 && (values.get(0) == null || values.get(0).toString().trim().equals("")))) {
				List errorsOnValues = getValidationErrorsOnValues(biparam);
				if (errorsOnValues != null && !errorsOnValues.isEmpty()) {
					LOGGER.warn("Found {} errors on values for biparameter {}", errorsOnValues.size(),
							biparam.getLabel());
				}
				toReturn.addAll(errorsOnValues);
			}
			boolean hasValidValues = false;
			// if parameter has values and there are no errors, the parameter has valid values
			if (values != null && !values.isEmpty() && toReturn.isEmpty()) {
				hasValidValues = true;
			}
			biparam.setHasValidValues(hasValidValues);
		}
		LOGGER.debug("End getting parameters errors");
		return toReturn;
	}

	// Thanks to Emanuele Granieri of osmosit.com
	private List normalizeList(List l) {
		Iterator i = l.iterator();
		while (i.hasNext()) {
			Object el = i.next();
			if (el instanceof String) {
				String elString = ((String) el);
				if (elString.length() == 0) {
					i.remove();
				}
			}
		}
		return l;
	}

	private List getValidationErrorsOnChecks(BIObjectParameter biparameter) throws Exception {
		LOGGER.debug("Getting validation error on checks of {}", biparameter);
		List toReturn = new ArrayList();
		List checks = biparameter.getParameter().getChecks();
		String label = biparameter.getLabel();
		if (checks == null || checks.isEmpty()) {
			LOGGER.debug("OUT. No checks associated for biparameter {}.", label);
			return toReturn;
		} else {
			Iterator it = checks.iterator();
			Check check = null;
			while (it.hasNext()) {
				check = (Check) it.next();
				if (check.getValueTypeCd().equalsIgnoreCase("MANDATORY"))
					continue;
				LOGGER.debug("Applying check {} to biparameter {}...", check.getLabel(), label);
				List errors = getValidationErrorOnCheck(biparameter, check);
				if (errors != null && !errors.isEmpty()) {
					Iterator errorsIt = errors.iterator();
					while (errorsIt.hasNext()) {
						EMFValidationError error = (EMFValidationError) errorsIt.next();
						LOGGER.warn("Found an error applying check {} for biparameter {}: {}", check.getLabel(), label,
								error.getDescription());
					}
					toReturn.addAll(errors);
				} else {
					LOGGER.debug("No errors found applying check {} to biparameter {}.", check.getLabel(), label);
				}
			}
			LOGGER.debug("End getting validation error on checks");
			return toReturn;
		}
	}

	private List getValidationErrorOnCheck(BIObjectParameter biparameter, Check check) throws Exception {
		LOGGER.debug("Getting validation error on checks of {} for check {}", biparameter, check);
		List toReturn = new ArrayList();
		String urlName = biparameter.getParameterUrlName();
		String label = biparameter.getLabel();
		List values = biparameter.getParameterValues();

		if (check.getValueTypeCd().equalsIgnoreCase("MANDATORY")) {
			if (values == null || values.isEmpty()) {
				EMFValidationError error = SpagoBIValidationImpl.validateField(urlName, label, null, "MANDATORY", null,
						null, null);
				toReturn.add(error);
			} else {
				Iterator valuesIt = values.iterator();
				boolean hasAtLeastOneValue = false;
				while (valuesIt.hasNext()) {
					String aValue = (String) valuesIt.next();
					if (aValue != null && !aValue.trim().equals("")) {
						hasAtLeastOneValue = true;
						break;
					}
				}
				if (!hasAtLeastOneValue) {
					EMFValidationError error = SpagoBIValidationImpl.validateField(urlName, label, null, "MANDATORY",
							null, null, null);
					toReturn.add(error);
				}
			}
		} else {
			if (values != null && !values.isEmpty()) {
				Iterator valuesIt = values.iterator();
				while (valuesIt.hasNext()) {
					String aValue = (String) valuesIt.next();
					EMFValidationError error = null;
					if (check.getValueTypeCd().equalsIgnoreCase("LETTERSTRING")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "LETTERSTRING", null, null,
								null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("ALFANUMERIC")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "ALFANUMERIC", null, null,
								null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("NUMERIC")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "NUMERIC", null, null,
								null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("EMAIL")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "EMAIL", null, null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("FISCALCODE")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "FISCALCODE", null, null,
								null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("INTERNET ADDRESS")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "URL", null, null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("DECIMALS")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "DECIMALS",
								check.getFirstValue(), check.getSecondValue(), null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("RANGE")) {
						if (biparameter.getParameter().getType().equalsIgnoreCase("DATE")) {
							// In a Parameter where parameterType == DATE the mask represent the date format
							error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "DATERANGE",
									check.getFirstValue(), check.getSecondValue(),
									biparameter.getParameter().getMask());
						} else if (biparameter.getParameter().getType().equalsIgnoreCase("NUM")) {
							// In a Parameter where parameterType == NUM the mask represent the decimal format
							error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "NUMERICRANGE",
									check.getFirstValue(), check.getSecondValue(),
									biparameter.getParameter().getMask());
						} else if (biparameter.getParameter().getType().equalsIgnoreCase("STRING")) {
							error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "STRINGRANGE",
									check.getFirstValue(), check.getSecondValue(), null);
						}
					} else if (check.getValueTypeCd().equalsIgnoreCase("MAXLENGTH")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "MAXLENGTH",
								check.getFirstValue(), null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("MINLENGTH")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "MINLENGTH",
								check.getFirstValue(), null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("REGEXP")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "REGEXP",
								check.getFirstValue(), null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("DATE")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "DATE",
								check.getFirstValue(), null, null);
					}
					if (error != null)
						toReturn.add(error);
				}
			}
		}
		LOGGER.debug("End getting validation error on checks");
		return toReturn;
	}

	private List getValidationErrorsOnValues(BIObjectParameter biparam) throws Exception {
		LOGGER.debug("Getting validation errors on values for {}", biparam);
		String biparamLabel = biparam.getLabel();

		// outputType parameter is not validated
		String urlName = biparam.getParameterUrlName();
		if ("outputType".equals(urlName)) {
			LOGGER.debug("Parameter is outputType parameter, it is not validated");
			return new ArrayList();
		}

		// manual inputs are not validated
		ModalitiesValue lov = biparam.getParameter().getModalityValue();
		if (lov.getITypeCd().equals("MAN_IN")) {
			LOGGER.debug("Modality in use for biparameter {} is manual input", biparamLabel);
			validateManualInput(biparam);
			return new ArrayList();
		}
		// patch for default date value
		if (biparam.getParameter().getType().equalsIgnoreCase("DATE")) {
			LOGGER.debug("Parameter {} has lov defined just for default value: any other chose allowed", biparamLabel);
			return new ArrayList();
		}

		// we need to process default values and non-default values separately: default values do not require validation,
		// non-default values instead require validation

		DefaultValuesRetriever retriever = new DefaultValuesRetriever();
		DefaultValuesList allDefaultValues = retriever.getDefaultValues(biparam, this, this.userProfile);
		// from the complete list of values, get the values that are default values
		DefaultValuesList selectedDefaultValue = this.getSelectedDefaultValues(biparam, allDefaultValues);

		// validation must proceed only with non-default values
		// from the complete list of values, get the values that are not default values
		List nonDefaultValues = null;
		if (lov.getITypeCd().equalsIgnoreCase("QUERY")) {
			DefaultValuesList allDefaultQueryValues = retriever.getDefaultQueryValues(biparam, this, this.userProfile);
			nonDefaultValues = this.getNonDefaultQueryValues(biparam, allDefaultQueryValues);
		} else {
			nonDefaultValues = this.getNonDefaultValues(biparam, allDefaultValues);
		}

		if (nonDefaultValues.isEmpty()) {
			LOGGER.debug("All selected values are default values; no need to validate them");
			return new ArrayList();
		}
		BIObjectParameter clone = biparam.clone();
		clone.setParameterValues(nonDefaultValues);

		// get the lov provider detail
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		// get lov result
		String lovResult = null;
		List toReturn = null;
		// if (lovProvDet instanceof QueryDetail) {
		if (lovProvDet instanceof QueryDetail && lovProvDet.isSimpleLovType()) {
			toReturn = getValidationErrorsOnValuesForQueries((QueryDetail) lovProvDet, clone);
		} else {
			LovResultCacheManager executionCacheManager = new LovResultCacheManager();
			lovResult = executionCacheManager.getLovResult(this.userProfile, lovProvDet, this.getDependencies(clone),
					this, true);
			toReturn = getValidationErrorsOnValuesByLovResult(lovResult, clone, lovProvDet);
		}
		mergeDescriptions(biparam, selectedDefaultValue, clone);
		LOGGER.debug("End getting validation errors on values");
		return toReturn;
	}

	private void validateManualInput(BIObjectParameter driver) {
		List<String> values = driver.getParameterValues();
		if (values == null || values.isEmpty()) {
			// no values to be validated
			return;
		}
		String value = values.get(0);
		if (StringUtils.isEmpty(value)) {
			// no values to be validated
			return;
		}
		if (driver.getParameter().getType().equalsIgnoreCase("DATE")) {
			validateDate(value);
		} else if (driver.getParameter().getType().equalsIgnoreCase("NUM")) {
			validateNumber(value);
		} else if (driver.getParameter().getType().equalsIgnoreCase("STRING")) {
			// unfortunately we cannot do anything for strings as a general validation, anything is a String!
		}
	}

	private void validateNumber(String value) {
		// @formatter:off
		if (!GenericValidator.isInt(value)
				&& !GenericValidator.isFloat(value)
				&& !GenericValidator.isDouble(value)
				&& !GenericValidator.isShort(value)
				&& !GenericValidator.isLong(value)) {
			// @formatter:on
			// The string is not a integer, not a float, not a double, not a short, not a long, therefore it is not a number
			throw new SecurityException("Input value " + value + " is not a valid number!");
		}
	}

	private void validateDate(String value) {
		String dateFormat = GeneralUtilities.getServerDateFormat();
		if (!GenericValidator.isDate(value, dateFormat, true)) {
			throw new SecurityException(
					"Input value " + value + " is not a valid date [considering fomat " + dateFormat + "]!");
		}
	}

	private void mergeDescriptions(BIObjectParameter biparam, DefaultValuesList selectedDefaultValue,
			BIObjectParameter cloned) {
		int valuePosition;
		List nonDefaultValues = cloned.getParameterValues();
		List nonDefaultDescriptions = cloned.getParameterValuesDescription();
		List parameterValues = biparam.getParameterValues();
		List parameterDescriptions = new ArrayList<String>();
		if (parameterValues != null) {
			for (int i = 0; i < parameterValues.size(); i++) {
				Object aValue = parameterValues.get(i);
				valuePosition = nonDefaultValues.indexOf(aValue);
				if (valuePosition >= 0) {
					// this means that the value IS NOT a default value
					parameterDescriptions.add(nonDefaultDescriptions.get(valuePosition));
				} else {
					// this means that the value IS a default value
					LovValue defaultValue = selectedDefaultValue.getDefaultValue(aValue);

					parameterDescriptions.add((defaultValue != null) ? defaultValue.getDescription() : "");
				}
			}
		}
		biparam.setParameterValuesDescription(parameterDescriptions);
	}

	private List getNonDefaultValues(BIObjectParameter analyticalDocumentParameter, DefaultValuesList defaultValues) {
		LOGGER.debug("Getting non default values for {} and {}", analyticalDocumentParameter, defaultValues);
		List toReturn = new ArrayList<String>();
		List values = analyticalDocumentParameter.getParameterValues();
		if (values != null && !values.isEmpty()) {
			for (int i = 0; i < values.size(); i++) {
				String value = values.get(i).toString();
				if (!defaultValues.contains(value)) {
					LOGGER.debug("Value {} is not a default value.", value);
					toReturn.add(value);
				}
			}
		}
		LOGGER.debug("End getting non default values");
		return toReturn;
	}

	private List<String> getNonDefaultQueryValues(BIObjectParameter analyticalDocumentParameter,
			DefaultValuesList defaultValues) {
		LOGGER.debug("Getting non default query values for {} and {}", analyticalDocumentParameter, defaultValues);
		List<String> toReturn = new ArrayList<>();
		List<String> values = analyticalDocumentParameter.getParameterValues();
		if (values != null && !values.isEmpty()) {
			for (int i = 0; i < values.size(); i++) {
				// Removes the single quotes from each single parameter value
				String value = values.get(i).replaceAll("^'(.*)'$", "$1");
				if (!defaultValues.contains(value)) {
					// if is multivalue the values come as a single string value
					if (analyticalDocumentParameter.isMultivalue()) {
						String[] singleLineValues = value.split("','");

						for (String singleValue : singleLineValues) {
							if (!defaultValues.contains(singleValue)) {
								LOGGER.debug("Value {} is not a default value.", value);
								toReturn.add(value);
								break;
							}
						}
					} else {
						LOGGER.debug("Value {} is not a default value.", value);
						toReturn.add(value);
					}
				}
			}
		}
		LOGGER.debug("End getting non default query values");
		return toReturn;
	}

	private DefaultValuesList getSelectedDefaultValues(BIObjectParameter analyticalDocumentParameter,
			DefaultValuesList defaultValues) {
		LOGGER.debug("Getting selected default values for {} and {}", analyticalDocumentParameter, defaultValues);
		DefaultValuesList toReturn = new DefaultValuesList();
		if (defaultValues == null || defaultValues.isEmpty()) {
			LOGGER.debug("No default values in input");
			return toReturn;
		}
		List values = analyticalDocumentParameter.getParameterValues();
		if (values != null && !values.isEmpty()) {
			for (int i = 0; i < values.size(); i++) {
				String value = values.get(i).toString();
				LovValue defaultValue = defaultValues.getDefaultValue(value);
				if (defaultValue != null) {
					LOGGER.debug("Value {} is a selected value.", defaultValue);
					toReturn.add(defaultValue);
				}
			}
		}
		LOGGER.debug("End getting selected default values");
		return toReturn;
	}

	private List getValidationErrorsOnValuesForQueries(QueryDetail queryDetail, BIObjectParameter biparam)
			throws Exception {
		LOGGER.debug("Getting validation errors on values for queries for {} and {}", queryDetail, biparam);
		List toReturn = null;
		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		List<ObjParuse> dependencies = this.getDependencies(biparam);
		List<? extends AbstractDriver> drivers = Collections.emptyList();
		// if query is not in cache, do not execute it as it is!!!
		String lovResult = executionCacheManager.getLovResult(this.userProfile, this.getLovDetail(biparam),
				this.getDependencies(biparam), this, false);
		if (lovResult == null) {
			// lov is not in cache: we must validate values
			toReturn = queryDetail.validateValues(this.userProfile, biparam, drivers, dependencies);
		} else {
			toReturn = getValidationErrorsOnValuesByLovResult(lovResult, biparam, queryDetail);
			if (toReturn.isEmpty()) {
				// values are ok, this should be most often the case
			} else {
				// if there are dependencies, we should not consider them since they are not mandatory
				if (!dependencies.isEmpty()) {
					toReturn = queryDetail.validateValues(this.userProfile, biparam, drivers, dependencies);
				}
			}
		}
		LOGGER.debug("End getting validation errors on values for queries");
		return toReturn;
	}

	private List getValidationErrorsOnValuesByLovResult(String lovResult, BIObjectParameter biparam,
			ILovDetail lovProvDet) throws Exception {
		LOGGER.debug("Getting validation errors on values by LOV result for {}, {} and {}", lovResult, biparam,
				lovProvDet);
		List toReturn = new ArrayList();
		boolean valueFound = false;
		List parameterValuesDescription = new ArrayList();
		// get lov result handler
		LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
		List values = biparam.getParameterValues();
		if (values != null && !values.isEmpty()) {
			for (int i = 0; i < values.size(); i++) {
				// String value = values.get(i).toString();
				String value = null;
				String val = values.get(i).toString();
				if (val.equalsIgnoreCase("%")) {
					value = "%";
				} else {
					value = URLDecoder.decode(val, UTF_8.name());
				}
				String description = null;
				if (value.equals("")) {
					valueFound = true;
					// } else if (lovProvDet.getLovType().equals(TREE_INNER_LOV_TYPE)) {
				} else if (!lovProvDet.isSimpleLovType()) {
					// List<String> treeColumns = lovProvDet.getTreeLevelsColumns();
					List<Couple<String, String>> treeColumns = lovProvDet.getTreeLevelsColumns();
					if (treeColumns != null) {
						// for (int j = 0; j < treeColumns.size(); j++) {
						// valueFound = lovResultHandler.containsValueForTree(value, treeColumns.get(j));
						// if (valueFound) {
						// break;
						// }
						// }
						Iterator<Couple<String, String>> it = treeColumns.iterator();
						while (it.hasNext()) {
							Couple<String, String> entry = it.next();
							valueFound = lovResultHandler.containsValue(value, entry.getFirst());
							if (valueFound) {
								description = lovResultHandler.getValueDescription(value, entry.getFirst(),
										entry.getSecond());
								break;
							}
						}
					}
				} else if (lovResultHandler.containsValue(value, lovProvDet.getValueColumnName())) {
					valueFound = true;
				}
				// if (valueFound) {
				// description = lovResultHandler.getValueDescription(value, lovProvDet.getValueColumnName(), lovProvDet.getDescriptionColumnName());
				// } else {
				// logger.error("Parameter '" + biparam.getLabel() + "' cannot assume value '" + value + "'" + " for user '"
				// + ((UserProfile) this.userProfile).getUserId().toString() + "' with role '" + this.executionRole + "'.");
				// List l = new ArrayList();
				// l.add(biparam.getLabel());
				// l.add(value);
				// EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 1077, l);
				// toReturn.add(userError);
				// description = "NOT ADMISSIBLE";
				// }
				if (!valueFound) {
					LOGGER.error("Parameter '{}' cannot assume value '{}' for user '{}' with role '{}'.",
							biparam.getLabel(), value, ((UserProfile) this.userProfile).getUserId(),
							this.executionRole);
					List l = new ArrayList();
					l.add(biparam.getLabel());
					l.add(value);
					EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 1077, l);
					toReturn.add(userError);
					description = "NOT ADMISSIBLE";
				}
				parameterValuesDescription.add(description);
			}
		}
		biparam.setParameterValuesDescription(parameterValuesDescription);
		LOGGER.debug("End getting validation errors on values by LOV result");
		return toReturn;
	}

	public void eraseParametersValues() {
		LOGGER.debug("IN");
		List<BIObjectParameter> biparams = object.getDrivers();
		Iterator<BIObjectParameter> iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			BIObjectParameter biparam = iterParams.next();
			biparam.setParameterValues(new ArrayList());
			biparam.setParameterValuesDescription(new ArrayList());
			biparam.setHasValidValues(false);
			List values = biparam.getParameterValues();
			if ((values == null) || (values.isEmpty())) {
				ArrayList paramvalues = new ArrayList();
				paramvalues.add("");
				biparam.setParameterValues(paramvalues);
			}
		}
		LOGGER.debug("OUT");
	}

	public String getSnapshotUrl() {
		LOGGER.debug("Getting snapshot URL");
		if (this.snapshot == null) {
			throw new SpagoBIServiceException("", "no snapshot set");
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(GeneralUtilities.getSpagoBIProfileBaseUrl(this.userProfile.getUserUniqueIdentifier().toString()));
		buffer.append("&ACTION_NAME=GET_SNAPSHOT_CONTENT");
		buffer.append("&" + SpagoBIConstants.SNAPSHOT_ID + "=" + snapshot.getId());
		buffer.append("&" + ObjectsTreeConstants.OBJECT_ID + "=" + object.getId());
		buffer.append("&" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE");

		String url = buffer.toString();
		LOGGER.debug("End getting snapshot URL: returning url {}", url);
		return url;
	}

	public String getSubObjectUrl(Locale locale) {
		LOGGER.debug("Getting sub object URL for locale {}", locale);
		if (this.subObject == null) {
			throw new SpagoBIServiceException("", "no subobject set");
		}
		String url = null;
		Engine engine = this.getBIObject().getEngine();
		Domain engineType;
		try {
			engineType = DAOFactory.getDomainDAO().loadDomainById(engine.getEngineTypeId());
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("Impossible to load engine type domain", e);
		}

		// IF THE ENGINE IS EXTERNAL
		if ("EXT".equalsIgnoreCase(engineType.getValueCd())) {
			// instance the driver class
			String driverClassName = engine.getDriverName();
			IEngineDriver aEngineDriver = null;
			try {
				aEngineDriver = (IEngineDriver) Class.forName(driverClassName).newInstance();
			} catch (Exception e) {
				throw new SpagoBIServiceException("Cannot istantiate engine driver class: " + driverClassName, e);
			}
			// get the map of the parameters
			Map mapPars = aEngineDriver.getParameterMap(object, this.subObject, userProfile, executionRole);
			// adding "system" parameters
			addSystemParametersForExternalEngines(mapPars, locale);

			url = GeneralUtilities.getUrl(engine.getUrl(), mapPars);
		} else {
			throw new RuntimeException("Internal engines does not support subobjects!!");
		}
		LOGGER.debug("End getting sub object URL for locale: returning URL {}", url);
		return url;
	}

	// Auditing
	private Integer createAuditId() {
		LOGGER.debug("IN");
		try {
			AuditManager auditManager = AuditManager.getInstance();
			Integer executionAuditId = auditManager.insertAudit(object, subObject, userProfile, executionRole,
					executionModality);
			return executionAuditId;
		} finally {
			LOGGER.debug("OUT");
		}
	}

	/**
	 * This method is called by SDK to execute a document; it takes as input a list of SDK parameters, each with its own set of values and fill the BiObject object
	 *
	 * @param obj        The Bi Object
	 * @param parameters an array of SDKDocumentParameter
	 */

	public void refreshBIObjectWithSDKParameters(SDKDocumentParameter[] parameters) {

		LOGGER.debug("IN");
		List<BIObjectParameter> listPars = object.getDrivers();

		HashMap<String, List<Object>> parametersMap = new HashMap<>();

		// create an hashmap of parameters
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				SDKDocumentParameter docParameter = parameters[i];
				List<Object> valuesToInsert = new ArrayList<>();

				for (int j = 0; j < docParameter.getValues().length; j++) {
					Object ob = docParameter.getValues()[j];
					String obString = ob.toString(); // for now I convert in string otherwise don't pass examination
					valuesToInsert.add(obString);
				}

				parametersMap.put(docParameter.getUrlName(), valuesToInsert);
			}
		}

		for (Iterator iterator = listPars.iterator(); iterator.hasNext();) {
			BIObjectParameter objectParameter = (BIObjectParameter) iterator.next();
			List<Object> listVals = parametersMap.get(objectParameter.getParameterUrlName());
			objectParameter.setParameterValues(listVals);
		}

		object.setDrivers(listPars);
		LOGGER.debug("OUT");
	}

	public ILovDetail getLovDetail(BIObjectParameter parameter) {
		Parameter par = parameter.getParameter();
		ModalitiesValue lov = par.getModalityValue();
		// build the ILovDetail object associated to the lov
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = null;
		try {
			lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get lov detail associated to input BIObjectParameter", e);
		}
		return lovProvDet;
	}

	public ILovDetail getLovDetailForDefault(BIObjectParameter parameter) {
		Parameter par = parameter.getParameter();
		ModalitiesValue lov = par.getModalityValueForDefault();
		if (lov == null) {
			LOGGER.debug("No LOV for default values defined");
			return null;
		}
		LOGGER.debug("A LOV for default values is defined: {}", lov);
		// build the ILovDetail object associated to the lov
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = null;
		try {
			lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(
					"Impossible to get LOV detail associated to the analytical driver for default values", e);
		}
		return lovProvDet;
	}

	/**
	 * Get lov detail for max.
	 *
	 * @param parameter
	 * @return
	 * @author Marco Libanori
	 */
	public ILovDetail getLovDetailForMax(BIObjectParameter parameter) {
		Parameter par = parameter.getParameter();
		ModalitiesValue lov = par.getModalityValueForMax();
		if (lov == null) {
			LOGGER.debug("No LOV for max value defined");
			return null;
		}
		LOGGER.debug("A LOV for max value is defined: {}", lov);
		// build the ILovDetail object associated to the lov
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = null;
		try {
			lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(
					"Impossible to get LOV detail associated to the analytical driver for max value", e);
		}
		return lovProvDet;
	}

	public List<ObjParuse> getDependencies(BIObjectParameter parameter) {
		List<ObjParuse> biParameterExecDependencies = new ArrayList<>();
		try {
			IParameterUseDAO parusedao = DAOFactory.getParameterUseDAO();
			ParameterUse biParameterExecModality = parusedao.loadByParameterIdandRole(parameter.getParID(),
					executionRole);
			IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
			biParameterExecDependencies
					.addAll(objParuseDAO.loadObjParuse(parameter.getId(), biParameterExecModality.getUseID()));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get dependencies", e);
		}
		return biParameterExecDependencies;
	}

	public String getExecutionUrl(Locale locale) {
		LOGGER.debug("Getting execution URL for locale {}", locale);
		String url = null;
		Engine engine = this.getBIObject().getEngine();
		Domain engineType;
		try {
			engineType = DAOFactory.getDomainDAO().loadDomainById(engine.getEngineTypeId());
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("Impossible to load engine type domain", e);
		}

		// IF THE ENGINE IS EXTERNAL
		if ("EXT".equalsIgnoreCase(engineType.getValueCd())) {
			// instance the driver class
			String driverClassName = engine.getDriverName();
			IEngineDriver aEngineDriver = null;
			try {
				aEngineDriver = (IEngineDriver) Class.forName(driverClassName).newInstance();
			} catch (Exception e) {
				throw new SpagoBIServiceException("Cannot istantiate engine driver class: " + driverClassName, e);
			}
			// get the map of the parameters
			Map mapPars = aEngineDriver.getParameterMap(object, userProfile, executionRole);
			// adding "system" parameters
			addSystemParametersForExternalEngines(mapPars, locale);

			url = GeneralUtilities.getUrl(engine.getUrl(), mapPars);

		}
		// IF THE ENGINE IS INTERNAL
		else {
			StringBuilder buffer = new StringBuilder();
			buffer.append(
					GeneralUtilities.getSpagoBIProfileBaseUrl(((UserProfile) userProfile).getUserId().toString()));
			buffer.append("&PAGE=ExecuteBIObjectPage");
			buffer.append("&" + SpagoBIConstants.TITLE_VISIBLE + "=FALSE");
			buffer.append("&" + SpagoBIConstants.TOOLBAR_VISIBLE + "=FALSE");
			buffer.append("&" + ObjectsTreeConstants.OBJECT_LABEL + "=" + object.getLabel());
			buffer.append("&" + SpagoBIConstants.ROLE + "=" + executionRole);
			buffer.append("&" + SpagoBIConstants.RUN_ANYWAY + "=TRUE");
			buffer.append("&" + SpagoBIConstants.IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS + "=TRUE");
			buffer.append("&SBI_EXECUTION_ID=" + this.executionId); // adds constants if it works!!

			String kpiClassName = KpiDriver.class.getCanonicalName();
			if (engine.getClassName().equals(kpiClassName)) {
				Integer auditId = createAuditId();
				if (auditId != null) {
					buffer.append("&" + AuditManager.AUDIT_ID + "=" + auditId); // adds constants if it works!!
				}
			}

			// identity string for context
			UUID uuid = UUID.randomUUID();
			buffer.append("&" + LightNavigationConstants.LIGHT_NAVIGATOR_ID + "=" + uuid.toString());

			List<BIObjectParameter> parameters = object.getDrivers();
			if (parameters != null && !parameters.isEmpty()) {
				Iterator<BIObjectParameter> it = parameters.iterator();
				while (it.hasNext()) {
					BIObjectParameter aParameter = it.next();

					List list = aParameter.getParameterValues();
					if (list != null && !list.isEmpty()) {
						Iterator r = list.iterator();
						while (r.hasNext()) {
							String value = (String) r.next();
							if (value != null && !value.equals("")) {
								// encoding value
								try {
									value = URLEncoder.encode(value, UTF_8.name());
								} catch (UnsupportedEncodingException e) {
									LOGGER.warn("UTF-8 encoding is not supported!!!", e);
									LOGGER.warn("Using system encoding...");
									value = URLEncoder.encode(value);
								}
								buffer.append("&" + aParameter.getParameterUrlName() + "=" + value);
							}
						}
					}
					/*
					 * ParameterValuesEncoder encoder = new ParameterValuesEncoder(); String encodedValue = encoder.encode(aParameter); if(encodedValue!=null &&
					 * !encodedValue.equals("")){ buffer.append("&" + aParameter.getParameterUrlName() + "=" + encodedValue); }
					 */
				}
			}
			url = buffer.toString();
		}

		LOGGER.debug("End getting execution URL for locale: returning url {}", url);
		return url;
	}

	private void addSystemParametersForExternalEngines(Map mapPars, Locale locale) {
		mapPars.put("SBI_EXECUTION_ID", this.executionId);
		mapPars.put(SpagoBIConstants.EXECUTION_ROLE, this.getExecutionRole());
		Integer auditId = createAuditId();
		if (auditId != null) {
			mapPars.put(AuditManager.AUDIT_ID, auditId);
		}
		if (locale != null) {
			if (locale.getLanguage() != null) {
				mapPars.put(SpagoBIConstants.SBI_LANGUAGE, locale.getLanguage());
			}
			if (locale.getCountry() != null) {
				mapPars.put(SpagoBIConstants.SBI_COUNTRY, locale.getCountry());
			}
			if (StringUtils.isNotBlank(locale.getScript())) {
				mapPars.put(SpagoBIConstants.SBI_SCRIPT, locale.getScript());
			}
		}
	}

	/**
	 * Gets the execution id.
	 *
	 * @return the execution id
	 */
	public String getExecutionId() {
		return executionId;
	}

	/**
	 * Gets the flow id.
	 *
	 * @return the flow id
	 */
	public String getFlowId() {
		return flowId;
	}

	/**
	 * Gets the bI object.
	 *
	 * @return the bI object
	 */
	public BIObject getBIObject() {
		return object;
	}

	/**
	 * Gets the calendar.
	 *
	 * @return the calendar
	 */
	public Calendar getCalendar() {
		return calendar;
	}

	/**
	 * Gets the current execution role.
	 *
	 * @return the execution role
	 */
	public String getExecutionRole() {
		return executionRole;
	}

	/**
	 * Gets the execution modality.
	 *
	 * @return the execution modality
	 */
	public String getExecutionModality() {
		return executionModality;
	}

	public boolean displayToolbar() {
		return displayToolbar;
	}

	public void setDisplayToolbar(boolean displayToolbar) {
		this.displayToolbar = displayToolbar;
	}

	public boolean displaySliders() {
		return displaySliders;
	}

	public void setDisplaySliders(boolean displaySliders) {
		this.displaySliders = displaySliders;
	}

	public SubObject getSubObject() {
		return subObject;
	}

	public void setSubObject(SubObject subObject) {
		this.subObject = subObject;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object another) {
		if (another instanceof ExecutionInstance) {

			ExecutionInstance anInstance = (ExecutionInstance) another;
			return this.executionId.equals(anInstance.executionId);
		} else
			return false;
	}

	public Locale getLocale() {
		return this.locale;
	}

}
