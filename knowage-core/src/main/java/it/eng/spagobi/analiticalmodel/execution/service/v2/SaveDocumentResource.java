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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.features.Feature;
import it.eng.knowage.rest.annotation.FeatureFlag;
import it.eng.spago.error.EMFInternalError;
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
import it.eng.spagobi.analiticalmodel.execution.service.v2.exception.InvalidHtmlPayloadInCockpitException;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.JSError;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.filters.XSSUtils;

@Path("/2.0/saveDocument")
@ManageAuthorization
public class SaveDocumentResource extends AbstractSpagoBIResource {
	// LOGGER component
	private static final Logger LOGGER = Logger.getLogger(SaveDocumentResource.class);
	// type of service
	private static final String DOC_SAVE = "DOC_SAVE";
	private static final String DOC_UPDATE = "DOC_UPDATE";
	private static final String MODIFY_GEOREPORT = "MODIFY_GEOREPORT";
	private static final String MODIFY_COCKPIT = "MODIFY_COCKPIT";
	private static final String MODIFY_KPI = "MODIFY_KPI";

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@FeatureFlag(Feature.EDIT_DOCUMENT)
	public Response saveDocument(@Valid SaveDocumentDTO saveDocumentDTO) {
		LOGGER.debug("IN");
		LOGGER.debug("saveDocumentDTO: " + saveDocumentDTO);

		JSError error = new JSError();
		Integer id = null;
		try {
			if (saveDocumentDTO.isUpdateFromWorkspace()) {
				DocumentDTO doc = saveDocumentDTO.getDocumentDTO();
				id = updateDocument(doc);
				return Response.ok(new JSONObject().put("id", id).toString()).build();

			} else {
				String action = saveDocumentDTO.getAction();
				LOGGER.debug("Action type is equal to [" + action + "]");
				if (DOC_SAVE.equalsIgnoreCase(action)) {
					LOGGER.debug("Sanitize XSS");
					checkAndSanitizeXSS(saveDocumentDTO);
					LOGGER.debug("Do insert document");
					id = doInsertDocument(saveDocumentDTO, error);
				} else if (DOC_UPDATE.equalsIgnoreCase(action)) {
					LOGGER.error("DOC_UPDATE action is no more supported");
					throw new SpagoBIServiceException(saveDocumentDTO.getPathInfo(),
							"sbi.document.unsupported.udpateaction");
				} else if (MODIFY_COCKPIT.equalsIgnoreCase(action) || MODIFY_KPI.equalsIgnoreCase(action)) {

					LOGGER.debug("Sanitize XSS");
					checkAndSanitizeXSS(saveDocumentDTO);
					LOGGER.debug("Do modify document");
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
			LOGGER.debug("OUT");
		}
	}

	private void checkAndSanitizeXSS(SaveDocumentDTO saveDocumentDTO) {
		XSSUtils xssUtils = new XSSUtils();
		DocumentDTO docDTO = saveDocumentDTO.getDocumentDTO();
		String docType = docDTO.getType();
		if (docType != null && !docType.equals(SpagoBIConstants.DOSSIER_TYPE)) {
			CustomDataDTO customDataDTO = saveDocumentDTO.getCustomDataDTO();
			Map<String, Object> templateContent = customDataDTO.getTemplateContent();
			ArrayList<Map<String, Object>> sheets = (ArrayList<Map<String, Object>>) templateContent.get("sheets");

			try {
				for (Map<String, Object> sheet : sheets) {
					String label = (String) sheet.get("label");
					ArrayList<Map<String, Object>> widgets = (ArrayList<Map<String, Object>>) sheet.get("widgets");

					for (Map<String, Object> widget : widgets) {

						String type = (String) widget.get("type");

						if ("html".equals(type)) {

							String html = (String) widget.get("htmlToRender");

							boolean isSafe = xssUtils.isSafe(html);

							if (!isSafe) {
								throw new InvalidHtmlPayloadInCockpitException(label, html);
							}

						} else if ("customchart".equals(type)) {

							Map<String, Object> html = (Map<String, Object>) widget.get("html");

							String code = (String) html.get("code");

							boolean isSafe = xssUtils.isSafe(code);

							if (!isSafe) {
								throw new InvalidHtmlPayloadInCockpitException(label, code);
							}

						}
					}

				}
				// TODO Change when new template is completed
			} catch (Exception e) {
				LOGGER.info("New template version", e);
			}
		}
	}

	private Integer updateDocument(DocumentDTO doc) {

		LOGGER.debug("IN");
		Integer docId = doc.getId();
		Assert.assertNotNull(StringUtils.isNotEmpty(doc.getName()), "Document's name cannot be null or empty");
		Assert.assertNotNull(StringUtils.isNotEmpty(doc.getLabel()), "Document's label cannot be null or empty");

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
			LOGGER.debug("OUT");
		}

		return docId;

	}

	private Integer doInsertDocument(SaveDocumentDTO saveDocumentDTO, JSError error)
			throws JSONException, EMFUserError {
		LOGGER.debug("IN");
		DocumentDTO documentDTO = saveDocumentDTO.getDocumentDTO();
		Assert.assertNotNull(StringUtils.isNotEmpty(documentDTO.getName()), "Document's name cannot be null or empty");
		Assert.assertNotNull(StringUtils.isNotEmpty(documentDTO.getLabel()),
				"Document's label cannot be null or empty");
		Assert.assertNotNull(StringUtils.isNotEmpty(documentDTO.getType()), "Document's type cannot be null or empty");
		Integer id = null;
		AnalyticalModelDocumentManagementAPI documentManagementAPI = null;
		try {
			documentManagementAPI = new AnalyticalModelDocumentManagementAPI(getUserProfile());
			if (documentManagementAPI.getDocument(documentDTO.getLabel()) != null) {
				LOGGER.error("sbi.document.labelAlreadyExistent");
				error.addErrorKey("sbi.document.labelAlreadyExistent");
			} else {
				String type = documentDTO.getType();
				LOGGER.debug("type: " + type);
				if (SpagoBIConstants.MAP_TYPE_CODE.equalsIgnoreCase(type)) {
					id = insertGeoreportDocument(saveDocumentDTO, documentManagementAPI);
				} else if (SpagoBIConstants.DOCUMENT_COMPOSITE_TYPE.equalsIgnoreCase(type)
						|| SpagoBIConstants.DASHBOARD_TYPE.equalsIgnoreCase(type)) {
					id = insertCockpitDocument(saveDocumentDTO, documentManagementAPI, getUserProfile());
				} else if (SpagoBIConstants.DOSSIER_TYPE.equalsIgnoreCase(type)) {
					id = insertDossierDocument(saveDocumentDTO, documentManagementAPI);
				} else if ("KPI".equalsIgnoreCase(type)) {
					id = insertKPIDocument(saveDocumentDTO, documentManagementAPI);
				} else {
					error.addErrorKey("Impossible to create a document of type [" + type + "]");
				}
			}
		} catch (Throwable t) {
			LOGGER.error("Error inserting document", t);
			LOGGER.debug("Document was: " + String.valueOf(saveDocumentDTO));
			error.addErrorKey("sbi.document.saveError");
		}
		LOGGER.debug("OUT");
		return id;
	}

	private Integer doModifyDocument(SaveDocumentDTO request, String action, JSError error)
			throws EMFUserError, JSONException {
		DocumentDTO documentDTO = request.getDocumentDTO();
		CustomDataDTO customDataDTO = request.getCustomDataDTO();

		Assert.assertNotNull(customDataDTO, "Custom data object cannot be null");

		// Load existing document
		IBIObjectDAO biObjectDao = DAOFactory.getBIObjectDAO();
		String documentLabel = documentDTO.getLabel();
		BIObject document = biObjectDao.loadBIObjectByLabel(documentLabel);

		List<Integer> filteredFolders = new ArrayList<>();
		if (request.getFolders() == null || request.getFolders().isEmpty()) {

			// if no folders are specified in request keep previous ones if present, else put on user home
			LOGGER.debug("no folders specified in request, search for previous ones.");
			ILowFunctionalityDAO functionalitiesDAO = DAOFactory.getLowFunctionalityDAO();
			IEngUserProfile profile = getUserProfile();
			// add personal folder for default

			List<Integer> functionalities = document.getFunctionalities();
			if (functionalities != null && !functionalities.isEmpty()) {
				LOGGER.debug("Document was already present in " + functionalities.size() + " folders, keep those ones");
				LowFunctionality lowFunc = null;
				for (int i = 0; i < functionalities.size(); i++) {
					lowFunc = functionalitiesDAO.loadLowFunctionalityByID(functionalities.get(i), false);
					filteredFolders.add(lowFunc.getId());
				}
			} else {
				LOGGER.debug("as default case put document in user home folder");
				LowFunctionality userFunc = null;
				try {
					userFunc = functionalitiesDAO.loadLowFunctionalityByPath("/" + ((UserProfile) profile).getUserId(),
							false);
				} catch (Exception e) {
					LOGGER.error(
							"Error on insertion of the document.. Impossible to get the id of the personal folder ", e);
					throw new SpagoBIRuntimeException(
							"Error on insertion of the document.. Impossible to get the id of the personal folder ", e);
				}
				filteredFolders.add(userFunc.getId());
			}

		} else {
			filteredFolders = filterFolders(request.getFolders());
		}

		// update document informations
		document = syncronizeDocument(document, filteredFolders, request.getDocumentDTO());

		String tempalteName = (MODIFY_GEOREPORT.equalsIgnoreCase(action)) ? "template.georeport"
				: "template.sbicockpit";
		String templateContent = customDataDTO.getTemplateContentAsString();

		if (MODIFY_KPI.equalsIgnoreCase(action)) {
			tempalteName = "template.xml";
			Map<String, Object> json = new HashMap<>();
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
			LOGGER.error("Error updating document", t);
			LOGGER.debug("Document was: " + String.valueOf(request));
			error.addErrorKey("sbi.document.saveError");
		}
		return null;
	}

	private BIObject syncronizeDocument(BIObject obj, List<Integer> folders, DocumentDTO documentDTO) {
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

	private Integer insertGeoreportDocument(SaveDocumentDTO saveDocumentDTO,
			AnalyticalModelDocumentManagementAPI documentManagementAPI) throws JSONException, EMFUserError {
		String sourceModelName = getAttributeAsString("model_name");
		DocumentDTO documentDTO = saveDocumentDTO.getDocumentDTO();
		List<Integer> filteredFolders = new ArrayList<>();
		filteredFolders = getFilteredFoldersList(saveDocumentDTO, filteredFolders);
		CustomDataDTO customDataDTO = saveDocumentDTO.getCustomDataDTO();

		Assert.assertNotNull(customDataDTO, "Custom data object cannot be null");

		Integer id = null;

		if (saveDocumentDTO.getSourceDatasetDTO() != null) {
			SourceDatasetDTO sourceDatasetDTO = saveDocumentDTO.getSourceDatasetDTO();
			id = insertGeoReportDocumentCreatedOnDataset(sourceDatasetDTO, documentDTO, customDataDTO, filteredFolders,
					documentManagementAPI);
		} else if (sourceModelName != null) {
			throw new SpagoBIRuntimeException("Impossible to create geo document defined on a metamodel");
		} else {
			id = insertGeoReportDocumentCreatedOnDataset(null, documentDTO, customDataDTO, filteredFolders,
					documentManagementAPI);
			// throw new SpagoBIServiceException(SERVICE_NAME,
			// "Impossible to create geo document because both sourceModel and sourceDataset are null");
		}
		return id;
	}

	private Integer insertKPIDocument(SaveDocumentDTO saveDocumentDTO,
			AnalyticalModelDocumentManagementAPI documentManagementAPI) throws JSONException, EMFUserError {
		DocumentDTO documentDTO = saveDocumentDTO.getDocumentDTO();
		List<Integer> filteredFolders = new ArrayList<>();
		filteredFolders = getFilteredFoldersList(saveDocumentDTO, filteredFolders);
		CustomDataDTO customDataDTO = saveDocumentDTO.getCustomDataDTO();
		Map<String, Object> json = new HashMap<>();
		String templateContent = customDataDTO.getTemplateContentAsString();
		json.put("templateContent", templateContent);

		customDataDTO.setTemplateContent(json);

		Assert.assertNotNull(customDataDTO, "Custom data object cannot be null");

		BIObject document = createBaseDocument(documentDTO, filteredFolders, documentManagementAPI, null);
		ObjTemplate template = buildDocumentTemplate("template.xml", customDataDTO, null);

		documentManagementAPI.saveDocument(document, template);
		return document.getId();
	}

	private Integer insertDossierDocument(SaveDocumentDTO saveDocumentDTO,
			AnalyticalModelDocumentManagementAPI documentManagementAPI) throws JSONException, EMFUserError {
		DocumentDTO documentDTO = saveDocumentDTO.getDocumentDTO();
		List<Integer> filteredFolders = new ArrayList<>();
		filteredFolders = getFilteredFoldersList(saveDocumentDTO, filteredFolders);
		CustomDataDTO customDataDTO = saveDocumentDTO.getCustomDataDTO();
		Map<String, Object> json = new HashMap<>();
		String templateContent = customDataDTO.getTemplateContentAsString();
		json.put("templateContent", templateContent);

		customDataDTO.setTemplateContent(json);

		Assert.assertNotNull(customDataDTO, "Custom data object cannot be null");

		BIObject document = createBaseDocument(documentDTO, filteredFolders, documentManagementAPI, null);
		ObjTemplate template = buildDocumentTemplate("template.xml", customDataDTO, null);

		documentManagementAPI.saveDocument(document, template);
		return document.getId();
	}

	private List<Integer> getFilteredFoldersList(SaveDocumentDTO request, List<Integer> filteredFolders)
			throws JSONException {
		if (request.getFolders() == null || request.getFolders().isEmpty()) {
			IEngUserProfile profile = getUserProfile();
			// add personal folder for default
			LowFunctionality userFunc = null;
			try {
				userFunc = UserUtilities.loadUserFunctionalityRoot((UserProfile) profile, true);
			} catch (Exception e) {
				LOGGER.error("Error on insertion of the document.. Impossible to get the id of the personal folder ",
						e);
				throw new SpagoBIRuntimeException(
						"Error on insertion of the document.. Impossible to get the id of the personal folder ", e);
			}
			filteredFolders.add(userFunc.getId());
		} else {
			filteredFolders = filterFolders(request.getFolders());
		}
		return filteredFolders;
	}

	private Integer insertCockpitDocument(SaveDocumentDTO saveDocumentDTO,
			AnalyticalModelDocumentManagementAPI documentManagementAPI, UserProfile profile)
			throws EMFUserError, JSONException {
		LOGGER.debug("IN");
		List<Integer> filteredFolders = new ArrayList<>();
		filteredFolders = getFilteredFoldersList(saveDocumentDTO, filteredFolders);
		CustomDataDTO customData = saveDocumentDTO.getCustomDataDTO();
		Assert.assertNotNull(customData, "Custom data object cannot be null");

		BIObject document = createBaseDocument(saveDocumentDTO.getDocumentDTO(), filteredFolders, documentManagementAPI,
				profile);
		ObjTemplate template = buildDocumentTemplate("template.sbicockpit", customData, null);
		LOGGER.debug("Template created");
		LOGGER.debug(template);

		documentManagementAPI.saveDocument(document, template);
		LOGGER.debug("OUT");
		return document.getId();
	}

	private Integer insertGeoReportDocumentCreatedOnDataset(SourceDatasetDTO sourceDataset, DocumentDTO documentDTO,
			CustomDataDTO customData, List<Integer> folders, AnalyticalModelDocumentManagementAPI documentManagementAPI)
			throws EMFUserError, JSONException {

		LOGGER.debug("IN");

		String sourceDatasetLabel = null;
		BIObject document = createBaseDocument(documentDTO, folders, documentManagementAPI, null);
		ObjTemplate template = buildDocumentTemplate("template.sbigeoreport", customData, null);

		IDataSet ISourceDataset = null;
		if (sourceDataset != null) {
			sourceDatasetLabel = sourceDataset.getLabel();
			Assert.assertNotNull(StringUtils.isNotEmpty(sourceDatasetLabel),
					"Source dataset's label cannot be null or empty");

			try {
				ISourceDataset = DAOFactory.getDataSetDAO().loadDataSetByLabel(sourceDatasetLabel);
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Impossible to load source datset [" + sourceDatasetLabel + "]");
			}
			if (ISourceDataset == null) {
				throw new SpagoBIRuntimeException("Source datset [" + sourceDatasetLabel + "] does not exist");
			}
			document.setDataSetId(ISourceDataset.getId());
		}

		documentManagementAPI.saveDocument(document, template);
		if (ISourceDataset != null) {
			documentManagementAPI.propagateDatasetParameters(ISourceDataset, document);
		}

		List<MetadataDTO> metadata = documentDTO.getMetadataDTOs();
		if (metadata != null && !metadata.isEmpty()) {
			documentManagementAPI.saveDocumentMetadataProperties(document, null, metadata);
		}
		return document.getId();
	}

	// TODO consolidate the following 2 methods
	private BIObject createBaseDocument(DocumentDTO documentDTO, List<Integer> folders,
			AnalyticalModelDocumentManagementAPI documentManagementAPI, UserProfile profile) throws EMFUserError {
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
		return createBaseDocument(documentDTO.getLabel(), documentDTO.getName(), documentDTO.getDescription(),
				visibility, previewFile, documentDTO.getType(), documentDTO.getEngineId(), folders, profile);

	}

	private BIObject createBaseDocument(String label, String name, String description, String visibility,
			String previewFile, String type, String engineId, List<Integer> folders, UserProfile profile)
			throws EMFUserError {

		BIObject document = new BIObject();

		document.setLabel(label);
		document.setName(name);
		document.setDescription(description);
		if (previewFile != null) {
			document.setPreviewFile(previewFile.replace("\"", ""));
		}

		if (SpagoBIConstants.DOCUMENT_COMPOSITE_TYPE.equalsIgnoreCase(type)
				|| SpagoBIConstants.DASHBOARD_TYPE.equalsIgnoreCase(type)) {
			// gets correct type of the engine for DOCUMENT_COMPOSITION (it's
			// cockpit and it uses the EXTERNAL engine)
			Engine engine = null;
			Domain engineType = null;

			List<Engine> engines = DAOFactory.getEngineDAO().loadAllEnginesForBIObjectTypeAndTenant(type);
			if (engines != null && !engines.isEmpty()) {
				if (SpagoBIConstants.DOCUMENT_COMPOSITE_TYPE.equalsIgnoreCase(type)
						|| SpagoBIConstants.DASHBOARD_TYPE.equalsIgnoreCase(type)) {
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

		setDocumentState(document, profile);
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

		if (StringUtils.isNotEmpty(engineId)) {
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

		Domain objState = DAOFactory.getDomainDAO().loadDomainByCodeAndValue(SpagoBIConstants.DOC_STATE,
				SpagoBIConstants.DOC_STATE_REL);
		Integer stateID = objState.getValueId();
		document.setStateID(stateID);
		document.setStateCode(objState.getValueCd());

		return document;
	}

	private BIObject setDocumentState(BIObject document, UserProfile profile) throws EMFUserError {
		boolean isOnlyDevRole = false;
		try {
			if (profile != null)
				isOnlyDevRole = isOnlyDevRole(profile);
		} catch (EMFInternalError e) {
			throw new SpagoBIServiceException("setDocumentState", "sbi.document.saveError");
		}
		Domain objState = null;
		if (isOnlyDevRole) {
			objState = DAOFactory.getDomainDAO().loadDomainByCodeAndValue(SpagoBIConstants.DOC_STATE,
					SpagoBIConstants.DOC_STATE_DEV);
		} else
			objState = DAOFactory.getDomainDAO().loadDomainByCodeAndValue(SpagoBIConstants.DOC_STATE,
					SpagoBIConstants.DOC_STATE_REL);
		Integer stateID = objState.getValueId();
		document.setStateID(stateID);
		document.setStateCode(objState.getValueCd());

		return document;
	}

	private boolean isOnlyDevRole(UserProfile profile) throws EMFInternalError, EMFUserError {
		boolean onlyDev = true;
		for (Object roleLabel : profile.getRoles()) {
			Role role = DAOFactory.getRoleDAO().loadByName(roleLabel.toString());
			if (!role.getRoleTypeCD().equals(SpagoBIConstants.ROLE_TYPE_DEV)) {
				onlyDev = false;
			}

		}
		return onlyDev;
	}

	private BIObject setFolders(BIObject document, List<Integer> functsList) {

		List<Integer> folders;

		folders = new ArrayList<>();

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

	private ObjTemplate buildDocumentTemplate(String templateName, CustomDataDTO customDataDTO, BIObject sourceDocument)
			throws JSONException {
		String templateContent = customDataDTO.getTemplateContentAsString();
		LOGGER.debug("templateContent: " + templateContent);

		String modelName = customDataDTO.getModelName();
		LOGGER.debug("modelName: " + modelName);

		return buildDocumentTemplate(templateName, templateContent, sourceDocument, modelName);
	}

	private ObjTemplate buildDocumentTemplate(String templateName, String templateContent, BIObject sourceDocument,
			String modelName) {

		ObjTemplate template = null;

		DocumentTemplateBuilder documentTemplateBuilder = new DocumentTemplateBuilder();

		UserProfile userProfile = this.getUserProfile();
		String templateAuthor = userProfile.getUserId().toString();

		if (StringUtils.isNotEmpty(templateContent)) {
			template = documentTemplateBuilder.buildDocumentTemplate(templateName, templateAuthor, templateContent);
		} else {
			throw new SpagoBIServiceException("buildDocumentTemplate", "sbi.document.saveError");
		}

		return template;
	}

	private List<Integer> filterFolders(List<FolderDTO> folders) {
		List<Integer> toReturn = new ArrayList<>();

		Set<Integer> folderIds = new HashSet<>();
		for (FolderDTO folderDTO : folders) {
			Integer id = Integer.valueOf(folderDTO.getId());
			if (!folderIds.contains(id)) {
				toReturn.add(id);
				folderIds.add(id);
			} else {
				LOGGER.debug("Folder filtered out because duplicate: [" + id + "]");
			}
		}

		return toReturn;
	}

}
