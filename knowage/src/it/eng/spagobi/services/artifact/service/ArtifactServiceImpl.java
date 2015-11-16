/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.artifact.service;

import it.eng.spagobi.services.artifact.bo.SpagoBIArtifact;
import it.eng.spagobi.services.common.AbstractServiceImpl;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class ArtifactServiceImpl extends AbstractServiceImpl {

	static private Logger logger = Logger.getLogger(ArtifactServiceImpl.class);

	/**
	 * Instantiates a new artifact service impl.
	 */
	public ArtifactServiceImpl() {
		super();
	}

	/**
	 * return the artifact by name and type
	 * @param token. The token.
	 * @param user. The user.
	 * @param name. The artifact's name.
	 * @param type. The artifact's type.
	 * @return the content of the artifact.
	 */
	public DataHandler getArtifactContentByNameAndType(String token,String user, String name, String type){
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.artifact.getArtifactContentByNameAndType");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			ArtifactServiceImplSupplier supplier = new ArtifactServiceImplSupplier();			
			return supplier.getArtifactContentByNameAndType(name, type);
		} catch (Exception e) {
			logger.error("Exception", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}				
	}

	/**
	 * return the artifact by the id
	 * @param token. The token.
	 * @param user. The user.
	 * @param id. The artifact's id.
	 * @return the content of the artifact.
	 */
    public DataHandler getArtifactContentById(String token, String user, Integer id){
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.artifact.getArtifactContentById");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			ArtifactServiceImplSupplier supplier = new ArtifactServiceImplSupplier();			
			return supplier.getArtifactContentById(id);
		} catch (Exception e) {
			logger.error("Exception", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}				
    }
    
	/**
	 * return the artifacts list of the given type
	 * @param token. The token.
	 * @param user. The user.
	 * @param type. The artifact's type.
	 * @return the list of the artifacts of the given type.
	 */
    public SpagoBIArtifact[] getArtifactsByType(String token, String user, String type){
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.artifact.getArtifactsByType");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			ArtifactServiceImplSupplier supplier = new ArtifactServiceImplSupplier();			
			return supplier.getArtifactsByType(type);
		} catch (Exception e) {
			logger.error("An error occurred while getting artifacts of type [" + type + "]", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}				
    }

}
