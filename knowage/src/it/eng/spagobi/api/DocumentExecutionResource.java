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
package it.eng.spagobi.api;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.AnalyticalModelDocumentManagementAPI;
import it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentParameters;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentUrlManager;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.indexing.LuceneIndexer;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Path("/1.0/documentexecution")
@ManageAuthorization
public class DocumentExecutionResource extends AbstractSpagoBIResource {

	// GENERAL METADATA NAMES
	public static final String LABEL = "metadata.docLabel";
	public static final String NAME = "metadata.docName";
	public static final String DESCR = "metadata.docDescr";
	public static final String TYPE = "metadata.docType";
	public static final String ENG_NAME = "metadata.docEngine";
	public static final String RATING = "metadata.docRating";
	public static final String SUBOBJ_NAME = "metadata.subobjName";
	public static final String METADATA = "METADATA";
	public static final String NODE_ID_SEPARATOR = "___SEPA__";

	// public static final String PARAMETERS = "PARAMETERS";
	// public static final String SERVICE_NAME = "GET_URL_FOR_EXECUTION_ACTION";
	public static String MODE_SIMPLE = "simple";
	// public static String MODE_COMPLETE = "complete";
	// public static String START = "start";
	// public static String LIMIT = "limit";

	private static IMessageBuilder message = MessageBuilderFactory.getMessageBuilder();

	private class DocumentExecutionException extends Exception {
		private static final long serialVersionUID = -1882998632783944575L;

		DocumentExecutionException(String message) {
			super(message);
		}
	}

	static protected Logger logger = Logger.getLogger(DocumentExecutionResource.class);
	protected AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());

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
		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		String label = requestVal.getString("label");
		String role = requestVal.getString("role");
		String modality = requestVal.optString("modality");
		String displayToolbar = requestVal.optString("displayToolbar");
		String snapshotId = requestVal.optString("snapshotId");
		String subObjectID = requestVal.optString("subObjectID");
		String sbiExecutionId = requestVal.optString("SBI_EXECUTION_ID");
		JSONObject jsonParameters = requestVal.optJSONObject("parameters");

		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		List errorList = new ArrayList<>();
		MessageBuilder m = new MessageBuilder();
		Locale locale = m.getLocale(req);

		if (sbiExecutionId == null || sbiExecutionId.isEmpty()) {
			// create execution id
			UUIDGenerator uuidGen = UUIDGenerator.getInstance();
			UUID uuidObj = uuidGen.generateTimeBasedUUID();
			sbiExecutionId = uuidObj.toString();
			sbiExecutionId = sbiExecutionId.replaceAll("-", "");
		}
		resultAsMap.put("sbiExecutionId", sbiExecutionId);

		try {
			String executingRole = getExecutionRole(role);
			// displayToolbar
			// modality
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByLabelAndRole(label, executingRole);
			// List<DocumentParameters> parameters = DocumentExecutionUtils.getParameters(obj, executingRole, locale, modality);
			String url = DocumentExecutionUtils.handleNormalExecutionUrl(this.getUserProfile(), obj, req, this.getAttributeAsString("SBI_ENVIRONMENT"),
					executingRole, modality, jsonParameters, locale);
			errorList = DocumentExecutionUtils.handleNormalExecutionError(this.getUserProfile(), obj, req, this.getAttributeAsString("SBI_ENVIRONMENT"),
					executingRole, modality, jsonParameters, locale);
			// resultAsMap.put("parameters", parameters);
			resultAsMap.put("url", url + "&SBI_EXECUTION_ID=" + sbiExecutionId);
			if (!errorList.isEmpty()) {
				resultAsMap.put("errors", errorList);
			}

		} catch (DocumentExecutionException e) {
			logger.error("Error while getting the document execution url", e);
			JSONObject err = new JSONObject();
			err.put("message", e.getMessage());
			err.put("type", "missingRole");
			JSONArray arrerr = new JSONArray();
			arrerr.put(err);
			JSONObject toRet = new JSONObject();
			toRet.put("errors", arrerr);
			return Response.ok(toRet.toString()).build();
		} catch (Exception e) {
			logger.error("Error while getting the document execution url", e);
			throw new SpagoBIRuntimeException("Error while getting the document execution url", e);
		}

		logger.debug("OUT");
		return Response.ok(resultAsMap).build();
	}

	/**
	 * @return { filterStatus: [{ title: 'Provincia', urlName: 'provincia', type: 'list', lista:[[k,v],[k,v], [k,v]] }, { title: 'Comune', urlName: 'comune',
	 *         type: 'list', lista:[], dependsOn: 'provincia' }, { title: 'Free Search', type: 'manual', urlName: 'freesearch' }],
	 *
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
		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		String label = requestVal.getString("label");
		String role = requestVal.getString("role");
		JSONObject jsonParameters = requestVal.getJSONObject("parameters");

		String toBeReturned = "{}";
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();

		IBIObjectDAO dao = DAOFactory.getBIObjectDAO();
		IParameterUseDAO parameterUseDAO = DAOFactory.getParameterUseDAO();
		BIObject biObject = dao.loadBIObjectForExecutionByLabelAndRole(label, role);

		MessageBuilder m = new MessageBuilder();
		Locale locale = m.getLocale(req);
		DocumentUrlManager documentUrlManager = new DocumentUrlManager(this.getUserProfile(), locale);

		ArrayList<HashMap<String, Object>> parametersArrayList = new ArrayList<>();

		List<DocumentParameters> parameters = DocumentExecutionUtils.getParameters(biObject, role, req.getLocale(), null);
		for (DocumentParameters objParameter : parameters) {
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

			parameterAsMap
					.put("allowInternalNodeSelection", objParameter.getPar().getModalityValue().getLovProvider().contains("<LOVTYPE>treeinner</LOVTYPE>"));
			if (jsonParameters.has(objParameter.getId())) {
				documentUrlManager.refreshParameterForFilters(objParameter.getAnalyticalDocumentParameter(), jsonParameters);
				parameterAsMap.put("parameterValue", objParameter.getAnalyticalDocumentParameter().getParameterValues());
			}
			if ("lov".equalsIgnoreCase(parameterUse.getValueSelection()) && !objParameter.getSelectionType().equalsIgnoreCase("tree")) {
				ArrayList<HashMap<String, Object>> defaultValues = DocumentExecutionUtils.getLovDefaultValues(role, biObject,
						objParameter.getAnalyticalDocumentParameter(), req);
				parameterAsMap.put("defaultValues", defaultValues);

				// if parameterValue is not null and is array, check if all element are present in lov
				Object o = parameterAsMap.get("parameterValue");
				if (o != null) {
					if (o instanceof List) {
						List<String> valList = (ArrayList) o;
						for (int k = 0; k < valList.size(); k++) {
							String itemVal = valList.get(k);
							boolean finded = false;
							for (HashMap<String, Object> parHashVal : defaultValues) {
								if (parHashVal.containsKey("value") && parHashVal.get("value").equals(itemVal)) {
									finded = true;
									break;
								}
							}
							if (!finded) {
								valList.remove(k);
								k--;
							}
						}
					}
				}

			}
			parameterAsMap.put("dependsOn", objParameter.getDependencies());
			parameterAsMap.put("dataDependencies", objParameter.getDataDependencies());
			parameterAsMap.put("visualDependencies", objParameter.getVisualDependencies());

			// load, if present, the json parameters

			// if (jsonParameters.has(objParameter.getId())) {
			// parameterAsMap.put("parameterValue", jsonParameters.getString(objParameter.getId()));
			// }

			parametersArrayList.add(parameterAsMap);
		}
		if (parameters.size() > 0) {
			resultAsMap.put("filterStatus", parametersArrayList);
		} else {
			resultAsMap.put("filterStatus", new ArrayList<>());
		}

		logger.debug("OUT");
		return Response.ok(resultAsMap).build();
	}

	@POST
	@Path("/parametervalues")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	// public Response getParameterValues(@QueryParam("label") String label, @QueryParam("role") String role, @QueryParam("biparameterId") String biparameterId,
	// @QueryParam("mode") String mode, @QueryParam("treeLovNode") String treeLovNode,
	// // @QueryParam("treeLovNode") Integer treeLovNodeLevel,
	// @Context HttpServletRequest req) throws EMFUserError {
	public Response getParameterValues(@Context HttpServletRequest req) throws EMFUserError, IOException, JSONException {

		MessageBuilder msgBuild = new MessageBuilder();
		Locale locale = msgBuild.getLocale(req);

		String role;
		String label;
		String biparameterId;
		String treeLovNode;
		String mode;
		// GET PARAMETER

		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		role = (String) requestVal.opt("role");
		label = (String) requestVal.opt("label");
		biparameterId = (String) requestVal.opt("biparameterId");
		treeLovNode = (String) requestVal.opt("treeLovNode");
		mode = (String) requestVal.opt("mode");

		IBIObjectDAO dao = DAOFactory.getBIObjectDAO();
		BIObject biObject = dao.loadBIObjectForExecutionByLabelAndRole(label, role);

		BIObjectParameter biObjectParameter = null;
		List<BIObjectParameter> parameters = biObject.getBiObjectParameters();
		for (int i = 0; i < parameters.size(); i++) {
			BIObjectParameter p = parameters.get(i);
			if (biparameterId.equalsIgnoreCase(p.getParameterUrlName())) {
				biObjectParameter = p;
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

		ArrayList<HashMap<String, Object>> result = DocumentExecutionUtils.getLovDefaultValues(role, biObject, biObjectParameter, requestVal, treeLovNodeLevel,
				treeLovNodeValue, req);

		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();

		if (result.size() > 0) {
			resultAsMap.put("filterValues", result);
			resultAsMap.put("errors", new ArrayList<>());
		} else {
			resultAsMap.put("filterValues", new ArrayList<>());

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
	 * Produces a json of document metadata grouped by typeCode ("GENERAL_META", "LONG_TEXT", "SHORT_TEXT")
	 *
	 * @param id
	 *            of document
	 * @param id
	 *            of subObject
	 * @param httpRequest
	 * @return a response with a json
	 * @throws EMFUserError
	 */
	@GET
	@Path("/{id}/documentMetadata")
	public Response documentMetadata(@PathParam("id") Integer objectId, @QueryParam("subobjectId") Integer subObjectId, @Context HttpServletRequest httpRequest)
			throws EMFUserError {

		try {
			MessageBuilder msgBuild = new MessageBuilder();
			Locale locale = msgBuild.getLocale(httpRequest);

			Map<String, JSONArray> documentMetadataMap = new HashMap<>();

			JSONArray generalMetadata = new JSONArray();
			documentMetadataMap.put("GENERAL_META", generalMetadata);

			// START GENERAL METADATA
			if (subObjectId != null) {
				// SubObj Name
				String textSubName = msgBuild.getMessage(SUBOBJ_NAME, locale);
				SubObject subobj = DAOFactory.getSubObjectDAO().getSubObject(subObjectId);
				addMetadata(generalMetadata, textSubName, subobj.getName());
			}

			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(objectId);

			// Obj Label
			String textLabel = msgBuild.getMessage(LABEL, locale);
			addMetadata(generalMetadata, textLabel, obj.getLabel());

			// Obj Name
			String textName = msgBuild.getMessage(NAME, locale);
			addMetadata(generalMetadata, textName, obj.getName());

			// Obj Type
			String textType = msgBuild.getMessage(TYPE, locale);
			addMetadata(generalMetadata, textType, obj.getBiObjectTypeCode());

			// Obj Description
			String description = msgBuild.getMessage(DESCR, locale);
			addMetadata(generalMetadata, description, obj.getDescription());

			// Obj Engine Name
			String textEngName = msgBuild.getMessage(ENG_NAME, locale);
			addMetadata(generalMetadata, textEngName, obj.getEngine().getName());

			// END GENERAL METADATA

			List metadata = DAOFactory.getObjMetadataDAO().loadAllObjMetadata();
			if (metadata != null && !metadata.isEmpty()) {
				Iterator it = metadata.iterator();
				while (it.hasNext()) {
					ObjMetadata objMetadata = (ObjMetadata) it.next();
					ObjMetacontent objMetacontent = DAOFactory.getObjMetacontentDAO().loadObjMetacontent(objMetadata.getObjMetaId(), objectId, subObjectId);
					addTextMetadata(documentMetadataMap, objMetadata.getDataTypeCode(), objMetadata.getName(),
							objMetacontent != null && objMetacontent.getContent() != null ? new String(objMetacontent.getContent()) : "",
							objMetadata.getObjMetaId());
				}
			}

			if (!documentMetadataMap.isEmpty()) {
				return Response.ok(new JSONObject(documentMetadataMap).toString()).build();
			}
		} catch (Exception e) {
			logger.error(httpRequest.getPathInfo(), e);
		}

		return Response.ok().build();
	}

	@POST
	@Path("/saveDocumentMetadata")
	public Response saveDocumentMetadata(@Context HttpServletRequest httpRequest) throws JSONException {
		try {
			JSONObject params = RestUtilities.readBodyAsJSONObject(httpRequest);
			IObjMetacontentDAO dao = DAOFactory.getObjMetacontentDAO();
			dao.setUserProfile(getUserProfile());
			Integer biobjectId = params.getInt("id");
			Integer subobjectId = params.has("subobjectId") ? params.getInt("subobjectId") : null;
			String jsonMeta = params.getString("jsonMeta");

			logger.debug("Object id = " + biobjectId);
			logger.debug("Subobject id = " + subobjectId);

			JSONArray metadata = new JSONArray(jsonMeta);
			for (int i = 0; i < metadata.length(); i++) {
				JSONObject aMetadata = metadata.getJSONObject(i);
				Integer metadataId = aMetadata.getInt("id");
				String text = aMetadata.getString("value");
				ObjMetacontent aObjMetacontent = dao.loadObjMetacontent(metadataId, biobjectId, subobjectId);
				if (aObjMetacontent == null) {
					logger.debug("ObjMetacontent for metadata id = " + metadataId + ", biobject id = " + biobjectId + ", subobject id = " + subobjectId
							+ " was not found, creating a new one...");
					aObjMetacontent = new ObjMetacontent();
					aObjMetacontent.setObjmetaId(metadataId);
					aObjMetacontent.setBiobjId(biobjectId);
					aObjMetacontent.setSubobjId(subobjectId);
					aObjMetacontent.setContent(text.getBytes("UTF-8"));
					aObjMetacontent.setCreationDate(new Date());
					aObjMetacontent.setLastChangeDate(new Date());
					dao.insertObjMetacontent(aObjMetacontent);
				} else {
					logger.debug("ObjMetacontent for metadata id = " + metadataId + ", biobject id = " + biobjectId + ", subobject id = " + subobjectId
							+ " was found, it will be modified...");
					aObjMetacontent.setContent(text.getBytes("UTF-8"));
					aObjMetacontent.setLastChangeDate(new Date());
					dao.modifyObjMetacontent(aObjMetacontent);
				}

			}
			/*
			 * indexes biobject by modifying document in index
			 */
			BIObject biObjToIndex = DAOFactory.getBIObjectDAO().loadBIObjectById(biobjectId);
			LuceneIndexer.updateBiobjInIndex(biObjToIndex, false);

		} catch (Exception e) {
			logger.error(request.getPathInfo(), e);
			return Response.ok(new JSONObject("{\"errors\":[{\"message\":\"Exception occurred while saving metadata\"}]}").toString()).build();
		}
		return Response.ok().build();
	}

	private void addMetadata(JSONArray generalMetadata, String name, String value) throws JsonMappingException, JsonParseException, JSONException, IOException {
		addMetadata(generalMetadata, name, value, null);
	}

	private void addMetadata(JSONArray generalMetadata, String name, String value, Integer id) throws JsonMappingException, JsonParseException, JSONException,
			IOException {
		JSONObject data = new JSONObject();
		if (id != null) {
			data.put("id", id);
		}
		data.put("name", name);
		data.put("value", value);
		generalMetadata.put(data);
	}

	private void addTextMetadata(Map<String, JSONArray> metadataMap, String type, String name, String value, Integer id) throws JSONException,
			JsonMappingException, JsonParseException, IOException {
		JSONArray jsonArray = metadataMap.get(type);
		if (jsonArray == null) {
			jsonArray = new JSONArray();
		}
		addMetadata(jsonArray, name, value, id);
		metadataMap.put(type, jsonArray);
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

}
