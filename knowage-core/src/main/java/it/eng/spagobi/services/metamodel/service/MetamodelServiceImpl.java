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
package it.eng.spagobi.services.metamodel.service;

import it.eng.spagobi.services.common.AbstractServiceImpl;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class MetamodelServiceImpl extends AbstractServiceImpl {

	static private Logger logger = Logger.getLogger(MetamodelServiceImpl.class);

	/**
	 * Instantiates a new metamodel service impl.
	 */
	public MetamodelServiceImpl() {
		super();
	}

	/**
	 * return the metamodel by the id
	 * @param token. The token.
	 * @param user. The user.
	 * @param id. The metamodel's name.
	 * @return the content of the metamodel.
	 */
    public DataHandler getMetamodelContentByName(String token, String user, String name){
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.metamodel.getMetamodelContentByName");
		try {
			DataHandler dataHandler = null;
			validateTicket(token, user);
			this.setTenantByUserId(user);
			MetamodelServiceImplSupplier supplier = new MetamodelServiceImplSupplier();			
			dataHandler = supplier.getMetamodelContentByName(name);
			return dataHandler;
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
	 * Returns the last modification date of the metamodel specified
	 * 
	 * @param token The token.
	 * @param user The user.
	 * @param name  The metamodel's name.
	 * 
	 * @return the last modification date of the metamodel specified
	 */
	public long getMetamodelContentLastModified(String token, String user, String name) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.metamodel.getMetamodelContentByName");
		try {
			long lastModified = -1;
			validateTicket(token, user);
			this.setTenantByUserId(user);
			MetamodelServiceImplSupplier supplier = new MetamodelServiceImplSupplier();			
			lastModified = supplier.getMetamodelContentLastModified(name);
			return lastModified;
		} catch (Exception e) {
			logger.error("Exception", e);
			return -1;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}				
	}
}
