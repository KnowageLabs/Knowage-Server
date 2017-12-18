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

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.artifact.bo.SpagoBIArtifact;
import it.eng.spagobi.services.artifact.stub.ArtifactServiceServiceLocator;
import it.eng.spagobi.services.metamodel.stub.MetamodelServiceServiceLocator;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import javax.activation.DataHandler;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * 
 * Proxy of Metamodel Service
 * 
 */
public final class MetamodelServiceProxy extends AbstractServiceProxy {

	static private final String SERVICE_NAME = "Metamodel Service";

	static private Logger logger = Logger.getLogger(MetamodelServiceProxy.class);

	/**
	 * use this i engine context only.
	 * 
	 * @param user
	 *            user ID
	 * @param session
	 *            http session
	 */
	public MetamodelServiceProxy(String user, HttpSession session) {
		super(user, session);
		if (user == null)
			logger.error("User ID IS NULL....");
		if (session == null)
			logger.error("HttpSession IS NULL....");
	}

	public MetamodelServiceProxy(String user,String secureAttributes,String serviceUrlStr,String spagoBiServerURL,String token, String pass) {
		super( user,secureAttributes,serviceUrlStr,spagoBiServerURL,token, pass);
	}
	
	
	private MetamodelServiceProxy() {
		super();
	}

	private it.eng.spagobi.services.metamodel.stub.MetamodelService lookUp()
			throws SecurityException {
		try {
			MetamodelServiceServiceLocator locator = new MetamodelServiceServiceLocator();
			it.eng.spagobi.services.metamodel.stub.MetamodelService service = null;
			if (serviceUrl != null) {
				service = locator.getMetamodelService(serviceUrl);
			} else {
				service = locator.getMetamodelService();
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
	 * Loads metamodel by name.
	 * 
	 * @param metamodelName
	 * 
	 * @return Metamodel
	 */
	public DataHandler getMetamodelContentByName(String metamodelName) {
		DataHandler serviceResponse;
		
		logger.debug("IN");
		
		serviceResponse = null;
		try {
			Assert.assertTrue(StringUtilities.isNotEmpty(metamodelName), "Input parameter [name] cannot be null when invoking method [getMetamodelContentByName] of service [" + SERVICE_NAME + "]");
			serviceResponse =  lookUp().getMetamodelContentByName(readTicket(), userId, metamodelName);
		} catch (Throwable se) {
			throw new SpagoBIRuntimeException("An unexpected error occerd while invoking method [getMetamodelContentByName] of service [" + SERVICE_NAME + "]", se);
		} finally {
			logger.debug("OUT");
		}
		
		return serviceResponse;
	}
	
	/**
	 * Returns the last modification date of the metamodel specified
	 * 
	 * @param metamodelName
	 * 
	 * @return the last modification date of the metamodel specified
	 */
	public long getMetamodelContentLastModified(String metamodelName) {
		long serviceResponse;
		
		logger.debug("IN");
		
		serviceResponse = -1;
		try {
			Assert.assertTrue(StringUtilities.isNotEmpty(metamodelName), "Input parameter [name] cannot be null when invoking method [getMetamodelContentByName] of service [" + SERVICE_NAME + "]");
			serviceResponse =  lookUp().getMetamodelContentLastModified(readTicket(), userId, metamodelName);
		} catch (Throwable se) {
			throw new SpagoBIRuntimeException("An unexpected error occerd while invoking method [getMetamodelContentByName] of service [" + SERVICE_NAME + "]", se);
		} finally {
			logger.debug("OUT");
		}
		
		return serviceResponse;
	}
}
