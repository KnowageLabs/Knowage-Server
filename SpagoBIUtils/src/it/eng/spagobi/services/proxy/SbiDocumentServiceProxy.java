/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.proxy;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.sbidocument.bo.SpagobiAnalyticalDriver;
import it.eng.spagobi.services.sbidocument.stub.SbiDocumentServiceServiceLocator;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class SbiDocumentServiceProxy extends AbstractServiceProxy   {

	static private final String SERVICE_NAME = "SbiDocumentService";

	static private Logger logger = Logger.getLogger(SbiDocumentServiceProxy.class);



	public SbiDocumentServiceProxy(String user,HttpSession session) {
		super( user,session);
		if (user==null) logger.error("User ID IS NULL....");
		if (session==null) logger.error("HttpSession IS NULL....");
	}

	private SbiDocumentServiceProxy() {
		super ();
	}     

	private it.eng.spagobi.services.sbidocument.stub.SbiDocumentService lookUp() throws SecurityException {
		try {
			SbiDocumentServiceServiceLocator locator = new SbiDocumentServiceServiceLocator();
			it.eng.spagobi.services.sbidocument.stub.SbiDocumentService service = null;
			if (serviceUrl!=null ){
				service = locator.getSbiDocumentService(serviceUrl);		
			}else {
				service = locator.getSbiDocumentService();		
			}
			return service;
		} catch (ServiceException e) {
			logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
			throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
		}
	}
  
  
  
	public SpagobiAnalyticalDriver[] getDocumentAnalyticalDrivers(java.lang.Integer id, java.lang.String language, java.lang.String country) throws java.rmi.RemoteException{
		SpagobiAnalyticalDriver[] array = null;

		logger.debug("IN.id="+id);

		if (id==null){
			logger.error("id is NULL");
			return null;
		}	
		try {
			array = lookUp().getDocumentAnalyticalDrivers(readTicket(), userId, id, language, country);
		} catch (Exception e) {
			logger.error("Error during Service LookUp",e);
		}finally{
			logger.debug("OUT");
		}
		return array;
	}
  
	
	public String getDocumentAnalyticalDriversJSON(java.lang.Integer id, java.lang.String language, java.lang.String country) throws java.rmi.RemoteException{
		String jSon = null;

		logger.debug("IN.id="+id);

		if (id==null){
			logger.error("id is NULL");
			return null;
		}	
		try {
			jSon = lookUp().getDocumentAnalyticalDriversJSON(readTicket(), userId, id, language, country);
		} catch (Exception e) {
			logger.error("Error during Service LookUp",e);
		}finally{
			logger.debug("OUT");
		}
		return jSon;
	}
  
  

  
  
}