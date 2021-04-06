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

import it.eng.spagobi.services.audit.AuditService;
import it.eng.spagobi.services.security.exceptions.SecurityException;

/**
 *
 * Proxy of Autit Service
 *
 */
public final class AuditServiceProxy extends AbstractServiceProxy {

	private static final String SERVICE_NAME = "Audit Service";

	private static final QName SERVICE_QNAME = new QName("http://audit.services.spagobi.eng.it/", "AuditService");

	private static Logger logger = Logger.getLogger(AuditServiceProxy.class);

	/**
	 * The Constructor.
	 *
	 * @param user    userId
	 * @param session Http Session
	 */
	public AuditServiceProxy(String user, HttpSession session) {
		super(user, session);
	}

	private AuditServiceProxy() {
		super();
	}

	private AuditService lookUp() throws SecurityException {
		try {
			AuditService service = null;

			if (serviceUrl != null) {
				URL serviceUrlWithWsdl = new URL(serviceUrl.toString() + "?wsdl");

				service = Service.create(serviceUrlWithWsdl, SERVICE_QNAME).getPort(AuditService.class);
			} else {
				service = Service.create(SERVICE_QNAME).getPort(AuditService.class);
			}
			return service;
		} catch (Exception e) {
			logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
			throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
		}
	}

	/**
	 * Log.
	 *
	 * @param id        id
	 * @param start     start time
	 * @param end       end time
	 * @param state     state
	 * @param message   message
	 * @param errorCode error code
	 *
	 * @return String
	 */
	public String log(String id, String start, String end, String state, String message, String errorCode) {
		logger.debug("IN");
		try {
			return lookUp().log(readTicket(), userId, id, start, end, state, message, errorCode);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}
}
