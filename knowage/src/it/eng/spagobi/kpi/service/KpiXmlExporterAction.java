/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.exporters.KpiExporter;
import it.eng.spagobi.engines.kpi.bo.KpiResourceBlock;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;

public class KpiXmlExporterAction extends AbstractHttpAction {

	private static transient Logger logger=Logger.getLogger(KpiXmlExporterAction.class);

	/**
	 * This action is called by the user who wants to export the result of a Kpi in XML
	 * 
	 */

	public void service(SourceBean serviceRequest, SourceBean serviceResponse)
	throws Exception {

		File tmpFile=null;
		logger.debug("IN");
		HttpServletRequest httpRequest = getHttpRequest();
		HttpSession session=httpRequest.getSession();

		this.freezeHttpResponse();

		try{
			// get KPI result
			List<KpiResourceBlock> listKpiBlocks=(List<KpiResourceBlock>)session.getAttribute("KPI_BLOCK");
			String title = (String)session.getAttribute("TITLE");
			String subtitle = (String)session.getAttribute("SUBTITLE");
			if(title==null)title = "";
			if(subtitle==null)subtitle = "";
			
			// recover BiObject Name
			Object idObject=serviceRequest.getAttribute(SpagoBIConstants.OBJECT_ID);
			if(idObject==null){
				logger.error("Document id not found");
				return;
			}

			Integer id=Integer.valueOf(idObject.toString());
			BIObject document=DAOFactory.getBIObjectDAO().loadBIObjectById(id);
			String docName=document.getName();

			//Recover user Id
			HashedMap parameters=new HashedMap();
			String userId=null;
			Object userIdO=serviceRequest.getAttribute("user_id");	
			if(userIdO!=null)userId=userIdO.toString();
			
			it.eng.spagobi.engines.exporters.KpiExporter exporter=new KpiExporter();
			tmpFile=exporter.getKpiExportXML(listKpiBlocks, document, userId);

			String outputType = "XML";

			String mimeType = "text/xml";

			logger.debug("Report exported succesfully");

			HttpServletResponse response = getHttpResponse();
			response.setContentType(mimeType);							
			response.setHeader("Content-Disposition", "filename=\"report." + outputType + "\";");
			response.setContentLength((int) tmpFile.length());

			BufferedInputStream in = new BufferedInputStream(new FileInputStream(tmpFile));
			int b = -1;
			while ((b = in.read()) != -1) {
				response.getOutputStream().write(b);
			}
			response.getOutputStream().flush();
			in.close();
			logger.debug("OUT");

		} catch(Throwable e) {
			logger.error("An exception has occured", e);
			throw new Exception(e);
		} finally {

			tmpFile.delete();

		}
	}
}
