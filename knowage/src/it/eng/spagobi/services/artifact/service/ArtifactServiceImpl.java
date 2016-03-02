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
