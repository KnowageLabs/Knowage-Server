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
package it.eng.spagobi.sdk.documents.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.service.DefaultRequestContext;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesRetriever;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.engines.InternalEngineIFace;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.exporters.ReportExporter;
import it.eng.spagobi.sdk.AbstractSDKService;
import it.eng.spagobi.sdk.documents.DocumentsService;
import it.eng.spagobi.sdk.documents.bo.SDKDocument;
import it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter;
import it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue;
import it.eng.spagobi.sdk.documents.bo.SDKExecutedDocumentContent;
import it.eng.spagobi.sdk.documents.bo.SDKFunctionality;
import it.eng.spagobi.sdk.documents.bo.SDKSchema;
import it.eng.spagobi.sdk.documents.bo.SDKTemplate;
import it.eng.spagobi.sdk.exceptions.InvalidParameterValue;
import it.eng.spagobi.sdk.exceptions.MissingParameterValue;
import it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.exceptions.SDKException;
import it.eng.spagobi.sdk.utilities.SDKObjectsConverter;
import it.eng.spagobi.sdk.utilities.SDKObjectsConverter.MemoryOnlyDataSource;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.file.FileUtils;
import it.eng.spagobi.utilities.mime.MimeUtils;

public class DocumentsServiceImpl extends AbstractSDKService implements DocumentsService {

	public static final String DATAMART_FILE_NAME = "datamart.jar";
	public static final String CFIELDS_FILE_NAME = "cfields_meta.xml";
	public static final String MONDRIAN_SCHEMA_TYPE = "MONDRIAN_SCHEMA";

	static private Logger logger = Logger.getLogger(DocumentsServiceImpl.class);

	@Override
	public SDKDocumentParameterValue[] getAdmissibleValues(Integer documentParameterId, String roleName) throws NonExecutableDocumentException {
		SDKDocumentParameterValue[] values = new SDKDocumentParameterValue[] {};
		logger.debug("IN: documentParameterId = [" + documentParameterId + "]; roleName = [" + roleName + "]");

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();
			BIObjectParameter documentParameter = DAOFactory.getBIObjectParameterDAO().loadForDetailByObjParId(documentParameterId);
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(documentParameter.getBiObjectID());
			if (!ObjectsAccessVerifier.canSee(obj, profile)) {
				logger.error("User [" + ((UserProfile) profile).getUserName() + "] cannot execute document with id = [" + obj.getId() + "]");
				throw new NonExecutableDocumentException();
			}
			List correctRoles = ObjectsAccessVerifier.getCorrectRolesForExecution(obj.getId(), profile);
			if (correctRoles == null || correctRoles.size() == 0) {
				logger.error("User [" + ((UserProfile) profile).getUserName() + "] has no roles to execute document with id = [" + obj.getId() + "]");
				throw new NonExecutableDocumentException();
			}
			if (!correctRoles.contains(roleName)) {
				logger.error("Role [" + roleName + "] is not a valid role for executing document with id = [" + obj.getId() + "] for user ["
						+ ((UserProfile) profile).getUserName() + "]");
				throw new NonExecutableDocumentException();
			}

			// reload BIObjectParameter in execution modality
			BIObjectParameter biParameter = null;
			obj = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByIdAndRole(obj.getId(), roleName);
			List biparameters = obj.getDrivers();
			Iterator biparametersIt = biparameters.iterator();
			while (biparametersIt.hasNext()) {
				BIObjectParameter aDocParameter = (BIObjectParameter) biparametersIt.next();
				if (aDocParameter.getId().equals(documentParameterId)) {
					biParameter = aDocParameter;
					break;
				}
			}

			Parameter par = biParameter.getParameter();
			ModalitiesValue paruse = par.getModalityValue();
			if (paruse.getITypeCd().equals("MAN_IN")) {
				logger.debug("Document parameter is manual input. An empty HashMap will be returned.");
			} else {
				String lovprov = paruse.getLovProvider();
				ILovDetail lovDetail = LovDetailFactory.getLovFromXML(lovprov);
				String lovResult = lovDetail.getLovResult(profile, null, null, null);
				LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
				List rows = lovResultHandler.getRows();
				values = new SDKDocumentParameterValue[rows.size()];
				for (int i = 0; i < rows.size(); i++) {
					SourceBean row = (SourceBean) rows.get(i);
					String value = (String) row.getAttribute(lovDetail.getValueColumnName());
					String description = (String) row.getAttribute(lovDetail.getDescriptionColumnName());
					values[i] = new SDKDocumentParameterValue(value, description);
				}
			}
		} catch (NonExecutableDocumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return values;
	}

	@Override
	public SDKDocumentParameterValue[] getDefaultValues(Integer documentParameterId, String roleName) throws NonExecutableDocumentException {
		SDKDocumentParameterValue[] values = new SDKDocumentParameterValue[] {};
		logger.debug("IN: documentParameterId = [" + documentParameterId + "]; roleName = [" + roleName + "]");

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();
			BIObjectParameter documentParameter = DAOFactory.getBIObjectParameterDAO().loadForDetailByObjParId(documentParameterId);
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(documentParameter.getBiObjectID());
			if (!ObjectsAccessVerifier.canSee(obj, profile)) {
				logger.error("User [" + ((UserProfile) profile).getUserName() + "] cannot execute document with id = [" + obj.getId() + "]");
				throw new NonExecutableDocumentException();
			}
			List correctRoles = ObjectsAccessVerifier.getCorrectRolesForExecution(obj.getId(), profile);
			if (correctRoles == null || correctRoles.size() == 0) {
				logger.error("User [" + ((UserProfile) profile).getUserName() + "] has no roles to execute document with id = [" + obj.getId() + "]");
				throw new NonExecutableDocumentException();
			}
			if (!correctRoles.contains(roleName)) {
				logger.error("Role [" + roleName + "] is not a valid role for executing document with id = [" + obj.getId() + "] for user ["
						+ ((UserProfile) profile).getUserName() + "]");
				throw new NonExecutableDocumentException();
			}

			ExecutionInstance executionInstance = new ExecutionInstance(profile, "", "", obj.getId(), roleName, null, null);
			logger.debug("Execution instance created");

			// reload BIObjectParameter in execution modality
			BIObjectParameter biParameter = null;
			obj = executionInstance.getBIObject();
			List biparameters = obj.getDrivers();
			Iterator biparametersIt = biparameters.iterator();
			while (biparametersIt.hasNext()) {
				BIObjectParameter aDocParameter = (BIObjectParameter) biparametersIt.next();
				if (aDocParameter.getId().equals(documentParameterId)) {
					biParameter = aDocParameter;
					break;
				}
			}

			DefaultValuesRetriever retriever = new DefaultValuesRetriever();
			logger.debug("Retrieving default values ...");
			DefaultValuesList defaultValues = retriever.getDefaultValues(biParameter, executionInstance, profile);
			logger.debug("Default values retrieved");

			values = new SDKDocumentParameterValue[defaultValues.size()];
			for (int i = 0; i < defaultValues.size(); i++) {
				LovValue defaultValue = defaultValues.get(i);
				String value = defaultValue.getValue() != null ? defaultValue.getValue().toString() : null;
				String description = defaultValue.getDescription() != null ? defaultValue.getDescription().toString() : "";
				logger.debug("Default value retrieved : value = [" + value + "], description = [" + description + "]");
				values[i] = new SDKDocumentParameterValue(value, description);
			}

		} catch (NonExecutableDocumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return values;
	}

	@Override
	public String[] getCorrectRolesForExecution(Integer documentId) throws NonExecutableDocumentException {
		String[] toReturn = null;
		logger.debug("IN: documentId = [" + documentId + "]");

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(documentId);
			if (!ObjectsAccessVerifier.canSee(obj, profile)) {
				logger.error("User [" + ((UserProfile) profile).getUserName() + "] cannot execute document with id = [" + documentId + "]");
				throw new NonExecutableDocumentException();
			}
			List correctRoles = ObjectsAccessVerifier.getCorrectRolesForExecution(documentId, profile);
			if (correctRoles != null) {
				toReturn = new String[correctRoles.size()];
				toReturn = (String[]) correctRoles.toArray(toReturn);
			} else {
				toReturn = new String[0];
			}
		} catch (NonExecutableDocumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public SDKDocumentParameter[] getDocumentParameters(Integer documentId, String roleName) throws NonExecutableDocumentException {
		SDKDocumentParameter parameters[] = null;
		logger.debug("IN: documentId = [" + documentId + "]; roleName = [" + roleName + "]");

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(documentId);
			if (!ObjectsAccessVerifier.canSee(obj, profile)) {
				logger.error("User [" + ((UserProfile) profile).getUserName() + "] cannot execute document with id = [" + documentId + "]");
				throw new NonExecutableDocumentException();
			}
			List correctRoles = ObjectsAccessVerifier.getCorrectRolesForExecution(documentId, profile);
			if (correctRoles == null || correctRoles.size() == 0) {
				logger.error("User [" + ((UserProfile) profile).getUserName() + "] has no roles to execute document with id = [" + documentId + "]");
				throw new NonExecutableDocumentException();
			}
			if (!correctRoles.contains(roleName)) {
				logger.error("Role [" + roleName + "] is not a valid role for executing document with id = [" + documentId + "] for user ["
						+ ((UserProfile) profile).getUserName() + "]");
				throw new NonExecutableDocumentException();
			}

			obj = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByIdAndRole(obj.getId(), roleName);
			List parametersList = obj.getDrivers();
			List toReturn = new ArrayList();
			if (parametersList != null) {
				SDKDocumentParameter aDocParameter;
				Iterator it = parametersList.iterator();
				while (it.hasNext()) {
					BIObjectParameter parameter = (BIObjectParameter) it.next();
					aDocParameter = new SDKObjectsConverter().fromBIObjectParameterToSDKDocumentParameter(parameter);
					toReturn.add(aDocParameter);
				}
			}
			parameters = new SDKDocumentParameter[toReturn.size()];
			parameters = (SDKDocumentParameter[]) toReturn.toArray(parameters);
		} catch (NonExecutableDocumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return parameters;
	}

	@Override
	public SDKDocument[] getDocumentsAsList(String type, String state, String folderPath) {
		SDKDocument documents[] = null;
		logger.debug("IN");

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();
			List list = DAOFactory.getBIObjectDAO().loadBIObjects(type, state, folderPath);
			List toReturn = new ArrayList();
			if (list != null) {
				for (Iterator it = list.iterator(); it.hasNext();) {
					BIObject obj = (BIObject) it.next();
					if (ObjectsAccessVerifier.canSee(obj, profile)) {
						SDKDocument aDoc = new SDKObjectsConverter().fromBIObjectToSDKDocument(obj);
						if (!profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)
								&& !profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV) && obj.getVisible().equals(0)) {
							logger.debug("Cannot view " + obj.getLabel() + " because user is not admin or dev and document is not visible");
						} else {
							toReturn.add(aDoc);
						}

					}
				}
			}
			documents = new SDKDocument[toReturn.size()];
			documents = (SDKDocument[]) toReturn.toArray(documents);
		} catch (Exception e) {
			logger.error("Error while loading documents as list", e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return documents;
	}

	@Override
	public SDKFunctionality getDocumentsAsTree(String initialPath) {
		logger.debug("IN: initialPath = [" + initialPath + "]");
		SDKFunctionality toReturn = null;

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();
			ILowFunctionalityDAO functionalityDAO = DAOFactory.getLowFunctionalityDAO();
			LowFunctionality initialFunctionality = null;
			if (initialPath == null || initialPath.trim().equals("")) {
				// loading root functionality, everybody can see it
				initialFunctionality = functionalityDAO.loadRootLowFunctionality(false);
			} else {
				initialFunctionality = functionalityDAO.loadLowFunctionalityByPath(initialPath, false);
			}
			boolean canSeeFunctionality = ObjectsAccessVerifier.canSee(initialFunctionality, profile);
			if (canSeeFunctionality) {
				toReturn = new SDKObjectsConverter().fromLowFunctionalityToSDKFunctionality(initialFunctionality);
				setFunctionalityContent(toReturn);
			}
		} catch (Exception e) {
			logger.error("Error while loading documents as tree", e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	private void setFunctionalityContent(SDKFunctionality parentFunctionality) throws Exception {
		logger.debug("IN");
		IEngUserProfile profile = getUserProfile();
		// loading contained documents
		List containedBIObjects = DAOFactory.getBIObjectDAO().loadBIObjects(parentFunctionality.getId(), profile, false);
		List visibleDocumentsList = new ArrayList();
		if (containedBIObjects != null && containedBIObjects.size() > 0) {
			for (Iterator it = containedBIObjects.iterator(); it.hasNext();) {
				BIObject obj = (BIObject) it.next();
				if (ObjectsAccessVerifier.checkProfileVisibility(obj, profile)) {
					SDKDocument aDoc = new SDKObjectsConverter().fromBIObjectToSDKDocument(obj);
					visibleDocumentsList.add(aDoc);
				}
			}
		}
		SDKDocument[] containedDocuments = new SDKDocument[visibleDocumentsList.size()];
		containedDocuments = (SDKDocument[]) visibleDocumentsList.toArray(containedDocuments);
		parentFunctionality.setContainedDocuments(containedDocuments);

		// loading contained functionalities
		List containedFunctionalitiesList = DAOFactory.getLowFunctionalityDAO().loadChildFunctionalities(parentFunctionality.getId(), false);
		List visibleFunctionalitiesList = new ArrayList();
		for (Iterator it = containedFunctionalitiesList.iterator(); it.hasNext();) {
			LowFunctionality lowFunctionality = (LowFunctionality) it.next();
			boolean canSeeFunctionality = ObjectsAccessVerifier.canSee(lowFunctionality, profile);
			if (canSeeFunctionality) {
				SDKFunctionality childFunctionality = new SDKObjectsConverter().fromLowFunctionalityToSDKFunctionality(lowFunctionality);
				visibleFunctionalitiesList.add(childFunctionality);
				// recursion
				setFunctionalityContent(childFunctionality);
			}
		}
		SDKFunctionality[] containedFunctionalities = new SDKFunctionality[visibleFunctionalitiesList.size()];
		containedFunctionalities = (SDKFunctionality[]) visibleFunctionalitiesList.toArray(containedFunctionalities);
		parentFunctionality.setContainedFunctionalities(containedFunctionalities);
		logger.debug("OUT");
	}

	@Override
	public Integer saveNewDocument(SDKDocument document, SDKTemplate sdkTemplate, Integer functionalityId) throws NotAllowedOperationException {
		logger.debug("IN");
		Integer toReturn = null;

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();

			BIObject obj = new SDKObjectsConverter().fromSDKDocumentToBIObject(document);
			String userId = ((UserProfile) profile).getUserId().toString();
			logger.debug("Current user id is [" + userId + "]");
			obj.setCreationUser(((UserProfile) profile).getUserId().toString());
			obj.setCreationDate(new Date());
			obj.setVisible(new Integer(1));
			List functionalities = new ArrayList();
			if (functionalityId != null)
				functionalities.add(functionalityId);
			obj.setFunctionalities(functionalities);

			ObjTemplate objTemplate = null;
			if (sdkTemplate != null && sdkTemplate.getContent() != null) {
				objTemplate = new SDKObjectsConverter().fromSDKTemplateToObjTemplate(sdkTemplate);
				if (objTemplate != null) {
					objTemplate.setActive(new Boolean(true));
					objTemplate.setCreationUser(userId);
					objTemplate.setCreationDate(new Date());
				}
			}

			logger.debug("Check if document with label " + obj.getLabel() + " i already existing.");
			BIObject existingObject = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(obj.getLabel());
			if (existingObject != null) {
				logger.debug("Found existing object: go on for update");
				obj.setId(existingObject.getId());

				// if Object has already functionalities associated and current
				// one is not specified then keep previous ones,
				if (functionalityId == null && existingObject.getFunctionalities() != null && !existingObject.getFunctionalities().isEmpty()) {
					logger.debug("Keep previous functionalities");
					obj.setFunctionalities(existingObject.getFunctionalities());
				} else {
					if (functionalityId != null)
						logger.debug("Insert into functionality with id " + functionalityId);
				}

				if (sdkTemplate != null && objTemplate != null) {
					DAOFactory.getBIObjectDAO().modifyBIObject(obj, objTemplate);
				} else {
					// pass functionalities that must not be changed
					obj.setFunctionalities(existingObject.getFunctionalities());
					DAOFactory.getBIObjectDAO().modifyBIObject(obj);
				}
				toReturn = existingObject.getId();
			} else {
				// check permission on saving new document

				// if user cannot develop in the specified folder, he cannot
				// save documents inside it
				if (!ObjectsAccessVerifier.canDev(functionalityId, profile)) {
					NotAllowedOperationException e = new NotAllowedOperationException();
					e.setFaultString("User cannot save new documents in the specified folder since he hasn't development permission.");
					throw e;
				}
				logger.debug("Not found existing document, saving new one.");
				IBIObjectDAO biObjDAO = DAOFactory.getBIObjectDAO();
				biObjDAO.setUserProfile(profile);
				biObjDAO.insertBIObject(obj, objTemplate);
				toReturn = obj.getId();
				if (toReturn != null) {
					logger.info("Document saved with id = " + toReturn);
				} else {
					logger.error("Document not saved!!");
				}
			}
		} catch (Exception e) {
			logger.error("Error while saving new document", e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public void uploadTemplate(Integer documentId, SDKTemplate sdkTemplate) throws NotAllowedOperationException {
		logger.debug("IN: documentId = [" + documentId + "]; template file name = [" + sdkTemplate.getFileName() + "]");

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();
			// if user cannot develop the specified document, he cannot upload
			// templates on it
			if (!ObjectsAccessVerifier.canDevBIObject(documentId, profile)) {
				NotAllowedOperationException e = new NotAllowedOperationException();
				e.setFaultString("User cannot upload templates on specified document since he cannot develop it.");
				throw e;
			}
			ObjTemplate objTemplate = new SDKObjectsConverter().fromSDKTemplateToObjTemplate(sdkTemplate);
			objTemplate.setBiobjId(documentId);
			objTemplate.setActive(new Boolean(true));
			String userId = ((UserProfile) profile).getUserId().toString();
			logger.debug("Current user id is [" + userId + "]");
			objTemplate.setCreationUser(userId);
			objTemplate.setCreationDate(new Date());
			logger.debug("Saving template....");
			BIObject biObj = DAOFactory.getBIObjectDAO().loadBIObjectById(documentId);
			IObjTemplateDAO tempDAO = DAOFactory.getObjTemplateDAO();
			tempDAO.setUserProfile(profile);
			tempDAO.insertBIObjectTemplate(objTemplate, biObj);
			logger.debug("Template stored without errors.");
		} catch (Exception e) {
			logger.error("Error while uploading template", e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
	}

	@Override
	public SDKTemplate downloadTemplate(Integer documentId) throws NotAllowedOperationException {
		logger.debug("IN");
		SDKTemplate toReturn = null;

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();
			// if user cannot develop the specified document, he cannot upload
			// templates on it
			if (!ObjectsAccessVerifier.canDevBIObject(documentId, profile)) {
				NotAllowedOperationException e = new NotAllowedOperationException();
				e.setFaultString("User cannot download templates of specified document since he cannot develop it.");
				throw e;
			}
			// retrieves template
			IObjTemplateDAO tempdao = DAOFactory.getObjTemplateDAO();
			ObjTemplate temp = tempdao.getBIObjectActiveTemplate(documentId);
			if (temp == null) {
				logger.warn("The template dor document [" + documentId + "] is NULL");
				return null;
			}
			logger.debug("Template dor document [" + documentId + "] retrieved: file name is [" + temp.getName() + "]");
			toReturn = new SDKObjectsConverter().fromObjTemplateToSDKTemplate(temp);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	private SDKExecutedDocumentContent executeKpi(SDKDocument document, BIObject biobj, String userId, String ouputType) {
		logger.debug("IN");
		SDKExecutedDocumentContent toReturn = null;
		SourceBean request = null;
		SourceBean resp = null;
		EMFErrorHandler errorHandler = null;

		try {
			request = new SourceBean("");
			resp = new SourceBean("");
		} catch (SourceBeanException e1) {
			logger.error("Source Bean Exception");
			return null;
		}
		RequestContainer reqContainer = new RequestContainer();
		ResponseContainer resContainer = new ResponseContainer();
		reqContainer.setServiceRequest(request);
		resContainer.setServiceResponse(resp);
		DefaultRequestContext defaultRequestContext = new DefaultRequestContext(reqContainer, resContainer);
		resContainer.setErrorHandler(new EMFErrorHandler());
		RequestContainer.setRequestContainer(reqContainer);
		ResponseContainer.setResponseContainer(resContainer);
		SessionContainer session = new SessionContainer(true);
		reqContainer.setSessionContainer(session);
		errorHandler = defaultRequestContext.getErrorHandler();

		Engine engine;
		try {
			engine = DAOFactory.getEngineDAO().loadEngineByID(document.getEngineId());
		} catch (EMFUserError e1) {
			logger.error("Error while retrieving engine", e1);
			return null;
		}
		if (engine == null) {
			logger.error("No engine found");
			return null;
		}
		String className = engine.getClassName();
		logger.debug("Try instantiating class " + className + " for internal engine " + engine.getName() + "...");
		InternalEngineIFace internalEngine = null;
		// tries to instantiate the class for the internal engine
		try {
			if (className == null || className.trim().equals(""))
				throw new ClassNotFoundException();
			internalEngine = (InternalEngineIFace) Class.forName(className).newInstance();
		} catch (ClassNotFoundException cnfe) {
			logger.error("The class ['" + className + "'] for internal engine " + engine.getName() + " was not found.", cnfe);
			return null;
		} catch (Exception e) {
			logger.error("Error while instantiating class " + className, e);
			return null;
		}

		File tmpFile = null;
		String mimeType = "application/pdf";
		logger.debug("setting object to return of type SDKExecuteDocumentContent");
		toReturn = new SDKExecutedDocumentContent();
		// call exporter!

		if (tmpFile == null) {
			logger.error("file not created");
			return null;
		} else {
			logger.debug("file created");
		}

		try {
			FileDataSource mods = new FileDataSource(tmpFile);
			toReturn.setFileType(mimeType);
			DataHandler dhSource = new DataHandler(mods);
			toReturn.setContent(dhSource);
		}

		finally {
			logger.debug("deleting file Tmp");
			logger.debug("file Tmp deleted");
		}
		logger.debug("OUT");
		return toReturn;

	}

	private SDKExecutedDocumentContent executeReport(SDKDocument document, BIObject biobj, IEngUserProfile profile, String output) {

		logger.debug("IN");
		SDKExecutedDocumentContent toReturn = null;

		try {

			ReportExporter jse = new ReportExporter();
			File tmpFile = jse.getReport(biobj, profile, output);
			if (tmpFile == null) {
				logger.error("File returned from exporter is NULL!");
				return null;
			}

			logger.debug("setting object to return of type SDKExecuteDocumentContent");
			toReturn = new SDKExecutedDocumentContent();
			FileDataSource mods = new FileDataSource(tmpFile);
			DataHandler dataHandler = new DataHandler(mods);
			toReturn.setContent(dataHandler);
			String fileExtension = FileUtils.getFileExtension(tmpFile);
			String fileName = null;
			if (fileExtension != null && !fileExtension.trim().equals("")) {
				fileName = biobj.getLabel() + "." + fileExtension;
			} else {
				fileName = biobj.getLabel();
			}
			String mimeType = MimeUtils.getMimeType(tmpFile);
			logger.debug("Produced file name is " + fileName);
			logger.debug("Produced file mimetype is " + mimeType);
			toReturn.setFileName(fileName);
			toReturn.setFileType(mimeType);
			DataHandler dhSource = new DataHandler(mods);
			toReturn.setContent(dhSource);

		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * Executes a document and return an object containing the result
	 *
	 * @param: document
	 *             : the document
	 * @param: parameters:
	 *             ana array of SDKDocumentParameters, already filled with values
	 * @param: roleName
	 *             : name of the role
	 */

	@Override
	public SDKExecutedDocumentContent executeDocument(SDKDocument document, SDKDocumentParameter[] parameters, String roleName, String outputType)
			throws NonExecutableDocumentException, NotAllowedOperationException, MissingParameterValue, InvalidParameterValue {
		logger.debug("IN");
		SDKExecutedDocumentContent toReturn = null;

		this.setTenant();

		try {

			String output = (outputType != null && !outputType.equals("")) ? outputType : "PDF";

			IEngUserProfile profile = null;

			Integer idDocument = document.getId();

			try {
				profile = getUserProfile();
			} catch (Exception e) {
				logger.error("could not retrieve profile", e);
				throw new NonExecutableDocumentException();
			}

			ExecutionInstance instance = null;
			try {
				instance = new ExecutionInstance(profile, "111", "111", idDocument, roleName, SpagoBIConstants.SDK_EXECUTION_SERVICE, false, false, null);
			} catch (Exception e) {
				logger.error("error while creating instance", e);
				throw new NonExecutableDocumentException();
			}
			// put the parameters value in SDKPArameters into BiObject
			instance.refreshBIObjectWithSDKParameters(parameters);

			// check if there were errors referring to parameters

			List errors = null;
			try {
				errors = instance.getParametersErrors();
			} catch (Exception e) {
				logger.error("error while retrieving parameters errors", e);
				throw new NonExecutableDocumentException();
			}
			if (errors != null && errors.size() > 0) {
				for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
					Object error = iterator.next();
					if (error instanceof EMFUserError) {
						EMFUserError emfUser = (EMFUserError) error;
						String message = "Error on parameter values ";
						if (emfUser.getMessage() != null)
							message += " " + emfUser.getMessage();
						if (emfUser.getAdditionalInfo() != null)
							message += " " + emfUser.getAdditionalInfo();
						logger.error(message);
						throw new MissingParameterValue();
					} else if (error instanceof EMFValidationError) {
						EMFValidationError emfValidation = (EMFValidationError) error;
						String message = "Error while checking parameters: ";
						if (emfValidation.getMessage() != null)
							message += " " + emfValidation.getMessage();
						if (emfValidation.getAdditionalInfo() != null)
							message += " " + emfValidation.getAdditionalInfo();
						logger.error(message);
						throw new InvalidParameterValue();

					}

				}
			}

			logger.debug("Check the document type and call the exporter (if present)");
			try {

				if (document.getType().equalsIgnoreCase("KPI")) { // CASE KPI
					toReturn = executeKpi(document, instance.getBIObject(), (String) profile.getUserUniqueIdentifier(), output);
				} else if (document.getType().equalsIgnoreCase("REPORT") || document.getType().equalsIgnoreCase("ACCESSIBLE_HTML")) { // CASE
					// REPORT
					// OR
					// ACCESSIBLE_HTML
					toReturn = executeReport(document, instance.getBIObject(), profile, output);
				} else {
					logger.error("NO EXPORTER AVAILABLE");
				}

			} catch (Exception e) {
				logger.error("Error while executing document");
				throw new NonExecutableDocumentException();
			}

			if (toReturn == null) {
				logger.error("No result returned by the document");
				throw new NonExecutableDocumentException();
			}

		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}

		return toReturn;
	}

	@Override
	public SDKDocument getDocumentById(Integer id) {
		SDKDocument toReturn = null;
		logger.debug("IN: document in input = " + id);

		this.setTenant();

		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DOCUMENT_MANAGEMENT, "User cannot see documents congifuration.");
			if (id == null) {
				logger.warn("Document identifier in input is null!");
				return null;
			}
			BIObject biObject = DAOFactory.getBIObjectDAO().loadBIObjectById(id);
			if (biObject == null) {
				logger.warn("BiObject with identifier [" + id + "] not existing.");
				return null;
			}
			toReturn = new SDKObjectsConverter().fromBIObjectToSDKDocument(biObject);
		} catch (Exception e) {
			logger.error("Error while retrieving document", e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public SDKDocument getDocumentByLabel(String label) {
		SDKDocument toReturn = null;
		logger.debug("IN: document in input = " + label);

		this.setTenant();

		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DOCUMENT_MANAGEMENT, "User cannot see documents congifuration.");
			if (label == null) {
				logger.warn("Document label in input is null!");
				return null;
			}
			BIObject biObject = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);
			if (biObject == null) {
				logger.warn("BiObject with label [" + label + "] not existing.");
				return null;
			}
			toReturn = new SDKObjectsConverter().fromBIObjectToSDKDocument(biObject);
		} catch (Exception e) {
			logger.error("Error while retrieving document", e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public void uploadDatamartTemplate(SDKTemplate sdkTemplate, SDKTemplate calculatedFields, String dataSourceLabel, String categoryLabel) {
		logger.debug("IN: template file name = [" + sdkTemplate.getFileName() + "] and optional calculatedFields file [" + calculatedFields + "]");

		this.setTenant();

		try {

			// no more step 1 and 1,5. datamart.jar and cfields.xml are no more
			// copied in resources but inserted ins ervice catalogue

			/***********************************************************************************************************/
			/* STEP 1: uploads the datamart document */
			/***********************************************************************************************************/
			try {
				uploadFisicalFile(sdkTemplate, DATAMART_FILE_NAME);
				logger.debug("datamart.jar file uploaded");
			} catch (Exception e) {
				logger.error("Could not upload datamart.jar file", e);
				throw new SpagoBIRuntimeException("Could not upload datamart.jar file: " + e.getMessage());
			}

			try {
				/***********************************************************************************************************/
				/* STEP 1,5: if present uploads also the calculatedFields xml */
				/***********************************************************************************************************/
				if (calculatedFields != null && calculatedFields.getContent() != null) {
					logger.debug("Upload calculatedFields xml: cfields.xml ");
					uploadFisicalFile(calculatedFields, CFIELDS_FILE_NAME);
					logger.debug("cfields.xml file uploaded");
				} else {
					logger.debug("No cfields xml recevied");
				}
			} catch (Exception e) {
				logger.error("Could not upload cfields file", e);
				throw new SpagoBIRuntimeException("Could not upload cfieldds.xml file: " + e.getMessage());
			}

			InputStream is = null;
			DataHandler dh = null;

			try {

				UserProfile userProfile = (UserProfile) this.getUserProfile();

				/***********************************************************************************************************/
				/*
				 * STEP 2: Inserting model in Business Service Catalogue
				 */
				/***********************************************************************************************************/

				// check if model already present
				IMetaModelsDAO metaModelsDAO = DAOFactory.getMetaModelsDAO();
				String modelName = sdkTemplate.getFolderName();
				MetaModel metaModel = metaModelsDAO.loadMetaModelByName(modelName);
				if (metaModel != null) {
					String lockerUserId = metaModel.getModelLocker();
					String uploaderUserId = userProfile.getUserId().toString();

					// Check if the model is locked by another user
					if (lockerUserId != null && !lockerUserId.equals(uploaderUserId)) {
						// model locked by another user, cannot proceed with the update
						logger.debug("Cannot update, the metamodel [" + metaModel.getName() + "] is currently locked by user [" + lockerUserId + "]");
						throw new SpagoBIRuntimeException("The metamodel [" + metaModel.getName() + "] is currently locked by user [" + lockerUserId + "]");
					}

				} else {
					logger.debug("Meta Model " + metaModel + " not already present: go on with insert");
					metaModel = new MetaModel();
					metaModel.setName(modelName);
					metaModel.setDescription(modelName);
					// upload model locked by default
					metaModel.setModelLocked(true);
					metaModel.setModelLocker(userProfile.getUserId().toString());
					// Data Source update
					if (dataSourceLabel != null) {
						metaModel.setDataSourceLabel(dataSourceLabel);
					}

					// retrieve Category Id
					ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
					Domain domain = Optional.ofNullable(categoryDao.getCategoryForBusinessModel(categoryLabel))
						.map(Domain::fromCategory)
						.orElse(null);
					if (domain != null) {
						Integer id = domain.getValueId();
						logger.debug("Associate domain with id: " + id + " and name " + categoryLabel);
						metaModel.setCategory(id);
					} else {
						logger.error("Could not find category domain with name " + categoryLabel + ": deploy anyway");
					}

					metaModelsDAO.insertMetaModel(metaModel);
				}

				// Update content
				Content content = new Content();
				content.setActive(true);
				content.setCreationDate(new Date());
				content.setCreationUser(userProfile.getUserId().toString());
				content.setFileName(DATAMART_FILE_NAME);

				dh = sdkTemplate.getContent();
				is = dh.getInputStream();
				logger.debug("Upload file template....");
				byte[] templateContent = SpagoBIUtilities.getByteArrayFromInputStream(is);
				content.setContent(templateContent);

				metaModelsDAO.insertMetaModelContent(metaModel.getId(), content);

				logger.debug("Meta Model inserted in meta model catalogue;");

			} catch (Exception e) {
				logger.error("Could not insert meta model into meta model catalogue", e);
				throw new SpagoBIRuntimeException("Could not insert meta model into meta model catalogue: " + e.getMessage());
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (Exception e) {
					logger.error("Error in closing io");
				}
			}

			// try {
			/***********************************************************************************************************/
			/*
			 * STEP 3: template creation in SpagoBI Metadata (under the personal folder) to use the previous datamart.
			 */
			/***********************************************************************************************************/
			// UserProfile userProfile = (UserProfile) this.getUserProfile();
			//
			// String datamartName = sdkTemplate.getFolderName();
			// // checks if the template already exists. In this case doesn't
			// // create the new one!
			// if (DAOFactory.getBIObjectDAO().loadBIObjectByLabel(datamartName) != null) {
			// logger.info("The datamart with name " + datamartName + " is already been inserted in SpagoBI. Template not loaded! ");
			// return;
			// }
			//
			// BIObject obj = createGenericObject(SpagoBIConstants.DATAMART_TYPE_CODE);
			// obj.setLabel(datamartName);
			// obj.setName(datamartName);
			// obj.setDescription("");
			// // get the dataSource if label is not null
			// IDataSource dataSource = null;
			// if (dataSourceLabel != null) {
			// logger.debug("retrieve data source with label " + dataSourceLabel);
			// dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dataSourceLabel);
			// obj.setDataSourceId(dataSource.getDsId());
			// }
			//
			// // sets the template's content
			// ObjTemplate objTemplate = createGenericTemplate(sdkTemplate.getFolderName() + ".xml");
			// String template = getDatamartTemplate(datamartName);
			// objTemplate.setContent(template.getBytes());
			//
			// // inserts the document
			// logger.debug("Saving document ...");
			// IBIObjectDAO biObjDAO = DAOFactory.getBIObjectDAO();
			// biObjDAO.setUserProfile(userProfile);
			// biObjDAO.insertBIObject(obj, objTemplate);
			// Integer newIdObj = obj.getId();
			// if (newIdObj != null) {
			// logger.info("Document saved with id = " + newIdObj);
			// } else {
			// logger.error("Document not saved!!");
			// }
			// } catch (Exception e) {
			// logger.error("Error while uploading template", e);
			// throw new SpagoBIRuntimeException("Error while uploading template");
			// }

		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
	}

	@Override
	public void uploadDatamartModel(SDKTemplate sdkTemplate) {
		logger.debug("IN: template file name = [" + sdkTemplate.getFileName() + "]");

		this.setTenant();

		try {
			uploadFisicalFile(sdkTemplate, "");
		} catch (Exception e) {
			logger.error("Error while uploading model template", e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
	}

	@Override
	public SDKTemplate downloadDatamartFile(String folderName, String fileName) {
		LogMF.debug(logger, "IN: folderName = [{0}], fileName = [{1}]", folderName, fileName);
		SDKTemplate toReturn = null;

		this.setTenant();

		try {
			// check first if model is in service catalogue, otherwise return
			// null

			IMetaModelsDAO metaModelsDAO = DAOFactory.getMetaModelsDAO();
			Content content = metaModelsDAO.loadActiveMetaModelContentByName(folderName);
			if (content == null) {
				logger.debug("MetaModel " + folderName + " not found on business service catalogue");
				return null;
			}

			// search for model file into datamart.jar
			byte[] templateContent = getModelFileFromJar(content);

			// model file has to be taken inside datamart.jar, if not found
			// means model could be old, than is taken from resources

			if (templateContent == null) {
				logger.debug("Model file not found inside datamart.jar, take it from resources");
				FileInputStream isDatamartFile = downloadSingleFile(folderName, fileName);
				templateContent = SpagoBIUtilities.getByteArrayFromInputStream(isDatamartFile);
			} else {
				logger.debug("Model file found inside datamart.jar");
			}

			toReturn = new SDKTemplate();
			toReturn.setFileName(fileName);
			SDKObjectsConverter objConverter = new SDKObjectsConverter();
			MemoryOnlyDataSource mods = objConverter.new MemoryOnlyDataSource(templateContent, null);
			DataHandler dhSource = new DataHandler(mods);
			toReturn.setContent(dhSource);

		} catch (Exception e) {
			logger.error("Error downloading datamart file", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * search for model file inside content parameter and return byte array
	 *
	 * @param content
	 * @param toReturn
	 */

	private byte[] getModelFileFromJar(Content content) {
		logger.debug("IN");

		// read jar
		byte[] contentBytes = content.getContent();

		JarFile jar = null;
		FileOutputStream output = null;
		java.io.InputStream is = null;

		try {
			UUIDGenerator uuidGen = UUIDGenerator.getInstance();
			UUID uuidObj = uuidGen.generateTimeBasedUUID();
			String idCas = uuidObj.toString().replaceAll("-", "");
			logger.debug("create temp file for jar");
			String path = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + idCas + ".jar";
			logger.debug("temp file for jar " + path);
			File filee = new File(path);
			output = new FileOutputStream(filee);
			IOUtils.write(contentBytes, output);

			jar = new JarFile(filee);
			logger.debug("jar file created ");

			Enumeration enumEntries = jar.entries();
			while (enumEntries.hasMoreElements()) {
				JarEntry fileEntry = (java.util.jar.JarEntry) enumEntries.nextElement();
				logger.debug("jar content " + fileEntry.getName());

				if (fileEntry.getName().endsWith("sbimodel")) {
					logger.debug("found model file " + fileEntry.getName());
					is = jar.getInputStream(fileEntry);
					byte[] byteContent = SpagoBIUtilities.getByteArrayFromInputStream(is);
					return byteContent;

				}

			}
		} catch (IOException e1) {
			logger.error("the model file could not be takend by datamart.jar due to error, it will be taken from resources ", e1);
			return null;
		} finally {
			try {

				if (jar != null)
					jar.close();
				if (output != null)
					output.close();
				if (is != null)
					is.close();
			} catch (IOException e) {
				logger.error("error in closing streams");
			}
			logger.debug("OUT");
		}
		logger.debug("the model file could not be takend by datamart.jar, probably datamart.jar is old, it will be taken from resources");
		return null;
	}

	// download a zip file with datamart.jar and modelfile
	@Override
	public SDKTemplate downloadDatamartModelFiles(String folderName, String fileDatamartName, String fileModelName) {
		logger.debug("IN");

		this.setTenant();

		File file = null;
		FileOutputStream fileZip = null;
		ZipOutputStream zip = null;
		File inFileZip = null;

		try {
			// These are the files to include in the ZIP file
			String[] filenames = new String[] { fileDatamartName, fileModelName };
			String fileZipName = folderName + ".zip";
			// String path = getResourcePath() +
			// System.getProperty("file.separator") + fileZipName;
			String path = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + fileZipName;

			// Create the ZIP file
			file = new File(path);
			fileZip = new FileOutputStream(file);
			zip = new ZipOutputStream(fileZip);

			for (int i = 0; i < filenames.length; i++) {
				if (filenames[i] != null && !filenames[i].equals("")) {
					// Add ZIP entry to output stream.
					zip.putNextEntry(new ZipEntry(filenames[i]));

					FileInputStream in = downloadSingleFile(folderName, filenames[i]);
					zip.write(SpagoBIUtilities.getByteArrayFromInputStream(in));
					// Complete the entry
					zip.closeEntry();
					in.close();

				}
			}
			// writes the fisical file just created
			zip.close();
			fileZip.close();
			// reopen the zip file as input stream to save as SDKTemplate object
			// because is not possible to convert
			// automatically an outputStream in inputStream
			inFileZip = new File(path);

			// creates the returned object
			SDKTemplate toReturn = new SDKTemplate();
			toReturn.setFileName(fileZipName);
			SDKObjectsConverter objConverter = new SDKObjectsConverter();
			MemoryOnlyDataSource mods = objConverter.new MemoryOnlyDataSource(new FileInputStream(inFileZip), null);
			DataHandler dhSource = new DataHandler(mods);
			toReturn.setContent(dhSource);

			logger.debug("OUT");
			return toReturn;

		} catch (Exception e) {
			logger.error("Error downloading datamart model file", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException e) {
					logger.error("Error closing output stream", e);
				}
			}
			if (fileZip != null) {
				try {
					fileZip.close();
				} catch (IOException e) {
					logger.error("Error closing file output", e);
				}
			}
			if (inFileZip != null) {
				try {
					if (!inFileZip.delete()) {
						inFileZip.deleteOnExit();
					}
				} catch (Exception e) {
					logger.error("Error deleting temporary input zip file", e);
				}
			}
			if (file != null) {
				try {
					if (!file.delete()) {
						file.deleteOnExit();
					}
				} catch (Exception e) {
					logger.error("Error deleting temporary output zip file", e);
				}
			}
		}
	}

	@Override
	public HashMap<String, String> getAllDatamartModels() {
		logger.debug("IN");

		HashMap<String, String> toReturn = new HashMap<String, String>();

		this.setTenant();
		// Models list must be taken by database not by resources
		List<MetaModel> metaModels = DAOFactory.getMetaModelsDAO().loadAllMetaModels();

		if (metaModels == null) {
			logger.warn("No models found");
			return toReturn;
		}

		for (Iterator iterator = metaModels.iterator(); iterator.hasNext();) {
			MetaModel metaModel = (MetaModel) iterator.next();
			toReturn.put(metaModel.getName(), metaModel.getName() + ".sbimodel");
		}

		// overwrite sbimodel files name if they are different form model name
		try {
			String pathDatatamartsDir = getResourcePath();
			File datamartsDir = new File(pathDatatamartsDir);
			File[] dirs = datamartsDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory()) {
						return true;
					}
					return false;
				}
			});
			if (dirs == null || dirs.length == 0) {
				throw new SpagoBIRuntimeException("No datamarts found!! Check configuration for datamarts repository");
			}
			for (int i = 0; i < dirs.length; i++) {
				File dir = dirs[i];
				File[] models = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						if (file.getName().endsWith(".sbimodel")) {
							return true;
						}
						return false;
					}
				});
				for (int j = 0; j < models.length; j++) {
					// return only if present in business service catalogue
					if (toReturn.get(dir.getName()) != null) {
						logger.debug("overwrite model file name of model " + dir.getName() + " to " + models[j].getName());
						toReturn.put(dir.getName(), models[j].getName());
					}
				}
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}

		return toReturn;
	}

	/**
	 * Add the schema mondrian to the catalogue and upload a template that uses it
	 *
	 * @param SDKSchema
	 *            . The object with all informations
	 */
	@Override
	public void uploadMondrianSchema(SDKSchema schema) throws SDKException, NotAllowedOperationException {
		logger.debug("IN");
		this.setTenant();

		try {
			// checks permission
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DOCUMENT_MANAGEMENT, "User cannot see documents configuration.");
			if (schema.getSchemaName() == null) {
				logger.error("Schema name in input is null!");
				// throw new
				// SpagoBIRuntimeException("Error while uploading schema. Schema name is null.");
				throw new SDKException("1000", "Error while uploading schema. Schema name is null.");
				// return;
			}
			if (schema.getSchemaFile() == null || schema.getSchemaFile().getContent() == null) {
				logger.error("Schema file content in input is null!");
				// throw new
				// SpagoBIRuntimeException("Error while uploading schema. Schema file is null.");
				throw new SDKException("1001", "Error while uploading schema. Schema file is null.");
				// return;
			}
			logger.debug("schema name = [" + schema.getSchemaName() + "] - schema description = [" + schema.getSchemaDescription() + "] - schema datasource = ["
					+ schema.getSchemaDataSourceLbl() + "] ");
			UserProfile userProfile = (UserProfile) this.getUserProfile();
			try {
				boolean isNewSchema = true;
				Integer artID = null;
				// defines content to insert
				Content content = createGenericContent();
				DataHandler dh = schema.getSchemaFile().getContent();
				content.setFileName(schema.getSchemaName());
				ByteArrayOutputStream outputDH = new ByteArrayOutputStream();
				dh.writeTo(outputDH);
				byte[] contentSchema = outputDH.toByteArray();
				content.setContent(contentSchema);

				IArtifactsDAO artdao = DAOFactory.getArtifactsDAO();
				Artifact artifact = artdao.loadArtifactByNameAndType(schema.getSchemaName(), MONDRIAN_SCHEMA_TYPE);
				// checks if the artifact already exists. In this case doesn't
				// create the new one!
				if (artifact != null) {
					logger.info(
							"The schema with name " + schema.getSchemaName() + " is already been inserted in SpagoBI catalogue. Artifact will be updated! ");
					isNewSchema = false;
					artID = artifact.getId();
				}
				if (isNewSchema) {
					logger.info("The schema with name " + schema.getSchemaName() + " doesn't exist in SpagoBI catalogue. Artifact will be inserted! ");
					// inserts schema into the catalogue (artifact)
					artifact = new Artifact();
					artifact.setId(new Integer(0));
					artifact.setName(schema.getSchemaName());
					artifact.setDescription(schema.getSchemaDescription());
					artifact.setType(MONDRIAN_SCHEMA_TYPE);
					artdao.insertArtifact(artifact);
					logger.debug("Artifact [" + artifact + "] inserted");
					// gets the new id
					artID = artdao.loadArtifactByNameAndType(schema.getSchemaName(), MONDRIAN_SCHEMA_TYPE).getId();
				}
				// inserts the content of artifact
				artdao.insertArtifactContent(artID, content);
				logger.debug("Content [" + content + "] inserted");

				// sets the template's content
				ObjTemplate objTemplate = createGenericTemplate(schema.getSchemaName() + ".xml");
				String template = getMondrianTemplate(schema.getSchemaName(), content.getContent());
				objTemplate.setContent(template.getBytes());

				// checks if the template already exists.
				boolean isNewObj = true;
				IBIObjectDAO biObjDAO = DAOFactory.getBIObjectDAO();
				biObjDAO.setUserProfile(userProfile);
				BIObject obj = biObjDAO.loadBIObjectByLabel(schema.getSchemaName());
				if (obj != null) {
					logger.info("The schema with name " + schema.getSchemaName()
							+ " is already been inserted in SpagoBI. A new template is loaded for the sbiObject with name  " + obj.getName());
					isNewObj = false;
					objTemplate.setBiobjId(obj.getId());
				} else {
					// creates the template in SpagoBI meta db
					obj = createGenericObject(SpagoBIConstants.OLAP_TYPE_CODE);
				}
				obj.setLabel(schema.getSchemaName());
				obj.setName(schema.getSchemaName());
				obj.setDescription(schema.getSchemaDescription());
				// sets the dataSource if label is not null
				IDataSource dataSource = null;
				if (schema.getSchemaDataSourceLbl() != null) {
					logger.debug("retrieve data source with label " + schema.getSchemaDataSourceLbl());
					dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(schema.getSchemaDataSourceLbl());
					obj.setDataSourceId(dataSource.getDsId());

				}

				if (isNewObj) {
					// inserts the document
					logger.debug("Create document ...");
					biObjDAO.insertBIObject(obj, objTemplate);
					Integer newIdObj = obj.getId();
					if (newIdObj != null) {
						logger.info("Document saved with id = " + newIdObj);
					} else {
						logger.error("Document not saved!!");
						// throw new
						// SpagoBIRuntimeException("Error while saving template.");
						throw new SDKException("1002", "Error while saving template.");
					}
				} else {
					// update the template document
					logger.debug("Modify document ...");
					biObjDAO.modifyBIObject(obj, objTemplate);
				}
			} catch (SDKException se) {
				throw new SDKException(se.getCode(), se.getDescription());
			} catch (Exception e) {
				logger.error("Error while uploading template", e);
				// throw new SpagoBIRuntimeException(e);
				throw new SDKException("1003", e.getMessage());
			}

		} catch (SDKException se) {
			throw new SDKException(se.getCode(), se.getDescription());
		} catch (Exception e) {
			logger.error("Error while uploading schema", e);
			// throw new SpagoBIRuntimeException(e);
			throw new SDKException("1004", e.getMessage());
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
	}

	private void uploadFisicalFile(SDKTemplate sdkTemplate, String defaultName) throws Exception {
		InputStream is = null;
		FileOutputStream osFile = null;
		DataHandler dh = null;

		try {
			String fileName = sdkTemplate.getFolderName();

			// if user cannot develop the specified document, he cannot upload
			// templates on it
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DOCUMENT_MANAGEMENT, "User cannot see documents congifuration.");
			if (sdkTemplate == null) {
				logger.error("SDKTemplate in input is null!");
				return;
			}

			// creates the folder correct (the name is given by the name of the
			// file).
			String path = getResourcePath() + System.getProperty("file.separator") + fileName;
			logger.debug("File path: " + path);
			File datamartFolder = new File(path);
			if (!datamartFolder.exists()) {
				datamartFolder.mkdir();
			}
			path += System.getProperty("file.separator")
					+ (sdkTemplate.getFileName() == null || sdkTemplate.getFileName().equals("") ? defaultName : sdkTemplate.getFileName());
			File datamartFile = new File(path);
			logger.debug("File: " + path);
			if (!datamartFile.exists()) {
				datamartFile.createNewFile();
			}
			osFile = new FileOutputStream(path);
			dh = sdkTemplate.getContent();
			is = dh.getInputStream();
			logger.debug("Upload file template....");
			byte[] templateContent = SpagoBIUtilities.getByteArrayFromInputStream(is);
			/*
			 * ----------- test code --------- String ss = new String(templateContent); logger.debug(ss); ----------- test code ---------
			 */

			osFile.write(templateContent);
			logger.debug("Template uploaded without errors.");

		} catch (Exception e) {
			logger.error("Error while uploading template", e);
			throw e;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("Error closing file input stream", e);
				}
			}
			if (osFile != null) {
				try {
					osFile.close();
				} catch (IOException e) {
					logger.error("Error closing output stream", e);
				}
			}
		}
	}

	private FileInputStream downloadSingleFile(String folderName, String fileName) throws Exception {
		FileInputStream toReturn = null;

		try {
			// if user cannot develop the specified document, he cannot upload
			// templates on it
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DOCUMENT_MANAGEMENT, "User cannot see documents congifuration.");

			// retrieves template
			String path = getResourcePath() + System.getProperty("file.separator") + folderName;
			logger.debug("Path: " + path);
			File folder = new File(path);
			if (!folder.exists()) {
				throw new RuntimeException("Folder [" + folder.getPath() + "] does not exist");
			}
			if (!folder.isDirectory()) {
				throw new RuntimeException("Folder [" + folder + "] is a file not a folder");
			}
			path += System.getProperty("file.separator") + fileName;
			File datamartFile = new File(path);
			logger.debug("File: " + path);
			if (!datamartFile.exists()) {
				throw new RuntimeException("File [" + datamartFile.getPath() + "] does not exist");
			}
			// check file content
			toReturn = new FileInputStream(path);
			if (toReturn == null) {
				logger.warn("The template for document [" + folderName + "] is NULL");
				return null;
			}

			logger.debug("Template for document [" + folderName + "] retrieved.");
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
		return toReturn;
	}

	private String getResourcePath() {

		String path = SpagoBIUtilities.getResourcePath() + File.separatorChar + "qbe";
		// checks if the 'qbe' folder exists. If not, it creates it.
		File datamartFolder = new File(path);
		if (!datamartFolder.exists()) {
			datamartFolder.mkdir();
		}
		// checks if the 'datamarts' folder exists. If not, it creates it.
		path += File.separatorChar + "datamarts";
		datamartFolder = new File(path);
		if (!datamartFolder.exists()) {
			datamartFolder.mkdir();
		}

		// returns the complete path
		return path;
	}

	private String getDatamartTemplate(String datamartName) throws IOException {
		String template = "";
		template += "<QBE>\n";
		template += "\t<DATAMART name=\"" + datamartName + "\"/>\n";
		template += "</QBE>";

		return template;
	}

	private String getMondrianTemplate(String schemaName, byte[] schemaContent) throws Exception {
		String template = "";

		String queryMDX = getQueryMDX(schemaContent);
		template += "<olap>\n";
		template += "\t<cube reference='" + schemaName + "'/>\n";
		template += "\t<MDXquery>\n";
		template += "\t\t" + queryMDX + "\n";
		template += "\t</MDXquery>\n";
		template += "\t<MDXMondrianQuery>\n";
		template += "\t\t" + queryMDX + "\n";
		template += "\t</MDXMondrianQuery>\n";
		template += "</olap>";
		return template;
	}

	private String getQueryMDX(byte[] fileContent) {
		String toReturn = "";
		String schemaStr = "";
		try {
			schemaStr = new String(fileContent);
			SourceBean schemaSB = SourceBean.fromXMLString(schemaStr);
			List cubeLst = schemaSB.getAttributeAsList("Cube");
			if (cubeLst == null || cubeLst.size() == 0)
				throw new Exception("Cannot recover cube bean. Check the schema.");
			SourceBean cubeSB = (SourceBean) cubeLst.get(0);
			if (cubeSB == null)
				throw new Exception("Cannot recover cube bean");
			// searching shared dimensions
			List dimensionLst = cubeSB.getAttributeAsList("DimensionUsage");
			if (dimensionLst == null || dimensionLst.size() == 0) {
				// searching local dimensions
				dimensionLst = cubeSB.getAttributeAsList("Dimension");
			}

			if (dimensionLst == null || dimensionLst.size() == 0)
				throw new Exception("Cannot recover dimensions bean. Check the schema.");
			SourceBean dimensionSB = (SourceBean) dimensionLst.get(0);
			List measuresLst = cubeSB.getAttributeAsList("Measure");
			if (measuresLst == null || measuresLst.size() == 0)
				throw new Exception("Cannot recover measure bean. Check the schema.");
			SourceBean measureSB = (SourceBean) measuresLst.get(0);
			// defines the start query
			toReturn = "select {[Measures].[" + measureSB.getAttribute("name") + "]} on columns, {([" + dimensionSB.getAttribute("name") + "])} on rows from ["
					+ cubeSB.getAttribute("name") + "]";
		} catch (Exception e) {
			LogMF.error(logger, e, "Error while loading SourceBean from xml \n {0}", new Object[] { schemaStr });
			throw new SpagoBIRuntimeException("Error while loading SourceBean from xml. " + e.getMessage(), e);
		}
		return toReturn;
	}

	private Content createGenericContent() throws Exception {
		Content toReturn = new Content();
		toReturn.setActive(new Boolean(true));
		UserProfile userProfile = (UserProfile) this.getUserProfile();
		toReturn.setCreationUser(userProfile.getUserId().toString());
		toReturn.setCreationDate(new Date());
		return toReturn;
	}

	private BIObject createGenericObject(String engineType) throws Exception {
		BIObject toReturn = new BIObject();
		UserProfile userProfile = (UserProfile) this.getUserProfile();
		String userId = userProfile.getUserId().toString();
		logger.debug("Current user id is [" + userId + "]");

		toReturn.setCreationUser(userId);
		toReturn.setCreationDate(new Date());
		toReturn.setVisible(new Boolean(true));
		toReturn.setEncrypt(0);
		toReturn.setStateCode("DEV");
		Domain state = DAOFactory.getDomainDAO().loadDomainByCodeAndValue("STATE", "DEV");
		toReturn.setStateID(state.getValueId());
		// sets the qbe engine
		Domain objectType = DAOFactory.getDomainDAO().loadDomainByCodeAndValue("BIOBJ_TYPE", engineType);
		toReturn.setBiObjectTypeID(objectType.getValueId());
		toReturn.setBiObjectTypeCode(objectType.getValueCd());
		List<Engine> lstEngines = DAOFactory.getEngineDAO().loadAllEnginesForBIObjectType(objectType.getValueCd());
		if (lstEngines == null || lstEngines.size() == 0) {
			logger.error("Error while retrieving Engine list.");
			return null;
		}
		Engine engine = lstEngines.get(0);
		toReturn.setEngine(engine);

		// sets the default functionality (personal folder).
		List functionalities = new ArrayList();
		LowFunctionality funct = null;
		funct = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath("/" + userId, false);
		if (funct != null) {
			functionalities.add(funct.getId());
			toReturn.setFunctionalities(functionalities);
		} else {
			// the personal folder doesn't exist yet. It creates it, and uses
			// it.
			UserUtilities.createUserFunctionalityRoot(userProfile);
			logger.error("Error while retrieving Functionality identifier.");
			funct = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath("/" + userId, false);
			functionalities.add(funct.getId());
			toReturn.setFunctionalities(functionalities);
		}
		return toReturn;
	}

	private ObjTemplate createGenericTemplate(String name) throws Exception {
		ObjTemplate toReturn = new ObjTemplate();
		UserProfile userProfile = (UserProfile) this.getUserProfile();
		toReturn.setName(name);
		toReturn.setActive(new Boolean(true));
		toReturn.setCreationUser((String) userProfile.getUserId());
		toReturn.setCreationDate(new Date());
		return toReturn;
	}

	@Override
	public String getLockStatus(SDKDocument document) throws NotAllowedOperationException {
		logger.debug("IN");
		logger.debug("OUT");
		return null;
	}

	@Override
	public String changeLockStatus(SDKDocument document) throws NotAllowedOperationException {
		logger.debug("IN");
		// TODO change parameter in String documentLabel
		String toReturn = null;
		String documentLabel = document.getLabel();
		try {
			BIObject biObj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(documentLabel);

			if (biObj == null) {
				logger.warn("Document with label " + documentLabel + " is not present");
				throw new RuntimeException("Document with label " + documentLabel + " is not present");
			}

			String previousLockerUser = biObj.getLockedByUser();
			String currentUser = (String) ((UserProfile) getUserProfile()).getUserId();

			boolean isUserAdmin = getUserProfile().isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN);

			boolean isLocked = false;
			boolean isLockedByCurrentUser = false;

			if (previousLockerUser == null || previousLockerUser.equals("")) {
				isLocked = false;
				isLockedByCurrentUser = false;
			} else if (previousLockerUser != null && previousLockerUser.equals(currentUser)) {
				isLocked = true;
				isLockedByCurrentUser = true;
			} else {
				isLocked = true;
				isLockedByCurrentUser = false;
			}
			IBIObjectDAO biObjDAO = DAOFactory.getBIObjectDAO();
			biObjDAO.setUserProfile(getUserProfile());

			if (isLocked == false) {
				logger.debug("Document " + documentLabel + " is not locked, lock it");
				toReturn = biObjDAO.changeLockStatus(documentLabel, isUserAdmin);
			} else if (isLocked == true && isLockedByCurrentUser == true) {
				logger.debug("Document " + documentLabel + " is locked by current user, unlock");
				toReturn = biObjDAO.changeLockStatus(documentLabel, isUserAdmin);
			} else if (isLocked == true && isLockedByCurrentUser == false && isUserAdmin == true) {
				logger.debug("Document " + documentLabel + " is not locked by current user but current user is administrator");
				toReturn = biObjDAO.changeLockStatus(documentLabel, isUserAdmin);
			} else {
				logger.debug("Document " + documentLabel + " is locked by another user and current user is not administrator is, don't change");
				toReturn = previousLockerUser;
			}

		} catch (Exception e) {
			logger.error("Error in changing lock status", e);
			throw new RuntimeException("Error in changing lock status for document with label " + documentLabel);

		}
		logger.debug("OUT");
		return toReturn;
	}
}
