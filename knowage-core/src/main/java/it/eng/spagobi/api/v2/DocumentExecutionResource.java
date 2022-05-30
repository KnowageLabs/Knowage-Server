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
package it.eng.spagobi.api.v2;

import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_OPTIONS_KEY;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_QUANTITY_JSON;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_TYPE_JSON;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import com.google.common.collect.BiMap;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.RequestContainerAccess;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.BusinessModelOpenUtils;
import it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.AbstractDriverRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelDriverRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentDriverRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DriversRuntimeLoaderFactory;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.api.BusinessModelOpenParameters;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIMetaModelParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IBinContentDAO;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.utilities.DateRangeDAOUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.profiling.PublicProfile;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.dataset.bo.BIObjDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.dao.IBIObjDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.AbstractEngineStartAction;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/2.0/documentexecution")
@ManageAuthorization
public class DocumentExecutionResource extends AbstractSpagoBIResource {

	// GENERAL METADATA NAMES
	private static final String LABEL = "metadata.docLabel";
	private static final String NAME = "metadata.docName";
	private static final String DESCR = "metadata.docDescr";
	private static final String TYPE = "metadata.docType";
	private static final String ENG_NAME = "metadata.docEngine";
	private static final String RATING = "metadata.docRating";
	private static final String SUBOBJ_NAME = "metadata.subobjName";
	private static final String METADATA = "METADATA";
	private static final String NODE_ID_SEPARATOR = "___SEPA__";
	private static final String EDIT_MODE_ON = "true";
	private static final String PROPERTY_DATA = "data";
	private static final String PROPERTY_METADATA = "metadata";

	public static String MODE_SIMPLE = "simple";
	// public static String MODE_COMPLETE = "complete";
	// public static String START = "start";
	// public static String LIMIT = "limit";
	public String runDocumentExecution = SingletonConfig.getInstance().getConfigValue("document.execution.startAutomatically");

	public static final String SERVICE_NAME = "DOCUMENT_EXECUTION_RESOURCE";
	private static final String DESCRIPTION_FIELD = "description";

	private static final String VALUE_FIELD = "value";

	private static final String LABEL_FIELD = "label";

	private static IMessageBuilder message = MessageBuilderFactory.getMessageBuilder();

	private static final String[] VISIBLE_COLUMNS = new String[] { VALUE_FIELD, LABEL_FIELD, DESCRIPTION_FIELD };
	private static final String METADATA_DIR = "metadata";

	private class DocumentExecutionException extends Exception {
		private static final long serialVersionUID = -1882998632783944575L;

		DocumentExecutionException(String message) {
			super(message);
		}
	}

	static protected Logger logger = Logger.getLogger(DocumentExecutionResource.class);

	/**
	 * @return { executionURL: 'http:...', errors: 1 - 'role missing' 2 -'Missing paramters' [list of missing mandatory filters ] 3 -'operation not allowed' [if
	 *         the request role is not owned by the requesting user] }
	 * @throws JSONException
	 * @throws IOException
	 * @throws EMFInternalError
	 */
	@POST
	@Path("/url")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionURL(@Context HttpServletRequest req) throws IOException, JSONException {

		logger.debug("IN");
		Monitor getDocumentExecutionURLMonitor = MonitorFactory.start("Knowage.DocumentExecutionResource.getDocumentExecutionURL");

		Monitor getDocumentExecutionURLIntroMonitor = MonitorFactory.start("Knowage.DocumentExecutionResource.getDocumentExecutionURL.intro");

		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		String label = requestVal.getString("label");
		String role = requestVal.getString("role");
		String modality = requestVal.optString("modality");

		String engineParam = "";

		String sbiExecutionId = requestVal.optString("SBI_EXECUTION_ID");

		String isForExport = requestVal.optString("IS_FOR_EXPORT");
		// cockpit selections
		String cockpitSelections = requestVal.optString("COCKPIT_SELECTIONS");

		JSONObject jsonParameters = requestVal.optJSONObject("parameters");

		RequestContainer aRequestContainer = RequestContainerAccess.getRequestContainer(req);
		SessionContainer aSessionContainer = aRequestContainer.getSessionContainer();
		SessionContainer permanentSession = aSessionContainer.getPermanentContainer();

		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		List errorList = new ArrayList<>();
		Locale locale = GeneralUtilities.getCurrentLocale(aRequestContainer);
		JSONObject err = new JSONObject();
		JSONArray arrerr = new JSONArray();
		if (sbiExecutionId == null || sbiExecutionId.isEmpty()) {
			// CREATE EXECUTION ID
			UUIDGenerator uuidGen = UUIDGenerator.getInstance();
			UUID uuidObj = uuidGen.generateTimeBasedUUID();
			sbiExecutionId = uuidObj.toString();
			sbiExecutionId = sbiExecutionId.replaceAll("-", "");
		}
		resultAsMap.put("sbiExecutionId", sbiExecutionId);
		getDocumentExecutionURLIntroMonitor.stop();
		try {
			String executingRole = getExecutionRole(role);
			// displayToolbar
			// modality
			Monitor loadBIObjectForExecutionByLabelAndRoleMonitor = MonitorFactory
					.start("Knowage.DocumentExecutionResource.getDocumentExecutionURL.loadBIObjectForExecutionByLabelAndRole");
			BIObject obj = DriversRuntimeLoaderFactory.getDriversRuntimeLoader().loadBIObjectForExecutionByLabelAndRole(label, executingRole);
			loadBIObjectForExecutionByLabelAndRoleMonitor.stop();
			IParameterUseDAO parameterUseDAO = DAOFactory.getParameterUseDAO();
			// BUILD THE PARAMETERS
			Monitor buildJsonParametersMonitor = MonitorFactory.start("Knowage.DocumentExecutionResource.getDocumentExecutionURL.buildJsonParametersMonitor");
			DocumentRuntime dum = new DocumentRuntime(this.getUserProfile(), locale);
			JSONObject jsonParametersToSend = buildJsonParameters(jsonParameters, req, role, permanentSession, parameterUseDAO, obj, dum);
			buildJsonParametersMonitor.stop();
			// BUILD URL
			Monitor buildJUrlMonitor = MonitorFactory.start("Knowage.DocumentExecutionResource.getDocumentExecutionURL.buildUrl");

			String url = DocumentExecutionUtils.handleNormalExecutionUrl(this.getUserProfile(), obj, req, this.getAttributeAsString("SBI_ENVIRONMENT"),
					executingRole, modality, jsonParametersToSend, locale);

			if (!isOLAPSubObjectExecution(obj, requestVal)) {
				// in case of the execution of an OLAP subobject, we skip the validations on drivers for now
				// TODO implement validation also for OLAP subobjects
				errorList = DocumentExecutionUtils.handleNormalExecutionError(this.getUserProfile(), obj, req, this.getAttributeAsString("SBI_ENVIRONMENT"),
						executingRole, modality, jsonParametersToSend, locale);
			}

			engineParam = buildEngineUrlString(requestVal, obj, req, isForExport, cockpitSelections);

			url += "&SBI_EXECUTION_ID=" + sbiExecutionId;
			url += engineParam;

			String editMode = requestVal.optString("EDIT_MODE");
			if (!StringUtilities.isEmpty(editMode)) {
				url += "&EDIT_MODE=" + editMode;
			}

			resultAsMap.put("url", url);
			if (errorList != null && !errorList.isEmpty()) {
				resultAsMap.put("errors", errorList);
			}
			buildJUrlMonitor.stop();
			// ADD TYPE CODE
			// TODO return EXPORT FORMAT MAP
			resultAsMap.put("typeCode", obj.getBiObjectTypeCode());
			resultAsMap.put("engineLabel", obj.getEngine().getLabel());
		} catch (DocumentExecutionException e) {
			err.put("message", e.getMessage());
			err.put("type", "missingRole");
			arrerr.put(err);
			JSONObject toRet = new JSONObject();
			toRet.put("errors", arrerr);
			return Response.ok(toRet.toString()).build();
		} catch (Exception e) {
			logger.error("Error while getting the document execution url", e);
			err.put("message", e.getMessage());
			arrerr.put(err);
			JSONObject toRet = new JSONObject();
			toRet.put("errors", arrerr);
			return Response.ok(toRet.toString()).build();
		} finally {
			getDocumentExecutionURLMonitor.stop();
		}
		logger.debug("OUT");
		return Response.ok(resultAsMap).build();
	}

	private boolean isOLAPSubObjectExecution(BIObject obj, JSONObject requestAsJSON) throws JSONException {
		String subObjectId = null;
		JSONObject parameters = requestAsJSON.getJSONObject("parameters");
		if (parameters.length() > 0) {
			subObjectId = parameters.optString("subobj_id");
		}
		String documentTypeCode = obj.getBiObjectTypeCode();
		return documentTypeCode.equals(SpagoBIConstants.OLAP_TYPE_CODE) && !StringUtilities.isEmpty(subObjectId);
	}

	private String buildEngineUrlString(JSONObject reqVal, BIObject obj, HttpServletRequest req, String isForExport, String cockpitSelections)
			throws JSONException {
		Monitor buildEngineUrlStringMonitor = MonitorFactory.start("Knowage.DocumentExecutionResource.buildEngineUrlString");

		String ret = "";

		if (obj.getBiObjectTypeCode().equals(SpagoBIConstants.OLAP_TYPE_CODE)) {
			JSONObject parameters = reqVal.getJSONObject("parameters");
			if (parameters.length() > 0) {

				String subViewObjectID = parameters.optString("subobj_id");
				String subViewObjectName = parameters.optString("subobj_name");
				String subViewObjectDescription = parameters.optString("subobj_description");
				String subViewObjectVisibility = parameters.optString("subobj_visibility");

				if (!StringUtilities.isEmpty(subViewObjectID)) {
					ret += "&" + AbstractEngineStartAction.SUBOBJ_ID + "=" + subViewObjectID;
				}
				if (!StringUtilities.isEmpty(subViewObjectName)) {
					ret += "&" + AbstractEngineStartAction.SUBOBJ_NAME + "=" + subViewObjectName;
				}
				if (!StringUtilities.isEmpty(subViewObjectDescription)) {
					ret += "&" + AbstractEngineStartAction.SUBOBJ_DESCRIPTION + "=" + subViewObjectDescription;
				}
				if (!StringUtilities.isEmpty(subViewObjectVisibility)) {
					ret += "&" + AbstractEngineStartAction.SUBOBJ_VISIBILITY + "=" + subViewObjectVisibility;
				}
			}
		}

		// REPORT BIRT - JASPER
		// MOBILE
		if (obj.getBiObjectTypeCode().equals(SpagoBIConstants.REPORT_TYPE_CODE) && obj.getEngine() != null
				&& (obj.getEngine().getLabel().equals(SpagoBIConstants.BIRT_ENGINE_LABEL)
						|| obj.getEngine().getLabel().equals(SpagoBIConstants.JASPER_ENGINE_LABEL))
				&& (req.getHeader("User-Agent").indexOf("Mobile") != -1 || req.getHeader("User-Agent").indexOf("iPad") != -1
						|| req.getHeader("User-Agent").indexOf("iPhone") != -1)) {
			ret = ret + "&outputType=PDF";
		}
		// COCKPIT
		if (obj.getBiObjectTypeCode().equals(SpagoBIConstants.DOCUMENT_COMPOSITE_TYPE)) {
			if (!("".equalsIgnoreCase(isForExport))) {
				ret += "&IS_FOR_EXPORT=" + isForExport;
				if (!("".equalsIgnoreCase(cockpitSelections))) {
					ret += "&COCKPIT_SELECTIONS=" + cockpitSelections;
				}
			}
		}
		buildEngineUrlStringMonitor.stop();
		return ret;
	}

	private JSONObject buildJsonParameters(JSONObject jsonParameters, HttpServletRequest req, String role, SessionContainer permanentSession,
			IParameterUseDAO parameterUseDAO, BIObject obj, DocumentRuntime dum) throws JSONException, EMFUserError {
		List<DocumentDriverRuntime> parameters = DocumentExecutionUtils.getParameters(obj, role, req.getLocale(), null, null, false, dum);
		for (DocumentDriverRuntime objParameter : parameters) {
			Monitor checkingsParameterMonitor = MonitorFactory.start("Knowage.DocumentExecutionResource.buildJsonParameters.checkings");
			try {
				// SETTING DEFAULT VALUE IF NO PRESENT IN JSON SUBMIT PARAMETER
				if (jsonParameters.isNull(objParameter.getId())) {
					if (objParameter.getDefaultValues() != null && objParameter.getDefaultValues().size() > 0) {
						if (objParameter.getDefaultValues().size() == 1) {
							// SINGLE
							Object value;
							// DEFAULT DATE FIELD : {date#format}

							if (objParameter.getParType().equals("DATE") && objParameter.getDefaultValues().get(0).getValue().toString().contains("#")) {
								// CONVERT DATE FORMAT FROM DEFAULT TO SERVER
								value = convertDate(objParameter.getDefaultValues().get(0).getValue().toString().split("#")[1],
										// GeneralUtilities.getLocaleDateFormat(permanentSession),
										SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"),
										objParameter.getDefaultValues().get(0).getValue().toString().split("#")[0]);
							}

							// DEFAULT DATE RANGE FIELD : {date_2W#format}
							else if (objParameter.getParType().equals("DATE_RANGE")
									&& objParameter.getDefaultValues().get(0).getValue().toString().contains("#")) {
								String dateRange = objParameter.getDefaultValues().get(0).getValue().toString().split("#")[0];
								String[] dateRangeArr = dateRange.split("_");
								String range = "_" + dateRangeArr[dateRangeArr.length - 1];
								dateRange = dateRange.replace(range, "");
								// CONVERT DATE FORMAT FROM DEFAULT TO Server
								value = convertDate(objParameter.getDefaultValues().get(0).getValue().toString().split("#")[1],
										// GeneralUtilities.getLocaleDateFormat(permanentSession)
										SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"), dateRange);
								value = value + range;
							} else {
								value = objParameter.getDefaultValues().get(0).getValue();
							}
							jsonParameters.put(objParameter.getId(), value);
							jsonParameters.put(objParameter.getId() + "_field_visible_description", value);
						} else {
							// MULTIPLE
							ArrayList<String> paramValArr = new ArrayList<String>();
							String paramDescStr = "";
							for (int i = 0; i < objParameter.getDefaultValues().size(); i++) {
								paramValArr.add(objParameter.getDefaultValues().get(i).getValue().toString());
								paramDescStr = paramDescStr + objParameter.getDefaultValues().get(i).getValue().toString();
								if (i < objParameter.getDefaultValues().size() - 1) {
									paramDescStr = paramDescStr + ";";
								}
							}
							jsonParameters.put(objParameter.getId(), paramValArr);
							jsonParameters.put(objParameter.getId() + "_field_visible_description", paramDescStr);
						}
					}
				}
			} finally {
				checkingsParameterMonitor.stop();
			}

			ParameterUse parameterUse = null;

			// SUBMIT LOV SINGLE MANDATORY PARAMETER
			Monitor lovSingleMandatoryParameterMonitor = MonitorFactory
					.start("Knowage.DocumentExecutionResource.buildJsonParameters.singleLovMandatoryParameter");
			try {
				if (jsonParameters.isNull(objParameter.getId()) && objParameter.isMandatory()) {
					Integer paruseId = objParameter.getParameterUseId();
					parameterUse = parameterUseDAO.loadByUseID(paruseId);
					if ("lov".equalsIgnoreCase(parameterUse.getValueSelection())
							&& !objParameter.getSelectionType().equalsIgnoreCase(DocumentExecutionUtils.SELECTION_TYPE_TREE)
							&& (objParameter.getLovDependencies() == null || objParameter.getLovDependencies().size() == 0)) {
						HashMap<String, Object> defaultValuesData = DocumentExecutionUtils.getLovDefaultValues(role, obj, objParameter.getDriver(), req);

						ArrayList<HashMap<String, Object>> defaultValues = (ArrayList<HashMap<String, Object>>) defaultValuesData
								.get(DocumentExecutionUtils.DEFAULT_VALUES);

						if (defaultValues != null && defaultValues.size() == 1 && !defaultValues.get(0).containsKey("error")) {
							jsonParameters.put(objParameter.getId(), defaultValues.get(0).get("value"));
							jsonParameters.put(objParameter.getId() + "_field_visible_description", defaultValues.get(0).get("value"));
						}
					}
				}
			} finally {
				lovSingleMandatoryParameterMonitor.stop();
			}

			// commented out because of KNOWAGE-3900: it is breaking member's name syntax
			// // CROSS NAV : INPUT PARAM PARAMETER TARGET DOC IS STRING
			// Monitor crossNavParameterMonitor = MonitorFactory.start("Knowage.DocumentExecutionResource.buildJsonParameters.crossNavParameter");
			// try {
			// if (!jsonParameters.isNull(objParameter.getId())) {
			// Integer paruseId = objParameter.getParameterUseId();
			// if (parameterUse == null) {
			// parameterUse = parameterUseDAO.loadByUseID(paruseId);
			// }
			// if (jsonParameters.getString(objParameter.getId()).startsWith("[") && jsonParameters.getString(objParameter.getId()).endsWith("]")
			// && parameterUse.getValueSelection().equals("man_in")) {
			// int strLength = jsonParameters.getString(objParameter.getId()).toString().length();
			// String jsonParamRet = jsonParameters.getString(objParameter.getId()).toString().substring(1, strLength - 1);
			// if (objParameter.isMultivalue()) {
			// jsonParamRet = jsonParamRet.replaceAll("\"", "'");
			// }
			// jsonParameters.put(objParameter.getId(), jsonParamRet);
			// }
			//
			// }
			// } finally {
			// crossNavParameterMonitor.stop();
			// }

		}

		return jsonParameters;
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

	/**
	 * @return { filterStatus: [{ title: 'Provincia', urlName: 'provincia', type: 'list', lista:[[k,v],[k,v], [k,v]] }, { title: 'Comune', urlName: 'comune',
	 *         type: 'list', lista:[], dependsOn: 'provincia' }, { title: 'Free Search', type: 'manual', urlName: 'freesearch' }], isReadyForExecution: true,
	 *         errors: [ 'role missing', 'operation not allowed' ] }
	 * @throws EMFUserError
	 * @throws JSONException
	 * @throws IOException
	 */
	@POST
	@Path("/filters")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionFilters(@Context HttpServletRequest req) throws DocumentExecutionException, EMFUserError, IOException, JSONException {

		logger.debug("IN");

		RequestContainer aRequestContainer = RequestContainerAccess.getRequestContainer(req);
		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		// decode requestVal parameters
		JSONObject requestValParams = requestVal.getJSONObject("parameters");
		if (requestValParams != null && requestValParams.length() > 0)
			requestVal.put("parameters", decodeRequestParameters(requestValParams));
		String label = requestVal.getString("label");
		String role = requestVal.getString("role");
		JSONObject jsonCrossParameters = requestVal.getJSONObject("parameters");

		Map<String, Object> resultAsMap = new LinkedHashMap<>();

		boolean driversCacheEnabled = false;
		Map<String, JSONObject> sessionParametersMap = new HashMap<String, JSONObject>();
		if (("true").equals(SingletonConfig.getInstance().getConfigValue("SPAGOBI.SESSION_PARAMETERS_MANAGER.enabled"))) {
			sessionParametersMap = getSessionParameters(requestVal);
			driversCacheEnabled = true;
		}

		// keep track of par coming from cross to get descriptions from admissible values
		List<String> parsFromCross = new ArrayList<>();

		IParameterUseDAO parameterUseDAO = DAOFactory.getParameterUseDAO();
		BIObject biObject = DriversRuntimeLoaderFactory.getDriversRuntimeLoader().loadBIObjectForExecutionByLabelAndRole(label, role);

		Locale locale = GeneralUtilities.getCurrentLocale(aRequestContainer);

		applyRequestParameters(biObject, jsonCrossParameters, sessionParametersMap, role, locale, parsFromCross);

		final ArrayList<HashMap<String, Object>> parametersArrayList = new ArrayList<>();
		DocumentRuntime dum = new DocumentRuntime(this.getUserProfile(), locale);
		List<DocumentDriverRuntime> parameters = DocumentExecutionUtils.getParameters(biObject, role, req.getLocale(), null, parsFromCross, true, dum);

		ArrayList<HashMap<String, Object>> datasetParametersArrayList = new ArrayList<>();
		datasetParametersArrayList = getQbeDrivers(biObject);

		if (!datasetParametersArrayList.isEmpty()) {
			parametersArrayList.addAll(datasetParametersArrayList);
		} else {
			for (DocumentDriverRuntime objParameter : parameters) {
				Integer paruseId = objParameter.getParameterUseId();
				ParameterUse parameterUse = parameterUseDAO.loadByUseID(paruseId);
				Map<String, Object> metadata = new LinkedHashMap<>();
				BiMap<String, String> colPlaceholder2ColName = objParameter.getColPlaceholder2ColName();
				String lovDescriptionColumnName = objParameter.getLovDescriptionColumnName();
				String lovValueColumnName = objParameter.getLovValueColumnName();

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
									itemVal = URLDecoder.decode(itemVal.replace("+", "%2B"), "UTF-8");
								}
								if (!itemDescr.contains("%")) {
									itemDescr = URLDecoder.decode(itemDescr.replace("+", "%2B"), "UTF-8");
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
							paramValues = URLDecoder.decode(((String) paramValues).replace("+", "%2B"), "UTF-8");
						}
						paramValueLst.add(paramValues.toString());

						String parDescrVal = paramDescriptionValues != null && paramDescriptionValues instanceof String ? paramDescriptionValues.toString()
								: paramValues.toString();
						if (!parDescrVal.contains("%")) {
							parDescrVal = URLDecoder.decode(parDescrVal.replace("+", "%2B"), "UTF-8");
						}
						paramDescrLst.add(parDescrVal);

					}

					parameterAsMap.put("parameterValue", paramValueLst);
					parameterAsMap.put("parameterDescription", paramDescriptionValues);
				}

				boolean showParameterLov = true;

				// Parameters NO TREE
				if ("lov".equalsIgnoreCase(parameterUse.getValueSelection())) {

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
						checkIfValuesAreAdmissible(values, admissibleValues);
					}

				}

				// DATE RANGE DEFAULT VALUE
				if (objParameter.getParType().equals("DATE_RANGE")) {
					try {
						ArrayList<HashMap<String, Object>> defaultValues = manageDataRange(biObject, role, objParameter.getId());
						parameterAsMap.put(PROPERTY_DATA, defaultValues);
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

				addDependencies(objParameter, parameterAsMap);

				// load DEFAULT VALUE if present and if the parameter value is empty

				Object defValue = null;
				if (objParameter.getDefaultValues() != null && objParameter.getDefaultValues().size() > 0
						&& objParameter.getDefaultValues().get(0).getValue() != null) {
					DefaultValuesList valueList = null;
					// check if the parameter is really valorized (for example if it isn't an empty list)
					List lstValues = (List) parameterAsMap.get("parameterValue");
					if (lstValues.size() == 0)
						jsonCrossParameters.remove(objParameter.getId());

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

					if (jsonCrossParameters.isNull(objParameter.getId())
							// && !sessionParametersMap.containsKey(objParameter.getId())) {
							&& !sessionParametersMap.containsKey(sessionKey)) {
						if (valueList != null) {
							parameterAsMap.put("parameterValue", valueList);
						}
					}

				}

				// in every case fill default values!
				parameterAsMap.put("driverDefaultValue", defValue);

				LovValue maxValue = objParameter.getMaxValue();
				if (maxValue != null && maxValue.getValue() != null) {
					parameterAsMap.put("driverMaxValue", maxValue.getValue().toString());
				}

				if (!showParameterLov) {
					parameterAsMap.put("showOnPanel", "false");
				} else {
					parameterAsMap.put("showOnPanel", "true");
				}

				parametersArrayList.add(parameterAsMap);

			}
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

		if (runDocumentExecution == null || runDocumentExecution.equalsIgnoreCase("true")) {
			resultAsMap.put("isReadyForExecution", isReadyForExecution(parameters));
		} else if (runDocumentExecution.equalsIgnoreCase("false")) {
			resultAsMap.put("isReadyForExecution", false);
		} else {
			throw new SpagoBIRuntimeException(
					"The value of configuration variable document.execution.startAutomatically is not valid, contact your administrator");
		}

		logger.debug("OUT");
		return Response.ok(resultAsMap).build();
	}

	// private List<AbstractDriverRuntime<AbstractDriver>>

	protected JSONObject decodeRequestParameters(JSONObject requestValParams) throws JSONException, IOException {
		JSONObject toReturn = new JSONObject();

		Iterator keys = requestValParams.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object valueObj = requestValParams.get(key);
			if (valueObj instanceof Number) {
				String value = String.valueOf(valueObj);
				// if (!value.equals("%7B%3B%7B") && !value.equalsIgnoreCase("%")) {
				if (!value.equals("") && !value.equalsIgnoreCase("%")) {
					toReturn.put(key, URLDecoder.decode(value.replaceAll("%", "%25").replace("+", "%2B"), "UTF-8"));
				} else {
					toReturn.put(key, value); // uses the original value for list and %
				}
			} else if (valueObj instanceof String) {
				String value = String.valueOf(valueObj);
				// if (!value.equals("%7B%3B%7B") && !value.equalsIgnoreCase("%")) {
				if (!value.equals("") && !value.equalsIgnoreCase("%")) {
					toReturn.put(key, URLDecoder.decode(value.replaceAll("%", "%25").replace("+", "%2B"), "UTF-8"));
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
						ValuesLstDecoded.put(URLDecoder.decode(value.replaceAll("%", "%25").replace("+", "%2B"), "UTF-8"));
					} else {
						ValuesLstDecoded.put(value);
						URLDecoder.decode(value.replaceAll("%", "%25").replace("+", "%2B"), "UTF-8"); // uses the original value for list and %
					}
				}
				toReturn.put(key, ValuesLstDecoded);
			} else if (valueObj instanceof JSONObject) {
				JSONObject valuesLst = (JSONObject) valueObj;
				JSONArray ValuesLstDecoded = new JSONArray();

				Iterator keysObject = valuesLst.keys();
				while (keysObject.hasNext()) {
					String keyObj = (String) keysObject.next();
					Object valueOb = valuesLst.get(keyObj);
					String value = String.valueOf(valueOb);
					// if (!value.equals("%7B%3B%7B") && !value.equalsIgnoreCase("%")) {
					if (!value.equals("") && !value.equalsIgnoreCase("%")) {
						ValuesLstDecoded.put(URLDecoder.decode(value.replaceAll("%", "%25").replace("+", "%2B"), "UTF-8"));
					} else {
						ValuesLstDecoded.put(value); // uses the original value for list and %
					}
				}

				toReturn.put(key, ValuesLstDecoded);
			}
		}

		return toReturn;
	}

	private void checkIfValuesAreAdmissible(Object values, ArrayList<HashMap<String, Object>> admissibleValues) {
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

	protected Map<String, JSONObject> getSessionParameters(JSONObject requestVal) {

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

	private boolean isReadyForExecution(List<DocumentDriverRuntime> parameters) {
		for (DocumentDriverRuntime parameter : parameters) {
			List values = parameter.getDriver().getParameterValues();
			// if parameter is mandatory and has no value, execution cannot start automatically
			if (parameter.isMandatory() && (values == null || values.isEmpty())) {
				logger.debug("Parameter [" + parameter.getId() + "] is mandatory but has no values. Execution cannot start automatically");
				return false;
			}
		}
		return true;
	}

	private void applyRequestParameters(BIObject biObject, JSONObject crossNavigationParametesMap, Map<String, JSONObject> sessionParametersMap, String role,
			Locale locale, List<String> parsFromCross) {
		DocumentRuntime documentUrlManager = new DocumentRuntime(this.getUserProfile(), locale);
		List<BIObjectParameter> parameters = biObject.getDrivers();
		for (BIObjectParameter parameter : parameters) {
			if (crossNavigationParametesMap.has(parameter.getParameterUrlName())) {
				logger.debug("Found value from request for parmaeter [" + parameter.getParameterUrlName() + "]");
				documentUrlManager.refreshParameterForFilters(parameter, crossNavigationParametesMap);
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

	private ArrayList<HashMap<String, Object>> manageDataRange(BIObject biObject, String executionRole, String biparameterId)
			throws EMFUserError, SerializationException, JSONException, IOException {

		BIObjectParameter biObjectParameter = null;
		List parameters = biObject.getDrivers();
		for (int i = 0; i < parameters.size(); i++) {
			BIObjectParameter p = (BIObjectParameter) parameters.get(i);
			if (biparameterId.equalsIgnoreCase(p.getParameterUrlName())) {
				biObjectParameter = p;
				break;
			}
		}

		try {
			if (DateRangeDAOUtilities.isDateRange(biObjectParameter)) {
				logger.debug("loading date range combobox");

			}
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Error on loading date range combobox values", e);
		}

		Integer parID = biObjectParameter.getParID();
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

	// private static int getDataRangeOptionsSize(String options) throws JSONException {
	// JSONObject json = new JSONObject(options);
	// JSONArray res = json.getJSONArray(DATE_RANGE_OPTIONS_KEY);
	// return res.length();
	// }

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
	@POST
	@Path("/admissibleValuesTree")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getParameterValuesV2(@Context HttpServletRequest req) throws EMFUserError, IOException, JSONException {
		return getParameterValues(req);
	}

	@POST
	@Path("/parametervalues")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@Deprecated
	public Response getParameterValues(@Context HttpServletRequest req) throws EMFUserError, IOException, JSONException {

		RequestContainer aRequestContainer = RequestContainerAccess.getRequestContainer(req);
		Locale locale = GeneralUtilities.getCurrentLocale(aRequestContainer);

		String role;
		String label;
		String biparameterId;
		String treeLovNode;
		String mode;
		// GET PARAMETER

		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		role = (String) requestVal.opt("role");
		label = (String) requestVal.opt("label");
		biparameterId = (String) requestVal.opt("parameterId");
		treeLovNode = (String) requestVal.opt("treeLovNode");
		mode = (String) requestVal.opt("mode");

		BIObject biObject = DriversRuntimeLoaderFactory.getDriversRuntimeLoader().loadBIObjectForExecutionByLabelAndRole(label, role);

		BIObjectParameter biObjectParameter = null;
		List<BIObjectParameter> parameters = biObject.getDrivers();
		for (int i = 0; i < parameters.size(); i++) {
			BIObjectParameter p = parameters.get(i);
			if (biparameterId.equalsIgnoreCase(p.getParameterUrlName())) {
				biObjectParameter = p;
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

		Map<String, Object> defaultValuesData = DocumentExecutionUtils.getLovDefaultValues(role, biObject, biObjectParameter, requestVal, treeLovNodeLevel,
				treeLovNodeValue, req.getLocale());

		ArrayList<Map<String, Object>> result = (ArrayList<Map<String, Object>>) defaultValuesData.get(DocumentExecutionUtils.DEFAULT_VALUES);

		Map<String, Object> resultAsMap = new HashMap<String, Object>();

		if (result != null && result.size() > 0) {
			resultAsMap.put("rows", result);
		} else {
			List errorList = DocumentExecutionUtils.handleNormalExecutionError(this.getUserProfile(), biObject, req,
					this.getAttributeAsString("SBI_ENVIRONMENT"), role, biObjectParameter.getParameter().getModalityValue().getSelectionType(), null, locale);

			resultAsMap.put("errors", errorList);
		}

		logger.debug("OUT");
		return Response.ok(resultAsMap).build();
	}

	/**
	 * @return the list of values when input parameter (urlName) is correlated to another
	 */
	@GET
	@Path("/filterlist")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionFilterList(@QueryParam("label") String label, @QueryParam("role") String role,
			@QueryParam("parameters") String jsonParameters, @QueryParam("urlName") String urlName, @Context HttpServletRequest req) {
		logger.debug("IN");

		String toBeReturned = "{}";

		try {
			role = getExecutionRole(role);

		} catch (DocumentExecutionException e) {
			return Response.ok("{errors: '" + e.getMessage() + "', }").build();
		} catch (Exception e) {
			logger.error("Error while getting the document execution filterlist", e);
			throw new SpagoBIRuntimeException("Error while getting the document execution filterlist", e);
		}

		logger.debug("OUT");
		return Response.ok(toBeReturned).build();
	}

	/**
	 * @return the list of values when input parameter (urlName) is correlated to another
	 */
	@POST
	@Path("/canHavePublicExecutionUrl")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response canHavePublicExecutionUrl(@Context HttpServletRequest req) {
		logger.debug("IN");

		Boolean toReturn = false;
		Boolean noPublicRoleError = false;
		JSONObject results = new JSONObject();
		try {
			BIObject biObj;
			UserProfile currentProfile = getUserProfile();
			String tenant = currentProfile.getOrganization();

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
			String label = requestVal.getString("label");

			biObj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);
			if (biObj != null) {
				SpagoBIUserProfile publicProfile = PublicProfile.createPublicUserProfile(PublicProfile.PUBLIC_USER_PREFIX + tenant);

				if (publicProfile == null) {
					noPublicRoleError = true;
					toReturn = false;
				} else {
					UserProfile publicUserProfile = new UserProfile(publicProfile);
					boolean canExec = ObjectsAccessVerifier.canExec(biObj, publicUserProfile);
					toReturn = canExec;
				}

				results.put("isPublic", toReturn);
				results.put("noPublicRoleError", noPublicRoleError);

			} else {
				logger.error("Object with label " + label + " not found");
				throw new SpagoBIRuntimeException("Object with label " + label + " not found");
			}
		} catch (Exception e) {
			logger.error("Exception when testing public execution possibility", e);
			throw new SpagoBIRuntimeException("Exception when testing public execution possibility", e);
		}

		logger.debug("OUT");

		return Response.ok(results.toString()).build();
	}

	/*
	 * File Upload to local temp directory
	 */
	@POST
	@Path("/uploadfilemetadata")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_METADATA_MANAGEMENT })
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON })
	public Response uploadFile(MultiPartBody input) {

		byte[] bytes = null;

		try {

			if (input != null) {

				String saveDirectoryPath = SpagoBIUtilities.getResourcePath() + "/" + METADATA_DIR + "/" + getUserProfile().getUserName().toString();
				final FormFile file = input.getFormFileParameterValues("file")[0];
				bytes = file.getContent();

				File saveDirectory = new File(saveDirectoryPath);
				if (!(saveDirectory.exists() && saveDirectory.isDirectory())) {
					saveDirectory.mkdirs();
				}
				String tempFile = saveDirectoryPath + "/" + file.getFileName();
				File tempFileToSave = new File(tempFile);
				tempFileToSave.createNewFile();
				DataOutputStream os = new DataOutputStream(new FileOutputStream(tempFileToSave));
				os.write(bytes);
				os.close();

			}

		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Error inserting new file metadataContent ", e);
		}

		return Response.status(200).build();

	}

	/**
	 * Produces a json with a bynary content of a metadata file and its name
	 *
	 * @param id          of document
	 * @param id          of subObject
	 * @param id          of a metaData
	 * @param httpRequest
	 * @return a response with a json
	 * @throws EMFUserError
	 */
	@GET
	@Path("/{id}/{metadataObjectId}/documentfilemetadata")
	public Response documentFileMetadata(@PathParam("id") Integer objectId, @PathParam("metadataObjectId") Integer metaObjId,
			@Context HttpServletRequest httpRequest) throws EMFUserError {
		try {
			Integer subObjectId = null;
			IObjMetacontentDAO metacontentDAO = DAOFactory.getObjMetacontentDAO();

			ObjMetacontent metacontent = metacontentDAO.loadObjMetacontent(metaObjId, objectId, subObjectId);
			JSONObject additionalInfoJSON = new JSONObject(metacontent.getAdditionalInfo());
			String fileName = additionalInfoJSON.getString("fileName");

			int binaryContentId = metacontent.getBinaryContentId();
			IBinContentDAO binaryContentDAO = DAOFactory.getBinContentDAO();
			byte[] fileContent = binaryContentDAO.getBinContent(binaryContentId);

			ResponseBuilder response = Response.ok(fileContent);
			response.header("Content-Disposition", "attachment; filename=" + fileName);

			return response.build();

		} catch (Exception e) {
			logger.error(httpRequest.getPathInfo(), e);
			throw new SpagoBIRuntimeException("Error returning file.", e);
		}

	}

	// @GET
	// @Path("/{id}/{metadataObjectId}/deletefilemetadata")
	// // (delete a metacontent)
	// public Response cleanFileMetadata(@PathParam("id") Integer objectId, @PathParam("metadataObjectId") Integer metaObjId,
	// @Context HttpServletRequest httpRequest) throws EMFUserError {
	// try {
	// Integer subObjectId = null;
	// IObjMetacontentDAO metacontentDAO = DAOFactory.getObjMetacontentDAO();
	//
	// ObjMetacontent metacontent = metacontentDAO.loadObjMetacontent(metaObjId, objectId, subObjectId);
	// JSONObject additionalInfoJSON = new JSONObject(metacontent.getAdditionalInfo());
	// String fileName = additionalInfoJSON.getString("fileName");
	//
	// String filePath = SpagoBIUtilities.getResourcePath() + "/" + METADATA_DIR + "/" + getUserProfile().getUserName().toString() + "/" + fileName;
	// metacontentDAO.eraseObjMetadata(metacontent);
	//
	// File metadataTempFile = new File(filePath);
	// if (metadataTempFile.exists()) {
	// metadataTempFile.delete();
	// }
	//
	// ResponseBuilder response = Response.ok();
	// return response.build();
	//
	// } catch (Exception e) {
	// logger.error(httpRequest.getPathInfo(), e);
	// throw new SpagoBIRuntimeException("Error returning file.", e);
	// }
	//
	// }

	@SuppressWarnings("resource")
	private byte[] getFileByteArray(String filePath, String fileName) throws IOException {

		filePath = filePath + "/" + fileName;
		File file = new File(filePath);
		FileInputStream fis = null;
		byte[] bFile = null;
		try {
			fis = new FileInputStream(file);
			bFile = new byte[(int) file.length()];

			// convert file into array of bytes
			fis = new FileInputStream(file);
			fis.read(bFile);
			fis.close();
		} catch (IOException e) {
			throw new IOException("Error reading " + filePath + " file.", e);
		}
		return bFile;

	}

	protected String getExecutionRole(String role) throws EMFInternalError, DocumentExecutionException {
		UserProfile userProfile = getUserProfile();
		if (role != null && !role.equals("")) {
			logger.debug("role for document execution: " + role);
		} else {
			if (userProfile.getRoles().size() == 1) {
				role = userProfile.getRoles().iterator().next().toString();
				logger.debug("profile role for document execution: " + role);
			} else {
				logger.debug("missing role for document execution, role:" + role);
				throw new DocumentExecutionException(message.getMessage("SBIDev.docConf.execBIObject.selRoles.Title"));
			}
		}

		return role;
	}

	private DefaultValuesList buildParameterSessionValueList(String sessionParameterValue, String sessionParameterDescription, BIObjectParameter objParameter) {

		logger.debug("IN");

		DefaultValuesList valueList = new DefaultValuesList();

		SimpleDateFormat serverDateFormat = new SimpleDateFormat(SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"));

		if (objParameter.getParameter().getType().equals("DATE")) {
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
		} else if (objParameter.getParameter().getType().equals("DATE_RANGE")) {
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

		else if (objParameter.isMultivalue()) {
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

	private String convertDate(String dateFrom, String dateTo, String dateStr) {
		String date = dateStr;
		SimpleDateFormat dateFromFormat = new SimpleDateFormat(dateFrom);
		try {
			Date d = dateFromFormat.parse(dateStr);
			Format formatter = new SimpleDateFormat(dateTo);
			date = formatter.format(d);
			// jsonParameters.put(objParameter.getId(), date);
		} catch (ParseException e) {
			logger.error("Error prase date server ", e);

		}
		return date;
	}

	private static void addToZipFile(String filePath, String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException {

		File file = new File(filePath + "/" + fileName);
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();
	}

	private static void deleteDirectoryContent(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectoryContent(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
	}

	private String getFileName(MultivaluedMap<String, String> header) {
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return null;
	}

	@SuppressWarnings("resource")
	private byte[] getFileByteArray(String filePath) throws IOException {
		File file = new File(filePath);
		FileInputStream fis = null;
		byte[] bFile = null;
		try {
			fis = new FileInputStream(file);
			bFile = new byte[(int) file.length()];

			// convert file into array of bytes
			fis = new FileInputStream(file);
			fis.read(bFile);
			fis.close();
		} catch (IOException e) {
			throw new IOException("Error reading " + filePath + " file.", e);
		}
		return bFile;

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
					parameterAsMap.put(PROPERTY_DATA, admissibleValues);
				} else {
					parameterAsMap.put(PROPERTY_DATA, new ArrayList<>());
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
					parameterAsMap.put(PROPERTY_DATA, defaultValues);
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

	private void addDependencies(AbstractDriverRuntime<?> objParameter, HashMap<String, Object> parameterAsMap) {
		Map<String, Object> dependencies = new LinkedHashMap<>();
		parameterAsMap.put("dependencies", dependencies);

		dependencies.put("data", objParameter.getDataDependencies());
		dependencies.put("visual", objParameter.getVisualDependencies());
		dependencies.put("lov", Optional.ofNullable(objParameter.getLovDependencies()).orElse(Collections.emptyList()));
	}

}
