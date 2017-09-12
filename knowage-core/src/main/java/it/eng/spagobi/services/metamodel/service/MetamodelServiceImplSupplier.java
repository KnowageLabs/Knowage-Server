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

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;

import javax.activation.DataHandler;

import org.apache.axis.attachments.ManagedMemoryDataSource;
import org.apache.log4j.Logger;

public class MetamodelServiceImplSupplier {
    static private Logger logger = Logger.getLogger(MetamodelServiceImplSupplier.class);

    /**
	 * return the metamodel by the name
	 * 
	 * @param id. The metamodel's name.
	 * @return the content of the metamodel.
	 */
    public DataHandler getMetamodelContentByName(String name){
    	logger.debug("IN.name:" + name);
    	DataHandler toReturn;
    	
    	if (name == null)
    	    return null;

    	// gets artifact metamodel from database
		try {
			IMetaModelsDAO metamodelsDAO = DAOFactory.getMetaModelsDAO();
			Content content = metamodelsDAO.loadActiveMetaModelContentByName(name);
			byte[] cont = content.getContent();
			ManagedMemoryDataSource mods =  new ManagedMemoryDataSource(new java.io.ByteArrayInputStream(cont), Integer.MAX_VALUE - 2,
			null, true);
			toReturn = new DataHandler(mods);		
			return toReturn;
		} catch (Exception e) {
			logger.error("The metamodel is not correctly returned", e);
			return null;
		} finally {
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
	public long getMetamodelContentLastModified(String name) {
		logger.debug("IN.name:" + name);
    	DataHandler toReturn;
    	
    	if (name == null) return -1;

    	// gets artifact metamodel from database
		try {
			IMetaModelsDAO metamodelsDAO = DAOFactory.getMetaModelsDAO();
			long lastModified = metamodelsDAO.getActiveMetaModelContentLastModified(name);
			return lastModified;
		} catch (Exception e) {
			logger.error("The metamodel is not correctly returned", e);
			return -1L;
		} finally {
			logger.debug("OUT");
		}			
	}
}
