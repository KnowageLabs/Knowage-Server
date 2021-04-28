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
package it.eng.spagobi.api.v2.documentdetails;

import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.utils.CockpitStatisticsTablesUtils;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.api.v2.documentdetails.subresources.DataDependenciesResource;
import it.eng.spagobi.api.v2.documentdetails.subresources.DocumentImageResource;
import it.eng.spagobi.api.v2.documentdetails.subresources.DriversResource;
import it.eng.spagobi.api.v2.documentdetails.subresources.OutputParametarsResource;
import it.eng.spagobi.api.v2.documentdetails.subresources.SubreportsDocumentResource;
import it.eng.spagobi.api.v2.documentdetails.subresources.TemplateResource;
import it.eng.spagobi.api.v2.documentdetails.subresources.VisualDependenciesResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.DocumentUtilities;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/documentdetails")
public class DocumentResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DocumentResource.class);

	@SuppressWarnings("unchecked")
	@GET
	@Path("/")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public List<BIObject> getDocuments() {
		logger.debug("IN");
		IBIObjectDAO documentsDao = null;
		List<BIObject> allDocuments = null;
		try {
			documentsDao = DAOFactory.getBIObjectDAO();
			allDocuments = documentsDao.loadAllBIObjects();
		} catch (EMFUserError e) {
			logger.debug("Documents objects can not be provided", e);
			throw new SpagoBIRestServiceException("Getting documents has failed", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return allDocuments;
	}

	@POST
	@Path("/")
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public BIObject insertDocument(@Valid BIObject document) {
		logger.debug("IN");
		IBIObjectDAO documentDao = null;
		Assert.assertNotNull(document, "Document can not be null");
		document.setCreationUser((String) getUserProfile().getUserId());

		if (documentLabelNameControl(document, "INSERT", "label")) {
			logger.error("Error while inserting document. Document with the same label already exists!");
			throw new SpagoBIRuntimeException("Error while inserting document. Document with the same label already exists!");
		}

		if (documentLabelNameControl(document, "INSERT", "name")) {
			logger.error("Error while inserting document. Document with the same name already exists!");
			throw new SpagoBIRuntimeException("Error while inserting document. Document with the same name already exists!");
		}

		try {
			documentDao = DAOFactory.getBIObjectDAO();
			Integer documentId = documentDao.insertBIObject(document);
			logger.debug("OUT");
			document.setId(documentId);
			return document;
		} catch (Exception e) {
			logger.error("Document could not be created", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public BIObject updateDocument(@PathParam("id") Integer id, @Valid BIObject document) {
		logger.debug("IN");
		IBIObjectDAO documentDao = null;

		try {
			documentDao = DAOFactory.getBIObjectDAO();

			// document = documentDao.loadBIObjectById(id);

			Assert.assertNotNull(document, "Document can not be null");

			documentDao.modifyBIObject(document);
			document = documentDao.loadBIObjectById(document.getId());
		} catch (EMFUserError e) {
			logger.error("Document can not be updated", e);
			throw new SpagoBIRestServiceException("Updating document has failed", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return document;
	}

	@GET
	@Path("/{id}")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public BIObject getDocumentById(@PathParam("id") Integer id) {
		logger.debug("IN");
		IBIObjectDAO documentDao = null;
		BIObject document = null;
		try {
			documentDao = DAOFactory.getBIObjectDAO();
			document = documentDao.loadBIObjectById(id);
			Assert.assertNotNull(document, "Document can not be null");
		} catch (EMFUserError e) {
			logger.error("Document could not be loaded", e);
			throw new SpagoBIRestServiceException("Could not get document", buildLocaleFromSession(), e);
		}
		logger.debug("Object that is returning is " + document);
		return document;
	}

	@DELETE
	@Path("/{id}")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public Integer deleteDocument(@PathParam("id") Integer id) {
		logger.debug("IN");
		IBIObjectDAO documentDao = null;
		BIObject document = null;

		try {
			documentDao = DAOFactory.getBIObjectDAO();
			document = documentDao.loadBIObjectById(id);

			if (!DocumentUtilities.getValidLicenses().isEmpty())
				CockpitStatisticsTablesUtils.deleteCockpitWidgetsTable(document, HibernateSessionManager.getCurrentSession());

			DAOFactory.getBIObjectDAO().eraseBIObject(document, null);
			Assert.assertNotNull(document, "Document can not be null");
		} catch (EMFUserError e) {
			logger.error("Document can not be deleted", e);
			throw new SpagoBIRestServiceException("Deleting of document has failed", buildLocaleFromSession(), e);
		}

		logger.debug("OUT");
		return id;
	}

	private boolean documentLabelNameControl(BIObject document, String operation, String comparator) {
		String labelToCheck = document.getLabel();
		String nameToCheck = document.getName();
		List<BIObject> allDocuments = null;
		try {
			allDocuments = DAOFactory.getBIObjectDAO().loadAllBIObjects();
		} catch (EMFUserError e) {
			logger.error("Error loading documents for label testing");
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
		if (operation.equalsIgnoreCase("INSERT")) {
			Iterator it = allDocuments.iterator();
			while (it.hasNext()) {
				BIObject aDocument = (BIObject) it.next();
				if (comparator.equalsIgnoreCase("label")) {
					String label = aDocument.getLabel();
					if (label.equals(labelToCheck)) {
						return true;
					}
				} else if (comparator.equalsIgnoreCase("name")) {
					String name = aDocument.getName();
					if (name.equals(nameToCheck)) {
						return true;
					}
				}
			}
		} else {
			Integer currentId = document.getId();
			Iterator it = allDocuments.iterator();
			while (it.hasNext()) {
				BIObject aDocument = (BIObject) it.next();
				Integer id = aDocument.getId();
				if (comparator.equalsIgnoreCase("label")) {
					String label = aDocument.getLabel();
					if (label.equals(labelToCheck) && (!id.equals(currentId))) {
						return true;
					}
				} else if (comparator.equalsIgnoreCase("name")) {
					String name = aDocument.getName();
					if (name.equals(nameToCheck) && (!id.equals(currentId))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Path("/{id}/drivers")
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public DriversResource getDocumentDrivers(@PathParam("id") Integer id) {
		logger.debug("Getting DriversResource instance");
		return new DriversResource();
	}

	@Path("/{id}/outputparameters")
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public OutputParametarsResource getDocumentOutputParameters(@PathParam("id") Integer id) {
		logger.debug("Getting OutputParametarsResource instance");
		return new OutputParametarsResource();
	}

	@Path("/{id}/templates")

	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public TemplateResource getDocumentTemplates(@PathParam("id") Integer id) {
		logger.debug("Getting TemplateResource instance");
		return new TemplateResource();
	}

	@Path("/{id}/datadependencies")
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public DataDependenciesResource getDataDependecies(@PathParam("id") Integer id, @QueryParam("driverId") Integer driverId) {
		logger.debug("Getting DataDependenciesResource instance");
		return new DataDependenciesResource();
	}

	@Path("/{id}/visualdependencies")
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public VisualDependenciesResource getVisualDependencies() {
		logger.debug("Getting VisualDependenciesResource instance");
		return new VisualDependenciesResource();
	}

	@Path("/{id}/subreports")
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public SubreportsDocumentResource getSubreports(@PathParam("id") Integer id) {
		logger.debug("Getting Subreport instance");
		return new SubreportsDocumentResource();
	}

	@Path("/{id}/image")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public DocumentImageResource getDocumentPreview(@PathParam("id") Integer id) {
		logger.debug("Getting Document Image instance");
		return new DocumentImageResource();
	}
}