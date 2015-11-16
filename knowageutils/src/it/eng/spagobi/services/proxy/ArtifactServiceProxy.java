/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.proxy;

import it.eng.spagobi.services.artifact.bo.SpagoBIArtifact;
import it.eng.spagobi.services.artifact.stub.ArtifactServiceServiceLocator;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import javax.activation.DataHandler;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * 
 * Proxy of Artifact Service
 * 
 */
public final class ArtifactServiceProxy extends AbstractServiceProxy {

	static private final String SERVICE_NAME = "Artifact Service";

	static private Logger logger = Logger.getLogger(ArtifactServiceProxy.class);

	/**
	 * use this i engine context only.
	 * 
	 * @param user
	 *            user ID
	 * @param session
	 *            http session
	 */
	public ArtifactServiceProxy(String user, HttpSession session) {
		super(user, session);
		if (user == null)
			logger.error("User ID IS NULL....");
		if (session == null)
			logger.error("HttpSession IS NULL....");
	}

	private ArtifactServiceProxy() {
		super();
	}

	private it.eng.spagobi.services.artifact.stub.ArtifactService lookUp()
			throws SecurityException {
		try {
			ArtifactServiceServiceLocator locator = new ArtifactServiceServiceLocator();
			it.eng.spagobi.services.artifact.stub.ArtifactService service = null;
			if (serviceUrl != null) {
				service = locator.getArtifactService(serviceUrl);
			} else {
				service = locator.getArtifactService();
			}
			return service;
		} catch (ServiceException e) {
			logger.error("Impossible to locate [" + SERVICE_NAME + "] at ["
					+ serviceUrl + "]");
			throw new SecurityException("Impossible to locate [" + SERVICE_NAME
					+ "] at [" + serviceUrl + "]", e);
		}
	}

	/**
	 * Loads artifact by name and type.
	 * 
	 * @param name
	 *            String
	 * @param type
	 *            String
	 * 
	 * @return Artifact
	 */
	public DataHandler getArtifactContentByNameAndType(String name, String type) {
		logger.debug("IN.name=" + name);
		logger.debug("IN.type=" + type);
		if (name == null || name.length() == 0) {
			logger.error("Artifact name is NULL");
			return null;
		}
		if (type == null || type.length() == 0) {
			logger.error("Artifact type is NULL");
			return null;
		}
		try {
			return lookUp().getArtifactContentByNameAndType(readTicket(),
					userId, name, type);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Loads artifact by id.
	 * 
	 * @param name
	 *            String
	 * @param type
	 *            String
	 * 
	 * @return Artifact
	 */
	public DataHandler getArtifactContentById(Integer id) {
		logger.debug("IN.id=" + id);
		if (id == null) {
			logger.error("Artifact id is NULL");
			return null;
		}
		try {
			return lookUp().getArtifactContentById(readTicket(), userId, id);
		} catch (Exception e) {
			logger.error("Error during service execution", e);

		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Loads artifacts by type.
	 * 
	 * @param type
	 *            String
	 * 
	 * @return Artifact
	 */
	
	
	/**
	 * Loads artifacts by type.
	 *  
	 * @param type String
	 * @return the array of artifact of the given type
	 */
	public SpagoBIArtifact[] getArtifactsByType( String type) {
		logger.debug("IN.type=" + type);
		if (type == null) {
			logger.error("Artifact type in input is NULL");
			return null;
		}
		try {
			return lookUp().getArtifactsByType(readTicket(), userId, type);
		} catch (Exception e) {
			logger.error("Error during service execution", e);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}
	
}
