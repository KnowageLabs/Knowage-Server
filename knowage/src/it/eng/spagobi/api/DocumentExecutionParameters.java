package it.eng.spagobi.api;

import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_OPTIONS_KEY;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_QUANTITY_JSON;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_TYPE_JSON;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentUrlManager;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.lov.bo.DependenciesPostProcessingLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.JSONStoreFeedTransformer;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

@Path("/1.0/documentExeParameters")
@ManageAuthorization
public class DocumentExecutionParameters extends AbstractSpagoBIResource {

	public static final String SERVICE_NAME = "GET DOCUMENT PARAMETERS ";

	// request parameters
	public static String PARAMETER_ID = "PARAMETER_ID";
	public static String SELECTED_PARAMETER_VALUES = "PARAMETERS";
	public static String FILTERS = "FILTERS";
	public static String NODE_ID_SEPARATOR = "___SEPA__";

	public static String MODE = "MODE";
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
	private static final String ROLE = "ROLE";
	private static final String OBJECT_LABEL = "OBJECT_LABEL";

	private static final String DESCRIPTION_FIELD = "description";

	private static final String VALUE_FIELD = "value";

	private static final String LABEL_FIELD = "label";

	private static IMessageBuilder message = MessageBuilderFactory.getMessageBuilder();

	private static final String[] VISIBLE_COLUMNS = new String[] { VALUE_FIELD, LABEL_FIELD, DESCRIPTION_FIELD };

	static protected Logger logger = Logger.getLogger(DocumentExecutionParameters.class);

	@POST
	@Path("/getParameters")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getParameters(@Context HttpServletRequest req) {
		// HashMap<String, Object> resultAsMap = new HashMap<String, Object>();

		String result = "";

		String biparameterId;
		JSONObject selectedParameterValuesJSON;
		JSONObject filtersJSON = null;
		Map selectedParameterValues;
		String mode;
		JSONObject valuesJSON;
		// String contest;
		BIObjectParameter biObjectParameter;
		// ExecutionInstance executionInstance;

		List rows;
		List<ObjParuse> biParameterExecDependencies;
		ILovDetail lovProvDet;

		List objParameterIds;
		int treeLovNodeLevel = 0;
		String treeLovNodeValue = null;
		DocumentUrlManager dum = new DocumentUrlManager(this.getUserProfile(), req.getLocale());
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
			selectedParameterValuesJSON = (JSONObject) requestVal.opt(SELECTED_PARAMETER_VALUES);
			if (requestVal.opt(FILTERS) != null) {
				filtersJSON = (JSONObject) requestVal.opt(FILTERS);
			}
			mode = (requestVal.opt(MODE) == null) ? MODE_SIMPLE : (String) requestVal.opt(MODE);
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

			BIObject obj;
			try {
				obj = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByLabelAndRole(label, role);
				if (selectedParameterValuesJSON != null) {
					dum.refreshParametersValues(selectedParameterValuesJSON, false, obj);
				}

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
				// END converts JSON object with document's parameters into an
				// hashmap
				// START get the relevant biobject parameter
				biObjectParameter = null;
				List parameters = obj.getBiObjectParameters();
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
					JSONArray valuesJSONArray = getChildrenForTreeLov(lovProvDet, rows, mode, treeLovNodeLevel, treeLovNodeValue);
					result = buildJsonResult("OK", "", null, valuesJSONArray, biparameterId).toString();
				} else {
					valuesJSON = buildJSONForLOV(lovProvDet, rows, mode, start, limit);
					result = buildJsonResult("OK", "", valuesJSON, null, biparameterId).toString();
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

	private JSONObject buildJsonResult(String status, String error, JSONObject obj, JSONArray objArr, String biparameterId) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("status", status);
			jsonObj.put("error", error);
			jsonObj.put("idParam", biparameterId);
			if (objArr != null) {
				jsonObj.put("result", objArr);
			}
			if (obj != null) {
				jsonObj.put("result", obj);
			}
		} catch (JSONException e) {
			logger.error("Error build Json Result Document Execution Parameter : " + e.getMessage());
		}

		return jsonObj;
	}

	private JSONArray getChildrenForTreeLov(ILovDetail lovProvDet, List rows, String mode, int treeLovNodeLevel, String treeLovNodeValue) {
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
			} else if (lovProvDet.getTreeLevelsColumns().size() > treeLovNodeLevel + 1) {// treeLovNodeLevel-1
																							// because
																							// the
																							// fake
																							// root
																							// node
																							// is
																							// the
																							// level
																							// 0

				treeLovNodeName = lovProvDet.getTreeLevelsColumns().get(treeLovNodeLevel + 1).getFirst();
				treeLovParentNodeName = lovProvDet.getTreeLevelsColumns().get(treeLovNodeLevel).getFirst();
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
				boolean notNullNode = false; // if the row does not contain the
												// value atribute we don't add
												// the node
				for (int i = 0; i < columns.size(); i++) {
					SourceBeanAttribute attribute = (SourceBeanAttribute) columns.get(i);
					if ((treeLovParentNodeName == "lovroot")
							|| (attribute.getKey().equalsIgnoreCase(treeLovParentNodeName) && (attribute.getValue().toString())
									.equalsIgnoreCase(treeLovNodeValue))) {
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

	private JSONObject buildJSONForLOV(ILovDetail lovProvDet, List rows, String mode, Integer start, Integer limit) {
		String valueColumn;
		String descriptionColumn;
		JSONObject valuesJSON;
		String displayColumn;

		// START building JSON object to be returned
		try {
			JSONArray valuesDataJSON = new JSONArray();

			valueColumn = lovProvDet.getValueColumnName();
			displayColumn = lovProvDet.getDescriptionColumnName();
			descriptionColumn = displayColumn;

			int lb = (start != null) ? start.intValue() : 0;
			int ub = (limit != null) ? lb + limit.intValue() : rows.size() - lb;
			ub = (ub > rows.size()) ? rows.size() : ub;

			for (int q = lb; q < ub; q++) {
				SourceBean row = (SourceBean) rows.get(q);
				JSONObject valueJSON = new JSONObject();

				if (MODE_EXTRA.equalsIgnoreCase(mode)) {
					List columns = row.getContainedAttributes();
					String value = (String) row.getAttribute(valueColumn);
					String description = (String) row.getAttribute(descriptionColumn);
					for (int i = 0; i < columns.size(); i++) {
						SourceBeanAttribute attribute = (SourceBeanAttribute) columns.get(i);
						valueJSON.put(attribute.getKey().toUpperCase(), attribute.getValue());
						valueJSON.put("value", value);
						valueJSON.put("label", description);
						valueJSON.put("description", description);
					}
				} else if (MODE_COMPLETE.equalsIgnoreCase(mode)) {
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

			valuesJSON = (JSONObject) JSONStoreFeedTransformer.getInstance().transform(valuesDataJSON, valueColumn.toUpperCase(), displayColumn.toUpperCase(),
					descriptionColumn.toUpperCase(), visiblecolumns, new Integer(rows.size()));
			return valuesJSON;
		} catch (Exception e) {
			throw new SpagoBIServiceException("Impossible to serialize response", e);
		}
		// END building JSON object to be returned

	}

	/*
	 * DATE RANGE
	 */

	private JSONObject manageDataRange(BIObjectParameter biObjectParameter, String executionRole, HttpServletRequest req) throws EMFUserError,
			SerializationException, JSONException, IOException {
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
