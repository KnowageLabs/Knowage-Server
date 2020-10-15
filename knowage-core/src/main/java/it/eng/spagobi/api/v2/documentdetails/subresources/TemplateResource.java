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
package it.eng.spagobi.api.v2.documentdetails.subresources;

import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.log4j.Logger;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.JSError;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("")
public class TemplateResource extends AbstractSpagoBIResource {

	public static enum FILETYPE {
		json, xml, bin, rptdesign, sbicockpit, jrxml, pdf, xls, xlsx, doc, docx, ppt, pptx
	};

	static protected Logger logger = Logger.getLogger(TemplateResource.class);

	@SuppressWarnings("unchecked")
	@GET
	@Path("/")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public List<ObjTemplate> getDocumentTemplates(@PathParam("id") Integer id) {
		logger.debug("IN");
		IBIObjectDAO documentDao = null;
		BIObject document = null;
		List<ObjTemplate> documentTemplates = null;
		try {
			documentDao = DAOFactory.getBIObjectDAO();
			document = documentDao.loadBIObjectById(id);
			documentTemplates = document.getTemplateList();

			Assert.assertNotNull(document, "Document can not be null");
			Assert.assertNotNull(documentTemplates, "Document Template can not be null");
		} catch (EMFUserError e) {
			logger.debug("Could not get content from template", e);
			throw new SpagoBIRestServiceException("Could not get content from templates", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return documentTemplates;
	}

	@GET
	@Path("/{templateId}")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public byte[] getActiveDocumentTemplate(@PathParam("id") Integer id) {
		logger.debug("IN");
		IBIObjectDAO documentDao = null;
		BIObject document = null;
		byte[] temp = null;
		try {
			documentDao = DAOFactory.getBIObjectDAO();
			document = documentDao.loadBIObjectById(id);
			ObjTemplate documentTemplate = document.getActiveTemplate();
			temp = documentTemplate.getContent();
			Assert.assertNotNull(document, "Document can not be null");
			Assert.assertNotNull(documentTemplate, "Document Template can not be null");
		} catch (EMFUserError e) {
			logger.debug("Could not get content from template", e);
			throw new SpagoBIRestServiceException("Could not get content from template", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return temp;
	}

	@GET
	@Path("/selected/{templateId}")
	@Produces("application/text")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public byte[] getDocumentTemplate(@PathParam("id") Integer id, @PathParam("templateId") Integer templateId) {
		logger.debug("IN");
		byte[] temp = null;
		IObjTemplateDAO templateDAO = null;
		try {
			templateDAO = DAOFactory.getObjTemplateDAO();
			ObjTemplate documentTemplate = templateDAO.loadBIObjectTemplate(templateId);
			temp = documentTemplate.getContent();
			Assert.assertNotNull(documentTemplate, "Document Template can not be null");
		} catch (EMFInternalError e) {
			logger.debug("Could not get content from template", e);
			throw new SpagoBIRestServiceException("Could not get content from template", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return temp;
	}

	/**
	 * Template upload
	 **/
	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadTemplate(MultiPartBody body, @PathParam("id") int id) {

		ObjTemplate template = new ObjTemplate();
		byte[] bytes = null;
		IBIObjectDAO documentDao = null;
		BIObject document = null;
		try {
			IObjTemplateDAO templateDAO = DAOFactory.getObjTemplateDAO();
			templateDAO.setUserProfile(getUserProfile());

			final FormFile file = body.getFormFileParameterValues("file")[0];

			template.setName(file.getFileName());
			bytes = file.getContent();
			template.setContent(bytes);
			template.setCreationDate(new Date());
			template.setCreationUser(getUserProfile().getUserName().toString());
			template.setBiobjId(id);

			documentDao = DAOFactory.getBIObjectDAO();
			document = documentDao.loadBIObjectById(id);

			templateDAO.insertBIObjectTemplate(template, document);

		} catch (Exception e) {
			logger.debug("Template could not be uploaded", e);
			throw new SpagoBIRestServiceException("Template could not be uploaded", buildLocaleFromSession(), e);
		}

		logger.debug("OUT");
		return Response.status(200).build();
	}

	/**
	 * Download template from database with specified id (in progress)
	 **/
	@GET
	@Path("/{templateId}/{filetype}/file")
	public Response downloadTemplate(@PathParam("templateId") Integer templateId, @PathParam("filetype") FILETYPE filetype) {
		logger.debug("IN");
		ResponseBuilder response = Response.ok();
		try {
			IObjTemplateDAO templateDAO = DAOFactory.getObjTemplateDAO();

			templateDAO.setUserProfile(getUserProfile());

			ObjTemplate template = new ObjTemplate();

			template = templateDAO.loadBIObjectTemplate(templateId);
			template.getContent();
			String filename = template.getName();
			int typePos = filename.lastIndexOf(".");
			if (typePos > 0) {
				filename = filename.substring(0, typePos) + "." + filetype.name().toLowerCase();
			}
			byte[] byteContent = null;
			switch (filetype) {
			case json:
			case xml:
			case bin:
			case rptdesign:
			case sbicockpit:
			case jrxml:
			case pdf:
			case xls:
			case xlsx:
			case doc:
			case docx:
			case ppt:
			case pptx:
				byteContent = template.getContent();
				response = Response.ok(byteContent);
				response.header("Content-Disposition", "attachment; filename=" + filename);
				break;
			default:
				response = Response.ok(new JSError().addError("Not valid filetype [" + filetype + "]"));
				break;
			}
		} catch (Exception e) {
			logger.debug("Template could not be downloaded", e);
			throw new SpagoBIRestServiceException("Template could not be downloaded", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return response.build();
	}

	@PUT
	@Path("/{templateId}")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public void setDocTemplateActive(@PathParam("id") Integer id, @PathParam("templateId") Integer templateId) {
		logger.debug("IN");
		Assert.assertNotNull(templateId, "Template ID can not be null");

		BIObject document = null;
		IObjTemplateDAO templateDAO = null;
		IBIObjectDAO documentDao = null;
		try {
			documentDao = DAOFactory.getBIObjectDAO();
			templateDAO = DAOFactory.getObjTemplateDAO();
			document = documentDao.loadBIObjectById(id);
			ObjTemplate template = templateDAO.loadBIObjectTemplate(templateId);
			templateDAO.setTemplateActive(template, document);
		} catch (EMFUserError | EMFInternalError e) {
			logger.debug("Template could not be modified", e);
			throw new SpagoBIRestServiceException("Template could not be modified", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
	}

	@DELETE
	@Path("/{templateId}")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public Integer deleteDocumentActiveTemplateById(@PathParam("id") Integer id, @PathParam("templateId") Integer templateId) {
		logger.debug("IN");
		Assert.assertNotNull(templateId, "Template ID can not be null");
		IObjTemplateDAO templateDAO = null;
		try {
			templateDAO = DAOFactory.getObjTemplateDAO();
			templateDAO.deleteBIObjectTemplate(templateId);
		} catch (EMFInternalError e) {
			logger.debug("Template could not be deleted", e);
			throw new SpagoBIRestServiceException("Template could not be deleted", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return templateId;
	}

}
