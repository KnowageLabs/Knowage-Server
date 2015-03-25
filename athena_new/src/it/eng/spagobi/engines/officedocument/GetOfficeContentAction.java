/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.officedocument;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.content.service.ContentServiceImplSupplier;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.utilities.mime.MimeUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;

public class GetOfficeContentAction extends AbstractHttpAction {

    private static transient Logger logger=Logger.getLogger(GetOfficeContentAction.class);
    
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean responseSb) throws Exception {
	    logger.debug("IN");
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
			    auditManager.updateAudit(auditId, new Long(System.currentTimeMillis()), null, "EXECUTION_STARTED", null,
				    null);
			}
			
			SessionContainer sessionContainer = this.getRequestContainer().getSessionContainer();
			SessionContainer permanentContainer = sessionContainer.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile) permanentContainer.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			if (profile == null) {
				throw new SecurityException("User profile not found in session");
			}
			
			String documentId = (String)request.getAttribute("documentId");
			if (documentId == null) 
				throw new Exception("Document id missing!!");
			logger.debug("Got parameter documentId = " + documentId);
			
			ContentServiceImplSupplier c = new ContentServiceImplSupplier();
			Content template = c.readTemplate(profile.getUserUniqueIdentifier().toString(), documentId, null);
			String templateFileName = template.getFileName();
	
			logger.debug("Template Read");
	
			if(templateFileName==null){
				logger.warn("Template has no name");
				templateFileName="";
			}
			
			response.setHeader("Cache-Control: ",""); // leave blank to avoid IE errors
			response.setHeader("Pragma: ",""); // leave blank to avoid IE errors 
			response.setHeader("content-disposition","inline; filename="+templateFileName);	
			
			String mimeType = MimeUtils.getMimeType(templateFileName);
			logger.debug("Mime type is = " + mimeType);
			response.setContentType(mimeType);

			BASE64Decoder bASE64Decoder = new BASE64Decoder();
			byte[] templateContent = bASE64Decoder.decodeBuffer(template.getContent());
			
			response.getOutputStream().write(templateContent);
			response.setContentLength(templateContent.length);
			response.getOutputStream().flush();	
			response.getOutputStream().close();
			
		    // AUDIT UPDATE
		    auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_PERFORMED", null,
			    null);
		
		} catch (Exception e) {
		    logger.error("Exception", e);
		    // AUDIT UPDATE
		    auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e
			    .getMessage(), null);
		} finally {
		    logger.debug("OUT");
		}
	}

}
