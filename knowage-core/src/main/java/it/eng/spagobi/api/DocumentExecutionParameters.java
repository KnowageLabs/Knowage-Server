package it.eng.spagobi.api;

import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_OPTIONS_KEY;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_QUANTITY_JSON;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_TYPE_JSON;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
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

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.BusinessModelOpenUtils;
import it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelDriverRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DriversRuntimeLoaderFactory;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.api.v2.DocumentExecutionParametersResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIMetaModelParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
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
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.dataset.bo.BIObjDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.dao.IBIObjDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @deprecated Replaced by {@link DocumentExecutionParametersResource}
 */
@Path("/1.0/documentExeParameters")
@ManageAuthorization
@Deprecated
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

			try {
				BIObject obj = DriversRuntimeLoaderFactory.getDriversRuntimeLoader().loadBIObjectForExecutionByLabelAndRole(label, role);
				ArrayList<HashMap<String, Object>> qbeDrivers = getQbeDrivers(obj);
				if (qbeDrivers == null || qbeDrivers.isEmpty()) {
					BIObjectParameter biObjectParameter;
					List<ObjParuse> biParameterExecDependencies;
					DocumentRuntime dum = new DocumentRuntime(this.getUserProfile(), req.getLocale());
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
						JSONArray valuesJSONArray = getChildrenForTreeLov(lovProvDet, rows, mode, treeLovNodeLevel, treeLovNodeValue);
						result = buildJsonResult("OK", "", null, valuesJSONArray, biparameterId).toString();
					} else {
						valuesJSON = buildJSONForLOV(lovProvDet, rows, mode, start, limit);
						result = buildJsonResult("OK", "", valuesJSON, null, biparameterId).toString();
					}
				} else {
					BusinessModelRuntime bum = new BusinessModelRuntime(UserProfileManager.getProfile(), req.getLocale());
					if (selectedParameterValuesJSON != null) {
						bum.refreshParametersMetamodelValues(selectedParameterValuesJSON, false, obj);
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
						JSONArray valuesJSONArray = getChildrenForTreeLov(lovProvDet, rows, mode, treeLovNodeLevel, treeLovNodeValue);
						result = buildJsonResult("OK", "", null, valuesJSONArray, biparameterId).toString();
					} else {
						valuesJSON = buildJSONForLOV(lovProvDet, rows, mode, start, limit);
						result = buildJsonResult("OK", "", valuesJSON, null, biparameterId).toString();
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

	public ArrayList<HashMap<String, Object>> transformRuntimeDrivers(List<BusinessModelDriverRuntime> parameters, IParameterUseDAO parameterUseDAO,
			String role, MetaModel businessModel, BusinessModelOpenParameters BMOP) {
		ArrayList<HashMap<String, Object>> parametersArrayList = new ArrayList<>();
		ParameterUse parameterUse;
		for (BusinessModelDriverRuntime objParameter : parameters) {
			Integer paruseId = objParameter.getParameterUseId();
			try {
				parameterUse = parameterUseDAO.loadByUseID(paruseId);
			} catch (EMFUserError e1) {
				logger.debug(e1.getCause(), e1);
				throw new SpagoBIRuntimeException(e1.getMessage(), e1);
			}

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
						try {
							paramValues = URLDecoder.decode((String) paramValues, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							logger.debug(e.getCause(), e);
							throw new SpagoBIRuntimeException(e.getMessage(), e);
						}
					}
					paramValueLst.add(paramValues.toString());

					String parDescrVal = paramDescriptionValues != null && paramDescriptionValues instanceof String ? paramDescriptionValues.toString()
							: paramValues.toString();
					if (!parDescrVal.contains("%")) {
						try {
							parDescrVal = URLDecoder.decode(parDescrVal, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							logger.debug(e.getCause(), e);
							throw new SpagoBIRuntimeException(e.getMessage(), e);
						}
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
					BMOP.checkIfValuesAreAdmissible(values, admissibleValues);
				}
			}

			// DATE RANGE DEFAULT VALUE
			if (objParameter.getParType().equals("DATE_RANGE")) {
				try {
					ArrayList<HashMap<String, Object>> defaultValues = BMOP.manageDataRange(businessModel, role, objParameter.getId());
					parameterAsMap.put("defaultValues", defaultValues);
				} catch (SerializationException | EMFUserError | JSONException | IOException e) {
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
		return parametersArrayList;
	}

	public ArrayList<HashMap<String, Object>> getDatasetDriversByModelName(String businessModelName, Boolean loadDSwithDrivers) {
		ArrayList<HashMap<String, Object>> parametersArrList = new ArrayList<>();
		IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
		IParameterUseDAO parameterUseDAO = DAOFactory.getParameterUseDAO();
		List<BusinessModelDriverRuntime> parameters = new ArrayList<>();
		BusinessModelOpenParameters BMOP = new BusinessModelOpenParameters();
		String role;
		try {
			role = getUserProfile().getRoles().contains("admin") ? "admin" : (String) getUserProfile().getRoles().iterator().next();
		} catch (EMFInternalError e2) {
			logger.debug(e2.getCause(), e2);
			throw new SpagoBIRuntimeException(e2.getMessage(), e2);
		}
		MetaModel businessModel = dao.loadMetaModelForExecutionByNameAndRole(businessModelName, role, loadDSwithDrivers);
		if (businessModel == null) {
			return null;
		}
		BusinessModelRuntime dum = new BusinessModelRuntime(this.getUserProfile(), null);
		parameters = BusinessModelOpenUtils.getParameters(businessModel, role, request.getLocale(), null, true, dum);
		parametersArrList = transformRuntimeDrivers(parameters, parameterUseDAO, role, businessModel, BMOP);

		return parametersArrList;
	}

	private ArrayList<HashMap<String, Object>> getQbeDrivers(BIObject biObject) {
		IDataSet dataset = null;
		ArrayList datasetList = null;
		BIObjDataSet biObjDataSet = null;
		Integer dsId = null;
		List docDrivers = null;
		IBIObjDataSetDAO biObjDataSetDAO = DAOFactory.getBIObjDataSetDAO();
		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
		IBIMetaModelParameterDAO driversDao = DAOFactory.getBIMetaModelParameterDAO();
		String businessModelName = null;
		MetaModel businessModel = null;
		ArrayList<BIObjDataSet> biObjDataSetList = null;
		ArrayList<HashMap<String, Object>> parametersArrayList = new ArrayList<>();
		try {

			biObjDataSetList = biObjDataSetDAO.getBiObjDataSets(biObject.getId());
			Iterator itDs = biObjDataSetList.iterator();
			while (itDs.hasNext()) {
				biObjDataSet = (BIObjDataSet) itDs.next();
				dsId = biObjDataSet.getDataSetId();
				dataset = datasetDao.loadDataSetById(dsId);
				dataset = dataset instanceof VersionedDataSet ? ((VersionedDataSet) dataset).getWrappedDataset() : dataset;
				if (dataset != null && dataset.getDsType() == "SbiQbeDataSet") {
					String config = dataset.getConfiguration();
					JSONObject jsonConfig = new JSONObject(dataset.getConfiguration());
					businessModelName = (String) jsonConfig.get("qbeDatamarts");
					parametersArrayList = getDatasetDriversByModelName(businessModelName, false);
					if (parametersArrayList != null && !parametersArrayList.isEmpty())
						break;
				}

			}
		} catch (Exception e) {
			logger.error("Cannot retrieve drivers list", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		}
		return parametersArrayList;
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
