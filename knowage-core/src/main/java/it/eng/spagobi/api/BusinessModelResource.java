/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.api;

import static it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils.createParameterValuesMap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.BiMap;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.RequestContainerAccess;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.BusinessModelOpenUtils;
import it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils;
import it.eng.spagobi.analiticalmodel.document.handlers.AbstractDriverRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelDriverRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DriversRuntimeLoader;
import it.eng.spagobi.analiticalmodel.document.handlers.DriversRuntimeLoaderFactory;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.api.common.MetaUtils;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.DependenciesPostProcessingLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.exceptions.MissingLOVDependencyException;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.dataset.bo.DataSetParameterItem;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @author Marco Libanori
 */
@Path("/1.0/businessmodel")
public class BusinessModelResource {

	private final Logger logger = Logger.getLogger(BusinessModelResource.class);

	private static final String PROPERTY_DATA = "data";
	private static final String PROPERTY_METADATA = "metadata";

	public static final String SERVICE_NAME = "GET DOCUMENT PARAMETERS ";

	private static final String ROLE = "role";
	public static String PARAMETER_ID = "paramId";
	public static String SELECTED_PARAMETER_VALUES = "parameters";
	public static String FILTERS = "FILTERS";
	public static String NODE = "node";

	/**
	 * @deprecated Replaced by {@link MetaUtils#NODE_ID_SEPARATOR}
	 */
	@Deprecated
	public static String NODE_ID_SEPARATOR = "___SEPA__";
	public static String START = "start";
	public static String LIMIT = "limit";

	@Context
	protected HttpServletRequest request;

	@POST
	@Path("/{qbeDatamart}/filters")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.BUILD_QBE_QUERIES_FUNCTIONALITY })
	public Response filters(@PathParam("qbeDatamart") String qbeDatamart) throws JSONException, IOException {

		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(request);
		String role = requestVal.getString("role");

		Map<String, Object> ret = new LinkedHashMap<String, Object>();
		final List<HashMap<String, Object>> parametersArrayList = new ArrayList<>();

		ret.put("filterStatus", parametersArrayList);

		getDriversFromQbeDataSet(role, ret, qbeDatamart);

		return Response.ok(ret).build();
	}

	@POST
	@Path("/{qbeDatamart}/admissibleValues")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String admissibleValues(@PathParam("qbeDatamart") String qbeDatamart) throws Exception {

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
		Integer start = null;
		Integer limit = null;

		// PARAMETER

		try {
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(request);
			role = (String) requestVal.opt(ROLE);
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

				DriversRuntimeLoader driversRuntimeLoader = DriversRuntimeLoaderFactory.getDriversRuntimeLoader();
				List<BIMetaModelParameter> drivers = driversRuntimeLoader.getBusinessModelDrivers(qbeDatamart, role);

				ArrayList<HashMap<String, Object>> qbeDrivers = metaUtils.getQbeDrivers(getUserProfile(), request.getLocale(), qbeDatamart);
				if (qbeDrivers == null || qbeDrivers.isEmpty()) {
					BIMetaModelParameter biObjectParameter;
					List<ObjParuse> biParameterExecDependencies;
					DocumentRuntime dum = new DocumentRuntime(this.getUserProfile(), request.getLocale());
					if (selectedParameterValuesJSON != null) {
						dum.refreshParametersValues(selectedParameterValuesJSON, false, drivers);
					}

					selectedParameterValues = createParameterValuesMap(selectedParameterValuesJSON);

					// START get the relevant biobject parameter
					biObjectParameter = null;
					List<BIMetaModelParameter> parameters = drivers;
					for (int i = 0; i < parameters.size(); i++) {
						BIMetaModelParameter p = parameters.get(i);
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
						lovResult = executionCacheManager.getLovResultDum(profile, lovProvDet, dum.getDependencies(biObjectParameter, role), drivers, true,
								request.getLocale());

						// get all the rows of the result
						LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
						rows = lovResultHandler.getRows();

					} catch (MissingLOVDependencyException mldaE) {
						String localizedMessage = getLocalizedMessage("sbi.api.documentExecParameters.dependencyNotFill");
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
					BusinessModelRuntime bum = new BusinessModelRuntime(UserProfileManager.getProfile(), request.getLocale());
					if (selectedParameterValuesJSON != null) {
						bum.refreshParametersMetamodelValues(selectedParameterValuesJSON, false, drivers);
					}

					selectedParameterValues = createParameterValuesMap(selectedParameterValuesJSON);

					// START get the relevant biobject parameter
					BIMetaModelParameter biMetaModelParameter = null;
					List<BIMetaModelParameter> parameters = drivers;
					for (int i = 0; i < parameters.size(); i++) {
						BIMetaModelParameter p = parameters.get(i);
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
						lovResult = executionCacheManager.getLovResultBum(profile, lovProvDet, biParameterExecDependencies, drivers, true, request.getLocale());

						// get all the rows of the result
						LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
						rows = lovResultHandler.getRows();

					} catch (MissingLOVDependencyException mldaE) {
						String localizedMessage = getLocalizedMessage("sbi.api.documentExecParameters.dependencyNotFill");
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

	@POST
	@Path("/{qbeDatamart}/admissibleValuesTree")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response admissibleValuesTree(@Context HttpServletRequest req, @PathParam("qbeDatamart") String qbeDatamart) throws EMFUserError, IOException, JSONException {

		Map<String, Object> resultAsMap = new HashMap<String, Object>();

		RequestContainer aRequestContainer = RequestContainerAccess.getRequestContainer(req);
		Locale locale = GeneralUtilities.getCurrentLocale(aRequestContainer);

		String role;
		String biparameterId;
		String treeLovNode;
		String mode;
		// GET PARAMETER

		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		role = (String) requestVal.opt("role");
		biparameterId = (String) requestVal.opt("parameterId");
		treeLovNode = (String) requestVal.opt("treeLovNode");
		mode = (String) requestVal.opt("mode");


		DriversRuntimeLoader driversRuntimeLoader = DriversRuntimeLoaderFactory.getDriversRuntimeLoader();
		MetaModel datasetMetaModel = DAOFactory.getMetaModelsDAO().loadMetaModelByName(qbeDatamart);
		List<BIMetaModelParameter> drivers = datasetMetaModel.getDrivers();

		BIMetaModelParameter biObjectParameter = null;
		List<? extends AbstractDriver> parameters = drivers;
		for (int i = 0; i < parameters.size(); i++) {
			AbstractDriver p = parameters.get(i);
			if (biparameterId.equalsIgnoreCase(p.getParameterUrlName())) {
				biObjectParameter = (BIMetaModelParameter) p;
				break;
			}
		}

		String treeLovNodeValue;
		Integer treeLovNodeLevel;

		if (treeLovNode == null || treeLovNode.equals("") || treeLovNode.contains("lovroot")) {
			treeLovNodeValue = "lovroot";
			treeLovNodeLevel = 0;
		} else {
			String[] splittedNode = treeLovNode.split(NODE_ID_SEPARATOR);
			treeLovNodeValue = splittedNode[0];
			treeLovNodeLevel = new Integer(splittedNode[1]);
		}

		Map<String, Object> defaultValuesData = DocumentExecutionUtils.getLovDefaultValues(role, drivers, biObjectParameter, requestVal, treeLovNodeLevel,
				treeLovNodeValue, req.getLocale());

		ArrayList<Map<String, Object>> result = (ArrayList<Map<String, Object>>) defaultValuesData.get(DocumentExecutionUtils.DEFAULT_VALUES);

		if (result != null && result.size() > 0) {
			resultAsMap.put("rows", result);
		} else {
			MetaModel loadMetaModelByName = DAOFactory.getMetaModelsDAO().loadMetaModelByName(qbeDatamart);

			List errorList = DocumentExecutionUtils.handleNormalExecutionError(this.getUserProfile(), datasetMetaModel, req,
					this.getAttributeAsString("SBI_ENVIRONMENT"), role, biObjectParameter.getParameter().getModalityValue().getSelectionType(), null, locale);

			resultAsMap.put("errors", errorList);
		}

		logger.debug("OUT");


		return Response.ok(resultAsMap).build();
	}

	/**
	 * TODO : Duplicated
	 */
	public String getAttributeAsString(String attrName) {
		return null;
	}

	/**
	 * TODO : Duplicated
	 */
	private String getLocalizedMessage(String code) {
		return MessageBuilderFactory.getMessageBuilder().getMessage(code, request);
	}

	private void getParametersFromDataSet(Map<String, Object> ret, SbiDataSet ds) {
		final List<HashMap<String, Object>> parametersArrayList = (List<HashMap<String, Object>>) ret.get("filterStatus");
		for (DataSetParameterItem e : ds.getParametersList()) {
			final Map<String, Object> metadata = new LinkedHashMap<>();

			String name = e.getName();
			String type = e.getType();
			String defaultValue = e.getDefaultValue();
			boolean multivalue = e.isMultivalue();

			Map<String, Object> parameterAsMap = new LinkedHashMap<String, Object>();
			parameterAsMap.put("id", null);
			parameterAsMap.put("label", name);
			parameterAsMap.put("urlName", name);
			parameterAsMap.put("type", type);
			parameterAsMap.put("selectionType", null);
			parameterAsMap.put("valueSelection", "MANUAL");
			parameterAsMap.put("visible", true);
			parameterAsMap.put("mandatory", true);
			parameterAsMap.put("multivalue", multivalue);
			parameterAsMap.put("driverLabel", name);
			parameterAsMap.put("driverUseLabel", name);
			parameterAsMap.put(PROPERTY_METADATA, metadata);

			Map<String, String> colPlaceholder2ColName = new HashMap<>();

			/*
			 * Here "data" is a dummy column just to simulate that
			 * a parameter is driver.
			 */
			colPlaceholder2ColName.put("_col0", "data");

			metadata.put("colsMap", colPlaceholder2ColName);
			metadata.put("descriptionColumn", "data");
			metadata.put("invisibleColumns", Collections.EMPTY_LIST);
			metadata.put("valueColumn", "data");
			metadata.put("visibleColumns", Arrays.asList("data"));

			parametersArrayList.add((HashMap<String, Object>) parameterAsMap);
		}
	}

	private String getDatamartFromDataSet(SbiDataSet ds) throws JSONException {
		String configuration = ds.getConfiguration();

		JSONObject jsonObject = new JSONObject(configuration);

		String qbeDatamart = jsonObject.getString("qbeDatamarts");
		return qbeDatamart;
	}

	private Response getDriversFromQbeDataSet(final String role, final Map<String, Object> resultAsMap, String businessModelName) {
		final List<HashMap<String, Object>> parametersArrayList = (List<HashMap<String, Object>>) resultAsMap.get("filterStatus");
		final List<BusinessModelDriverRuntime> parameters = new ArrayList<>();
		IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
		IParameterUseDAO parameterUseDAO = DAOFactory.getParameterUseDAO();
		ParameterUse parameterUse;

		MetaModel businessModel = dao.loadMetaModelForExecutionByNameAndRole(businessModelName, role, false);
		BusinessModelOpenParameters bmop = new BusinessModelOpenParameters();
		try {
			// role = this.getUserProfile().getRoles().iterator().next().toString();
			Locale locale = request.getLocale();
			BusinessModelRuntime dum = new BusinessModelRuntime(this.getUserProfile(), locale);
			parameters.addAll(BusinessModelOpenUtils.getParameters(businessModel, role, request.getLocale(), null, true, dum));
		} catch (SpagoBIRestServiceException e) {
			logger.debug(e.getCause(), e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		}
		for (BusinessModelDriverRuntime objParameter : parameters) {
			final Map<String, Object> metadata = new LinkedHashMap<>();
			BiMap<String, String> colPlaceholder2ColName = objParameter.getColPlaceholder2ColName();
			String lovDescriptionColumnName = objParameter.getLovDescriptionColumnName();
			String lovValueColumnName = objParameter.getLovValueColumnName();

			Integer paruseId = objParameter.getParameterUseId();
			try {
				parameterUse = parameterUseDAO.loadByUseID(paruseId);
			} catch (EMFUserError e1) {
				logger.debug("Error loading parameter use with id " + paruseId, e1);
				throw new SpagoBIRuntimeException(e1.getMessage(), e1);
			}

			HashMap<String, Object> parameterAsMap = new HashMap<String, Object>();
			parameterAsMap.put("id", objParameter.getBiObjectId());
			parameterAsMap.put("label", objParameter.getLabel());
			parameterAsMap.put("urlName", objParameter.getId());
			parameterAsMap.put("type", objParameter.getParType());
			parameterAsMap.put("selectionType", objParameter.getSelectionType());
			parameterAsMap.put("valueSelection", parameterUse.getValueSelection());
			parameterAsMap.put("visible", ((objParameter.isVisible())));
			parameterAsMap.put("mandatory", ((objParameter.isMandatory())));
			parameterAsMap.put("multivalue", objParameter.isMultivalue());
			parameterAsMap.put("driverLabel", objParameter.getPar().getLabel());
			parameterAsMap.put("driverUseLabel", objParameter.getAnalyticalDriverExecModality().getLabel());
			parameterAsMap.put(PROPERTY_METADATA, metadata);

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

				metadata.put("colsMap", colPlaceholder2ColName);
				metadata.put("descriptionColumn", lovDescriptionColumnName);
				metadata.put("invisibleColumns", objParameter.getLovInvisibleColumnsNames());
				metadata.put("valueColumn", lovValueColumnName);
				metadata.put("visibleColumns", objParameter.getLovVisibleColumnsNames());

				if (!objParameter.getSelectionType().equalsIgnoreCase(DocumentExecutionUtils.SELECTION_TYPE_LOOKUP)) {
					parameterAsMap.put(PROPERTY_DATA, admissibleValues);
				} else {
					parameterAsMap.put(PROPERTY_DATA, new ArrayList<>());
				}

				// hide the parameter if is mandatory and have one value in lov (no error parameter)
				if (admissibleValues != null && admissibleValues.size() == 1 && objParameter.isMandatory() && !admissibleValues.get(0).containsKey("error")
						&& (objParameter.getDataDependencies() == null || objParameter.getDataDependencies().isEmpty())
						&& (objParameter.getLovDependencies() == null || objParameter.getLovDependencies().isEmpty())) {
					showParameterLov = false;
				}

				// if parameterValue is not null and is array, check if all element are present in lov
				Object values = parameterAsMap.get("parameterValue");
				if (values != null && admissibleValues != null) {

					bmop.checkIfValuesAreAdmissible(values, admissibleValues);
				}

			}

			// DATE RANGE DEFAULT VALUE
			if (objParameter.getParType().equals("DATE_RANGE")) {
				try {

					ArrayList<HashMap<String, Object>> defaultValues = bmop.manageDataRange(businessModel, role, objParameter.getId());
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

			addDependencies(objParameter, parameterAsMap);

			// load DEFAULT VALUE if present and if the parameter value is empty

			Object defValue = null;
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

				if (!valueList.isEmpty()) {
					defValue = valueList.stream()
						.map(e -> {

							BiMap<String, String> inverse = colPlaceholder2ColName.inverse();
							String valColName = inverse.get(lovValueColumnName);
							String descColName = inverse.get(lovDescriptionColumnName);

							// TODO : workaround
							valColName = Optional.ofNullable(valColName).orElse("value");
							descColName = Optional.ofNullable(descColName).orElse("desc");

							Map<String, Object> ret = new LinkedHashMap<>();

							ret.put(valColName, e.getValue());

							if (!valColName.equals(descColName)) {
								ret.put(descColName, e.getDescription());
							}

							return ret;
						})
						.collect(Collectors.toList());
				}

				// if (jsonCrossParameters.isNull(objParameter.getId())
				// 		// && !sessionParametersMap.containsKey(objParameter.getId())) {
				// 		&& !sessionParametersMap.containsKey(sessionKey)) {
				// 	if (valueList != null) {
				// 		parameterAsMap.put("parameterValue", valueList);
				// 	}
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

		for (int i = 0; i < parametersArrayList.size(); i++) {
			Map<String, Object> parameter = parametersArrayList.get(i);
			List<Map<String, Object>> defaultValuesList = (List<Map<String, Object>>) parameter.get(PROPERTY_DATA);

			parameter.remove("parameterValue");

			if (defaultValuesList != null) {
				// Filter out null values
				defaultValuesList.removeIf(e -> e.get("value") == JSONObject.NULL || e.get("description") == JSONObject.NULL);

				// Fix JSON structure of admissible values
				defaultValuesList.forEach(e -> {
					List<String> fieldsToBeRemoved = new ArrayList<>();

					e.keySet().forEach(f -> {
						if (!f.startsWith("_col")) {
							fieldsToBeRemoved.add(f);
						}
					});

					fieldsToBeRemoved.forEach(f -> {
						e.remove(f);
					});
				});

			}
		}

		resultAsMap.put("filterStatus", parametersArrayList);

		resultAsMap.put("isReadyForExecution", bmop.isReadyForExecution(parameters));

		logger.debug("OUT");

		return Response.ok(resultAsMap).build();
	}

	private UserProfile getUserProfile() {
		return UserProfileManager.getProfile();
	}

	private void addDependencies(AbstractDriverRuntime<?> objParameter, HashMap<String, Object> parameterAsMap) {
		Map<String, Object> dependencies = new LinkedHashMap<>();
		parameterAsMap.put("dependencies", dependencies);

		dependencies.put("data", objParameter.getDataDependencies());
		dependencies.put("visual", objParameter.getVisualDependencies());
		dependencies.put("lov", Optional.ofNullable(objParameter.getLovDependencies()).orElse(Collections.emptyList()));
	}

}
