/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.tools.dataset.bo.DataSetParameterItem;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.bo.IDataSet;

import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class GetDatasetResultAction extends AbstractHttpAction {

    private static transient Logger logger = Logger.getLogger(GetDatasetResultAction.class);

    /* (non-Javadoc)
     * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
     */
    public void service(SourceBean requestSB, SourceBean responseSB) throws Exception {
	logger.debug("IN");

	freezeHttpResponse();
	HttpServletRequest request = getHttpRequest();
	HttpServletResponse response = getHttpResponse();

	// AUDIT UPDATE
	Integer auditId = null;
	String auditIdStr = request.getParameter("SPAGOBI_AUDIT_ID");
	if (auditIdStr == null) {
	    logger.warn("Audit record id not specified! No operations will be performed");
	} else {
	    logger.debug("Audit id = [" + auditIdStr + "]");
	    auditId = new Integer(auditIdStr);
	}
	AuditManager auditManager = AuditManager.getInstance();
	if (auditId != null) {
	    auditManager.updateAudit(auditId, new Long(System.currentTimeMillis()), null, "EXECUTION_STARTED", null,
		    null);
	}
	IEngUserProfile profile = null;
	try {
	    RequestContainer reqCont = RequestContainer.getRequestContainer();
	    SessionContainer sessCont = reqCont.getSessionContainer();
	    SessionContainer permSess = sessCont.getPermanentContainer();
	    profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	    if (profile == null) {
		    logger.error("User profile not found");
		    throw new SecurityException("User profile not found");
	    }

	    String documentId = request.getParameter("documentId");
	    logger.debug("got parameter documentId=" + documentId);
   
	    
	  

		String result="";
			String dataName = (String)request.getParameter("datasetid");
			if((dataName==null) || dataName.trim().equals("")) {
				response.getOutputStream().write(createErrorMsg(10, "Param dataset not found"));
				response.getOutputStream().flush();
	 		return;
			}
			HashMap parameters=new HashMap();

			  try{
			// if dataset needs parameter I must recover them from request
			 IDataSet dataSet=DAOFactory.getDataSetDAO().loadDataSetById(Integer.valueOf(dataName)); 
			 if(dataSet!=null){	
			 	Object parametersO=dataSet.getParameters();
			 	if(parametersO!=null){
			 		String parametersXML=parametersO.toString();
			 	DataSetParametersList dsList=new DataSetParametersList(parametersXML);
			 	for (Iterator iterator = dsList.getItems().iterator(); iterator.hasNext();) {
					DataSetParameterItem item = (DataSetParameterItem) iterator.next();
					String name=item.getName();
					// check if parameter is in request
					if(requestSB.getAttribute(name)!=null){
						String value=((Object)requestSB.getAttribute(name)).toString();						
						logger.debug("Processed parameter " + name + " with value -" + value +"-");
						parameters.put(name, value);
						
					}
					
				}
			 	}
					}
			
	    } catch (Exception e) {
	    	logger.error("Exception while recovering attributes needed by dataset", e);
	    	response.getOutputStream().write(createErrorMsg(10, "Error while recovering attributes needed by dataset"));
			response.getOutputStream().flush();	    
			return;
	    }
			 
			
			
			String res=DataSetAccessFunctions.getDataSetResultFromId(profile, dataName,parameters);
			if (res!=null){
				SourceBean sbRows=SourceBean.fromXMLString(res);
				//SourceBean myResultBean=(SourceBean)sbRows.getAttribute("ROW");
				result=sbRows.toString();
			}
	    
	    // replace special characters
	    result = result.replaceAll("&lt;", "<");
	    result = result.replaceAll("&gt;", ">");	
	    //result = result.replaceAll("\"", "'");
	    result= result.replaceAll("<ROWS", "<rows");
	    result= result.replaceAll("<ROW", "<row");
	    logger.debug("result: " + result);
	    
	    // write the result into response
	    response.getOutputStream().write(result.getBytes());
	    response.getOutputStream().flush();
	    // AUDIT UPDATE
	    auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_PERFORMED", null,
		    null);

	} catch (Exception e) {
	    logger.error("Exception", e);
	    // AUDIT UPDATE
	    auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e
		    .getMessage(), null);
    	response.getOutputStream().write(createErrorMsg(10, "Error"));
		response.getOutputStream().flush();	    
		return;
	} finally {
	    logger.debug("OUT");
	}
    }


    private byte[] createErrorMsg(int code, String message) {
	String response = "<response><error><code>" + code + "</code>" + "<message>" + message
		+ "</message></error></response>";
	return response.getBytes();
    }
	
	
	
	
	
}
