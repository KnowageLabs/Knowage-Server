/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.chart.service;

/**   @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */


/**
 * This action provides to get the file.PNG content.
 * 
 */

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spagobi.engines.exporters.ChartExporter;
import it.eng.spagobi.monitoring.dao.AuditManager;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class GetPngAction extends AbstractHttpAction {

	private static transient Logger logger=Logger.getLogger(GetPngAction.class);
	private static String PARAM_OUTPUT_FORMAT="outputType";

	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean serviceRequest, SourceBean serviceResponse)
	throws Exception {
		logger.debug("IN");
		freezeHttpResponse();

		HttpServletResponse res = getHttpResponse();
		HttpServletRequest req = getHttpRequest();

		Integer auditId = null;
		String auditIdStr = req.getParameter("SPAGOBI_AUDIT_ID");
		AuditManager auditManager = AuditManager.getInstance();

		try{		
		// AUDIT UPDATE
		if (auditIdStr == null) {
		    logger.warn("Audit record id not specified! No operations will be performed");
		} else {
		    logger.debug("Audit id = [" + auditIdStr + "]");
		    auditId = new Integer(auditIdStr);
		}
		if (auditId != null) {
		    auditManager.updateAudit(auditId, new Long(System.currentTimeMillis()), null, "EXECUTION_STARTED", null,
			    null);
		}
		
		if(!(this.getErrorHandler().isOKBySeverity(EMFErrorSeverity.ERROR))){
			logger.error("There are errors into the error handler!!!");
			throw new Exception("errors in error handler!");
		}

		String outputType = (serviceRequest.getAttribute(PARAM_OUTPUT_FORMAT)==null)?"JPG":(String)serviceRequest.getAttribute(PARAM_OUTPUT_FORMAT);	
		String mimeType = "";
		//ChartExporter exporter = new ChartExporter();
		if ("PDF".equalsIgnoreCase(outputType)){
			//tmpFile=exporter.getChartPDF(profile, document);
			mimeType = "application/pdf";
		}
		else if ("JPG".equalsIgnoreCase(outputType)){
			//tmpFile=exporter.getChartJPG(document);
			mimeType = "image/gif";
		}
		
		HttpServletResponse response = getHttpResponse();
		ServletOutputStream out = response.getOutputStream();
	//	response.setContentType("image/gif");
		response.setContentType(mimeType);
		
		// Set Cache for print images
		java.text.SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");	    
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 10);	//Adding 10 minute to current date time
		Date date=cal.getTime(); 
		String dateString=dateFormat.format( date )+" GMT";
		logger.debug(dateString);	
		response.setDateHeader("Expires", date.getTime());
		//response.setHeader("Expires", "Sat, 6 May 2010 12:00:00 GMT");	
		response.setHeader("Cache-Control: max-age", "600");
		

		String filePath = (String)serviceRequest.getAttribute("path");

		String dir=System.getProperty("java.io.tmpdir");
		String path=dir+"/"+filePath+".png";

		FileInputStream fis=new FileInputStream(path);

		int avalaible = fis.available();   // Mi informo sul num. bytes.

		for(int i=0; i<avalaible; i++) {
			out.write(fis.read()); 
		}

		fis.close();
		out.flush();	
		out.close();

		// RIMUOVO FISICAMENTE IL FILE DAL REPOSITORY
		/*
		File fileToDelete = new File(path);
		if( fileToDelete.delete() ){ 
			logger.debug("File deleted");	
		}else{ 
			logger.error("File not correctle deleted");
		} 
		*/
	    // AUDIT UPDATE
		if(auditId!=null){
	    auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_PERFORMED", null,
		    null);
		}
		
		}
		catch (Exception e) {
			logger.error("Errors in retrieving the .png content");
		// Audit Update
			if(auditId!=null){
			auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e
				    .getMessage(), null);		
		   }
		return;    
		}
		
		logger.debug("OUT");


	}

}
