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
package it.eng.spagobi.services.proxy;

import java.net.URL;
import java.util.HashMap;

import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;

import it.eng.spagobi.services.common.ParametersWrapper;
import it.eng.spagobi.services.execute.DocumentExecuteService;
import it.eng.spagobi.services.security.exceptions.SecurityException;

/**
 * @author Angelo Bernabei angelo.bernabei@eng.it
 */
public class DocumentExecuteServiceProxy extends AbstractServiceProxy {

	private static final String SERVICE_NAME = "DocumentExecute Service";

	private static final QName SERVICE_QNAME = new QName("http://documentexecute.services.spagobi.eng.it/", "DocumentExecuteService");

	private static Logger logger = Logger.getLogger(DocumentExecuteServiceProxy.class);

	/**
	 * The Constructor.
	 *
	 * @param user    userId
	 * @param session Http Session
	 */
	public DocumentExecuteServiceProxy(String user, HttpSession session) {
		super(user, session);
	}

	private DocumentExecuteServiceProxy() {
		super();
	}

	/**
	 * LookUp Method
	 *
	 * @return
	 * @throws SecurityException
	 */
	private DocumentExecuteService lookUp() throws SecurityException {
		try {
			DocumentExecuteService service = null;

			if (serviceUrl != null) {
				URL serviceUrlWithWsdl = new URL(serviceUrl.toString() + "?wsdl");

				service = Service.create(serviceUrlWithWsdl, SERVICE_QNAME).getPort(DocumentExecuteService.class);
			} else {
				service = Service.create(SERVICE_QNAME).getPort(DocumentExecuteService.class);
			}
			return service;
		} catch (Exception e) {
			logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
			throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
		}
	}

	/**
	 * Return the image of a Chart
	 *
	 * @param documentLabel
	 * @param parameters
	 * @return
	 */
	public byte[] executeChart(String documentLabel, HashMap parameters) {
		logger.debug("IN.documentLabel=" + documentLabel);
		if (documentLabel == null || documentLabel.length() == 0) {
			logger.error("documentLabel is NULL");
			return null;
		}
		try {
			ParametersWrapper _parameters = new ParametersWrapper();
			_parameters.setMap(parameters);
			return lookUp().executeChart(readTicket(), userId, documentLabel, _parameters);
		} catch (Exception e) {
			logger.error("Error during Service LookUp", e);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	public java.lang.String getKpiValueXML(java.lang.Integer kpiValueID) {
		logger.debug("IN.kpiValueID=" + kpiValueID);
		if (kpiValueID == null) {
			logger.error("kpiValueID is NULL");
			return null;
		}
		try {
			return lookUp().getKpiValueXML(readTicket(), userId, kpiValueID);
		} catch (Exception e) {
			logger.error("Error during Service LookUp", e);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}

}
