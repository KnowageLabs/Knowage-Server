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
package it.eng.spagobi.analiticalmodel.execution.service.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.AnalyticalModelDocumentManagementAPI;
import it.eng.spagobi.analiticalmodel.document.DocumentTemplateBuilder;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.execution.service.v2.dto.CustomDataDTO;
import it.eng.spagobi.analiticalmodel.execution.service.v2.dto.DocumentDTO;
import it.eng.spagobi.analiticalmodel.execution.service.v2.dto.FolderDTO;
import it.eng.spagobi.analiticalmodel.execution.service.v2.dto.MetadataDTO;
import it.eng.spagobi.analiticalmodel.execution.service.v2.dto.SaveDocumentDTO;
import it.eng.spagobi.analiticalmodel.execution.service.v2.dto.SourceDatasetDTO;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.JSError;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/2.0/saveDocument")
@ManageAuthorization
public class SaveDocumentResource extends AbstractSpagoBIResource {
	// logger component
	private static Logger logger = Logger.getLogger(SaveDocumentResource.class);

	private static final String MESSAGE_DET = "MESSAGE_DET";
	// type of service
	private static final String DOC_SAVE = "DOC_SAVE";
	private static final String DOC_UPDATE = "DOC_UPDATE";
	private static final String MODIFY_GEOREPORT = "MODIFY_GEOREPORT";
	private static final String MODIFY_COCKPIT = "MODIFY_COCKPIT";
	private static final String MODIFY_KPI = "MODIFY_KPI";

	// RES detail
	private static final String ID = "id";
	private static final String OBJ_ID = "obj_id";
	private static final String NAME = "name";
	private static final String LABEL = "label";
	private static final String DESCRIPTION = "description";
	private static final String ENGINE = "engineid";
	private static final String TYPE = "typeid";
	private static final String TEMPLATE = "template";
	private static final String FUNCTS = "functs";
	private static final String PREVIEW_FILE = "previewFile";
	private static final String BUSINESS_METADATA = "business_metadata";
	private static final String OBJECT_QUERY = "query";
	private static final String FORMVALUES = "formValues";
	private static final String VISIBILITY = "visibility";
	private static final String COMMUNITY = "communityId";

	public static final String OBJ_DATASET_ID = "dataSetId";
	public static final String OBJ_DATASET_LABEL = "dataset_label";

	// default type
	public static final String BIOBJ_TYPE_DOMAIN_CD = "BIOBJ_TYPE";

	// default for parameters
	public static final Integer REQUIRED = 0;
	public static final Integer MODIFIABLE = 1;
	public static final Integer MULTIVALUE = 0;
	public static final Integer VISIBLE = 1;

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveDocument(@Valid SaveDocumentDTO saveDocumentDTO) {
		logger.debug("IN");

		JSError error = new JSError();
		Integer id = null;
		try {
			if (saveDocumentDTO.isUpdateFromWorkspace()) {
				DocumentDTO doc = saveDocumentDTO.getDocumentDTO();
				id = updateDocument(doc);
				return Response.ok(new JSONObject().put("id", id).toString()).build();

			} else {
				String action = saveDocumentDTO.getAction();
				logger.debug("Action type is equal to [" + action + "]");
				if (DOC_SAVE.equalsIgnoreCase(action)) {
					id = doInsertDocument(saveDocumentDTO, error);
				} else if (DOC_UPDATE.equalsIgnoreCase(action)) {
					logger.error("DOC_UPDATE action is no more supported");
					throw new SpagoBIServiceException(saveDocumentDTO.getPathInfo(), "sbi.document.unsupported.udpateaction");
				} else if (MODIFY_GEOREPORT.equalsIgnoreCase(action) || MODIFY_COCKPIT.equalsIgnoreCase(action) || MODIFY_KPI.equalsIgnoreCase(action)) {
					id = doModifyDocument(saveDocumentDTO, action, error);
				} else {
					throw new SpagoBIServiceException(saveDocumentDTO.getPathInfo(), "sbi.document.unsupported.action");
				}
				if (error.hasErrors()) {
					return Response.ok(error.toString()).build();
				} else {
					return Response.ok(new JSONObject().put("id", id).toString()).build();
				}
			}

		} catch (SpagoBIServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new SpagoBIServiceException(saveDocumentDTO.getPathInfo(), "sbi.document.saveError", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private Integer updateDocument(DocumentDTO doc) {

		logger.debug("IN");
		Integer docId = doc.getId();
		Assert.assertNotNull(StringUtilities.isNotEmpty(doc.getName()), "Document's name cannot be null or empty");
		Assert.assertNotNull(StringUtilities.isNotEmpty(doc.getLabel()), "Document's label cannot be null or empty");

		try {

			IBIObjectDAO ibiObjectDAO = DAOFactory.getBIObjectDAO();
			String name = doc.getName();
			String label = doc.getLabel();
			String description = doc.getDescription();

			BIObject biObject = ibiObjectDAO.loadBIObjectById(docId);
			biObject.setName(name);
			biObject.setLabel(label);
			biObject.setDescription(description);

			ibiObjectDAO.modifyBIObject(biObject);

		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("sbi.document.updateError", e);

		} finally {
			logger.debug("OUT");
		}

		return docId;

	}

	private Integer doInsertDocument(SaveDocumentDTO saveDocumentDTO, JSError error) throws JSONException, EMFUserError {
		DocumentDTO documentDTO = saveDocumentDTO.getDocumentDTO();
		Assert.assertNotNull(StringUtilities.isNotEmpty(documentDTO.getName()), "Document's name cannot be null or empty");
		Assert.assertNotNull(StringUtilities.isNotEmpty(documentDTO.getLabel()), "Document's label cannot be null or empty");
		Assert.assertNotNull(StringUtilities.isNotEmpty(documentDTO.getType()), "Document's type cannot be null or empty");
		Integer id = null;
		AnalyticalModelDocumentManagementAPI documentManagementAPI = null;
		try {
			documentManagementAPI = new AnalyticalModelDocumentManagementAPI(getUserProfile());
			if (documentManagementAPI.getDocument(documentDTO.getLabel()) != null) {
				logger.error("sbi.document.labelAlreadyExistent");
				error.addErrorKey("sbi.document.labelAlreadyExistent");
			} else {
				String type = documentDTO.getType();
				if ("MAP".equalsIgnoreCase(type)) {
					insertGeoreportDocument(saveDocumentDTO, documentManagementAPI);
				} else if ("DOCUMENT_COMPOSITE".equalsIgnoreCase(type)) {
					id = insertCockpitDocument(saveDocumentDTO, documentManagementAPI);
				} else if ("KPI".equalsIgnoreCase(type)) {
					id = insertKPIDocument(saveDocumentDTO, documentManagementAPI);
				} else {
					error.addErrorKey("Impossible to create a document of type [" + type + "]");
				}
			}
		} catch (Throwable t) {
			logger.error(t);
			error.addErrorKey("sbi.document.saveError");
		}
		return id;
	}

	private Integer doModifyDocument(SaveDocumentDTO request, String action, JSError error) throws EMFUserError, JSONException {
		DocumentDTO documentDTO = request.getDocumentDTO();
		CustomDataDTO customDataDTO = request.getCustomDataDTO();

		Assert.assertNotNull(customDataDTO, "Custom data object cannot be null");

		// Load existing document
		IBIObjectDAO biObjectDao = DAOFactory.getBIObjectDAO();
		String documentLabel = documentDTO.getLabel();
		BIObject document = biObjectDao.loadBIObjectByLabel(documentLabel);

		List<Integer> filteredFolders = new ArrayList<Integer>();
		if (request.getFolders() == null || request.getFolders().size() == 0) {

			// if no folders are specified in request keep previous ones if present, else put on user home
			logger.debug("no folders specified in request, search for previous ones.");
			ILowFunctionalityDAO functionalitiesDAO = DAOFactory.getLowFunctionalityDAO();
			IEngUserProfile profile = getUserProfile();
			// add personal folder for default

			List<Integer> functionalities = document.getFunctionalities();
			if (functionalities != null && functionalities.size() > 0) {
				logger.debug("Document was already present in " + functionalities.size() + " folders, keep those ones");
				LowFunctionality lowFunc = null;
				for (int i = 0; i < functionalities.size(); i++) {
					Integer id = functionalities.get(i);
					lowFunc = functionalitiesDAO.loadLowFunctionalityByID(functionalities.get(i), false);
					filteredFolders.add(lowFunc.getId());
				}
			} else {
				logger.debug("as default case put document in user home folder");
				LowFunctionality userFunc = null;
				try {
					userFunc = functionalitiesDAO.loadLowFunctionalityByPath("/" + ((UserProfile) profile).getUserId(), false);
				} catch (Exception e) {
					logger.error("Error on insertion of the document.. Impossible to get the id of the personal folder ", e);
					throw new SpagoBIRuntimeException("Error on insertion of the document.. Impossible to get the id of the personal folder ", e);
				}
				filteredFolders.add(userFunc.getId());
			}

		} else {
			filteredFolders = filterFolders(request.getFolders());
		}

		// update document informations
		document = syncronizeDocument(document, filteredFolders, request.getDocumentDTO());

		String tempalteName = (MODIFY_GEOREPORT.equalsIgnoreCase(action)) ? "template.georeport" : "template.sbicockpit";
		String templateContent = customDataDTO.getTemplateContentAsString();

		if (MODIFY_KPI.equalsIgnoreCase(action)) {
			tempalteName = "template.xml";
			Map<String, Object> json = new HashMap<String, Object>();
			json.put("templateContent", templateContent);
			customDataDTO.setTemplateContent(json);
		}
		ObjTemplate template = buildDocumentTemplate(tempalteName, templateContent, document, null);
		AnalyticalModelDocumentManagementAPI documentManagementAPI = null;
		try {
			documentManagementAPI = new AnalyticalModelDocumentManagementAPI(getUserProfile());
			documentManagementAPI.saveDocument(document, template);
			return document.getId();
		} catch (Throwable t) {
			logger.error(t);
			error.addErrorKey("sbi.document.saveError");
		}
		return null;
	}

	private BIObject syncronizeDocument(BIObject obj, List<Integer> folders, DocumentDTO documentDTO) throws JSONException {
		BIObject toReturn = obj;
		String name = documentDTO.getName();
		String description = documentDTO.getDescription();
		String previewFile = documentDTO.getPreviewFile();

		toReturn.setName(name);
		toReturn.setDescription(description);
		if (previewFile != null && !previewFile.equals(""))
			toReturn.setPreviewFile(previewFile.replace("\"", ""));

		// syncronize the folders
		toReturn = setFolders(obj, folders);

		return toReturn;
	}

	private JSError insertGeoreportDocument(SaveDocumentDTO saveDocumentDTO, AnalyticalModelDocumentManagementAPI documentManagementAPI)
			throws JSONException, EMFUserError {
		String sourceModelName = getAttributeAsString("model_name");
		DocumentDTO documentDTO = saveDocumentDTO.getDocumentDTO();
		List<Integer> filteredFolders = new ArrayList<Integer>();
		filteredFolders = getFilteredFoldersList(saveDocumentDTO, filteredFolders);
		CustomDataDTO customDataDTO = saveDocumentDTO.getCustomDataDTO();

		Assert.assertNotNull(customDataDTO, "Custom data object cannot be null");

		if (saveDocumentDTO.getSourceDatasetDTO() != null) {
			SourceDatasetDTO sourceDatasetDTO = saveDocumentDTO.getSourceDatasetDTO();
			insertGeoReportDocumentCreatedOnDataset(sourceDatasetDTO, documentDTO, customDataDTO, filteredFolders, documentManagementAPI);
		} else if (sourceModelName != null) {
			return new JSError().addError("Impossible to create geo document defined on a metamodel");
		} else {
			insertGeoReportDocumentCreatedOnDataset(null, documentDTO, customDataDTO, filteredFolders, documentManagementAPI);
			// throw new SpagoBIServiceException(SERVICE_NAME,
			// "Impossible to create geo document because both sourceModel and sourceDataset are null");
		}
		return new JSError();
	}

	private Integer insertKPIDocument(SaveDocumentDTO saveDocumentDTO, AnalyticalModelDocumentManagementAPI documentManagementAPI)
			throws JSONException, EMFUserError {
		DocumentDTO documentDTO = saveDocumentDTO.getDocumentDTO();
		List<Integer> filteredFolders = new ArrayList<Integer>();
		filteredFolders = getFilteredFoldersList(saveDocumentDTO, filteredFolders);
		CustomDataDTO customDataDTO = saveDocumentDTO.getCustomDataDTO();
		Map<String, Object> json = new HashMap<String, Object>();
		String templateContent = customDataDTO.getTemplateContentAsString();
		json.put("templateContent", templateContent);

		customDataDTO.setTemplateContent(json);

		Assert.assertNotNull(customDataDTO, "Custom data object cannot be null");

		BIObject document = createBaseDocument(documentDTO, filteredFolders, documentManagementAPI);
		ObjTemplate template = buildDocumentTemplate("template.xml", customDataDTO, null);

		documentManagementAPI.saveDocument(document, template);
		return document.getId();
	}

	private List<Integer> getFilteredFoldersList(SaveDocumentDTO request, List<Integer> filteredFolders) throws JSONException {
		if (request.getFolders() == null || request.getFolders().size() == 0) {
			IEngUserProfile profile = getUserProfile();
			// add personal folder for default
			LowFunctionality userFunc = null;
			try {
				userFunc = UserUtilities.loadUserFunctionalityRoot((UserProfile) profile, true);
			} catch (Exception e) {
				logger.error("Error on insertion of the document.. Impossible to get the id of the personal folder ", e);
				throw new SpagoBIRuntimeException("Error on insertion of the document.. Impossible to get the id of the personal folder ", e);
			}
			filteredFolders.add(userFunc.getId());
		} else {
			filteredFolders = filterFolders(request.getFolders());
		}
		return filteredFolders;
	}

	private Integer insertCockpitDocument(SaveDocumentDTO saveDocumentDTO, AnalyticalModelDocumentManagementAPI documentManagementAPI)
			throws EMFUserError, JSONException {

		List<Integer> filteredFolders = new ArrayList<Integer>();
		filteredFolders = getFilteredFoldersList(saveDocumentDTO, filteredFolders);
		CustomDataDTO customData = saveDocumentDTO.getCustomDataDTO();
		Assert.assertNotNull(customData, "Custom data object cannot be null");

		BIObject document = createBaseDocument(saveDocumentDTO.getDocumentDTO(), filteredFolders, documentManagementAPI);
		ObjTemplate template = buildDocumentTemplate("template.sbicockpit", customData, null);

		documentManagementAPI.saveDocument(document, template);
		return document.getId();
	}

	private JSError insertGeoReportDocumentCreatedOnDataset(SourceDatasetDTO sourceDataset, DocumentDTO documentDTO, CustomDataDTO customData,
			List<Integer> folders, AnalyticalModelDocumentManagementAPI documentManagementAPI) throws EMFUserError, JSONException {

		logger.debug("IN");

		String sourceDatasetLabel = null;
		BIObject document = createBaseDocument(documentDTO, folders, documentManagementAPI);
		ObjTemplate template = buildDocumentTemplate("template.sbigeoreport", customData, null);

		IDataSet ISourceDataset = null;
		if (sourceDataset != null) {
			sourceDatasetLabel = sourceDataset.getLabel();
			Assert.assertNotNull(StringUtilities.isNotEmpty(sourceDatasetLabel), "Source dataset's label cannot be null or empty");

			try {
				ISourceDataset = DAOFactory.getDataSetDAO().loadDataSetByLabel(sourceDatasetLabel);
			} catch (Throwable t) {
				return new JSError().addError("Impossible to load source datset [" + sourceDatasetLabel + "]");
			}
			if (ISourceDataset == null) {
				return new JSError().addError("Source datset [" + sourceDatasetLabel + "] does not exist");
			}
			document.setDataSetId(ISourceDataset.getId());
		}

		documentManagementAPI.saveDocument(document, template);
		if (ISourceDataset != null) {
			documentManagementAPI.propagateDatasetParameters(ISourceDataset, document);
		}

		List<MetadataDTO> metadata = documentDTO.getMetadataDTOs();
		if (metadata != null && metadata.size() > 0) {
			documentManagementAPI.saveDocumentMetadataProperties(document, null, metadata);
		}
		return new JSError();
	}

	// TODO consolidate the following 2 methods
	private BIObject createBaseDocument(DocumentDTO documentDTO, List<Integer> folders, AnalyticalModelDocumentManagementAPI documentManagementAPI)
			throws JSONException, EMFUserError {
		BIObject sourceDocument = null;
		String visibility = "true"; // default value
		String previewFile = "";

		if (documentDTO.getVisibility() != null && !documentDTO.getVisibility().equals("")) {
			visibility = documentDTO.getVisibility();// overriding
			// default
			// value
		}
		if (documentDTO.getPreviewFile() != null && !documentDTO.getPreviewFile().equals("")) {
			previewFile = documentDTO.getPreviewFile();// overriding
			// default
			// value
		}
		return createBaseDocument(documentDTO.getLabel(), documentDTO.getName(), documentDTO.getDescription(), visibility, previewFile, documentDTO.getType(),
				documentDTO.getEngineId(), folders);

	}

	private BIObject createBaseDocument(String label, String name, String description, String visibility, String previewFile, String type, String engineId,
			List<Integer> folders) throws EMFUserError, JSONException {

		BIObject document = new BIObject();

		document.setLabel(label);
		document.setName(name);
		document.setDescription(description);
		if (previewFile != null) {
			document.setPreviewFile(previewFile.replace("\"", ""));
		}

		if ("DOCUMENT_COMPOSITE".equalsIgnoreCase(type)) {
			// gets correct type of the engine for DOCUMENT_COMPOSITION (it's
			// cockpit and it uses the EXTERNAL engine)
			Engine engine = null;
			Domain engineType = null;

			List<Engine> engines = DAOFactory.getEngineDAO().loadAllEnginesForBIObjectTypeAndTenant(type);
			if (engines != null && !engines.isEmpty()) {
				if ("DOCUMENT_COMPOSITE".equalsIgnoreCase(type)) {
					for (Engine e : engines) {
						try {
							engineType = DAOFactory.getDomainDAO().loadDomainById(e.getEngineTypeId());
						} catch (EMFUserError ex) {
							throw new SpagoBIServiceException("Impossible to load engine type domain", ex);
						}

						if ("EXT".equalsIgnoreCase(engineType.getValueCd())) {
							engine = e;
						}
					}
				}
			}

			document.setBiObjectTypeID(engine.getBiobjTypeId());
			document.setBiObjectTypeCode(type);
			document.setEngine(engine);
		} else {
			setDocumentEngine(document, type, engineId);
		}
		Boolean isVisible = Boolean.parseBoolean(visibility);
		document.setVisible(isVisible);

//		if (sourceDocument != null) {
//			setDatasource(document, sourceDocument);
//			setDataset(document, sourceDocument);
//		}

		setDocumentState(document);
		setFolders(document, folders);
		setCreationUser(document);

		return document;
	}

	private JSError setDocumentEngine(BIObject document, String type, String engineId) throws EMFUserError {
		Engine engine = null;

		Domain objType = DAOFactory.getDomainDAO().loadDomainByCodeAndValue(SpagoBIConstants.BIOBJ_TYPE, type);
		Integer biObjectTypeID = objType.getValueId();
		document.setBiObjectTypeID(biObjectTypeID);
		document.setBiObjectTypeCode(objType.getValueCd());

		if (StringUtilities.isNotEmpty(engineId)) {
			engine = DAOFactory.getEngineDAO().loadEngineByID(new Integer(engineId));
			if (engine == null) {
				return new JSError().addError("Impossible to load engine with id equals to[" + engineId + "]");
			}
		} else {
			IEngineDAO enginedao = DAOFactory.getEngineDAO();
			enginedao.setUserProfile(getUserProfile());
			List<Engine> engines = enginedao.loadAllEnginesForBIObjectTypeAndTenant(type);
			if (engines != null && !engines.isEmpty()) {
				engine = engines.get(0);
				if ("MAP".equalsIgnoreCase(type)) {
					for (Engine e : engines) {
						if (e.getLabel().equals("SpagoBIGisEngine")) {
							engine = e;
						}
					}
				}
			} else {
				return new JSError().addError("No suitable engine found for document type [" + type + "]");
			}
		}
		document.setEngine(engine);

		return new JSError();
	}

	private BIObject setDatasource(BIObject document, BIObject sourceDocument) {

		document.setDataSourceId(sourceDocument.getDataSourceId());

		return document;
	}

	private BIObject setDataset(BIObject document, BIObject sourceDocument) {

		Integer dataSetId = sourceDocument.getDataSetId();
		document.setDataSetId(dataSetId);

		return document;
	}

	private BIObject setDocumentState(BIObject document) throws EMFUserError {

		Domain objState = DAOFactory.getDomainDAO().loadDomainByCodeAndValue(SpagoBIConstants.DOC_STATE, SpagoBIConstants.DOC_STATE_REL);
		Integer stateID = objState.getValueId();
		document.setStateID(stateID);
		document.setStateCode(objState.getValueCd());

		return document;
	}

	private BIObject setFolders(BIObject document, List<Integer> functsList) throws JSONException {

		List<Integer> folders;

		folders = new ArrayList<Integer>();

		for (Integer id : functsList) {
			if (id.intValue() == -1) {
				// -1 stands for personal folder: check is it exists
				// load personal folder to get its id: in case it does not
				// exist, create it
				LowFunctionality folder = UserUtilities.loadUserFunctionalityRoot(getUserProfile(), true);
				id = folder.getId();
			}
			folders.add(id);
		}

		document.setFunctionalities(folders);

		return document;
	}

	private BIObject setCreationUser(BIObject document) {
		UserProfile userProfile = this.getUserProfile();
		String creationUser = userProfile.getUserId().toString();
		document.setCreationUser(creationUser);
		return document;
	}

	private ObjTemplate buildDocumentTemplate(String templateName, CustomDataDTO customDataDTO, BIObject sourceDocument) throws JSONException {
		String templateContent = customDataDTO.getTemplateContentAsString();

		String modelName = customDataDTO.getModelName();
		return buildDocumentTemplate(templateName, templateContent, sourceDocument, modelName);
	}

	private ObjTemplate buildDocumentTemplate(String templateName, String templateContent, BIObject sourceDocument, String modelName) {

		ObjTemplate template = null;

		DocumentTemplateBuilder documentTemplateBuilder = new DocumentTemplateBuilder();

		UserProfile userProfile = this.getUserProfile();
		String templateAuthor = userProfile.getUserId().toString();

		if (StringUtilities.isNotEmpty(templateContent)) {
			template = documentTemplateBuilder.buildDocumentTemplate(templateName, templateAuthor, templateContent);
		} else {
			throw new SpagoBIServiceException("buildDocumentTemplate", "sbi.document.saveError");
		}

		return template;
	}

	/**
	 * @return a JSON object representing the input request to the service with the following structure: <code>
	 * 		{
	 * 			action: STRING
	 * 			, sourceDataset: {
	 * 				label: STRING
	 * 			}
	 * 			, sourceDocument: {
	 * 				id: NUMBER
	 * 			}
	 * 			, document: {
	 * 				id: NUMBER
	 * 				label: STRING
	 * 				name: STRING
	 * 				description: STRING
	 * 				type: STRING
	 * 				engineId: NUMBER
	 * 				metadata: [JSON, ..., JSON]
	 * 			}
	 * 			, customData: {
	 * 				query: [STRING]
	 * 				workseheet: [JSON]
	 * 				smartfilter:  [JSON]
	 * 			}
	 * 			, folders: [STRING, ... , STRING]
	 * 		}
	 * 	</code>
	 * @throws JSONException
	 * @throws EMFUserError
	 **/

	public JSONObject parseRequest() throws JSONException, EMFUserError {

		JSONObject request = new JSONObject();

		String action = this.getAttributeAsString(MESSAGE_DET);
		request.put("action", action);

		// sourceDatasetLabel
		String sourceDatasetLabel = getAttributeAsString(OBJ_DATASET_LABEL);

		if (StringUtilities.isNotEmpty(sourceDatasetLabel)) {
			JSONObject sourceDataset = new JSONObject();
			sourceDataset.put("label", sourceDatasetLabel);
			request.put("sourceDataset", sourceDataset);
		}

		// sourceDocumentId
		String sourceDocumentId = getAttributeAsString(OBJ_ID);

		if (StringUtilities.isNotEmpty(sourceDocumentId)) {
			JSONObject sourceDocument = new JSONObject();
			sourceDocument.put("id", sourceDocumentId);
			request.put("sourceDocument", sourceDocument);
		}

		// document
		JSONObject document = new JSONObject();

		String documentId = getAttributeAsString(ID);
		if (documentId != null)
			document.put("id", documentId);

		String label = getAttributeAsString(LABEL);
		if (label != null)
			document.put("label", label);

		String name = getAttributeAsString(NAME);
		if (name != null)
			document.put("name", name);

		String description = getAttributeAsString(DESCRIPTION);
		if (description != null)
			document.put("description", description);

		String visibility = getAttributeAsString(VISIBILITY);
		if (visibility != null)
			document.put("visibility", visibility);

		String type = getAttributeAsString(TYPE);
		if (type != null)
			document.put("type", type);

		String engineId = getAttributeAsString(ENGINE);
		if (engineId != null)
			document.put("engineId", engineId);

		// preview file
		String previewFile = getAttributeAsString(PREVIEW_FILE);
		if (previewFile != null)
			document.put("previewFile", previewFile);

		String businessMetadata = getAttributeAsString(BUSINESS_METADATA);
		if (StringUtilities.isNotEmpty(businessMetadata)) {
			JSONObject businessMetadataJSON = new JSONObject(businessMetadata);
			JSONArray metaProperties = new JSONArray();
			JSONArray names = businessMetadataJSON.names();
			for (int i = 0; i < names.length(); i++) {
				String key = names.getString(i);
				String value = businessMetadataJSON.getString(key);
				JSONObject metaProperty = new JSONObject();
				metaProperty.put("meta_name", key);
				metaProperty.put("meta_content", value);
				metaProperties.put(metaProperty);
			}
			document.put("metadata", metaProperties);
		}

		request.put("document", document);

		// customData
		JSONObject customData = new JSONObject();

		if (requestContainsAttribute(FORMVALUES) && StringUtilities.isNotEmpty(getAttributeAsString(FORMVALUES))) {
			JSONObject smartFilterData = getAttributeAsJSONObject(FORMVALUES);
			if (smartFilterData != null)
				customData.put("smartFilter", smartFilterData);
		}

		String query = getAttributeAsString(OBJECT_QUERY);
		if (query != null)
			customData.put("query", query);

		// String templateContent = getAttributeAsString(TEMPLATE);
		JSONObject templateContent = getAttributeAsJSONObject(TEMPLATE);
		if (templateContent != null)
			customData.put("templateContent", templateContent);

		request.put("customData", customData);

		// folders
		JSONArray foldersJSON = new JSONArray();
		if (requestContainsAttribute(FUNCTS) && StringUtilities.isNotEmpty(getAttributeAsString(FUNCTS))) {
			foldersJSON = getAttributeAsJSONArray(FUNCTS);
			if (foldersJSON != null)
				request.put("folders", foldersJSON);
		}
		// COMMUNITY
		String communityFCode = getAttributeAsString(COMMUNITY);
		if (communityFCode != null && !"".equalsIgnoreCase(communityFCode)) {
			if (communityFCode.startsWith("-1")) {
				// clean the community
				String realCode = communityFCode.substring(communityFCode.indexOf("__") + 2);
				if (!realCode.equals("")) {
					for (int i = 0; i < foldersJSON.length(); i++) {
						if (foldersJSON.get(i).equals(Integer.valueOf(realCode))) {
							foldersJSON.remove(i);
							break;
						}
					}
				}
			} else {
				// add community folder to functionalities community folder
				LowFunctionality commF = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByCode(communityFCode, false);
				Integer commFId = commF.getId();
				foldersJSON.put(commFId);
			}
		}

		logger.debug("Request succesfully parsed: " + request.toString(3));

		return request;

	}

	// ==============================================================
	// TODO refactor the following code...
	// ==============================================================

	// protected byte[] getTemplateContent() throws Exception {
	// logger.debug("OUT");
	// String wkDefinition = getAttributeAsString(OBJECT_WK_DEFINITION);
	// String query = getAttributeAsString(OBJECT_QUERY);
	// JSONObject smartFilterValues = getAttributeAsJSONObject(FORMVALUES);
	// String smartFilterValuesString = null;
	// if (smartFilterValues != null) {
	// smartFilterValuesString = smartFilterValues.toString();
	// }
	// logger.debug("Base query definition : " + query);
	// logger.debug("Smart filter values : " + smartFilterValues);
	// ExecutionInstance executionInstance = getContext().getExecutionInstance(ExecutionInstance.class.getName());
	// BIObject biobj = executionInstance.getBIObject();
	// ObjTemplate qbETemplate = biobj.getActiveTemplate();
	// String templCont = new String(qbETemplate.getContent());
	// logger.debug("OUT");
	// return content;
	// }
	//
	// protected ObjTemplate createNewTemplate(byte[] content) {
	// logger.debug("IN");
	// ObjTemplate objTemp = new ObjTemplate();
	// objTemp.setContent(content);
	// UserProfile userProfile = (UserProfile) this.getUserProfile();
	// objTemp.setCreationUser(userProfile.getUserId().toString());
	// objTemp.setDimension(Long.toString(content.length / 1000) + " KByte");
	// logger.debug("OUT");
	// return objTemp;
	// }

//	OLD METHOD TO DELETE ALBNALE
//	private JSONArray filterFolders(JSONArray foldersJSON) throws JSONException {
//		JSONArray toReturn = new JSONArray();
//
//		Set<Integer> folderIds = new HashSet<Integer>();
//		for (int i = 0; i < foldersJSON.length(); i++) {
//			int id = foldersJSON.getInt(i);
//			Integer folderId = new Integer(id);
//			if (!folderIds.contains(folderId)) {
//				toReturn.put(id);
//				folderIds.add(new Integer(folderId));
//			} else {
//				logger.debug("Folder filtered out because duplicate: [" + id + "]");
//			}
//		}
//
//		return toReturn;
//	}

	private List<Integer> filterFolders(List<FolderDTO> folders) throws JSONException {
		List<Integer> toReturn = new ArrayList<Integer>();

		Set<Integer> folderIds = new HashSet<Integer>();
		for (FolderDTO folderDTO : folders) {
			Integer id = Integer.valueOf(folderDTO.getId());
			if (!folderIds.contains(id)) {
				toReturn.add(id);
				folderIds.add(id);
			} else {
				logger.debug("Folder filtered out because duplicate: [" + id + "]");
			}
		}

		return toReturn;
	}

//	private static void insertCockpitRelationsWithDataset(String template, BIObject obj) throws JSONException, EMFUserError {
//
//		// 0. Get the engine. Only engines with 1:N relation with datasets are managed.
//		// 09.05.2016 : for the moment only the cockpit engine is multidatasets.
//		Engine engineObj = obj.getEngine();
//		if (!engineObj.getLabel().toLowerCase().contains("cockpit")) {
//			logger.debug("The engine [" + engineObj.getLabel() + "] cannot use multiple datasets.");
//			return;
//		}
//		String driverName = engineObj.getDriverName();
//		if (driverName != null && !"".equals(driverName)) {
//			try {
//				IEngineDriver driver = (IEngineDriver) Class.forName(driverName).newInstance();
//				ArrayList<String> datasetsAssociated = driver.getDatasetAssociated(template.getBytes());
//				if (datasetsAssociated != null) {
//					HashMap<Integer, Boolean> lstDsInsertedForObj = new HashMap<Integer, Boolean>();
//					for (Iterator<String> iterator = datasetsAssociated.iterator(); iterator.hasNext();) {
//						String dsLabel = iterator.next();
//						logger.debug("Insert relation for dataset with id [" + dsLabel + "]");
//						VersionedDataSet ds = ((VersionedDataSet) DAOFactory.getDataSetDAO().loadDataSetByLabel(dsLabel));
//						// insert only relations with new ds
//						if (lstDsInsertedForObj.get(ds.getId()) != null) {
//							continue;
//						}
//
//						String dsOrganization = ds.getOrganization();
//						logger.debug("Dataset organization used for insert relation is: " + dsOrganization);
//						Integer dsVersion = ds.getVersionNum();
//						logger.debug("Dataset version used for insert relation is: " + dsVersion);
//
//						// creating relation object
//						SbiMetaObjDs relObjDs = new SbiMetaObjDs();
//						SbiMetaObjDsId relObjDsId = new SbiMetaObjDsId();
//						relObjDsId.setDsId(ds.getId());
//						relObjDsId.setOrganization(dsOrganization);
//						relObjDsId.setVersionNum(dsVersion);
//						relObjDsId.setObjId(obj.getId());
//						relObjDs.setId(relObjDsId);
//
//						DAOFactory.getSbiObjDsDAO().insertObjDs(relObjDs);
//						lstDsInsertedForObj.put(ds.getId(), true);
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				throw new SpagoBIRuntimeException("Driver not found: " + driverName, e);
//			}
//
//		} else {
//			logger.debug("The document doesn't use any dataset! ");
//		}
//	}

}
