package it.eng.spagobi.api;

import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_OPTIONS_KEY;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_QUANTITY_JSON;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_TYPE_JSON;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.RequestContainerAccess;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.BusinessModelOpenUtils;
import it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelDriverRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.DependenciesPostProcessingLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.exceptions.MissingLOVDependencyException;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.JSONStoreFeedTransformer;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.commons.utilities.DateRangeDAOUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/businessModelOpening")
public class BusinessModelOpenParameters extends AbstractSpagoBIResource {

	public static final String SERVICE_NAME = "GET BUSINESS MODEL PARAMETERS ";

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

	private static final String ROLE = "ROLE";
	private static final String OBJECT_LABEL = "OBJECT_LABEL";
	private static final String OBJECT_NAME = "OBJECT_NAME";
	private static final String DESCRIPTION_FIELD = "description";

	private static final String VALUE_FIELD = "value";

	private static final String LABEL_FIELD = "label";

	private class BusinessModelExecutionException extends Exception {
		private static final long serialVersionUID = -1882998632783944575L;

		BusinessModelExecutionException(String message) {
			super(message);
		}
	}

	@POST
	@Path("/getParameters")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getParameters(@Context HttpServletRequest req) {
		// HashMap<String, Object> resultAsMap = new HashMap<String, Object>();

		String result = "";

		String metaModelParameterId;
		JSONObject selectedParameterValuesJSON;
		JSONObject filtersJSON = null;
		Map selectedParameterValues;
		String mode;
		JSONObject valuesJSON;
		// String contest;
		BIMetaModelParameter biMetaModelParameter;
		// ExecutionInstance executionInstance;

		List rows;
		List<MetaModelParuse> biMetaModelParameterExecDependencies;
		ILovDetail lovProvDet;

		List metaModelParameterIds;
		int treeLovNodeLevel = 0;
		String treeLovNodeValue = null;
		BusinessModelRuntime dum = new BusinessModelRuntime(this.getUserProfile(), req.getLocale());
		String role;
		String name;
		Integer start = null;
		Integer limit = null;

		// PARAMETER

		try {
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
			role = (String) requestVal.opt(ROLE);
			if ((String) requestVal.opt(OBJECT_LABEL) != null) {
				name = (String) requestVal.opt(OBJECT_LABEL);
			} else {
				name = (String) requestVal.opt(OBJECT_NAME);
			}
			metaModelParameterId = (String) requestVal.opt(PARAMETER_ID);
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

			MetaModel obj;
			try {
				obj = DAOFactory.getMetaModelsDAO().loadMetaModelForExecutionByNameAndRole(name, role, false);
				if (selectedParameterValuesJSON != null) {
					dum.refreshParametersValues(selectedParameterValuesJSON, false, obj);
				}

				// START converts JSON object with business model's parameters into an
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
				// START get the relevant meta model parameter
				biMetaModelParameter = null;
				List parameters = obj.getDrivers();
				for (int i = 0; i < parameters.size(); i++) {
					BIMetaModelParameter p = (BIMetaModelParameter) parameters.get(i);
					if (metaModelParameterId.equalsIgnoreCase(p.getParameterUrlName())) {
						biMetaModelParameter = p;
						break;
					}
				}
				Assert.assertNotNull(biMetaModelParameter, "Impossible to find parameter [" + metaModelParameterId + "]");

				lovProvDet = dum.getLovDetail(biMetaModelParameter);
				// START get the lov result
				String lovResult = null;
				try {
					// get the result of the lov
					IEngUserProfile profile = getUserProfile();

					// get from cache, if available
					LovResultCacheManager executionCacheManager = new LovResultCacheManager();
					lovResult = executionCacheManager.getLovResultDum(profile, lovProvDet, dum.getDependencies(biMetaModelParameter, role), obj, true,
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
				biMetaModelParameterExecDependencies = dum.getDependencies(biMetaModelParameter, role);
				if (lovProvDet instanceof DependenciesPostProcessingLov && selectedParameterValues != null && biMetaModelParameterExecDependencies != null
						&& biMetaModelParameterExecDependencies.size() > 0) { // && contest != null && !contest.equals(MASSIVE_EXPORT)
					rows = ((DependenciesPostProcessingLov) lovProvDet).processDependencies(rows, selectedParameterValues,
							biMetaModelParameterExecDependencies);
				}
				// END filtering for correlation

				if (lovProvDet.getLovType() != null && lovProvDet.getLovType().contains("tree")) {
					JSONArray valuesJSONArray = getChildrenForTreeLov(lovProvDet, rows, mode, treeLovNodeLevel, treeLovNodeValue);
					result = buildJsonResult("OK", "", null, valuesJSONArray, metaModelParameterId).toString();
				} else {
					valuesJSON = buildJSONForLOV(lovProvDet, rows, mode, start, limit);
					result = buildJsonResult("OK", "", valuesJSON, null, metaModelParameterId).toString();
				}
			} catch (Exception e1) {
				// result = buildJsonResult("KO", e1.getMessage(), null,null).toString();
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get business model Execution Parameter EMFUserError", e1);
			}

		} catch (IOException e2) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get business model Execution Parameter IOException", e2);
		} catch (JSONException e2) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get business model Execution Parameter JSONException", e2);
		}

		// return Response.ok(resultAsMap).build();
		return result;
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
					if ((treeLovParentNodeName == "lovroot") || (attribute.getKey().equalsIgnoreCase(treeLovParentNodeName)
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

	private JSONObject buildJsonResult(String status, String error, JSONObject obj, JSONArray objArr, String metamodelparameterId) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("status", status);
			jsonObj.put("error", error);
			jsonObj.put("idParam", metamodelparameterId);
			if (objArr != null) {
				jsonObj.put("result", objArr);
			}
			if (obj != null) {
				jsonObj.put("result", obj);
			}
		} catch (JSONException e) {
			logger.error("Error build Json Result Business Model Open Parameter : " + e.getMessage());
		}

		return jsonObj;
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

			visiblecolumns = (String[]) lovProvDet.getVisibleColumnNames().toArray(new String[0]);
			for (int j = 0; j < visiblecolumns.length; j++) {
				visiblecolumns[j] = visiblecolumns[j].toUpperCase();
			}

			valuesJSON = (JSONObject) JSONStoreFeedTransformer.getInstance().transform(valuesDataJSON, valueColumn.toUpperCase(), displayColumn.toUpperCase(),
					descriptionColumn.toUpperCase(), visiblecolumns, new Integer(rows.size()));
			return valuesJSON;
		} catch (Exception e) {
			throw new SpagoBIServiceException("Impossible to serialize response", e);
		}
		// END building JSON object to be returned

	}

	private String getLocalizedMessage(String code, HttpServletRequest req) {
		return MessageBuilderFactory.getMessageBuilder().getMessage(code, req);
	}

	@POST
	@Path("/filters")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getBusinessModelExecutionFilters(@Context HttpServletRequest req)
			throws BusinessModelExecutionException, EMFUserError, IOException, JSONException {

		logger.debug("IN");

		RequestContainer aRequestContainer = RequestContainerAccess.getRequestContainer(req);
		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		// decode requestVal parameters
		// JSONObject requestValParams = requestVal.getJSONObject("parameters");
		// if (requestValParams != null && requestValParams.length() > 0)
		// requestVal.put("parameters", decodeRequestParameters(requestValParams));
		String name = requestVal.getString("name");
		String role = requestVal.getString("role");
		// JSONObject jsonCrossParameters = requestVal.getJSONObject("parameters");
		Map<String, JSONObject> sessionParametersMap = new HashMap<String, JSONObject>();
		if (("true").equals(SingletonConfig.getInstance().getConfigValue("SPAGOBI.SESSION_PARAMETERS_MANAGER.enabled")))
			sessionParametersMap = getSessionParameters(requestVal);

		// keep track of par coming from cross to get descriptions from admissible values
		// List<String> parsFromCross = new ArrayList<String>();

		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();

		IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
		IParameterUseDAO parameterUseDAO = DAOFactory.getParameterUseDAO();
		MetaModel businessModel = dao.loadMetaModelForExecutionByNameAndRole(name, role, false);

		Locale locale = GeneralUtilities.getCurrentLocale(aRequestContainer);

		// applyRequestParameters(businessModel, jsonCrossParameters, sessionParametersMap, role, locale, parsFromCross);

		ArrayList<HashMap<String, Object>> parametersArrayList = new ArrayList<>();
		BusinessModelRuntime dum = new BusinessModelRuntime(this.getUserProfile(), locale);
		List<BusinessModelDriverRuntime> parameters = BusinessModelOpenUtils.getParameters(businessModel, role, req.getLocale(), null, true, dum);
		for (BusinessModelDriverRuntime objParameter : parameters) {
			Integer paruseId = objParameter.getParameterUseId();
			ParameterUse parameterUse = parameterUseDAO.loadByUseID(paruseId);

			HashMap<String, Object> parameterAsMap = new HashMap<String, Object>();
			parameterAsMap.put("id", objParameter.getBiObjectId());
			parameterAsMap.put("label", objParameter.getLabel());
			parameterAsMap.put("urlName", objParameter.getId());
			parameterAsMap.put("type", objParameter.getParType());
			parameterAsMap.put("typeCode", objParameter.getTypeCode());
			parameterAsMap.put("selectionType", objParameter.getSelectionType());
			parameterAsMap.put("valueSelection", parameterUse.getValueSelection());
			parameterAsMap.put("selectedLayer", objParameter.getSelectedLayer());
			parameterAsMap.put("selectedLayerProp", objParameter.getSelectedLayerProp());
			parameterAsMap.put("visible", ((objParameter.isVisible())));
			parameterAsMap.put("mandatory", ((objParameter.isMandatory())));
			parameterAsMap.put("multivalue", objParameter.isMultivalue());
			parameterAsMap.put("driverLabel", objParameter.getPar().getLabel());
			parameterAsMap.put("driverUseLabel", objParameter.getAnalyticalDriverExecModality().getLabel());

			parameterAsMap.put("allowInternalNodeSelection",
					objParameter.getPar().getModalityValue().getLovProvider().contains("<LOVTYPE>treeinner</LOVTYPE>"));

			// get values
			if (objParameter.getDriver().getParameterValues() != null) {

				List paramValueLst = new ArrayList();
				List paramDescrLst = new ArrayList();
				Object paramValues = objParameter.getDriver().getParameterValues();
				Object paramDescriptionValues = objParameter.getDriver().getParameterValuesDescription();

				if (paramValues instanceof List) {

					List<String> valuesList = (List) paramValues;
					List<String> descriptionList = (List) paramDescriptionValues;
					if (paramDescriptionValues == null || !(paramDescriptionValues instanceof List)) {
						descriptionList = new ArrayList<String>();
					}

					// String item = null;
					for (int k = 0; k < valuesList.size(); k++) {

						String itemVal = valuesList.get(k);

						String itemDescr = descriptionList.size() > k && descriptionList.get(k) != null ? descriptionList.get(k) : itemVal;

						try {
							// % character breaks decode method
							if (!itemVal.contains("%")) {
								itemVal = URLDecoder.decode(itemVal, "UTF-8");
							}
							if (!itemDescr.contains("%")) {
								itemDescr = URLDecoder.decode(itemDescr, "UTF-8");
							}

							// check input value and convert if it's an old multivalue syntax({;{xxx;yyy}STRING}) to list of values :["A-OMP", "A-PO", "CL"]
							if (objParameter.isMultivalue() && itemVal.indexOf("{") >= 0) {
								String sep = itemVal.substring(1, 2);
								String val = itemVal.substring(3, itemVal.indexOf("}"));
								String[] valLst = val.split(sep);
								for (int k2 = 0; k2 < valLst.length; k2++) {
									String itemVal2 = valLst[k2];
									if (itemVal2 != null && !"".equals(itemVal2))
										paramValueLst.add(itemVal2);

								}
							} else {
								if (itemVal != null && !"".equals(itemVal))
									paramValueLst.add(itemVal);
								paramDescrLst.add(itemDescr);

							}
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
							logger.debug("An error occured while decoding parameter with value[" + itemVal + "]" + e);
						}
					}
				} else if (paramValues instanceof String) {
					// % character breaks decode method
					if (!((String) paramValues).contains("%")) {
						paramValues = URLDecoder.decode((String) paramValues, "UTF-8");
					}
					paramValueLst.add(paramValues.toString());

					String parDescrVal = paramDescriptionValues != null && paramDescriptionValues instanceof String ? paramDescriptionValues.toString()
							: paramValues.toString();
					if (!parDescrVal.contains("%")) {
						parDescrVal = URLDecoder.decode(parDescrVal, "UTF-8");
					}
					paramDescrLst.add(parDescrVal);

				}

				parameterAsMap.put("parameterValue", paramValueLst);
				parameterAsMap.put("parameterDescription", paramDescriptionValues);
			}

			boolean showParameterLov = true;

			// Parameters NO TREE
			if ("lov".equalsIgnoreCase(parameterUse.getValueSelection())
					&& !objParameter.getSelectionType().equalsIgnoreCase(DocumentExecutionUtils.SELECTION_TYPE_TREE)) {

				ArrayList<HashMap<String, Object>> admissibleValues = objParameter.getAdmissibleValues();

				if (!objParameter.getSelectionType().equalsIgnoreCase(DocumentExecutionUtils.SELECTION_TYPE_LOOKUP)) {
					parameterAsMap.put("defaultValues", admissibleValues);
				} else {
					parameterAsMap.put("defaultValues", new ArrayList<>());
				}
				parameterAsMap.put("defaultValuesMeta", objParameter.getLovVisibleColumnsNames());
				parameterAsMap.put(DocumentExecutionUtils.VALUE_COLUMN_NAME_METADATA, objParameter.getLovValueColumnName());
				parameterAsMap.put(DocumentExecutionUtils.DESCRIPTION_COLUMN_NAME_METADATA, objParameter.getLovDescriptionColumnName());

				// hide the parameter if is mandatory and have one value in lov (no error parameter)
				if (admissibleValues != null && admissibleValues.size() == 1 && objParameter.isMandatory() && !admissibleValues.get(0).containsKey("error")
						&& (objParameter.getDataDependencies() == null || objParameter.getDataDependencies().isEmpty())
						&& (objParameter.getLovDependencies() == null || objParameter.getLovDependencies().isEmpty())) {
					showParameterLov = false;
				}

				// if parameterValue is not null and is array, check if all element are present in lov
				Object values = parameterAsMap.get("parameterValue");
				if (values != null && admissibleValues != null) {
					checkIfValuesAreAdmissible(values, admissibleValues);
				}

			}

			// DATE RANGE DEFAULT VALUE
			if (objParameter.getParType().equals("DATE_RANGE")) {
				try {
					ArrayList<HashMap<String, Object>> defaultValues = manageDataRange(businessModel, role, objParameter.getId());
					parameterAsMap.put("defaultValues", defaultValues);
				} catch (SerializationException e) {
					logger.debug("Filters DATE RANGE ERRORS ", e);
				}

			}

			// convert the parameterValue from array of string in array of object
			DefaultValuesList parameterValueList = new DefaultValuesList();
			Object oVals = parameterAsMap.get("parameterValue");
			Object oDescr = parameterAsMap.get("parameterDescription") != null ? parameterAsMap.get("parameterDescription") : new ArrayList<String>();

			if (oVals != null) {
				if (oVals instanceof List) {
					// CROSS NAV : INPUT PARAM PARAMETER TARGET DOC IS STRING
					if (oVals.toString().startsWith("[") && oVals.toString().endsWith("]") && parameterUse.getValueSelection().equals("man_in")) {
						List<String> valList = (ArrayList) oVals;
						String stringResult = "";
						for (int k = 0; k < valList.size(); k++) {
							String itemVal = valList.get(k);
							if (objParameter.getParType().equals("STRING") && objParameter.isMultivalue()) {
								stringResult += "'" + itemVal + "'";
							} else {
								stringResult += itemVal;
							}
							if (k != valList.size() - 1) {
								stringResult += ",";
							}
						}
						LovValue defValue = new LovValue();
						defValue.setValue(stringResult);
						defValue.setDescription(stringResult);
						parameterValueList.add(defValue);
					} else {
						List<String> valList = (ArrayList) oVals;
						List<String> descrList = (ArrayList) oDescr;
						for (int k = 0; k < valList.size(); k++) {
							String itemVal = valList.get(k);
							String itemDescr = descrList.size() > k ? descrList.get(k) : itemVal;
							LovValue defValue = new LovValue();
							defValue.setValue(itemVal);
							defValue.setDescription(itemDescr != null ? itemDescr : itemVal);
							parameterValueList.add(defValue);

						}
					}
					parameterAsMap.put("parameterValue", parameterValueList);
				}
			}

			parameterAsMap.put("dependsOn", objParameter.getDependencies());
			parameterAsMap.put("dataDependencies", objParameter.getDataDependencies());
			parameterAsMap.put("visualDependencies", objParameter.getVisualDependencies());
			parameterAsMap.put("lovDependencies", (objParameter.getLovDependencies() != null) ? objParameter.getLovDependencies() : new ArrayList<>());

			// load DEFAULT VALUE if present and if the parameter value is empty
			if (objParameter.getDefaultValues() != null && objParameter.getDefaultValues().size() > 0
					&& objParameter.getDefaultValues().get(0).getValue() != null) {
				DefaultValuesList valueList = null;
				// check if the parameter is really valorized (for example if it isn't an empty list)
				List lstValues = (List) parameterAsMap.get("parameterValue");
				// if (lstValues.size() == 0)
				// jsonCrossParameters.remove(objParameter.getId());

				String parLab = objParameter.getDriver() != null && objParameter.getDriver().getParameter() != null
						? objParameter.getDriver().getParameter().getLabel()
						: "";
				String useModLab = objParameter.getAnalyticalDriverExecModality() != null ? objParameter.getAnalyticalDriverExecModality().getLabel() : "";
				String sessionKey = parLab + "_" + useModLab;

				valueList = objParameter.getDefaultValues();

				// if (jsonCrossParameters.isNull(objParameter.getId())
				// // && !sessionParametersMap.containsKey(objParameter.getId())) {
				// && !sessionParametersMap.containsKey(sessionKey)) {
				// if (valueList != null) {
				// parameterAsMap.put("parameterValue", valueList);
				// }
				// }

				// in every case fill default values!
				parameterAsMap.put("driverDefaultValue", valueList);
			}

			if (!showParameterLov) {
				parameterAsMap.put("showOnPanel", "false");
			} else {
				parameterAsMap.put("showOnPanel", "true");
			}
			parametersArrayList.add(parameterAsMap);

		}
		for (int z = 0; z < parametersArrayList.size(); z++) {

			Map docP = parametersArrayList.get(z);
			DefaultValuesList defvalList = (DefaultValuesList) docP.get("parameterValue");
			if (defvalList != null && defvalList.size() == 1) {
				LovValue defval = defvalList.get(0);
				if (defval != null) {
					Object val = defval.getValue();
					if (val != null && val.equals("$")) {
						docP.put("parameterValue", "");
					}
				}

			}
		}

		if (parameters.size() > 0) {
			resultAsMap.put("filterStatus", parametersArrayList);

		} else {
			resultAsMap.put("filterStatus", new ArrayList<>());

		}

		resultAsMap.put("isReadyForExecution", isReadyForExecution(parameters));

		logger.debug("OUT");
		return Response.ok(resultAsMap).build();
	}

	private JSONObject decodeRequestParameters(JSONObject requestValParams) throws JSONException, IOException {
		JSONObject toReturn = new JSONObject();

		Iterator keys = requestValParams.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object valueObj = requestValParams.get(key);
			if (valueObj instanceof Number) {
				String value = String.valueOf(valueObj);
				// if (!value.equals("%7B%3B%7B") && !value.equalsIgnoreCase("%")) {
				if (!value.equals("") && !value.equalsIgnoreCase("%")) {
					toReturn.put(key, URLDecoder.decode(value, "UTF-8"));
				} else {
					toReturn.put(key, value); // uses the original value for list and %
				}
			} else if (valueObj instanceof String) {
				String value = String.valueOf(valueObj);
				// if (!value.equals("%7B%3B%7B") && !value.equalsIgnoreCase("%")) {
				if (!value.equals("") && !value.equalsIgnoreCase("%")) {
					toReturn.put(key, URLDecoder.decode(value, "UTF-8"));
				} else {
					toReturn.put(key, value); // uses the original value for list and %
				}
			} else if (valueObj instanceof JSONArray) {
				JSONArray valuesLst = (JSONArray) valueObj;
				JSONArray ValuesLstDecoded = new JSONArray();
				for (int v = 0; v < valuesLst.length(); v++) {
					// String value = (String) valuesLst.get(v);
					String value = (valuesLst.get(v) != null) ? String.valueOf(valuesLst.get(v)) : "";
					if (!value.equals("") && !value.equalsIgnoreCase("%")) {
						ValuesLstDecoded.put(URLDecoder.decode(value, "UTF-8"));
					} else {
						ValuesLstDecoded.put(value);
						URLDecoder.decode(value, "UTF-8"); // uses the original value for list and %
					}
				}
				toReturn.put(key, ValuesLstDecoded);
			}
		}

		return toReturn;
	}

	private Map<String, JSONObject> getSessionParameters(JSONObject requestVal) {

		Map<String, JSONObject> sessionParametersMap = new HashMap<String, JSONObject>();

		try {
			Object jsonSessionParametersObject = requestVal.get("sessionParameters");
			JSONObject sessionParametersJSON = new JSONObject(jsonSessionParametersObject.toString());

			Iterator<String> it = sessionParametersJSON.keys();
			while (it.hasNext()) {
				String key = it.next();
				JSONObject parJson = sessionParametersJSON.getJSONObject(key);
				sessionParametersMap.put(key, parJson);
			}
		} catch (Exception e) {
			logger.error("Error converting session parameters to JSON: ", e);
		}

		return sessionParametersMap;
	}

	private void applyRequestParameters(MetaModel businessModel, JSONObject crossNavigationParametesMap, Map<String, JSONObject> sessionParametersMap,
			String role, Locale locale, List<String> parsFromCross) {
		BusinessModelRuntime dum = new BusinessModelRuntime(this.getUserProfile(), locale);
		List<BIMetaModelParameter> parameters = businessModel.getDrivers();
		for (BIMetaModelParameter parameter : parameters) {
			if (crossNavigationParametesMap.has(parameter.getParameterUrlName())) {
				logger.debug("Found value from request for parmaeter [" + parameter.getParameterUrlName() + "]");
				dum.refreshParameterForFilters(parameter, crossNavigationParametesMap);
				parsFromCross.add(parameter.getParameterUrlName());
				continue;
			}

			ParameterUse parameterUse;
			try {
				parameterUse = DAOFactory.getParameterUseDAO().loadByParameterIdandRole(parameter.getParID(), role);
			} catch (EMFUserError e) {
				throw new SpagoBIRuntimeException(e);
			}

			String key = parameter.getParameter().getLabel() + "_" + parameterUse.getLabel();
			if (parameter.getParameter().getModalityValue().getITypeCd().equals("MAN_IN")) {
				key += "_" + parameter.getParameterUrlName();
			}

			JSONObject sessionValue = sessionParametersMap.get(key);
			if (sessionValue != null && sessionValue.optString("value") != null) {

				DefaultValuesList defValueList = buildParameterSessionValueList(sessionValue.optString("value"), sessionValue.optString("description"),
						parameter);
				List values = defValueList.getValuesAsList();
				List descriptions = defValueList.getDescriptionsAsList();

				parameter.setParameterValues(values);
				parameter.setParameterValuesDescription(descriptions);

			}
		}
	}

	private DefaultValuesList buildParameterSessionValueList(String sessionParameterValue, String sessionParameterDescription,
			BIMetaModelParameter metaModelParameter) {

		logger.debug("IN");

		DefaultValuesList valueList = new DefaultValuesList();

		SimpleDateFormat serverDateFormat = new SimpleDateFormat(SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"));

		if (metaModelParameter.getParameter().getType().equals("DATE")) {
			String valueDate = sessionParameterValue;

			String[] date = valueDate.split("#");
			if (date.length < 2) {
				throw new SpagoBIRuntimeException("Illegal format for Value List Date Type [" + valueDate + "+], unable to find symbol [#]");
			}
			SimpleDateFormat format = new SimpleDateFormat(date[1]);
			LovValue valueDef = new LovValue();
			try {
				Date d = format.parse(date[0]);
				String dateServerFormat = serverDateFormat.format(d);
				valueDef.setValue(dateServerFormat);
				valueDef.setDescription(sessionParameterDescription);
				valueList.add(valueDef);
				return valueList;
			} catch (ParseException e) {
				logger.error("Error while building default Value List Date Type ", e);
				return null;
			}
		} else if (metaModelParameter.getParameter().getType().equals("DATE_RANGE")) {
			// String valueDate = objParameter.getDefaultValues().get(0).getValue().toString();
			String valueDate = sessionParameterValue;
			String[] date = valueDate.split("#");
			SimpleDateFormat format = new SimpleDateFormat(date[1]);
			LovValue valueDef = new LovValue();
			try {

				String dateRange = date[0];
				String[] dateRangeArr = dateRange.split("_");
				String range = dateRangeArr[dateRangeArr.length - 1];
				dateRange = dateRange.replace("_" + range, "");
				Date d = format.parse(dateRange);
				String dateServerFormat = serverDateFormat.format(d);
				valueDef.setValue(dateServerFormat + "_" + range);
				valueDef.setDescription(sessionParameterDescription);
				valueList.add(valueDef);
				return valueList;
			} catch (ParseException e) {
				logger.error("Error while building default Value List Date Type ", e);
				return null;
			}
		}

		else if (metaModelParameter.isMultivalue()) {
			logger.debug("Multivalue case");
			try {
				// split sessionValue
				JSONArray valuesArray = new JSONArray(sessionParameterValue);
				StringTokenizer st = new StringTokenizer(sessionParameterDescription, ";", false);

				ArrayList<String> values = new ArrayList<String>();
				ArrayList<String> descriptions = new ArrayList<String>();

				int i = 0;
				while (st.hasMoreTokens()) {
					String parDescription = st.nextToken();
					descriptions.add(i, parDescription);
					i++;
				}

				for (int j = 0; j < valuesArray.length(); j++) {
					String value = (String) valuesArray.get(j);
					values.add(value);
				}

				for (int z = 0; z < values.size(); z++) {
					String parValue = values.get(z);
					String parDescription = descriptions.size() > z ? descriptions.get(z) : parValue;
					LovValue valueDef = new LovValue();
					valueDef.setValue(parValue);
					valueDef.setDescription(parDescription);
					valueList.add(valueDef);
				}

			} catch (Exception e) {
				logger.error("Error in converting multivalue session values", e);
			}

		} else {
			logger.debug("NOT - multivalue case");
			// value could be String or array

			try {
				String value = null;
				if (sessionParameterValue != null && sessionParameterValue.length() > 0 && sessionParameterValue.charAt(0) == '[') {
					JSONArray valuesArray = new JSONArray(sessionParameterValue);
					if (valuesArray.get(0) != null) {
						value = valuesArray.get(0).toString();
					}
				} else {
					value = sessionParameterValue;
				}

				LovValue valueDef = new LovValue();
				valueDef.setValue(value);
				valueDef.setDescription(sessionParameterDescription);
				valueList.add(valueDef);

			} catch (Exception e) {
				logger.error("Error in converting single value session values", e);
			}
		}

		logger.debug("OUT");
		return valueList;
	}

	public void checkIfValuesAreAdmissible(Object values, ArrayList<HashMap<String, Object>> admissibleValues) {
		if (values instanceof List) {
			List<String> valuesList = (List) values;
			for (int k = 0; k < valuesList.size(); k++) {
				String item = valuesList.get(k);
				boolean found = false;
				if (item != null && item.equals("$")) {
					found = true;
				} else {
					for (HashMap<String, Object> parHashVal : admissibleValues) {
						if (parHashVal.containsKey("value") && parHashVal.get("value").equals(item)) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					valuesList.remove(k);
					k--;
				}
			}
		}
	}

	public ArrayList<HashMap<String, Object>> manageDataRange(MetaModel businessModel, String executionRole, String biparameterId)
			throws EMFUserError, SerializationException, JSONException, IOException {

		BIMetaModelParameter biMetaModelParameter = null;
		List parameters = businessModel.getDrivers();
		for (int i = 0; i < parameters.size(); i++) {
			BIMetaModelParameter p = (BIMetaModelParameter) parameters.get(i);
			if (biparameterId.equalsIgnoreCase(p.getParameterUrlName())) {
				biMetaModelParameter = p;
				break;
			}
		}

		try {
			if (DateRangeDAOUtilities.isDateRange(biMetaModelParameter)) {
				logger.debug("loading date range combobox");

			}
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Error on loading date range combobox values", e);
		}

		Integer parID = biMetaModelParameter.getParID();
		Assert.assertNotNull(parID, "parID");
		ParameterUse param = DAOFactory.getParameterUseDAO().loadByParameterIdandRole(parID, executionRole);
		String options = param.getOptions();
		Assert.assertNotNull(options, "options");

		ArrayList<HashMap<String, Object>> dateRangeValuesDataJSON = getDateRangeValuesDataJSON(options);

		// TODO
		// int dataRangeOptionsSize = getDataRangeOptionsSize(options);
		// JSONObject valuesJSON = (JSONObject) JSONStoreFeedTransformer.getInstance().transform(dateRangeValuesDataJSON, VALUE_FIELD.toUpperCase(),
		// LABEL_FIELD.toUpperCase(), DESCRIPTION_FIELD.toUpperCase(), VISIBLE_COLUMNS, dataRangeOptionsSize);

		return dateRangeValuesDataJSON;

	}

	private ArrayList<HashMap<String, Object>> getDateRangeValuesDataJSON(String optionsJson) throws JSONException {
		JSONObject json = new JSONObject(optionsJson);
		JSONArray options = json.getJSONArray(DATE_RANGE_OPTIONS_KEY);
		JSONArray res = new JSONArray();

		ArrayList<HashMap<String, Object>> defaultValues = new ArrayList<>();

		for (int i = 0; i < options.length(); i++) {
			// JSONObject opt = new JSONObject();
			JSONObject optJson = (JSONObject) options.get(i);
			String type = (String) optJson.get(DATE_RANGE_TYPE_JSON);
			// String typeDesc = getLocalizedMessage("SBIDev.paramUse." + type);
			String quantity = (String) optJson.get(DATE_RANGE_QUANTITY_JSON);
			String value = type + "_" + quantity;
			String label = quantity + " " + type;
			// message properties !!!
			HashMap<String, Object> obj = new HashMap<String, Object>();
			obj.put(VALUE_FIELD, value);
			obj.put(LABEL_FIELD, label);
			obj.put(DESCRIPTION_FIELD, label);
			obj.put(DATE_RANGE_TYPE_JSON, type);
			obj.put(DATE_RANGE_QUANTITY_JSON, quantity);
			defaultValues.add(obj);

		}
		return defaultValues;
	}

	public boolean isReadyForExecution(List<BusinessModelDriverRuntime> parameters) {
		for (BusinessModelDriverRuntime parameter : parameters) {
			List values = parameter.getDriver().getParameterValues();
			// if parameter is mandatory and has no value, execution cannot start automatically
			if (parameter.isMandatory() && (values == null || values.isEmpty())) {
				logger.debug("Parameter [" + parameter.getId() + "] is mandatory but has no values. Execution cannot start automatically");
				return false;
			}
		}
		return true;
	}

	@POST
	@Path("/parametervalues")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	// public Response getParameterValues(@QueryParam("label") String label, @QueryParam("role") String role, @QueryParam("driverId") String driverId,
	// @QueryParam("mode") String mode, @QueryParam("treeLovNode") String treeLovNode,
	// // @QueryParam("treeLovNode") Integer treeLovNodeLevel,
	// @Context HttpServletRequest req) throws EMFUserError {
	public Response getParameterValues(@Context HttpServletRequest req) throws EMFUserError, IOException, JSONException {

		RequestContainer aRequestContainer = RequestContainerAccess.getRequestContainer(req);
		Locale locale = GeneralUtilities.getCurrentLocale(aRequestContainer);

		String role;
		String name;
		String driverId;
		String treeLovNode;
		String mode;
		// GET PARAMETER

		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		role = (String) requestVal.opt("role");
		name = (String) requestVal.opt("name");
		driverId = (String) requestVal.opt("parameterId");
		treeLovNode = (String) requestVal.opt("treeLovNode");
		mode = (String) requestVal.opt("mode");

		IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
		MetaModel businessModel = dao.loadMetaModelForExecutionByNameAndRole(name, role, false);

		BIMetaModelParameter biMetaModelParameter = null;
		List<BIMetaModelParameter> parameters = businessModel.getDrivers();
		for (int i = 0; i < parameters.size(); i++) {
			BIMetaModelParameter p = parameters.get(i);
			if (driverId.equalsIgnoreCase(p.getParameterUrlName())) {
				biMetaModelParameter = p;
				break;
			}
		}

		String treeLovNodeValue;
		Integer treeLovNodeLevel;

		if (treeLovNode.contains("lovroot")) {
			treeLovNodeValue = "lovroot";
			treeLovNodeLevel = 0;
		} else {
			String[] splittedNode = treeLovNode.split(NODE_ID_SEPARATOR);
			treeLovNodeValue = splittedNode[0];
			treeLovNodeLevel = new Integer(splittedNode[1]);
		}

		// ArrayList<HashMap<String, Object>> result = DocumentExecutionUtils.getLovDefaultValues(
		// role, biObject, biObjectParameter, requestVal, treeLovNodeLevel, treeLovNodeValue, req);
		HashMap<String, Object> defaultValuesData = BusinessModelOpenUtils.getLovDefaultValues(role, businessModel, biMetaModelParameter, requestVal,
				treeLovNodeLevel, treeLovNodeValue, req);

		ArrayList<HashMap<String, Object>> result = (ArrayList<HashMap<String, Object>>) defaultValuesData.get(BusinessModelOpenUtils.DEFAULT_VALUES);

		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();

		if (result != null && result.size() > 0) {
			resultAsMap.put("filterValues", result);
			resultAsMap.put("errors", new ArrayList<>());
		} else {
			resultAsMap.put("filterValues", new ArrayList<>());

			List errorList = BusinessModelOpenUtils.handleNormalExecutionError(this.getUserProfile(), businessModel, req,
					this.getAttributeAsString("SBI_ENVIRONMENT"), role, biMetaModelParameter.getParameter().getModalityValue().getSelectionType(), null,
					locale);

			resultAsMap.put("errors", errorList);
		}

		logger.debug("OUT");
		return Response.ok(resultAsMap).build();
	}

}
