/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class GetThresholdImageAction extends AbstractHttpAction {

	private static transient Logger logger=Logger.getLogger(GetThresholdImageAction.class);
	private static String PARAM_OUTPUT_FORMAT="outputType";

	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) throws Exception
	 {
		logger.debug("IN");
		freezeHttpResponse();
		try {
		HttpServletResponse res = getHttpResponse();
		HttpServletRequest req = getHttpRequest();
		
		if(!(this.getErrorHandler().isOKBySeverity(EMFErrorSeverity.ERROR))){
			logger.error("There are errors into the error handler!!!");
			throw new Exception("errors in error handler!");
		}

		String mimeType = "image/gif";
		
		HttpServletResponse response = getHttpResponse();
		ServletOutputStream out = response.getOutputStream();
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
		
		SingletonConfig configSingleton = SingletonConfig.getInstance();
		String pathh = configSingleton.getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
		String filePath= SpagoBIUtilities.readJndiResource(pathh);
		filePath += "/kpi_images/";
		String dirName = (String)serviceRequest.getAttribute("dirName");
		filePath += dirName+"/";
		logger.debug("filePath="+filePath);
		String fileName = (String)serviceRequest.getAttribute("fileName");
		String path=filePath+fileName+".png";
		logger.debug("path:"+path);

		FileInputStream fis;
		
			fis = new FileInputStream(path);
		    int avalaible = fis.available();   // Mi informo sul num. bytes.

			for(int i=0; i<avalaible; i++) {
				out.write(fis.read()); 
			}

			fis.close();
			out.flush();	
			out.close();
			
		} catch (FileNotFoundException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
		
		logger.debug("OUT");

	}

}
