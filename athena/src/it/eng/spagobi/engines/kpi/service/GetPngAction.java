/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
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

/**
 * 
 * @author Chiara Chiarelli
 * 
 */

public class GetPngAction extends AbstractHttpAction{
	
	private static transient Logger logger=Logger.getLogger(GetPngAction.class);

	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean serviceRequest, SourceBean serviceResponse)
	throws Exception {
		logger.debug("IN");
		freezeHttpResponse();

		HttpServletResponse response = getHttpResponse();
		ServletOutputStream out = response.getOutputStream();
		response.setContentType("image/gif");
		
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
	

		HttpServletRequest req = getHttpRequest();
		//String path = (String)serviceRequest.getAttribute("path");
		
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
		File fileToDelete = new File(path);
		if( fileToDelete.delete() ){ 
			logger.debug("File deleted");	
		}else{ 
			logger.error("File not correctly deleted");
		} 
		
		logger.debug("OUT");
	}
}
