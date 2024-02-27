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
package it.eng.spagobi.analiticalmodel.execution.service;

import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_OPTIONS_KEY;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_QUANTITY_JSON;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_TYPE_JSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.lov.bo.DependenciesPostProcessingLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.JSONStoreFeedTransformer;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.commons.utilities.DateRangeDAOUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetParameterValuesForExecutionAction extends AbstractSpagoBIAction {

	public static final String SERVICE_NAME = "GET_PARAMETERS_FOR_EXECUTION_SERVICE";

	private static final String DESCRIPTION_FIELD = "description";

	private static final String VALUE_FIELD = "value";

	private static final String LABEL_FIELD = "label";

	// request parameters
	public static final String PARAMETER_ID = "PARAMETER_ID";
	public static final String SELECTED_PARAMETER_VALUES = "PARAMETERS";
	public static final String FILTERS = "FILTERS";
	public static final String NODE_ID_SEPARATOR = "___SEPA__";

	public static final String MODE = "MODE";
	public static final String NODE = "node";
	public static final String MODE_SIMPLE = "simple";
	public static final String MODE_COMPLETE = "complete";
	public static final String START = "start";
	public static final String LIMIT = "limit";
	// in massive export case
	public static final String OBJ_PARAMETER_IDS = "OBJ_PARAMETER_IDS";
	public static final String CONTEST = "CONTEST"; // used to check if mssive export
	// case; cannot use MODALITY
	// because already in use
	public static final String MASSIVE_EXPORT = "massiveExport";

	private static final String[] VISIBLE_COLUMNS = new String[] { VALUE_FIELD, LABEL_FIELD, DESCRIPTION_FIELD };

	// logger component
	private static Logger logger = Logger.getLogger(GetParameterValuesForExecutionAction.class);

	@Override
	public void doService() {

		String biparameterId;
		JSONObject selectedParameterValuesJSON;
		JSONObject filtersJSON = null;
		Map selectedParameterValues;
		String mode;
		JSONObject valuesJSON;
		String contest;
		BIObjectParameter biObjectParameter;
		ExecutionInstance executionInstance;
		List rows;
		List<ObjParuse> biParameterExecDependencies;
		ILovDetail lovProvDet;
		List objParameterIds;
		int treeLovNodeLevel = 0;
		String treeLovNodeValue = null;

		logger.debug("IN");

		try {

			biparameterId = getAttributeAsString(PARAMETER_ID);
			selectedParameterValuesJSON = getAttributeAsJSONObject(SELECTED_PARAMETER_VALUES);
			if (this.requestContainsAttribute(FILTERS)) {
				filtersJSON = getAttributeAsJSONObject(FILTERS);
			}

			mode = getAttributeAsString(MODE);
			try {
				treeLovNodeValue = getAttributeAsString(NODE);
				if (treeLovNodeValue.contains("lovroot")) {
					treeLovNodeValue = "lovroot";
					treeLovNodeLevel = 0;
				} else {
					String[] splittedNode = treeLovNodeValue.split(NODE_ID_SEPARATOR);
					treeLovNodeValue = splittedNode[0];
					treeLovNodeLevel = new Integer(splittedNode[1]);
				}

			} catch (NullPointerException e) {
				logger.debug("there is no tree attribute for the Parameter [" + PARAMETER_ID + "]");
			}

			objParameterIds = getAttributeAsList(OBJ_PARAMETER_IDS);

			contest = getAttributeAsString(CONTEST);

			logger.debug("Parameter [" + PARAMETER_ID + "] is equals to [" + biparameterId + "]");
			logger.debug("Parameter [" + MODE + "] is equals to [" + mode + "]");

			logger.debug("Parameter [" + CONTEST + "] is equals to [" + contest + "]");

			if (mode == null) {
				mode = MODE_SIMPLE;
			}

			Assert.assertNotNull(getContext(), "Parameter [" + PARAMETER_ID + "] cannot be null");
			Assert.assertNotNull(getContext(), "Execution context cannot be null");
			Assert.assertNotNull(getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName()),
					"Execution instance cannot be null");

			boolean isAMap = getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName());
			executionInstance = null;
			if (!isAMap) {
				executionInstance = getContext().getExecutionInstance(ExecutionInstance.class.getName());
			} else {
				Map<Integer, ExecutionInstance> instances = getContext()
						.getExecutionInstancesAsMap(ExecutionInstance.class.getName());
				// I want to get (at least one) of the document the parameter is
				// referring to,
				// I can reach it via the ObjectParameter passed from
				// ParametersPanel
				Integer biObjectId = null;
				Assert.assertNotNull(objParameterIds, "In map case objParameterids list cannot be null");
				if (objParameterIds.isEmpty()) {
					throw new SpagoBIServiceException("In map case objParameterids list cannot be empty", SERVICE_NAME);

				}
				Integer objParId = Integer.valueOf(objParameterIds.get(0).toString());
				try {
					BIObjectParameter biObjPar = DAOFactory.getBIObjectParameterDAO().loadBiObjParameterById(objParId);
					biObjectId = biObjPar.getBiObjectID();
				} catch (HibernateException e) {
					throw new SpagoBIServiceException("Could not recover document", e);
				}
				executionInstance = instances.get(biObjectId);

			}
			if (selectedParameterValuesJSON != null) {
				executionInstance.refreshParametersValues(selectedParameterValuesJSON, false);
			}

			BIObject obj = executionInstance.getBIObject();

			// START converts JSON object with document's parameters into an
			// hashmap
			selectedParameterValues = null;
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
						} else {
							Assert.assertUnreachable("Attribute [" + key + "] value [" + v
									+ "] of PARAMETERS is not of type JSONArray nor String. It is of type ["
									+ v.getClass().getName() + "]");
						}
					}
				} catch (JSONException e) {
					throw new SpagoBIServiceException("parameter JSONObject is malformed", e);
				}
			}
			// END converts JSON object with document's parameters into an
			// hashmap

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
			try {
				Parameter parameter = biObjectParameter.getParameter();
				if (DateRangeDAOUtilities.isDateRange(parameter)) {
					manageDataRange(biObjectParameter, executionInstance);
					return;
				}
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Error on loading date range combobox values", e);
			}

			lovProvDet = executionInstance.getLovDetail(biObjectParameter);

			// START get the lov result
			String lovResult = null;
			try {
				// get the result of the lov
				IEngUserProfile profile = getUserProfile();

				// get from cache, if available
				LovResultCacheManager executionCacheManager = new LovResultCacheManager();
				lovResult = executionCacheManager.getLovResult(profile, lovProvDet,
						executionInstance.getDependencies(biObjectParameter), executionInstance, true);

				// get all the rows of the result
				LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
				rows = lovResultHandler.getRows();

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
					rows = DelegatedBasicListService.filterList(rows, valuefilter, typeValueFilter, columnfilter,
							typeFilter);
				}
			} catch (JSONException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to read filter's configuration", e);
			}
			// END filtering the list by filtering toolbar

			// START filtering for correlation (only for
			// DependenciesPostProcessingLov, i.e. scripts, java classes and
			// fixed lists)
			biParameterExecDependencies = executionInstance.getDependencies(biObjectParameter);
			if (lovProvDet instanceof DependenciesPostProcessingLov && selectedParameterValues != null
					&& biParameterExecDependencies != null && !biParameterExecDependencies.isEmpty()
					&& !contest.equals(MASSIVE_EXPORT)) {
				rows = ((DependenciesPostProcessingLov) lovProvDet).processDependencies(rows, selectedParameterValues,
						biParameterExecDependencies);
			}
			// END filtering for correlation

			if (lovProvDet.getLovType() != null && lovProvDet.getLovType().contains("tree")) {
				JSONArray valuesJSONArray = getChildrenForTreeLov(lovProvDet, rows, mode, treeLovNodeLevel,
						treeLovNodeValue);
				try {
					writeBackToClient(new JSONSuccess(valuesJSONArray));
				} catch (IOException e) {
					throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
				}
			} else {

				valuesJSON = buildJSONForLOV(lovProvDet, rows, mode);
				try {
					writeBackToClient(new JSONSuccess(valuesJSON));
				} catch (IOException e) {
					throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
				}
			}

		} finally {
			logger.debug("OUT");
		}

	}

	private void manageDataRange(BIObjectParameter biObjectParameter, ExecutionInstance executionInstance)
			throws EMFUserError, SerializationException, JSONException, IOException {
		Integer parID = biObjectParameter.getParID();
		Assert.assertNotNull(parID, "parID");
		String executionRole = executionInstance.getExecutionRole();
		ParameterUse param = DAOFactory.getParameterUseDAO().loadByParameterIdandRole(parID, executionRole);
		String options = param.getOptions();
		Assert.assertNotNull(options, "options");

		JSONArray dateRangeValuesDataJSON = getDateRangeValuesDataJSON(options);
		int dataRangeOptionsSize = getDataRangeOptionsSize(options);
		JSONObject valuesJSON = (JSONObject) JSONStoreFeedTransformer.getInstance().transform(dateRangeValuesDataJSON,
				VALUE_FIELD.toUpperCase(), LABEL_FIELD.toUpperCase(), DESCRIPTION_FIELD.toUpperCase(), VISIBLE_COLUMNS,
				dataRangeOptionsSize);
		writeBackToClient(new JSONSuccess(valuesJSON));
	}

	private static int getDataRangeOptionsSize(String options) throws JSONException {
		JSONObject json = new JSONObject(options);
		JSONArray res = json.getJSONArray(DATE_RANGE_OPTIONS_KEY);
		return res.length();
	}

	private JSONArray getDateRangeValuesDataJSON(String optionsJson) throws JSONException {
		JSONObject json = new JSONObject(optionsJson);
		JSONArray options = json.getJSONArray(DATE_RANGE_OPTIONS_KEY);
		JSONArray res = new JSONArray();
		for (int i = 0; i < options.length(); i++) {
			JSONObject opt = new JSONObject();
			JSONObject optJson = (JSONObject) options.get(i);
			String type = (String) optJson.get(DATE_RANGE_TYPE_JSON);
			String typeDesc = getLocalizedMessage("SBIDev.paramUse." + type);
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

	private String getLocalizedMessage(String code) {
		return MessageBuilderFactory.getMessageBuilder().getMessage(code, getHttpRequest());
	}

	private JSONArray getChildrenForTreeLov(ILovDetail lovProvDet, List rows, String mode, int treeLovNodeLevel,
			String treeLovNodeValue) {
		String valueColumn;
		String descriptionColumn;
		boolean addNode;
		String treeLovNodeName = "";
		String treeLovParentNodeName = "";

		try {

			if (treeLovNodeValue == "lovroot") {// root node
				treeLovNodeName = lovProvDet.getTreeLevelsColumns().get(0).getFirst();
				treeLovParentNodeName = "lovroot";
				treeLovNodeLevel = -1;
			} else if (lovProvDet.getTreeLevelsColumns().size() > treeLovNodeLevel + 1) {
				// treeLovNodeLevel-1 because the fake root node is the level 0
				treeLovNodeName = lovProvDet.getTreeLevelsColumns().get(treeLovNodeLevel + 1).getFirst();
				treeLovParentNodeName = lovProvDet.getTreeLevelsColumns().get(treeLovNodeLevel).getFirst();
			}

			Set<JSONObject> valuesDataJSON = new LinkedHashSet<>();

			valueColumn = lovProvDet.getValueColumnName();
			descriptionColumn = lovProvDet.getDescriptionColumnName();

			for (int q = 0; q < rows.size(); q++) {
				SourceBean row = (SourceBean) rows.get(q);
				JSONObject valueJSON = null;
				addNode = false;
				List columns = row.getContainedAttributes();
				valueJSON = new JSONObject();
				boolean notNullNode = false; // if the row does not contain the
											 // value atribute we don't add
											 // the node
				for (int i = 0; i < columns.size(); i++) {
					SourceBeanAttribute attribute = (SourceBeanAttribute) columns.get(i);
					if ((treeLovParentNodeName == "lovroot")
							|| (attribute.getKey().equalsIgnoreCase(treeLovParentNodeName)
									&& (attribute.getValue().toString()).equalsIgnoreCase(treeLovNodeValue))) {
						addNode = true;
					}

					// its a leaf so we take the value and description defined
					// in the lov definition
					if (lovProvDet.getTreeLevelsColumns().size() == treeLovNodeLevel + 2) {
						if (attribute.getKey().equalsIgnoreCase(descriptionColumn)) {// its
																						// the
																						// column
																						// of
																						// the
																						// description
							valueJSON.put("description", attribute.getValue());
							notNullNode = true;
						}
						if (attribute.getKey().equalsIgnoreCase(valueColumn)) {// its
																				// the
																				// column
																				// of
																				// the
																				// value
							valueJSON.put("value", attribute.getValue());
							valueJSON.put("id", attribute.getValue() + NODE_ID_SEPARATOR + (treeLovNodeLevel + 1));
							notNullNode = true;
						}
						valueJSON.put("leaf", true);
					} else if (attribute.getKey().equalsIgnoreCase(treeLovNodeName)) {
						valueJSON = new JSONObject();
						valueJSON.put("description", attribute.getValue());
						valueJSON.put("value", attribute.getValue());
						valueJSON.put("id", attribute.getValue() + NODE_ID_SEPARATOR + (treeLovNodeLevel + 1));
						notNullNode = true;
					}
				}
				// if(lovProvDet.getLovType().equals("treeinner")){
				// valueJSON.put("checked",false);
				// }
				//
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

	private JSONObject buildJSONForLOV(ILovDetail lovProvDet, List rows, String mode) {
		String valueColumn;
		String descriptionColumn;
		JSONObject valuesJSON;
		Integer start;
		Integer limit;
		String displayColumn;

		// START building JSON object to be returned
		try {
			JSONArray valuesDataJSON = new JSONArray();

			valueColumn = lovProvDet.getValueColumnName();
			displayColumn = lovProvDet.getDescriptionColumnName();
			descriptionColumn = displayColumn;

			start = getAttributeAsInteger(START);
			limit = getAttributeAsInteger(LIMIT);

			logger.debug("Parameter [" + START + "] is equals to [" + start + "]");
			logger.debug("Parameter [" + LIMIT + "] is equals to [" + limit + "]");

			int lb = (start != null) ? start.intValue() : 0;
			int ub = (limit != null) ? lb + limit.intValue() : rows.size() - lb;
			ub = (ub > rows.size()) ? rows.size() : ub;

			for (int q = lb; q < ub; q++) {
				SourceBean row = (SourceBean) rows.get(q);
				JSONObject valueJSON = new JSONObject();

				if (MODE_COMPLETE.equalsIgnoreCase(mode)) {
					List columns = row.getContainedAttributes();
					for (int i = 0; i < columns.size(); i++) {
						SourceBeanAttribute attribute = (SourceBeanAttribute) columns.get(i);
						valueJSON.put(attribute.getKey().toUpperCase(), attribute.getValue());
					}
				} else {
					String value = (String) row.getAttribute(valueColumn);
					String description = (String) row.getAttribute(descriptionColumn);
					valueJSON.put("value", value);
					valueJSON.put("label", description);
					valueJSON.put("description", description);
				}

				valuesDataJSON.put(valueJSON);
			}

			String[] visiblecolumns;

			if (MODE_COMPLETE.equalsIgnoreCase(mode)) {
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

			valuesJSON = (JSONObject) JSONStoreFeedTransformer.getInstance().transform(valuesDataJSON,
					valueColumn.toUpperCase(), displayColumn.toUpperCase(), descriptionColumn.toUpperCase(),
					visiblecolumns, rows.size());
			return valuesJSON;
		} catch (Exception e) {
			throw new SpagoBIServiceException("Impossible to serialize response", e);
		}
		// END building JSON object to be returned

	}
}
