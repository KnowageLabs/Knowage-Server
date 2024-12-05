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
package it.eng.spagobi.engines.officedocument;

import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.owasp.esapi.HTTPUtilities;
import org.owasp.esapi.reference.DefaultHTTPUtilities;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.content.service.ContentServiceImplSupplier;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.utilities.mime.MimeUtils;

public class GetOfficeContentAction extends AbstractHttpAction {

    private static transient Logger logger=Logger.getLogger(GetOfficeContentAction.class);
    private static HTTPUtilities httpUtils = new DefaultHTTPUtilities();

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	@Override
	public void service(SourceBean request, SourceBean responseSb) throws Exception {
		logger.debug("IN");
		// delete userProfile in PermanentContainer spagoFramework put by officedoc.jsp
		// it was necessary for ActionCoordinator of spagoFramework
		this.getRequestContainer().getSessionContainer().getPermanentContainer().delAttribute(IEngUserProfile.ENG_USER_PROFILE);
		freezeHttpResponse();

		HttpServletResponse response = getHttpResponse();
		HttpServletRequest req = getHttpRequest();
		AuditManager auditManager = AuditManager.getInstance();
		// AUDIT UPDATE
		Integer auditId = null;
		String auditIdStr = req.getParameter(AuditManager.AUDIT_ID);
		if (auditIdStr == null) {
			logger.warn("Audit record id not specified! No operations will be performed");
		} else {
			logger.debug("Audit id = [" + auditIdStr + "]");
			auditId = new Integer(auditIdStr);
		}

		try {

			if (auditId != null) {
				auditManager.updateAudit(auditId, new Long(System.currentTimeMillis()), null, "EXECUTION_STARTED", null, null);
			}

			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			if (profile == null) {
				throw new SecurityException("User profile not found in session");
			}

			String documentId = (String) request.getAttribute("documentId");
			if (documentId == null) {
				throw new Exception("Document id missing!!");
			}
			logger.debug("Got parameter documentId = " + documentId);

			ContentServiceImplSupplier c = new ContentServiceImplSupplier();
			Content template = c.readTemplate(profile.getUserUniqueIdentifier().toString(), documentId, null);
			String templateFileName = template.getFileName();

			logger.debug("Template Read");

			if (templateFileName == null) {
				logger.warn("Template has no name");
				templateFileName = "";
			}

			httpUtils.setHeader(response,"Cache-Control", ""); // leave blank to avoid IE errors
			httpUtils.setHeader(response,"Pragma", ""); // leave blank to avoid IE errors
			httpUtils.setHeader(response,"content-disposition","inline; filename="+templateFileName);

			String mimeType = MimeUtils.getMimeType(templateFileName);
			logger.debug("Mime type is = " + mimeType);
			response.setContentType(mimeType);

			Base64.Decoder bASE64Decoder = Base64.getDecoder();
			byte[] templateContent = bASE64Decoder.decode(template.getContent());

			response.getOutputStream().write(templateContent);
			response.setContentLength(templateContent.length);
			response.getOutputStream().flush();
			response.getOutputStream().close();

			// AUDIT UPDATE
			auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_PERFORMED", null, null);

		} catch (Exception e) {
			logger.error("Exception", e);
			// AUDIT UPDATE
			auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e.getMessage(), null);
		} finally {
			logger.debug("OUT");
		}
	}

}
