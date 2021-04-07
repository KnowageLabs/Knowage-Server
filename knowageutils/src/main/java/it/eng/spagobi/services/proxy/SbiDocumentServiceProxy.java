/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.services.proxy;

import java.net.URL;

import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;

import it.eng.spagobi.services.sbidocument.SbiDocumentService;
import it.eng.spagobi.services.sbidocument.bo.SpagobiAnalyticalDriver;
import it.eng.spagobi.services.security.exceptions.SecurityException;

public class SbiDocumentServiceProxy extends AbstractServiceProxy   {

	private static final String SERVICE_NAME = "SbiDocumentService";

	private static final QName SERVICE_QNAME = new QName("http://sbidocument.services.spagobi.eng.it/", "SbiDocumentService");

	private static Logger logger = Logger.getLogger(SbiDocumentServiceProxy.class);

	public SbiDocumentServiceProxy(String user,HttpSession session) {
		super( user,session);
		if (user==null) logger.error("User ID IS NULL....");
		if (session==null) logger.error("HttpSession IS NULL....");
	}

	private SbiDocumentServiceProxy() {
		super ();
	}

	private SbiDocumentService lookUp() throws SecurityException {
		try {
			SbiDocumentService service = null;

			if (serviceUrl!=null ){
				URL serviceUrlWithWsdl = new URL(serviceUrl.toString() + "?wsdl");

				service = Service.create(serviceUrlWithWsdl, SERVICE_QNAME).getPort(SbiDocumentService.class);
			}else {
				service = Service.create(SERVICE_QNAME).getPort(SbiDocumentService.class);
			}
			return service;
		} catch (Exception e) {
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