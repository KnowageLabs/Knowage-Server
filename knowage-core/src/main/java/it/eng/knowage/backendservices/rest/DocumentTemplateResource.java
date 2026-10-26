/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.backendservices.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.knowage.backendservices.utils.BirtTemplateUpgrade;
import it.eng.spagobi.analiticalmodel.document.DocumentTemplateBuilder;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.engines.drivers.birt.BirtReportDriver;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.content.service.ContentServiceImplSupplier;
import it.eng.spagobi.user.UserProfileManager;

@Path("/2.0/backendservices/documenttemplate")
public class DocumentTemplateResource {

	static protected Logger logger = Logger.getLogger(DocumentTemplateResource.class);

	@GET
	@Path("/{document_id}/birt-upgrade")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> checkBirtUpgrade(@PathParam("document_id") Integer documentId) throws Exception {
		UserProfile profile = UserProfileManager.getProfile();
		BIObject document = getAccessibleDocument(documentId, profile);
		Map<String, Object> result = new HashMap<>();
		boolean birt = document.getEngine() != null && BirtReportDriver.class.getName().equals(document.getEngine().getDriverName());
		ObjTemplate template = birt ? DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(documentId) : null;
		result.put("needsUpgrade", template != null && BirtTemplateUpgrade.needsUpgrade(template.getContent(), template.getName()));
		result.put("templateId", template == null ? null : template.getId());
		result.put("canUpgrade", ObjectsAccessVerifier.canDevBIObject(document, profile));
		return result;
	}

	@POST
	@Path("/{document_id}/birt-upgrade")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upgradeBirtTemplate(@PathParam("document_id") Integer documentId, Map<String, Integer> params) throws Exception {
		UserProfile profile = UserProfileManager.getProfile();
		BIObject document = getAccessibleDocument(documentId, profile);
		if (!ObjectsAccessVerifier.canDevBIObject(document, profile)) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
		if (document.getEngine() == null || !BirtReportDriver.class.getName().equals(document.getEngine().getDriverName())) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		IObjTemplateDAO dao = DAOFactory.getObjTemplateDAO();
		ObjTemplate current = dao.getBIObjectActiveTemplate(documentId);
		// The confirmation applies only to the revision inspected by the user.
		if (current == null || params == null || !current.getId().equals(params.get("templateId"))) {
			throw new WebApplicationException(Response.Status.CONFLICT);
		}
		byte[] content = current.getContent();
		if (BirtTemplateUpgrade.needsUpgrade(content, current.getName())) {
			ObjTemplate upgraded = new DocumentTemplateBuilder().buildDocumentTemplate(current.getName(),
					profile.getUserName().toString(), BirtTemplateUpgrade.upgrade(content, current.getName()));
			upgraded.setBiobjId(documentId);
			upgraded.setActive(true);
			dao.setUserProfile(profile);
			dao.insertBIObjectTemplate(upgraded, document);
		}
		return Response.ok("{\"upgraded\":true}").build();
	}

	private BIObject getAccessibleDocument(Integer documentId, UserProfile profile) throws Exception {
		BIObject document = DAOFactory.getBIObjectDAO().loadBIObjectById(documentId);
		if (document == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		if (!ObjectsAccessVerifier.canSee(document, profile)) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
		return document;
	}

	@POST
	@Path("/{document_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public String readTemplate(@PathParam("document_id") Integer documentId, Map<String, String> params) {
		logger.debug("IN");
		UserProfile userProfile = UserProfileManager.getProfile();
		String userId = (String) userProfile.getUserUniqueIdentifier();
		ContentServiceImplSupplier supplier = new ContentServiceImplSupplier();
		Content content;
		try {
			content = supplier.readTemplate(userId, Integer.toString(documentId), params);
		} catch (Exception e) {
			logger.error("error while retrieving template for userId [" + userId + "] and documentId [" + documentId + "]");
			return null;
		} finally {
			logger.debug("OUT");
		}
		return content.getContent();
	}

}
