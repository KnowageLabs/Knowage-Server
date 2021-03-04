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

import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.AnalyticalModelDocumentManagementAPI;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.analiticalmodel.document.utils.CockpitStatisticsTablesUtils;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.DocumentUtilities;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public abstract class AbstractDocumentResource extends AbstractSpagoBIResource {

	public Response insertDocument(String body) {
		BIObject document = (BIObject) JsonConverter.jsonToValidObject(body, BIObject.class);

		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());

		document.setTenant(getUserProfile().getOrganization());
		document.setCreationUser((String) getUserProfile().getUserId());

		List<Integer> functionalities = document.getFunctionalities();
		for (Integer functionality : functionalities) {
			if (!ObjectsAccessVerifier.canDev(functionality, getUserProfile())) {
				String path = "";
				try {
					path = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(functionality, false).getPath();
				} catch (EMFUserError e) {
					// Do nothing, the correct SpagoBIRuntimeException will be
					// throwed anyway. Only the path will be missing
				}

				throw new SpagoBIRuntimeException("User [" + getUserProfile().getUserName() + "] has no rights to create a document inside [" + path + "]");
			}
		}

		if (documentManager.isAnExistingDocument(document))
			throw new SpagoBIRuntimeException("The document already exists");

		documentManager.saveDocument(document, null);

		try {
			String encodedLabel = URLEncoder.encode(document.getLabel(), "UTF-8");
			encodedLabel = encodedLabel.replaceAll("\\+", "%20");
			return Response.created(new URI("1.0/documents/" + encodedLabel)).build();
		} catch (Exception e) {
			logger.error("Error while creating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while creating url of the new resource", e);
		}
	}

	public Response getDocumentDetails(Object documentIdentifier) {

		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(documentIdentifier);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with identifier [" + documentIdentifier + "] doesn't exist");

		try {
			if (ObjectsAccessVerifier.canSee(document, getUserProfile())) {
				String toBeReturned = JsonConverter.objectToJson(document, BIObject.class);
				return Response.ok(toBeReturned).build();
			} else
				throw new SpagoBIRuntimeException(
						"User [" + getUserProfile().getUserName() + "] has no rights to see document with identifier [" + documentIdentifier + "]");

		} catch (SpagoBIRuntimeException e) {
			throw e;
		} catch (EMFInternalError e) {
			logger.error("Error while looking for authorizations", e);
			throw new SpagoBIRuntimeException("Error while looking for authorizations", e);
		} catch (Exception e) {
			logger.error("Error while converting document in Json", e);
			throw new SpagoBIRuntimeException("Error while converting document in Json", e);
		}

	}

	public Response updateDocument(String label, String body) {
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject oldDocument = documentManager.getDocument(label);
		if (oldDocument == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");

		Integer id = oldDocument.getId();
		if (!ObjectsAccessVerifier.canDevBIObject(id, getUserProfile()))
			throw new SpagoBIRuntimeException("User [" + getUserProfile().getUserName() + "] has no rights to update document with label [" + label + "]");

		BIObject document = (BIObject) JsonConverter.jsonToValidObject(body, BIObject.class);

		document.setLabel(label);
		document.setId(id);
		documentManager.saveDocument(document, null);
		return Response.ok().build();
	}

	public Response deleteDocument(@PathParam("label") String label) {
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(label);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");

		if (ObjectsAccessVerifier.canDeleteBIObject(document.getId(), getUserProfile())) {

			if (!DocumentUtilities.getValidLicenses().isEmpty()) {
				try {
					CockpitStatisticsTablesUtils.deleteCockpitWidgetsTable(document, HibernateSessionManager.getCurrentSession());
					DAOFactory.getBIObjectDAO().eraseBIObject(document, null);
				} catch (EMFUserError e) {
					logger.error("Error while deleting the specified document", e);
					throw new SpagoBIRuntimeException("Error while deleting the specified document", e);
				}
			}

			return Response.ok().build();
		} else
			throw new SpagoBIRuntimeException("User [" + getUserProfile().getUserName() + "] has no rights to delete document with label [" + label + "]");
	}

	protected Object getObjectIdentifier(String labelOrId) {
		Object documentIdentifier = null;
		try {
			Integer id = Integer.parseInt(labelOrId);
			documentIdentifier = id;
		} catch (NumberFormatException e) {
			logger.debug("Cannot parse input parameter [" + labelOrId + "] as an integer");
			documentIdentifier = labelOrId;
		}
		logger.debug("Document identifier [" + documentIdentifier + "]");
		return documentIdentifier;
	}

	public Response getDocumentTemplate(String label) {
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(label);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");

		if (!ObjectsAccessVerifier.canDevBIObject(document, getUserProfile()))
			throw new SpagoBIRuntimeException(
					"User [" + getUserProfile().getUserName() + "] has no rights to see template of document with label [" + label + "]");

		ResponseBuilder rb;
		ObjTemplate template = document.getActiveTemplate();

		// The template has not been found
		if (template == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't contain a template");
		try {
			rb = Response.ok(template.getContent());
		} catch (Exception e) {
			logger.error("Error while getting document template", e);
			throw new SpagoBIRuntimeException("Error while getting document template", e);
		}

		rb.header("Content-Disposition", "attachment; filename=" + document.getActiveTemplate().getName());
		return rb.build();
	}

	public Response addDocumentTemplate(String label, MultiPartBody input) {
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(label);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");

		if (!ObjectsAccessVerifier.canDevBIObject(document, getUserProfile()))
			throw new SpagoBIRuntimeException(
					"User [" + getUserProfile().getUserName() + "] has no rights to manage the template of document with label [" + label + "]");

		final FormFile file = input.getFormFileParameterValues("file")[0];

		if (file == null)
			return Response.status(Response.Status.BAD_REQUEST).build();

		try {

			byte[] content = file.getContent();

			ObjTemplate template = new ObjTemplate();
			template.setContent(content);
			template.setName(file.getFileName());

			documentManager.saveDocument(document, template);

			return Response.ok().build();
		} catch (SpagoBIRuntimeException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error while getting the template", e);
			throw new SpagoBIRuntimeException("Error while getting the template", e);
		}

	}

	public Response deleteCurrentTemplate(String documentLabel) {
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(documentLabel);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + documentLabel + "] doesn't exist");

		if (!ObjectsAccessVerifier.canDevBIObject(document, getUserProfile()))
			throw new SpagoBIRuntimeException(
					"User [" + getUserProfile().getUserName() + "] has no rights to manage the template of document with label [" + documentLabel + "]");

		IObjTemplateDAO templateDAO = null;
		try {
			templateDAO = DAOFactory.getObjTemplateDAO();
			ObjTemplate template = templateDAO.getBIObjectActiveTemplate(document.getId());
			if (template != null) {
				templateDAO.setPreviousTemplateActive(document.getId(), template.getId());
				templateDAO.deleteBIObjectTemplate(template.getId());
			} else {
				logger.debug("Document with label [" + documentLabel + "] has no active template, nothing to delete...");
			}

		} catch (Exception e) {
			logger.error("Error with deleting current template for document with label: " + documentLabel, e);
			throw new SpagoBIRestServiceException("Error with deleting template for document with label: " + documentLabel, buildLocaleFromSession(), e);
		}
		return Response.ok().build();
	}

	public Response deleteTemplateById(String documentLabel, Integer templateId) {
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(documentLabel);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + documentLabel + "] doesn't exist");

		if (!ObjectsAccessVerifier.canDevBIObject(document, getUserProfile()))
			throw new SpagoBIRuntimeException(
					"User [" + getUserProfile().getUserName() + "] has no rights to manage the template of document with label [" + documentLabel + "]");

		IObjTemplateDAO templateDAO = null;
		try {
			templateDAO = DAOFactory.getObjTemplateDAO();
			if (document.getActiveTemplate().getId().equals(templateId)) {
				templateDAO.setPreviousTemplateActive(document.getId(), templateId);
			}
			templateDAO.deleteBIObjectTemplate(templateId);

		} catch (Exception e) {
			logger.error("Error with deleting template with id: " + templateId, e);
			throw new SpagoBIRestServiceException("Error with deleting template with id: " + templateId, buildLocaleFromSession(), e);
		}
		return Response.ok().build();
	}

}
