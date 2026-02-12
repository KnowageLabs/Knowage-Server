package it.eng.spagobi.engines.officedocument;

import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.content.service.ContentServiceImplSupplier;
import it.eng.spagobi.utilities.mime.MimeUtils;

@Path("/2.0/officeContent")
public class GetOfficeContentResource extends AbstractSpagoBIResource {

	private static Logger logger = Logger.getLogger(GetOfficeContentResource.class);

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getOfficeContent(@Context HttpServletRequest req) throws Exception {
		logger.debug("IN");

		String documentId = req.getParameter("documentId");
		String role = req.getParameter(SpagoBIConstants.ROLE);
		IEngUserProfile profile = getUserProfile();
		BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(Integer.valueOf(documentId));

		if (!ObjectsAccessVerifier.canSee(obj, profile)) {
			return Response.status(Response.Status.FORBIDDEN).entity("Access denied for document " + obj.getLabel()).build();
		}

		List correctRoles = ObjectsAccessVerifier.getCorrectRolesForExecution(obj.getId(), profile);
		if (correctRoles == null || correctRoles.isEmpty()) {
			return Response.status(Response.Status.FORBIDDEN).entity("No valid roles for execution").build();
		}
		if (role != null && !correctRoles.contains(role)) {
			return Response.status(Response.Status.FORBIDDEN).entity("No valid roles for execution").build();
		}

		AuditManager auditManager = AuditManager.getInstance();
		Integer auditId = auditManager.insertAudit(obj, null, profile, role, SpagoBIConstants.NORMAL_EXECUTION_MODALITY);

		if (auditId != null) {
			auditManager.updateAudit(auditId, System.currentTimeMillis(), null, "EXECUTION_STARTED", null, null);
		}

		try {

			if (documentId == null) {
				throw new WebApplicationException("Document id missing!", Response.Status.BAD_REQUEST);
			}

			ContentServiceImplSupplier supplier = new ContentServiceImplSupplier();
			Content template = supplier.readTemplate(profile.getUserUniqueIdentifier().toString(), documentId, null);
			String fileName = template.getFileName() != null ? template.getFileName() : "";
			String mimeType = MimeUtils.getMimeType(fileName);

			byte[] contentBytes = Base64.getDecoder().decode(template.getContent());

			Response.ResponseBuilder builder = Response.ok(contentBytes);
			builder.header("Content-Disposition", "inline; filename=" + fileName);
			builder.type(mimeType);

			if (auditId != null) {
				auditManager.updateAudit(auditId, null, System.currentTimeMillis(), "EXECUTION_PERFORMED", null, null);
			}

			return builder.build();

		} catch (Exception e) {
			logger.error("Exception", e);
			if (auditId != null) {
				auditManager.updateAudit(auditId, null, System.currentTimeMillis(), "EXECUTION_FAILED", e.getMessage(), null);
			}
			throw new WebApplicationException("Error retrieving content", e, Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			logger.debug("OUT");
		}
	}

}
